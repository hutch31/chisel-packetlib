package packet

import chisel3._
import chisel3.util._
import packet._

class PacketCode extends Bundle {
  //val sop :: dat :: eop :: err :: Nil = Enum(4)
  val code = UInt(2.W)
  def isSop() : Bool = { this.code === packetSop }
  def isEop() : Bool = { (this.code === packetGoodEop) || (this.code === packetBadEop)}
  def isGoodEop() : Bool = { this.code === packetGoodEop}
  def isBadEop() : Bool = { this.code === packetBadEop}
}

/** Class for carrying packetized data around within a design.  By convention this class stores
  * data in network byte order, meaning the first byte transmitted is data(0) and the last byte
  * transmitted is data(size-1).
 *
 * The count field is valid only for the last (eop) word of data, and indicates how many bytes of that
 * word are valid.  For all other words, this should be set as 0.U and read as don't care
  */
class PacketData(size: Int) extends Bundle {
  val data = Vec(size, UInt(8.W))
  val count = UInt(log2Ceil(size).W)
  val code = new PacketCode
  override def cloneType = new PacketData(size).asInstanceOf[this.type]
}
