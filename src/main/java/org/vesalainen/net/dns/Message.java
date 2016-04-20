/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author tkv
 */
public class Message
{
    private int udpPayload = 512;
    /**
     * A 16 bit identifier assigned by the program that
        generates any kind of query.  This identifier is copied
        the corresponding reply and can be used by the requester
        to match up replies to outstanding queries.
     */
    protected int id;
    /**
     * A one bit field that specifies whether this message is a
        query (0), or a response (1).
     */
    protected int qr;
    /**
     * A four bit field that specifies kind of query in this
        message.  This value is set by the originator of a query
        and copied into the response.  The values are:

        0               a standard query (QUERY)

        1               an inverse query (IQUERY)

        2               a server status request (STATUS)

        3-15            reserved for future use

     */
    protected int opCode;
    /**
     * Authoritative Answer - this bit is valid in responses,
        and specifies that the responding name server is an
        authority for the domain name in question section.

        Note that the contents of the answer section may have
        multiple owner names because of aliases.  The AA bit
        corresponds to the name which matches the query name, or
        the first owner name in the answer section
     */
    protected int aa;
    /**
     * TrunCation - specifies that this message was truncated
        due to length greater than that permitted on the
        transmission channel
     */
    protected int tc;
    /**
     * Recursion Desired - this bit may be set in a query and
        is copied into the response.  If RD is set, it directs
        the name server to pursue the query recursively.
        Recursive query support is optional.

     */
    protected int rd;
    /**
     * Recursion Available - this be is set or cleared in a
        response, and denotes whether recursive query support is
        available in the name server.
     */
    protected int ra;
    /**
     * Reserved for future use.  Must be zero in all queries
        and responses.
     */
    protected int z;
    protected int rCode;
    protected Question question;
    protected ResourceRecord[] answers;
    protected ResourceRecord[] authorities;
    protected ResourceRecord[] additionals;
    private SocketAddress recipient;

    public Message(
        long id,
        SocketAddress recipient,
        boolean query,
        int opCode,
        boolean authorative,
        boolean truncated,
        boolean recursionDesired,
        boolean recursionAvailable,
        int z,
        int rCode,
        Question question,
        ResourceRecord[] answers,
        ResourceRecord[] authorities,
        ResourceRecord[] additionals
            )
    {
        this.id = (int) (id & 0xffff);
        this.recipient = recipient;
        if (!query)
        {
            this.qr = 1;
        }
        this.opCode = opCode;
        if (authorative)
        {
            this.aa = 1;
        }
        if (truncated)
        {
            this.tc = 1;
        }
        if (recursionDesired)
        {
            this.rd = 1;
        }
        if (recursionAvailable)
        {
            this.ra = 1;
        }
        this.z = z;
        this.rCode = rCode;
        this.question = question;
        this.answers = answers;
        this.authorities = authorities;
        this.additionals = additionals;
    }

    public Message(byte[] data) throws IOException, RCodeException
    {
        MessageReader reader = new MessageReader(data);
        id = reader.read16();
        int xi = reader.read16();
        qr = xi>>15;
        opCode = (xi>>11) & 0xf;
        aa = (xi>>10) & 0x1;
        tc = (xi>>9) & 0x1;
        rd = (xi>>8) & 0x1;
        ra = (xi>>7) & 0x1;
        z = (xi>>4) & 0x7;
        rCode = xi & 0xf;
        int qdCount = reader.read16();
        if (qdCount > 1)
        {
            throw new RCodeException("Multiple questions", RCodeException.NOT_IMPLEMENTED);
        }
        int anCount = reader.read16();
        int nsCount = reader.read16();
        int arCount = reader.read16();
        question = new Question(reader);
        if (anCount > 0)
        {
            List<ResourceRecord> list = new ArrayList<ResourceRecord>();
            for (int ii=0;ii<anCount;ii++)
            {
                try
                {
                    list.add(new ResourceRecord(reader));
                }
                catch (OPTException ex)
                {
                    udpPayload = ex.getUdpPayload();
                }
            }
            answers = list.toArray(new ResourceRecord[list.size()]);
        }
        if (nsCount > 0)
        {
            List<ResourceRecord> list = new ArrayList<ResourceRecord>();
            for (int ii=0;ii<nsCount;ii++)
            {
                try
                {
                    list.add(new ResourceRecord(reader));
                }
                catch (OPTException ex)
                {
                    udpPayload = ex.getUdpPayload();
                }
            }
            authorities = list.toArray(new ResourceRecord[list.size()]);
        }
        if (arCount > 0)
        {
            List<ResourceRecord> list = new ArrayList<ResourceRecord>();
            for (int ii=0;ii<arCount;ii++)
            {
                try
                {
                    list.add(new ResourceRecord(reader));
                }
                catch (OPTException ex)
                {
                    udpPayload = ex.getUdpPayload();
                }
            }
            additionals = list.toArray(new ResourceRecord[list.size()]);
        }
        //check(data);
    }

