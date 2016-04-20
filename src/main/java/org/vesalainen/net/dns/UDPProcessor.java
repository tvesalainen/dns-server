/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

/**
 *
 * @author tkv
 */
public class UDPProcessor extends Processor
{
    private static volatile Message previous;
    private DatagramPacket packet;
    public UDPProcessor(DatagramPacket packet)
    {
        sender = (InetSocketAddress)packet.getSocketAddress();
        data = packet.getData();
        this.packet = packet;
    }

    @Override
    public Object call() throws Exception
    {
        try
        {
            Message msg = new Message(data);
            if (previous != null && previous.equals(msg))
            {
                fine("Duplicate: %s <- %s", sender, msg);
                return null;
            }
            previous = msg;
            fine("UDP %s <- %s", sender, msg);
            if (msg.isQuery())
            {
                processQuery(msg);
            }
            else
            {
                processResponse(msg);
            }
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
    public void send(Message msg)
    {
        UDPResponder.send(msg);
    }

}
