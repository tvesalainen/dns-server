/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tkv
 */
public class MessageWriter
{
    private byte[] buffer = new byte[4096];
    private int index;
    private int mark;
    private Map<DomainName,Integer> domainNameMap = new HashMap<DomainName,Integer>();

    public MessageWriter()
    {
    }

    public void write(int value)
    {
        buffer[index++] = (byte) (value & 0xff);
    }

    public void write16(int value)
    {
        write((value>>8) & 0xff);
        write((value) & 0xff);
    }

    public void write32(int value)
    {
        write((value>>24) & 0xff);
        write((value>>16) & 0xff);
        write((value>>8) & 0xff);
        write((value) & 0xff);
    }

    public void write(byte[] bb)
    {
        for (byte b : bb)
        {
            write(b);
        }
    }

    public void writeDomainName(DomainName name) throws IOException
    {
        try
        {
            DomainName dn = name;
            while (dn != null)
            {
                Integer offset = domainNameMap.get(dn);
                if (offset != null)
                {
                    offset |= 0xc000;
                    write16(offset);
                    return;
                }
                domainNameMap.put(dn, size());
                String label = dn.getSubDomain();
                writeCharacterString(label);
                dn = dn.getDomain();
            }
            write(0);
        }
        catch (IndexOutOfBoundsException ex)
        {
            System.err.println("Name='"+name+"'");
            throw ex;
        }
    }

    public void writeCharacterString(String str) throws IOException
    {
        write(str.length());
        write(str.getBytes());
    }

    public byte[] toByteArray()
    {
        return Arrays.copyOf(buffer, index);
    }

    public int size()
    {
        return index;
    }
    public void mark()
    {
        mark = index;
    }
    public void reset()
    {
        index = mark;
    }
    public void skip(int bytes)
    {
        index += bytes;
    }
}
