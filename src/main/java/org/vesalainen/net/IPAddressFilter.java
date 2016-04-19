/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net;

import java.io.IOException;
import java.net.InetAddress;

/**
 *
 * @author tkv
 */
public interface IPAddressFilter
{
    public boolean includes(InetAddress address) throws IOException;
}
