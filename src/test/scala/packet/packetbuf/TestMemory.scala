package packet.packetbuf

import chisel3._
import chisel3.experimental.BaseModule
import packet.generic.{Memgen1R1W, Memgen1RW, Memory1R1W, Memory1RW, MemoryControl}

class TestMemoryControl extends MemoryControl {
  val myStatus = Output(Bool())
  override def factory : MemoryControl = new TestMemoryControl
}

trait HasTestMemoryControl extends BaseModule {
  val memStatus = IO(new TestMemoryControl)
}

class TestMemory1R1W[D <: Data](dtype: D, words: Int, rlat: Int=1) extends Memory1R1W(dtype, words, rlat, new TestMemoryControl) with HasTestMemoryControl {
  override def attachMemory(m : MemoryControl) = {
    memStatus <> m
  }
  memStatus.myStatus := 0.B
}

class TestMemory1RW[D <: Data](dtype: D, words: Int, rlat: Int=1) extends Memory1RW(dtype, words, rlat, new TestMemoryControl) with HasTestMemoryControl {
  override def attachMemory(m : MemoryControl) = {
    memStatus <> m
  }
  memStatus.myStatus := 0.B
}

class TestMemgen1RW extends Memgen1RW {
  override def apply[D <: Data](dtype: D, depth: Int, latency: Int=1,  memCon : MemoryControl = new TestMemoryControl) : Memory1RW[D] = {
    new TestMemory1RW(dtype, depth, latency)
  }
}

class TestMemgen1R1W extends Memgen1R1W  {
  override def apply[D <: Data](dtype: D, depth: Int, latency: Int=1, memCon : MemoryControl = new TestMemoryControl) : Memory1R1W[D] = {
    new TestMemory1R1W(dtype, depth, latency)
  }
}

