package packet.dp

import chisel.lib.dclib._
import chisel3._
import chisel3.util._
import chisel3.util.ImplicitConversions.intToUInt
import packet.packet.packetSop
import packet.{PacketCode, PacketData}

/**
 * BusUpsize converts a lower-width bus to a higher-width bus.  To do this it
 * accumulates data words until it can form a full word on the larger bus, or
 * until it sees an incoming EOP.
 *
 * BusUpsize has no partial buffering, so requires that the outgoing interface is
 * an integer multiple of the incoming width.
 *
 * @param inWidth  Byte width of incoming bus
 * @param outWidth Byte width of outgoing bus
 */
class BusUpsize(inWidth : Int, outWidth : Int) extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new PacketData(inWidth)))
    val out = Decoupled(new PacketData(outWidth))
  })
  val maxWords = outWidth / inWidth
  require ((maxWords * inWidth) == outWidth)

  val accum = Reg(Vec(maxWords, new PacketData(inWidth)))
  val wordsStored = RegInit(init=0.U(log2Ceil(maxWords + 1).W))
  val lastWordEop = Wire(Bool())
  val fullCondition = Wire(Bool())
  val outHold = Module(new DCHold(new PacketData(outWidth)))
  val byteCount = Reg(UInt(log2Ceil(outWidth).W))

  outHold.io.deq <> io.out
  lastWordEop := (wordsStored =/= 0.U) && accum(wordsStored).code.isEop()
  fullCondition := lastWordEop || (wordsStored === maxWords.U)

  // Need an indication when we are simultaneously shifting data from from the accumulator into
  // outHold and storing a new word in the accumulator
  val shiftCondition = io.in.valid && fullCondition & outHold.io.enq.ready
  io.in.ready := !fullCondition || outHold.io.enq.ready

  outHold.io.enq.valid := false.B

  for (i <- 0 until outWidth) {
    outHold.io.enq.bits.data(i) := accum(i / inWidth).data(i % inWidth)
  }

  // When last word is an EOP, force output to be an EOP
  // When not an EOP and first word is an SOP, set to SOP, otherwise use the packet
  // code of the last word
  when (!lastWordEop && accum(0).code.isSop()) {
    outHold.io.enq.bits.code.code := packetSop
  }.elsewhen (wordsStored > 0) {
    outHold.io.enq.bits.code := accum(wordsStored-1).code
  }.otherwise {
    outHold.io.enq.bits.code.code := packetSop
  }

  // When last word is an EOP, use the running byte count plus last word count
  when (lastWordEop) {
    outHold.io.enq.bits.count := accum(wordsStored-1).count + byteCount
  }.otherwise {
    outHold.io.enq.bits.count := inWidth.U + byteCount
  }

  when (wordsStored === 0.U && io.in.valid) {
    when (io.in.bits.code.isEop()) {
      byteCount := io.in.bits.count
    }.otherwise {
      byteCount := inWidth.U
    }
  }

  outHold.io.enq.valid := false.B

  when (fullCondition) {
    outHold.io.enq.valid := true.B
    when (shiftCondition) {
      wordsStored := 1.U
      accum(0) := io.in.bits
    }.elsewhen (outHold.io.enq.ready) {
      wordsStored := 0.U
    }
  }.elsewhen (!fullCondition && io.in.valid) {
    accum(wordsStored) := io.in.bits
    wordsStored := wordsStored + 1.U
  }

}


