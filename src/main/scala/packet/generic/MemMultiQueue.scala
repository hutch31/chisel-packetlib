package packet.generic

import chisel3._
import chisel3.util._

class MemMultiQueue[D <: Data](data: D, depth : Int, numQueues : Int, gen : Memgen1R1W, memCon : MemoryControl, readLatency : Int = 1) extends Module {
  require (numQueues >= 2)
  //val outqSize = readLatency*2+1
  val pctl = for (i <- 0 until numQueues) yield Module(new MemQueuePtrCtl(depth, readLatency))

  def outqSize = pctl(0).outqSize

  val io = IO(new Bundle {
    val enq = Vec(numQueues, Flipped(new DecoupledIO(data.cloneType)))
    val deq = Vec(numQueues, new DecoupledIO(data.cloneType))
    val usage = Output(Vec(numQueues, UInt(log2Ceil(depth + outqSize + 1).W)))
    val lowerBound = Input(Vec(numQueues, UInt(log2Ceil(depth).W)))
    val upperBound = Input(Vec(numQueues, UInt(log2Ceil(depth).W)))
    val memControl = memCon.factory
  })

  val mem = Module(gen.apply(data, depth, readLatency))
  val asz = log2Ceil(depth)
  val writeSelect = PriorityEncoder(Reverse(Cat(io.enq.map(_.valid))))
  val readSelect = PriorityEncoder(Reverse(Cat(pctl.map(_.io.fifoDataAvail))))

  for (m <- 0 until numQueues) {
    val outq = Module(new Queue(data.cloneType, pctl(m).outqSize))

    pctl(m).io.outputQueueCount := outq.io.count
    pctl(m).io.lowerBound := io.lowerBound(m)
    pctl(m).io.upperBound := io.upperBound(m)
    pctl(m).io.readGate := readSelect === m.U
    pctl(m).io.writeGate := writeSelect === m.U
    pctl(m).io.enqValid := io.enq(m).valid
    pctl(m).io.deqFire := io.deq(m).fire
    io.enq(m).ready := pctl(m).io.enqReady
    io.usage(m) := pctl(m).io.usage


    outq.io.enq.bits := mem.io.readData
    outq.io.enq.valid := pctl(m).io.memRdValid
    io.deq(m) <> outq.io.deq
  }

  mem.io.readEnable := Cat(pctl.map(_.io.rd_en)).orR
  mem.io.readAddr := MuxLookup(readSelect, 0.U, for (i <- 0 until numQueues) yield i.U -> pctl(i).io.rdptr(asz - 1, 0))
  mem.io.writeData := io.enq(writeSelect).bits
  mem.io.writeEnable := Cat(pctl.map(_.io.wr_en)).orR
  mem.io.writeAddr := MuxLookup(writeSelect, 0.U,for (i <- 0 until numQueues) yield i.U -> pctl(i).io.wrptr(asz - 1, 0))
}
