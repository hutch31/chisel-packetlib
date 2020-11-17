package packet.packetbuf

import chisel.lib.packet._
import chisel3._
import chisel3.util.ImplicitConversions.intToUInt
import chisel3.util._
import packet.packet._

class PacketRequest extends Bundle {
  val length = UInt(16.W)
  val pid = UInt(16.W)
  val packetGood = Bool()
}

class PacketSender(wordSize : Int) extends Module {
  val io = IO(new Bundle {
    val packetData = Decoupled(new PacketData(wordSize))
    val sendPacket = Flipped(Decoupled(new PacketRequest))
  })
  // latch incoming packet send requests
  val info = Module(new Queue(new PacketRequest, 4))
  val txq = Module(new Queue(new PacketData(wordSize), 2))
  val count = Reg(UInt(16.W))
  val s_idle :: s_packet :: Nil = Enum(2)
  val state = RegInit(init=s_idle)
  io.sendPacket <> info.io.enq
  io.packetData <> txq.io.deq

  info.io.deq.ready := false.B
  txq.io.enq.valid := false.B
  txq.io.enq.bits := 0.asTypeOf(new PacketData(wordSize))

  switch (state) {
    is (s_idle) {
      when (info.io.deq.valid) {
        count := 0.U
        state := s_packet
      }
    }

    is (s_packet) {
      txq.io.enq.valid := true.B
      when(txq.io.enq.ready) {
        for (i <- 0 to wordSize - 1) {
          txq.io.enq.bits.data(i) := count + i.U
        }
        when(count + wordSize.U >= info.io.deq.bits.length) {
          txq.io.enq.bits.count := info.io.deq.bits.length - count - 1.U
          info.io.deq.ready := true.B
          state := s_idle

          when(info.io.deq.bits.packetGood) {
            txq.io.enq.bits.code.code := packetGoodEop
          }.otherwise {
            txq.io.enq.bits.code.code := packetBadEop
          }
        }.otherwise {
          txq.io.enq.bits.count := 0.U
          when(count === 0.U) {
            txq.io.enq.bits.code.code := packetSop
          }.otherwise {
            txq.io.enq.bits.code.code := packetBody
          }
          count := count + wordSize.U
        }
      }
    }
  }
}
