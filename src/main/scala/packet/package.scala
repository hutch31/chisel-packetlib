//package packet

import chisel3._

package object packet {
  val packetSop = 0.U(2.W)
  val packetBody = 1.U(2.W)
  val packetGoodEop = 2.U(2.W)
  val packetBadEop = 3.U(2.W)
}
