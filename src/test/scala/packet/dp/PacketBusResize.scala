package packet.dp


import chisel3._
import chisel3.tester.{testableClock, testableData}
import chiseltest.{ChiselScalatestTester, WriteVcdAnnotation}
import org.scalatest.freespec.AnyFreeSpec

class PacketBusResize extends AnyFreeSpec with ChiselScalatestTester {

  "receive correct packet size on upsize" in {
    val in_width = 4
    val out_width = 48
    test(new ResizeTestbench(in_width, out_width)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        for (len <- out_width + 1 to out_width * 4) {
          c.io.req.valid.poke(true.B)
          c.io.req.bits.packetGood.poke(true.B)
          c.io.req.bits.length.poke(len.U)
          c.clock.step(1)
          c.io.req.valid.poke(false.B)
          c.clock.step(len / in_width + 5)
          c.io.error.expect(false.B)
        }
      }
    }
  }

  "receive correct packet size on downsize" in {
    val in_width = 48
    val out_width = 4
    test(new ResizeTestbench(in_width, out_width)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        for (len <- in_width + 1 to in_width * 4) {
          c.io.req.valid.poke(true.B)
          c.io.req.bits.packetGood.poke(true.B)
          c.io.req.bits.length.poke(len.U)
          c.clock.step(1)
          c.io.req.valid.poke(false.B)
          c.clock.step(len / out_width + 5)
          c.io.error.expect(false.B)
        }
      }
    }
  }
}
