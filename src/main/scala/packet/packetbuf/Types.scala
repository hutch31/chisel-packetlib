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
  def asAddr() : UInt = { constantMult(pool, c.PagePerPool) +& pageNum }
}

class PageLink(c : BufferConfig) extends Bundle {
  val nextPage = new PageType(c)
  val nextPageValid = Bool()
}

class LinkListWriteReq(c : BufferConfig) extends Bundle {
  val addr = new PageType(c)
  val data = new PageLink(c)
}

class LinkListReadReq(c : BufferConfig) extends Bundle {
  val requestor = UInt(log2Ceil(c.IntReadClients).W)
  val addr = new PageType(c)
}

class LinkListReadResp(c : BufferConfig) extends Bundle {
  val requestor = UInt(log2Ceil(c.IntReadClients).W)
  val data = new PageLink(c)
}

class BufferWriteReq(c : BufferConfig) extends Bundle {
  val slotValid = Bool()
  val slot = UInt(log2Ceil(c.WriteClients).W)
  val page = new PageType(c)
  val line = UInt(log2Ceil(c.LinesPerPage).W)
  val data = Vec(c.WordSize, UInt(8.W))
}

class PageReq(val c : BufferConfig) extends Bundle {
  val requestor = UInt(log2Ceil(c.WriteClients).W)
  val pool = UInt(log2Ceil(c.NumPools).W)
}

class PageResp(c : BufferConfig) extends Bundle {
  val requestor = UInt(log2Ceil(c.WriteClients).W)
  val page = new PageType(c)
}

class BufferReadReq(c : BufferConfig) extends Bundle {
  val requestor = UInt(log2Ceil(c.ReadClients).W)
  val page = new PageType(c)
  val line = UInt(log2Ceil(c.LinesPerPage).W)
}

class BufferReadResp(c : BufferConfig) extends Bundle {
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
  val refCountAdd = Decoupled(new RefCountAdd(c))
}

class PacketReaderInterface(c: BufferConfig) extends Bundle {
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
  val pageCount = UInt(log2Ceil(c.maxPagePerPacket+1).W)
  val startPage = new PageType(c)
}

class RoutingResult(val destinations : Int) extends Bundle {
  val dest = UInt(destinations.W)
  def getNextDest() : UInt = { PriorityEncoder(dest) }
  def getNumDest() : UInt = { PopCount(dest) }
}

class DropQueueConfig(c : BufferConfig) extends Bundle {
  val packetDropThreshold = Vec(c.ReadClients, UInt(log2Ceil(c.MaxPacketsPerPort+1).W))
  val pageDropThreshold = Vec(c.ReadClients, UInt(log2Ceil(c.MaxPagesPerPort+1).W))
}

class DropQueueStatus(c : BufferConfig) extends Bundle {
  val tailDropInc = Vec(c.ReadClients, Bool())
  val outputQueueSize = Vec(c.ReadClients, UInt(log2Ceil(c.MaxPacketsPerPort+1).W))
  val outputPageSize = Vec(c.ReadClients, UInt(log2Ceil(c.MaxPagesPerPort+1).W))
}

class BufferConfiguration(c : BufferConfig) extends Bundle {
  val dropQueueConfig = Input(new DropQueueConfig(c))
}

class PktBufStatus(c : BufferConfig) extends Bundle {
  val buffer = new BufferStatus(c)
  val dropQueueStatus = Output(new DropQueueStatus(c))
}

class BufferStatus(c : BufferConfig) extends Bundle {
  val freePages = Output(Vec(c.NumPools, UInt(log2Ceil(c.PagePerPool+1).W)))
  val pagesPerPort = Output(Vec(c.WriteClients, UInt(log2Ceil(c.totalPages).W)))
}

class RefCountAdd(val c : BufferConfig) extends Bundle {
  val page = new PageType(c)
  val amount = UInt(log2Ceil(c.MaxReferenceCount).W)
}

class PacketCounters(val c : BufferConfig) extends Bundle {
  val incRxCount = UInt(c.WriteClients.W)
  val incTxCount = UInt(c.ReadClients.W)
  val incDropCount = Bool()
}

