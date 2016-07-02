/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.echo;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class EchoClient implements Runnable
{
    public static final long TIMEOUT = 1;
    public static final TimeUnit TIMEOUTUNIT = TimeUnit.SECONDS;
    public static final long DELAY = 5;
    public static final TimeUnit DELAYUNIT = TimeUnit.SECONDS;

    private static ScheduledExecutorService _scheduler;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            System.err.println("Echo Client Starting...");
            String[] aa = {"localhost"};
            if (args.length > 0)
            {
                aa = args;
            }
            InetAddress[] target = new InetAddress[aa.length];
            int idx = 0;
            for (String host : aa)
            {
                target[idx++] = InetAddress.getByName(host);
            }
            Thread thr = new Thread(new EchoClient());
            Runtime.getRuntime().addShutdownHook(thr);
            _scheduler = Executors.newScheduledThreadPool(target.length);
            for (InetAddress trg : target)
            {
                _scheduler.scheduleWithFixedDelay(new Echo(trg, TIMEOUT, TIMEOUTUNIT), 5, DELAY, DELAYUNIT);
            }
            System.err.println("Echo Client Started "+target.length+" tasks");
        }
        catch (UnknownHostException | SocketException ex)
        {
            JavaLogging.getLogger(EchoClient.class).log(Level.SEVERE, ex, "%s", ex.getMessage());
        }
    }

    @Override
    public void run()
    {
        _scheduler.shutdownNow();
    }
}
