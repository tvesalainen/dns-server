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
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import org.vesalainen.net.IllegalNetMaskException;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class DNSServer extends JavaLogging implements Runnable
{
    protected static ExecutorService executor = Executors.newCachedThreadPool();
    private File cache;
    public DNSServer()
    {
        super(DNSServer.class);
    }
    public DNSServer(File cache) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        super(DNSServer.class);
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
        catch (JAXBException | IllegalNetMaskException | IOException | RCodeException | ClassNotFoundException | InterruptedException ex)
        {
            cmdLine.getLog().log(Level.SEVERE, ex, "%s", ex.getMessage());
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
            catch (IOException ex)
            {
                log(Level.SEVERE, ex, "%s", ex.getMessage());
            }
        }
        executor.shutdownNow();
    }
}
