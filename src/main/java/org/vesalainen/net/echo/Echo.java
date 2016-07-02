/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.echo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class Echo extends JavaLogging implements Runnable
{
    private InetAddress _target;
    private DatagramSocket _socket;
    private long _now;
    private long _timeout;
    public Echo(InetAddress target, long timeout, TimeUnit unit) throws SocketException
    {
        super(Echo.class);
        _target = target;
        _timeout = TimeUnit.MILLISECONDS.convert(timeout, unit);
        _socket = new DatagramSocket(null);
    }

    public void run()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            _now = System.nanoTime();
            dos.writeLong(_now);
            dos.flush();
            DatagramPacket sent = new DatagramPacket(baos.toByteArray(), baos.size(), _target, EchoServer.ECHOPORT);
            //System.err.println(_target+" Sent");
            _socket.send(sent);
            byte[] buffer = new byte[256];
            DatagramPacket received = new DatagramPacket(buffer, buffer.length);
            _socket.setSoTimeout((int) _timeout);
            _socket.receive(received);
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            DataInputStream dis = new DataInputStream(bais);
            long then = dis.readLong();
            long trip = then - _now;
            //System.err.println(_target+" Round Trip "+trip+ " Nanos");
        }
        catch (SocketTimeoutException ex)
        {
            Date now = new Date(_now);
            warning("%s: %s Timeout!", now, _target);
        }
        catch (IOException ex)
        {
            log(Level.SEVERE, ex, "%s", ex.getMessage());
        }
    }

}
