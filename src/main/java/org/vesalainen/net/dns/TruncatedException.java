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

/**
 * TruncatedException is thrown when TC bit is set in incoming message
 * @author tkv
 */
public class TruncatedException extends Exception
{
    private int id;
    private Question question;

    TruncatedException(int id, Question question)
    {
        this.id = id;
        this.question = question;
    }
    /**
     * Returns the message id of truncated message.
     * @return 
     */
    public int getMessageId()
    {
        return id;
    }

    public Question getQuestion()
    {
        return question;
    }
    
}
