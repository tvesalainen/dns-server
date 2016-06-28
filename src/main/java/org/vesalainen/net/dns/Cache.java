/*
 * Copyright (C) 2016 tkv
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.net.dns;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.vesalainen.util.ConcurrentHashMapSet;
import org.vesalainen.util.MapSet;

/**
 *
 * @author tkv
 */
public class Cache implements Serializable
{
    private static final long serialVersionUID = 1L;
    private MapSet<DomainName, ResourceRecord> cache = new ConcurrentHashMapSet<>();

    /**
     * Writes new RR's to cache
     * @param answer
     */
    public void update(Answer answer)
    {
        update(answer.getAnswers());
        update(answer.getAuthorities());
        update(answer.getAdditionals());
    }

    private void update(Collection<ResourceRecord> set)
    {
        for (ResourceRecord rr : set)
        {
            Set<ResourceRecord> cacheSet = cache.get(rr.getName());
            if (cacheSet != null && !cacheSet.isEmpty())
            {
                cacheSet.remove(rr);
            }
            cache.add(rr.getName(), rr);
        }
    }

    public void cleanup()
    {
        cache.entrySet().stream().forEach((Entry<DomainName, java.util.Set<ResourceRecord>> e) ->
        {
            e.getValue().removeIf(ResourceRecord::isStale);
        });
    }

    @Override
    public String toString()
    {
        return "Cache{" + cache + '}';
    }
    public List<ResourceRecord> list(Question question)
    {
        return stream(question).collect(Collectors.toList());
    }
    public Stream<ResourceRecord> stream(Question question)
    {
        return stream(question.getQName());
    }
    public Stream<ResourceRecord> stream(DomainName domain)
    {
        return StreamSupport.stream(new RRSpliterator(domain), false);
    }
    private class RRSpliterator extends AbstractSpliterator<ResourceRecord>
    {
        private DomainName name;
        private Iterator<ResourceRecord> iterator;
        public RRSpliterator(DomainName name)
        {
            super(10, 0);
            this.name = name;
        }

        @Override
        public boolean tryAdvance(Consumer<? super ResourceRecord> action)
        {
            while (true)
            {
                if (iterator != null && iterator.hasNext())
                {
                    ResourceRecord rr = iterator.next();
                    RData rData = rr.getRData();
                    if (
                            (rData instanceof A) ||
                            (rData instanceof AAAA)
                            )
                    {
                        action.accept(rr);
                        return true;
                    }
                    if (rData instanceof CName)
                    {
                        CName cname = (CName) rData;
                        name = cname.getName();
                        iterator = null;
                        action.accept(rr);
                        return true;
                    }
                    return false;
                }
                else
                {
                    if (name == null)
                    {
                        return false;
                    }
                    Set<ResourceRecord> set = cache.get(name);
                    name = null;
                    if (set != null)
                    {
                        iterator = set.iterator();
                    }
                    else
                    {
                        return false;
                    }
                }
            }
        }
        
    }
}
