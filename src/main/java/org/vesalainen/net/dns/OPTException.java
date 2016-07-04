/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author tkv
 * @see <a href="https://tools.ietf.org/html/rfc6891">Extension Mechanisms for DNS (EDNS(0))</a>
 */
public class OPTException extends Exception
{
    private int udpPayload;
    private int extendedRCode;
    private int version;
    private final int dnsSecOk;
    private int z;
    private int rdLength;
    private List<Option> options;

    /**
     * Constructs an instance of <code>OPTException</code> with the specified detail message.
     * @param msg the detail message.
     * @param udpPayload
     * @param ttl
     * @param rdLength
     * @param reader
     * @throws java.io.IOException
     */
    public OPTException(String msg, int udpPayload, int ttl, int rdLength, MessageReader reader) throws IOException
    {
        super(msg);
        this.udpPayload = udpPayload;
        this.extendedRCode = ttl >> 24;
        this.version = (ttl >> 16) & 0xff;
        this.dnsSecOk = (ttl >> 15) & 0x1;
        this.z = ttl & 0b111111111111111;
        this.rdLength = rdLength;
        int length = 0;
        if (rdLength > length)
        {
            options = new ArrayList<>();
        }
        while (rdLength > length)
        {
            Option option = new Option(reader);
            options.add(option);
            length += option.getOptLength() + 2;
        }
        
    }

    /**
     * @return the udpPayload
     */
    public int getUdpPayload()
    {
        return udpPayload;
    }

    @Override
    public String toString()
    {
        return "OPTException{" + "udpPayload=" + udpPayload + ", extendedRCode=" + extendedRCode + ", version=" + version + ", DO=" + dnsSecOk + ", z=" + z + ", optCount=" + rdLength + ", options=" + options + '}';
    }
    
    public static class Option
    {
        private int code;
        private final int optLength;
        private byte[] data;

        public Option(MessageReader reader) throws IOException
        {
            this.code = reader.read8();
            this.optLength = reader.read8();
            data = new byte[optLength];
            reader.read(data);
        }

        public int getCode()
        {
            return code;
        }

        public int getOptLength()
        {
            return optLength;
        }

        public byte[] getData()
        {
            return data;
        }

        @Override
        public String toString()
        {
            return "Option{" + "code=" + code + ", optLength=" + optLength + ", data=" + Arrays.toString(data) + '}';
        }
        
    }
}
