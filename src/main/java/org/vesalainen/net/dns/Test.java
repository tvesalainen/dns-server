/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.util.Hashtable;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

/**
 *
 * @author tkv
 */
public class Test
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            Hashtable env = new Hashtable();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            env.put("java.naming.provider.url", "dns://localhost/facebook.com");
            DirContext ictx = new InitialDirContext(env);
            Attributes attrs1 = ictx.getAttributes("apps", new String[]{"A"});
            System.err.println(attrs1.toString());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
