package org.ghutchis.codegen;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

public class PacketBufferGenerationFactory {
    public static PacketBufferGeneration unmarshalResponse(String xmlData) {
        PacketBufferGeneration device = null;
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(PacketBufferGeneration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(xmlData);

            device = (PacketBufferGeneration) jaxbUnmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            System.out.println("Parsing exception: " + e.toString());
            device = new PacketBufferGeneration();
        }
        return device;
    }
}
