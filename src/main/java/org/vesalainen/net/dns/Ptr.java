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
public class Ptr implements RData
{
    private static final long serialVersionUID = 1L;
    private DomainName ptrDName;
    public Ptr(DomainName dn)
    {
        ptrDName = dn;
    }

    public Ptr(MessageReader reader) throws IOException
    {
        ptrDName = reader.readDomainName();
    }

    public void write(MessageWriter writer) throws IOException
    {
        writer.writeDomainName(ptrDName);
    }

    @Override
    public boolean equals(Object oth)
    {
        if (oth instanceof Ptr)
        {
            Ptr ptr = (Ptr) oth;
            return ptrDName.equals(ptr.ptrDName);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 71 * hash + (this.ptrDName != null ? this.ptrDName.hashCode() : 0);
        return hash;
    }

    public int compareTo(RData oth)
    {
        if (oth instanceof Ptr)
        {
            Ptr ptr = (Ptr) oth;
            return ptrDName.compareTo(ptr.ptrDName);
        }
        return -1;
    }

    @Override
    public String toString()
    {
        return "PTR("+ptrDName.toString()+")";
    }

}
