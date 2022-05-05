package packet.packetbuf

import chisel3._
import chisel3.experimental.BundleLiterals.AddBundleLiteralConstructor
import chisel3.tester.{decoupledToDriver, testableClock, testableData}
import chiseltest.ChiselScalatestTester
import chiseltest.simulator.{VerilatorBackendAnnotation, WriteVcdAnnotation}
import org.scalatest.freespec.AnyFreeSpec
import packet.generic.{Memgen1R1W, Memgen1RW, VerilogMemgen1RW}
import packet._

import scala.Console.in

class SinglePortTester extends AnyFreeSpec with ChiselScalatestTester {
  "sp send a packet" in {
    val readClients = 4
    val writeClients = 4
    val conf = new BufferConfig(new Memgen1R1W(), new VerilogMemgen1RW(), 1, 8, 4, 4, readClients, writeClients, MTU=2048, credit=1, PacketBuffer2Port=false)

    test(new BufferComplexTestbench(conf, writeDepth = readClients, readDepth = writeClients)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) {
      c => {
        var pid : Int = 1

        for (src <- 0 until writeClients) {
          for (dst <- 0 until readClients) {
            c.io.req.valid.poke(true.B)
            c.io.req.bits.src.poke(src.U)
            c.io.req.bits.dst.poke(dst.U)
            c.io.req.bits.pid.poke(pid.U)
            c.io.req.bits.length.poke(8.U)
            c.clock.step(1)
            pid += 1
          }
        }

        c.io.req.valid.poke(false.B)
        c.clock.step(150)
        c.io.error.expect(false.B)
        c.io.expQueueEmpty.expect(true.B)
      }
    }
  }

  "work with multiple pools with 1p" in {
    for (numPools <- List(2)) {
      val readClients = 4
      val writeClients = 4
      val conf = new BufferConfig(new Memgen1R1W(), new VerilogMemgen1RW(), numPools, 8,
        4, 4, readClients, writeClients, MTU = 2048, credit = 2, ReadWordBuffer=4, PacketBufferReadLatency = 1)

      test(new BufferComplexTestbench(conf, writeDepth = readClients, readDepth = writeClients)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) {
        c => {
          var pid : Int = 1

          for (src <- 0 until writeClients) {
            for (dst <- 0 until readClients) {
              c.io.req.valid.poke(true.B)
              c.io.req.bits.src.poke(src.U)
              c.io.req.bits.dst.poke(dst.U)
              c.io.req.bits.pid.poke(pid.U)
              c.io.req.bits.length.poke((8+writeClients*4+readClients).U)
              c.clock.step(1)
              pid += 1
            }
          }

          c.io.req.valid.poke(false.B)

          c.clock.step(300)
          c.io.error.expect(false.B)
          c.io.expQueueEmpty.expect(true.B)
        }
      }
    }
  }

}
