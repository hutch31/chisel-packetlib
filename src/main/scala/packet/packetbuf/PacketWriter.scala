package packet.packetbuf

import chisel.lib.dclib._
import packet.PacketData
import chisel3._
import chisel3.util.ImplicitConversions.intToUInt
import chisel3.util._


class PacketLineInfo(val c : BufferConfig) extends Bundle {
  val data = Vec(c.WordSize, UInt(8.W))
  val page = new PageType(c)
  val line = UInt(log2Ceil(c.LinesPerPage).W)
  val lastWord = Bool() // track if this is the last word of a packet
}

/**
 * The PacketWriter is responsible for receive data words from the port, requesting
 * memory pages, and sequencing the writes into the packet buffer.
 *
 * @param writeBuf number of data words the PacketWriter can hold while waiting for a slot on the write ring
 * @param c  Packet buffer configuration
 * @param id Unique ID for this packet writer, for write slot requests
 */
class PacketWriter(val c: BufferConfig, val writeBuf : Int = 1) extends Module {
  val io = IO(new Bundle {
    val portDataIn = Flipped(Decoupled(new PacketData(c.WordSize)))
    val destIn = Flipped(Decoupled(new RoutingResult(c.ReadClients)))
    val interface = new PacketWriterInterface(c)
    val writeReqIn = Flipped(ValidIO(new BufferWriteReq(c)))
    val writeReqOut = ValidIO(new BufferWriteReq(c))
    val schedOut = new CreditIO(new SchedulerReq(c))
    val id = Input(UInt(log2Ceil(c.WriteClients).W))
  })

  val dest = Module(new DCHold(new RoutingResult(c.ReadClients)))
  val linkWriteSend = Module(new DCCreditSender(new LinkListWriteReq(c), c.linkWriteCredit))
  val schedOutSend = Module(new DCCreditSender(new SchedulerReq(c), c.schedWriteCredit))
  val bufferAllocator = Module(new BasicPacketBufferAllocator(c))
  bufferAllocator.io.id := io.id
  io.destIn <> dest.io.enq

  // this queue holds the metadata about the data line to be written (buffer address and line address)
  val lineInfoHold = Module(new Queue(new PacketLineInfo(c), writeBuf))
  // count number of lines we have used in the current page, used for linking to the next page
  val pageCount = Reg(UInt(log2Ceil(c.LinesPerPage).W))
  // output data holding register, this forms part of the write ring
  val interfaceOutReg = Reg(io.writeReqOut.bits.cloneType)
  val interfaceOutValid = RegInit(false.B)
  // small queue for holding data between accepting it from portDataIn and sending on the ring
  val s_idle :: s_page :: s_sched :: Nil = Enum(3)
  val state = RegInit(init=s_idle)
  val currentPage = Reg(new PageType(c))
  val schedOutHold = Reg(new SchedulerReq(c))
  val lastWordWritten = RegInit(init=0.B)

  // connect incoming data to buffer, send out a slot request for each data line
  io.portDataIn.ready := false.B

  // connect interface to packet buffer
  io.interface.writeSlotReq := io.portDataIn.ready && lineInfoHold.io.enq.valid
  io.interface.linkListWriteReq <> linkWriteSend.io.deq
  io.interface.freeListReq <> bufferAllocator.io.freeListReq
  io.interface.freeListPage <> bufferAllocator.io.freeListPage

  lineInfoHold.io.enq.valid := false.B
  lineInfoHold.io.enq.bits.page := currentPage
  lineInfoHold.io.enq.bits.line := pageCount
  lineInfoHold.io.enq.bits.data := io.portDataIn.bits.data
  lineInfoHold.io.enq.bits.lastWord := io.portDataIn.bits.code.isEop()

  linkWriteSend.io.enq.valid := false.B
  linkWriteSend.io.enq.bits.addr := currentPage
  linkWriteSend.io.enq.bits.data.nextPageValid := true.B
  linkWriteSend.io.enq.bits.data.nextPage := bufferAllocator.io.freePage.bits

  // send packet info to scheduler once last word of data is received
  schedOutSend.io.enq.bits := schedOutHold
  schedOutSend.io.enq.valid := false.B
  io.schedOut <> schedOutSend.io.deq

  // all resources needed by the state machine are available
  val pageAvailable = bufferAllocator.io.freePage.valid || ((state === s_page) && !(pageCount === (c.LinesPerPage-1).U))
  val fsmResourceOk = io.portDataIn.valid & lineInfoHold.io.enq.ready & dest.io.deq.valid & linkWriteSend.io.enq.ready
  val multicast = PopCount(dest.io.deq.bits.dest) =/= 1.U

  io.interface.refCountAdd.valid := false.B
  io.interface.refCountAdd.bits.amount := PopCount(dest.io.deq.bits.dest) - 1.U
  io.interface.refCountAdd.bits.page := currentPage

