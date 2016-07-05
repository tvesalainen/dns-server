/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class MessageReader
{
    private byte[] data;
    private InputStream in;
    private int index;

    public MessageReader(byte[] data)
    {
        this.data = data;
        this.in = new ByteArrayInputStream(data);
    }

    public MessageReader(byte[] data, int offset, int length)
    {
        this.data = data;
        this.in = new ByteArrayInputStream(data, offset, length);
        this.index = offset;
    }

    public MessageReader offsetReader(int offset)
    {
        return new MessageReader(data, offset, data.length-offset);
    }

    public void read(byte[] bb) throws IOException
    {
        in.read(bb);
        index += bb.length;
    }

    public int read8() throws IOException
    {
        index++;
        return in.read();
    }

    public int read16() throws IOException
    {
        int i1 = read8();
        int i2 = read8();
        return (i1<<8)+i2;
    }

    public int read32() throws IOException
    {
        int i1 = read8();
        int i2 = read8();
        int i3 = read8();
        int i4 = read8();
        return (i1<<24)+(i2<<16)+(i3<<8)+i4;
    }

    public DomainName readDomainName() throws IOException
    {
        List<String> list = new ArrayList<>();
        int length = read8();
        while (length > 0)
        {
            if ((length & 0xc0) == 0xc0)
            {
                int offset = read8();
                offset += ((length & 0x3f)<<8);
                MessageReader mr = offsetReader(offset);
                DomainName dn = mr.readDomainName();
                return new DomainName(list, dn);
            }
            byte[] bb = new byte[length];
            read(bb);
            list.add(new String(bb));
            length = read8();
        }
        return new DomainName(list);
    }

    public String readCharacterString() throws IOException
    {
        int length = read8();
        byte[] bb = new byte[length];
        read(bb);
        return new String(bb);
    }

    public void mark()
    {
        in.mark(Integer.MAX_VALUE);
    }

    public void reset() throws IOException
    {
        in.reset();
    }

    public void skip(long bytes) throws IOException
    {
        in.skip(bytes);
    }

    @Override
    public String toString()
    {
        return "MessageReader{" + "at=" + index + " 0x" + Integer.toHexString(index) + '}';
    }
    
}
