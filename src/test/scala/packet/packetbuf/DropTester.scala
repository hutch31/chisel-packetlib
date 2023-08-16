package packet.packetbuf

import chisel3._
import chiseltest._
import org.scalatest._
import org.scalatest.freespec.AnyFreeSpec
import packet.generic.{Memgen1R1W, Memgen1RW, VerilogMemgen1RW}

class DropTester extends AnyFreeSpec with ChiselScalatestTester {
  "drop packets" in {
    val readClients = 4
    val writeClients = 4
    val conf = new BufferConfig(new TestMemgen1R1W(), new TestMemgen1RW(), 1, 12, 4, 4,
      readClients, writeClients, MTU=2048, credit=1, PacketBuffer2Port=false, MemControl = new TestMemoryControl)

    test(new BufferComplexTestbench(conf, writeDepth = readClients, readDepth = writeClients)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) {
      c => {
        var pid : Int = 0

        c.io.pageDropThres.poke (10.U)
        c.io.packetDropThres.poke (2.U)

        def send_packet(src : Int, dst : Int) = {
          c.io.req.valid.poke(true.B)
          c.io.req.bits.src.poke(src.U)
          c.io.req.bits.dst.poke(dst.U)
          c.io.req.bits.pid.poke(pid.U)
          c.io.req.bits.length.poke(8.U)
          c.clock.step(1)
          c.io.req.valid.poke(false.B)
          pid += 1
        }

        // send 1 packet to each port
        for (i <- 0 until readClients) {
          send_packet(i, i)
        }

        // send 1 drop packet to each port
        for (i <- 0 until readClients) {
          send_packet(i, 100)
        }

        // send 1 packet to each port
        for (i <- 0 until readClients) {
          send_packet(i, i)
        }

        c.clock.step(200)
      }
    }
  }

  "drop packets past packet threshold" in {
    val readClients = 4
    val writeClients = 4
    val numPackets = 5
    val conf = BufferConfig(new TestMemgen1R1W(), new TestMemgen1RW(), 1, 12, 4, 4,
      readClients, writeClients, MTU=2048, credit=1, PacketBuffer2Port=false, MemControl = new TestMemoryControl)

    test(new BufferComplexTestbench(conf, writeDepth = numPackets, readDepth = numPackets)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) {
      c => {
        var pid : Int = 0

        c.io.pageDropThres.poke (10.U)
        c.io.packetDropThres.poke (2.U)

        def send_packet(src : Int, dst : Int) = {
          c.io.req.valid.poke(true.B)
          c.io.req.bits.src.poke(src.U)
          c.io.req.bits.dst.poke(dst.U)
          c.io.req.bits.pid.poke(pid.U)
          c.io.req.bits.length.poke(16.U)
          c.io.req.bits.packetGood.poke(1)
          c.clock.step(1)
          c.io.req.valid.poke(false.B)
          pid += 1
        }

        // send 5 packets from each port to port 0
        for (j <- 0 until numPackets) {
          for (i <- 0 until readClients) {
            send_packet(i, 0)
          }
        }

        c.clock.step(500)

        c.io.totalPacketCount.expect(pid.U)
        assert(c.io.totalDropCount.peekInt() > 0)
      }
    }
  }
}
