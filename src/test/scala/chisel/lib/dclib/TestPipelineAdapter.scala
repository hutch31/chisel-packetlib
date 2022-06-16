package chisel.lib.dclib

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec

import scala.util.Random

class PipelineTesterBlock[D <: Data](data: D, depth : Int) extends Module {
  val io = IO(new Bundle {
    val enq = Flipped(Decoupled(data))
    val deq = Decoupled(data)
  })

  val data_in = Wire(ValidIO(data))
  val pipe = ShiftRegister(data_in, depth, en=1.B)
  val adap = Module(new DCPipelineAdapter(data, depth))

  adap.io.valid_in := io.enq.valid
  io.enq.ready := adap.io.ready_in

  data_in.valid := adap.io.valid_pipe_in
  data_in.bits := io.enq.bits

  adap.io.pipeOut := pipe
  io.deq <> adap.io.deq
}

class TestPipelineAdapter extends AnyFreeSpec with ChiselScalatestTester{
  "start and stop randomly" in {
    for (depth <- 1 until 10) {
      for (prob <- Seq((0.9, 0.2), (0.3, 0.3), (1.0, 1.0), (0.2, 0.9), (0.1, 0.1))) {
        test(new PipelineTesterBlock(UInt(16.W), depth)).withAnnotations(Seq(WriteVcdAnnotation)) {
          c => {
            c.io.enq.initSource().setSourceClock(c.clock)
            c.io.deq.initSink().setSinkClock(c.clock)
            val rand = new Random(1)

            val total_count = 100
            var tx_count: Int = 0
            var rx_count: Int = 0

            fork {
              while (tx_count < total_count) {
                if (rand.nextFloat() > prob._1) {
                  c.clock.step(1)
                }
                c.io.enq.enqueue(tx_count.U)
                tx_count += 1
              }
            }.fork {
              while (rx_count < total_count) {
                if (rand.nextFloat() > prob._2) {
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
}
