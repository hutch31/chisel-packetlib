package packet

import chisel3._

package object packet {
  val packetSop = 0.U
  val packetBody = 1.U
  val packetGoodEop = 2.U
  val packetBadEop = 3.U
}
