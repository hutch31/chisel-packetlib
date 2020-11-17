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
  val count = RegInit(init=0.U(16.W))
  val s_idle :: s_packet :: Nil = Enum(2)
  val state = RegInit(init=s_idle)
  io.sendPacket <> info.io.enq

  info.io.deq.ready := false.B
  io.packetData.valid := false.B
  io.packetData.bits := 0.asTypeOf(new PacketData(wordSize))

  switch (state) {
    is (s_idle) {
      when (info.io.deq.valid) {
        count := 0.U
        state := s_packet
      }
    }

    is (s_packet) {
      for (i <- 0 to wordSize-1) {
        io.packetData.bits.data(i) := count + i.U
      }
      when (count + wordSize.U >= info.io.deq.bits.length) {
        io.packetData.bits.count := info.io.deq.bits.length - count - 1.U
        when (info.io.deq.bits.packetGood) {
          io.packetData.bits.code := packetGoodEop
        }.otherwise {
          io.packetData.bits.code := packetBadEop
        }
      }.otherwise {
        io.packetData.bits.count := 0.U
        when (count === 0.U) {
          io.packetData.bits.code := packetBadEop
        }.otherwise {
          io.packetData.bits.code := packetBody
        }
        count := count + wordSize.U
      }
    }
  }
}
