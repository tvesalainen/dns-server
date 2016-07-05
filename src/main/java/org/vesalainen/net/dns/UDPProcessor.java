/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import org.vesalainen.util.HexDump;

/**
 *
 * @author tkv
 */
public class UDPProcessor extends Processor
{
    private static volatile Message previous;
    private int length;
    public UDPProcessor(DatagramPacket packet)
    {
        this.sender = (InetSocketAddress)packet.getSocketAddress();
        this.data = packet.getData();
        this.length = packet.getLength();
    }

    @Override
    public Object call() throws Exception
    {
        try
        {
            Message msg = Resolver.resolveMessage(sender, data);
            if (previous != null && previous.equals(msg))
            {
                fine("Duplicate: %s <- %s", sender, msg);
                return null;
            }
            previous = msg;
            fine("UDP %s <- %s len=%d", sender, msg, length);
            if (msg.isQuery())
            {
                processQuery(msg);
            }
            else
            {
                processResponse(msg);
            }
        }
        catch (IOException | InterruptedException | RCodeException ex)
        {
            log(Level.SEVERE, ex, "%s", ex.getMessage());
        }
        return null;
    }

    @Override
    public void send(Message msg)
    {
        UDPResponder.send(msg);
    }

    @Override
    public int getMaxSize()
    {
        return Zones.getMaxUDPPacketSize();
    }

}
