package packet

import chisel3.util._
import chisel3._

package object packetbuf {
  def nxt_grant(inputs : Int, cur_grant: UInt, cur_req: UInt, cur_accept: Bool): UInt = {
    val msk_req = Wire(UInt(inputs.W))
    val tmp_grant = Wire(UInt(inputs.W))
    val tmp_grant2 = Wire(UInt(inputs.W))
    val rv = Wire(UInt(inputs.W))

    msk_req := cur_req & ~((cur_grant - 1.U) | cur_grant)
    tmp_grant := msk_req & (~msk_req + 1.U)
    tmp_grant2 := cur_req & (~cur_req + 1.U)


    when(cur_accept) {
      when(msk_req =/= 0.U) {
        rv := tmp_grant
      }.otherwise {
        rv := tmp_grant2
      }
    }.elsewhen(cur_req =/= 0.U) {
      when(msk_req =/= 0.U) {
        rv := Cat(tmp_grant(0), tmp_grant(inputs - 1, 1))
      }.otherwise {
        rv := Cat(tmp_grant2(0), tmp_grant2(inputs - 1, 1))
      }
    }.otherwise {
      rv := cur_grant
    }
    rv
  }

}
