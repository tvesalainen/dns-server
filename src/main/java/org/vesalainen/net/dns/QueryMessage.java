/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author tkv
 */
public class QueryMessage extends Message
{
    private static Map<Integer,QueryMessage> pending = new ConcurrentHashMap<>();
    private static long nextId = 1;

    private SynchronousQueue<Message> queue = new SynchronousQueue<>();

    public QueryMessage(int maxSize, SocketAddress recipient, String domainName, int type)
    {
        super(
        maxSize,
        nextId++,
        recipient,
        true,
        Constants.OPCODE_QUERY,
        false,
        false,
        true,
        false,
        0,
        0,
        new Question(domainName, type),
        null,
        null,
        null
        );
    }
    public QueryMessage(int maxSize, SocketAddress recipient, DomainName domainName, int type)
    {
        super(
        maxSize,
        nextId++,
        recipient,
        true,
        Constants.OPCODE_QUERY,
        false,
        false,
        true,
        false,
        0,
        0,
        new Question(domainName, type),
        null,
        null,
        null
        );
    }
    public QueryMessage(int maxSize, SocketAddress recipient, Question question)
    {
        super(
        maxSize,
        nextId++,
        recipient,
        true,
        Constants.OPCODE_QUERY,
        false,
        false,
        true,
        false,
        0,
        0,
        question,
        null,
        null,
        null
        );
    }

    public Message waitForAnswer(long timeout, TimeUnit unit) throws InterruptedException
    {
        pending.put(id, this);
        try
        {
            Message message = queue.poll(timeout, unit);
            return message;
        }
        finally
        {
            pending.remove(id);
        }
    }

    public static QueryMessage getQuestion(Message message, SocketAddress sender)
    {
        return getQuestion(message.getId(), sender);
    }
    public static QueryMessage getQuestion(int messageId, SocketAddress sender)
    {
        QueryMessage qm = pending.get(messageId);
        if (qm != null && sender.equals(qm.getRecipient()) && messageId == qm.getId())
        {
            return qm;
        }
        return null;
    }

    public static void answer(QueryMessage qm, Message message)
    {
        qm.queue.offer(message);
    }

}
