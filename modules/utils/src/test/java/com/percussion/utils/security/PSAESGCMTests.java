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
