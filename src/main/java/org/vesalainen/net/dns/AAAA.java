/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;

/**
 *
 * @author tkv
 */
public class AAAA extends A
{
    private static final long serialVersionUID = 1L;
    public AAAA(Inet6Address address)
    {
        this.address = address;
    }

    public AAAA(MessageReader reader) throws IOException
    {
        byte[] bb = new byte[16];
        reader.read(bb);
        this.address = InetAddress.getByAddress(bb);
    }

    @Override
    public int order()
    {
        return 1;
    }

}
