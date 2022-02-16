package packet.packetbuf

import chisel.lib.dclib.{DCArbiter, DCCreditReceiver, DCCreditSender, DCDemux}
import chisel3._

class FlatPacketBufferComplex(c : BufferConfig) extends Module {
  val io = IO(new Bundle {

  })
  val buffer = Module(new FlatPacketBuffer(c))
  val readers = for (i <- 0 until c.ReadClients) yield Module(new PacketReader(c))
  val writers = for (i <- 0 until c.WriteClients) yield Module(new PacketWriter(c))
  val reqMux = Module(new DCArbiter(new LinkListReadReq(c), c.WriteClients, false))
  val respDemux = Module(new DCDemux(new LinkListReadResp(c), c.WriteClients))

  for (i <- 0 until c.WriteClients) {
    // Create DC credit senders and receivers to convert ready/valid interface to credit
    val freeReqReceiver = Module(new DCCreditReceiver(new LinkListReadReq(c), c.credit))
    val freeRespSender = Module(new DCCreditSender(new LinkListReadResp(c), c.credit))

    if (i == c.WriteClients-1) {
      writers(i).io.interface.writeReqIn := buffer.io.buf.writeReqOut
    } else {
      writers(i).io.interface.writeReqIn := writers(i + 1).io.interface.writeReqOut
    }
    buffer.io.free.freeRequestIn(i) <> freeReqReceiver.io.deq
    writers(i).io.interface.freeListReq <> freeReqReceiver.io.enq
    buffer.io.free.freeRequestOut(i) <> freeRespSender.io.enq
    writers(i).io.interface.freeListPage <> freeRespSender.io.deq
  }
}
