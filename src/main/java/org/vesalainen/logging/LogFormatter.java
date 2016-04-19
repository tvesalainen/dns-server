/*
 * LogFormatter.java
 *
 * Created on 2. joulukuuta 2006, 12:15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vesalainen.logging;

import java.io.*;
import java.util.*;
import java.util.logging.*; 
import java.text.*;
/**
 *
 * @author tkv
 */
public class LogFormatter extends java.util.logging.Formatter
{
    private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS z");
    private static String _lineSeparator = System.getProperty("line.separator");
    /** Creates a new instance of LogFormatter */
    public LogFormatter()
    {
    }

    /**
     * Format the given log record and return the formatted string. 
     * <p>
     * The resulting formatted String will normally include a
     * localized and formated version of the LogRecord's message field.
     * The Formatter.formatMessage convenience method can (optionally)
     * be used to localize and format the message field.
     * 
     * 
     * @param record the log record to be formatted.
     * @return the formatted log record
     */
    public String format(LogRecord record)
    {
        CharArrayWriter caw = new CharArrayWriter();
        PrintWriter out = new PrintWriter(caw);
        Date dd = new Date();
        dd.setTime(record.getMillis());
        out.append(DATEFORMAT.format(dd)+" ");
        String cls = record.getSourceClassName();
        if (cls != null)
        {
            out.append(" "+cls);
        }
        String method = record.getSourceMethodName();
        if (method != null)
        {
            out.append("."+method+" ");
        }
        String message = record.getMessage();
        if (message != null)
        {
            Object[] params = record.getParameters();
            if (params != null && params.length > 0)
            {
                out.append(MessageFormat.format(record.getMessage(), record.getParameters()));
            }
            else
            {
                out.append(record.getMessage());
            }
        }
        out.append(_lineSeparator);
        Throwable thr = record.getThrown();
        if (thr != null)
        {
            thr.printStackTrace(out);
        }
        out.flush();
        return caw.toString();
    }
    
}
