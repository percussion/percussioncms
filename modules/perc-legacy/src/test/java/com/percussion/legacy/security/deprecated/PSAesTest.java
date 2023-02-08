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

package com.percussion.legacy.security.deprecated;


import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Deprecated
public class PSAesTest
{
    /**
     * Encryption key used to test AES functionality. Must be 16 bytes
     */
    final String encryptionKey = "0123456789abcdef";

    /**
     * Assert that the encrypt method returns a String that is different from
     * the input. Assert that the decrypt method returns a String equal to the
     * original input of the encrypt method.
     * 
     * @throws Exception
     */
    @Test
    public void testEncryptDecryptJsonData() throws Exception {

        final String input = "~!@$%^&*()_+aB®©";

        PSAesCBC aes = new PSAesCBC();
        final String encrypted = aes.encrypt(input, encryptionKey);
        final String decrypted = aes.decrypt(encrypted, encryptionKey);

        Assert.assertFalse("encrypted not equals input",
                input.equalsIgnoreCase(encrypted));
        Assert.assertTrue("decrypted is same as input", input.equals(decrypted));
    }

}
