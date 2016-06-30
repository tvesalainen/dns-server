/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.util.logging.MinimalFormatter;

/**
 *
 * @author tkv
 */
public class DNSServer implements Runnable
{
    protected static ExecutorService executor = Executors.newCachedThreadPool();
    private File cache;
    public DNSServer()
    {
    }
    public DNSServer(File cache) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        this.cache = cache;
        if (cache.exists())
        {
            Zones.restoreCache(cache);
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String... args)
    {
        CommandLine cmdLine = new CommandLine();
        cmdLine.command(args);
        try
        {
            new Zones(cmdLine.getArgument("zone")); // creates static Zones
            DNSServer server = null;
            if (args.length > 1)
            {
                server = new DNSServer(cmdLine.getArgument("cache"));
            }
            else
            {
                server = new DNSServer();
            }
            Runtime.getRuntime().addShutdownHook(new Thread(server));
            DatagramSocket socket = new DatagramSocket(53);
            executor.submit(new UDPListener(socket));
            executor.submit(new TCPListener());
            executor.submit(new UDPResponder(socket));
            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        if (cache != null)
        {
            try
            {
                Zones.storeCache(cache);
            }
            catch (FileNotFoundException ex)
            {
                ex.printStackTrace();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        executor.shutdownNow();
    }
}
