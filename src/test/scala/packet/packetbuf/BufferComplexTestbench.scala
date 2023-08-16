package packet.packetbuf

import chisel.lib.dclib.DCDemux
import chisel3._
import chisel3.util.ImplicitConversions.intToUInt
import chisel3.util._
import packet.test.{PacketReceiver, PacketRequest, PacketSender}
import chiseltest._

class BufferComplexTestbench(conf : BufferConfig, writeDepth : Int, readDepth : Int) extends Module {
  val io = IO(new Bundle {
    val req = Flipped(ValidIO(new PacketRequest))
    val error = Output(Bool())
    val expQueueEmpty = Output(Bool())
    val pagesUsed = Output(UInt(log2Ceil(conf.totalPages+1).W))
    val pageDropThres = Input(UInt(log2Ceil(conf.MaxPagesPerPort+1).W))
    val packetDropThres = Input(UInt(log2Ceil(conf.MaxPacketsPerPort+1).W))
    val status = new TopBufferStatus(conf)
    val receivePacketCount = Output(Vec(conf.ReadClients, UInt(16.W)))
    val totalReceiveCount = Output(UInt(16.W))
    val dropPacketCount = Output(Vec(conf.ReadClients, UInt(16.W)))
    val totalDropCount = Output(UInt(16.W))
    val totalPacketCount = Output(UInt(16.W))
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
  val dropPacketCount = RegInit(VecInit(for (i <- 0 until conf.ReadClients) yield 0.U(16.W)))

  io.status := buffer.io.status
  buffer.io.config.dropQueueConfig.maxActivePortThreshold := conf.ReadClients

  for (i <- 0 until conf.WriteClients) {
    senders(i).io.packetData <> buffer.io.portDataIn(i)
    senders(i).io.sendPacket <> senderMux.io.p(i)
    senders(i).io.destIn <> buffer.io.destIn(i)
    senders(i).io.id := i.U
  }
  senderMux.io.c.valid := io.req.valid
  senderMux.io.c.bits := io.req.bits
  senderMux.io.sel := io.req.bits.src
  io.pagesUsed := buffer.io.status.flat.buffer.pagesPerPort.reduce(_ +& _)
  io.totalReceiveCount := io.receivePacketCount.reduce(_ +& _)

  for (i <- 0 until conf.ReadClients) {
    receivers(i).io.packetData <> buffer.io.portDataOut(i)
    receivers(i).io.sendPacket <> receiverMux.io.p(i)
    errorBits(i) := receivers(i).io.error
    emptyBits(i) := receivers(i).io.expQueueEmpty
    receivers(i).io.id := i.U
    buffer.io.config.dropQueueConfig.pageDropThreshold(i) := io.pageDropThres
    buffer.io.config.dropQueueConfig.packetDropThreshold(i) := io.packetDropThres
    io.receivePacketCount(i) := receivers(i).io.receivePacketCount
    when (buffer.io.status.flat.dropQueueStatus.tailDropInc(i)) {
      dropPacketCount(i) := dropPacketCount(i) + 1.U
    }
  }
  io.dropPacketCount := dropPacketCount
  io.totalDropCount := io.dropPacketCount.reduce(_ +& _)
  io.totalPacketCount := io.totalDropCount + io.totalReceiveCount

  when (io.req.bits.dst < conf.ReadClients.U) {
    receiverMux.io.c.valid := io.req.valid
  }.otherwise {
    receiverMux.io.c.valid := 0.B
  }
  receiverMux.io.c.bits := io.req.bits
  receiverMux.io.sel := io.req.bits.dst

  errorReg := errorReg | Cat(errorBits).orR()
  io.error := errorReg
  io.expQueueEmpty := Cat(emptyBits).andR()

  when (statusCount === 100.U) {
    statusCount := 0.U
    for (i <- 0 until conf.WriteClients) {
      printf("INFO : TX%d using %d pages\n", i.U, buffer.io.status.flat.buffer.pagesPerPort(i))
    }
    printf("INFO : Total pages used=%d\n", io.pagesUsed)
  }.otherwise {
    statusCount := statusCount + 1.U
  }

  def getConf = conf
}

object BufferComplexTestbench {

  /** Set drop threshold for all queues */
  def setDropThresholds(b : BufferComplexTestbench, pkt : Int, pages : Int) = {
    b.io.packetDropThres.poke(pkt.U)
    b.io.pageDropThres.poke(pages.U)
  }
}