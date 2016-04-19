/*
 * ConcurrentLogHandler.java
 *
 * Created on 18. marraskuuta 2006, 10:05
 */

package org.vesalainen.logging;

import java.util.logging.*;
import java.util.concurrent.*;
/**
 *
 * @author tkv
 */
public class ConcurrentLogHandler extends Handler
{
    private Handler _handler;
    private ExecutorService _executor;
    /** Creates a new instance of ConcurrentLogHandler */
    public ConcurrentLogHandler(Handler handler, ExecutorService executor)
    {
        _handler = handler;
        _executor = executor;
    }

    /**
     * Publish a <tt>LogRecord</tt>.
     * <p>
     * The logging request was made initially to a <tt>Logger</tt> object,
     * which initialized the <tt>LogRecord</tt> and forwarded it here.
     * <p>
     * The <tt>Handler</tt>  is responsible for formatting the message, when and
     * if necessary.  The formatting should include localization.
     * 
     * @param  record  description of the log event. A null record is
     *                 silently ignored and is not published
     */
    public void publish(LogRecord record)
    {
        ConcurrentLogRecord clr = new ConcurrentLogRecord(record, _handler);
        _executor.submit(clr);
    }

    /**
     * Flush any buffered output.
     */
    public void flush()
    {
        _handler.flush();
    }

    /**
     * Close the <tt>Handler</tt> and free all associated resources.
     * <p>
     * The close method will perform a <tt>flush</tt> and then close the
     * <tt>Handler</tt>.   After close has been called this <tt>Handler</tt>
     * should no longer be used.  Method calls may either be silently
     * ignored or may throw runtime exceptions.
     * 
     * @exception  SecurityException  if a security manager exists and if
     *             the caller does not have <tt>LoggingPermission("control")</tt>.
     */
    public void close() throws SecurityException
    {
        _handler.close();
    }

    /**
     * Check if this <tt>Handler</tt> would actually log a given <tt>LogRecord</tt>.
     * <p>
     * This method checks if the <tt>LogRecord</tt> has an appropriate 
     * <tt>Level</tt> and  whether it satisfies any <tt>Filter</tt>.  It also
     * may make other <tt>Handler</tt> specific checks that might prevent a
     * handler from logging the <tt>LogRecord</tt>. It will return false if 
     * the <tt>LogRecord</tt> is Null.
     * <p>
     * @param record  a <tt>LogRecord</tt>
     * @return true if the <tt>LogRecord</tt> would be logged.
     */
    public boolean isLoggable(LogRecord record)
    {
        return _handler.isLoggable(record);
    }

}
