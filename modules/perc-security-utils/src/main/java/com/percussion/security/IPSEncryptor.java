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

