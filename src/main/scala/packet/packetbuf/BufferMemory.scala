package packet.packetbuf

import chisel.lib.dclib._
import chisel3._
import chisel3.experimental.BundleLiterals.AddBundleLiteralConstructor
import chisel3.util.ImplicitConversions.intToUInt
import chisel3.util._

class BufferMemoryIO(c : BufferConfig) extends Bundle {
  val wrSlotReqIn = Input(Vec(c.WriteClients, Bool()))
  //val rdSlotReqIn = Input(Vec(c.WriteClients, Bool()))
  val writeReqIn = Flipped(ValidIO(new BufferWriteReq(c)))
  val writeReqOut = ValidIO(new BufferWriteReq(c))
  val readReqIn = Flipped(ValidIO(new BufferReadReq(c)))
  val readReqOut = ValidIO(new BufferReadReq(c))
  val readRespOut = ValidIO(new BufferReadResp(c))
  override def cloneType =
    new BufferMemoryIO(c).asInstanceOf[this.type]
}

class BufferMemory(c : BufferConfig) extends Module {
  val io = IO(new BufferMemoryIO(c))

  val wrScheduler = Module(new RingScheduler(c.WriteClients))
  //val rdScheduler = Module(new RingScheduler(c.ReadClients))
  val memPools = for (p <- 0 until c.NumPools) yield Module(new BufferMemoryPool(c))

  //val valid = Bool()
  //val slot = UInt(log2Ceil(c.WriteClients).W)
  //val page = new PageType(c)
  //val line = UInt(log2Ceil(c.LinesPerPage).W)
  //val data = Vec(c.WordSize, UInt(8.W))

  // collect read and write slot requests and create slot assignments for read/write rings
  wrScheduler.io.slotReqIn := io.wrSlotReqIn
  //io.writeReqOut := new WriteReq(c).Lit(_.valid -> false.B, _.slot -> 0.U, _.page -> 0.U, _.line -> 0.U, _.data -> 0.asTypeOf(Vec(c.WordSize, UInt(8.W))))
  io.writeReqOut.valid := false.B
  io.writeReqOut.bits := 0.asTypeOf(new BufferWriteReq(c))
  io.writeReqOut.bits.slot := wrScheduler.io.writeReqOut
  //rdScheduler.io.slotReqIn := io.rdSlotReqIn
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
}

class BufferMemoryPool(c : BufferConfig) extends Module {
  val io = IO(new Bundle {
    val writeReqIn = Flipped(ValidIO(new BufferWriteReq(c)))
    val readReqIn = Flipped(ValidIO(new BufferReadReq(c)))
    val readRespOut = ValidIO(new BufferReadResp(c))
  })
  /*
  val mem = SyncReadMem(c.LinesPerPage*c.PagePerPool, Vec(c.WordSize, UInt(8.W)))
  val memWrAddr = Cat(io.writeReqIn.bits.page.pageNum, io.writeReqIn.bits.line)
  val memRdAddr = Cat(io.readReqIn.bits.page.pageNum, io.readReqIn.bits.line)

  when (io.writeReqIn.valid) {
    mem.write(memWrAddr, io.writeReqIn.bits.data)
  }
  io.readRespOut.bits.data := mem.read(memRdAddr, io.readReqIn.valid)
  io.readRespOut.bits.req  := RegEnable(next=io.readReqIn.bits, enable=io.readReqIn.valid)
  io.readRespOut.valid     := RegNext(next=io.readReqIn.valid)
   */
  val mem = Module(c.mgen(Vec(c.WordSize, UInt(8.W)), c.LinesPerPage*c.PagePerPool, latency=c.PacketBufferReadLatency))
  mem.io.writeAddr := Cat(io.writeReqIn.bits.page.pageNum, io.writeReqIn.bits.line)
  mem.io.readAddr  := Cat(io.readReqIn.bits.page.pageNum, io.readReqIn.bits.line)
  mem.io.writeEnable := io.writeReqIn.valid
  mem.io.writeData   := io.writeReqIn.bits.data
  mem.io.readEnable  := io.readReqIn.valid
  io.readRespOut.bits.data := mem.io.readData
  io.readRespOut.bits.req  := ShiftRegister(io.readReqIn.bits, c.PacketBufferReadLatency)
  io.readRespOut.valid     := ShiftRegister(io.readReqIn.valid, c.PacketBufferReadLatency)
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
    val writeReqOut = Output(UInt(log2Ceil(clients).W))
  })
  val slotReqCount = RegInit(init=0.asTypeOf(Vec(clients, UInt(countSize.W))))
  val countPos = Wire(Vec(clients, Bool()))
  val nextSlot = PriorityEncoder(Cat(countPos.reverse))

  for (i <- 0 until clients) {
    countPos(i) := slotReqCount(i) =/= 0.U
    when (io.slotReqIn(i) && (nextSlot =/= i.U)) {
      slotReqCount(i) := slotReqCount(i) + 1.U
    }.elsewhen (!io.slotReqIn(i) && (nextSlot === i.U) && (slotReqCount(nextSlot) > 0.U)) {
      slotReqCount(i) := slotReqCount(i) - 1.U
    }
  }

  io.writeReqOut := nextSlot
}
