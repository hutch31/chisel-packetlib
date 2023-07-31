package packet.generic

import chisel3._
import chisel3.util.ImplicitConversions.intToUInt
import chisel3.util._

class MemoryControl extends Bundle {
  def connectMemory   = {

  }

  def factory : MemoryControl = new MemoryControl
}

/**
  * Memory1R1W is a memory generator class, which can be extended to create
  * platform-specific memories.  It generates a two-port memory with one read
  * and one write port.
  *
  * @param dtype  Data type to wrap
  * @param words  Memory words
  */
class Memory1R1W[D <: Data](dtype: D, words: Int, rlat: Int=1, memCon : MemoryControl = new MemoryControl) extends Module {
  val hsize = log2Ceil(words)
  val hard_inst = false

  val io = IO(new Bundle {
    val readAddr = Input(UInt(hsize.W))
    val writeAddr = Input(UInt(hsize.W))
    val readEnable = Input(Bool())
    val writeEnable = Input(Bool())
    val writeData = Input(dtype)
    val readData = Output(dtype)
  })
  val m = SyncReadMem(words, dtype)
  val rdata = Reg(dtype)

  when(io.writeEnable) {
    m.write(io.writeAddr, io.writeData)
  }
  if (rlat == 1) {
    io.readData := m.read(io.readAddr, io.readEnable)
  } else {
    io.readData := ShiftRegister(m.read(io.readAddr, io.readEnable), rlat-1)
  }

  memCon.connectMemory

  // Populate this with code needed to connect parent to this memory
  def attachMemory(m : MemoryControl) = {

  }
}

/**
 * Memory1RW is a memory generator class, which can be extended to create
 * platform-specific memories.  It generates a single port memory with one combined
 * read/write port.
 *
 * @param dtype  Data type to wrap
 * @param words
 * @param rlat
 */
class Memory1RW[D <: Data](dtype: D, words: Int, rlat: Int=1, memCon : MemoryControl = new MemoryControl) extends Module {
  val hsize = log2Ceil(words)
  val hard_inst = false

  val io = IO(new Bundle {
    val addr = Input(UInt(hsize.W))
    val readEnable = Input(Bool())
    val writeEnable = Input(Bool())
    val writeData = Input(dtype)
    val readData = Output(dtype)
  })

  // Populate this with code needed to connect parent to this memory
  def attachMemory(m : MemoryControl) = {
  }
  val m = SyncReadMem(words, dtype)

  io.readData := 0.asTypeOf(dtype.cloneType)

  when(io.writeEnable) {
    m.write(io.addr, io.writeData)
  }
  //  .elsewhen(io.readEnable) {
  if (rlat == 1) {
    io.readData := m.read(io.addr)
  } else {
    io.readData := ShiftRegister(m.read(io.addr), rlat-1)
  }
  //}
}

class Memgen1RW {
  def apply[D <: Data](dtype: D, depth: Int, latency: Int=1,  memCon : MemoryControl = new MemoryControl) : Memory1RW[D] = {
    new Memory1RW(dtype, depth, latency, memCon)
  }
}

class Memgen1R1W {
  def apply[D <: Data](dtype: D, depth: Int, latency: Int=1, memCon : MemoryControl = new MemoryControl) : Memory1R1W[D] = {
    new Memory1R1W(dtype, depth, latency, memCon)
  }
}

class Adapter1R1W[D <: Data](dtype: D, words: Int, rlat: Int=1) extends Module {
  val hsize = log2Ceil(words)

  val io = IO(new Bundle {
    val readAddr = Input(UInt(hsize.W))
    val writeAddr = Input(UInt(hsize.W))
    val readEnable = Input(Bool())
    val writeEnable = Input(Bool())
    val writeData = Input(dtype)
    val readData = Output(dtype)
  })

  val m1p = for (i <- 0 to 1) yield Module(new Memory1RW(dtype, words, rlat))
  val side = RegInit(init=VecInit(Seq.fill(words)(0.B)))
  val readSel = side(io.readAddr)
  val readSelD = ShiftRegister(readSel, rlat)

  for (i <- 0 to 1) {
    val thisReadSel = (i.U === readSel)
    m1p(i).io.writeEnable := io.writeEnable & !thisReadSel
    m1p(i).io.readEnable := io.readEnable & thisReadSel
    when (thisReadSel) {
      m1p(i).io.addr := io.readAddr
    }.otherwise {
      m1p(i).io.addr := io.writeAddr
    }
    m1p(i).io.writeData := io.writeData
  }
  when (readSelD) {
    io.readData := m1p(1).io.readData
  }.otherwise {
    io.readData := m1p(0).io.readData
  }

  // reads have priority
  when (io.writeEnable) {
    side(io.writeAddr) := ~readSel
  }
}


