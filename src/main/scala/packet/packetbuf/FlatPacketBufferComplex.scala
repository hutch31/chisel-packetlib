package packet.packetbuf

import chisel.lib.dclib._
import chisel3._
import chisel3.util._
import packet.PacketData

class FlatPacketBufferComplex(c : BufferConfig) extends Module {
  val io = IO(new Bundle {
    val portDataOut = Vec(c.ReadClients, Decoupled(new PacketData(c.WordSize)))
    val portDataIn = Vec(c.WriteClients, Flipped(Decoupled(new PacketData(c.WordSize))))
    val destIn = Vec(c.WriteClients, Flipped(Decoupled(new RoutingResult(c.ReadClients))))
    val config = new BufferConfiguration(c)
    val status = new PktBufStatus(c)
    val pkt_count = Output(new PacketCounters(c))
    val readerSchedResult = Vec(c.ReadClients, ValidIO(new SchedulerReq(c)))
    val memControl = Vec(c.totalMemoryCount, c.MemControl.factory)
  })
  val readers = for (i <- 0 until c.ReadClients) yield Module(new PacketReader(c))
  val writers = for (i <- 0 until c.WriteClients) yield Module(new PacketWriter(c))
  val buffer = Module(new FlatPacketBuffer(c))
  val schedMux = Module(new DropSchedulerInputMux(c))
  val scheduler = Module(new DropQueueScheduler(c))

  val writePktInc = Wire(Vec(c.WriteClients, Bool()))
  val readPktInc = Wire(Vec(c.ReadClients, Bool()))
  for (i <- 0 until c.WriteClients) {
    writers(i).io.id := i.U
    buffer.io.writerInterface(i) <> writers(i).io.interface
    writers(i).io.portDataIn <> io.portDataIn(i)
    writers(i).io.destIn <> io.destIn(i)
    writers(i).io.schedOut <> schedMux.io.schedIn(i)
    if (i == c.WriteClients-1) {
      writers(c.WritePortSeq(i)).io.writeReqIn := buffer.io.writeReqOut
    } else {
      writers(c.WritePortSeq(i)).io.writeReqIn := writers(c.WritePortSeq(i+1)).io.writeReqOut
    }
    writePktInc(i) := writers(i).io.schedOut.valid
  }
  buffer.io.writeReqIn := writers(c.WritePortSeq(0)).io.writeReqOut

  schedMux.io.schedOut <> scheduler.io.schedIn

  for (i <- 0 until c.ReadClients) {
    readers(i).io.id := i.U
    buffer.io.readerInterface(i) <> readers(i).io.interface
    readers(i).io.portDataOut <> io.portDataOut(i)
    readers(i).io.schedIn <> scheduler.io.schedOut(i)
    readers(i).io.bufferReadResp := buffer.io.readRespOut
    readPktInc(i) := readers(i).io.schedIn.valid

    io.readerSchedResult(i).bits := scheduler.io.schedOut(i).bits
    io.readerSchedResult(i).valid := scheduler.io.schedOut(i).valid
  }

  val dropper = Module(new PacketDropper(c))
  dropper.io.interface <> buffer.io.dropInterface.get
  dropper.io.schedIn <> scheduler.io.dropOut
  io.pkt_count.incDropCount := dropper.io.schedIn.valid

  io.status.buffer <> buffer.io.status
  io.status.dropQueueStatus := scheduler.io.dropQueueStatus
  scheduler.io.dropQueueConfig := io.config.dropQueueConfig

  io.pkt_count.incTxCount := Cat(writePktInc.reverse)
  io.pkt_count.incRxCount := Cat(readPktInc.reverse)
  //io.memControl <> buffer.io.memControl
  for (i <- 0 until c.bufferMemoryCount) buffer.io.memControl(i) <> io.memControl(i)
  for (i <- 0 until c.schedMemoryCount) scheduler.io.memControl(i) <> io.memControl(i+c.bufferMemoryCount)
}

// Basic "scheduler" takes requests from source and sends them to their requested destination
class DirectScheduler(c : BufferConfig) extends Module {
  val io = IO(new Bundle {
    val schedIn = Vec(c.WriteClients, Flipped(new CreditIO(new SchedulerReq(c))))
    val schedOut = Vec(c.ReadClients, new CreditIO(new SchedulerReq(c)))
    val dropOut = if (c.HasDropPort) Some(new CreditIO(new SchedulerReq(c))) else None
  })
  val credRx = for (i <- 0 until c.WriteClients) yield Module(new DCCreditReceiver(new SchedulerReq(c), c.credit))
  val credTx = for (i <- 0 until c.ReadClients) yield Module(new DCCreditSender(new SchedulerReq(c), c.readerSchedCredit))
  var xbar = Module(new DcMcCrossbar(new SchedulerReq(c), inputs=c.WriteClients, outputs=c.ReadClients))
  val dropMux = if (c.HasDropPort) Some(Module(new DCArbiter(new SchedulerReq(c), c.WriteClients, false))) else None

  for (i <- 0 until c.WriteClients) {
    io.schedIn(i) <> credRx(i).io.enq
    credRx(i).io.deq <> xbar.io.c(i)
    xbar.io.sel(i) := credRx(i).io.deq.bits.dest

    // If we have a drop port, when the destination bits are all zero, do not send to the crossbar, but instead send into
    // the drop port.  Take away the valid/ready for the crossbar when we do this based on the dest bits.
    if (c.HasDropPort) {
      dropMux.get.io.c(i).bits := credRx(i).io.deq.bits
      when (credRx(i).io.deq.bits.dest === 0.U) {
        xbar.io.c(i).valid := 0.B
        dropMux.get.io.c(i).valid := credRx(i).io.deq.valid
        credRx(i).io.deq.ready := dropMux.get.io.c(i).ready
      }.otherwise {
        dropMux.get.io.c(i).valid := 0.B
      }
    }
  }

  if (c.HasDropPort) {
    val dropSender = Module(new DCCreditSender(new SchedulerReq(c), c.credit))
    dropSender.io.enq <> dropMux.get.io.p
    io.dropOut.get <> dropSender.io.deq
  }

  for (i <- 0 until c.ReadClients) {
    io.schedOut(i) <> credTx(i).io.deq
    credTx(i).io.enq <> xbar.io.p(i)
  }
}
