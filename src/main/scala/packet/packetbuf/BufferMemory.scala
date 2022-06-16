package packet.packetbuf

import chisel3._
import chisel3.util.ImplicitConversions.intToUInt
import chisel3.util._
import packet.packetbuf

class BufferMemoryIO(val c : BufferConfig) extends Bundle {
  val wrSlotReqIn = Input(Vec(c.WriteClients, Bool()))
  val writeReqIn = Flipped(ValidIO(new BufferWriteReq(c)))
  val writeReqOut = ValidIO(new BufferWriteReq(c))
  val readReqIn = Flipped(ValidIO(new BufferReadReq(c)))
  val readReqOut = ValidIO(new BufferReadReq(c))
  val readRespOut = ValidIO(new BufferReadResp(c))
}

class BufferMemory(val c : BufferConfig) extends Module {
  val io = IO(new BufferMemoryIO(c))

  val wrScheduler = Module(new RingScheduler(c.WriteClients))
  val memPools = for (p <- 0 until c.NumPools) yield Module(new BufferMemoryPool(c))

  // collect read and write slot requests and create slot assignments for read/write rings
  wrScheduler.io.slotReqIn := io.wrSlotReqIn
  io.writeReqOut.valid := false.B
  io.writeReqOut.bits := 0.asTypeOf(new BufferWriteReq(c))
  io.writeReqOut.bits.slot := wrScheduler.io.writeReqOut
  io.writeReqOut.bits.slotValid := wrScheduler.io.slotValid
  io.readReqOut.valid := false.B
  io.readReqOut.bits := 0.asTypeOf(new BufferReadReq(c))

  // write incoming data to correct packet buffer
  for (i <- 0 until c.NumPools) {
    memPools(i).io.writeReqIn := io.writeReqIn
    memPools(i).io.writeReqIn.valid := io.writeReqIn.valid && (io.writeReqIn.bits.page.pool === i.U)

    memPools(i).io.readReqIn := io.readReqIn
    memPools(i).io.readReqIn.valid := io.readReqIn.valid && (io.readReqIn.bits.page.pool === i.U)
  }

  // send reads back from correct pool
  io.readRespOut.bits := 0.asTypeOf(new BufferReadResp(c))
  io.readRespOut.valid := false.B
  for (i <- 0 until c.NumPools) {
    when (memPools(i).io.readRespOut.valid) {
      io.readRespOut := memPools(i).io.readRespOut
      io.readRespOut.valid := true.B
    }
  }

  if (c.PacketBuffer2Port) {
    wrScheduler.io.slotReqMask := Fill(c.WriteClients, 1.U)
  } else {
    // Suppress writes on odd cycles so that read and write requests never happen on the same cycle
    // Start in odd or even state based on number of write clients, which dictates how long the write
    // ring is
    val odd = RegInit(init=(c.WriteClients % 2).B)
    odd := ~odd
    wrScheduler.io.slotReqMask := Fill(c.WriteClients, odd)
  }
}

class BufferMemoryPool(c : BufferConfig) extends Module {
  val io = IO(new Bundle {
    val writeReqIn = Flipped(ValidIO(new BufferWriteReq(c)))
    val readReqIn = Flipped(ValidIO(new BufferReadReq(c)))
    val readRespOut = ValidIO(new BufferReadResp(c))
  })
  if (c.PacketBuffer2Port) {
    val mem = Module(c.mgen2p(Vec(c.WordSize, UInt(8.W)), c.LinesPerPage * c.PagePerPool, latency = c.PacketBufferReadLatency))
    mem.io.writeAddr := Cat(io.writeReqIn.bits.page.pageNum, io.writeReqIn.bits.line)
    mem.io.readAddr := Cat(io.readReqIn.bits.page.pageNum, io.readReqIn.bits.line)
    mem.io.writeEnable := io.writeReqIn.valid
    mem.io.writeData := io.writeReqIn.bits.data
    mem.io.readEnable := io.readReqIn.valid
    io.readRespOut.bits.data := mem.io.readData
    io.readRespOut.bits.req := ShiftRegister(io.readReqIn.bits, c.PacketBufferReadLatency)
    io.readRespOut.valid := ShiftRegister(io.readReqIn.valid, c.PacketBufferReadLatency)
  } else {
    val mem = Module(c.mgen1p(Vec(c.WordSize, UInt(8.W)), c.LinesPerPage * c.PagePerPool, latency = c.PacketBufferReadLatency))
    when (io.readReqIn.valid) {
      mem.io.addr := Cat(io.readReqIn.bits.page.pageNum, io.readReqIn.bits.line)
    }.otherwise {
      mem.io.addr := Cat(io.writeReqIn.bits.page.pageNum, io.writeReqIn.bits.line)
    }
    when (io.writeReqIn.valid & io.readReqIn.valid) {
      printf("Error - read and write in same cycle\n")
    }
    mem.io.writeEnable := io.writeReqIn.valid & !io.readReqIn.valid
    mem.io.writeData := io.writeReqIn.bits.data
    mem.io.readEnable := io.readReqIn.valid
    io.readRespOut.bits.data := mem.io.readData
    io.readRespOut.bits.req := ShiftRegister(io.readReqIn.bits, c.PacketBufferReadLatency)
    io.readRespOut.valid := ShiftRegister(io.readReqIn.valid, c.PacketBufferReadLatency)
  }
}

/**
 * Implements a simple scheduler for slots on the ring, assumes
 * ring has enough bandwidth for all requests
 *
 * @param c
 */
class RingScheduler(clients : Int, countSize : Int = 4) extends Module {
  val io = IO(new Bundle {
    val slotReqIn = Input(Vec(clients, Bool()))
    val slotReqMask = Input(UInt(clients.W))
    val writeReqOut = Output(UInt(log2Ceil(clients).W))
    val slotValid = Output(Bool())
  })
  val slotReqCount = RegInit(init=0.asTypeOf(Vec(clients, UInt(countSize.W))))
  val countPos = Wire(Vec(clients, Bool()))
  val activeReq = Cat(countPos.reverse) & io.slotReqMask
  val nextSlot = Wire(UInt(log2Ceil(clients).W))
  val granted = RegInit(init=1.U(clients.W))
  val nextGranted = Wire(UInt(clients.W))
  val anyReq = activeReq =/= 0.U

  // if more than one request is present, arbitrate round robin for next write slot
  nextGranted := packetbuf.nxt_grant(clients, granted, activeReq, true.B)
  granted := nextGranted
  nextSlot := PriorityEncoder(nextGranted)

  for (i <- 0 until clients) {
    countPos(i) := slotReqCount(i) =/= 0.U
    val consuming = anyReq && countPos(i) && (nextSlot === i.U)
    when (io.slotReqIn(i) && !consuming) {
      slotReqCount(i) := slotReqCount(i) + 1.U
    }.elsewhen (!io.slotReqIn(i) && consuming) {
      slotReqCount(i) := slotReqCount(i) - 1.U
    }
  }

  io.writeReqOut := nextSlot
  io.slotValid := anyReq
}