  // state machine to put links into pages
  bufferAllocator.io.freePage.ready := false.B
  dest.io.deq.ready := false.B
  switch (state) {
    is (s_idle) {
      when (fsmResourceOk && pageAvailable && io.portDataIn.bits.code.isSop()) {
        state := s_page
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
      when (fsmResourceOk && (pageAvailable || io.portDataIn.bits.code.isEop())) {
        lineInfoHold.io.enq.valid := true.B
        io.portDataIn.ready := true.B

        when (io.portDataIn.bits.code.isEop()) {
          schedOutHold.length := schedOutHold.length + io.portDataIn.bits.count + 1.U
          schedOutHold.dest := dest.io.deq.bits.dest
          linkWriteSend.io.enq.bits.data.nextPageValid := false.B
          linkWriteSend.io.enq.valid := true.B

          if (c.MaxReferenceCount > 1) {
            io.interface.refCountAdd.valid := multicast
          } else {
            io.interface.refCountAdd.valid := 0.B
          }
          // Go to scheduler wait state and block until all writes are dispatched
          state := s_sched
        }.otherwise {
          schedOutHold.length := schedOutHold.length + c.WordSize.U

          // when we are on the last word of the page, perform a link operation
          // write the link, then copy out the next page into the current page
          when (pageCount === (c.LinesPerPage-1).U) {
            linkWriteSend.io.enq.valid := true.B
            if (c.MaxReferenceCount > 1) {
              io.interface.refCountAdd.valid := multicast
            } else {
              io.interface.refCountAdd.valid := 0.B
            }
            currentPage := bufferAllocator.io.freePage.bits
            bufferAllocator.io.freePage.ready := true.B
            pageCount := 0.U
          }.otherwise {
            pageCount := pageCount + 1.U
          }
        }
      }
    }

    // wait until last word has been written to the packet buffer to dispatch the descriptor
    is (s_sched) {
      when (schedOutSend.io.enq.ready & lastWordWritten) {
        dest.io.deq.ready := true.B
        schedOutSend.io.enq.valid := true.B
        state := s_idle
        lastWordWritten := false.B
      }
    }
  }


  // Insert the data from portDataIn on to the ring, waiting for our slot number to come up
  io.writeReqOut.valid := interfaceOutValid
  io.writeReqOut.bits := interfaceOutReg
  lineInfoHold.io.deq.ready := false.B
  val insertDataOnBus = lineInfoHold.io.deq.valid && !io.writeReqIn.valid && io.writeReqIn.bits.slotValid && io.writeReqIn.bits.slot === io.id

  when (insertDataOnBus) {
    printf("Writer %d wr page=%d/%d line=%d data=%x\n", io.id, lineInfoHold.io.deq.bits.page.pool, lineInfoHold.io.deq.bits.page.pageNum, lineInfoHold.io.deq.bits.line, lineInfoHold.io.deq.bits.data.asUInt())
    interfaceOutValid := true.B
    lineInfoHold.io.deq.ready := true.B
    interfaceOutReg.slot := io.id;
    interfaceOutReg.data := lineInfoHold.io.deq.bits.data
    interfaceOutReg.line := lineInfoHold.io.deq.bits.line
    interfaceOutReg.page := lineInfoHold.io.deq.bits.page
    when (lineInfoHold.io.deq.bits.lastWord) {
      lastWordWritten := true.B
    }
  }.otherwise {
    // if not for us, simply forward data around the ring
    interfaceOutValid := io.writeReqIn.valid
    interfaceOutReg := io.writeReqIn.bits
  }
}


class BasicPacketBufferAllocator(c : BufferConfig) extends Module {
  val io = IO(new Bundle {
    val freeListReq = new CreditIO(new PageReq(c))
    val freeListPage = Flipped(new CreditIO(new PageResp(c)))
    val freePage = Decoupled(new PageType(c))
    val id = Input(UInt(log2Ceil(c.WriteClients).W))
  })
  val freeReqSender = Module(new DCCreditSender(new PageReq(c), c.freeListReqCredit))
  val freeRespRx = Module(new DCCreditReceiver(new PageResp(c), c.freeListReqCredit))
  val freePageReqCount = RegInit(init=0.U(log2Ceil(c.freeListReqCredit+1).W))

  io.freeListReq <> freeReqSender.io.deq
  freeRespRx.io.enq <> io.freeListPage

  // connect freePage output to page portion of return
  io.freePage.valid := freeRespRx.io.deq.valid
  freeRespRx.io.deq.ready := io.freePage.ready
  io.freePage.bits := freeRespRx.io.deq.bits.page

  // keep number of outstanding requests below the amount of buffer credit available in the receiver
  when (freeReqSender.io.enq.valid && !io.freePage.fire) {
    freePageReqCount := freePageReqCount + 1.U
  }.elsewhen (!freeReqSender.io.enq.valid && io.freePage.fire) {
    freePageReqCount := freePageReqCount - 1.U
  }

  freeReqSender.io.enq.valid := (freePageReqCount +& freeRespRx.io.fifoCount) < c.freeListReqCredit.U
  freeReqSender.io.enq.bits.requestor := io.id
  if (c.NumPools > 1) {
    // when more than one pool is in use, simply rotate requests between pools to statistically share load
    val curPool = RegInit(init=io.id)
    freeReqSender.io.enq.bits.pool := curPool
    when (freeReqSender.io.enq.valid) {
      when (curPool === (c.NumPools-1).U) {
        curPool := 0.U
      }.otherwise {
        curPool := curPool + 1.U
      }
    }
  } else {
    freeReqSender.io.enq.bits.pool := 0.U
  }
}
