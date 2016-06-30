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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.vesalainen.lang.Primitives;
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
        List<ResourceRecord> list = stream(question).collect(Collectors.toList());
        int index = 0;
        for (ResourceRecord rr : list)
        {
            int type = rr.getType();
            if (type == Constants.A || type == Constants.AAAA)
            {
                break;
            }
            index++;
        }
        Collections.sort(list.subList(index, list.size()), (r1, r2)->
        {
            return (int)Primitives.signum(r2.getExpires() - r1.getExpires());
        });
        if (list.size() > 16)
        {
            list = list.subList(0, 16);
        }
        return list;
    }
    public Stream<ResourceRecord> stream(Question question)
    {
        return StreamSupport.stream(new RRSpliterator(question), false);
    }
    private class RRSpliterator extends AbstractSpliterator<ResourceRecord>
    {
        private DomainName name;
        private int qType;
        private Iterator<ResourceRecord> iterator;
        private boolean resolved;
        public RRSpliterator(Question question)
        {
            super(10, 0);
            this.name = question.getQName();
            this.qType = question.getQType();
        }

        @Override
        public boolean tryAdvance(Consumer<? super ResourceRecord> action)
        {
            while (true)
            {
                if (iterator != null && iterator.hasNext())
                {
                    ResourceRecord rr = iterator.next();
                    int type = rr.getType();
                    switch (type)
                    {
                        case Constants.A:
                        case Constants.AAAA:
                            if (type == qType)
                            {
                                action.accept(rr);
                                resolved = true;
                                return true;
                            }
                            break;
                        case Constants.CNAME:
                            if (resolved)
                            {
                                return false;
                            }
                            CName cname = (CName) rr.getRData();
                            name = cname.getName();
                            iterator = null;
                            action.accept(rr);
                            return true;
                        default:
                            return false;
                    }
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
