package chisel.lib.dclib

import chisel3._
import chisel3.util._

/*
 * This block is used to convert a fixed (ValidIO) pipeline into a Decoupled
 * pipeline.  It tracks the number of requests entering and exiting the fixed
 * pipeline, and uses an output Queue to hold the output of the pipeline in
 * cases where ready is deasserted.
 *
 * The adapter tracks the number of items currently in the pipeline as well as
 * the number of items in the output Queue to determine if new requests can be
 * accepted
 */
class DCPipelineAdapter[D <: Data](data: D, pipelineDepth : Int) extends Module {
  val io = IO(new Bundle {
    val ready_in = Output(Bool())
    val valid_in = Input(Bool())
    val valid_pipe_in = Output(Bool())
    val pipeOut = Flipped(ValidIO(data))
    val deq = Decoupled(data)
  })
  override def desiredName: String = "DCPipelineAdapter_" + data.toString + "_D" + pipelineDepth.toString

  val pipeCount = RegInit(init=0.U(log2Ceil(pipelineDepth+1).W))
  val outQueue = Module(new Queue(data, pipelineDepth))
  val totalCount = pipeCount +& outQueue.io.count
  val ready = totalCount < pipelineDepth.U

  io.ready_in := ready
  io.valid_pipe_in := io.valid_in & ready

  when (io.valid_pipe_in & !io.pipeOut.valid) {
    pipeCount := pipeCount + 1.U
  }.elsewhen (!io.valid_pipe_in & io.pipeOut.valid) {
    pipeCount := pipeCount - 1.U
  }

  outQueue.io.enq.valid := io.pipeOut.valid
  outQueue.io.enq.bits := io.pipeOut.bits
  outQueue.io.deq <> io.deq
}
