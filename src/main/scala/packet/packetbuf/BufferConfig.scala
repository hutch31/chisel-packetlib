package packet.packetbuf

import packet.generic._

import java.text.spi.NumberFormatProvider

case class BufferConfig
(mgen2p: Memgen1R1W,
 mgen1p : Memgen1RW,
 NumPools: Int,
 PagePerPool: Int,
 WordSize: Int,
 LinesPerPage: Int,
 ReadClients: Int,
 WriteClients: Int,
 MTU: Int,
 credit: Int,
 ReadWordBuffer : Int = 2,
 PacketBuffer2Port : Boolean = true,
 PacketBufferReadLatency : Int = 1,
 // When set to more than 1, implements a reference count inside FreeList, so that packets are not freed until
 // the reference count returns to zero.
 MaxReferenceCount : Int = 1,
 WritePortOrder : Seq[Int] = Nil,
 MemControl : MemoryControl = new MemoryControl,
 FreeListReadLatency : Int = 1,
 LinkListReadLatency : Int = 1,
 MaxPacketsPerPort : Int = 16,
 MaxPagesPerPort : Int = 64,
 DropQueueReadLatency : Int = 1
) {
  val freeListReqCredit = credit
  val freeListRespCredit = credit
  val linkWriteCredit = credit
  val schedWriteCredit = credit
  val linkReadCredit = credit
  val bytesPerPage = WordSize * LinesPerPage
  val maxPagePerPacket = MTU / bytesPerPage + 1
  val totalPages = NumPools * PagePerPool
  val HasDropPort : Boolean = true

  val IntReadClients = if (HasDropPort) ReadClients+1 else ReadClients
  val readerSchedCredit = 1
  // Each pool has 1 packet buffer memory, 1 free list memory, and 1 link list memory
  val bufferMemoryCount = 3 * NumPools
  val schedMemoryCount = ReadClients
  val totalMemoryCount = bufferMemoryCount + schedMemoryCount

  // The BufferComplex creates read and write rings by default in port order, but if WritePortOrder
  // is specified, then the user can choose a different sequence for stitching the ring, which can help layout
  // WritePortSeq is ordered from the packet buffer outwards, so element 0 is the writer that writes directly to the
  // buffer, element 1 sends to element 0, etc.
  val WritePortSeq = if (WritePortOrder == Nil) for (i <- 0 until WriteClients) yield i else WritePortOrder

  // Parameter Checking
  // If MaxReferenceCount is enabled, the max number of references is the same as the number of read clients
  require ((MaxReferenceCount == 1) || (MaxReferenceCount == ReadClients))
  require (ReadClients >= 2)
  require (WriteClients >= 2)
  require (MTU >= bytesPerPage)
}
