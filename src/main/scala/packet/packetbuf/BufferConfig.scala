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
 PacketBufferReadLatency : Int = 1
) {
  val freeListReqCredit = credit
  val freeListRespCredit = credit
  val linkWriteCredit = credit
  val schedWriteCredit = credit
  val linkReadCredit = credit
  val bytesPerPage = WordSize * LinesPerPage
  val maxPagePerPacket = MTU / bytesPerPage + 1
}
