package packet.packetbuf

import chisel.lib.dclib._
import chisel3._
import chisel3.util.ImplicitConversions.intToUInt
import chisel3.util._
import packet.generic.MemQueue

class FreeListIO(c : BufferConfig) extends Bundle {
  val freeRequestIn = Vec(c.WriteClients, Flipped(Decoupled(new PageReq(c))))
  val freeRequestOut = Vec(c.WriteClients, Decoupled(new PageResp(c)))
  val freeReturnIn = Vec(c.IntReadClients, Flipped(Decoupled(new PageType(c))))
  val refCountAdd = Vec(c.WriteClients, Flipped(Decoupled(new RefCountAdd(c))))
  val pagesPerPort = Output(Vec(c.WriteClients, UInt(log2Ceil(c.totalPages).W)))
  val freePages = Output(Vec(c.NumPools, UInt(log2Ceil(c.PagePerPool+1).W)))
  val memControl = Vec(c.NumPools, c.MemControl.factory)
}

class FreeList(val c : BufferConfig) extends Module {
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
    val retXbar = Module(new DCCrossbar(new PageType(c), inputs = c.IntReadClients, outputs = c.NumPools))
    val retXbarHold = for (i <- 0 until c.NumPools) yield Module(new DCOutput(new PageType(c)))

    if (c.MaxReferenceCount > 1)  {
      val refCount = for (i <- 0 until c.NumPools) yield Module(new FreeListRefCount(c)).suggestName("refCount_"+i.toString)
      val refcountXbar = Module(new DCCrossbar(new RefCountAdd(c), inputs = c.WriteClients, outputs = c.NumPools))

      refcountXbar.io.c <> io.refCountAdd
      for (wc <- 0 until c.WriteClients) {
        refcountXbar.io.sel(wc) := io.refCountAdd(wc).bits.page.pool
      }
      for (p <- 0 until c.NumPools) {
        refCount(p).io.refCountAdd <> refcountXbar.io.p(p)

        refCount(p).io.returnIn <> retXbar.io.p(p)
        retXbarHold(p).io.enq <> refCount(p).io.returnOut

        // monitor request out so we know when initial page is allocated
        refCount(p).io.requestOut.bits := reqOutXbar.io.c(p).bits
        refCount(p).io.requestOut.valid := reqOutXbar.io.c(p).fire
      }
    } else {
      for (i <- 0 until c.WriteClients)
        io.refCountAdd(i).ready := 0.B

      for (p <- 0 until c.NumPools) {
        retXbarHold(p).io.enq <> retXbar.io.p(p)
      }
    }

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
          stallThisPort(src) := retXbarHold(p).io.deq.valid && reqOutXbar.io.c(p).valid && listPools(p).io.requestOut.bits.requestor === sourcePage(retXbarHold(src).io.deq.bits.asAddr())
        }
      }
      listPools(p).io.requestIn <> reqInXbar.io.p(p)
      reqOutXbar.io.c(p) <> listPools(p).io.requestOut
      reqOutXbar.io.sel(p) := listPools(p).io.requestOut.bits.requestor
      listPools(p).io.returnIn <> retXbarHold(p).io.deq

      // take away ready/valid signals if returning a page for a port being incremented this cycle
      when (stallPort) {
        listPools(p).io.returnIn.valid := false.B
        retXbarHold(p).io.deq.ready := false.B
      }

      when (reqOutXbar.io.c(p).fire) {
        sourcePage(reqOutXbar.io.c(p).bits.page.asAddr()) := listPools(p).io.requestOut.bits.requestor
        pagesPerPort(listPools(p).io.requestOut.bits.requestor) := pagesPerPort(listPools(p).io.requestOut.bits.requestor) + 1.U
      }

      when (retXbarHold(p).io.deq.fire) {
        pagesPerPort(sourcePage(retXbarHold(p).io.deq.bits.asAddr())) := pagesPerPort(sourcePage(retXbarHold(p).io.deq.bits.asAddr())) - 1.U
      }
    }
    io.freeRequestOut <> reqOutXbar.io.p

    for (rc <- 0 until c.IntReadClients) {
      retXbar.io.sel(rc) := io.freeReturnIn(rc).bits.pool
    }
    retXbar.io.c <> io.freeReturnIn
  } else {
    // degenerate case for single pool, removes need for input/output crossbars
    val reqInMux = Module(new DCArbiter(new PageReq(c), c.WriteClients, false))
    val reqOutDemux = Module(new DCDemux(new PageResp(c), c.WriteClients))
    val retMux = Module(new DCArbiter(new PageType(c), c.IntReadClients, false))
    val retMuxOut = Wire(Decoupled(new PageType(c)))

    io.freeRequestIn <> reqInMux.io.c
    reqInMux.io.p <> listPools(0).io.requestIn
    listPools(0).io.requestOut <> reqOutDemux.io.c
    reqOutDemux.io.sel := listPools(0).io.requestOut.bits.requestor

    if (c.MaxReferenceCount > 1)  {
      val refCount = Module(new FreeListRefCount(c)).suggestName("refCount")
      val refcountDemux = Module(new DCArbiter(new RefCountAdd(c),  c.WriteClients, false)).suggestName("refcountDemux")

      refcountDemux.io.c <> io.refCountAdd
      refCount.io.refCountAdd <> refcountDemux.io.p

      refCount.io.returnIn <> retMux.io.p
      retMuxOut <> refCount.io.returnOut

      // monitor request out so we know when initial page is allocated
      refCount.io.requestOut.bits := reqOutDemux.io.c.bits
      refCount.io.requestOut.valid := reqOutDemux.io.c.fire
    } else {
      for (i <- 0 until c.WriteClients)
        io.refCountAdd(i).ready := 0.B

      retMuxOut <> retMux.io.p
    }

    val samePortUpdate = reqOutDemux.io.c.fire && retMuxOut.fire && listPools(0).io.requestOut.bits.requestor === sourcePage(retMuxOut.bits.asAddr())
    when (reqOutDemux.io.c.fire) {
      sourcePage(listPools(0).io.requestOut.bits.page.asAddr()) := listPools(0).io.requestOut.bits.requestor
      when (!samePortUpdate) {
        pagesPerPort(listPools(0).io.requestOut.bits.requestor) := pagesPerPort(listPools(0).io.requestOut.bits.requestor) + 1.U
      }
    }
    io.freeRequestOut <> reqOutDemux.io.p

    io.freeReturnIn <> retMux.io.c
    listPools(0).io.returnIn <> retMuxOut
    when (retMuxOut.fire && !samePortUpdate) {
      pagesPerPort(sourcePage(retMuxOut.bits.asAddr())) := pagesPerPort(sourcePage(retMuxOut.bits.asAddr())) - 1.U
    }
  }

  for (i <- 0 until c.NumPools) {
    io.freePages(i) := listPools(i).io.freePageCount
    io.memControl(i) <> listPools(i).io.memControl
  }

}

