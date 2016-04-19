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
public class NS implements RData
{
    private static final long serialVersionUID = 1L;
    private DomainName nsDName;

    public NS(String name)
    {
        nsDName = new DomainName(name);
    }

    public NS(MessageReader reader) throws IOException
    {
        nsDName = reader.readDomainName();
    }

    public void write(MessageWriter writer) throws IOException
    {
        writer.writeDomainName(getNsDName());
    }
    @Override
    public String toString()
    {
        return "NS("+getNsDName()+")";
    }

    /**
     * @return the nsDName
     */
    public DomainName getNsDName()
    {
        return nsDName;
    }

    @Override
    public boolean equals(Object oth)
    {
        if (oth instanceof NS)
        {
            NS ns = (NS) oth;
            return nsDName.equals(ns.nsDName);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 67 * hash + (this.nsDName != null ? this.nsDName.hashCode() : 0);
        return hash;
    }

    public int compareTo(RData oth)
    {
        if (oth instanceof NS)
        {
            NS ns = (NS) oth;
            return nsDName.compareTo(ns.nsDName);
        }
        return -1;
    }

}
