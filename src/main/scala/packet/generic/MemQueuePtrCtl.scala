package packet.generic

import chisel3._
import chisel3.util._
import chisel3.util.experimental.InlineInstance

class MemQueuePtrCtlIO(maxDepth : Int, outQueueSize : Int) extends Bundle {
  val init = Input(Bool())
  val asz = log2Ceil(maxDepth)
  val enqValid = Input(Bool())
  val enqReady = Output(Bool())
  val wrptr = Output(UInt(asz.W))
  val rdptr = Output(UInt(asz.W))
  val readGate = Input(Bool())
  val writeGate = Input(Bool())
  val usage = Output(UInt(log2Ceil(maxDepth+outQueueSize+1).W))
  val outputQueueCount = Input(UInt(log2Ceil(outQueueSize+1).W))
  val lowerBound = Input(UInt(log2Ceil(maxDepth).W))
  val upperBound = Input(UInt(log2Ceil(maxDepth).W))
  val wr_en = Output(Bool())
  val rd_en = Output(Bool())
  val memRdValid = Output(Bool())
  val deqFire = Input(Bool())
  val fifoDataAvail = Output(Bool())
}

class MemQueuePtrCtl(depth : Int, readLatency : Int) extends Module with InlineInstance {
  val outqSize = readLatency + 2
  val io = IO(new MemQueuePtrCtlIO(depth, outqSize))

  val asz = log2Ceil(depth)
  val wrptr = RegInit(init = 0.U(asz.W))
  val rdptr = RegInit(init = 0.U(asz.W))
  val nxt_rdptr = WireDefault(rdptr)
  val nxt_wrptr = WireDefault(wrptr)
  val wrptr_p1 = Wire(UInt(asz.W))
  val rdptr_p1 = Wire(UInt(asz.W))
  val full = RegInit(0.B)
  val wr_addr = wrptr(asz - 1, 0)
  val rd_addr = rdptr(asz - 1, 0)
  val nxt_valid = wrptr =/= rdptr
  val deq_valid = RegInit(init=0.U(readLatency.W))
  val readInFlight =  PopCount(deq_valid)
  val outPipeSize = readInFlight +& io.outputQueueCount
  //val outPipeFull = outPipeSize >= outqSize.U
  val rd_en = nxt_valid & ((outPipeSize < outqSize.U) || io.deqFire) & io.readGate
  val wr_en = !io.init & io.enqValid & !full & !rd_en & io.writeGate
  val intUsage = Wire(UInt(log2Ceil(depth + 1).W))


  if (readLatency==1) deq_valid := rd_en.asUInt
  else deq_valid := Cat(deq_valid(readLatency-2,0), rd_en)
  io.memRdValid := deq_valid(readLatency-1)

  def sat_add(ptr: UInt): UInt = {
    val plus1 = Wire(UInt(ptr.getWidth.W))
    when(ptr === io.upperBound) {
      plus1 := io.lowerBound
    }.otherwise {
      plus1 := ptr + 1.U
    }
    plus1
  }

  wrptr_p1 := sat_add(wrptr)
  rdptr_p1 := sat_add(rdptr)
  io.fifoDataAvail := nxt_valid
  val queueDepth = io.upperBound-io.lowerBound+1.U

  when (io.init) {
    full := false.B
    intUsage := 0.U
  }.elsewhen(!full) {
    full := (wrptr_p1 === rdptr) && (nxt_wrptr === nxt_rdptr)
    when(wrptr >= rdptr) {
      intUsage := wrptr - rdptr
    }.otherwise {
      intUsage := wrptr +& queueDepth - rdptr
    }
  }.otherwise {
    intUsage := queueDepth
    full := !rd_en
  }
  io.usage := intUsage +& outPipeSize

  io.enqReady := !io.init & !full & !rd_en & io.writeGate

  when(wr_en) {
    nxt_wrptr := wrptr_p1
  }

  when(rd_en) {
    nxt_rdptr := rdptr_p1
  }
  when (io.init) {
    rdptr := io.lowerBound
    wrptr := io.lowerBound
  }.otherwise {
    rdptr := nxt_rdptr
    wrptr := nxt_wrptr
  }

  io.wrptr := wrptr
  io.rdptr := rdptr
  io.wr_en := wr_en
  io.rd_en := rd_en
}
