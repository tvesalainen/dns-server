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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author tkv
 */
public class TCPProcessor extends Processor
{
    private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS z");
    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public TCPProcessor(Socket socket)
    {
        sender = (InetSocketAddress) socket.getRemoteSocketAddress();
        this.socket = socket;
    }

    public Object call() //throws Exception
    {
        try
        {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            while (true)
            {
                int i1 = in.read();
                if (i1 == -1) return null;
                int i2 = in.read();
                if (i2 == -1) return null;
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
        catch (RCodeException ex)
        {
            // TODO
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void send(Message msg) throws IOException
    {
        byte[] dd = msg.toByteArray();
        out.write(dd.length>>8);
        out.write(dd.length & 0xff);
        out.write(dd);
        out.flush();
    }
}
