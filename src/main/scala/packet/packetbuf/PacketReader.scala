package packet.packetbuf

import chisel.lib.dclib._
import packet._
import chisel3._
import chisel3.util.ImplicitConversions.intToUInt
import chisel3.util._

class PageListEntry(val c : BufferConfig) extends Bundle {
  val page = new PageType(c)
  val lastPage = Bool()
}

class WordMetadata(val c : BufferConfig) extends Bundle {
  val code = new PacketCode()
  val count = UInt(log2Ceil(c.WordSize).W)
  val pageValid = Bool()
  val page = new PageType(c)
}

class PacketReader(val c : BufferConfig) extends Module {
  val io = IO(new Bundle {
    val id = Input(UInt(log2Ceil(c.ReadClients).W))
    val portDataOut = Decoupled(new PacketData(c.WordSize))
    val interface = new PacketReaderInterface(c)
    val bufferReadResp = Flipped(ValidIO(new BufferReadResp(c)))
    val schedIn = Flipped(new CreditIO(new SchedulerReq(c)))
    val pageLinkError = Output(Bool())
  })
  val linkListTx = Module(new DCCreditSender(new LinkListReadReq(c), c.linkReadCredit))
  val linkListRx = Module(new DCCreditReceiver(new LinkListReadResp(c), c.linkReadCredit))
  val freeListTx = Module(new DCCreditSender(new PageType(c), c.linkReadCredit))
  val bufferReadTx = Module(new DCCreditSender(new BufferReadReq(c), c.linkReadCredit))
  val schedRx = Module(new DCCreditReceiver(new SchedulerReq(c), c.readerSchedCredit))
  val txq = Module(new Queue(new PacketData(c.WordSize), c.ReadWordBuffer).suggestName("ReaderTransmitQueue"))
  val metaQueue = Module(new Queue(new WordMetadata(c), c.ReadWordBuffer).suggestName("ReaderMetaQueue"))
  val txRequestCount = RegInit(init=0.U(log2Ceil(c.ReadWordBuffer+1).W))
  val ws_idle :: ws_wait_link :: Nil = Enum(2)
  val walkState = RegInit(init=ws_idle)
  val pageCount = RegInit(init=0.U(log2Ceil(c.LinesPerPage).W))
  val bytesRemaining = Reg(UInt(log2Ceil(c.MTU).W))
  val currentPage = Reg(new PageType(c))
  val pageList = Module(new Queue(new PageListEntry(c), 2).suggestName("ReaderPageListQueue"))
  val length = Module(new Queue(UInt(log2Ceil(c.MTU).W), 1).suggestName("ReaderLengthQueue"))
  val fs_idle :: fs_fetch :: Nil = Enum(2)
  val fetchState = RegInit(init=fs_idle)

  // connect credit receivers and buffers to IO
  linkListTx.io.deq <> io.interface.linkListReadReq
  linkListRx.io.enq <> io.interface.linkListReadResp
  freeListTx.io.deq <> io.interface.freeListReturn
  bufferReadTx.io.deq <> io.interface.bufferReadReq
  schedRx.io.enq <> io.schedIn

  // default link list request is for our current page
  linkListTx.io.enq.valid := false.B
  linkListTx.io.enq.bits.addr := currentPage
  linkListTx.io.enq.bits.requestor := io.id

  // length always comes from the scheduler interface
  linkListRx.io.deq.ready := false.B
  schedRx.io.deq.ready := false.B
  length.io.enq.valid := false.B
  length.io.enq.bits := schedRx.io.deq.bits.length

  pageList.io.enq.valid := false.B
  pageList.io.enq.bits.page := currentPage
  pageList.io.enq.bits.lastPage := false.B

  switch (walkState) {
    is (ws_idle) {
      when(schedRx.io.deq.valid & length.io.enq.ready & linkListTx.io.enq.ready) {
        length.io.enq.valid := true.B
        linkListTx.io.enq.valid := true.B
        currentPage := schedRx.io.deq.bits.startPage
        linkListTx.io.enq.bits.addr := schedRx.io.deq.bits.startPage
        walkState := ws_wait_link
      }
    }

    is (ws_wait_link) {
      when (linkListRx.io.deq.valid & linkListTx.io.enq.ready & pageList.io.enq.ready) {
        // if the next page is valid, then we are not done walking the list
        // stick our current page in the queue and fetch the next page
        pageList.io.enq.valid := true.B
        linkListRx.io.deq.ready := true.B
        when (linkListRx.io.deq.bits.data.nextPageValid) {
          currentPage := linkListRx.io.deq.bits.data.nextPage
          linkListTx.io.enq.valid := true.B
          pageList.io.enq.bits.page := currentPage
          linkListTx.io.enq.bits.addr := linkListRx.io.deq.bits.data.nextPage
        }.otherwise {
          // no more pages, go back to idle
          pageList.io.enq.bits.lastPage := true.B
          walkState := ws_idle
          // delay schedRx ready until we return to idle state, to avoid false low indication on output queue
          schedRx.io.deq.ready := true.B
        }
      }
    }
  }

