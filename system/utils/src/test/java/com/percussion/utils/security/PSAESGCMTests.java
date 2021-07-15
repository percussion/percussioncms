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

package com.percussion.utils.security;

import com.percussion.security.IPSDecryptor;
import com.percussion.security.IPSEncryptor;
import com.percussion.security.IPSKey;
import com.percussion.security.IPSSecretKey;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptionKeyFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

//@todo: Fix it...
@Ignore
public class PSAESGCMTests {


    public PSAESGCMTests(){}

    @Before
    public void setup(){

    }

    @After
    public void teardown(){

    }

    @Test
    public void testEncryptDecryptBasic() throws PSEncryptionException {

        IPSKey key = PSEncryptionKeyFactory.getKeyGenerator(PSEncryptionKeyFactory.AES_GCM_ALGORIYTHM);
        key.setSecret(((IPSSecretKey)key).generateKey().getEncoded());

        IPSEncryptor encryptor = key.getEncryptor();

        byte[] data = encryptor.encrypt("This is a test");

        assertNotNull(data);
        assertNotEquals(data,"This is a test".getBytes(StandardCharsets.UTF_8));
        IPSDecryptor decryptor = key.getDecryptor();

        String decrypted = decryptor.decrypt(data);

        assertEquals("This is a test",decrypted);
    }

    @Test
    public void testGenerateKey(){
        SecretKey key = PSEncryptionKeyFactory.getKeyGenerator(PSEncryptionKeyFactory.AES_GCM_ALGORIYTHM).generateKey();

        assertNotNull(key);

        System.out.println("Algorithm: " + key.getAlgorithm());
        System.out.println("Format:" + key.getFormat());
        System.out.println("Key:" + Base64.getEncoder().encodeToString(key.getEncoded()));
    }

}
