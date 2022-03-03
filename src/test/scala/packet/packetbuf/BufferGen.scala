package packet.packetbuf

import chisel3.stage.ChiselGeneratorAnnotation
import packet.generic.{Memgen1R1W, Memgen1RW}

object BufferGen extends App {
  val conf = new BufferConfig(
    new Memgen1R1W,
    new Memgen1RW,
    NumPools = 2,
    PagePerPool = 8,
    WordSize = 4,
    LinesPerPage = 4,
    ReadClients = 2,
    WriteClients = 2,
    MTU = 2048,
    credit = 2)
  val chiselStage = new chisel3.stage.ChiselStage

  chiselStage.execute(
    Array("-X", "verilog"),
    Seq(ChiselGeneratorAnnotation(() =>new PacketReader(conf)))
  )
}
