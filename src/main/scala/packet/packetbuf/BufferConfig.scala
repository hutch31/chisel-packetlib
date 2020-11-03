package packet.packetbuf

case class BufferConfig(
                         NumPools : Int,
                         PagePerPool : Int,
                         WordSize : Int,
                         LinesPerPage : Int,
                         ReadClients : Int,
                         WriteClients : Int) {

}
