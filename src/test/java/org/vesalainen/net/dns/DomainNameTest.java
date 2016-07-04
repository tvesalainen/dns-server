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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class DomainNameTest
{
    
    public DomainNameTest()
    {
    }

    @Test
    public void testCompression()
    {
        try
        {
            MessageWriter mw = new MessageWriter(1024);
            DomainName dn1 = new DomainName("www.kauaspois.fi");
            DomainName dn2 = new DomainName("kauaspois.fi");
            DomainName dn3 = new DomainName("yle.fi");
            mw.writeDomainName(dn1);
            mw.writeDomainName(dn2);
            mw.writeDomainName(dn3);
            byte[] toByteArray = mw.toByteArray();
            
            MessageReader mr = new MessageReader(toByteArray);
            assertEquals(dn1, mr.readDomainName());
            assertEquals(dn2, mr.readDomainName());
            assertEquals(dn3, mr.readDomainName());
        }
        catch (IOException ex)
        {
            Logger.getLogger(DomainNameTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
