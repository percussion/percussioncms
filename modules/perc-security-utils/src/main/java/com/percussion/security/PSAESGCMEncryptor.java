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

package com.percussion.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class PSAESGCMEncryptor implements IPSEncryptor {

    private static final Logger log = LogManager.getLogger(PSAESGCMEncryptor.class);

    private PSAESGCMKey m_key;


    public byte[] getIV(int size) {
        byte[] nonce = new byte[size];
        SecureStringUtils.getSecureRandom().nextBytes(nonce);
        return nonce;
    }

    /**
     * Construct an AES encryptor using the specified AES key.
     *
     * @param      key      the AES key to use for encryption
     *
     * @throws IllegalArgumentException if <code>key</code> is <code>null</code>
     */
    public PSAESGCMEncryptor(PSAESGCMKey key)
    {
        if (key == null)
            throw new IllegalArgumentException("key cannot be null");

        // store key for later use
        m_key = key;
    }

    /**
     * Encrypt the data in the specified input stream.
     *
     * @param in  the stream containing the plain text
     *            representation of the data
     * @param out the stream to store the encrypted data
     * @throws PSEncryptionException if an exception occurs
     */
    @Override
    public void encrypt(InputStream in, OutputStream out) throws PSEncryptionException {

        throw new PSEncryptionException("Not yet implemented");
    }

    /**
     * A convenience method to encrypt a String.
     *
     * @param in  the string containing the plain text
     *            representation of the data
     * @param out the stream to store the encrypted data
     * @throws PSEncryptionException if an I/O exception occurs
     */
    @Override
    public void encrypt(String in, OutputStream out) throws PSEncryptionException {
        throw new PSEncryptionException("Not yet implemented");
    }

    /**
     * A convenience method to encrypt a String and retrieve the
     * resulting byte array.
     *
     * @param in the string containing the plain text
     *           representation of the data
     * @return a byte array containing the encrypted data
     */
    @Override
    public byte[] encrypt(String in) throws PSEncryptionException {
        try {
            PSAESGCMKey aesKey = m_key;
            byte[] iv = getIV(12);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey.getSecretKey(), new GCMParameterSpec(128, iv));
            byte[] encryptedText = cipher.doFinal(in.getBytes(StandardCharsets.UTF_8));

            //Prepend encrypted text with the IV block so that it can be decrypted.
            return ByteBuffer.allocate(iv.length + encryptedText.length)
                    .put(iv)
                    .put(encryptedText)
                    .array();

        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            throw new PSEncryptionException("Error in AES-GCM encryption: " + e.getMessage(),e);
        }
    }

    @Override
    public byte[] encryptWithPassword(String in, String password) throws PSEncryptionException {

        try {
            byte[] salt = getIV(16);

            byte[] iv = getIV(12);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1000, 256);
            SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            // ASE-GCM needs GCMParameterSpec
            cipher.init(Cipher.ENCRYPT_MODE, secret, new GCMParameterSpec(128, iv));

            byte[] cipherText = cipher.doFinal(in.getBytes(StandardCharsets.UTF_8));

            // prefix IV and Salt to cipher text
            return  ByteBuffer.allocate(iv.length + salt.length + cipherText.length)
                    .put(iv)
                    .put(salt)
                    .put(cipherText)
                    .array();
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException e) {
            throw new PSEncryptionException(String.format("Error in AES-GCM encryption: %s", e.getMessage()),e);
        }
    }
}
