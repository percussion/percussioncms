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


import static org.apache.commons.lang3.StringUtils.isBlank;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Deprecated
public class PSAesCBC
{
    /**
     * Strings encrypted with this IV can only be de-crypted with this IV
     */
    private final byte[] InitialVector;

    /**
     * Create a new AES. Sets initial values of byte array.
     */
    @Deprecated
    public PSAesCBC() {
        super();
        InitialVector = new byte[] { 0x04, 0x38, 0x40, 0x33, 0x17, 0x65, 0x32,
                0x28, 0x56, 0x39, 0x50, 0x23, 0x7c, 0x6a, 0x0f, 0x3a };
    }

    /**
     * Encrypt a given plain text String using a given encryption key. Character encode
     * the encrypted text as ISO-8859-1 String.
     * 
     * @param plainText
     *            String to encrypt. Not null.
     * @param encryptionKey
     *            String used for encryption. Not null.
     * @return The resultant String of encrypted text
     * @throws Exception
     */
    @Deprecated
    public String encrypt(String plainText, String encryptionKey)
            throws Exception {
        if(isBlank(plainText))
            plainText = "";
        if(isBlank(encryptionKey))
            encryptionKey = "";
        
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");

        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("ISO-8859-1"),
                "AES");

        cipher.init(Cipher.ENCRYPT_MODE, key,
                new IvParameterSpec(InitialVector));

        final byte[] encrypted = cipher.doFinal(plainText.getBytes("ISO-8859-1"));

        return  new String(encrypted,"ISO-8859-1");
    }
    
    /**
     * Decode a given ISO-8859-1 character encoded String. Decrypt resulting String using encryption
     * key String.
     * 
     * @param secretText
     *            String to decyrpt. May be null.
     * @param encryptionKey
     *            String used for decryption. Not null.
     * @return The resultant String of decrypted and decoded text.
     * @throws Exception
     */
    @Deprecated
    public String decrypt(String secretText, String encryptionKey)
            throws Exception {
        if(isBlank(secretText))
            secretText = "";
        if(isBlank(encryptionKey))
            encryptionKey = "";
        
        final byte[] cipherText = secretText.getBytes("ISO-8859-1");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");

        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("ISO-8859-1"),
                "AES");

        cipher.init(Cipher.DECRYPT_MODE, key,
                new IvParameterSpec(InitialVector));

        return new String(cipher.doFinal(cipherText),"ISO-8859-1");
    }

}
