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
import java.net.InetSocketAddress;

/**
 *
 * @author tkv
 */
public class TCPResolver extends TCPProcessor
{
    private Message response;
    public TCPResolver(InetSocketAddress sender) throws IOException
    {
        super(sender);
    }

    @Override
    protected void processResponse(Message msg)
    {
        response = msg;
        continuous = false;
    }

    public Message getResponse()
    {
        return response;
    }
    
}