    private void check(byte[] data) throws IOException
    {
        byte[] bb = toByteArray();
        for (int ii=0;ii<bb.length;ii++)
        {
            if (bb[ii] != data[ii])
            {
                System.err.println(ii+": "+bb[ii]+" != "+data[ii]);
            }
        }
    }

    public byte[] toByteArray() throws IOException
    {
        byte[] bb = bytes();
        if (bb.length > udpPayload)
        {
            tc = 1;
            bb = bytes();
        }
        return bb;
    }
    private byte[] bytes() throws IOException
    {
        MessageWriter writer = new MessageWriter();
        writer.write16(id);
        int xi = 0;
        xi |= qr<<15;
        xi |= getOpCode()<<11;
        xi |= aa<<10;
        xi |= tc<<9;
        xi |= rd<<8;
        xi |= ra<<7;
        xi |= z<<4;
        xi |= getRCode();
        writer.write16(xi);
        if (question != null)
        {
            writer.write16(1);
        }
        else
        {
            writer.write16(0);
        }
        if (answers != null)
        {
            writer.write16(answers.length);
        }
        else
        {
            writer.write16(0);
        }
        if (authorities != null)
        {
            writer.write16(authorities.length);
        }
        else
        {
            writer.write16(0);
        }
        if (additionals != null)
        {
            writer.write16(additionals.length);
        }
        else
        {
            writer.write16(0);
        }
        if (question != null)
        {
            question.write(writer);
        }
        if (answers != null)
        {
            for (ResourceRecord answer : answers)
            {
                answer.write(writer);
            }
        }
        if (authorities != null)
        {
            for (ResourceRecord authority : authorities)
            {
                authority.write(writer);
            }
        }
        if (additionals != null)
        {
            for (ResourceRecord additional : additionals)
            {
                additional.write(writer);
            }
        }
        return writer.toByteArray();
    }

