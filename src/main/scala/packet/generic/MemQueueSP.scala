package packet.generic

import chisel3._
import chisel3.util._

class MemQueueSP[D <: Data](data: D, depth : Int, gen : Memgen1RW) extends Module {
  val io = IO(new Bundle {
    val enq = Flipped(new DecoupledIO(data.cloneType))
    val deq = new DecoupledIO(data.cloneType)
    val usage = Output(UInt(log2Ceil(depth+1).W))
  })
  override def desiredName: String = "MemQueueSP_" + data.toString + "_D" + depth.toString

  val mem = Module(gen.apply(data, depth, 1))
  val asz = log2Ceil(depth)
  val wrptr = RegInit(init=0.U(asz.W))
  val rdptr = RegInit(init=0.U(asz.W))
  val nxt_rdptr = WireDefault(rdptr)
  val nxt_wrptr = WireDefault(wrptr)
  val wrptr_p1 = Wire(UInt(asz.W))
  val rdptr_p1 = Wire(UInt(asz.W))
  val full = RegInit(0.B)
  val wr_addr = wrptr(asz-1,0)
  val rd_addr = nxt_rdptr(asz-1,0)
  val wr_en = io.enq.valid & !full
  val nxt_valid = wrptr =/= nxt_rdptr
  val deq_valid = RegNext(next=nxt_valid, init=0.B)
  val rd_en = nxt_valid & !(deq_valid & !io.deq.ready)

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
      io.usage := wrptr - rdptr
    }.otherwise {
      io.usage := wrptr +& depth.U - rdptr
    }
  }.otherwise {
    io.usage := depth.U
    full := !io.deq.fire
  }

  io.enq.ready := !full & !rd_en

  when (io.enq.valid & !full) {
    nxt_wrptr := wrptr_p1
  }

  when (io.deq.fire) {
    nxt_rdptr := rdptr_p1
  }
  rdptr := nxt_rdptr
  wrptr := nxt_wrptr

  mem.io.readEnable := rd_en
  mem.io.addr := Mux(rd_en, rd_addr, wr_addr)
  mem.io.writeData := io.enq.bits
  mem.io.writeEnable := wr_en

  io.deq.valid := deq_valid
  io.deq.bits := mem.io.readData
}
