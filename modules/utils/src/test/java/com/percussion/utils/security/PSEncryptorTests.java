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
import com.percussion.security.PSEncryptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for PSEncryptor
 */
//@todo: Fix it...
@Ignore
public class PSEncryptorTests {

    @Rule
    public TemporaryFolder temporaryFolder = TemporaryFolder.builder().build();
    private String rxdeploydir;

    @Before
    public void setup(){
        rxdeploydir = System.getProperty("rxdeploydir");
        System.setProperty("rxdeploydir",temporaryFolder.getRoot().getAbsolutePath());
    }

    @After
    public void teardown(){
        //Reset the deploy dir property if it was set prior to test
        if(rxdeploydir != null)
            System.setProperty("rxdeploydir",rxdeploydir);
    }

    public PSEncryptorTests(){}

    @Test
    public void testKeyStorage() throws PSEncryptionException {

        assertTrue(PSEncryptor.decryptString(rxdeploydir, PSEncryptor.encryptString(rxdeploydir, "Gnomes rule!!!")).equals("Gnomes rule!!!"));

    }

    @Test
    public void testgetUIDandPassword(){
        assertEquals(PSEncryptor.getUID("uid:password"),("uid"));
        assertEquals(PSEncryptor.getPassword("uid:password"),("password"));

       assertEquals(PSEncryptor.getUID("uid:pass::ord"),("uid"));
       assertEquals(PSEncryptor.getPassword("uid:pass::ord"),("pass::ord"));

    }

    @Test
    public void testCredentials() throws PSEncryptionException {
        String enc = PSEncryptor.getInstance("AES",
                rxdeploydir + PSEncryptor.SECURE_DIR).encryptCredentials(
                        "user1", "user1$:Pass");

        System.out.println("--------------------------");
        System.out.println("Encoded Credentials:" + enc);
        System.out.println("--------------------------");

        assertNotNull(enc);
        assertNotEquals("user1:user1$:Pass",enc);

        enc = PSEncryptor.getInstance("AES",
                rxdeploydir + PSEncryptor.SECURE_DIR).decryptCredentials(
                        enc,"user1$:Pass");

        assertNotNull(enc);
        assertEquals("user1:user1$:Pass",enc);
        System.out.println("--------------------------");
        System.out.println("Decoded Credentials:" + enc);
        System.out.println("--------------------------");



    }

    @Test
    public void testClientStyleOfGetInstance() throws PSEncryptionException {

        teardown();

        String pw = PSEncryptor.encryptString(System.getProperty("user.home") + File.separator + ".perc-secure"  + File.separator,"Cocaine is a hell of a drug.");
        assertNotEquals(pw,"Cocaine is a hell of a drug.");
        String dpw = PSEncryptor.decryptString(System.getProperty("user.home") + File.separator + ".perc-secure"  + File.separator,pw);
        assertEquals("Cocaine is a hell of a drug.", dpw);

    }

}
