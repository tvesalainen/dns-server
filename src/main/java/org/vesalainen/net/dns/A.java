/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import org.vesalainen.util.stream.Streams;

/**
 *
 * @author tkv
 */
public class A implements RData
{
    private static final long serialVersionUID = 1L;
    protected InetAddress address;

    protected A()
    {
    }

    public A(Inet4Address address)
    {
        this.address = address;
    }

    public A(MessageReader reader) throws IOException
    {
        byte[] bb = new byte[4];
        reader.read(bb);
        this.address = InetAddress.getByAddress(bb);
    }

    @Override
    public void write(MessageWriter writer) throws IOException
    {
        writer.write(address.getAddress());
    }

    @Override
    public boolean equals(Object oth)
    {
        if (oth instanceof A)
        {
            A a = (A) oth;
            return address.equals(a.address);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 67 * hash + (this.address != null ? this.address.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return "A("+address.getHostAddress()+")";
    }

    /**
     * @return the address
     */
    public InetAddress getAddress()
    {
        return address;
    }

    @Override
    public int compareTo(RData oth)
    {
        if (getClass().equals(oth.getClass()))
        {
            A a = (A) oth;
            return compare(address.getAddress(), a.address.getAddress());
        }
        return order() - oth.order();
    }

    @Override
    public int order()
    {
        return 0;
    }

    protected int compare(byte[] a1, byte[] a2)
    {
        return Streams.compare(Streams.stream(a1), Streams.stream(a2));
    }
}
