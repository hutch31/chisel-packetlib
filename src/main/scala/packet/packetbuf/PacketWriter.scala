package packet.packetbuf

import chisel.lib.dclib._
import packet.PacketData
import chisel3._
import chisel3.util.ImplicitConversions.intToUInt
import chisel3.util._


class PacketLineInfo(c : BufferConfig) extends Bundle {
  val data = Vec(c.WordSize, UInt(8.W))
  val page = new PageType(c)
  val line = UInt(log2Ceil(c.LinesPerPage).W)
  override def cloneType = new PacketLineInfo(c).asInstanceOf[this.type]
}

/**
 * The PacketWriter is responsible for receive data words from the port, requesting
 * memory pages, and sequencing the writes into the packet buffer.
 *
 * @param writeBuf number of data words the PacketWriter can hold while waiting for a slot on the write ring
 * @param c  Packet buffer configuration
 * @param id Unique ID for this packet writer, for write slot requests
 */
class PacketWriter(c: BufferConfig, id : Int, writeBuf : Int = 1) extends Module {
  val io = IO(new Bundle {
    val portDataIn = Flipped(Decoupled(new PacketData(c.WordSize)))
    val destIn = Flipped(ValidIO(new RoutingResult(c.ReadClients)))
    val interface = new PacketWriterInterface(c)
    val schedOut = new CreditIO(new SchedulerReq(c))
  })

  val dest = RegEnable(init=0.U, next=io.destIn.bits.getDest(), enable=io.destIn.valid)
  val linkWriteSend = Module(new DCCreditSender(io.interface.linkListWriteReq.bits, c.linkWriteCredit))
  val schedOutSend = Module(new DCCreditSender(io.schedOut.bits, c.schedWriteCredit))
  val bufferAllocator = Module(new BasicPacketBufferAllocator(c, id))

  // this queue holds the metadata about the data line to be written (buffer address and line address)
  val lineInfoHold = Module(new Queue(new PacketLineInfo(c), writeBuf))
  // count number of lines we have used in the current page, used for linking to the next page
  val pageCount = Reg(UInt(log2Ceil(c.LinesPerPage).W))
  // output data holding register, this forms part of the write ring
  val interfaceOutReg = Reg(io.interface.writeReqOut.bits.cloneType)
  val interfaceOutValid = RegInit(false.B)
  // small queue for holding data between accepting it from portDataIn and sending on the ring
  //val dataQ = Module(new Queue(new PacketData(c.WordSize), writeBuf))
  val s_idle :: s_page :: Nil = Enum(2)
  val state = RegInit(init=s_idle)
  val currentPage = Reg(new PageType(c))
  val schedOutHold = Reg(new SchedulerReq(c))
  val schedOutValid = RegInit(false.B)

  // connect incoming data to buffer, send out a slot request for each data line
  io.portDataIn.ready := false.B
  //dataQ.io.enq.valid := false.B
  //dataQ.io.enq.bits := io.portDataIn.bits

  // connect interface to packet buffer
  io.interface.writeSlotReq := io.portDataIn.ready && lineInfoHold.io.enq.valid
  io.interface.linkListWriteReq <> linkWriteSend.io.deq
  io.interface.freeListReq <> bufferAllocator.io.freeListReq
  io.interface.freeListPage <> bufferAllocator.io.freeListPage

  lineInfoHold.io.enq.valid := false.B
  lineInfoHold.io.enq.bits.page := currentPage
  lineInfoHold.io.enq.bits.line := pageCount
  lineInfoHold.io.enq.bits.data := io.portDataIn.bits.data

  linkWriteSend.io.enq.valid := false.B
  linkWriteSend.io.enq.bits.addr := currentPage
  linkWriteSend.io.enq.bits.data.nextPageValid := true.B
  linkWriteSend.io.enq.bits.data.nextPage := bufferAllocator.io.freePage.bits

  // send packet info to scheduler once last word of data is received
  schedOutSend.io.enq.bits := schedOutHold
  schedOutSend.io.enq.valid := schedOutValid
  io.schedOut <> schedOutSend.io.deq

  // all resources needed by the state machine are available
  val fsmResourceOk = io.portDataIn.valid && lineInfoHold.io.enq.ready && bufferAllocator.io.freePage.valid

