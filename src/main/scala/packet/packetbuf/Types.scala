package packet.packetbuf

import chisel.lib.dclib.CreditIO
import chisel3._
import chisel3.util._

/**
 * Refers to a specific page in the packet memory buffer
 */
class PageType(val c : BufferConfig) extends Bundle {
  val pool = UInt(log2Ceil(c.NumPools).W)
  val pageNum = UInt(log2Ceil(c.PagePerPool).W)
  def asAddr() : UInt = { constantMult(pool, c.PagePerPool) +& pageNum }
}

class PageLink(val c : BufferConfig) extends Bundle {
  val nextPage = new PageType(c)
  val nextPageValid = Bool()
}

class LinkListWriteReq(val c : BufferConfig) extends Bundle {
  val addr = new PageType(c)
  val data = new PageLink(c)
}

class LinkListReadReq(val c : BufferConfig) extends Bundle {
  val requestor = UInt(log2Ceil(c.ReadClients).W)
  val addr = new PageType(c)
}

class LinkListReadResp(val c : BufferConfig) extends Bundle {
  val requestor = UInt(log2Ceil(c.ReadClients).W)
  val data = new PageLink(c)
}

class BufferWriteReq(val c : BufferConfig) extends Bundle {
  val slotValid = Bool()
  val slot = UInt(log2Ceil(c.WriteClients).W)
  val page = new PageType(c)
  val line = UInt(log2Ceil(c.LinesPerPage).W)
  val data = Vec(c.WordSize, UInt(8.W))
}

class PageReq(val c : BufferConfig) extends Bundle {
  val requestor = UInt(log2Ceil(c.WriteClients).W)
  val pool = if (c.NumPools > 1) Some(UInt(log2Ceil(c.NumPools).W)) else None
}

class PageResp(val c : BufferConfig) extends Bundle {
  val requestor = UInt(log2Ceil(c.WriteClients).W)
  val page = new PageType(c)
}

class BufferReadReq(val c : BufferConfig) extends Bundle {
  val requestor = UInt(log2Ceil(c.ReadClients).W)
  val page = new PageType(c)
  val line = UInt(log2Ceil(c.LinesPerPage).W)
}

class BufferReadResp(val c : BufferConfig) extends Bundle {
  val req = new BufferReadReq(c)
  val data = Vec(c.WordSize, UInt(8.W))
}

class PacketWriterInterface(val c: BufferConfig) extends Bundle {
  // Interface to free list for requesting free pages to write to
  val freeListReq = new CreditIO(new PageReq(c))
  val freeListPage = Flipped(new CreditIO(new PageResp(c)))
  // Link list write interface
  val linkListWriteReq = new CreditIO(new LinkListWriteReq(c))
  // Connection to page write ring
  val writeSlotReq = Output(Bool())
  // Connection to Free list reference count
  val refCountAdd = if (c.MaxReferenceCount > 1) Some(Decoupled(new RefCountAdd(c))) else None
}

class PacketReaderInterface(val c: BufferConfig) extends Bundle {
  // Interface to link list for requesting pages
  val linkListReadReq = new CreditIO(new LinkListReadReq(c))
  val linkListReadResp = Flipped(new CreditIO(new LinkListReadResp(c)))
  // Interface to free list for returning pages
  val freeListReturn = new CreditIO(new PageType(c))
  // Buffer read request and response
  val bufferReadReq = new CreditIO(new BufferReadReq(c))
}

class PacketDropInterface(c : BufferConfig) extends Bundle {
  val linkListReadReq = new CreditIO(new LinkListReadReq(c))
  val linkListReadResp = Flipped(new CreditIO(new LinkListReadResp(c)))
  // Interface to free list for returning pages
  val freeListReturn = new CreditIO(new PageType(c))
}

class SchedulerReq(c : BufferConfig) extends Bundle {
  val dest = UInt(c.ReadClients.W)
  val length = UInt(log2Ceil(c.MTU).W)
  val startPage = new PageType(c)
}

class RoutingResult(val destinations : Int) extends Bundle {
  val dest = UInt(destinations.W)
  def getNextDest() : UInt = { PriorityEncoder(dest) }
  def getNumDest() : UInt = { PopCount(dest) }
}

class BufferStatus(c : BufferConfig) extends Bundle {
  val pagesPerPort = Output(Vec(c.WriteClients, UInt(log2Ceil(c.totalPages).W)))
}

class RefCountAdd(val c : BufferConfig) extends Bundle {
  val page = new PageType(c)
  val amount = UInt(log2Ceil(c.MaxReferenceCount).W)
}

