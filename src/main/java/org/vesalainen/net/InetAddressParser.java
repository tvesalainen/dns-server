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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.regex.SyntaxErrorException;

/**
 * Parses internet address in it's text form
 * @author tkv
 */
@GenClassname("org.vesalainen.net.InetAddressParserImpl")
@GrammarDef
public abstract class InetAddressParser
{
    /**
     * Returns new instance
     * @return 
     */
    public static InetAddressParser newInstance()
    {
        return (InetAddressParser) GenClassFactory.getGenInstance(InetAddressParser.class);
    }
    /**
     * Parses internet address in it's text form. Returns null if address is not 
     * correct.
     * <p>Currently only ipv4!
     * @param text
     * @return 
     */
    public InetAddress parse(CharSequence text)
    {
        try
        {
            return parseIt(text);
        }
        catch (SyntaxErrorException ex)
        {
            return null;
        }
    }
    @ParseMethod(start="address")
    protected abstract InetAddress parseIt(CharSequence text);
    
    @Rules({
    @Rule("inet4Address"),
    @Rule("localhost")
    })
    protected InetAddress address(InetAddress Inet4Address)
    {
        return Inet4Address;
    }
    @Rule("'localhost'")
    protected InetAddress localhost()
    {
        try
        {
            return InetAddress.getLocalHost();
        }
        catch (UnknownHostException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    @Rule("d10 '\\.' d10 '\\.' d10 '\\.' d10")
    protected InetAddress inet4Address(int b1, int b2, int b3, int b4)
    {
        try
        {
            return Inet4Address.getByAddress(new byte[] {(byte)b1, (byte)b2, (byte)b3, (byte)b4});
        }
        catch (UnknownHostException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    @Terminal(expression="[0-9]{1,3}")
    protected abstract int d10(int b);
}
