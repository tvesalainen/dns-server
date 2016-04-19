/*
 * IllegalMXRecordException.java
 *
 * Created on November 23, 2007, 12:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vesalainen.net;

/**
 *
 * @author tkv
 */
public class IllegalMXRecordException extends java.lang.Exception
{
    
    /**
     * Creates a new instance of <code>IllegalMXRecordException</code> without detail message.
     */
    public IllegalMXRecordException()
    {
    }
    
    
    /**
     * Constructs an instance of <code>IllegalMXRecordException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public IllegalMXRecordException(String msg)
    {
        super(msg);
    }
}
