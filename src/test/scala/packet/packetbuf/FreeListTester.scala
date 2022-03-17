package packet.packetbuf

import chisel3._
import chiseltest._
import org.scalatest._
import chiseltest.ChiselScalatestTester
import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.WriteVcdAnnotation
import chisel3.experimental.BundleLiterals._
import packet.generic.{Memgen1R1W, Memgen1RW}

class FreeListTester extends FlatSpec with ChiselScalatestTester with Matchers {
    behavior of "Testers2 with Queue"

    // gets strange "none.get" error from freeRequestIn
    ignore should "init all pages" in {
      val pagePerPool = 8
      val conf = new BufferConfig(new Memgen1R1W, new Memgen1RW,1, pagePerPool, 2, 4, 2, 2, MTU=2048, credit=2)

      test(new FreeList(conf)).withAnnotations(Seq(WriteVcdAnnotation)) {
        c => {
          for (client <- 0 until conf.WriteClients) {
            c.io.freeRequestIn(client).initSource().setSourceClock(c.clock)
            c.io.freeRequestOut(client).initSink().setSinkClock(c.clock)
          }
          for (client <- 0 until conf.ReadClients) {
            c.io.freeReturnIn(client).initSource().setSourceClock(c.clock)

          }

          val reqSeq = for(i <- 0 until pagePerPool) yield new PageReq(conf).Lit(_.requestor -> 0.U)
          val replySeq = for(i <- 0 until pagePerPool) yield new PageResp(conf).Lit(_.requestor -> 0.U, _.page -> new PageType(conf).Lit(_.pageNum -> i.U))

          fork {
            c.io.freeRequestIn(0).enqueueSeq(reqSeq)
          }.fork {
            c.io.freeRequestOut(0).expectDequeueSeq(replySeq)
          }.join()
        }
      }
    }

