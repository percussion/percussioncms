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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.security;

import org.junit.Test;

import static org.junit.Assert.*;

public class PSPasswordHandlerTests {



    @Test
    public void testPasswordHandler() throws PSEncryptionException {
        String encodedPw = PSPasswordHandler.getHashedPassword("HEY WHADAYA DOIN' WITH YOUR LIFE HEY WHADAYA DOIN' WITH YOUR LIFE");

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
}
