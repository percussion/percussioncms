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
import java.util.Base64;

public class PSAESGCMDecryptor implements IPSDecryptor {


    private PSAESGCMKey m_key=null;

    /**
     * Construct a AES decryptor using the specified DES key.
     *
     * @param      key      the AES key to use for decryption
     * @throws IllegalArgumentException if <code>key</code> is <code>null</code>
     */
    public PSAESGCMDecryptor(PSAESGCMKey key) throws IllegalArgumentException
    {
        if (key == null)
            throw new IllegalArgumentException( "key cannot be null" );

        // store key for later use
        m_key = key;
    }

    /**
     * Decrypt the data in the specified input stream.
     *
     * @param in  the stream containing the encrypted data
     * @param out the stream to store the plain text
     *            representation of the data
     */
    @Override
    public void decrypt(InputStream in, OutputStream out) {

    }

    /**
     * A convenience method to decrypt data into a String.
     *
     * @param in the stream containing the encrypted data
     * @return a string containing the plain text
     * representation of the data
     */
    @Override
    public String decrypt(InputStream in) {
        return null;
    }

    /**
     * A convenidece method to decrypt data from a byte array into a String.
     *
     * @param in the byte array containing the encrypted data
     * @return a string containing the plain text
     * representation of the data
     * @throws PSEncryptionException
     */
    @Override
    public String decrypt(byte[] in) throws PSEncryptionException {
        byte[] decryptedText=null;

        ByteBuffer bb = ByteBuffer.wrap(in);

        int size = (bb.capacity() > 12? 12: bb.capacity());
        byte[] iv = new byte[size];
        bb.get(iv);

        byte[] cipherText = new byte[bb.remaining()];
        bb.get(cipherText);

        try {

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            PSAESGCMKey aesKey = (PSAESGCMKey)m_key;
            cipher.init(Cipher.DECRYPT_MODE, aesKey.getSecretKey(), new GCMParameterSpec(128, iv));
            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, StandardCharsets.UTF_8);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            throw new PSEncryptionException(e.getMessage(),e);
        }

    }

    @Override
    public String decryptWithPassword(String in, String password) throws PSEncryptionException {

        try {
            byte[] decoded = Base64.getDecoder().decode(in.getBytes(StandardCharsets.UTF_8));

            ByteBuffer bb = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[12];
            bb.get(iv);

            byte[] salt = new byte[16];
            bb.get(salt);

            byte[] encryptedText = new byte[bb.remaining()];
            bb.get(encryptedText);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            // iterationCount = 65536
            // keyLength = 256
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1000, 256);
            SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(Cipher.DECRYPT_MODE, secret, new GCMParameterSpec(128, iv));

            return new String(cipher.doFinal(encryptedText),StandardCharsets.UTF_8);

        } catch (InvalidKeySpecException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException e) {
            throw new PSEncryptionException(e.getMessage(),e);
        }
    }


}
