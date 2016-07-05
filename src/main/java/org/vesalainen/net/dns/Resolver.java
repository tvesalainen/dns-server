/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.vesalainen.net.InetAddressParser;
import static org.vesalainen.net.dns.Constants.*;
import org.vesalainen.util.HexDump;
import org.vesalainen.util.ThreadSafeTemporary;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class Resolver extends JavaLogging
{
    private static final int PacketSize = 512;
    private static final InetAddressParser parser = InetAddressParser.newInstance();
    private ThreadSafeTemporary<DatagramSocket> socketStore;
    private InetAddress master;

    public Resolver(InetAddress master) throws SocketException
    {
        super(Resolver.class);
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
        InetAddress address = parser.parse(host);
        if (address != null)
        {
            return Collections.singleton(address);
        }
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
    public static Message resolveMessage(InetSocketAddress sender, byte[] data) throws IOException, RCodeException
    {
        try
        {
            return new Message(data);
        }
        catch (TruncatedException ex)
        {
            try 
            {
                TCPResolver resolver = new TCPResolver(sender);
                Future<Object> future = DNSServer.executor.submit(resolver);
                QueryMessage query = new QueryMessage(512, sender, ex.getQuestion());
                resolver.send(query);
                future.get(5, TimeUnit.SECONDS);
                return resolver.getResponse();
            }
            catch (InterruptedException | ExecutionException ex1) 
            {
                throw new IOException(ex1);
            }
            catch (TimeoutException ex1)
            {
                return null;
            }
        }
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
            QueryMessage query = new QueryMessage(Zones.getMaxUDPPacketSize(), socket.getLocalSocketAddress(), host, A);
            byte[] data = query.toByteArray();
            DatagramPacket packet = new DatagramPacket(data, data.length);
            packet.setAddress(master);
            packet.setPort(53);
            socket.send(packet);

            data = new byte[512];
            packet = new DatagramPacket(data, data.length);
            socket.receive(packet);
            Message msg = resolveMessage((InetSocketAddress) packet.getSocketAddress(), packet.getData());
            Set<InetAddress> set = new HashSet<>();
            if (msg != null)
            {
                for (ResourceRecord rr : msg.getAnswer().getAnswers())
                {
                    RData rData = rr.getRData();
                    if (rData instanceof A)
                    {
                        A a = (A) rData;
                        set.add(a.getAddress());
                    }
                }
            }
            return set;
        }
    }
}
