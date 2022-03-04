package packet.axis

import chisel3._

class PktAxiStreaming(val size: Int, val user : Int = 0) extends Bundle {
  val tvalid = Output(Bool())
  val tready = Input(Bool())
  val tkeep = Output(UInt(size.W))
  val tdata = Output(UInt((size*8).W))
  val tlast = Output(Bool())
  val tuser = if (user > 0) Some(Output(UInt(user.W))) else None
}

class GenAxiDataBits(val size: Int, val user : Int = 0) extends Bundle {
  val tkeep = UInt(size.W)
  val tdata = UInt((size*8).W)
  val tlast = Bool()
  val tuser = if (user > 0) Some(UInt(user.W)) else None
}
