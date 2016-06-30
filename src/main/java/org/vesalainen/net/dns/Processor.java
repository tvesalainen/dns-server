/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.vesalainen.util.HashMapSet;
import org.vesalainen.util.MapSet;
import org.vesalainen.util.logging.JavaLogging;



/**
 *
 * @author tkv
 */
public abstract class Processor extends JavaLogging implements Callable<Object>
{
    protected InetSocketAddress sender;
    protected byte[] data;
    private int searchLevel;
    public Processor()
    {
        super(Processor.class);
    }

    public abstract void send(Message msg) throws IOException;
    protected void processQuery(Message msg) throws SocketException, IOException, InterruptedException
    {
        try
        {
            doProcessQuery(msg);
        }
        catch (RCodeException ex)
        {
            sendNegative(msg, ex.getRCode());
        }
    }
    
    private void doProcessQuery(Message msg) throws SocketException, IOException, InterruptedException, RCodeException
    {

        switch (msg.getOpCode())
        {
            case Constants.OPCODE_QUERY:
                break;
            default:    
                throw new RCodeException(msg.getOpCode()+" OPCODE not implemented", RCodeException.NOT_IMPLEMENTED);
        }
        Question question = msg.getQuestion();
        switch (question.getQType())
        {
            case Constants.AXFR:
                ResponseMessage zoneTransfer = Zones.getZoneTransfer(question.getQName());
                if (zoneTransfer != null)
                {
                    zoneTransfer.setId(msg.getId());
                    send(zoneTransfer);
                }
                else
                {
                    throw new RCodeException(question.getQName()+" zone not known", RCodeException.REFUSED);
                }
                break;
            case Constants.A:
            case Constants.MX:
            case Constants.NS:
            case Constants.AAAA:
            case Constants.ANY:
            break;
            case Constants.PTR:
            {
                String address = question.getQName().toString();
                String[] ss = address.split("\\.");
                InetAddress ia = InetAddress.getByName(ss[3]+"."+ss[2]+"."+ss[1]+"."+ss[0]);
                if (ia.isAnyLocalAddress() || ia.isLinkLocalAddress() || ia.isLoopbackAddress() || ia.isSiteLocalAddress())
                {
                    throw new RCodeException(question.getQName()+" Local Address", RCodeException.REFUSED);
                }
            }
            break;
            default:
                throw new RCodeException(question.getQType()+" QTYPE not implemented", RCodeException.NOT_IMPLEMENTED);
        }
        Answer answer = search(question, msg.recursionDesired());
        if (answer.hasAnswer() || !answer.isAuthorative())
        {
            sendAnswer(msg, answer);
        }
        else
        {
            sendNegative(msg, Constants.RCODE_NAME_ERROR);
        }
    }

    protected Answer search(Question question, boolean recursionDesired) throws IOException, InterruptedException
    {
        fine("search(%s)",question);
        Answer answer = new Answer();
        if (searchLevel++ > 10)
        {
            return answer;
        }
        if (Zones.resolveAuthorative(question, sender, answer))
        {
            fine("Zone hit %s",question);
            answer.setAuthorative(true);
        }
        else
        {
            if (recursionDesired)
            {
                Zones.getFromCache(question, answer);
                if (answer.isAnswerFor(question))
                {
                    if (answer.isFresh())
                    {
                        fine("%s has fresh answers %s",question, answer);
                        answer.setAuthorative(false);
                        return answer;
                    }
                    else
                    {
                        Future<Answer> future = DNSServer.executor.submit(new Resolver(question, answer));
                        try
                        {
                            future.get(1, TimeUnit.SECONDS);
                        }
                        catch (TimeoutException ex)
                        {
                            fine("%s timeout stale %s used", question, answer);
                        }
                        catch (ExecutionException ex)
                        {
                            throw new IllegalArgumentException(ex);
                        }
                    }
                }
                else
                {
                    Future<Answer> future = DNSServer.executor.submit(new Resolver(question, answer));
                    try
                    {
                        future.get();
                    }
                    catch (ExecutionException ex)
                    {
                        throw new IllegalArgumentException(ex);
                    }
                }
            }
        }
        return answer;
    }
    protected void sendAnswer(Message msg, Answer answer) throws IOException, InterruptedException
    {
        ResponseMessage response = new ResponseMessage(
                msg.getId(),
                sender,
                msg.getQuestion(),
                answer
                );
        send(response);
    }

    protected void sendNegative(Message msg, int rCode) throws IOException
    {
        ResponseMessage response = new ResponseMessage(
                msg.getId(),
                sender,
                rCode,
                msg.getQuestion()
                );
        send(response);
    }

    protected void fillAdditionals(Question question, Answer answer) throws IOException, InterruptedException
    {
        Iterator<ResourceRecord> it = answer.getAnswers().iterator();
        while (it.hasNext())
        {
            ResourceRecord rr = it.next();
            switch (rr.getType())
            {
                case Constants.MX:
                {
                    MX mx = (MX) rr.getRData();
                    Answer recur = search(new Question(mx.getExchange(), Constants.A), true);
                    answer.getAdditionals().addAll(recur.getAnswers());
                }
                break;
                case Constants.NS:
                {
                    NS ns = (NS) rr.getRData();
                    Answer recur = search(new Question(ns.getNsDName(), Constants.A), true);
                    answer.getAdditionals().addAll(recur.getAnswers());
                }
                break;
                case Constants.CNAME:
                {
                    CName cname = (CName) rr.getRData();
                    Answer recur = search(new Question(cname.getName(), question.getQType()), true);
                    answer.getAdditionals().addAll(recur.getAnswers());
                }
                break;
            }
        }
    }
    protected void processResponse(Message msg)
    {
        Zones.add(msg.getQuestion(), msg.getAnswer());
        QueryMessage question = QueryMessage.getQuestion(msg, sender);
        if (question != null)
        {
            QueryMessage.answer(question, msg);
        }
        else
        {
            fine("DROPPED %s", msg);
        }
    }

    private class Resolver implements Callable<Answer>
    {
        private final Question question;
        private final Answer answer;

        public Resolver(Question question, Answer answer)
        {
            this.question = question;
            this.answer = answer;
        }

        @Override
        public Answer call() throws Exception
        {
            Set<InetAddress> nsList = Zones.getNameServerFor(question.getQName());
            nsList.addAll(Zones.getNameServers());
            int timeOut = 1;
            for (InetAddress nameServer : nsList)
            {
                QueryMessage qm = new QueryMessage(new InetSocketAddress(nameServer, 53), question);
                UDPResponder.send(qm);
                Message response = qm.waitForAnswer(timeOut++, TimeUnit.SECONDS);
                if (response != null)
                {
                    answer.merge(response.getAnswer());
                    if (answer.isAnswerFor(question))
                    {
                        fine("%s succeed after merge %s", question, response.getAnswer());
                        break;
                    }
                    else
                    {
                        DomainName cname = answer.getCNameFor(question);
                        if (cname != null)
                        {
                            Answer recu = search(new Question(cname, question.getQType()), true);
                            answer.merge(recu);
                            if (answer.isAnswerFor(question))
                            {
                                fine("%s succeed after recursion merge %s", question, recu);
                                break;
                            }
                        }
                    }
                }
            }
            return answer;
        }
        
    }
}
