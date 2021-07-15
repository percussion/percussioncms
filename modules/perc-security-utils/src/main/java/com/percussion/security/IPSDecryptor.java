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


/**
 * IPSDecryptor defines the interface for a decryption
 * algorithm which can be used within the product.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public interface IPSDecryptor
{
   /**
    * Decrypt the data in the specified input stream.
    *
    * @param      in            the stream containing the encrypted data
    *
    * @param      out         the stream to store the plain text
    *                           representation of the data
    *
    */
   public abstract void decrypt(java.io.InputStream in, java.io.OutputStream out)
           throws PSEncryptionException;

   /**
    * A convenidece method to decrypt data into a String.
    *
    * @param      in            the stream containing the encrypted data
    *
    * @return                  a string containing the plain text
    *                           representation of the data
    *
    */
   public abstract java.lang.String decrypt(java.io.InputStream in)
      throws PSEncryptionException;

   /**
    * A convenidece method to decrypt data from a byte array into a String.
    *
    * @param      in            the byte array containing the encrypted data
    *
    * @return                  a string containing the plain text
    *                           representation of the data
    *
    */
   public abstract java.lang.String decrypt(byte[] in)
      throws PSEncryptionException;

   public abstract String decryptWithPassword(String in, String password)
           throws PSEncryptionException;
}

