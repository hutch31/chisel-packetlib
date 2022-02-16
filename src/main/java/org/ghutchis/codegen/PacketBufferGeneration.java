package org.ghutchis.codegen;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="PacketBufferGeneration")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class PacketBufferGeneration {
    @XmlElement(required=true)
    public PacketBufferConfig BufferConfig;
}
