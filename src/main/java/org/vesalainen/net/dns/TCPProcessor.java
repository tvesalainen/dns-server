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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.util.HexDump;

/**
 *
 * @author tkv
 */
public class TCPProcessor extends Processor
{
    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public TCPProcessor(Socket socket)
    {
        sender = (InetSocketAddress) socket.getRemoteSocketAddress();
        this.socket = socket;
    }

    @Override
    public Object call() //throws Exception
    {
        try
        {
            fine("TCP from %s", sender);
            in = socket.getInputStream();
            out = socket.getOutputStream();
            while (true)
            {
                int i1 = in.read();
                if (i1 == -1)
                {
                    finest("TCP client %s closed connection", socket);
                    return null;
                }
                int i2 = in.read();
                if (i2 == -1)
                {
                    finest("TCP client %s closed connection", socket);
                    return null;
                }
                int length = (i1<<8)+i2;
                data = new byte[length];
                in.read(data);
                Message msg = new Message(data);
                fine("TCP %s <- %s", sender, msg);
                if (msg.isQuery())
                {
                    processQuery(msg);
                }
                else
                {
                    processResponse(msg);
                }
            }
        }
        catch (Exception  ex)
        {
            log(Level.SEVERE, ex, "%s", ex.getMessage());
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch (IOException ex)
            {
                log(Level.SEVERE, ex, "%s", ex.getMessage());
            }
        }
        return null;
    }

    @Override
    public void send(Message msg) throws IOException
    {
        byte[] dd = msg.toByteArray();
        fine("TCP send %s len=%d", msg, dd.length);
        out.write(dd.length>>8);
        out.write(dd.length & 0xff);
        out.write(dd);
        out.flush();
        try
        {
            fine("CHECK \n%s", HexDump.toHex(dd));
            Message check = new Message(dd);
            fine("CHECK %s", check);
        }
        catch (RCodeException ex)
        {
            Logger.getLogger(TCPProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int getMaxSize()
    {
        return 8192;
    }
}
