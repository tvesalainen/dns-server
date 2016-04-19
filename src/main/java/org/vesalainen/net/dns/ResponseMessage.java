/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.net.SocketAddress;

/**
 *
 * @author tkv
 */
public class ResponseMessage extends Message
{
    public ResponseMessage(
            int id,
            SocketAddress recipient,
            boolean authorative,
            Question question,
            ResourceRecord[] answers,
            ResourceRecord[] authorities,
            ResourceRecord[] additionals
            )
    {
        super(
        id,
        recipient,
        false,
        Constants.OPCODE_QUERY,
        authorative,
        false,
        true,
        true,
        0,
        0,
        question,
        answers,
        authorities,
        additionals
        );
    }
    public ResponseMessage(
            int id,
            SocketAddress recipient,
            Question question,
            Answer answer
            )
    {
        super(
        id,
        recipient,
        false,
        Constants.OPCODE_QUERY,
        answer.isAuthorative(),
        false,
        true,
        true,
        0,
        0,
        question,
        answer.answers(),
        answer.authorities(),
        answer.additionals()
        );
    }
    public ResponseMessage(
            int id,
            SocketAddress recipient,
            int rCode,
            Question question
            )
    {
        super(
        id,
        recipient,
        false,
        Constants.OPCODE_QUERY,
        false,
        false,
        true,
        true,
        0,
        rCode,
        question,
        null,
        null,
        null
        );
    }
}
