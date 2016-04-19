/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net;

import java.io.IOException;
import java.net.InetAddress;

/**
 * This class handles sub domain filtering
 * @author tkv
 */
public class SubDomain implements IPAddressFilter
{
    private String _domain;
    /**
     * Creates a new SubDomain
     * @param domain End of domain name. Ex. sun.com
     */
    public SubDomain(String domain)
    {
        _domain = domain;
    }
    /**
     * Filters ip address by using domain name
     * @param address Address to be filtered
     * @return true if address.getHostName ends with domain name
     * @throws java.io.IOException
     */
    public boolean includes(InetAddress address) throws IOException
    {
        return address.getHostName().endsWith(_domain);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            SubDomain netace = new SubDomain("sw-nets.fi");
            System.err.println(netace.includes(InetAddress.getByName("192.168.0.167")));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
