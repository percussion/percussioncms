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


import com.percussion.utils.security.deprecated.PSDESKey;

/**
 * This factory class determines which encryptor is available for use
 * by the system. It is the callers responsibility to understand the
 * key generation scheme, etc. and use the returned object appropriately.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public abstract class PSEncryptionKeyFactory
{

   @Deprecated
   public static final String DES_ALGORITHM="DES";

   public static final String AES_GCM_ALGORIYTHM="AES";

   private PSEncryptionKeyFactory() { super(); }

   /**
    * Get an instance of the key generator which can be used for the
    * default encryption/decryption algorithm.
    * From the key type returned, the caller can determine what to use
    * to generate the key. The updated key can then be passed in to the
    * encryptor/decryptor.
    */
   public static IPSKey getKeyGenerator(String algorithm)
   {
      IPSKey key = null;

      if(algorithm == null)
         throw new IllegalArgumentException("Algorithm cannot be null.");

      if(!(algorithm.equalsIgnoreCase(AES_GCM_ALGORIYTHM) || algorithm.equalsIgnoreCase(DES_ALGORITHM)))
         throw new IllegalArgumentException("Algorithm not supported");

      try {
         if(algorithm.equalsIgnoreCase(AES_GCM_ALGORIYTHM))
            key = new PSAESGCMKey();
         else if (algorithm.equalsIgnoreCase(DES_ALGORITHM)){
            key = new PSDESKey();
         }
      } catch (Exception e) {

      }

      return key;
   }

}

