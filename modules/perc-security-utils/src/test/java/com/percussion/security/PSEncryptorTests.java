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


        PSEncryptor crypt = PSEncryptor.getInstance("AES",
                rxdeploydir + PSEncryptor.SECURE_DIR);

        assertTrue(crypt.decrypt(crypt.encrypt("Gnomes rule!!!")).equals("Gnomes rule!!!"));

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

        PSEncryptor enc = PSEncryptor.getInstance("AES",
                System.getProperty("user.home") + File.separator + ".perc-secure" + File.separator);

        String pw = enc.encrypt("Cocaine is a hell of a drug.");
        assertNotEquals(pw,"Cocaine is a hell of a drug.");
        assertEquals("Cocaine is a hell of a drug.", enc.decrypt(pw));

    }

}
