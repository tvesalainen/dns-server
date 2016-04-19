/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Callable;

/**
 *
 * @author tkv
 */
public class UDPListener implements Callable<Object>
{
    private DatagramSocket socket;
    public UDPListener(DatagramSocket socket)
    {
        this.socket = socket;
    }

    public Object call() throws Exception
    {
        System.out.println("DNS Server Starting...");
        while (true)
        {
            try
            {
                byte[] buffer = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                UDPProcessor processor = new UDPProcessor(packet);
                DNSServer.executor.submit(processor);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
}
