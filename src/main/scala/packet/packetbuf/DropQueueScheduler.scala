package packet.packetbuf

import chisel.lib.dclib.{CreditIO, DCArbiter, DCCreditReceiver, DCCreditSender, DCMirror, DcMcCrossbar}
import chisel3._
import chisel3.util._
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

  io.schedIn <> credRx.io.enq
  credRx.io.deq <> packetRep.io.c
  packetRep.io.dst := credRx.io.deq.bits.dest

  dropMux.io.p <> dropQueue.io.enq

  for (out <- 0 until c.ReadClients) {
    io.dropQueueStatus.outputQueueSize(out) := outputQ(out).io.usage
    io.dropQueueStatus.tailDropInc(out) := 0.B
    outputQ(out).io.enq <> packetRep.io.p(out)
    outputQ(out).io.memControl <> io.memControl(out)
    dropMux.io.c(out).valid := 0.B
    dropMux.io.c(out).bits := packetRep.io.p(out).bits

    val overDropThresh = outputQ(out).io.usage >= io.dropQueueConfig.packetDropThreshold(out) || portPageCount(out) >= io.dropQueueConfig.pageDropThreshold(out)

    when (overDropThresh) {
      outputQ(out).io.enq.valid := 0.B
      dropMux.io.c(out).valid := 1.B
      packetRep.io.p(out).ready := dropMux.io.c(out).ready
    }
    io.dropQueueStatus.tailDropInc(out) := RegNext(dropMux.io.c(out).fire, init=0.B)

    when (outputQ(out).io.enq.fire && outputQ(out).io.deq.fire) {
      portPageCount(out) := portPageCount(out) + outputQ(out).io.enq.bits.pageCount - outputQ(out).io.deq.bits.pageCount
    }.elsewhen(outputQ(out).io.enq.fire) {
      portPageCount(out) := portPageCount(out) + outputQ(out).io.enq.bits.pageCount
    }.elsewhen(outputQ(out).io.deq.fire) {
      portPageCount(out) := portPageCount(out) - outputQ(out).io.deq.bits.pageCount
    }

    credTx(out).io.enq <> outputQ(out).io.deq
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