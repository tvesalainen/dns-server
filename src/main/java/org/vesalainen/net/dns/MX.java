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
public class MX implements RData
{
    private static final long serialVersionUID = 1L;
    private int preference;
    private DomainName exchange;

    public MX(String name, int preference)
    {
        exchange = new DomainName(name);
        this.preference = preference;
    }

    public MX(MessageReader reader) throws IOException
    {
        preference = reader.read16();
        exchange = reader.readDomainName();
    }

    public void write(MessageWriter writer) throws IOException
    {
        writer.write16(getPreference());
        writer.writeDomainName(getExchange());
    }

    @Override
    public String toString()
    {
        return "MX("+getPreference()+" "+getExchange()+")";
    }

    /**
     * @return the preference
     */
    public int getPreference()
    {
        return preference;
    }

    /**
     * @return the exchange
     */
    public DomainName getExchange()
    {
        return exchange;
    }

    @Override
    public boolean equals(Object oth)
    {
        if (oth instanceof MX)
        {
            MX mx = (MX) oth;
            return exchange.equals(mx.exchange) && preference == mx.preference;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 67 * hash + this.preference;
        hash = 67 * hash + (this.exchange != null ? this.exchange.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(RData oth)
    {
        if (oth instanceof MX)
        {
            MX mx = (MX) oth;
            return exchange.compareTo(mx.exchange) + 13 * (preference - mx.preference);
        }
        return order() - oth.order();
    }

    @Override
    public int order()
    {
        return 5;
    }

}
