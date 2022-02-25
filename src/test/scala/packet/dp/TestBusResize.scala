package packet.dp

import chisel3._
import chisel3.tester.experimental.TestOptionBuilder.ChiselScalatestOptionBuilder
import chisel3.tester.testableData
import chiseltest.ChiselScalatestTester
import chiseltest.internal.WriteVcdAnnotation
import org.scalatest.{FlatSpec, Matchers}

class TestBusResize extends FlatSpec with ChiselScalatestTester with Matchers{
  behavior of "Testers2"

  it should "accept multiple words until full" in {
    test (new BusUpsize(2, 4)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        c.io.in.valid.poke(true.B)
      }
    }
  }
}
