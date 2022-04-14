package packet.generic

import chisel3._
import chisel3.util._

class MemQueue[D <: Data](data: D, depth : Int, gen : Memgen1R1W) extends Module {
  val io = IO(new Bundle {
    val enq = Flipped(new DecoupledIO(data.cloneType))
    val deq = new DecoupledIO(data.cloneType)
  })
  override def desiredName: String = "MemQueue_" + data.toString + "_D" + depth.toString

  val mem = Module(gen.apply(data, depth, 1))
  val asz = log2Ceil(depth)
  val wrptr = RegInit(init=0.U((asz+1).W))
  val rdptr = RegInit(init=0.U((asz+1).W))
  val nxt_rdptr = WireDefault(rdptr)
  val wrptr_p1 = wrptr + 1.U
  val rdptr_p1 = rdptr + 1.U
  val full = (wrptr(asz-1,0) === rdptr(asz-1,0)) && (wrptr(asz) === !rdptr(asz))
  val wr_addr = wrptr(asz-1,0)
  val rd_addr = nxt_rdptr(asz-1,0)
  val wr_en = io.enq.valid & !full
  val nxt_valid = wrptr =/= nxt_rdptr
  val deq_valid = RegNext(next=nxt_valid, init=0.B)
  val rd_en = nxt_valid & !(deq_valid & !io.deq.ready)

  io.enq.ready := !full

  when (io.enq.valid & !full) {
    wrptr := wrptr_p1
  }

  when (io.deq.fire()) {
    nxt_rdptr := rdptr_p1
  }
  rdptr := nxt_rdptr

  mem.io.readEnable := rd_en
  mem.io.readAddr := rd_addr
  mem.io.writeData := io.enq.bits
  mem.io.writeEnable := wr_en
  mem.io.writeAddr := wr_addr

  io.deq.valid := deq_valid
  io.deq.bits := mem.io.readData
}
