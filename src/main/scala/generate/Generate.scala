package generate

import net.sourceforge.argparse4j.ArgumentParsers
import chisel3.stage.ChiselStage
import net.sourceforge.argparse4j.impl.Arguments.storeTrue
import packet.generic.{Memgen1R1W, Memgen1RW}
import packet.packetbuf.{BufferConfig, FlatPacketBufferComplex}

object Generate extends App {
  def getIntField(node : scala.xml.NodeSeq, name : String, default : Int = 0) : Int = {
    if ((node \ name).text == "")
      return default
    else return (node \ name).text.toInt
  }
  val parser = ArgumentParsers.newFor("SwitchFabricCodeGenerator").build.description("Create structures needed for Big Sur Switch Fabric")
  parser.addArgument("-x").dest("xml").metavar("FILE").`type`(classOf[String]).help("XML description file")
  parser.addArgument("--buffer").dest("buffer").action(storeTrue).help("Add output buffering")

  try {
    val parsed = parser.parseArgs(args)
    val stage = new ChiselStage

    val device = scala.xml.XML.loadFile(parsed.get("xml").toString)
    
    val bconf = device \ "BufferConfig"
    val numWritePorts = getIntField(bconf, "WriteClients", 2)
    val writePortSeq = for (i <- 0 until numWritePorts) yield i

    val bconf2 = new BufferConfig(new Memgen1R1W, new Memgen1RW, getIntField(bconf, "NumPools", 1),
      getIntField(bconf, "PagePerPool", 1), getIntField(bconf, "WordSize", 4), getIntField(bconf, "LinesPerPage", 4),
      getIntField(bconf, "ReadClients", 2), numWritePorts, getIntField(bconf, "MTU", 1518),
      getIntField(bconf, "credit", 1), getIntField(bconf, "ReadWordBuffer", 1), (bconf \ "PacketBuffer2Port").text.toBoolean,
      getIntField(bconf, "PacketBufferReadLatency", 1), getIntField(bconf, "MaxReferenceCount", 1), writePortSeq)

    val buildArgs = Array("--target-dir", "generated")

    stage.emitVerilog(new FlatPacketBufferComplex(bconf2), buildArgs)
  } catch {
    case e : net.sourceforge.argparse4j.helper.HelpScreenException => ;
    case e : net.sourceforge.argparse4j.inf.ArgumentParserException => println(e);
  }
}
