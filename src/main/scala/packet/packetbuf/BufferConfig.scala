package packet.packetbuf

import packet.generic._

case class BufferConfig
(mgen: Memgen1R1W,
 NumPools: Int,
 PagePerPool: Int,
 WordSize: Int,
 LinesPerPage: Int,
 ReadClients: Int,
 WriteClients: Int,
 MTU: Int,
 credit: Int,
 ReadWordBuffer : Int = 2,
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
