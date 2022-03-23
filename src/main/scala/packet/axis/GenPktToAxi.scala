package packet.axis

import chisel3._
import chisel3.util._
import chisel.lib.dclib._
import packet._

class GenPktToAxi(size: Int, user : Int = 0, userBadCrc : Boolean = false) extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new PacketData(size)))
    val userIn = if (user > 0) Some(Input(UInt(user.W))) else None
    val out = new PktAxiStreaming(size, user)
  })
  override def desiredName: String = "GenPktToAxi_W" + size.toString + "_U" + user.toString
  if (userBadCrc) require(user >= 1)

  val outHold = Module(new DCOutput(new GenAxiDataBits(size, user)))
  val tkeep = Wire(Vec(size, Bool()))
  for (i <- 0 until size) {
    tkeep(i) := i.U <= io.in.bits.count
  }

  outHold.io.enq.bits.tdata := Cat(io.in.bits.data.reverse)
  outHold.io.enq.bits.tlast := io.in.bits.code.isEop()
  when (io.in.bits.code.isEop()) {
    outHold.io.enq.bits.tkeep := Cat(tkeep.reverse)
  }.otherwise {
    outHold.io.enq.bits.tkeep := Fill(size, true.B)
  }
  if (user > 0) {
    outHold.io.enq.bits.tuser.get := io.userIn.get
    if (userBadCrc) {
      when (io.in.bits.code.isEop()) {
        outHold.io.enq.bits.tuser.get(0) := io.in.bits.code.isBadEop()
      }
    }
  }
  outHold.io.enq.valid := io.in.valid
  io.in.ready := outHold.io.enq.ready

  io.out.tvalid := outHold.io.deq.valid
  outHold.io.deq.ready := io.out.tready
  io.out.tdata := outHold.io.deq.bits.tdata
  io.out.tkeep := outHold.io.deq.bits.tkeep
  io.out.tlast := outHold.io.deq.bits.tlast
  io.out.tuser.get := outHold.io.deq.bits.tuser.get
}
