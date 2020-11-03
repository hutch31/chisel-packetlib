package packet.packetbuf

import chisel3._

class FlatPacketBuffer(c : BufferConfig) extends Module {
  val io = IO(new Bundle {
    val writeReqOut = Output(new WriteReq(c))
    val writeReqIn = Input(new WriteReq(c))
  })
}