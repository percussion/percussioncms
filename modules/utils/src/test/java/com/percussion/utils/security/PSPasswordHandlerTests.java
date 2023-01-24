/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.utils.security;

import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSPasswordHandler;
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
