/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author tkv
 */
public class DomainName implements Comparable<DomainName>, Serializable
{
    private static final long serialVersionUID = 1L;
    private List<String> list = new ArrayList<>();
    private String name;

    public DomainName(String dn)
    {
        String[] ss = dn.split("\\.");
        this.list.addAll(Arrays.asList(ss));
        this.name = dn.toLowerCase();
    }

    public DomainName(List<String> list)
    {
        this.list.addAll(list);
        this.name = makeString();
    }

    public DomainName(List<String> list, DomainName dn)
    {
        this.list.addAll(list);
        this.list.addAll(dn.list);
        this.name = makeString();
    }

    public DomainName(DomainName sub, DomainName dn)
    {
        this.list.addAll(sub.list);
        this.list.addAll(dn.list);
        this.name = makeString();
    }

    public DomainName(String sub, DomainName dn)
    {
        this.list.add(sub);
        this.list.addAll(dn.list);
        this.name = makeString();
    }

    public int getLevel()
    {
        return list.size();
    }

    private String makeString()
    {
        StringBuilder sb = new StringBuilder();
        for (String label : list)
        {
            if (sb.length() > 0)
            {
                sb.append(".");
            }
            sb.append(label);
        }
        return sb.toString().toLowerCase();
    }

    public boolean isRoot()
    {
        return list.isEmpty();
    }
    
    public boolean isSubDomainOf(DomainName dn)
    {
        return name.endsWith(dn.name);
    }
    /**
     * Return the host or subdomain. For www.sw-nets.fi it returns www
     * @return
     */
    public String getSubDomain()
    {
        if (list.size() > 0)
        {
            return list.get(0);
        }
        return "";
    }

    /**
     * Return the domain part. For www.sw-nets.fi it returns sw-nets.fi
     * @return
     */
    public DomainName getDomain()
    {
        if (list.size() > 1)
        {
            return new DomainName(list.subList(1, list.size()));
        }
        return null;
    }
    /**
     * Return the domain part. For www.sw-nets.fi it returns sw-nets.fi
     * @return
     */
    public DomainName getBaseDomain()
    {
        if (list.size() > 1)
        {
            return new DomainName(list.subList(1, list.size()));
        }
        return null;
    }
    @Override
    public boolean equals(Object oth)
    {
        if (oth instanceof DomainName)
        {
            DomainName dn = (DomainName) oth;
            return name.equalsIgnoreCase(dn.name);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public int length()
    {
        int len = 0;
        for (String label : list)
        {
            len += label.length()+1;
        }
        return len+1;
    }

    public int compareTo(DomainName dn)
    {
        return name.compareTo(dn.name);
    }
}