    @Override
    public boolean equals(Object oth)
    {
        if (oth instanceof Message)
        {
            Message m = (Message) oth;
            return
                this.id == m.id &&
                this.recipient == m.recipient &&
                this.qr == m.qr &&
                this.opCode == m.opCode &&
                this.aa == m.aa &&
                this.tc == m.tc &&
                this.rd == m.rd &&
                this.ra == m.ra &&
                this.z == m.z &&
                this.rCode == m.rCode &&
                this.question.equals(m.question) &&
                Arrays.equals(this.answers, m.answers) &&
                Arrays.equals(this.authorities, m.authorities) &&
                Arrays.equals(this.additionals, m.additionals);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 97 * hash + this.id;
        hash = 97 * hash + this.qr;
        hash = 97 * hash + this.opCode;
        hash = 97 * hash + this.aa;
        hash = 97 * hash + this.tc;
        hash = 97 * hash + this.rd;
        hash = 97 * hash + this.ra;
        hash = 97 * hash + this.z;
        hash = 97 * hash + this.rCode;
        hash = 97 * hash + (this.question != null ? this.question.hashCode() : 0);
        hash = 97 * hash + (this.answers != null ? this.answers.hashCode() : 0);
        hash = 97 * hash + (this.authorities != null ? this.authorities.hashCode() : 0);
        hash = 97 * hash + (this.additionals != null ? this.additionals.hashCode() : 0);
        hash = 97 * hash + (this.recipient != null ? this.recipient.hashCode() : 0);
        return hash;
    }
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Message(ID="+getId()+" "+Constants.qr(qr)+" OPCODE="+Constants.opCode(getOpCode()));
        if (aa != 0)
        {
            sb.append(" Authorative");
        }
        if (tc != 0)
        {
            sb.append(" Truncation");
        }
        if (rd != 0)
        {
            sb.append(" Recursion desired");
        }
        if (ra != 0)
        {
            sb.append(" Recursion available");
        }
        switch (rCode)
        {
            case Constants.RCODE_FORMAT_ERROR:
                sb.append("\nRCODE(Format error)");
                break;
            case Constants.RCODE_NAME_ERROR:
                sb.append("\nRCODE(Name error)");
                break;
            case Constants.RCODE_NOT_IMPLEMENTED:
                sb.append("\nRCODE(Not implemented)");
                break;
            case Constants.RCODE_REFUSED:
                sb.append("\nRCODE(Refused)");
                break;
            case Constants.RCODE_SERVER_FAILURE:
                sb.append("\nRCODE(Server failure)");
                break;
            case Constants.RCODE_NO_ERROR:
                break;
            default:
                sb.append("\nRCODE("+rCode+")");
                break;
        }
        if (question != null)
        {
            sb.append("\nQuestion\n");
            sb.append(question.toString()+"\n");
        }
        if (answers != null && answers.length > 0)
        {
            sb.append("Answers\n");
            for (ResourceRecord r : answers)
            {
                sb.append(r.toString()+"\n");
            }
        }
        if (authorities != null && authorities.length > 0)
        {
            sb.append("Authorities\n");
            for (ResourceRecord r : authorities)
            {
                sb.append(r.toString()+"\n");
            }
        }
        if (additionals != null && additionals.length > 0)
        {
            sb.append("Additionals\n");
            for (ResourceRecord r : additionals)
            {
                sb.append(r.toString()+"\n");
            }
        }
        sb.append(")\n");
        return sb.toString();
    }

    public boolean hasAnswer()
    {
        if (answers != null)
        {
            return answers.length > 0;
        }
        return false;
    }

    public boolean isQuery()
    {
        return qr == 0;
    }

    /**
     * @return the id
     */
    public int getId()
    {
        return id;
    }

    /**
     * @return the opCode
     */
    public int getOpCode()
    {
        return opCode;
    }

    /**
     * @return the rCode
     */
    public int getRCode()
    {
        return rCode;
    }

    /**
     * @param rCode the rCode to set
     */
    public void setRCode(int rCode)
    {
        this.rCode = rCode;
    }

    /**
     * @return the question
     */
    public Question getQuestion()
    {
        return question;
    }

    /**
     * @param question the question to set
     */
    public void setQuestion(Question question)
    {
        this.question = question;
    }

    /**
     * @return the answers
     */
    public Answer getAnswer()
    {
        Answer answer = new Answer();
        if (answers != null)
        {
            for (ResourceRecord rr : answers)
            {
                answer.getAnswers().add(rr);
            }
        }
        if (authorities != null)
        {
            for (ResourceRecord rr : authorities)
            {
                answer.getAuthorities().add(rr);
            }
        }
        if (additionals != null)
        {
            for (ResourceRecord rr : additionals)
            {
                answer.getAdditionals().add(rr);
            }
        }
        return answer;
    }

    /**
     * @return the answers
     */
    public ResourceRecord[] getAnswers()
    {
        return answers;
    }

    /**
     * @param answers the answers to set
     */
    public void setAnswers(ResourceRecord... answers)
    {
        this.answers = answers;
    }

    /**
     * @return the authorities
     */
    public ResourceRecord[] getAuthorities()
    {
        return authorities;
    }

    /**
     * @param authorities the authorities to set
     */
    public void setAuthorities(ResourceRecord... authorities)
    {
        this.authorities = authorities;
    }

    /**
     * @return the additionals
     */
    public ResourceRecord[] getAdditionals()
    {
        return additionals;
    }

    /**
     * @param additionals the additionals to set
     */
    public void setAdditionals(ResourceRecord... additionals)
    {
        this.additionals = additionals;
    }

    public boolean isAuthoritative()
    {
        return aa == 1;
    }
    public boolean isTruncated()
    {
        return tc == 1;
    }
    public boolean recursionDesired()
    {
        return rd == 1;
    }
    public boolean recursionAvailable()
    {
        return ra == 1;
    }

    /**
     * @return the recipient
     */
    public SocketAddress getRecipient()
    {
        return recipient;
    }

    /**
     * @param recipient the recipient to set
     */
    public void setRecipient(SocketAddress recipient)
    {
        this.recipient = recipient;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id)
    {
        this.id = id;
    }

    /**
     * @return the udpPayload
     */
    public int getUdpPayload()
    {
        return udpPayload;
    }
}