  it should "not free page until ref count zero" in {
    val pagePerPool = 4
    val numPools = 2
    val conf = new BufferConfig(new Memgen1R1W, new Memgen1RW, numPools, pagePerPool, 2, 4, 2, 2, MTU=2048, credit=2, MaxReferenceCount = 3)

    test(new FreeList(conf)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        for (client <- 0 to 1) {
          c.io.freeRequestIn(client).initSource().setSourceClock(c.clock)
          c.io.freeRequestOut(client).initSink().setSinkClock(c.clock)
          c.io.freeReturnIn(client).initSource().setSourceClock(c.clock)
          c.io.refCountAdd.get(client).initSource().setSourceClock(c.clock)
        }


        for (client <- 0 to 1) {
          for (poolNum <- 0 until numPools) {
            val reqSeq = for (i <- 0 until pagePerPool) yield new PageReq(conf).Lit(_.requestor -> client.U, _.pool.get -> poolNum.U)
            val replySeq = for (i <- 0 until pagePerPool) yield new PageResp(conf).Lit(_.requestor -> client.U, _.page -> new PageType(conf).Lit(_.pool -> poolNum.U, _.pageNum -> i.U))
            val returnSeq = for (i <- 0 until pagePerPool) yield new PageType(conf).Lit(_.pool -> poolNum.U, _.pageNum -> i.U)
            val addCount = for (i <- 0 until pagePerPool) yield new RefCountAdd(conf).Lit(_.amount -> 2.U, _.page -> new PageType(conf).Lit(_.pool -> poolNum.U, _.pageNum -> i.U))

            // First allocate all pages from pool
            fork {
              c.io.freeRequestIn(client).enqueueSeq(reqSeq)
            }.fork {
              c.io.freeRequestOut(client).expectDequeueSeq(replySeq)
            }.join()

            // Update the reference count for all pages
            c.io.refCountAdd.get(client).enqueueSeq(addCount)

            // client should own all pages
            c.io.pagesPerPort(client).expect(pagePerPool.U)

            // Free the pages twice, client should still own all pages
            c.io.freeReturnIn(client).enqueueSeq(returnSeq)
            c.io.freeReturnIn(client).enqueueSeq(returnSeq)
            c.clock.step(1)
            c.io.pagesPerPort(client).expect(pagePerPool.U)

            // return the used pages so to be ready for next step
            c.io.freeReturnIn(client).enqueueSeq(returnSeq)
            c.clock.step(2)
            c.io.pagesPerPort(client).expect(0.U)
          }
        }
      }
    }
  }

  it should "init all pages in multiple pools" in {
    val pagePerPool = 16
    val numPools = 4
    val conf = new BufferConfig(new Memgen1R1W, new Memgen1RW, numPools, pagePerPool, 2, 4, 2, 2, MTU=2048, credit=2, MaxReferenceCount = 2)

    test(new FreeList(conf)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        for (client <- 0 to 1) {
          c.io.freeRequestIn(client).initSource().setSourceClock(c.clock)
          c.io.freeRequestOut(client).initSink().setSinkClock(c.clock)
          c.io.freeReturnIn(client).initSource().setSourceClock(c.clock)
        }


        for (client <- 0 to 1) {
          for (poolNum <- 0 until numPools) {
            val reqSeq = for (i <- 0 until pagePerPool) yield new PageReq(conf).Lit(_.requestor -> client.U, _.pool.get -> poolNum.U)
            val replySeq = for (i <- 0 until pagePerPool) yield new PageResp(conf).Lit(_.requestor -> client.U, _.page -> new PageType(conf).Lit(_.pool -> poolNum.U, _.pageNum -> i.U))
            val returnSeq = for (i <- 0 until pagePerPool) yield new PageType(conf).Lit(_.pool -> poolNum.U, _.pageNum -> i.U)
            fork {
              c.io.freeRequestIn(client).enqueueSeq(reqSeq)
            }.fork {
              c.io.freeRequestOut(client).expectDequeueSeq(replySeq)
            }.join()

            c.io.pagesPerPort(client).expect(pagePerPool.U)
            // return the used pages so to be ready for next step
            c.io.freeReturnIn(client).enqueueSeq(returnSeq)
            c.clock.step(1)
            c.io.pagesPerPort(client).expect(0.U)
          }
        }
      }
    }
  }

  it should "stall return on update conflict" in {
    val pagePerPool = 16
    val numPools = 4
    val conf = new BufferConfig(new Memgen1R1W, new Memgen1RW, numPools, pagePerPool, 2, 4, 2, 2, MTU = 2048, credit = 2)

    test(new FreeList(conf)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        for (client <- 0 to 1) {
          c.io.freeRequestIn(client).initSource().setSourceClock(c.clock)
          //c.io.freeRequestOut(client).initSink().setSinkClock(c.clock)
          c.io.freeReturnIn(client).initSource().setSourceClock(c.clock)
        }

        val client = 0
        for (poolNum <- 0 until numPools) {
          val reqSeq = for (i <- 0 until pagePerPool) yield new PageReq(conf).Lit(_.requestor -> client.U, _.pool.get -> poolNum.U)
          val returnSeq = for (i <- 0 until pagePerPool) yield new PageType(conf).Lit(_.pool -> poolNum.U, _.pageNum -> i.U)

          // accept all requests, don't care for this test
          c.io.freeRequestOut(client).ready.poke(true.B)

          fork {
            c.io.freeRequestIn(client).enqueueSeq(reqSeq)
          }.fork {
            c.io.freeReturnIn(client).enqueueSeq(returnSeq)
          }.join()

          c.clock.step(2)
          c.io.pagesPerPort(client).expect(0.U)
        }
      }
    }
  }

  ignore should "init all pages in single pool" in {
    val pagePerPool = 4
    val conf = new BufferConfig(new Memgen1R1W, new Memgen1RW, 1, pagePerPool, 2, 4, 2, 2, MTU=2048, credit=2)
    val poolNum = 0

    test(new FreeList(conf)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        for (client <- 0 to 1) {
          c.io.freeRequestIn(client).initSource().setSourceClock(c.clock)
          c.io.freeRequestOut(client).initSink().setSinkClock(c.clock)
          c.io.freeReturnIn(client).initSource().setSourceClock(c.clock)
        }


        for (client <- 0 to 1) {
          val reqSeq = for(i <- 0 until pagePerPool) yield new PageReq(conf).Lit(_.requestor -> client.U)
          fork {
            c.io.freeRequestIn(client).enqueueSeq(reqSeq)
          }
        }
        for (client <- 0 to 1) {
          val replySeq = for(i <- 0 until pagePerPool) yield new PageResp(conf).Lit(_.requestor -> client.U, _.page -> new PageType(conf).Lit(_.pool -> poolNum.U, _.pageNum -> i.U))
          c.io.freeRequestOut(client).expectDequeueSeq(replySeq)
        }
      }
    }
  }
}
