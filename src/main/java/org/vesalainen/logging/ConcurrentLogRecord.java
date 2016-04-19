/*
 * ConcurrentLogRecord.java
 *
 * Created on 18. marraskuuta 2006, 10:10
 */

package org.vesalainen.logging;

import java.util.logging.*;
import java.util.concurrent.*;
/**
 *
 * @author tkv
 */
public class ConcurrentLogRecord implements Callable<Object>
{
    private LogRecord _record;
    private Handler _handler;
    /** Creates a new instance of ConcurrentLogRecord */
    public ConcurrentLogRecord(LogRecord record, Handler handler)
    {
        _record = record;
        _handler = handler;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     * 
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    public Object call() throws Exception
    {
        _handler.publish(_record);
        return null;
    }
    
}
