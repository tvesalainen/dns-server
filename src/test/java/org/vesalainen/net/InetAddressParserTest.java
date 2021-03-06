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
package org.vesalainen.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class InetAddressParserTest
{
    
    public InetAddressParserTest()
    {
    }

    @Test
    public void test1() throws UnknownHostException
    {
        InetAddressParser parser = InetAddressParser.newInstance();
        InetAddress address = parser.parse("192.168.88.1");
        assertEquals("192.168.88.1", address.getHostAddress());
        assertNull(parser.parse("bbc.com"));
        InetAddress localhost = parser.parse("localhost");
        assertNotNull(localhost);
        assertEquals(InetAddress.getLocalHost(), localhost);
    }
    
}
