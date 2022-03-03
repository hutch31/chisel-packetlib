package packet.packetbuf

import chisel.lib.dclib.CreditIO
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

class PageLink(c : BufferConfig) extends Bundle {
  val nextPage = new PageType(c)
  val nextPageValid = Bool()
  override def cloneType = new PageLink(c).asInstanceOf[this.type]
}

class LinkListWriteReq(c : BufferConfig) extends Bundle {
  val addr = new PageType(c)
  val data = new PageLink(c)
  override def cloneType = new LinkListWriteReq(c).asInstanceOf[this.type]
}

class LinkListReadReq(c : BufferConfig) extends Bundle {
  val requestor = UInt(log2Ceil(c.ReadClients).W)
  val addr = new PageType(c)
  override def cloneType = new LinkListReadReq(c).asInstanceOf[this.type]
}

class LinkListReadResp(c : BufferConfig) extends Bundle {
  val requestor = UInt(log2Ceil(c.ReadClients).W)
  val data = new PageLink(c)
  override def cloneType = new LinkListReadResp(c).asInstanceOf[this.type]
}

class BufferWriteReq(c : BufferConfig) extends Bundle {
  val slotValid = Bool()
  val slot = UInt(log2Ceil(c.WriteClients).W)
  val page = new PageType(c)
  val line = UInt(log2Ceil(c.LinesPerPage).W)
  val data = Vec(c.WordSize, UInt(8.W))
  override def cloneType =
    new BufferWriteReq(c).asInstanceOf[this.type]
}

class PageReq(c : BufferConfig) extends Bundle {
  val requestor = UInt(log2Ceil(c.WriteClients).W)
  val pool = UInt(log2Ceil(c.NumPools).W)
  override def cloneType =
    new PageReq(c).asInstanceOf[this.type]
}

class PageResp(c : BufferConfig) extends Bundle {
  val requestor = UInt(log2Ceil(c.WriteClients).W)
  val page = new PageType(c)
  override def cloneType =
    new PageResp(c).asInstanceOf[this.type]
}

class BufferReadReq(c : BufferConfig) extends Bundle {
  val requestor = UInt(log2Ceil(c.ReadClients).W)
  val page = new PageType(c)
  val line = UInt(log2Ceil(c.LinesPerPage).W)
  override def cloneType =
    new BufferReadReq(c).asInstanceOf[this.type]
}

class BufferReadResp(c : BufferConfig) extends Bundle {
  val req = new BufferReadReq(c)
  val data = Vec(c.WordSize, UInt(8.W))
  override def cloneType =
    new BufferReadResp(c).asInstanceOf[this.type]
}

class PacketWriterInterface(c: BufferConfig) extends Bundle {
  // Interface to free list for requesting free pages to write to
  val freeListReq = new CreditIO(new PageReq(c))
  val freeListPage = Flipped(new CreditIO(new PageResp(c)))
  // Link list write interface
  val linkListWriteReq = new CreditIO(new LinkListWriteReq(c))
  // Connection to page write ring
  val writeSlotReq = Output(Bool())
  override def cloneType =
    new PacketWriterInterface(c).asInstanceOf[this.type]
}

class PacketReaderInterface(c: BufferConfig) extends Bundle {
  // Interface to link list for requesting pages
  val linkListReadReq = new CreditIO(new LinkListReadReq(c))
  val linkListReadResp = Flipped(new CreditIO(new LinkListReadResp(c)))
  // Interface to free list for returning pages
  val freeListReturn = new CreditIO(new PageType(c))
  // Buffer read request and response
  val bufferReadReq = new CreditIO(new BufferReadReq(c))
  override def cloneType =
    new PacketReaderInterface(c).asInstanceOf[this.type]
}

class SchedulerReq(c : BufferConfig) extends Bundle {
  val dest = UInt(log2Ceil(c.ReadClients).W)
  val length = UInt(log2Ceil(c.MTU).W)
  val startPage = new PageType(c)
  override def cloneType = new SchedulerReq(c).asInstanceOf[this.type]
}

class RoutingResult(destinations : Int) extends Bundle {
  val dest = UInt(log2Ceil(destinations).W)
  def getDest() : UInt = { dest }
  override def cloneType = new RoutingResult(destinations).asInstanceOf[this.type]
}

