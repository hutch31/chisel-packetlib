package packet.packetbuf

import chisel.lib.dclib._
import chisel3._
import chisel3.util.ImplicitConversions.intToUInt
import chisel3.util._

class LinkList(c : BufferConfig) extends Module {
  val io = IO(new Bundle {
    val writeReq = Vec(c.WriteClients, Flipped(Decoupled(new LinkListWriteReq(c))))
    val readReq = Vec(c.ReadClients, Flipped(Decoupled(new LinkListReadReq(c))))
    val readResp = Vec(c.ReadClients, Decoupled(new LinkListReadResp(c)))
  })
  val writeXbar = Module(new DCCrossbar(new LinkListWriteReq(c), inputs = c.WriteClients, outputs = c.NumPools))
  val readReqXbar = Module(new DCCrossbar(new LinkListReadReq(c), inputs = c.ReadClients, outputs = c.NumPools))
  val readRespXbar = Module(new DCCrossbar(new LinkListReadResp(c), inputs = c.NumPools, outputs = c.ReadClients))
  val lpool = for (i <- 0 until c.NumPools) yield Module(new LinkListPool(c))

  io.writeReq <> writeXbar.io.c
  for (i <- 0 until c.WriteClients) {
    writeXbar.io.sel(i) := io.writeReq(i).bits.addr.pool
  }
  io.readReq <> readReqXbar.io.c
  for (i <- 0 until c.ReadClients) {
    readReqXbar.io.sel(i) := io.readReq(i).bits.addr.pool
  }
  readRespXbar.io.p <> io.readResp
  for (i <- 0 until c.NumPools) {
    writeXbar.io.p(i) <> lpool(i).io.writeReq
    readReqXbar.io.p(i) <> lpool(i).io.readReq
    readRespXbar.io.sel(i) := lpool(i).io.readResp.bits.requestor
    readRespXbar.io.c(i) <> lpool(i).io.readResp
  }
}

class LinkListPool(c : BufferConfig) extends Module {
  val io = IO(new Bundle {
    val writeReq = Flipped(Decoupled(new LinkListWriteReq(c)))
    val readReq = Flipped(Decoupled(new LinkListReadReq(c)))
    val readResp = Decoupled(new LinkListReadResp(c))
  })
  val mem = SyncReadMem(c.PagePerPool, new PageLink(c))
  val s_init :: s_ready :: Nil = Enum(2)
  val state = RegInit(init=s_init)
  val initCount = RegInit(init=0.U(log2Ceil(c.PagePerPool).W))
  val outq = Module(new Queue(new LinkListReadResp(c), 2).suggestName("LinkListOutputQ"))
  val readValid = RegNext(init=false.B, next=io.readReq.valid)

  io.writeReq.ready := false.B
  io.readReq.ready := outq.io.count < 2.U

  switch (state) {
    is (s_init) {
      mem.write(initCount, 0.asTypeOf(new PageLink(c)))
      initCount := initCount + 1.U
      when (initCount === (c.PagePerPool-1).U) {
        state := s_ready
      }
    }

    is (s_ready) {
      io.writeReq.ready := true.B
      when (io.writeReq.valid) {
        mem.write(io.writeReq.bits.addr.pageNum, io.writeReq.bits.data)
      }
    }
  }

  outq.io.enq.bits := mem.read(io.readReq.bits.addr.pageNum, io.readReq.valid)
  outq.io.enq.valid := readValid
  io.readResp <> outq.io.deq
}
