package packet.generic

import chisel3._
import chisel3.util._

class MemQueueSP[D <: Data](data: D, depth : Int, gen : Memgen1RW, memCon : MemoryControl, readLatency : Int = 1) extends Module {
  val pctl = Module(new MemQueuePtrCtl(depth, readLatency))
  val outqSize = pctl.outqSize

  val io = IO(new Bundle {
    val enq = Flipped(new DecoupledIO(data.cloneType))
    val deq = new DecoupledIO(data.cloneType)
    val usage = Output(UInt(log2Ceil(depth+outqSize+1).W))
    val memControl = memCon.factory
  })
  override def desiredName: String = "MemQueueSP_" + data.toString + "_D" + depth.toString

  val mem = Module(gen.apply(data, depth, readLatency))
  val asz = log2Ceil(depth)
  val outq = Module(new Queue(data.cloneType, outqSize))

  pctl.io.outputQueueCount := outq.io.count
  pctl.io.lowerBound := 0.U
  pctl.io.upperBound := (depth - 1).U
  pctl.io.readGate := true.B
  pctl.io.writeGate := !pctl.io.rd_en
  pctl.io.enqValid := io.enq.valid
  pctl.io.deqFire := io.deq.fire
  io.enq.ready := pctl.io.enqReady
  io.usage := pctl.io.usage
  pctl.io.init := 0.B

  mem.io.readEnable := pctl.io.rd_en
  mem.io.addr := Mux(pctl.io.rd_en, pctl.io.rdptr(asz - 1, 0), pctl.io.wrptr(asz-1,0))
  mem.io.writeData := io.enq.bits
  mem.io.writeEnable := pctl.io.wr_en
  mem.attachMemory(io.memControl)

  outq.io.enq.bits := mem.io.readData
  outq.io.enq.valid := pctl.io.memRdValid
  io.deq <> outq.io.deq
}
