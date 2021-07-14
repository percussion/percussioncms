/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.security;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @todo Fix it... fails on Linux buildServer
 */
@Ignore
public class PSPasswordHandlerTests {

    private static String savedEncPass;

    @Test
    public void testPasswordHandler() throws PSEncryptionException,InterruptedException {
        String encodedPw = PSPasswordHandler.getHashedPassword("HEY WHADAYA DOIN' WITH YOUR LIFE HEY WHADAYA DOIN' WITH YOUR LIFE");
        savedEncPass = encodedPw;

       System.out.println("Encoded:" + encodedPw);
       System.out.println("Encoded Length:" + encodedPw.length());
        assertNotNull(encodedPw);

        assertTrue(PSPasswordHandler.checkHashedPassword("HEY WHADAYA DOIN' WITH YOUR LIFE HEY WHADAYA DOIN' WITH YOUR LIFE",encodedPw));

        assertFalse(PSPasswordHandler.checkHashedPassword("I'll be home for Christmas",encodedPw));

        try {
            assertFalse(PSPasswordHandler.checkHashedPassword(null, encodedPw));
        }catch(IllegalArgumentException e){
           System.out.println("Null check passed.");
        }

        try {
            assertFalse(PSPasswordHandler.checkHashedPassword("test", null));
        }catch(IllegalArgumentException e){
           System.out.println("Null check passed.");
         }
    }

    @Test
    public void testSecondTime() throws PSEncryptionException {
        assertTrue(PSPasswordHandler.checkHashedPassword("HEY WHADAYA DOIN' WITH YOUR LIFE HEY WHADAYA DOIN' WITH YOUR LIFE","h5ihvAb3oi2/uTS2jeA1GEnPE0zRs4N6viD0wE1AI6FDCU9FR5ccnryu6P820VEqDDBTQTYxPSRQTm1E6McklzOIsT/Us1YmJw8PfJ7QXd7G2GctJcmoIoUIllXScHtp8zdqHw9/MPDzpyjJ/s5lmsgPHxX55/Acl6XHRU+B9fCo0U13su7mtgxNVElsNnRf"));

    }
}
