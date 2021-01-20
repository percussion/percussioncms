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

package com.percussion.utils.security;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * IPSEncryptor defines the interface for an encryption
 * algorithm which can be used within the product.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public interface IPSEncryptor
{
   /**
    * Encrypt the data in the specified input stream.
    *
    * @param      in            the stream containing the plain text
    *                           representation of the data
    *
    * @param      out         the stream to store the encrypted data
    *
    */
   public abstract void encrypt(java.io.InputStream in, java.io.OutputStream out) throws PSEncryptionException;

   /**
    * A convenience method to encrypt a String.
    *
    * @param      in            the string containing the plain text
    *                           representation of the data
    *
    * @param      out         the stream to store the encrypted data
    *
    */
   public abstract void encrypt(java.lang.String in, java.io.OutputStream out)
           throws PSEncryptionException;

   /**
    * A convenience method to encrypt a String and retrieve the
    * resulting byte array.
    *
    * @param      in            the string containing the plain text
    *                           representation of the data
    *
    * @return                  a byte array containing the encrypted data
    *
    */
   public abstract byte[] encrypt(java.lang.String in)
           throws PSEncryptionException;

   public abstract byte[] encryptWithPassword(String in, String password)
           throws PSEncryptionException;

}

