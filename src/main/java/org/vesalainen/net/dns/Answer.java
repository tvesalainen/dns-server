/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author tkv
 */
public class Answer
{
    private Set<ResourceRecord> answers = new ConcurrentSkipListSet<>();
    private Set<ResourceRecord> authorities = new ConcurrentSkipListSet<>();
    private Set<ResourceRecord> additionals = new ConcurrentSkipListSet<>();
    private boolean authorative;

    public void merge(Answer other)
    {
        authorative = authorative && other.authorative;
        answers.addAll(other.answers);
        authorities.addAll(other.authorities);
        additionals.addAll(other.additionals);
    }
    public boolean hasAnswer()
    {
        return !answers.isEmpty();
    }
    public boolean hasFreshAnswers()
    {
        return answers.stream().anyMatch(ResourceRecord::isFresh);
    }
    public void removeStaleAnswers()
    {
        answers.removeIf(ResourceRecord::isStale);
    }
    public ResourceRecord getAnswerFor(Question question)
    {
        if (hasAnswer())
        {
            for (ResourceRecord rr : answers)
            {
                switch (rr.getType())
                {
                    default:
                        if (
                            question.getQType() == rr.getType() &&
                            question.getQName().equals(rr.getName())
                            )
                        {
                            return rr;
                        }
                    break;
                    case Constants.CNAME:
                        if (
                            question.getQName().equals(rr.getName())
                            )
                        {
                            CName cname = (CName) rr.getRData();
                            Question rec = new Question(cname.getName(), question.getQType());
                            return getAnswerFor(rec);
                        }
                    break;
                }
            }
        }
        return null;
    }
    public DomainName getCNameFor(Question question)
    {
        if (hasAnswer())
        {
            for (ResourceRecord rr : answers)
            {
                switch (rr.getType())
                {
                    case Constants.CNAME:
                        if (
                            question.getQName().equals(rr.getName())
                            )
                        {
                            CName cname = (CName) rr.getRData();
                            return cname.getName();
                        }
                    break;
                }
            }
        }
        return null;
    }
    /**
     * @return the answers
     */
    public Set<ResourceRecord> getAnswers()
    {
        return answers;
    }

    /**
     * @param answers the answers to set
     */
    public void setAnswers(Set<ResourceRecord> answers)
    {
        this.answers = answers;
    }

    /**
     * @return the authorities
     */
    public Set<ResourceRecord> getAuthorities()
    {
        return authorities;
    }

    /**
     * @param authorities the authorities to set
     */
    public void setAuthorities(Set<ResourceRecord> authorities)
    {
        this.authorities = authorities;
    }

    /**
     * @return the additionals
     */
    public Set<ResourceRecord> getAdditionals()
    {
        return additionals;
    }

    /**
     * @param additionals the additionals to set
     */
    public void setAdditionals(Set<ResourceRecord> additionals)
    {
        this.additionals = additionals;
    }

    /**
     * @return the authorative
     */
    public boolean isAuthorative()
    {
        return authorative;
    }

    /**
     * @param authorative the authorative to set
     */
    public void setAuthorative(boolean authorative)
    {
        this.authorative = authorative;
    }

    public ResourceRecord[] answers()
    {
        return answers.toArray(new ResourceRecord[answers.size()]);
    }
    public ResourceRecord[] authorities()
    {
        return authorities.toArray(new ResourceRecord[authorities.size()]);
    }
    public ResourceRecord[] additionals()
    {
        return additionals.toArray(new ResourceRecord[additionals.size()]);
    }

    @Override
    public String toString()
    {
        return "Answer{" + "count=" + answers.size() + '}';
    }

}