class FreeListPool(c : BufferConfig, poolNum : Int) extends Module {
  val pageBits = log2Ceil(c.PagePerPool)
  val io = IO(new Bundle {
    val requestIn = Flipped(Decoupled(new PageReq(c)))
    val requestOut = Decoupled(new PageResp(c))
    val returnIn = Flipped(Decoupled(new PageType(c)))
    val freePageCount = Output(UInt(log2Ceil(c.PagePerPool+1).W))
    val memControl = c.MemControl.factory
  })
  val s_init :: s_run :: Nil = Enum(2)
  val state = RegInit(init = s_init)
  val initCount = RegInit(init=0.U(pageBits.W))
  val freeList = Module(new MemQueue(UInt(pageBits.W), c.PagePerPool, c.mgen2p, io.memControl))
  val requestIn = DCInput(io.requestIn)
  val returnIn = Wire(Flipped(Decoupled(new PageType(c))))
  val requestOut = Wire(Decoupled(new PageResp(c)))

  returnIn <> DCInput(io.returnIn)
  freeList.io.memControl <> io.memControl

  freeList.io.enq.valid := false.B
  freeList.io.enq.bits := 0.U
  freeList.io.deq.ready := false.B
  returnIn.ready := false.B
  requestIn.ready := false.B
  requestOut.valid := false.B
  requestOut.bits.requestor := requestIn.bits.requestor
  requestOut.bits.page.pageNum := freeList.io.deq.bits
  requestOut.bits.page.pool := poolNum.U
  io.freePageCount := freeList.io.usage

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

class FreeListRefCount(c : BufferConfig) extends Module {
  val io = IO(new Bundle {
    val returnIn = Flipped(Decoupled(new PageType(c)))
    val returnOut = Decoupled(new PageType(c))
    val requestOut = Flipped(ValidIO(new PageResp(c)))
    val refCountAdd = Flipped(Decoupled(new RefCountAdd(c)))
  })
  val refCount = Reg(Vec(c.PagePerPool, UInt(log2Ceil(c.MaxReferenceCount).W)))
  val retIn = DCInput(io.returnIn)
  val retOutHold = Module(new DCOutput(new PageType(c)))
  retOutHold.io.deq <> io.returnOut

  when (io.requestOut.valid) {
    refCount(io.requestOut.bits.page.pageNum) := 1.U
  }

  io.refCountAdd.ready := true.B
  when (io.refCountAdd.valid) {
    refCount(io.refCountAdd.bits.page.pageNum) := refCount(io.refCountAdd.bits.page.pageNum) + io.refCountAdd.bits.amount
  }

  retIn.ready := false.B
  retOutHold.io.enq.valid := false.B
  retOutHold.io.enq.bits := retIn.bits

  // hazard detection to stall if simultaneous increment and decrement to same address
  val hazard = retIn.valid && io.refCountAdd.valid && retIn.bits.pageNum === io.refCountAdd.bits.page.pageNum
  when (retIn.valid && !hazard) {
    refCount(retIn.bits.pageNum) := refCount(retIn.bits.pageNum) - 1.U
    when (refCount(retIn.bits.pageNum) > 1.U) {
      retIn.ready := true.B
    }.otherwise {
      retOutHold.io.enq.valid := true.B
      retIn.ready := retOutHold.io.enq.ready
    }
  }
}
