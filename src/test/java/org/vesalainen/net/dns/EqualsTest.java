/*
 * Copyright (C) 2016 tkv
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.net.dns;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.net.dns.Constants.IN;

/**
 *
 * @author tkv
 */
public class EqualsTest
{
    
    public EqualsTest()
    {
    }

    @Test
    public void testRR()
    {
        try
        {
            InetAddress ia1 = InetAddress.getByName("23.56.192.45");
            ResourceRecord rr1 = new ResourceRecord(new DomainName("e19.e12.akamaiedge.net"), IN, 19, new A((Inet4Address) ia1));
            InetAddress ia2 = InetAddress.getByName("23.56.192.45");
            ResourceRecord rr2 = new ResourceRecord(new DomainName("e19.e12.akamaiedge.net"), IN, 19, new A((Inet4Address) ia2));
            assertTrue(rr1.equals(rr2));
            ConcurrentSkipListSet<ResourceRecord> set = new ConcurrentSkipListSet();
            set.add(rr1);
            set.add(rr2);
            assertEquals(1, set.size());
        }
        catch (UnknownHostException ex)
        {
            ex.printStackTrace();
        }
    }
    @Test
    public void testA()
    {
        try
        {
            A a1 = new A((Inet4Address) InetAddress.getLocalHost());
            A a2 = new A((Inet4Address) InetAddress.getLocalHost());
            assertTrue(a1.equals(a2));
        }
        catch (UnknownHostException ex)
        {
            ex.printStackTrace();
        }
    }
    
}
