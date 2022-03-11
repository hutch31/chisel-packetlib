package packet.packetbuf

import chisel.lib.dclib._
import chisel3._
import chisel3.util.ImplicitConversions.intToUInt
import chisel3.util._

class FreeListIO(c : BufferConfig) extends Bundle {
  val freeRequestIn = Vec(c.WriteClients, Flipped(Decoupled(new PageReq(c))))
  val freeRequestOut = Vec(c.WriteClients, Decoupled(new PageResp(c)))
  val freeReturnIn = Vec(c.ReadClients, Flipped(Decoupled(new PageType(c))))
  val pagesPerPort = Output(Vec(c.WriteClients, UInt(log2Ceil(c.totalPages).W)))
  override def cloneType =
    new FreeListIO(c).asInstanceOf[this.type]
}

class FreeList(c : BufferConfig) extends Module {
  val io = IO(new FreeListIO(c))
  require (c.WriteClients >= 2)
  require (c.ReadClients >= 2)

  val sourcePage = Reg(Vec(c.totalPages, UInt(log2Ceil(c.ReadClients).W)))
  val pagesPerPort = RegInit(init=0.asTypeOf(Vec(c.WriteClients, UInt(log2Ceil(c.totalPages).W))))
  io.pagesPerPort := pagesPerPort

  val listPools = for (i <- 0 until c.NumPools) yield Module(new FreeListPool(c, i))
  if (c.NumPools > 1) {
    val reqInXbar = Module(new DCCrossbar(new PageReq(c), inputs = c.WriteClients, outputs = c.NumPools))
    val reqOutXbar = Module(new DCCrossbar(new PageResp(c), inputs = c.NumPools, outputs = c.WriteClients))
    val retXbar = Module(new DCCrossbar(new PageType(c), inputs = c.ReadClients, outputs = c.NumPools))
    val retXbarHold = for (i <- 0 until c.NumPools) yield DCOutput(retXbar.io.p(i))

    io.freeRequestIn <> reqInXbar.io.c
    for (wc <- 0 until c.WriteClients) {
      reqInXbar.io.sel(wc) := io.freeRequestIn(wc).bits.pool
    }
    for (p <- 0 until c.NumPools) {
      val stallThisPort = Wire(Vec(c.NumPools, Bool()))
      val stallPort = Cat(stallThisPort).orR().suggestName("stallPort" + p.toString)

      // determine if we need to stall the return to avoid a same-cycle update
      for (src <- 0 until c.NumPools) {
        if (src == p) {
          stallThisPort(src) := false.B
        } else {
          stallThisPort(src) := retXbarHold(p).valid && reqOutXbar.io.c(p).valid && listPools(p).io.requestOut.bits.requestor === sourcePage(retXbarHold(src).bits.asAddr())
        }
      }
      listPools(p).io.requestIn <> reqInXbar.io.p(p)
      reqOutXbar.io.c(p) <> listPools(p).io.requestOut
      reqOutXbar.io.sel(p) := listPools(p).io.requestOut.bits.requestor
      listPools(p).io.returnIn <> retXbarHold(p)

      // take away ready/valid signals if returning a page for a port being incremented this cycle
      when (stallPort) {
        listPools(p).io.returnIn.valid := false.B
        retXbarHold(p).ready := false.B
      }

      when (reqOutXbar.io.c(p).fire()) {
        sourcePage(reqOutXbar.io.c(p).bits.page.asAddr()) := listPools(p).io.requestOut.bits.requestor
        pagesPerPort(listPools(p).io.requestOut.bits.requestor) := pagesPerPort(listPools(p).io.requestOut.bits.requestor) + 1.U
      }

      when (retXbarHold(p).fire()) {
        pagesPerPort(sourcePage(retXbarHold(p).bits.asAddr())) := pagesPerPort(sourcePage(retXbarHold(p).bits.asAddr())) - 1.U
      }
    }
    io.freeRequestOut <> reqOutXbar.io.p

    for (rc <- 0 until c.ReadClients) {
      retXbar.io.sel(rc) := io.freeReturnIn(rc).bits.pool
    }
    retXbar.io.c <> io.freeReturnIn
  } else {
    // degenerate case for single pool, removes need for input/output crossbars
    val reqInMux = Module(new DCArbiter(new PageReq(c), c.WriteClients, false))
    val reqOutDemux = Module(new DCDemux(new PageResp(c), c.WriteClients))
    val retMux = Module(new DCArbiter(new PageType(c), c.ReadClients, false))

    io.freeRequestIn <> reqInMux.io.c
    reqInMux.io.p <> listPools(0).io.requestIn
    listPools(0).io.requestOut <> reqOutDemux.io.c
    reqOutDemux.io.sel := listPools(0).io.requestOut.bits.requestor

    val samePortUpdate = reqOutDemux.io.c.fire() && retMux.io.p.fire() && listPools(0).io.requestOut.bits.requestor === sourcePage(retMux.io.p.bits.asAddr())
    when (reqOutDemux.io.c.fire()) {
      sourcePage(listPools(0).io.requestOut.bits.page.asAddr()) := listPools(0).io.requestOut.bits.requestor
      when (!samePortUpdate) {
        pagesPerPort(listPools(0).io.requestOut.bits.requestor) := pagesPerPort(listPools(0).io.requestOut.bits.requestor) + 1.U
      }
    }
    io.freeRequestOut <> reqOutDemux.io.p

    io.freeReturnIn <> retMux.io.c
    listPools(0).io.returnIn <> retMux.io.p
    when (retMux.io.p.fire() && !samePortUpdate) {
      pagesPerPort(sourcePage(retMux.io.p.bits.asAddr())) := pagesPerPort(sourcePage(retMux.io.p.bits.asAddr())) - 1.U
    }
  }
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
  val freeList = Module(new Queue(UInt(pageBits.W), c.PagePerPool).suggestName("FreeListQueue"))
  val requestIn = DCInput(io.requestIn)
  val returnIn = DCInput(io.returnIn)
  val requestOut = Wire(Decoupled(new PageResp(c)))

  freeList.io.enq.valid := false.B
  freeList.io.enq.bits := 0.U
  freeList.io.deq.ready := false.B
  returnIn.ready := false.B
  requestIn.ready := false.B
  requestOut.valid := false.B
  requestOut.bits.requestor := requestIn.bits.requestor
  requestOut.bits.page.pageNum := freeList.io.deq.bits
  requestOut.bits.page.pool := poolNum.U

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
      freeList.io.enq.valid := returnIn.valid
      returnIn.ready := freeList.io.enq.ready
      freeList.io.enq.bits := returnIn.bits.pageNum
    }
  }

  // Wait for a request to come in, then append the pool number and page number
  // to the request and send back out
  when (requestIn.valid && requestOut.ready && freeList.io.deq.valid) {
    requestIn.ready := true.B
    requestOut.valid := true.B
    freeList.io.deq.ready := true.B
  }
  io.requestOut <> DCOutput(requestOut)
}
