package packet.packetbuf

import chisel.lib.dclib.DCDemux
import chisel3._
import chisel3.util._
import packet.test.{PacketReceiver, PacketRequest, PacketSender}

class BufferComplexTestbench(conf : BufferConfig, writeDepth : Int, readDepth : Int) extends Module {
  val io = IO(new Bundle {
    val req = Flipped(ValidIO(new PacketRequest))
    val error = Output(Bool())
    val expQueueEmpty = Output(Bool())
    val pagesUsed = Output(UInt(log2Ceil(conf.totalPages+1).W))
  })
  val buffer = Module(new FlatPacketBufferComplex(conf))
  val senders = for (i <- 0 until conf.WriteClients) yield Module(new PacketSender(conf.WordSize, conf.ReadClients, writeDepth))
  val receivers = for (i <- 0 until conf.ReadClients) yield Module(new PacketReceiver(conf.WordSize, conf.WriteClients, readDepth))
  val senderMux = Module(new DCDemux(new PacketRequest, conf.WriteClients))
  val receiverMux = Module(new DCDemux(new PacketRequest, conf.ReadClients))
  val errorBits = Wire(Vec(conf.ReadClients, Bool()))
  val emptyBits = Wire(Vec(conf.ReadClients, Bool()))
  val errorReg = RegInit(init=false.B)
  val statusCount = RegInit(init=0.U(8.W))

  for (i <- 0 until conf.WriteClients) {
    senders(i).io.packetData <> buffer.io.portDataIn(i)
    senders(i).io.sendPacket <> senderMux.io.p(i)
    senders(i).io.destIn <> buffer.io.destIn(i)
    senders(i).io.id := i.U
  }
  senderMux.io.c.valid := io.req.valid
  senderMux.io.c.bits := io.req.bits
  senderMux.io.sel := io.req.bits.src
  io.pagesUsed := buffer.io.status.pagesPerPort.reduce(_ +& _)

  for (i <- 0 until conf.ReadClients) {
    receivers(i).io.packetData <> buffer.io.portDataOut(i)
    receivers(i).io.sendPacket <> receiverMux.io.p(i)
    errorBits(i) := receivers(i).io.error
    emptyBits(i) := receivers(i).io.expQueueEmpty
    receivers(i).io.id := i.U
  }
  receiverMux.io.c.valid := io.req.valid
  receiverMux.io.c.bits := io.req.bits
  receiverMux.io.sel := io.req.bits.dst

  errorReg := errorReg | Cat(errorBits).orR()
  io.error := errorReg
  io.expQueueEmpty := Cat(emptyBits).andR()

  when (statusCount === 100.U) {
    statusCount := 0.U
    for (i <- 0 until conf.WriteClients) {
      printf("INFO : TX%d using %d pages\n", i.U, buffer.io.status.pagesPerPort(i))
    }
    printf("INFO : Total pages used=%d\n", io.pagesUsed)
  }.otherwise {
    statusCount := statusCount + 1.U
  }
}

