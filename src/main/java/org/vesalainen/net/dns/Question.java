/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import org.vesalainen.net.dns.Constants.Type;
import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author tkv
 */
public class Question implements Serializable, Comparable<Question>
{
    private static final long serialVersionUID = 1L;
    private DomainName qName;
    private int qType;
    private int qClass;

    public Question(String domainName, int type)
    {
        qName = new DomainName(domainName);
        qType = type;
        qClass = Constants.IN;
    }

    public Question(String domainName, Type type)
    {
        qName = new DomainName(domainName);
        qType = type.ordinal();
        qClass = Constants.IN;
    }

    public Question(DomainName domainName, int type)
    {
        qName = domainName;
        qType = type;
        qClass = Constants.IN;
    }

    public Question(MessageReader reader) throws IOException, RCodeException
    {
        qName = reader.readDomainName();
        qType = reader.read16();
        qClass = reader.read16();
        if (qClass != Constants.IN)
        {
            throw new RCodeException("CLASS "+getQClass()+" not supported", RCodeException.NOT_IMPLEMENTED);
        }
    }

    public Question getCName()
    {
        return new Question(qName, Constants.CNAME);
    }

    public void write(MessageWriter writer) throws IOException
    {
        writer.writeDomainName(getQName());
        writer.write16(getQType());
        writer.write16(getQClass());
    }

    @Override
    public boolean equals(Object oth)
    {
        if (oth instanceof Question)
        {
            Question qq = (Question) oth;
            return getQName().equals(qq.getQName()) && getQType() == qq.getQType() && getQClass() == qq.getQClass();
        }
        return false;
    }

    public boolean equalsANY(Question oth)
    {
        int qType1 = getQType();
        int qType2 = oth.getQType();
        return
            getQName().equals(oth.getQName()) &&
            (
                qType1 == qType2 ||
                qType1 == Constants.ANY ||
                qType2 == Constants.ANY ||
                qType1 == Constants.CNAME ||
                qType2 == Constants.CNAME
            ) &&
            getQClass() == oth.getQClass();
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 31 * hash + (this.getQName() != null ? this.getQName().hashCode() : 0);
        hash = 31 * hash + this.getQType();
        hash = 31 * hash + this.getQClass();
        return hash;
    }

    @Override
    public String toString()
    {
        return "Question("+getQName().toString()+" "+Constants.type(getQType())+" "+Constants.clazz(getQClass())+")";
    }

    /**
     * @return the qName
     */
    public DomainName getQName()
    {
        return qName;
    }

    /**
     * @return the qType
     */
    public int getQType()
    {
        return qType;
    }

    /**
     * @return the qClass
     */
    public int getQClass()
    {
        return qClass;
    }

    public int compareTo(Question q)
    {
        int rc = qName.compareTo(q.qName);
        if (rc == 0)
        {
            if (qType == q.qType)
            {
                return qClass - q.qClass;
            }
            else
            {
                return qType - q.qType;
            }
        }
        return rc;
    }
}
