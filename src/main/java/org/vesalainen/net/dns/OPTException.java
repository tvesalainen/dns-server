/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

/**
 *
 * @author tkv
 */
public class OPTException extends Exception
{
    private int udpPayload;
    /**
     * Creates a new instance of <code>OPTException</code> without detail message.
     */
    public OPTException()
    {
    }

    /**
     * Constructs an instance of <code>OPTException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public OPTException(String msg, int udpPayload)
    {
        super(msg);
        this.udpPayload = udpPayload;
    }

    /**
     * @return the udpPayload
     */
    public int getUdpPayload()
    {
        return udpPayload;
    }
}
