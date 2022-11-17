package packet.packetbuf

import chisel.lib.dclib._
import chisel3._
import chisel3.util._
import packet.PacketData

class PacketDropper (c : BufferConfig) extends Module {
  val io = IO(new Bundle {
    val interface = new PacketDropInterface(c)
    val schedIn = Flipped(new CreditIO(new SchedulerReq(c)))
    val pageLinkError = Output(Bool())
  })
  val linkListTx = Module(new DCCreditSender(new LinkListReadReq(c), c.linkReadCredit))
  val linkListRx = Module(new DCCreditReceiver(new LinkListReadResp(c), c.linkReadCredit))
  val freeListTx = Module(new DCCreditSender(new PageType(c), c.linkReadCredit))
  val ws_idle :: ws_wait_link :: Nil = Enum(2)
  val walkState = RegInit(init=ws_idle)
  val currentPage = Reg(new PageType(c))
  val schedRx = Module(new DCCreditReceiver(new SchedulerReq(c), c.credit))

  // connect credit receivers and buffers to IO
  linkListTx.io.deq <> io.interface.linkListReadReq
  linkListRx.io.enq <> io.interface.linkListReadResp
  freeListTx.io.deq <> io.interface.freeListReturn
  schedRx.io.enq <> io.schedIn

  // default link list request is for our current page
  linkListTx.io.enq.valid := false.B
  linkListTx.io.enq.bits.addr := currentPage
  linkListTx.io.enq.bits.requestor := c.ReadClients.U
  schedRx.io.deq.ready := false.B

  freeListTx.io.enq.bits := currentPage
  freeListTx.io.enq.valid := 0.B
  linkListRx.io.deq.ready := 0.B
  io.pageLinkError := 0.B

  switch (walkState) {
    is (ws_idle) {
      when(schedRx.io.deq.valid & linkListTx.io.enq.ready) {
        schedRx.io.deq.ready := true.B
        linkListTx.io.enq.valid := true.B
        currentPage := schedRx.io.deq.bits.startPage
        linkListTx.io.enq.bits.addr := schedRx.io.deq.bits.startPage
        walkState := ws_wait_link
      }
    }

    is (ws_wait_link) {
      when (linkListRx.io.deq.valid & linkListTx.io.enq.ready & freeListTx.io.enq.ready) {
        // if the next page is valid, then we are not done walking the list
        // stick our current page in the queue and fetch the next page
        linkListRx.io.deq.ready := true.B
        freeListTx.io.enq.valid := true.B
        when (linkListRx.io.deq.bits.data.nextPageValid) {
          currentPage := linkListRx.io.deq.bits.data.nextPage
          linkListTx.io.enq.valid := true.B
          linkListTx.io.enq.bits.addr := linkListRx.io.deq.bits.data.nextPage
        }.otherwise {
          // no more pages, go back to idle
          walkState := ws_idle
        }
      }
    }
  }
}
