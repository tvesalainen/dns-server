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
public class CName implements RData
{
    private static final long serialVersionUID = 1L;
    private DomainName name;
    public CName(DomainName name)
    {
        this.name = name;
    }

    public CName(MessageReader reader) throws IOException
    {
        name = reader.readDomainName();
    }

    public void write(MessageWriter writer) throws IOException
    {
        writer.writeDomainName(name);
    }

    @Override
    public boolean equals(Object oth)
    {
        if (oth instanceof CName)
        {
            CName cname = (CName) oth;
            return name.equals(cname.name);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 11 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(RData oth)
    {
        if (oth instanceof CName)
        {
            CName cname = (CName) oth;
            return name.compareTo(cname.name);
        }
        return order() - oth.order();
    }

    @Override
    public int order()
    {
        return 2;
    }

    @Override
    public String toString()
    {
        return "CNAME("+name.toString()+")";
    }

    /**
     * @return the name
     */
    public DomainName getName()
    {
        return name;
    }

}
