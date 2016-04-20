/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class ZoneTransfer extends JavaLogging
{
    public ZoneTransfer()
    {
        super(ZoneTransfer.class);
    }

    public static Message getZone(String domain, InetSocketAddress ns) throws IOException, RCodeException
    {
        return getZone(new DomainName(domain), ns);
    }
    public static Message getZone(DomainName domain, InetSocketAddress ns) throws IOException, RCodeException
    {
        Socket socket = new Socket();
        socket.connect(ns);
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        QueryMessage query = new QueryMessage(null, domain, Constants.AXFR);
        byte[] data = query.toByteArray();
        out.write(data.length>>8);
        out.write(data.length & 0xff);
        out.write(data);
        int i1 = in.read();
        int i2 = in.read();
        int length = (i1<<8)+i2;
        data = new byte[length];
        in.read(data);
        Message msg = new Message(data);
        socket.close();
        return msg;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            Message msg = ZoneTransfer.getZone("it-apu.fi", new InetSocketAddress("192.168.0.167", 53));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
