package packet.dp

import chisel3._
import chisel3.tester.experimental.TestOptionBuilder.ChiselScalatestOptionBuilder
import chisel3.tester.{testableClock, testableData}
import chiseltest.ChiselScalatestTester
import chiseltest.internal.WriteVcdAnnotation
import org.scalatest.{FlatSpec, Matchers}
import packet._

class TestBusResize extends FlatSpec with ChiselScalatestTester with Matchers {
  behavior of "Testers2"

  it should "accept multiple words until full" in {
    test(new BusUpsize(2, 4)).withAnnotations(Seq(WriteVcdAnnotation)) {
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

        c.io.out.ready.poke(true.B)
        c.io.in.ready.expect(true.B)
        c.io.in.bits.data(0).poke(4.U)
        c.io.in.bits.data(1).poke(5.U)

        // first double word of data should be valid
        c.io.out.valid.expect(true.B)
        for (i <- 0 to 3) {
          c.io.out.bits.data(i).expect(i.U)
        }

        c.clock.step(1)
        c.io.in.valid.poke(false.B)


        c.clock.step(1)
        c.io.out.valid.expect(false.B)

        // Send in one more word
        c.io.in.valid.poke(true.B)
        c.io.in.bits.data(0).poke(6.U)
        c.io.in.bits.data(1).poke(7.U)

        c.clock.step(1)
        c.io.in.valid.poke(false.B)

        c.io.out.valid.expect(true.B)

        for (i <- 0 to 3) {
          c.io.out.bits.data(i).expect((i + 4).U)
        }
      }
    }
  }

  it should "upsize propagate SOP" in {
    test(new BusUpsize(1, 4)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        c.io.in.valid.poke(true.B)
        for (i <- 0 to 3) {
          if (i == 0) {
            c.io.in.bits.code.code.poke(packetSop)
          } else {
            c.io.in.bits.code.code.poke(packetBody)
          }
          c.io.in.bits.data(0).poke(i.U)

          c.clock.step(1)
        }

        c.io.out.valid.expect(true.B)
        c.io.out.bits.code.code.expect(packetSop)
      }
    }
  }

  it should "upsize propagate EOP" in {
    test(new BusUpsize(1, 4)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        for (endWord <- 0 to 3) {
          c.io.in.valid.poke(true.B)
          for (i <- 0 to endWord) {
            if (i == endWord) {
              c.io.in.bits.code.code.poke(packetGoodEop)
            } else {
              c.io.in.bits.code.code.poke(packetBody)
            }
            c.io.in.bits.data(0).poke(i.U)

            c.clock.step(1)
          }

          c.io.out.valid.expect(true.B)
          c.io.out.ready.poke(true.B)
          c.io.out.bits.count.expect(endWord.U)
          c.io.out.bits.code.code.expect(packetGoodEop)
          c.io.in.valid.poke(false.B)
          c.clock.step(1)
        }
      }
    }
  }

  it should "split body word into multiple words" in {
    test(new BusDownsize(4, 1)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        c.io.in.valid.poke(true.B)
        c.io.in.ready.expect(true.B)
        c.io.in.bits.code.code.poke(packetBody)
        for (i <- 0 to 3) {
          c.io.in.bits.data(i).poke(i.U)
        }

        c.clock.step(1)
        c.io.in.valid.poke(false.B)

        for (i <- 0 to 3) {
          c.io.out.ready.poke(true.B)
          c.io.out.valid.expect(true.B)
          c.io.out.bits.data(0).expect(i.U)
          c.io.out.bits.code.code.expect(packetBody)

          c.io.in.ready.expect((i == 3).B)

          c.clock.step(1)
        }

        c.io.in.ready.expect(true.B)
        c.io.out.valid.expect(false.B)
      }
    }
  }

  it should "stop on EOP" in {
    for (endWord <- 0 to 3) {
      test(new BusDownsize(4, 1)).withAnnotations(Seq(WriteVcdAnnotation)) {
        c => {
          c.io.in.valid.poke(true.B)
          c.io.in.ready.expect(true.B)
          c.io.in.bits.code.code.poke(packetGoodEop)
          c.io.in.bits.count.poke(endWord.U)

          for (i <- 0 to 3) {
            c.io.in.bits.data(i).poke(i.U)
          }

          c.clock.step(1)
          c.io.in.valid.poke(false.B)

          for (i <- 0 to endWord) {
            c.io.out.ready.poke(true.B)
            c.io.out.valid.expect(true.B)
            c.io.out.bits.data(0).expect(i.U)

            if (i == endWord) {
              c.io.in.ready.expect(true.B)
              c.io.out.bits.code.code.expect(packetGoodEop)
            } else {
              c.io.out.bits.code.code.expect(packetBody)
              c.io.in.ready.expect(false.B)
            }
            c.clock.step(1)
          }

          c.io.in.ready.expect(true.B)
          c.io.out.valid.expect(false.B)
        }
      }
    }
  }

  it should "start on SOP" in {
    test(new BusDownsize(4, 1)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        c.io.in.valid.poke(true.B)
        c.io.in.ready.expect(true.B)
        c.io.in.bits.code.code.poke(packetSop)
        c.io.in.bits.count.poke(0.U)

        for (i <- 0 to 3) {
          c.io.in.bits.data(i).poke(i.U)
        }

        c.clock.step(1)
        c.io.in.valid.poke(false.B)

        for (i <- 0 to 3) {
          c.io.out.ready.poke(true.B)
          c.io.out.valid.expect(true.B)
          c.io.out.bits.data(0).expect(i.U)

          if (i == 0) {
            c.io.out.bits.code.code.expect(packetSop)
          } else {
            c.io.out.bits.code.code.expect(packetBody)
          }
          c.io.in.ready.expect((i == 3).B)
          c.clock.step(1)
        }

        c.io.in.ready.expect(true.B)
        c.io.out.valid.expect(false.B)
      }
    }
  }
}
