package packet.packetbuf

import chisel3._
import chisel3.util._

/**
 * Refers to a specific page in the packet memory buffer
 */
class PageType(c : BufferConfig) extends Bundle {
  val pool = UInt(log2Ceil(c.NumPools).W)
  val pageNum = UInt(log2Ceil(c.PagePerPool).W)
  override def cloneType =
    new PageType(c).asInstanceOf[this.type]
}

class WriteReq(c : BufferConfig) extends Bundle {
  val slot = UInt(log2Ceil(c.WriteClients).W)
  val page = new PageType(c)
  val line = UInt(log2Ceil(c.LinesPerPage).W)
  val data = Vec(c.WordSize, UInt(8.W))
  override def cloneType =
    new WriteReq(c).asInstanceOf[this.type]
}

class PageReq(c : BufferConfig) extends Bundle {
  val requestor = UInt(log2Ceil(c.WriteClients).W)
  override def cloneType =
    new PageReq(c).asInstanceOf[this.type]
}

class PageResp(c : BufferConfig) extends Bundle {
  val requestor = UInt(log2Ceil(c.WriteClients).W)
  val page = new PageType(c)
  override def cloneType =
    new PageResp(c).asInstanceOf[this.type]
}
