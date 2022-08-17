package packet.axis

import chisel.lib.dclib.DCMirror
import chisel3._
import chisel3.util._
import packet.test.{PacketReceiver, PacketRequest, PacketSender}

class AxisTestbench (width : Int) extends Module {
  val io = IO(new Bundle {
    val req = Flipped(ValidIO(new PacketRequest))
    val error = Output(Bool())
    val expQueueEmpty = Output(Bool())
  })
  val sender = Module(new PacketSender(width, 2, 2))
  val receiver = Module(new PacketReceiver(width, 2, 2))
  val reqmirror = Module(new DCMirror(new PacketRequest, 2))
  val error = RegInit(init=false.B)

  reqmirror.io.c.valid := io.req.valid
  reqmirror.io.dst := 3.U
  reqmirror.io.c.bits := io.req.bits
  sender.io.id := 0.U
  receiver.io.id := 0.U

  sender.io.sendPacket <> reqmirror.io.p(0)
  receiver.io.sendPacket <> reqmirror.io.p(1)
  sender.io.destIn.ready := 1.B

  val p2a = Module(new GenPktToAxi(width))
  val a2p = Module(new GenAxiToPkt(width))

  sender.io.packetData <> p2a.io.in
  p2a.io.out <> a2p.io.in
  a2p.io.out <> receiver.io.packetData

  error := error | receiver.io.error
  io.error := error
  io.expQueueEmpty := receiver.io.expQueueEmpty
}

