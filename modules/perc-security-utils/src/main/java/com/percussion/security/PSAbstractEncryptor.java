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
