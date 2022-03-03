package org.ghutchis.codegen;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import chisel3.stage.ChiselStage;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;

import packet.generic.Memgen1R1W;
import packet.generic.Memgen1RW;
import packet.packetbuf.*;
import scala.collection.immutable.Seq;

public class GeneratorMain {
	public static Namespace parseArgs(String[] args) {
		Namespace res = null;
		
		ArgumentParser parser = ArgumentParsers.newFor("ChiselCodeGenerator").build()
                .description("Create multiplexer based on user provided record");
		parser.addArgument("-x").dest("xml").metavar("FILE").type(String.class).help("XML description file");
		parser.addArgument("--buffer").dest("buffer").action(storeTrue()).help("Add output buffering");
		try {
            res = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
		return res;
	}
	
    public static void main(String[] args) throws IOException {
    	Namespace parsed = parseArgs(args);
    	
    	if (parsed.get("xml") == null) {
    		System.out.println("XML argument is required");
    		System.exit(0);
    	}
    	File xmlFile = new File(parsed.get("xml").toString());
    	
        byte[] encoded = Files.readAllBytes(Paths.get(xmlFile.getPath()));
        String xmlConfig = new String(encoded, StandardCharsets.UTF_8);
        PacketBufferGeneration device = PacketBufferGenerationFactory.unmarshalResponse(xmlConfig);

        BufferConfig bconf = new BufferConfig(
				new Memgen1R1W(),
				new Memgen1RW(),
                device.BufferConfig.NumPools,
                device.BufferConfig.PagePerPool,
                device.BufferConfig.WordSize,
                device.BufferConfig.LinesPerPage,
                device.BufferConfig.ReadClients,
                device.BufferConfig.WriteClients,
                device.BufferConfig.MTU,
                device.BufferConfig.credit,
				2,
				true,
				1);

        ChiselStage stage = new ChiselStage();
        String[] buildArgs = {"--target-dir", "generated"};

		stage.emitVerilog(() -> { return new FlatPacketBufferComplex(bconf); } , buildArgs, stage.emitVerilog$default$3());
    }
}
