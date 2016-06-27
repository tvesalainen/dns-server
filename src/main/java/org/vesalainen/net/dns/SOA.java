/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.IOException;

/**
 *
 * @author tkv
 */
public class SOA implements RData
{
    private static final long serialVersionUID = 1L;
    private DomainName mName;
    private DomainName rName;
    private int serial;
    private int refresh;
    private int retry;
    private int expire;
    private int minimum;

    public SOA(
        String mName,
        String rName,
        int serial,
        int refresh,
        int retry,
        int expire,
        int minimum
            )
    {
        this.mName = new DomainName(mName);
        this.rName = new DomainName(rName);
        this.serial = serial;
        this.refresh = refresh;
        this.retry = retry;
        this.expire = expire;
        this.minimum = minimum;
    }
    public SOA(MessageReader reader) throws IOException
    {
        mName = reader.readDomainName();
        rName = reader.readDomainName();
        serial = reader.read32();
        refresh = reader.read32();
        retry = reader.read32();
        expire = reader.read32();
        minimum = reader.read32();
    }

    public void write(MessageWriter writer) throws IOException
    {
        writer.writeDomainName(mName);
        writer.writeDomainName(rName);
        writer.write32(serial);
        writer.write32(refresh);
        writer.write32(retry);
        writer.write32(expire);
        writer.write32(minimum);
    }

    @Override
    public String toString()
    {
        return "SOA("+getMName().toString()+" "+getRName().toString()+" "+getSerial()+" "+getRefresh()+" "+getRetry()+" "+getExpire()+" "+getMinimum()+")";
    }

    /**
     * The domain-name of the name server that was the
     *   original or primary source of data for this zone.
     *
     * @return the mName
     */
    public DomainName getMName()
    {
        return mName;
    }

    /**
     * A domain-name which specifies the mailbox of the
     *   person responsible for this zone.
     * @return the rName
     */
    public DomainName getRName()
    {
        return rName;
    }

    /**
     * @return the serial
     */
    public int getSerial()
    {
        return serial;
    }

    /**
     * @return the refresh
     */
    public int getRefresh()
    {
        return refresh;
    }

    /**
     * @return the retry
     */
    public int getRetry()
    {
        return retry;
    }

    /**
     * @return the expire
     */
    public int getExpire()
    {
        return expire;
    }

    /**
     * @return the minimum
     */
    public int getMinimum()
    {
        return minimum;
    }

    @Override
    public boolean equals(Object oth)
    {
        if (oth instanceof SOA)
        {
            SOA soa = (SOA) oth;
            return
                mName.equals(soa.mName);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 23 * hash + (this.mName != null ? this.mName.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(RData oth)
    {
        if (oth instanceof SOA)
        {
            SOA soa = (SOA) oth;
            return
                mName.compareTo(soa.mName);
        }
        return order() - oth.order();
    }

    @Override
    public int order()
    {
        return 8;
    }

}
