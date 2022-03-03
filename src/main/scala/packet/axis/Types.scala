package packet.axis

import chisel3._

class PktAxiStreaming(val size: Int) extends Bundle {
  val tvalid = Output(Bool())
  val tready = Input(Bool())
  val tkeep = Output(UInt(size.W))
  val tdata = Output(UInt((size*8).W))
  val tlast = Output(Bool())
}

class GenAxiDataBits(val size: Int) extends Bundle {
  val tkeep = UInt(size.W)
  val tdata = UInt((size*8).W)
  val tlast = Bool()
}
