package packet.packetbuf

import chisel3._
import chisel3.util._

class FreeList(c : BufferConfig) extends Module {
  val io = IO(new Bundle {
    val requestIn = Vec(c.WriteClients, Flipped(Decoupled(new PageReq(c))))
    val requestOut = Vec(c.WriteClients, Decoupled(new PageResp(c)))
  })
}

class FreeListPool(c : BufferConfig, poolNum : Int) extends Module {
  val pageBits = log2Ceil(c.PagePerPool)
  val io = IO(new Bundle {
    val requestIn = Flipped(Decoupled(new PageReq(c)))
    val requestOut = Decoupled(new PageResp(c))
    val returnIn = Flipped(Decoupled(new PageType(c)))
  })
  val s_init :: s_run :: Nil = Enum(2)
  val state = RegInit(init = s_init)
  val initCount = RegInit(init=0.U(pageBits.W))
  val freeList = Module(new Queue(UInt(pageBits.W), c.PagePerPool))

  freeList.io.enq.valid := false.B
  freeList.io.enq.bits := 0.U
  freeList.io.deq.ready := false.B
  io.returnIn.ready := false.B
  io.requestIn.ready := false.B
  io.requestOut.valid := false.B
  io.requestOut.bits.requestor := io.requestIn.bits.requestor
  io.requestOut.bits.page.pageNum := freeList.io.deq.bits
  io.requestOut.bits.page.pool := poolNum.U

  switch (state) {
    is (s_init) {
      freeList.io.enq.valid := true.B
      freeList.io.enq.bits := initCount
      initCount := initCount + 1.U
      when (initCount === (c.PagePerPool-1).U) {
        state := s_run
      }
    }

    is (s_run) {
      freeList.io.enq.valid := io.returnIn.valid
      io.returnIn.ready := freeList.io.enq.ready
      freeList.io.enq.bits := io.returnIn.bits.pageNum
    }
  }

  // Wait for a request to come in, then append the pool number and page number
  // to the request and send back out
  when (io.requestIn.valid && io.requestOut.ready && freeList.io.deq.valid) {
    io.requestIn.ready := true.B
    io.requestOut.valid := true.B
    freeList.io.deq.ready := true.B
  }
}
