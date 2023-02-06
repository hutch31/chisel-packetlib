//package packet

import chisel3._
import chisel3.util._

package object packet {
  val packetSop = 0.U(2.W)
  val packetBody = 1.U(2.W)
  val packetGoodEop = 2.U(2.W)
  val packetBadEop = 3.U(2.W)

  /**
   * Connect two decoupled interfaces with an enable signal, which allows the interface to be
   * cleanly shut off.
   * @param in   Decoupled input
   * @param out  Decoupled output
   * @param enable Active-high enable signal
   */
  def Spigot[T <: Data](in : DecoupledIO[T], out : DecoupledIO[T], enable : Bool) = {
    in <> out
    out.valid := in.valid & enable
    in.ready := out.ready & enable
  }
}
