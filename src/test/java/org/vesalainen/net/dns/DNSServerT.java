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

import java.io.File;
import java.net.URL;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class DNSServerT
{
    
    public DNSServerT()
    {
    }

    @Test
    public void test()
    {
        try
        {
            URL url = DNSServerT.class.getResource("/iiris.xml");
            File xml = new File(url.toURI());
            File cache = new File("c:\\temp\\cache.ser");
            DNSServer.main(xml.toString(), cache.toString());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
}
