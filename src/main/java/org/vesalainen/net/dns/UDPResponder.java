/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class UDPResponder extends JavaLogging implements Callable<Object>
{
    private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS z");
    private static LinkedBlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();
    private DatagramSocket socket;
    public UDPResponder(DatagramSocket socket)
    {
        super(UDPResponder.class);
        this.socket = socket;
    }

    public static void send(Message message)
    {
        queue.add(message);
    }

    @Override
    public Object call() throws Exception
    {
        fine("UDPResponder started...");
        while (true)
        {
            try
            {
                Message message = queue.take();
                byte[] bb = message.toByteArray();
                if (bb.length > 512)
                {
                    message.setAdditionals();
                    bb = message.toByteArray();
                }
                if (bb.length > 512)
                {
                    message.setAuthorities();
                    bb = message.toByteArray();
                }
                fine("%s -> %s len=%d", message.getRecipient(), message, bb.length);
                DatagramPacket packet = new DatagramPacket(bb, bb.length);
                packet.setSocketAddress(message.getRecipient());
                socket.send(packet);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

}
