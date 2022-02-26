package packet.packetbuf

import Chisel.Cat
import chisel.lib.dclib.DCDemux
import chisel3._
import chisel3.experimental.BundleLiterals.AddBundleLiteralConstructor
import chisel3.tester.{decoupledToDriver, testableClock, testableData}
import chisel3.tester.experimental.TestOptionBuilder.ChiselScalatestOptionBuilder
import chisel3.util.ImplicitConversions.intToUInt
import chisel3.util.ValidIO
import chiseltest.ChiselScalatestTester
import chiseltest.internal.WriteVcdAnnotation
import org.scalatest.{FlatSpec, Matchers}
import packet.generic.Memgen1R1W
import packet.{PacketCode, PacketData}
import packet.packet.packetSop

class BufferComplexTestbench(conf : BufferConfig, writeDepth : Int, readDepth : Int) extends Module {
  val io = IO(new Bundle {
    val req = Flipped(ValidIO(new PacketRequest))
    val error = Output(Bool())
    val expQueueEmpty = Output(Bool())
  })
  val buffer = Module(new FlatPacketBufferComplex(conf))
  val senders = for (i <- 0 until conf.WriteClients) yield Module(new PacketSender(conf.WordSize, conf.ReadClients, writeDepth))
  val receivers = for (i <- 0 until conf.ReadClients) yield Module(new PacketReceiver(conf.WordSize, conf.WriteClients, readDepth))
  val senderMux = Module(new DCDemux(new PacketRequest, conf.WriteClients))
  val receiverMux = Module(new DCDemux(new PacketRequest, conf.ReadClients))
  val errorBits = Wire(Vec(conf.ReadClients, Bool()))
  val emptyBits = Wire(Vec(conf.ReadClients, Bool()))
  val errorReg = RegInit(init=false.B)

  for (i <- 0 until conf.WriteClients) {
    senders(i).io.packetData <> buffer.io.portDataIn(i)
    senders(i).io.sendPacket <> senderMux.io.p(i)
    senders(i).io.destIn <> buffer.io.destIn(i)
  }
  senderMux.io.c.valid := io.req.valid
  senderMux.io.c.bits := io.req.bits
  senderMux.io.sel := io.req.bits.src

  for (i <- 0 until conf.ReadClients) {
    receivers(i).io.packetData <> buffer.io.portDataOut(i)
    receivers(i).io.sendPacket <> receiverMux.io.p(i)
    errorBits(i) := receivers(i).io.error
    emptyBits(i) := receivers(i).io.expQueueEmpty
  }
  receiverMux.io.c.valid := io.req.valid
  receiverMux.io.c.bits := io.req.bits
  receiverMux.io.sel := io.req.bits.dst

  errorReg := errorReg | Cat(errorBits).orR()
  io.error := errorReg
  io.expQueueEmpty := Cat(emptyBits).andR()
}

class BufferComplexTester extends FlatSpec with ChiselScalatestTester with Matchers{
  behavior of "Testers2"

  it should "send a packet" in {
    val readClients = 4
    val writeClients = 4
    val conf = new BufferConfig(new Memgen1R1W(), 1, 8, 4, 4, readClients, writeClients, MTU=2048, credit=1)

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
        c.clock.step(150)
        c.io.error.expect(false.B)
        c.io.expQueueEmpty.expect(true.B)
      }
    }
  }

  it should "link two pages" in {
    val readClients = 2
    val writeClients = 2
    val conf = new BufferConfig(new Memgen1R1W, 1, 8, 4, 4, readClients, writeClients, MTU = 2048, credit = 4, ReadWordBuffer=4, PacketBufferReadLatency = 2)

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

  it should "have different writers and readers" in {
    for ((writeClients, readClients) <- List((2,5), (3,4), (5,2), (8,3), (3,8))) {
    //for ((writeClients, readClients) <- List(((3,8)))) {
      val cycles = 50 + readClients * writeClients * 10
      val conf = new BufferConfig(new Memgen1R1W(), 1, 16, 4, 4, readClients, writeClients, MTU=2048, credit=1)

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

  it should "work with high memory latency" in {
    for (memLatency <- List(2, 3, 5, 8)) {
      val readClients = 2
      val writeClients = 2
      val conf = new BufferConfig(new Memgen1R1W, 1, 8, 4, 4, readClients, writeClients, MTU = 2048, credit = 4, ReadWordBuffer=4, PacketBufferReadLatency = memLatency)


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

}
