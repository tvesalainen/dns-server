/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

/**
 *
 * @author tkv
 */
public class RCodeException extends Exception
{
    /**
     * Format error - The name server was
        unable to interpret the query.
     */
    public static final int FORMAT_ERROR = 1;
    /**
     * Server failure - The name server was
        unable to process this query due to a
        problem with the name server.
     */
    public static final int SERVER_FAILURE = 2;
    /**
     * Name Error - Meaningful only for
        responses from an authoritative name
        server, this code signifies that the
        domain name referenced in the query does
        not exist.
     */
    public static final int NAME_ERROR = 3;
    /**
     * Not Implemented - The name server does
        not support the requested kind of query
     */
    public static final int NOT_IMPLEMENTED = 4;
    /**
     * Refused - The name server refuses to
        perform the specified operation for
        policy reasons.  For example, a name
        server may not wish to provide the
        information to the particular requester,
        or a name server may not wish to perform
        a particular operation (e.g., zone
        transfer) for particular data.
     */
    public static final int REFUSED = 5;
    
    private int rCode;
    /**
     * Creates a new instance of <code>RCodeException</code> without detail message.
     */
    public RCodeException(String msg, int rCode)
    {
        super(msg);
        this.rCode = rCode;
    }

    /**
     * @return the rCode
     */
    public int getRCode()
    {
        return rCode;
    }

}
