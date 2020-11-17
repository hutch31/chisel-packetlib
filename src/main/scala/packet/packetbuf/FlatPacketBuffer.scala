package packet.packetbuf

import chisel3._

class FlatPacketBuffer(c : BufferConfig) extends Module {
  val io = IO(new Bundle {
    val buf = new BufferMemoryIO(c)
    val free = new FreeListIO(c)
  })
  val buffer = Module(new BufferMemory(c))
  val freeList = Module(new FreeList(c))

  buffer.io <> io.buf
  freeList.io <> io.free
}
