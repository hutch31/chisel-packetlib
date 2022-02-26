package packet.packetbuf

import packet._
import chisel3._
import chisel3.util.ImplicitConversions.intToUInt
import chisel3.util._

class PacketRequest extends Bundle {
  val length = UInt(16.W)
  val pid = UInt(16.W)
  val src = UInt(8.W)
  val dst = UInt(8.W)
  val packetGood = Bool()
}

class PacketSender(wordSize : Int, ReadClients : Int, reqSize : Int = 4) extends Module {
  val io = IO(new Bundle {
    val packetData = Decoupled(new PacketData(wordSize))
    val sendPacket = Flipped(Decoupled(new PacketRequest))
    val destIn = ValidIO(new RoutingResult(ReadClients))
  })
  // latch incoming packet send requests
  val info = Module(new Queue(new PacketRequest, reqSize))
  val txq = Module(new Queue(new PacketData(wordSize), 2))
  val count = Reg(UInt(16.W))
  val s_idle :: s_packet :: Nil = Enum(2)
  val state = RegInit(init=s_idle)
  io.sendPacket <> info.io.enq
  io.packetData <> txq.io.deq

  info.io.deq.ready := false.B
  txq.io.enq.valid := false.B
  txq.io.enq.bits := 0.asTypeOf(new PacketData(wordSize))
  txq.io.enq.bits.code.code := packet.packetBody

  io.destIn.valid := false.B
  io.destIn.bits.dest := info.io.deq.bits.dst

  switch (state) {
    is (s_idle) {
      when (info.io.deq.valid && txq.io.count === 0.U) {
        count := 0.U
        state := s_packet
        io.destIn.valid := true.B
      }
    }

    is (s_packet) {
      txq.io.enq.valid := true.B
      when(txq.io.enq.ready) {
        when (count === 0.U) {
          txq.io.enq.bits.code.code := packet.packetSop
          for (i <- 0 to wordSize - 1) {
            i match {
              case 0 => txq.io.enq.bits.data(i) := info.io.deq.bits.pid(15,8)
              case 1 => txq.io.enq.bits.data(i) := info.io.deq.bits.pid(7, 0)
              case 2 => txq.io.enq.bits.data(i) := info.io.deq.bits.src
              case 3 => txq.io.enq.bits.data(i) := info.io.deq.bits.dst
              case _ => txq.io.enq.bits.data(i) := count + i.U
            }
          }
        }.otherwise {
          for (i <- 0 to wordSize - 1) {
            txq.io.enq.bits.data(i) := count + i.U
          }
        }

        when(count + wordSize.U >= info.io.deq.bits.length) {
          txq.io.enq.bits.count := info.io.deq.bits.length - count - 1.U
          info.io.deq.ready := true.B
          state := s_idle

          when(info.io.deq.bits.packetGood) {
            txq.io.enq.bits.code.code := packet.packetGoodEop
          }.otherwise {
            txq.io.enq.bits.code.code := packet.packetBadEop
          }
        }.otherwise {
          txq.io.enq.bits.count := 0.U
          count := count + wordSize.U
        }
      }
    }
  }
}

class PacketReceiver(wordSize : Int, senders: Int, reqSize : Int = 8) extends Module {
  val io = IO(new Bundle {
    val packetData = Flipped(Decoupled(new PacketData(wordSize)))
    val sendPacket = Flipped(Decoupled(new PacketRequest))
    val error = Output(Bool())
    val expQueueEmpty = Output(Bool())
  })
  val pidQueue = for (i <- 0 until senders) yield Module(new Queue(new PacketRequest, reqSize))
  val queueData = Wire(Vec(senders, new PacketRequest))
  val queueReady = Wire(UInt(8.W))
  val pidQueueValid = Wire(UInt(senders.W))
  val packetId = Wire(UInt(16.W))
  val packetSrc = Wire(UInt(8.W))
  val packetDst = Wire(UInt(8.W))
  val queueEmpty = Wire(Vec(senders, Bool()))

  io.packetData.ready := true.B
  io.expQueueEmpty := Cat(queueEmpty).andR()

  io.sendPacket.ready := true.B
  for (i <- 0 until senders) {
    pidQueue(i).io.enq.valid := pidQueueValid(i)
    pidQueue(i).io.deq.ready := queueReady(i)
    pidQueue(i).io.enq.bits := io.sendPacket.bits
    queueData(i) := pidQueue(i).io.deq.bits
    queueEmpty(i) := pidQueue(i).io.count === 0.U
  }
  when (io.sendPacket.valid) {
    pidQueueValid := 1.U << io.sendPacket.bits.src
  }.otherwise {
    pidQueueValid := 0.U
  }

  packetId := Cat(io.packetData.bits.data(0), io.packetData.bits.data(1))
  packetSrc := io.packetData.bits.data(2)
  packetDst := io.packetData.bits.data(3)

  when (io.packetData.valid && io.packetData.bits.code.isSop()) {
    queueReady := 1.U << packetSrc
    when (packetId =/= queueData(packetSrc).pid || packetDst =/= queueData(packetSrc).dst) {
      io.error := true.B
    }.otherwise {
      io.error := false.B
    }
  }.otherwise {
    io.error := false.B
    queueReady := 0.U
  }
}

