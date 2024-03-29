package chisel.lib.dclib

import chisel3._
import chisel3.util.DecoupledIO
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec

import scala.util.Random

class AsyncWrap[D <: Data](data: D, depth : Int) extends Module {
  val io = IO(new Bundle {
    val enq = Flipped(new DecoupledIO(data.cloneType))
    val deq = new DecoupledIO(data.cloneType)
  })

  val af = Module(new DCAsyncFifo(data, depth))
  af.io.enqClock := clock
  af.io.enqReset := reset
  af.io.deqClock := clock
  af.io.deqReset := reset

  af.io.enq <> io.enq
  af.io.deq <> io.deq
}

class AsyncWordWrap[D <: Data](data: D) extends Module {
  val io = IO(new Bundle {
    val enq = Flipped(new DecoupledIO(data.cloneType))
    val deq = new DecoupledIO(data.cloneType)
  })
  val aw = Module(new DCAsyncWord(data))
  aw.io.enqClock := clock
  aw.io.enqReset := reset
  aw.io.enq <> io.enq
  aw.io.deq <> io.deq
}

class TestAsyncFifo  extends AnyFreeSpec with ChiselScalatestTester{
  "start and stop randomly" in {
    for (d <- Seq(2,4,8,16,32)) {
      test(new AsyncWrap(UInt(16.W), d)).withAnnotations(Seq(WriteVcdAnnotation)) {
        c => {
          c.io.enq.initSource().setSourceClock(c.clock)
          c.io.deq.initSink().setSinkClock(c.clock)
          val rand = new Random(1)

          val total_count = 250
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
              if (rand.nextFloat() > 0.65) {
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

  "start and stop randomly for async word" in {
    test(new AsyncWordWrap(UInt(16.W))).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        c.io.enq.initSource().setSourceClock(c.clock)
        c.io.deq.initSink().setSinkClock(c.clock)
        val rand = new Random(1)

        val total_count = 250
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
            c.clock.step(rand.between(5,20))
            c.io.deq.expectDequeue(rx_count.U)
            rx_count += 1
          }
        }.join()
      }
    }
  }
}
