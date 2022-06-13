package packet.generic

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{FlatSpec, Matchers}

class TestMemQueue extends AnyFreeSpec with ChiselScalatestTester {

  "read and write" in {
    for (depth <- Seq(8, 9, 16, 17, 127, 128, 129)) {
      test(new MemQueue(UInt(16.W), 17, new Memgen1R1W())).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) {
        c => {
          val rand = new scala.util.Random()
          val cycles = 100
          c.io.enq.initSource().setSourceClock(c.clock)
          c.io.deq.initSink().setSinkClock(c.clock)

          var tx: Int = 0
          var rx: Int = 0
          val tx_fc = Seq(0.5, 0.05, 0.25, 0.1, 0.25, 0.5, 0.75, 0.9)
          val rx_fc = Seq(0.1, 0.5, 0.25, 0.9, 0.75, 0.5, 0.25, 0.1)

          fork {
            for (fc <- tx_fc) {
              for (i <- 0 until cycles) {
                c.io.enq.enqueue(tx.U)
                tx += 1
                if (rand.nextFloat() < fc) {
                  c.clock.step(1)
                }
              }
            }
          }.fork {
            for (fc <- rx_fc) {
              for (i <- 0 until cycles) {
                c.io.deq.expectDequeue(rx.U)
                rx += 1
                if (rand.nextFloat() < fc) {
                  c.clock.step(1)
                }
              }
            }
          }.join()
        }
      }
    }
  }

  "read and write single port" in {
    for (depth <- Seq(8, 9, 16, 17, 127, 128, 129)) {
      test(new MemQueueSP(UInt(16.W), 17, new VerilogMemgen1RW())).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) {
        c => {
          val rand = new scala.util.Random()
          val cycles = 100
          c.io.enq.initSource().setSourceClock(c.clock)
          c.io.deq.initSink().setSinkClock(c.clock)

          var tx: Int = 0
          var rx: Int = 0
          val tx_fc = Seq(0.5, 0.05, 0.25, 0.1, 0.25, 0.5, 0.75, 0.9)
          val rx_fc = Seq(0.1, 0.5, 0.25, 0.9, 0.75, 0.5, 0.25, 0.1)

          fork {
            for (fc <- tx_fc) {
              for (i <- 0 until cycles) {
                c.io.enq.enqueue(tx.U)
                tx += 1
                if (rand.nextFloat() < fc) {
                  c.clock.step(1)
                }
              }
            }
          }.fork {
            for (fc <- rx_fc) {
              for (i <- 0 until cycles) {
                c.io.deq.expectDequeue(rx.U)
                rx += 1
                if (rand.nextFloat() < fc) {
                  c.clock.step(1)
                }
              }
            }
          }.join()
        }
      }
    }
  }

  "stop when full" in {
    val depth = 5
    test(new MemQueue(UInt(16.W), depth, new Memgen1R1W())).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        val rand = new scala.util.Random()
        val cycles = 100
        c.io.enq.initSource().setSourceClock(c.clock)
        c.io.deq.initSink().setSinkClock(c.clock)

        for (j <- 0 to 10) {
          c.io.usage.expect(0.U)
          c.io.enq.ready.expect(1.B)
          for (i <- 0 until depth) {
            c.io.enq.enqueue(i.U)
            c.io.usage.expect((i+1).U)
          }
          c.io.enq.ready.expect(0.B)
          c.io.usage.expect(depth.U)

          for (i <- 0 until depth) {
            c.io.deq.expectDequeue(i.U)
            c.io.usage.expect((depth-(i+1)).U)
          }
          c.io.deq.valid.expect(0.B)
        }
      }
    }
  }
}
