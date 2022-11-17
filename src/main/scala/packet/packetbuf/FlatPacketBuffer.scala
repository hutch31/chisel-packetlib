package packet.packetbuf

import chisel.lib.dclib.{CreditIO, DCArbiter, DCCreditReceiver, DCCreditSender}
import chisel3._
import chisel3.util.ValidIO

class FlatPacketBuffer(c : BufferConfig) extends Module {
  val io = IO(new Bundle {
    val writerInterface = Vec(c.WriteClients, Flipped(new PacketWriterInterface(c)))
    val readerInterface = Vec(c.ReadClients, Flipped(new PacketReaderInterface(c)))
    val writeReqIn = Flipped(ValidIO(new BufferWriteReq(c)))
    val writeReqOut = ValidIO(new BufferWriteReq(c))
    val readRespOut = ValidIO(new BufferReadResp(c))
    val status = new BufferStatus(c)
    val dropInterface = if (c.HasDropPort) Some(Flipped(new PacketDropInterface(c))) else None
    val memControl = Vec(c.totalMemoryCount, c.MemControl.factory)
  })
  val buffer = Module(new BufferMemory(c))
  val linkList = Module(new LinkList(c))
  val freeList = Module(new FreeList(c))
  val readReqArbiter = Module(new DCArbiter(new BufferReadReq(c), c.ReadClients, false))

  for (i <- 0 until c.WriteClients) {
    val freeReqReceiver = Module(new DCCreditReceiver(new PageReq(c), c.credit))
    val freeRespSender = Module(new DCCreditSender(new PageResp(c), c.credit))
    val linkWriteReceiver = Module(new DCCreditReceiver(new LinkListWriteReq(c), c.credit))

    io.writerInterface(i).freeListReq <> freeReqReceiver.io.enq
    freeReqReceiver.io.deq <> freeList.io.freeRequestIn(i)
    io.writerInterface(i).freeListPage <> freeRespSender.io.deq
    freeRespSender.io.enq <> freeList.io.freeRequestOut(i)
    io.writerInterface(i).linkListWriteReq <> linkWriteReceiver.io.enq
    linkWriteReceiver.io.deq <> linkList.io.writeReq(i)
    buffer.io.wrSlotReqIn(i) := io.writerInterface(i).writeSlotReq
    freeList.io.refCountAdd(i) <> io.writerInterface(i).refCountAdd
  }

  for (i <- 0 until c.ReadClients) {
    val readReqReceiver = Module(new DCCreditReceiver(new BufferReadReq(c), c.credit))
    val returnReceiver = Module(new DCCreditReceiver(new PageType(c), c.credit))
    val linkReadReqReceiver = Module(new DCCreditReceiver(new LinkListReadReq(c), c.credit))
    val linkReadRespSender = Module(new DCCreditSender(new LinkListReadResp(c), c.credit))

    returnReceiver.io.enq <> io.readerInterface(i).freeListReturn
    returnReceiver.io.deq <> freeList.io.freeReturnIn(i)
    io.readerInterface(i).bufferReadReq <> readReqReceiver.io.enq
    readReqReceiver.io.deq <> readReqArbiter.io.c(i)

    io.readerInterface(i).linkListReadReq <> linkReadReqReceiver.io.enq
    linkReadReqReceiver.io.deq <> linkList.io.readReq(i)
    linkList.io.readResp(i) <> linkReadRespSender.io.enq
    linkReadRespSender.io.deq <> io.readerInterface(i).linkListReadResp
  }

  if (c.HasDropPort) {
    val returnReceiver = Module(new DCCreditReceiver(new PageType(c), c.credit))
    val linkReadReqReceiver = Module(new DCCreditReceiver(new LinkListReadReq(c), c.credit))
    val linkReadRespSender = Module(new DCCreditSender(new LinkListReadResp(c), c.credit))

    io.dropInterface.get.linkListReadReq <> linkReadReqReceiver.io.enq
    linkReadReqReceiver.io.deq <> linkList.io.readReq(c.ReadClients)
    linkList.io.readResp(c.ReadClients) <> linkReadRespSender.io.enq
    linkReadRespSender.io.deq <> io.dropInterface.get.linkListReadResp
    returnReceiver.io.enq <> io.dropInterface.get.freeListReturn
    returnReceiver.io.deq <> freeList.io.freeReturnIn(c.ReadClients)
  }

  // connect arbiter to read request in, take read requests as they come
  val phase = Wire(Bool())
  if (c.PacketBuffer2Port) {
    phase := true.B
  } else {
    val odd = RegInit(init=true.B)
    odd := !odd
    phase := odd
  }
  buffer.io.readReqIn.valid := readReqArbiter.io.p.valid & phase
  readReqArbiter.io.p.ready := phase
  buffer.io.readReqIn.bits := readReqArbiter.io.p.bits

  // Connect data buses directly to packet buffer
  buffer.io.writeReqIn := io.writeReqIn
  io.writeReqOut := buffer.io.writeReqOut
  io.readRespOut := buffer.io.readRespOut

  // connect buffer status signals
  io.status.freePages := freeList.io.freePages
  io.status.pagesPerPort := freeList.io.pagesPerPort

  for (i <- 0 until c.NumPools) {
    buffer.io.memControl(i) <> io.memControl(i*3)
    freeList.io.memControl(i) <> io.memControl(i*3+1)
    linkList.io.memControl(i) <> io.memControl(i*3+2)
  }
}
