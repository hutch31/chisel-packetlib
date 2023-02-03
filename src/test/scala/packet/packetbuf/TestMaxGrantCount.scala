package packet.packetbuf

import chisel3._
import chiseltest._
import chiseltest.simulator.{VerilatorBackendAnnotation, WriteVcdAnnotation}
import org.scalatest.freespec.AnyFreeSpec

class TestMaxGrantCount extends AnyFreeSpec with ChiselScalatestTester{
  "limit number of grants" in {
    test(new MaxGrantCount(8)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        c.io.maxGrant.poke(4.U)
        c.io.requests.poke(0xFF.U)
        c.io.grants.expect(0xF.U)
        c.clock.step()
        c.io.grants.expect(0xF0.U)
        c.clock.step()
        c.io.requests.poke(0.U)
        c.clock.step()
        // rotate through each request
        c.io.requests.poke(0xFF.U)
        c.io.maxGrant.poke(1.U)
        for (i <- 0 to 7) {
          c.io.grants.expect((1 << i).U)
          c.clock.step()
        }

        // no grant when maxGrant zero
        c.io.maxGrant.poke(0.U)
        c.io.grants.expect(0.U)
        c.clock.step()

        // all requests granted when maxGrant large
        c.io.maxGrant.poke(8.U)
        c.io.grants.expect(0xFF.U)
      }
    }
  }
}
