package packet.packetbuf

import chisel3._
import chisel3.util._

class MaxGrantCount(numReq : Int) extends Module {
  val io = IO(new Bundle {
    val requests = Input(UInt(numReq.W))
    val grants = Output(UInt(numReq.W))
    val maxGrant = Input(UInt(log2Ceil(numReq+1).W))
  })
  val priority = RegInit(init=0.U(log2Ceil(numReq).W))
  val numGrant = Wire(Vec(numReq, UInt(log2Ceil(numReq+1).W)))
  val granted = Wire(Vec(numReq, Bool()))
  val rotRequest = io.requests.rotateRight(priority)

  def HighPriorityEncoder(in : UInt) : UInt = {
    PriorityMux(in.asBools.reverse, for (i <- in.getWidth-1 to 0 by -1) yield i.U)
  }

  def addMod(x : UInt, y : UInt, m : Int) : UInt = {
    val sum = x +& y
    val result = sum
    when (sum >= m.U) {
      result := sum - m.U
    }
    result
  }

  for (i <- 0 until numReq) {
    if (i == 0) {
      granted(0) := rotRequest(0) & (io.maxGrant =/= 0.U)
      numGrant(0) := granted(0)
    } else {
      granted(i) := rotRequest(i) & (numGrant(i-1) < io.maxGrant)
      numGrant(i) := granted(i) +& numGrant(i-1)
    }
  }
  io.grants := Reverse(Cat(granted)).rotateLeft(priority)
  val curGrantNum = HighPriorityEncoder(io.grants)
  val nextPriority = Mux(curGrantNum === (numReq-1).U, 0.U, curGrantNum + 1.U)
  when (io.grants =/= 0.U) {
    priority := nextPriority
  }
}
