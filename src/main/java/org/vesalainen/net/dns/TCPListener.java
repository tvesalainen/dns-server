/*
 * TCPListener.java
 *
 * Created on November 26, 2007, 10:31 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vesalainen.net.dns;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import javax.net.ServerSocketFactory;

/**
 *
 * @author tkv
 */
public class TCPListener implements Callable<Object>
{
    /** Creates a new instance of TCPListener */
    public TCPListener()
    {
    }

    public Object call() throws Exception
    {
        ServerSocket ss = ServerSocketFactory.getDefault().createServerSocket(53);
        while (true)
        {
            try
            {
                Socket socket = ss.accept();
                TCPProcessor processor = new TCPProcessor(socket);
                DNSServer.executor.submit(processor);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}
