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
