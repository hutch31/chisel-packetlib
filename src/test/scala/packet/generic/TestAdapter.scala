package packet.generic

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec

import scala.util.Random

class TestAdapter extends AnyFreeSpec with ChiselScalatestTester {
  "random reads and writes" in {
    val words = 32
    test(new Adapter1R1W(UInt(16.W), words, 1)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        var contents = new Array[Int](words)
        val rand = new Random(100)

        def write(addr : Int, data : Int) = {
          c.io.writeAddr.poke(addr.U)
          c.io.writeData.poke(data.U)
          contents(addr) = data
          c.io.writeEnable.poke(1.B)
          c.clock.step(1)
          c.io.writeEnable.poke(0.B)
        }

        def read(addr : Int) = {
          c.io.readAddr.poke(addr.U)
          c.io.readEnable.poke(1.B)
          c.clock.step(1)
          c.io.readData.expect(contents(addr))
          c.io.readEnable.poke(0.B)
        }

        def readAndWrite(waddr : Int, wdata: Int, raddr : Int) = {
          c.io.writeAddr.poke(waddr.U)
          c.io.writeData.poke(wdata.U)
          contents(waddr) = wdata
          c.io.writeEnable.poke(1.B)
          c.io.readAddr.poke(raddr.U)
          c.io.readEnable.poke(1.B)
          c.clock.step(1)
          c.io.readData.expect(contents(raddr))
          c.io.readEnable.poke(0.B)
          c.io.writeEnable.poke(0.B)
        }

        // Fill memory with data
        for (i <- 0 until words) {
          write(i, rand.nextInt(16383))
        }

        // Randomly access memory
        for (i <- 0 until 256) {
          val raddr = rand.nextInt(words)
          val waddr = rand.nextInt(words)
          val wdata = rand.nextInt(16383)

          if (raddr == waddr) {
            write(waddr, wdata)
          } else {
            readAndWrite(waddr, wdata, raddr)
          }
        }

        // check all memory values
        for (i <- 0 until words) {
          read(i)
        }
      }
    }
  }
}
