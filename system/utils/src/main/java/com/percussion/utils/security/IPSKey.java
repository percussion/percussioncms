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

import com.percussion.utils.security.IPSDecryptor;
import com.percussion.utils.security.IPSEncryptor;

import javax.crypto.SecretKey;

/**
 * IPSKey is a transparent interface for encryption/decryption
 * algorithms which can be used within the product.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public interface IPSKey
{
   /**
    * Generate an IPSEncryptor object which can make use of this key.
    *
    * @return            the associated encryptor
    */
   public IPSEncryptor getEncryptor();

   /**
    * Generate an IPSDecryptor object which can make use of this key.
    *
    * @return            the associated decryptor
    */
   public IPSDecryptor getDecryptor();

   /**
    * Returns a byte aray containing the secret key
    * @return
    */
   public byte[] getSecret();

   /**
    * Sets the secret to the specified byte array.
    * @param secret
    */
   public void setSecret(byte[] secret);

   /**
    * Generates a new key.
    *
    * @return a byte array containing the new encryption key
    */
   public SecretKey generateKey();


}