  // state machine to put links into pages
  bufferAllocator.io.freePage.ready := false.B
  switch (state) {
    is (s_idle) {
      when (fsmResourceOk && io.portDataIn.bits.code.isSop()) {
        state := s_page
        //dataQ.io.enq.valid := true.B
        io.portDataIn.ready := true.B
        schedOutHold.length := c.WordSize.U
        pageCount := 1.U
        lineInfoHold.io.enq.valid := true.B
        lineInfoHold.io.enq.bits.line := 0.U
        lineInfoHold.io.enq.bits.page := bufferAllocator.io.freePage.bits
        bufferAllocator.io.freePage.ready := true.B
        currentPage := bufferAllocator.io.freePage.bits
        schedOutHold.startPage := bufferAllocator.io.freePage.bits
      }
    }

    is (s_page) {
      when (fsmResourceOk && !schedOutValid) {
        lineInfoHold.io.enq.valid := true.B
        //dataQ.io.enq.valid := true.B
        io.portDataIn.ready := true.B

        when (io.portDataIn.bits.code.isEop()) {
          schedOutHold.length := schedOutHold.length + io.portDataIn.bits.count + 1.U
          linkWriteSend.io.enq.bits.data.nextPageValid := false.B
          linkWriteSend.io.enq.valid := true.B
          state := s_idle
          schedOutValid := true.B
          schedOutHold.dest := dest
        }.otherwise {
          schedOutHold.length := schedOutHold.length + c.WordSize.U

          // when we are on the last word of the page, perform a link operation
          // write the link, then copy out the next page into the current page
          when (pageCount === (c.LinesPerPage-1).U) {
            linkWriteSend.io.enq.valid := true.B
            currentPage := bufferAllocator.io.freePage.bits
            bufferAllocator.io.freePage.ready := true.B
            pageCount := 0.U
          }.otherwise {
            pageCount := pageCount + 1.U
          }
        }
      }
    }
  }


  // Insert the data from portDataIn on to the ring, waiting for our slot number to come up
  //dataQ.io.deq.ready := false.B
  io.interface.writeReqOut.valid := interfaceOutValid
  io.interface.writeReqOut.bits := interfaceOutReg
  lineInfoHold.io.deq.ready := false.B

  when (lineInfoHold.io.deq.valid && !io.interface.writeReqIn.valid && io.interface.writeReqIn.bits.slot === id.U) {
    interfaceOutValid := true.B
    lineInfoHold.io.deq.ready := true.B
    //dataQ.io.deq.ready := true.B
    interfaceOutReg.slot := id.U
    interfaceOutReg.data := lineInfoHold.io.deq.bits.data
    interfaceOutReg.line := lineInfoHold.io.deq.bits.line
    interfaceOutReg.page := lineInfoHold.io.deq.bits.page
  }.otherwise {
    // if not for us, simply forward data around the ring
    interfaceOutValid := io.interface.writeReqIn.valid
    interfaceOutReg := io.interface.writeReqIn.bits
  }
}


class BasicPacketBufferAllocator(c : BufferConfig, id : Int) extends Module {
  val io = IO(new Bundle {
    val freeListReq = new CreditIO(new PageReq(c))
    val freeListPage = Flipped(new CreditIO(new PageResp(c)))
    val freePage = Decoupled(new PageType(c))
  })
  val freeReqSender = Module(new DCCreditSender(io.freeListReq.bits, c.freeListReqCredit))
  val freeRespRx = Module(new DCCreditReceiver(io.freeListPage.bits, c.freeListReqCredit))
  val freePageReqCount = RegInit(init=0.U(log2Ceil(c.freeListReqCredit+1).W))

  io.freeListReq <> freeReqSender.io.deq
  freeRespRx.io.enq <> io.freeListPage

  // connect freePage output to page portion of return
  io.freePage.valid := freeRespRx.io.deq.valid
  freeRespRx.io.deq.ready := io.freePage.ready
  io.freePage.bits := freeRespRx.io.deq.bits.page

  // keep number of outstanding requests below the amount of buffer credit available in the receiver
  freeReqSender.io.enq.valid := freePageReqCount < c.freeListReqCredit.U
  freeReqSender.io.enq.bits.requestor := id.U
  if (c.NumPools == 1) {
    freeReqSender.io.enq.bits.pool := 0.U // tie off
  } else {
    // when more than one pool is in use, simply rotate requests between pools to statistically share load
    val curPool = RegInit(init=(id % c.NumPools).U(log2Ceil(c.NumPools).W))
    freeReqSender.io.enq.bits.pool := curPool
    when (freeReqSender.io.enq.valid) {
      when (curPool === (c.NumPools-1).U) {
        curPool := 0.U
      }.otherwise {
        curPool := curPool + 1.U
      }
    }
  }
}
