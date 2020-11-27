package packet.packetbuf

case class BufferConfig(
                         NumPools : Int,
                         PagePerPool : Int,
                         WordSize : Int,
                         LinesPerPage : Int,
                         ReadClients : Int,
                         WriteClients : Int,
                         MTU : Int,
                         credit : Int) {
  val freeListReqCredit = credit
  val freeListRespCredit = credit
  val linkWriteCredit = credit
  val schedWriteCredit = credit
  val linkReadCredit = credit
  val maxPagePerPacket = MTU / (WordSize*LinesPerPage) + 1
}
