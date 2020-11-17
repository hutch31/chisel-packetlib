package packet.packetbuf

object BufferGen extends App {
  val conf = new BufferConfig(NumPools = 2,
    PagePerPool = 8,
    WordSize = 4,
    LinesPerPage = 16,
    ReadClients = 2,
    WriteClients = 2,
    MTU = 2048,
    credit = 2)
  //chisel3.Driver.execute(args, () => new BufferMemory(conf))
  //chisel3.Driver.execute(args, () => new RingScheduler(8, 4))
  //chisel3.Driver.execute(args, () => new FlatPacketBuffer(conf))
  //chisel3.Driver.execute(args, () => new FreeList(conf))
  chisel3.Driver.execute(args, () => new PacketWriter(conf, 0))
}
