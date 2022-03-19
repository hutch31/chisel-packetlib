package packet.axis


import chisel3._
import chisel3.tester.experimental.TestOptionBuilder.ChiselScalatestOptionBuilder
import chisel3.tester.{testableClock, testableData}
import chiseltest.ChiselScalatestTester
import chiseltest.internal.WriteVcdAnnotation
import org.scalatest.{FlatSpec, Matchers}

class AxisTester extends FlatSpec with ChiselScalatestTester with Matchers {
  behavior of "Testers2"

  it should "convert packets correctly" in {
    val width = 8
    test(new AxisTestbench(width)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        for (len <- width + 1 to width * 8) {
          c.io.req.valid.poke(true.B)
          c.io.req.bits.packetGood.poke(true.B)
          c.io.req.bits.length.poke(len.U)
          c.clock.step(1)
          c.io.req.valid.poke(false.B)
          c.clock.step(len / width + 5)
          c.io.error.expect(false.B)
        }
      }
    }
  }
}
