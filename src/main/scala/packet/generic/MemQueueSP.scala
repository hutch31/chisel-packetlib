package packet.generic

import chisel3._
import chisel3.util._

class MemQueueSP[D <: Data](data: D, depth : Int, gen : Memgen1RW, memCon : MemoryControl, readLatency : Int = 1) extends Module {
  val outqSize = readLatency+1

  val io = IO(new Bundle {
    val enq = Flipped(new DecoupledIO(data.cloneType))
    val deq = new DecoupledIO(data.cloneType)
    val usage = Output(UInt(log2Ceil(depth+outqSize+1).W))
    val memControl = memCon.factory
  })
  override def desiredName: String = "MemQueueSP_" + data.toString + "_D" + depth.toString

  val mem = Module(gen.apply(data, depth, readLatency))
  val asz = log2Ceil(depth)
  val wrptr = RegInit(init=0.U(asz.W))
  val rdptr = RegInit(init=0.U(asz.W))
  val nxt_rdptr = WireDefault(rdptr)
  val nxt_wrptr = WireDefault(wrptr)
  val wrptr_p1 = Wire(UInt(asz.W))
  val rdptr_p1 = Wire(UInt(asz.W))
  val full = RegInit(0.B)
  val wr_addr = wrptr(asz-1,0)
  val rd_addr = rdptr(asz-1,0)
  val nxt_valid = wrptr =/= rdptr
  val deq_valid = RegInit(VecInit(Seq.fill(readLatency)(0.B)))
  val outq = Module(new Queue(data.cloneType, outqSize))
  val outPipeSize = PopCount(Cat(deq_valid)) +& outq.io.count
  val outPipeFull = outPipeSize >= outqSize.U
  val rd_en = nxt_valid & !outPipeFull
  val wr_en = io.enq.valid & !full & !rd_en
  val intUsage = Wire(UInt(log2Ceil(depth+1).W))

  deq_valid(0) := rd_en
  for (i <- 1 until readLatency) {
    deq_valid(i) := deq_valid(i-1)
  }

  mem.attachMemory(io.memControl)

  def sat_add(ptr : UInt) : UInt = {
    val plus1 = Wire(UInt(ptr.getWidth.W))
    when (ptr === (depth-1).U) {
      plus1 := 0.U
    }.otherwise {
      plus1 := ptr + 1.U
    }
    plus1
  }

  wrptr_p1 := sat_add(wrptr)
  rdptr_p1 := sat_add(rdptr)

  when (!full) {
    full := (wrptr_p1 === rdptr) && (nxt_wrptr === nxt_rdptr)
    when (wrptr >= rdptr) {
      intUsage := wrptr - rdptr
    }.otherwise {
      intUsage := wrptr +& depth.U - rdptr
    }
  }.otherwise {
    intUsage := depth.U
    full := !rd_en
  }
  io.usage := intUsage +& outq.io.count

  io.enq.ready := !full & !rd_en

  when (wr_en) {
    nxt_wrptr := wrptr_p1
  }

  when (rd_en) {
    nxt_rdptr := rdptr_p1
  }
  rdptr := nxt_rdptr
  wrptr := nxt_wrptr

  mem.io.readEnable := rd_en
  mem.io.addr := Mux(rd_en, rd_addr, wr_addr)
  mem.io.writeData := io.enq.bits
  mem.io.writeEnable := wr_en

  outq.io.enq.bits := mem.io.readData
  outq.io.enq.valid := deq_valid(readLatency-1)
  io.deq <> outq.io.deq
}
