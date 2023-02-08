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


import com.percussion.security.IPSKey;
import com.percussion.security.PSAESGCMKey;

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

      if(!algorithm.equalsIgnoreCase(AES_GCM_ALGORIYTHM))
         throw new IllegalArgumentException("Algorithm not supported");

      key = new PSAESGCMKey();

      return key;

   }

}

