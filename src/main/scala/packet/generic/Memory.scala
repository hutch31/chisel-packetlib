package packet.generic

import chisel3._
import chisel3.util._

/**
  * Memory1R1W is a memory generator class, which can be extended to create
  * platform-specific memories.  It generates a two-port memory with one read
  * and one write port.
  *
  * @param dtype  Data type to wrap
  * @param words  Memory words
  */
class Memory1R1W[D <: Data](dtype: D, words: Int, rlat: Int=1) extends Module {
  val hsize = log2Ceil(words)

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
class Memory1RW[D <: Data](dtype: D, words: Int, rlat: Int=1) extends Module {
  val hsize = log2Ceil(words)

  val io = IO(new Bundle {
    val addr = Input(UInt(hsize.W))
    val readEnable = Input(Bool())
    val writeEnable = Input(Bool())
    val writeData = Input(dtype)
    val readData = Output(dtype)
  })

  val m = SyncReadMem(words, dtype)

  when(io.writeEnable) {
    m.write(io.addr, io.writeData)
  }.elsewhen(io.readEnable) {
    if (rlat == 1) {
      io.readData := m.read(io.addr, io.readEnable)
    } else {
      io.readData := ShiftRegister(m.read(io.addr, io.readEnable), rlat-1)
    }
  }
}

class Memgen1RW {
  def apply[D <: Data](dtype: D, depth: Int, latency: Int=1) : Memory1RW[D] = {
    new Memory1RW(dtype, depth, latency)
  }
}

class Memgen1R1W {
  def apply[D <: Data](dtype: D, depth: Int, latency: Int=1) : Memory1R1W[D] = {
    new Memory1R1W(dtype, depth, latency)
  }
}


