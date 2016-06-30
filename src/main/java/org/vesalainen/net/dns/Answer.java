/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tkv
 */
public class Answer
{
    private List<ResourceRecord> answers = new ArrayList<>();
    private List<ResourceRecord> authorities = new ArrayList<>();
    private List<ResourceRecord> additionals = new ArrayList<>();
    private boolean authorative;

    public void merge(Answer other)
    {
        authorative = authorative && other.authorative;
        answers.clear();
        authorities.clear();
        additionals.clear();
        answers.addAll(other.answers);
        authorities.addAll(other.authorities);
        additionals.addAll(other.additionals);
    }
    public static boolean isFresh(List<ResourceRecord> list)
    {
        return list.stream().allMatch(ResourceRecord::isFresh);
    }
    public static boolean isResolved(List<ResourceRecord> list)
    {
        if (!list.isEmpty())
        {
            ResourceRecord rr = list.get(list.size()-1);
            int type = rr.getType();
            return type == Constants.A || type == Constants.AAAA;
        }
        return false;
    }
    public boolean hasAnswer()
    {
        return !answers.isEmpty();
    }
    public boolean isFresh()
    {
        return isFresh(answers);
    }
    public void removeStaleAnswers()
    {
        answers.removeIf(ResourceRecord::isStale);
        authorities.removeIf(ResourceRecord::isStale);
        additionals.removeIf(ResourceRecord::isStale);
    }
    public boolean isAnswerFor(Question question)
    {
        DomainName qName = question.getQName();
        for (ResourceRecord rr : answers)
        {
            int type = rr.getType();
            switch (type)
            {
                case Constants.A:
                case Constants.AAAA:
                    if (type == question.getQType())
                    {
                        return qName.equals(rr.getName());
                    }
                    break;
                case Constants.CNAME:
                    if (qName.equals(rr.getName()))
                    {
                        CName cname = (CName) rr.getRData();
                        qName = cname.getName();
                    }
                    else
                    {
                        return false;
                    }
                    break;
            }
        }
        return false;
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
    public List<ResourceRecord> getAnswers()
    {
        return answers;
    }

    /**
     * @return the authorities
     */
    public List<ResourceRecord> getAuthorities()
    {
        return authorities;
    }

    /**
     * @return the additionals
     */
    public List<ResourceRecord> getAdditionals()
    {
        return additionals;
    }

    public void setAnswers(List<ResourceRecord> answers)
    {
        this.answers = answers;
    }

    public void setAuthorities(List<ResourceRecord> authorities)
    {
        this.authorities = authorities;
    }

    public void setAdditionals(List<ResourceRecord> additionals)
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
