/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.echo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 *
 * @author tkv
 */
public class EchoServer
{

    public static final int ECHOPORT = 888;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            System.err.println("Echo Server Starting...");
            DatagramSocket socket = new DatagramSocket(ECHOPORT);
            while (true)
            {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                socket.send(packet);
                System.err.println("Sent reply to "+packet.getSocketAddress());
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
