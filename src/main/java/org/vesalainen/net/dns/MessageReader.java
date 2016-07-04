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

    public MessageReader(byte[] data)
    {
        this.data = data;
        this.in = new ByteArrayInputStream(data);
    }

    public MessageReader(byte[] data, int offset, int length)
    {
        this.data = data;
        this.in = new ByteArrayInputStream(data, offset, length);
    }

    public MessageReader offsetReader(int offset)
    {
        return new MessageReader(data, offset, data.length-offset);
    }

    public void read(byte[] bb) throws IOException
    {
        in.read(bb);
    }

    public int read8() throws IOException
    {
        return in.read();
    }

    public int read16() throws IOException
    {
        int i1 = in.read();
        int i2 = in.read();
        return (i1<<8)+i2;
    }

    public int read32() throws IOException
    {
        int i1 = in.read();
        int i2 = in.read();
        int i3 = in.read();
        int i4 = in.read();
        return (i1<<24)+(i2<<16)+(i3<<8)+i4;
    }

    public DomainName readDomainName() throws IOException
    {
        List<String> list = new ArrayList<>();
        int length = in.read();
        while (length > 0)
        {
            if ((length & 0xc0) == 0xc0)
            {
                int offset = in.read(); // ????
                offset += ((length & 0x3f)<<8);
                MessageReader mr = offsetReader(offset);
                DomainName dn = mr.readDomainName();
                JavaLogging.getLogger(MessageReader.class).fine("compressed domain name %s", dn);
                return new DomainName(list, dn);
            }
            byte[] bb = new byte[length];
            in.read(bb);
            list.add(new String(bb));
            length = in.read();
        }
        return new DomainName(list);
    }

    public String readCharacterString() throws IOException
    {
        int length = in.read();
        byte[] bb = new byte[length];
        in.read(bb);
        return new String(bb);
    }

    public void mark()
    {
        in.mark(512);
    }

    public void reset() throws IOException
    {
        in.reset();
    }

    public void skip(long bytes) throws IOException
    {
        in.skip(bytes);
    }
}
