package packet.dp

import chisel3._
import chisel3.tester.experimental.TestOptionBuilder.ChiselScalatestOptionBuilder
import chisel3.tester.{testableClock, testableData}
import chiseltest.ChiselScalatestTester
import chiseltest.internal.WriteVcdAnnotation
import org.scalatest.{FlatSpec, Matchers}
import packet.packet._

class TestBusResize extends FlatSpec with ChiselScalatestTester with Matchers{
  behavior of "Testers2"

  it should "accept multiple words until full" in {
    test (new BusUpsize(2, 4)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        // send in first word of data
        c.io.in.valid.poke(true.B)
        c.io.in.bits.code.code.poke(packetBody)
        c.io.in.bits.data(0).poke(0.U)
        c.io.in.bits.data(1).poke(1.U)
        c.io.in.ready.expect(true.B)

        c.clock.step(1)

        // send in second word of data
        c.io.in.bits.data(0).poke(2.U)
        c.io.in.bits.data(1).poke(3.U)
        c.io.in.ready.expect(true.B)

        c.io.out.ready.poke(true.B)

        // Send in third word of data
        // no flow control should be asserted
        c.clock.step(1)
        c.io.in.ready.expect(true.B)
        c.io.in.bits.data(0).poke(4.U)
        c.io.in.bits.data(1).poke(5.U)


        c.clock.step(1)
        c.io.in.valid.poke(false.B)

        // first double word of data should be valid
        c.io.out.valid.expect(true.B)
        for (i <- 0 until 3) {
          c.io.out.bits.data(i).expect(i.U)
        }

        c.clock.step(1)
        c.io.out.valid.expect(false.B)

        // Send in one more word
        c.io.in.valid.poke(true.B)
        c.io.in.bits.data(0).poke(6.U)
        c.io.in.bits.data(1).poke(7.U)

        c.clock.step(1)
        c.io.in.valid.poke(false.B)

        c.clock.step(1)
        c.io.out.valid.expect(true.B)

        for (i <- 0 until 3) {
          c.io.out.bits.data(i).expect((i+4).U)
        }

      }
    }
  }
}
