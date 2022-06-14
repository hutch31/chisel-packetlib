package packet.packetbuf

import packet.generic._

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
 HasDropPort : Boolean = true,
 WritePortOrder : Seq[Int] = Nil
) {
  val freeListReqCredit = credit
  val freeListRespCredit = credit
  val linkWriteCredit = credit
  val schedWriteCredit = credit
  val linkReadCredit = credit
  val bytesPerPage = WordSize * LinesPerPage
  val maxPagePerPacket = MTU / bytesPerPage + 1
  val totalPages = NumPools * PagePerPool
  val IntReadClients = if (HasDropPort) ReadClients+1 else ReadClients
  val readerSchedCredit = 1

  // The BufferComplex creates read and write rings by default in port order, but if WritePortOrder
  // is specified, then the user can choose a different sequence for stitching the ring, which can help layout
  // WritePortSeq is ordered from the packet buffer outwards, so element 0 is the writer that writes directly to the
  // buffer, element 1 sends to element 0, etc.
  val WritePortSeq = if (WritePortOrder == Nil) for (i <- 0 until WriteClients) yield i else WritePortOrder
}
