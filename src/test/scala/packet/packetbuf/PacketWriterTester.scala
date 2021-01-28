package packet.packetbuf

import chisel3._
import chisel3.util.ImplicitConversions.intToUInt
import chisel3.util._
import chiseltest._
import org.scalatest._
import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.WriteVcdAnnotation
import chisel3.experimental.BundleLiterals._

class StubStatistics(c : BufferConfig) extends Bundle {
  val writePageCount = UInt(log2Ceil(c.maxPagePerPacket).W)
  val writeLineCount = UInt(log2Ceil(c.MTU).W)
  override def cloneType = new StubStatistics(c).asInstanceOf[this.type]
}

class BufferInterfaceStub(c: BufferConfig, id: Int) extends Module {
  val io = IO(new Bundle {
    val interface = Flipped(new PacketWriterInterface(c))
    val writeReqOut = ValidIO(new BufferWriteReq(c))
    val error = Output(Bool())
    val count = ValidIO(new StubStatistics(c))
  })

  val writePageCount = RegInit(init=0.U(log2Ceil(c.maxPagePerPacket).W))
  val writeLineCount = RegInit(0.U(log2Ceil(c.MTU).W))
  val error = RegInit(false.B)
  // automatically return credit for each request
  io.interface.freeListReq.credit := RegNext(next=io.interface.freeListReq.valid, init=false.B)
  io.interface.linkListWriteReq.credit := RegNext(next=io.interface.linkListWriteReq.valid, init=false.B)
  io.interface.freeListPage.valid := RegNext(next=io.interface.freeListReq.valid, init=false.B)

  // check request is from proper id
  when (io.interface.freeListReq.valid) {
    when (io.interface.freeListReq.bits.requestor =/= id.U) {
      error := true.B
    }
  }

  // return incrementing page numbers, client won't care if they go out of range
  val pageNum = RegInit(init=0.U(log2Ceil(c.PagePerPool).W))
  io.interface.freeListPage.bits.requestor := id.U
  io.interface.freeListPage.bits.page.pageNum := pageNum
  io.interface.freeListPage.bits.page.pool := RegNext(next=io.interface.freeListReq.bits.pool)
  when (io.interface.freeListPage.valid) {
    pageNum := pageNum + 1.U
  }

  // have slot count increment between all write clients, regardless of requests
  val slotCount = RegInit(init=0.U(log2Ceil(c.WriteClients).W))
  when (slotCount === (c.WriteClients-1).U) {
    slotCount := 0.U
  }.otherwise {
    slotCount := slotCount + 1.U
  }
  io.interface.writeReqIn.valid := false.B
  io.interface.writeReqIn.bits := 0.asTypeOf(new BufferWriteReq(c))
  io.interface.writeReqIn.bits.slot := slotCount

  // pass write req out through
  io.writeReqOut := io.interface.writeReqOut
  io.error := error
  when (io.interface.writeReqOut.valid) {
    writeLineCount := writeLineCount + 1.U
  }

  // Keep track of how many pages are written for each packet
  when (io.interface.linkListWriteReq.valid) {
    writePageCount := writePageCount + 1.U
  }
  io.count.bits.writePageCount := writePageCount
  io.count.bits.writeLineCount := writeLineCount
  io.count.valid := RegNext(next=io.interface.linkListWriteReq.valid & !io.interface.linkListWriteReq.bits.data.nextPageValid)
}

class PacketWriterTestbench(c: BufferConfig) extends Module {
  val io = IO(new Bundle {
    val sendPacket = Flipped(Decoupled(new PacketRequest))
    val writeReqOut = ValidIO(new BufferWriteReq(c))
    val error = Output(Bool())
    val count = ValidIO(new StubStatistics(c))
  })
  val sender = Module(new PacketSender(c.WordSize))
  val ifStub = Module(new BufferInterfaceStub(c, 0))
  val dut = Module(new PacketWriter(c, 0))

  io.sendPacket <> sender.io.sendPacket
  io.writeReqOut <> ifStub.io.writeReqOut
  io.count <> ifStub.io.count

  sender.io.packetData <> dut.io.portDataIn
  dut.io.interface <> ifStub.io.interface

  // return sched credit
  dut.io.schedOut.credit := RegNext(next=dut.io.schedOut.valid, init=false.B)

  // tie destination to 1
  dut.io.destIn.valid := true.B
  dut.io.destIn.bits.dest := 1.U
  io.error := ifStub.io.error
}

class PacketWriterTester extends FlatSpec with ChiselScalatestTester with Matchers  {
  behavior of "Testers2"

  ignore should "write a packet with correct number of lines and pages" in {
    val pagePerPool = 4
    val wordSize = 2
    val linesPerPage = 4
    val conf = new BufferConfig(1, pagePerPool, wordSize, linesPerPage, 2, 2, MTU = 2048, credit = 2)
    val poolNum = 1

    test(new PacketWriterTestbench(conf)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        val length = 128
        val expNumPages = length / (wordSize*linesPerPage)
          c.io.sendPacket.bits.poke(new PacketRequest().Lit(_.length -> length.U, _.packetGood -> true.B, _.pid -> 0.U))
        c.io.sendPacket.valid.poke(true.B)
        c.clock.step(1)
        c.io.sendPacket.valid.poke(false.B)
        c.clock.step(10)
        while (c.io.count.valid.peek().litToBoolean == false) {
          c.clock.step(1)
        }
        c.io.count.bits.writePageCount.expect(expNumPages.U)
        c.io.count.bits.writeLineCount.expect(length / conf.WordSize - 1)
        c.clock.step(5)
      }
    }
  }

  ignore should "request pages from pools" in {
    val pagePerPool = 32
    val conf = new BufferConfig(4, pagePerPool, 2, 4, 2, 2, MTU = 2048, credit = 2)
    test(new BasicPacketBufferAllocator(conf, 0)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        c.io.freeListReq.credit.poke(false.B)
        c.clock.step(1)
        c.io.freeListReq.valid.expect(true.B)

        for (i <- 0 to 31) {
          c.io.freeListPage.valid.poke(true.B)
          c.io.freeListPage.bits.page.pool.poke((i%4).U)
          c.io.freeListPage.bits.page.pageNum.poke(i.U)
          c.clock.step(1)
          c.io.freeListPage.valid.poke(false.B)
          c.clock.step(1)
          c.io.freePage.ready.poke(true.B)
          c.io.freePage.valid.expect(true.B)
          c.io.freePage.bits.pageNum.expect(i.U)
          c.io.freePage.bits.pool.expect((i%4).U)
          c.clock.step(1)
          c.io.freePage.ready.poke(false.B)
        }
      }
    }
  }
}
