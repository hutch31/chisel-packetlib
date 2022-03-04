package packet.axis

import chisel.lib.dclib._
import chisel3._
import chisel3.util._
import packet._

class GenAxiToPkt(size: Int, user : Int = 0) extends Module {
  val io = IO(new Bundle {
    val in = Flipped(new PktAxiStreaming(size, user))
    val out = Decoupled(new PacketData(size))
    val userOut = if(user > 0) Some(Output(UInt(user.W))) else None
    val error = Output(Bool())
  })
  val in = Module(new DCInput(new GenAxiDataBits(size)))
  val sopSent = RegInit(init=false.B)

  in.io.enq.valid := io.in.tvalid
  io.in.tready := in.io.enq.ready
  in.io.enq.bits.tdata := io.in.tdata
  in.io.enq.bits.tkeep := io.in.tkeep
  in.io.enq.bits.tlast := io.in.tlast

  in.io.deq.ready :=  false.B
  io.out.valid := false.B
  io.out.bits.count := PopCount(in.io.deq.bits.tkeep) - 1.U
  io.out.bits.code.code := packetBody
  io.error := false.B

  if (user > 0) {
    io.userOut.get := in.io.deq.bits.tuser.get
  }
  for (i <- 0 until size) {
    io.out.bits.data(i) := in.io.deq.bits.tdata(i*8+7,i*8)
  }

  when (in.io.deq.valid && io.out.ready) {
    in.io.deq.ready := true.B
    when(in.io.deq.bits.tkeep =/= 0.U) {
      io.out.valid := true.B
    }
    when (!sopSent) {
      io.out.bits.code.code := packetSop
      sopSent := true.B
    }.elsewhen (in.io.deq.bits.tlast) {
      io.out.bits.code.code := packetGoodEop
      sopSent := false.B
    }

    when (!in.io.deq.bits.tlast && in.io.deq.bits.tkeep =/= Fill(size, true.B)) {
      io.error := true.B
    }
  }
}
