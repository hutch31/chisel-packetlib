package packet.packetbuf

import chisel3._
import chisel3.util._

/**
 * Refers to a specific page in the packet memory buffer
 */
class PageType(c : BufferConfig) extends Bundle {
  val pool = UInt(log2Ceil(c.NumPools).W)
  val pageNum = UInt(log2Ceil(c.PagePerPool).W)
}

class WriteReq(c : BufferConfig) extends Bundle {
  val slot = UInt(log2Ceil(c.WriteClients))
  val page = new PageType(c)
  val line = UInt(log2Ceil(c.LinesPerPage))
  val data = Vec(c.WordSize, UInt(8.W))
}
