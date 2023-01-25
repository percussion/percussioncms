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

