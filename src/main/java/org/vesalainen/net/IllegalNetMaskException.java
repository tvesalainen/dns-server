/*
 * IllegalNetMaskException.java
 *
 * Created on November 23, 2007, 2:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vesalainen.net;

/**
 *
 * @author tkv
 */
public class IllegalNetMaskException extends java.lang.Exception
{
    
    /**
     * Creates a new instance of <code>IllegalNetMaskException</code> without detail message.
     */
    public IllegalNetMaskException()
    {
    }
    
    
    /**
     * Constructs an instance of <code>IllegalNetMaskException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public IllegalNetMaskException(String msg)
    {
        super(msg);
    }
}
