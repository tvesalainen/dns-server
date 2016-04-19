/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.dns;

import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author tkv
 */
public interface RData extends Comparable<RData>, Serializable
{
    void write(MessageWriter writer) throws IOException;
}
