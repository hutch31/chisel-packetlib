package chisel.lib.dclib

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec

import scala.util.Random

class TestGearboxFixture(intWidth : Int = 8) extends DCAbstractBuffer(UInt(16.W)) {
  val gbIn = Module(new DCGearbox(16, intWidth))
  val gbOut = Module(new DCGearbox(intWidth, 16))

  io.enq <> gbIn.io.c
  gbIn.io.p <> gbOut.io.c
  gbOut.io.p <> io.deq
}

class TestGearbox extends AnyFreeSpec with ChiselScalatestTester{
  "pass data" in {
    for (w <- Seq(15, 17, 21, 31, 33)) {
      test(new TestGearboxFixture(w)).withAnnotations(Seq(WriteVcdAnnotation)) {
        c => {
          c.io.enq.initSource().setSourceClock(c.clock)
          c.io.deq.initSink().setSinkClock(c.clock)

          val q = for (i <- 5 to 4 + 16 *w) yield i.U(16.W)
          //val q = Seq(10.U, 20.U)

          fork {
            c.io.enq.enqueueSeq(q)
          }.fork {
            c.io.deq.expectDequeueSeq(q)
          }.join()
        }
      }
    }
  }

  "start and stop randomly" in {
    for (w <- Seq(15, 17, 21, 31, 33)) {
      test(new TestGearboxFixture(w)).withAnnotations(Seq(WriteVcdAnnotation)) {
        c => {
          c.io.enq.initSource().setSourceClock(c.clock)
          c.io.deq.initSink().setSinkClock(c.clock)
          val rand = new Random(1)

          val total_count = 16*w * 3
          var tx_count: Int = 0
          var rx_count: Int = 0

          fork {
            while (tx_count < total_count) {
              if (rand.nextFloat() > 0.35) {
                c.clock.step(1)
              }
              c.io.enq.enqueue(tx_count.U)
              tx_count += 1
            }
          }.fork {
            while (rx_count < total_count) {
              if (rand.nextFloat() > 0.35) {
                c.clock.step(1)
              }
              c.io.deq.expectDequeue(rx_count.U)
              rx_count += 1
            }
          }.join()
        }
      }
    }
  }
}
