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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.net.dns.Constants.A;
import org.vesalainen.net.dns.Constants.Type;
import org.vesalainen.util.ConcurrentHashMapSet;

/**
 *
 * @author tkv
 */
public class CacheTest
{
    
    public CacheTest()
    {
    }

    @Test
    public void testSerialize0()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            ConcurrentHashMapSet<String,String> chms = new ConcurrentHashMapSet<>();
            chms.add("foo", "bar");
            oos.writeObject(chms);
            
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            chms = (ConcurrentHashMapSet<String, String>) ois.readObject();
            assertTrue(chms.contains("foo", "bar"));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }
    
    @Test
    public void testSerialize()
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            ConcurrentHashMapSet<Question,ResourceRecord> chms = new ConcurrentHashMapSet<>();
            Question q = new Question("iiris", Type.A);
            ResourceRecord rr = new ResourceRecord(InetAddress.getLocalHost(), A, 1, new A((Inet4Address) Inet4Address.getLocalHost()));
            chms.add(q, rr);
            oos.writeObject(chms);
            
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            chms = (ConcurrentHashMapSet<Question,ResourceRecord>) ois.readObject();
            assertEquals(1, chms.size());
            assertTrue(chms.contains(q, rr));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }
    
}
