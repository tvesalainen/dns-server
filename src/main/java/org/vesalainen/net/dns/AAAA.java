/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 *
 * @author tkv
 */
public class AAAA extends A
{
    private static final long serialVersionUID = 1L;
    public AAAA(Inet6Address address)
    {
        this.address = new InetSocketAddress(address, 53);
    }

    public AAAA(MessageReader reader) throws IOException
    {
        byte[] bb = new byte[16];
        reader.read(bb);
        InetAddress a = InetAddress.getByAddress(bb);
        this.address = new InetSocketAddress(a, 53);
    }

}
