package packet.generic

import chisel3._
import chisel3.util._
import chisel3.util.experimental.BoringUtils
import chiseltest.ChiselScalatestTester
import chiseltest.formal.{BoundedCheck, Formal, past}
import org.scalatest.freespec.AnyFreeSpec

class FormalMemQueueTB(depth : Int, gen : Memgen1R1W, memCon : MemoryControl, readLatency : Int = 1) extends Module {
  val memqueue = Module(new MemQueue(UInt(8.W), depth, gen, memCon, readLatency))
  val io = IO(new Bundle {
    val enq = Flipped(new DecoupledIO(UInt(8.W)))
    val deq = new DecoupledIO(UInt(8.W))
    val usage = Output(UInt(log2Ceil(depth + memqueue.outqSize + 1).W))
    val memControl = memCon.factory
  })
  val expqueue = Module(new Queue(UInt(8.W), depth+memqueue.outqSize))

  io.enq <> memqueue.io.enq
  io.deq <> memqueue.io.deq
  io.usage <> memqueue.io.usage
  io.memControl <> memqueue.io.memControl

  expqueue.io.enq.valid := io.enq.fire
  expqueue.io.enq.bits := io.enq.bits
  expqueue.io.deq.ready := io.deq.fire

  // data should always match expected queue
  when (io.deq.fire) {
    assert (memqueue.io.deq.bits === expqueue.io.deq.bits)
  }

  // usage should always match
  assert(memqueue.io.usage === expqueue.io.count)

  // if data enqueued N cycles ago, deq should be valid
  when (past(io.enq.fire, readLatency+2)) {
    assert(io.deq.valid)
  }

  // Pull out ready/valid for prefetch fifo
  val prefetchReady = Wire(Bool())
  val readValid = Wire(Bool())
  prefetchReady := DontCare
  readValid := DontCare
  BoringUtils.bore(memqueue.outq.io.enq.ready, Seq(prefetchReady))
  BoringUtils.bore(memqueue.pctl.io.memRdValid, Seq(readValid))
  when (readValid) { assert(prefetchReady) }

  when (memqueue.io.usage < depth.U) {
    assert (io.enq.ready)
  }
}

class FormalMemQueueTest extends AnyFreeSpec with ChiselScalatestTester with Formal {
  "pass formal checks" in {
    for (readLatency <- 1 to 4) {
      println(s"Verifying latency=$readLatency")
      verify(new FormalMemQueueTB(4, new Memgen1R1W, new MemoryControl, readLatency), Seq(BoundedCheck(10 + readLatency)))
    }
  }
}