  pageList.io.deq.ready := false.B
  length.io.deq.ready := false.B
  metaQueue.io.enq.valid := false.B
  metaQueue.io.enq.bits.code.code := packet.packetBody
  metaQueue.io.enq.bits.count := 0.U
  metaQueue.io.enq.bits.pageValid := false.B

  // buffer read requests are always from our ID, for the page at the head of the pageList
  bufferReadTx.io.enq.valid := false.B
  bufferReadTx.io.enq.bits.line := pageCount
  bufferReadTx.io.enq.bits.requestor := io.id
  bufferReadTx.io.enq.bits.page := pageList.io.deq.bits.page

  io.pageLinkError := false.B

  //freeListTx.io.enq.valid := false.B
  //freeListTx.io.enq.bits := pageList.io.deq.bits.page
  freeListTx.io.enq.bits := metaQueue.io.deq.bits.page
  metaQueue.io.enq.bits.page := pageList.io.deq.bits.page

  val txResourceUsed = txRequestCount +& txq.io.count
  val fsIdleReady = (txResourceUsed < c.ReadWordBuffer.U) & metaQueue.io.enq.ready & pageList.io.deq.valid & length.io.deq.valid & bufferReadTx.io.enq.ready
  val fsFetchReady = (txResourceUsed < c.ReadWordBuffer.U) & metaQueue.io.enq.ready & pageList.io.deq.valid & bufferReadTx.io.enq.ready & freeListTx.io.enq.ready

  // Track number of read requests in flight
  when (bufferReadTx.io.enq.fire && !txq.io.enq.fire) {
    txRequestCount := txRequestCount + 1.U
  }.elsewhen (!bufferReadTx.io.enq.fire && txq.io.enq.fire) {
    txRequestCount := txRequestCount - 1.U
  }

  switch (fetchState) {
    is (fs_idle) {
      when (fsIdleReady) {
        fetchState := fs_fetch
        pageCount := 1.U
        bytesRemaining := length.io.deq.bits - c.WordSize
        metaQueue.io.enq.valid := true.B
        metaQueue.io.enq.bits.code.code := packet.packetSop
        length.io.deq.ready := true.B
        bufferReadTx.io.enq.valid := true.B
        printf("Reader %d rreq page=%d/%d line=%d\n", io.id, bufferReadTx.io.enq.bits.page.pool, bufferReadTx.io.enq.bits.page.pageNum, bufferReadTx.io.enq.bits.line)
      }
    }

    is (fs_fetch) {
      // keep track of how many requests are outstanding so we don't overflow the buffer
      // when we have space, send a new request
      // once we have requested all the words in a page, pop the current page off the pageList and return
      // the now-empty page to the free list
      when (fsFetchReady) {
        bufferReadTx.io.enq.valid := true.B
        metaQueue.io.enq.valid := true.B
        when (bytesRemaining <= c.WordSize) {
          metaQueue.io.enq.bits.code.code := packet.packetGoodEop
          metaQueue.io.enq.bits.count := bytesRemaining - 1.U
          fetchState := fs_idle
          pageList.io.deq.ready := true.B
          metaQueue.io.enq.bits.pageValid := true.B
          //freeListTx.io.enq.valid := true.B
          // Error if we are on the last data word, but page link indicates that the list is not finished
          io.pageLinkError := !pageList.io.deq.bits.lastPage
          pageCount := 0.U
        }.otherwise {
          bytesRemaining := bytesRemaining - c.WordSize
          when (pageCount === (c.LinesPerPage-1).U) {
            pageCount := 0.U
            pageList.io.deq.ready := true.B
            //freeListTx.io.enq.valid := true.B
            metaQueue.io.enq.bits.pageValid := true.B
            io.pageLinkError := pageList.io.deq.bits.lastPage
          }.otherwise {
            pageCount := pageCount + 1.U
          }
        }
        printf("Reader %d rreq page=%d/%d line=%d\n", io.id, bufferReadTx.io.enq.bits.page.pool, bufferReadTx.io.enq.bits.page.pageNum, bufferReadTx.io.enq.bits.line)
      }
    }
  }

  // metadata for the output queue is prepared and ready once data arrives from the packet buffer.
  // Once data arrives, join the data request with the prepared metadata and place it in the output queue
  txq.io.enq.valid := io.bufferReadResp.valid & io.bufferReadResp.bits.req.requestor === io.id
  metaQueue.io.deq.ready := txq.io.enq.valid
  freeListTx.io.enq.valid := metaQueue.io.deq.bits.pageValid & metaQueue.io.deq.fire
  txq.io.enq.bits.code.code := metaQueue.io.deq.bits.code.code
  txq.io.enq.bits.count := metaQueue.io.deq.bits.count
  txq.io.enq.bits.data := io.bufferReadResp.bits.data

  io.portDataOut <> txq.io.deq
}
