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

package com.percussion.security;

import java.math.BigInteger;

public abstract class PSAbstractEncryptor {
    /**
     * The length of the byte array passed to the cipher encryption object for
     * decryption in {@link #decrypt(String, String, PSAbstractEncryptor)} must be a multiple of this
     * value.  Used in {@link #toByteArray(BigInteger)}.
     */
    public  int BYTE_ARRAY_MULTIPLE = 8;


    /**
     * Encrypts the provided string using the supplied secret key
     *
     * @param str The string to encrypt, may not be <code>null</code>, may be
     *            empty.
     * @param key The secret key to encrypt the string, may not be
     *            <code>null</code> or empty.
     * @return The encrypted string, never <code>null</code>, may be empty.
     */
    public abstract String encrypt(String str, String key) throws PSEncryptionException;

    /**
     * Decrypts the provided string using the supplied secret key.
     *
     * @param str The string to decrypt, may not be <code>null</code>, may be
     *            empty.
     * @param key The secret key that was used to encrypt the string, may not
     *            be <code>null</code> or empty.
     * @param legacyDecryptor
     * @return The decrypted string, never <code>null</code>, may be empty.
     */
    public abstract String decrypt(String str, String key, PSAbstractEncryptor legacyDecryptor) throws PSEncryptionException;

    /**
     * Converts a <code>BigInteger</code> to a byte array whose size is a
     * multiple of {@link #BYTE_ARRAY_MULTIPLE}.  For positive values or a value
     * of zero, the byte array is padded with leading 0 byte values if necessary.
     * For negative values, the byte array is padded with leading -1 byte values
     * if necessary.
     *
     * @param bigInt The <code>BigInteger</code> to convert, may not be
     *               <code>null</code>.
     * @return A valid byte array form of a <code>BigInteger</code> with a size
     * which is a multiple of {@link #BYTE_ARRAY_MULTIPLE}.
     */
    public abstract byte[] toByteArray(BigInteger bigInt) throws PSEncryptionException;


    /**
     * Encrypts the specified credentials.
     *
     * @param uid Not null.  The user id.
     * @param pw  Not null.  The password.
     * @return A Base64 encoded encrypted credential string.
     * @throws PSEncryptionException
     */
    public abstract String encryptCredentials(String uid, String pw) throws PSEncryptionException;

    /**
     * Decrypts the specified credentials using the supplied password.
     * @param encrypted The encrypted credentials
     * @param pw The password to use for decryption.
     * @return
     * @throws PSEncryptionException
     */
    public abstract String decryptCredentials(String encrypted, String pw) throws PSEncryptionException;

}
