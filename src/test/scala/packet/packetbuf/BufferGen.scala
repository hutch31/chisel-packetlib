package packet.packetbuf

import chisel3.stage.ChiselGeneratorAnnotation

object BufferGen extends App {
  val conf = new BufferConfig(NumPools = 2,
    PagePerPool = 8,
    WordSize = 4,
    LinesPerPage = 4,
    ReadClients = 2,
    WriteClients = 2,
    MTU = 2048,
    credit = 2)
  //chisel3.Driver.execute(args, () => new BufferMemory(conf))
  //chisel3.Driver.execute(args, () => new RingScheduler(8, 4))
  //chisel3.Driver.execute(args, () => new FlatPacketBuffer(conf))
  //chisel3.Driver.execute(args, () => new FreeList(conf))
  //chisel3.Driver.execute(args, () => new PacketWriter(conf, 0))

  val conf2 = new BufferConfig(1, 4, 2, 4, 2, 2, MTU = 2048, credit = 2)
  //chisel3.Driver.execute(args, () => new PacketWriterTestbench(conf))
  (new chisel3.stage.ChiselStage).execute(
    Array("-X", "verilog"),
    Seq(ChiselGeneratorAnnotation(() => new PacketWriterTestbench(conf2))))
}
