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

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.HexDump;

/**
 *
 * @author tkv
 */
public class MessageTest
{
    
    public MessageTest()
    {
    }

    @Test
    public void test1()
    {
        try
        {
            String hd = 
"      00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f\n" +
"0000: 00 01 81 80 00 01 00 02 00 0d 00 0d 03 77 77 77  . . . . . . . . . . . . . w w w\n" +
"0010: 09 6b 61 75 61 73 70 6f 69 73 02 66 69 00 00 01  . k a u a s p o i s . f i . . .\n" +
"0020: 00 01 c0 0c 00 05 00 01 00 00 03 84 00 16 03 67  . . . . . . . . . . . . . . . g\n" +
"0030: 68 73 0c 67 6f 6f 67 6c 65 68 6f 73 74 65 64 03  h s . g o o g l e h o s t e d .\n" +
"0040: 63 6f 6d 00 c0 2e 00 01 00 01 00 00 00 b8 00 04  c o m . . . . . . . . . . . . .\n" +
"0050: 4a 7d 8d 79 00 00 02 00 01 00 03 01 ee 00 14 01  J } . y . . . . . . . . . . . .\n" +
"0060: 6b 0c 72 6f 6f 74 2d 73 65 72 76 65 72 73 03 6e  k . r o o t - s e r v e r s . n\n" +
"0070: 65 74 00 00 00 02 00 01 00 03 01 ee 00 04 01 65  e t . . . . . . . . . . . . . e\n" +
"0080: c0 61 00 00 02 00 01 00 03 01 ee 00 04 01 62 c0  . a . . . . . . . . . . . . b .\n" +
"0090: 61 00 00 02 00 01 00 03 01 ee 00 04 01 6d c0 61  a . . . . . . . . . . . . m . a\n" +
"00a0: 00 00 02 00 01 00 03 01 ee 00 04 01 6a c0 61 00  . . . . . . . . . . . . j . a .\n" +
"00b0: 00 02 00 01 00 03 01 ee 00 04 01 68 c0 61 00 00  . . . . . . . . . . . h . a . .\n" +
"00c0: 02 00 01 00 03 01 ee 00 04 01 67 c0 61 00 00 02  . . . . . . . . . . g . a . . .\n" +
"00d0: 00 01 00 03 01 ee 00 04 01 64 c0 61 00 00 02 00  . . . . . . . . . d . a . . . .\n" +
"00e0: 01 00 03 01 ee 00 04 01 63 c0 61 00 00 02 00 01  . . . . . . . . c . a . . . . .\n" +
"00f0: 00 03 01 ee 00 04 01 61 c0 61 00 00 02 00 01 00  . . . . . . . a . a . . . . . .\n" +
"0100: 03 01 ee 00 04 01 69 c0 61 00 00 02 00 01 00 03  . . . . . . i . a . . . . . . .\n" +
"0110: 01 ee 00 04 01 66 c0 61 00 00 02 00 01 00 03 01  . . . . . f . a . . . . . . . .\n" +
"0120: ee 00 04 01 6c c0 61 c0 5f 00 01 00 01 00 00 4c  . . . . l . a . _ . . . . . . L\n" +
"0130: 2a 00 04 c1 00 0e 81 c0 7e 00 01 00 01 00 01 7a  * . . . . . . . ~ . . . . . . z\n" +
"0140: 08 00 04 c0 cb e6 0a c0 8d 00 01 00 01 00 01 7a  . . . . . . . . . . . . . . . z\n" +
"0150: 08 00 04 c0 e4 4f c9 c0 9c 00 01 00 01 00 05 41  . . . . . O . . . . . . . . . A\n" +
"0160: 7e 00 04 ca 0c 1b 21 c0 ab 00 01 00 01 00 00 4c  ~ . . . . . ! . . . . . . . . L\n" +
"0170: 2b 00 04 c0 3a 80 1e c0 ba 00 01 00 01 00 01 7a  + . . . : . . . . . . . . . . z\n" +
"0180: 08 00 04 c6 61 be 35 c0 c9 00 01 00 01 00 01 7a  . . . . a . 5 . . . . . . . . z\n" +
"0190: 08 00 04 c0 70 24 04 c0 d8 00 01 00 01 00 01 7a  . . . . p $ . . . . . . . . . z\n" +
"01a0: 08 00 04 c7 07 5b 0d c0 e7 00 01 00 01 00 01 7a  . . . . . [ . . . . . . . . . z\n" +
"01b0: 08 00 04 c0 21 04 0c c0 f6 00 01 00 01 00 00 28  . . . . ! . . . . . . . . . . (\n" +
"01c0: 80 00 04 c6 29 00 04 c1 05 00 01 00 01 00 00 4c  . . . . ) . . . . . . . . . . L\n" +
"01d0: 2b 00 04 c0 24 94 11 c1 14 00 01 00 01 00 01 7a  + . . . $ . . . . . . . . . . z\n" +
"01e0: 08 00 04 c0 05 05 f1 c1 23 00 01 00 01 00 00 4c  . . . . . . . . # . . . . . . L\n" +
"01f0: 29 00 04 c7 07 53 2a   ) . . . . S * . . . . . . . . .";
            byte[] orig = HexDump.fromHex(hd);
            Zones.setClock(Clock.fixed(Instant.now(), ZoneOffset.UTC));
            Message msg = new Message(orig);
            byte[] test = msg.toByteArray();
            test[2] &= 0b11111101;
            System.err.println(HexDump.toHex(test));
            Assert.assertArrayEquals(orig, test);
        }
        catch (IOException | RCodeException ex)
        {
            Logger.getLogger(MessageTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }
    
    //@Test
    public void test2()
    {
        try
        {
            String hd = "     00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f\n" +
                        "000: 4a af 81 80 00 01 00 04 00 0d 00 0d 03 77 77 77  J ﾯ . . . . . . . . . . . w w w\n" +
                        "010: 04 66 65 6d 61 03 67 6f 76 00 00 01 00 01 c0 0c  . f e m a . g o v . . . . . ￀ .\n" +
                        "020: 00 05 00 01 00 00 3a f0 00 1a 03 77 77 77 04 66  . . . . . . : ￰ . . . w w w . f\n" +
                        "030: 65 6d 61 03 67 6f 76 07 65 64 67 65 6b 65 79 03  e m a . g o v . e d g e k e y .\n" +
                        "040: 6e 65 74 00 c0 2a 00 05 00 01 00 00 01 2b 00 21  n e t . ￀ * . . . . . . . + . !\n" +
                        "050: 04 66 65 6d 61 0d 67 65 6f 72 65 64 69 72 65 63  . f e m a . g e o r e d i r e c\n" +
                        "060: 74 6f 72 04 66 65 6d 61 06 61 6b 61 64 6e 73 c0  t o r . f e m a . a k a d n s ￀\n" +
                        "070: 3f c0 50 00 05 00 01 00 00 01 2b 00 18 05 65 36  ? ￀ P . . . . . . . + . . . e 6\n" +
                        "080: 34 38 35 04 64 73 63 62 0a 61 6b 61 6d 61 69 65  4 8 5 . d s c b . a k a m a i e\n" +
                        "090: 64 67 65 c0 3f c0 7d 00 01 00 01 00 00 00 3c 00  d g e ￀ ? ￀ } . . . . . . . < .\n" +
                        "0a0: 04 17 01 74 38 00 00 00 02 00 01 00 03 2a 53 00  . . . t 8 . . . . . . . . * S .\n" +
                        "0b0: 11 01 6c 0c 72 6f 6f 74 2d 73 65 72 76 65 72 73  . . l . r o o t - s e r v e r s\n" +
                        "0c0: c0 3f c0 a5 00 02 00 01 00 03 2a 53 00 04 01 6b  ￀ ? ￀ ﾥ . . . . . . * S . . . k\n" +
                        "0d0: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 65  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . e\n" +
                        "0e0: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 62  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . b\n" +
                        "0f0: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 6d  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . m\n" +
                        "100: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 6a  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . j\n" +
                        "110: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 68  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . h\n" +
                        "120: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 67  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . g\n" +
                        "130: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 64  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . d\n" +
                        "140: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 63  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . c\n" +
                        "150: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 61  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . a\n" +
                        "160: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 69  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . i\n" +
                        "170: c0 b3 c0 a5 00 02 00 01 00 03 2a 53 00 04 01 66  ￀ ﾳ ￀ ﾥ . . . . . . * S . . . f\n" +
                        "180: c0 b3 c0 b1 00 01 00 01 00 00 74 8e 00 04 c7 07  ￀ ﾳ ￀ ﾱ . . . . . . t . . . ￇ .\n" +
                        "190: 53 2a c0 ce 00 01 00 01 00 00 74 8f 00 04 c1 00  S * ￀ ￎ . . . . . . t . . . ￁ .\n" +
                        "1a0: 0e 81 c0 de 00 01 00 01 00 01 a2 6d 00 04 c0 cb  . . ￀ ￞ . . . . . . ﾢ m . . ￀ ￋ\n" +
                        "1b0: e6 0a c0 ee 00 01 00 01 00 01 a2 6d 00 04 c0 e4  ￦ . ￀ ￮ . . . . . . ﾢ m . . ￀ ￤\n" +
                        "1c0: 4f c9 c0 fe 00 01 00 01 00 05 69 e3 00 04 ca 0c  O ￉ ￀ <fffe> . . . . . . i ￣ . . ￊ .\n" +
                        "1d0: 1b 21 c1 0e 00 01 00 01 00 00 74 90 00 04 c0 3a  . ! ￁ . . . . . . . t . . . ￀ :\n" +
                        "1e0: 80 1e c1 1e 00 01 00 01 00 01 a2 6d 00 04 c6 61  . . ￁ . . . . . . . ﾢ m . . ￆ a\n" +
                        "1f0: be 35 c1 2e 00 01 00 01 00 01 a2 6d 00 04 c0 70  ﾾ 5 ￁ . . . . . . . ﾢ m . . ￀ p\n" +
                        "200: 24 04 c1 3e 00 01 00 01 00 01 a2 6d 00 04 c7 07  $ . ￁ > . . . . . . ﾢ m . . ￇ .\n" +
                        "210: 5b 0d c1 4e 00 01 00 01 00 01 a2 6d 00 04 c0 21  [ . ￁ N . . . . . . ﾢ m . . ￀ !\n" +
                        "220: 04 0c c1 5e 00 01 00 01 00 00 50 e5 00 04 c6 29  . . ￁ ^ . . . . . . P ￥ . . ￆ )\n" +
                        "230: 00 04 c1 6e 00 01 00 01 00 00 74 90 00 04 c0 24  . . ￁ n . . . . . . t . . . ￀ $\n" +
                        "240: 94 11 c1 7e 00 01 00 01 00 01 a2 6d 00 04 c0 05  . . ￁ ~ . . . . . . ﾢ m . . ￀ .\n" +
                        "250: 05 f1                                            . ￱";
            byte[] fromHex = HexDump.fromHex(hd);
            Message msg = new Message(fromHex);
        }
        catch (IOException | RCodeException ex)
        {
            Logger.getLogger(MessageTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }
    
}
