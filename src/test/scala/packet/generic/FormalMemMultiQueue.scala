package packet.generic

import chisel3._
import chisel3.util._
import chiseltest.ChiselScalatestTester
import chiseltest.formal.{BoundedCheck, Formal, past}
import org.scalatest.freespec.AnyFreeSpec

class FormalMemMultiQueueTB(depth : Int, numQueue : Int, gen : Memgen1R1W, memCon : MemoryControl, readLatency : Int = 1) extends Module {
  val memqueue = Module(new MemMultiQueue(UInt(8.W), depth*numQueue, numQueue, gen, memCon, readLatency))
  val io = IO(new Bundle {
    val enq = Vec(numQueue, Flipped(Decoupled(UInt(8.W))))
    val deq = Vec(numQueue, Decoupled(UInt(8.W)))
    val usage = Output(Vec(numQueue, UInt(log2Ceil(depth + memqueue.outqSize + 1).W)))
    val memControl = memCon.factory
  })
  val expqueue = for (q <- 0 until numQueue) yield Module(new Queue(UInt(8.W), depth+memqueue.outqSize))

  io.enq <> memqueue.io.enq
  io.deq <> memqueue.io.deq
  io.usage <> memqueue.io.usage
  io.memControl <> memqueue.io.memControl

  for (q <- 0 until numQueue) {
    expqueue(q).io.enq.valid := io.enq(q).fire
    expqueue(q).io.enq.bits := io.enq(q).bits
    expqueue(q).io.deq.ready := io.deq(q).fire
    memqueue.io.lowerBound(q) := (q*depth).U
    memqueue.io.upperBound(q) := (q*depth+depth-1).U

    // data should always match expected queue
    when(io.deq(q).fire) {
      assert(memqueue.io.deq(q).bits === expqueue(q).io.deq.bits)
    }

    // usage should always match
    assert(memqueue.io.usage(q) === expqueue(q).io.count)

//    // if data enqueued N cycles ago, deq should be valid
//    when(past(io.enq(q).fire, readLatency + 2)) {
//      assert(io.deq(q).valid)
//    }
  }
}

class FormalMemMultiQueueTest extends AnyFreeSpec with ChiselScalatestTester with Formal {
  "pass formal checks" in {
    for (readLatency <- 1 to 4) {
      println(s"Verifying latency=$readLatency")
      verify(new FormalMemMultiQueueTB(3, 2, new Memgen1R1W, new MemoryControl, readLatency), Seq(BoundedCheck(10 + readLatency)))
    }
  }
}
