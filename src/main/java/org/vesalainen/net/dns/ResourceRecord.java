/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
import org.vesalainen.lang.Primitives;

/**
 *
 * @author tkv
 */
public class ResourceRecord implements Serializable, Comparable<ResourceRecord>
{
    private static final long serialVersionUID = 1L;
    private Question question;
    private int ttl;
    private RData rData;
    private long expires = Long.MAX_VALUE;

    public ResourceRecord(DomainName domainName, int type, int ttl, RData rData)
    {
        question = new Question(domainName, type);
        this.ttl =ttl;
        this.rData = rData;
    }

    public ResourceRecord(InetAddress address, int type, int ttl, RData rData)
    {
        byte[] aa = address.getAddress();
        DomainName ptrDName = new DomainName(String.format("%d.%d.%d.%d.in-addr.arpa", aa[3], aa[2], aa[1], aa[0]));
        question = new Question(ptrDName, type);
        this.ttl =ttl;
        this.rData = rData;
    }

    public ResourceRecord(MessageReader reader) throws IOException, RCodeException, OPTException
    {
        DomainName name = reader.readDomainName();
        int type = reader.read16();
        int clazz = reader.read16();
        if (type == Constants.OPT)
        {
            throw new OPTException("opt", clazz);
        }
        if (clazz != Constants.IN)
        {
            throw new RCodeException("CLASS "+clazz+" not supported", RCodeException.NOT_IMPLEMENTED);
        }
        question = new Question(name, type);
        ttl = reader.read32();
        int rdLength = reader.read16();
        reader.mark();

        switch (type)
        {
            case Constants.A:  // a host address
                rData = new A(reader);
                break;
            case Constants.NS: // an authoritative name server
                rData = new NS(reader);
                break;
            case Constants.CNAME: // the canonical name for an alias
                rData = new CName(reader);
                break;
            case Constants.SOA: // marks the start of a zone of authority
                rData = new SOA(reader);
                break;
            case Constants.PTR: // a domain name pointer
                rData = new Ptr(reader);
                break;
            case Constants.HINFO: // host information
                rData = new HInfo(reader);
                break;
            case Constants.MX: // mail exchange
                rData = new MX(reader);
                break;
            case Constants.AAAA: // mail exchange
                rData = new AAAA(reader);
                break;
            default:
                throw new RCodeException("TYPE "+getType()+" not supported", RCodeException.NOT_IMPLEMENTED);
        }
        reader.reset();
        reader.skip(rdLength);
    }

    public void write(MessageWriter writer) throws IOException
    {
        writer.writeDomainName(getName());
        writer.write16(getType());
        writer.write16(getClazz());
        writer.write32(getTtl());
        writer.mark();
        writer.write16(0);
        int start = writer.size();
        getRData().write(writer);
        int end = writer.size();
        int len = end-start;
        writer.reset();
        writer.write16(len);
        writer.skip(len);
    }

    public ResourceRecord getPTR()
    {
        if (rData instanceof A)
        {
            A a = (A) rData;
            String aa = a.getAddress().getAddress().getHostAddress();
            String[] ss = aa.split("\\.");
            StringBuilder sb = new StringBuilder();
            for (int ii=ss.length-1;ii>=0;ii--)
            {
                sb.append(ss[ii]+".");
            }
            sb.append("in-addr.arpa");
            DomainName dn = new DomainName(sb.toString());
            Ptr ptr = new Ptr(getName());
            return new ResourceRecord(dn, Constants.PTR, ttl, ptr);
        }
        return null;
    }

    public boolean expired(long current)
    {
        return current > expires;
    }

    @Override
    public String toString()
    {
        return "RR("+getName().toString()+" "+Constants.type(getType())+" "+Constants.clazz(getClazz())+" ttl="+getTtl()+" "+getRData().toString()+")";
    }

    public Question getQuestion()
    {
        return question;
    }
    /**
     * @return the name
     */
    public DomainName getName()
    {
        return question.getQName();
    }

    /**
     * @return the type
     */
    public int getType()
    {
        return question.getQType();
    }

    /**
     * @return the clazz
     */
    public int getClazz()
    {
        return question.getQClass();
    }

    /**
     * @return the ttl
     */
    public int getTtl()
    {
        return ttl;
    }

    /**
     * @return the rData
     */
    public RData getRData()
    {
        return rData;
    }

    public void setExpires()
    {
        expires = Cache.getClock().millis()+ttl*1000;
    }
    /**
     * @return the expires
     */
    public long getExpires()
    {
        return expires;
    }

    @Override
    public boolean equals(Object oth)
    {
        if (oth instanceof ResourceRecord)
        {
            ResourceRecord rr = (ResourceRecord) oth;
            return
                question.equals(rr.question) &&
                rData.equals(rr.rData);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 97 * hash + (this.question != null ? this.question.hashCode() : 0);
        hash = 97 * hash + (this.rData != null ? this.rData.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(ResourceRecord rr)
    {
        return (int) Primitives.signum(expires - rr.expires);
    }

    public static void main(String[] args)
    {
        try
        {
            /*
            ResourceRecord rr1 = new ResourceRecord("aa.bb", "A", 123, new A((Inet4Address)Inet4Address.getByName("192.168.0.1")));
            ResourceRecord rr2 = new ResourceRecord("aa.bb", "A", 12, new A((Inet4Address)Inet4Address.getByName("192.168.0.1")));
            Answer answer = new Answer();
            answer.getAnswers().add(rr1);
            answer.getAnswers().add(rr2);
            System.err.println(answer.getAnswers().size());
             */
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
