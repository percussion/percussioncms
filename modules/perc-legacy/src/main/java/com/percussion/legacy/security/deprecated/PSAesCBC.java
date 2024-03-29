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


import com.percussion.security.PSEncryptionException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static org.apache.commons.lang3.StringUtils.isBlank;

@SuppressFBWarnings("CIPHER_INTEGRITY")
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
    //Suppressing warnings as the class is deprecated.
    @SuppressFBWarnings({"PADDING_ORACLE", "CIPHER_INTEGRITY", "STATIC_IV"})
    @Deprecated
    public String encrypt(String plainText, String encryptionKey)
            throws Exception {
        if(isBlank(plainText))
            plainText = "";
        if(isBlank(encryptionKey))
            encryptionKey = "";
        
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");

        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.ISO_8859_1),
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
    @SuppressFBWarnings("PADDING_ORACLE")
    @Deprecated
    public String decrypt(String secretText, String encryptionKey)
            throws PSEncryptionException {
        if(isBlank(secretText))
            secretText = "";
        if(isBlank(encryptionKey))
            encryptionKey = "";

        try {
            final byte[] cipherText = secretText.getBytes(StandardCharsets.ISO_8859_1);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");

            SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.ISO_8859_1),
                    "AES");

            cipher.init(Cipher.DECRYPT_MODE, key,
                    new IvParameterSpec(InitialVector));

            return new String(cipher.doFinal(cipherText), StandardCharsets.ISO_8859_1);
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | BadPaddingException | NoSuchProviderException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException e) {
            throw new PSEncryptionException(e);
        }
    }

}
