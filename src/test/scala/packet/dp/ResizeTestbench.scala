package packet.dp

import chisel.lib.dclib.DCMirror
import chisel3._
import chisel3.util._
import packet.test.{PacketReceiver, PacketRequest, PacketSender}

class ResizeTestbench(in_width : Int, out_width : Int) extends Module {
  val io = IO(new Bundle {
    val req = Flipped(ValidIO(new PacketRequest))
    val error = Output(Bool())
    val expQueueEmpty = Output(Bool())
  })
  val sender = Module(new PacketSender(in_width, 2, 2))
  val receiver = Module(new PacketReceiver(out_width, 2, 2))
  val reqmirror = Module(new DCMirror(new PacketRequest, 2))
  val error = RegInit(init=false.B)

  reqmirror.io.c.valid := io.req.valid
  reqmirror.io.dst := 3.U
  reqmirror.io.c.bits := io.req.bits

  sender.io.id := 0.U
  receiver.io.id := 0.U
  sender.io.sendPacket <> reqmirror.io.p(0)
  receiver.io.sendPacket <> reqmirror.io.p(1)
  sender.io.destIn.ready := true.B

  if (out_width > in_width) {
    val upsize = Module(new BusUpsize(inWidth = in_width, outWidth = out_width))
    sender.io.packetData <> upsize.io.in
    upsize.io.out <> receiver.io.packetData
  } else {
    val downsize = Module(new BusDownsize(in_width, out_width))
    sender.io.packetData <> downsize.io.in
    downsize.io.out <> receiver.io.packetData
  }

  error := error | receiver.io.error
  io.error := error
  io.expQueueEmpty := receiver.io.expQueueEmpty
}
