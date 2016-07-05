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

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class ResolverT
{
    
    public ResolverT()
    {
    }

    @Test
    public void test()
    {
        try
        {
            Resolver resolver = new Resolver(InetAddress.getByName("192.168.88.236"));
            Set<InetAddress> set = resolver.resolv("b.scorecardresearch.com");
            assertNotNull(set);
            assertFalse(set.isEmpty());
        }
        catch (UnknownHostException ex)
        {
            Logger.getLogger(ResolverT.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (SocketException ex)
        {
            Logger.getLogger(ResolverT.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getLogger(ResolverT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
