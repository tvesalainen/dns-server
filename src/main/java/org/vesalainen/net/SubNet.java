/*
 * SubNet.java
 *
 * Created on November 23, 2007, 2:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vesalainen.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.BitSet;

/**
 * Class SubNet handles IP subnets
 * @author tkv
 */
public class SubNet implements IPAddressFilter
{
    
    private BitSet _address = null;
    private BitSet _mask = null;
    /**
     * Creates a new instance of SubNet
     * @param address Base address
     * @param mask Mask in byte array. C-class mask 255.255.255.0 equals new byte[] {255, 255, 255, 0}
     * @throws org.vesalainen.maild.IllegalNetMaskException
     */
    public SubNet(InetAddress address, byte[] mask) throws IllegalNetMaskException
    {
        byte[] bb = address.getAddress();
        if (bb.length != mask.length)
        {
            throw new IllegalNetMaskException("Mask length length differs from address length");
        }
        _address = convert(bb);
        _mask = convert(mask);
        _address.and(_mask);
    }
    /**
     * Creates a new instance of SubNet
     * @param netmask Netmask in format <start>/<mask> Ex. 1.2.3.0/24
     * @throws org.vesalainen.maild.IllegalNetMaskException
     * @throws java.net.UnknownHostException
     */
    public SubNet(String netmask) throws IllegalNetMaskException, UnknownHostException
    {
        String[] ss = netmask.split("/");
        InetAddress address = InetAddress.getByName(ss[0]);
        byte[] bb = address.getAddress();
        _address = convert(bb);
        if (ss.length == 2)
        {
            int mask = Integer.parseInt(ss[1]);
            if (mask < 0 || mask > bb.length*8)
            {
                throw new IllegalNetMaskException(netmask);
            }
            _mask = new BitSet(bb.length*8);
            _mask.set(0, mask);
        }
        else
        {
            _mask = new BitSet(32);
            _mask.set(0, 32);
        }
        _address.and(_mask);
    }
    /**
     * Creates a new instance of SubNet
     * @param address Base address
     * @param mask Mask in address format ex. 255.255.255.0
     * @throws java.net.UnknownHostException
     */
    public SubNet(String address, String mask) throws UnknownHostException
    {
        InetAddress ad = InetAddress.getByName(address);
        byte[] b1 = ad.getAddress();
        InetAddress ma = InetAddress.getByName(mask);
        byte[] b2 = ma.getAddress();
        _address = convert(b1);
        _mask = convert(b2);
        _address.and(_mask);
    }
    
    private static BitSet convert(byte[] bb)
    {
        BitSet bs = new BitSet(bb.length*8);
        int idx = bb.length*8-1;
        for (int ii=bb.length-1;ii>=0;ii--)
        {
            int b = bb[ii];
            for (int jj=0;jj<8;jj++)
            {
                if ((b & 1) == 1)
                {
                    bs.set(idx);
                }
                b = b >> 1;
                idx--;
            }
        }
        return bs;
    }
    /**
     * Textual presentation of netmask (only IPv4)
     * @return Ex. 1.2.3.0/24
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (int ii=0;ii<4;ii++)    // IPV4
        {
            int cc = 0;
            int xx = 128;
            for (int jj=0;jj<8;jj++)
            {
                int idx = 8*ii+jj;
                if (_address.get(idx))
                {
                    cc += xx;
                }
                xx = xx >> 1;
            }
            sb.append(cc);
            if (ii != 3)
            {
                sb.append(".");
            }
        }
        return sb.toString()+"/"+_mask.cardinality();
    }
    
    public boolean includes(InetAddress address)
    {
        byte[] bb = address.getAddress();
        BitSet bs = convert(bb);
        bs.and(_mask);
        return _address.equals(bs);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            SubNet sb = new SubNet("123.1.2.3");
            System.err.println(sb);
            sb = new SubNet("123.1.2.3", "255.255.255.0");
            InetAddress ia = InetAddress.getByName("123.2.2.3");
            System.err.println(sb.includes(ia));
            System.err.println(sb);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
