package packet.packetbuf

import chisel.lib.dclib.DCDemux
import chisel3._
import chisel3.tester._
import chiseltest.{ChiselScalatestTester, WriteVcdAnnotation}
import org.scalatest.freespec.AnyFreeSpec
import packet.generic.{Memgen1R1W, Memgen1RW}
import packet._

class BufferComplexTester extends AnyFreeSpec with ChiselScalatestTester{
   "send a packet" in {
    val readClients = 4
    val writeClients = 4
    val conf = new BufferConfig(new Memgen1R1W(), new Memgen1RW(), 1, 8, 4, 4, readClients, writeClients, MTU=2048, credit=1, MaxReferenceCount = 4)

    test(new BufferComplexTestbench(conf, writeDepth = readClients, readDepth = writeClients)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) {
      c => {
        var pid : Int = 1

        for (src <- 0 until writeClients) {
          for (dst <- 0 until readClients) {
            c.io.req.valid.poke(true.B)
            c.io.req.bits.src.poke(src.U)
            c.io.req.bits.dst.poke(dst.U)
            c.io.req.bits.pid.poke(pid.U)
            c.io.req.bits.length.poke((8+pid).U)
            c.clock.step(1)
            pid += 1
          }
        }

        c.io.req.valid.poke(false.B)
        c.clock.step(350)
        c.io.error.expect(false.B)
        c.io.pagesUsed.expect(readClients.U)
        c.io.expQueueEmpty.expect(true.B)
      }
    }
  }

   "link two pages" in {
    val readClients = 2
    val writeClients = 2
    val conf = new BufferConfig(new Memgen1R1W(), new Memgen1RW(), 1, 8, 4, 4, readClients, writeClients, MTU = 2048, credit = 4, ReadWordBuffer=4, PacketBufferReadLatency = 2)

    test(new BufferComplexTestbench(conf, writeDepth = readClients, readDepth = writeClients)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        // send three 32B packets
        c.io.req.valid.poke(true.B)
        c.io.req.bits.src.poke(0.U)
        c.io.req.bits.dst.poke(1.U)
        c.io.req.bits.pid.poke(1.U)
        c.io.req.bits.length.poke(32.U)
        c.clock.step(1)

        c.io.req.valid.poke(true.B)
        c.io.req.bits.src.poke(1.U)
        c.io.req.bits.dst.poke(0.U)
        c.io.req.bits.pid.poke(2.U)
        c.io.req.bits.length.poke(32.U)
        c.clock.step(1)

        c.io.req.valid.poke(true.B)
        c.io.req.bits.src.poke(0.U)
        c.io.req.bits.dst.poke(1.U)
        c.io.req.bits.pid.poke(3.U)
        c.io.req.bits.length.poke(32.U)
        c.clock.step(1)

        c.io.req.valid.poke(false.B)

        c.clock.step(100)
        c.io.error.expect(false.B)
        c.io.expQueueEmpty.expect(true.B)

      }
    }
  }

   "have different writers and readers" in {
    for ((writeClients, readClients) <- List((2,5), (3,4), (5,2), (8,3), (3,8))) {
    //for ((writeClients, readClients) <- List(((3,8)))) {
      val cycles = 50 + readClients * writeClients * 10
      val conf = new BufferConfig(new Memgen1R1W(), new Memgen1RW(), 1, 16, 4, 4, readClients, writeClients, MTU=2048, credit=1)

      test(new BufferComplexTestbench(conf, writeDepth = readClients, readDepth = writeClients)).withAnnotations(Seq(WriteVcdAnnotation)) {
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
          c.clock.step(cycles)
          c.io.error.expect(false.B)
          c.io.expQueueEmpty.expect(true.B)
        }
      }
    }
  }

   "work with high memory latency" in {
    for (memLatency <- List(2, 3, 5, 8)) {
      val readClients = 2
      val writeClients = 2
      val conf = new BufferConfig(new Memgen1R1W(), new Memgen1RW(), 1, 8, 4, 4, readClients, writeClients, MTU = 2048, credit = 4, ReadWordBuffer=4, PacketBufferReadLatency = memLatency)


      test(new BufferComplexTestbench(conf, writeDepth = readClients, readDepth = writeClients)).withAnnotations(Seq(WriteVcdAnnotation)) {
        c => {
          for (p <- 1 to 5) {
            c.io.req.valid.poke(true.B)
            c.io.req.bits.src.poke(0.U)
            c.io.req.bits.dst.poke(1.U)
            c.io.req.bits.pid.poke(p.U)
            c.io.req.bits.length.poke(32.U)
            c.clock.step(1)
          }
          c.io.req.valid.poke(false.B)

          c.clock.step(150)
          c.io.error.expect(false.B)
          c.io.expQueueEmpty.expect(true.B)
        }
      }
    }
  }

   "work with multiple pools" in {
    for (numPools <- List(2)) {
      val readClients = 4
      val writeClients = 4
      val conf = new BufferConfig(new Memgen1R1W(), new Memgen1RW(), numPools, 8, 4, 4, readClients, writeClients, MTU = 2048, credit = 4, ReadWordBuffer=4, PacketBufferReadLatency = 1)

      test(new BufferComplexTestbench(conf, writeDepth = readClients, readDepth = writeClients)).withAnnotations(Seq(WriteVcdAnnotation)) {
        c => {
          for (p <- 1 to 5) {
            c.io.req.valid.poke(true.B)
            c.io.req.bits.src.poke(0.U)
            c.io.req.bits.dst.poke(1.U)
            c.io.req.bits.pid.poke(p.U)
            c.io.req.bits.length.poke(32.U)
            c.clock.step(1)
          }
          c.io.req.valid.poke(false.B)

          c.clock.step(200)
          c.io.error.expect(false.B)
          c.io.expQueueEmpty.expect(true.B)
        }
      }
    }
  }
}
