package org.ghutchis.codegen;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class PacketBufferConfig {
    public int NumPools;
    public int PagePerPool ;
    public int WordSize ;
    public int LinesPerPage ;
    public int ReadClients ;
    public int WriteClients ;
    public int MTU ;
    public int credit;
    public int PacketBufferReadLatency;
    public int ReadWordBuffer;
    public boolean PacketBuffer2Port;
    public int MaxReferenceCount;
    public boolean HasDropPort;
    public List<Integer> WritePortOrder;
}
