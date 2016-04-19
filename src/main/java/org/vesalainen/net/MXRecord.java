/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author tkv
 */
public class MXRecord implements Comparable<MXRecord>
{
    private Integer priority;
    private InetAddress host;
    
    public MXRecord(String record) throws UnknownHostException
    {
        String[] ss = record.split(" ", 2);
        priority = Integer.parseInt(ss[0]);
        host = InetAddress.getByName(ss[1]);
    }

    public int compareTo(MXRecord o)
    {
        return priority.compareTo(o.getPriority());
    }

    public Integer getPriority()
    {
        return priority;
    }

    public InetAddress getHost()
    {
        return host;
    }
}
