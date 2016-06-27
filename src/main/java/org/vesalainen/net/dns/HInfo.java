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
public class HInfo implements RData
{
    private static final long serialVersionUID = 1L;
    private String cpu;
    private String os;
    public HInfo(MessageReader reader) throws IOException
    {
        cpu = reader.readCharacterString();
        os = reader.readCharacterString();
    }

    public void write(MessageWriter writer) throws IOException
    {
        writer.writeCharacterString(cpu);
        writer.writeCharacterString(os);
    }

    @Override
    public boolean equals(Object oth)
    {
        if (oth instanceof HInfo)
        {
            HInfo hinfo = (HInfo) oth;
            return cpu.equals(hinfo.cpu) && os.equals(hinfo.os);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 23 * hash + (this.cpu != null ? this.cpu.hashCode() : 0);
        hash = 23 * hash + (this.os != null ? this.os.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(RData oth)
    {
        if (oth instanceof HInfo)
        {
            HInfo hinfo = (HInfo) oth;
            return cpu.compareTo(hinfo.cpu) + 13 * os.compareTo(hinfo.os);
        }
        return order() - oth.order();
    }

    @Override
    public int order()
    {
        return 4;
    }

}
