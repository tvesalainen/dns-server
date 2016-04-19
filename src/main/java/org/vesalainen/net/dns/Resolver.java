/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 *
 * @author tkv
 */
public class Resolver
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            DatagramSocket socket = new DatagramSocket();
            InetSocketAddress master = null;
            String host = null;
            int type = 0;
            if (args.length > 0)
            {
                master = new InetSocketAddress(args[0], 53);
                host = args[1];
                type = Constants.Type.valueOf(args[2]).ordinal();
            }
            else
            {
                //master = new InetSocketAddress("217.30.180.230", 53);
                master = new InetSocketAddress("192.168.0.167", 53);
                host = "www.sw-nets.fi";
                type = Constants.A;
            }
            QueryMessage query = new QueryMessage(socket.getLocalSocketAddress(), host, type);
            byte[] data = query.toByteArray();
            DatagramPacket packet = new DatagramPacket(data, data.length);
            packet.setSocketAddress(master);
            socket.send(packet);

            data = new byte[512];
            packet = new DatagramPacket(data, data.length);
            socket.receive(packet);
            Message message = new Message(packet.getData());
            System.err.println(packet.getSocketAddress());
            System.err.println(message);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
