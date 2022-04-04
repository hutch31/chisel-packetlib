package packet.packetbuf

import chisel3._
import chisel3.util.ImplicitConversions.intToUInt
import chisel3.util._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import packet.generic.{Memgen1R1W, Memgen1RW}

class PacketReaderTester extends AnyFreeSpec with ChiselScalatestTester {
   "request pages after getting descriptor" in {
    val pagePerPool = 32
    val requestor = 1
    val LinesPerPage = 4
    val conf = new BufferConfig(new Memgen1R1W, new Memgen1RW, 4, pagePerPool, 2, LinesPerPage, 2, 2, MTU = 2048, credit = 2)
    test(new PacketReader(conf)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        c.io.id.poke(requestor.U)
        c.io.schedIn.valid.poke(true.B)
        c.io.schedIn.bits.length.poke((2*LinesPerPage).U) // enough data for a full single page
        c.io.schedIn.bits.startPage.pool.poke(1.U)
        c.io.schedIn.bits.startPage.pageNum.poke(8.U)
        c.clock.step(1)
        c.io.schedIn.valid.poke(false.B)

        // wait for module to request the start page from the linked list
        while (c.io.interface.linkListReadReq.valid.peek().litToBoolean == false) {
          c.clock.step(1)
        }
        // check requested values are correct
        c.io.interface.linkListReadReq.bits.requestor.expect(requestor.U)
        c.io.interface.linkListReadReq.bits.addr.pool.expect(1.U)
        c.io.interface.linkListReadReq.bits.addr.pageNum.expect(8.U)

        // send back link list entry with nextPage false
        c.io.interface.linkListReadResp.valid.poke(true.B)
        c.io.interface.linkListReadResp.bits.requestor.poke(requestor.U)
        c.io.interface.linkListReadResp.bits.data.nextPageValid.poke(false.B)
        c.clock.step(1)
        c.io.interface.linkListReadResp.valid.poke(false.B)

        for (i <- 0 until LinesPerPage) {
          // wait for the module to request a page from the buffer
          while (c.io.interface.bufferReadReq.valid.peek().litToBoolean == false) {
            c.clock.step(1)
          }
          c.io.interface.bufferReadReq.bits.page.pageNum.expect(8.U)
          c.io.interface.bufferReadReq.bits.line.expect(i.U)

          // send the page response back, and a request credit
          c.io.bufferReadResp.valid.poke(true.B)
          c.io.bufferReadResp.bits.req.requestor.poke(requestor.U)
          c.io.interface.bufferReadReq.credit.poke(true.B)
          c.clock.step(1)
          c.io.bufferReadResp.valid.poke(false.B)
          c.io.interface.bufferReadReq.credit.poke(false.B)

          // wait for the page data to be written to the client
          while (c.io.portDataOut.valid.peek().litToBoolean == false) {
            c.clock.step(1)
          }
          c.io.portDataOut.ready.poke(true.B)
        }
      }
    }
  }
}
