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

import java.util.List;
import org.vesalainen.parser.GenClassFactory;
import static org.vesalainen.parser.ParserFeature.SingleThread;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.Terminal;

/**
 *
 * @author tkv
@GenClassname("org.vesalainen.net.dns.RRParserImpl")
@GrammarDef
public abstract class RRParser
{
    @Terminal(expression = "[a-zA-z][a-zA-z0-9\\.]*")
    protected abstract String domain(String value);

    @Terminal(expression = "[0-9]+")
    protected int integer(int value)
    {
        return value;
    }
    
    @Terminal(expression = "[ \t\r\n]+")
    protected abstract void whiteSpace();
    
    public static RRParser newInstance()
    {
        return (RRParser) GenClassFactory.loadGenInstance(RRParser.class);
    }
    @ParseMethod(start = "statements", whiteSpace = {"whiteSpace"})
    public abstract ResourceRecord parse(String text);
}
 */
