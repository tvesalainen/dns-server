/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.vesalainen.net.dns.Constants.*;
import org.vesalainen.util.ThreadSafeTemporary;

/**
 *
 * @author tkv
 */
public class Resolver
{
    private static final int PacketSize = 512;
    private ThreadSafeTemporary<DatagramSocket> socketStore;
    private InetAddress master;

    public Resolver(InetAddress master) throws SocketException
    {
        this.socketStore = new ThreadSafeTemporary<>(Resolver::createSocket);
        this.master = master;
    }

    public Set<InetAddress> resolv(String host) throws IOException
    {
        return resolv(host, 2, 1, TimeUnit.SECONDS);
    }
    public Set<InetAddress> resolv(String host, int retries, long timeout, TimeUnit unit) throws IOException
    {
        return resolv(host, A, retries, timeout, unit);
    }
    public Set<InetAddress> resolv(String host, int type, int retries, long timeout, TimeUnit unit) throws IOException
    {
        Res res = new Res(host, type);
        for (int ii=0;ii<retries;ii++)
        {
            Future<Set<InetAddress>> future = DNSServer.executor.submit(res);
            try
            {
                return future.get(timeout, unit);
            }
            catch (InterruptedException | ExecutionException ex)
            {
                throw new IOException(ex);
            }
            catch (TimeoutException ex)
            {
            }
        }
        return null;
    }
    private static DatagramSocket createSocket()
    {
        try
        {
            return new DatagramSocket();
        }
        catch (SocketException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    private class Res implements Callable<Set<InetAddress>>
    {
        private String host;
        private int type;

        public Res(String host, int type)
        {
            this.host = host;
            this.type = type;
        }
        
        @Override
        public Set<InetAddress> call() throws Exception
        {
            DatagramSocket socket = socketStore.get();
            QueryMessage query = new QueryMessage(socket.getLocalSocketAddress(), host, A);
            byte[] data = query.toByteArray();
            DatagramPacket packet = new DatagramPacket(data, data.length);
            packet.setAddress(master);
            packet.setPort(53);
            socket.send(packet);

            data = new byte[512];
            packet = new DatagramPacket(data, data.length);
            socket.receive(packet);
            Message msg = new Message(packet.getData());
            Set<InetAddress> set = new HashSet<>();
            for (ResourceRecord rr : msg.getAnswer().getAnswers())
            {
                RData rData = rr.getRData();
                if (rData instanceof A)
                {
                    A a = (A) rData;
                    set.add(a.getAddress());
                }
            }
            return set;
        }
    }
}
