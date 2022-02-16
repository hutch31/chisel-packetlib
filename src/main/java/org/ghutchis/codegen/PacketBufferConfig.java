package org.ghutchis.codegen;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

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
}
