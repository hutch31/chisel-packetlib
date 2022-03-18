package packet.packetbuf

import chisel.lib.dclib.{CreditIO, DCArbiter, DCCreditReceiver, DCCreditSender, DCCrossbar, DCDemux}
import chisel3._
import chisel3.util.{Decoupled, ValidIO}
import packet.PacketData

class FlatPacketBufferComplex(c : BufferConfig) extends Module {
  val io = IO(new Bundle {
    val portDataOut = Vec(c.ReadClients, Decoupled(new PacketData(c.WordSize)))
    val portDataIn = Vec(c.WriteClients, Flipped(Decoupled(new PacketData(c.WordSize))))
    val destIn = Vec(c.WriteClients, Flipped(ValidIO(new RoutingResult(c.ReadClients))))
    val status = new BufferStatus(c)
  })
  val readers = for (i <- 0 until c.ReadClients) yield Module(new PacketReader(c))
  val writers = for (i <- 0 until c.WriteClients) yield Module(new PacketWriter(c))
  val buffer = Module(new FlatPacketBuffer(c))
  val scheduler = Module(new DirectScheduler(c))

  for (i <- 0 until c.WriteClients) {
    writers(i).io.id := i.U
    buffer.io.writerInterface(i) <> writers(i).io.interface
    writers(i).io.portDataIn <> io.portDataIn(i)
    writers(i).io.destIn <> io.destIn(i)
    writers(i).io.schedOut <> scheduler.io.schedIn(i)
    if (i == c.WriteClients-1) {
      writers(i).io.writeReqIn := buffer.io.writeReqOut
    } else {
      writers(i).io.writeReqIn := writers(i+1).io.writeReqOut
    }
  }
  buffer.io.writeReqIn := writers(0).io.writeReqOut

  for (i <- 0 until c.ReadClients) {
    readers(i).io.id := i.U
    buffer.io.readerInterface(i) <> readers(i).io.interface
    readers(i).io.portDataOut <> io.portDataOut(i)
    readers(i).io.schedIn <> scheduler.io.schedOut(i)
    readers(i).io.bufferReadResp := buffer.io.readRespOut
  }

  io.status <> buffer.io.status
}

// Basic "scheduler" takes requests from source and sends them to their requested destination
class DirectScheduler(c : BufferConfig) extends Module {
  val io = IO(new Bundle {
    val schedIn = Vec(c.WriteClients, Flipped(new CreditIO(new SchedulerReq(c))))
    val schedOut = Vec(c.ReadClients, new CreditIO(new SchedulerReq(c)))
  })
  val credRx = for (i <- 0 until c.WriteClients) yield Module(new DCCreditReceiver(new SchedulerReq(c), c.credit))
  val credTx = for (i <- 0 until c.ReadClients) yield Module(new DCCreditSender(new SchedulerReq(c), c.credit))
  var xbar = Module(new DCCrossbar(new SchedulerReq(c), inputs=c.WriteClients, outputs=c.ReadClients))

  for (i <- 0 until c.WriteClients) {
    io.schedIn(i) <> credRx(i).io.enq
    credRx(i).io.deq <> xbar.io.c(i)
    xbar.io.sel(i) := credRx(i).io.deq.bits.dest
  }

  for (i <- 0 until c.ReadClients) {
    io.schedOut(i) <> credTx(i).io.deq
    credTx(i).io.enq <> xbar.io.p(i)
  }
}
