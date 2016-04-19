/*
 * __NAME__.java
 *
 * Created on __DATE__, __TIME__
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vesalainen.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

/**
 *
 * @author __USER__
 */
public class IPAddressResolver
{
    public static final String A = "IN A";
    public static final String NS = "IN NS";
    public static final String PTR = "IN PTR";
    public static final String MX = "IN MX";
    public static final String LOC = "IN 29";
    
    /**
     * Resolves mail servers for given domain.
     * @param domain Domain to be resolved
     * @return A list of mail server ip-addresses in MX order
     * @throws javax.naming.NamingException
     * @throws org.vesalainen.maild.IllegalMXRecordException
     * @throws java.net.UnknownHostException
     */
    public static List<InetAddress> getMailServers(String domain) throws NamingException, IllegalMXRecordException, UnknownHostException
    {
        //domain = getDomain(domain);
        List<InetAddress> list = new ArrayList<InetAddress>();
        Hashtable<String,String> env = new Hashtable<String,String>();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        env.put("java.naming.provider.url",    "dns:/"+domain);

        DirContext ictx = new InitialDirContext(env);
        Attributes attrs = ictx.getAttributes("", new String[] {MX});
        List<MXRecord> recordList = new ArrayList<MXRecord>();
        NamingEnumeration ne = attrs.getAll();
        while (ne.hasMore())
        {
            Attribute attr = (Attribute)ne.next();
            NamingEnumeration nee = attr.getAll();
            while (nee.hasMore())
            {
                String value = (String)nee.next();
                recordList.add(new MXRecord(value));
            }
        }
        Collections.sort(recordList);
        for (MXRecord record : recordList)
        {
            list.add(record.getHost());
        }
        ictx.close();
        return list;
    }
    
    /**
     * Resolves name servers for domain
     * @param domain Domain to be resolved
     * @return A list if name servers for given domain. Addresses are created by name. Each address might have
     * several ip addresses.
     * @throws javax.naming.NamingException
     * @throws java.net.UnknownHostException
     */
    public static List<InetAddress> getNameServers(String domain) throws NamingException, UnknownHostException
    {
        domain = getDomain(domain);
        List<InetAddress> list = new ArrayList<InetAddress>();
        Hashtable<String,String> env = new Hashtable<String,String>();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        env.put("java.naming.provider.url",    "dns:/"+domain);

        DirContext ictx = new InitialDirContext(env);
        Attributes attrs = ictx.getAttributes("", new String[] {NS});
        NamingEnumeration ne = attrs.getAll();
        while (ne.hasMore())
        {
            Attribute attr = (Attribute)ne.next();
            NamingEnumeration nee = attr.getAll();
            while (nee.hasMore())
            {
                String value = (String)nee.next();
                InetAddress ia = InetAddress.getByName(value);
                list.add(InetAddress.getByAddress(ia.getAddress()));
            }
        }
        ictx.close();
        return list;
    }

    /**
     * Checks if given address has reverse address mapping in given domain's name server
     * TO DO not working -> don't use
     * @param domain Domain to check
     * @param address Address to be checked
     * @return true if reverse mapping exists.
     * @throws javax.naming.NamingException
     * @throws java.net.UnknownHostException
     */
    public static boolean isInside(String domain, InetAddress address) throws NamingException, UnknownHostException
    {
        domain = getDomain(domain);
        List<InetAddress> nameServers = getNameServers(domain);
        String ipAddress = address.getHostAddress();
        String[] split = ipAddress.split("\\.");
        String reverseAddress = split[3]+"."+split[2]+"."+split[1]+"."+split[0];

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (InetAddress ia : nameServers)
        {
            if (!first)
            {
                sb.append(" ");
            }
            first = false;
            sb.append("dns://"+ia.getHostName()+"/in-addr.arpa");
        }
        
        String url = sb.toString();
        Hashtable<String,String> env = new Hashtable<String,String>();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        env.put("java.naming.provider.url",    url);
        env.put("com.sun.jndi.dns.recursion", "false");

        DirContext ictx = new InitialDirContext(env);
        try
        {
            Attributes attrs = ictx.getAttributes(reverseAddress, new String[] {PTR});
            NamingEnumeration ne = attrs.getAll();
            while (ne.hasMore())
            {
                Attribute attr = (Attribute)ne.next();
                NamingEnumeration nee = attr.getAll();
                while (nee.hasMore())
                {
                    String value = (String)nee.next();
                    System.err.println(value);
                }
            }
            return attrs.size() > 0;
        }
        catch (NameNotFoundException ex)
        {
            return false;
        }
    }

    private static String getDomain(String host)
    {
        if (host.indexOf('.') == host.lastIndexOf('.'))
        {
            return host;
        }
        return host.substring(host.indexOf('.')+1);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            //System.err.println(IPAddressResolver.isInside("www.sw-nets.fi", InetAddress.getByName("cs181044197.pp.htv.fi")));
            //MailAddress ma = new MailAddress("<tom.finell@pp.inet.fi>");
            //List<InetAddress> list = IPAddressResolver.getMailServers(ma.getDomain());
            //System.err.println(list);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
}
