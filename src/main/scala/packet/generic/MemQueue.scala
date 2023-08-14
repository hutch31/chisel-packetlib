package packet.generic

import chisel3._
import chisel3.util._

class MemQueue[D <: Data](data: D, depth : Int, gen : Memgen1R1W, memCon : MemoryControl, readLatency : Int = 1) extends Module {
  //val outqSize = readLatency*2+1
  val pctl = Module(new MemQueuePtrCtl(depth, readLatency))
  def outqSize = pctl.outqSize
  val io = IO(new Bundle {
    val enq = Flipped(new DecoupledIO(data.cloneType))
    val deq = new DecoupledIO(data.cloneType)
    val usage = Output(UInt(log2Ceil(depth+pctl.outqSize+1).W))
    val memControl = memCon.factory
  })
  override def desiredName: String = "MemQueue_" + data.toString + "_D" + depth.toString + "_L" + readLatency.toString

  val mem = Module(gen.apply(data, depth, readLatency))
  val asz = log2Ceil(depth)
  val outq = Module(new Queue(data.cloneType, pctl.outqSize))

  pctl.io.init := false.B
  pctl.io.outputQueueCount := outq.io.count
  pctl.io.lowerBound := 0.U
  pctl.io.upperBound := (depth-1).U
  pctl.io.readGate := true.B
  pctl.io.writeGate := true.B
  pctl.io.enqValid := io.enq.valid
  pctl.io.deqFire := io.deq.fire
  io.enq.ready := pctl.io.enqReady
  io.usage := pctl.io.usage

  mem.io.readEnable := pctl.io.rd_en
  mem.io.readAddr := pctl.io.rdptr(asz-1,0)
  mem.io.writeData := io.enq.bits
  mem.io.writeEnable := pctl.io.wr_en
  mem.io.writeAddr := pctl.io.wrptr(asz-1,0)
  mem.attachMemory(io.memControl)

  outq.io.enq.bits := mem.io.readData
  outq.io.enq.valid := pctl.io.memRdValid
  io.deq <> outq.io.deq
}
