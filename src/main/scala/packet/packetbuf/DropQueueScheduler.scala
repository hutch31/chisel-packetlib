package packet.packetbuf

import chisel.lib.dclib.{CreditIO, DCArbiter, DCCreditReceiver, DCCreditSender, DCDemux, DCMirror, DcMcCrossbar}
import chisel3._
import chisel3.util._
import packet.Spigot
import packet.generic.MemQueueSP

class DropQueueScheduler (c : BufferConfig) extends Module {
  require(c.HasDropPort == true)

  val io = IO(new Bundle {
    val schedIn = Flipped(new CreditIO(new SchedulerReq(c)))
    val schedOut = Vec(c.ReadClients, new CreditIO(new SchedulerReq(c)))
    val dropOut = new CreditIO(new SchedulerReq(c))
    val dropQueueConfig = Input(new DropQueueConfig(c))
    val dropQueueStatus = Output(new DropQueueStatus(c))
    val memControl = Vec(c.schedMemoryCount, c.MemControl.factory)
  })
  val credRx = Module(new DCCreditReceiver(new SchedulerReq(c), c.credit))
  val credTx = for (i <- 0 until c.ReadClients) yield Module(new DCCreditSender(new SchedulerReq(c), c.readerSchedCredit))
  val packetRep = Module(new DCMirror(new SchedulerReq(c), c.ReadClients))
  val dropMux = Module(new DCArbiter(new SchedulerReq(c), c.ReadClients, false))
  val outputQ = for (i <- 0 until c.ReadClients) yield Module(new MemQueueSP(new SchedulerReq(c), c.MaxPacketsPerPort, c.mgen1p, c.MemControl, c.DropQueueReadLatency))
  val portPageCount = RegInit(VecInit(for (i <- 0 until c.ReadClients) yield 0.U(log2Ceil(c.MaxPagesPerPort+1).W)))
  val dropSender = Module(new DCCreditSender(new SchedulerReq(c), c.credit))
  val dropQueue = Module(new Queue(new SchedulerReq(c), c.ReadClients))
  val activeRequests = Reverse(Cat(for (i <- 0 until c.ReadClients) yield outputQ(i).io.deq.valid))
  val activePackets = PopCount(Cat(for (i <- 0 until c.ReadClients) yield credTx(i).io.curCredit === 0.U))
  val maxGrantCount = Module(new MaxGrantCount(c.ReadClients))

  val maxGrant = io.dropQueueConfig.maxActivePortThreshold - activePackets
  maxGrantCount.io.maxGrant := maxGrant
  maxGrantCount.io.requests := activeRequests

  io.schedIn <> credRx.io.enq
  credRx.io.deq <> packetRep.io.c
  packetRep.io.dst := credRx.io.deq.bits.dest

  dropMux.io.p <> dropQueue.io.enq

  for (out <- 0 until c.ReadClients) {
    val dropSelect = Module(new DCDemux(new SchedulerReq(c), 2))
    io.dropQueueStatus.outputQueueSize(out) := outputQ(out).io.usage
    io.dropQueueStatus.tailDropInc(out) := 0.B
    dropSelect.io.c <> packetRep.io.p(out)
    dropSelect.io.p(0) <> outputQ(out).io.enq
    dropSelect.io.p(1) <> dropMux.io.c(out)
    outputQ(out).io.memControl <> io.memControl(out)

    val overDropThresh = outputQ(out).io.usage >= io.dropQueueConfig.packetDropThreshold(out) || portPageCount(out) >= io.dropQueueConfig.pageDropThreshold(out)
    dropSelect.io.sel := overDropThresh
    io.dropQueueStatus.tailDropInc(out) := RegNext(dropMux.io.c(out).fire, init=0.B)

    when (outputQ(out).io.enq.fire && outputQ(out).io.deq.fire) {
      portPageCount(out) := portPageCount(out) + outputQ(out).io.enq.bits.pageCount - outputQ(out).io.deq.bits.pageCount
    }.elsewhen(outputQ(out).io.enq.fire) {
      portPageCount(out) := portPageCount(out) + outputQ(out).io.enq.bits.pageCount
    }.elsewhen(outputQ(out).io.deq.fire) {
      portPageCount(out) := portPageCount(out) - outputQ(out).io.deq.bits.pageCount
    }

    Spigot(outputQ(out).io.deq, credTx(out).io.enq, maxGrantCount.io.grants(out))
    io.schedOut(out) <> credTx(out).io.deq
    io.dropQueueStatus.outputQueueSize(out) := outputQ(out).io.usage
  }

  dropQueue.io.deq <> dropSender.io.enq
  dropSender.io.deq <> io.dropOut
  io.dropQueueStatus.outputPageSize := portPageCount
}

class DropSchedulerInputMux(c : BufferConfig) extends Module {
  val io = IO(new Bundle {
    val schedIn = Vec(c.WriteClients, Flipped(new CreditIO(new SchedulerReq(c))))
    val schedOut = new CreditIO(new SchedulerReq(c))
  })
  val credRx = for (i <- 0 until c.WriteClients) yield Module(new DCCreditReceiver(new SchedulerReq(c), c.credit))
  val credTx = Module(new DCCreditSender(new SchedulerReq(c), c.credit))
  val arb = Module(new DCArbiter(new SchedulerReq(c), c.WriteClients, false))

  for (i <- 0 until c.WriteClients) {
    credRx(i).io.enq <> io.schedIn(i)
    credRx(i).io.deq <> arb.io.c(i)
  }
  arb.io.p <> credTx.io.enq
  credTx.io.deq <> io.schedOut
}