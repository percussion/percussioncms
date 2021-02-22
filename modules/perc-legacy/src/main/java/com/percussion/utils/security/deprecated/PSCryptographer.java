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

package com.percussion.utils.security.deprecated;

import com.percussion.security.IPSDecryptor;
import com.percussion.security.IPSEncryptor;
import com.percussion.security.IPSKey;
import com.percussion.security.IPSSecretKey;
import com.percussion.security.PSEncryptionKeyFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;

/**
 * This class is used to encrypt and decrypt using percussion encryption
 * algorithms.
 */
@Deprecated
public class PSCryptographer
{
   /**
    * Decrypts the specified string using the "lasagna" method.  Requires the
    * two keys that were used to encrypt the string originally.
    *
    * @param key1 The first key used when encrypting the string.  May not be
    * <code>null</code> or empty.
    * @param key2 The second key used when encrypting the string.  May not be
    * <code>null</code> or empty.
    * @param str The string to decrypt.  If <code>null</code> or empty, an
    * empty string is returned.
    *
    * @return The decrypted string, never <code>null</code>, may be empty.
    *
    * @throws IllegalArgumentException if <code>key1</code> or <code>key2</code>
    * is <code>null</code> or empty.
    */
   @Deprecated
   public static String decrypt(String key1, String key2, String str)
   {
      if (key1 == null || key1.trim().length() == 0)
         throw new IllegalArgumentException("key1 may not be null or empty");

      if (key2 == null || key2.trim().length() == 0)
         throw new IllegalArgumentException("key2 may not be null or empty");

      if ((str == null) || (str.equals("")))
         return "";

      int partone = key1.hashCode();
      int parttwo = key2.hashCode();

      partone /= 7;
      parttwo /= 13;


         int padLen = 0;
         try(ByteArrayOutputStream bOut = new ByteArrayOutputStream()){

            byte[] bOutarr = Base64.getMimeDecoder().decode(str.getBytes(
                    StandardCharsets.UTF_8));

            IPSKey key = PSEncryptionKeyFactory.getKeyGenerator(PSEncryptionKeyFactory.DES_ALGORITHM);
            if ((key != null) && (key instanceof IPSSecretKey))
            {
               IPSSecretKey secretKey = (IPSSecretKey)key;
               byte[] baOuter = new byte[8];
               for (int i = 0; i < 4; i++)
                  baOuter[i] = (byte)((partone >> i) & 0xFF);
               for (int i = 4; i < 8; i++)
                  baOuter[i] = (byte)((parttwo >> (i-4)) & 0xFF);

               secretKey.setSecret(baOuter);
               IPSDecryptor decr = secretKey.getDecryptor();

               try(ByteArrayOutputStream bOut2 = new ByteArrayOutputStream())
               {
                  try(ByteArrayInputStream by = new ByteArrayInputStream(bOutarr)) {
                     decr.decrypt(by, bOut2);
                  }
                  byte[] bTemp = bOut2.toByteArray();
                  byte[] baInner = new byte[8];
                  System.arraycopy(bTemp, 0, baInner, 0, 4);
                  System.arraycopy(bTemp, bTemp.length - 4, baInner, 4, 4);
                  int innerDataLength = bTemp.length - 8;

                  for (int i = 0; i < 8; i++)
                     baInner[i] ^= (byte) ((1 << i) & innerDataLength);

                  padLen = baInner[0];

                  secretKey.setSecret(baInner);
                  try(ByteArrayOutputStream bOut3 = new ByteArrayOutputStream())
                  {
                     try(ByteArrayInputStream bin = new ByteArrayInputStream(bTemp, 4, innerDataLength)) {
                        decr.decrypt(bin, bOut3);
                     }
                  }
               }
            }

            String ret = bOut.toString();
            // pad must be between 1 and 7 bytes, fix for bug id Rx-99-11-0049
            if ((padLen > 0) & (padLen  < 8))
               ret = ret.substring(0, ret.length() - padLen);

            return ret;
         }
      catch (Exception e)
      {
         // we were returning null which caused a decryption error downstream
         // now we return ""
         return "";
      }
   }

   /**
    * Encrypts the specified string using the "lasagna" method.  Requires two
    * keys that will be used to decrypt the string later.
    *
    * @param key1 The first key used to encrypt the string.  May not be
    * <code>null</code> or empty.
    * @param key2 The second key used to encrypt the string.  May not be
    * <code>null</code> or empty.
    * @param str The string to encrypt.  If <code>null</code> or empty, an
    * empty string is returned.
    *
    * @return The encrypted string, never <code>null</code>, may be empty.
    *
    * @throws IllegalArgumentException if <code>key1</code> or <code>key2</code>
    * is <code>null</code> or empty.
    */
   @Deprecated
   public static String encrypt(String key1, String key2, String str)
   {
      if (key1 == null || key1.trim().length() == 0)
         throw new IllegalArgumentException("key1 may not be null or empty");

      if (key2 == null || key2.trim().length() == 0)
         throw new IllegalArgumentException("key2 may not be null or empty");

      if ((str == null) || (str.equals("")))
         return "";

      try {

         IPSKey key = PSEncryptionKeyFactory.getKeyGenerator(PSEncryptionKeyFactory.DES_ALGORITHM);
         byte[] encrData = str.getBytes(StandardCharsets.UTF_8);

         if ((key != null) && (key instanceof IPSSecretKey))
         {
            IPSSecretKey secretKey = (IPSSecretKey)key;

            int partone = key1.hashCode();
            int parttwo = key2.hashCode();

            partone /= 7;
            parttwo /= 13;

            long time = new Date().getTime();
            byte[] baInner = new byte[8];
            for (int i = 0; i < 8; i++)
               baInner[i] = (byte)((time >> i) & 0xFF);
            baInner[0] = (byte)(8 - (encrData.length % 8));

            secretKey.setSecret(baInner);

            IPSEncryptor encr = secretKey.getEncryptor();
            try(ByteArrayOutputStream bOut = new ByteArrayOutputStream()) {
               try(ByteArrayInputStream bis = new ByteArrayInputStream(encrData) ) {
                  encr.encrypt(bis, bOut);
               }
               encrData = bOut.toByteArray();
               int innerDataLength = encrData.length;

               for (int i = 0; i < 8; i++)
                  baInner[i] ^= (byte) ((1 << i) & innerDataLength);

               byte[] outerData = new byte[baInner.length + innerDataLength];

               System.arraycopy(baInner, 0, outerData, 0, 4);
               System.arraycopy(encrData, 0, outerData, 4, innerDataLength);
               System.arraycopy(baInner, 4, outerData, innerDataLength + 4, 4);

               byte[] baOuter = new byte[8];
               for (int i = 0; i < 4; i++)
                  baOuter[i] = (byte) ((partone >> i) & 0xFF);
               for (int i = 4; i < 8; i++)
                  baOuter[i] = (byte) ((parttwo >> (i - 4)) & 0xFF);

               secretKey.setSecret(baOuter);
               try(ByteArrayOutputStream bOut2 = new ByteArrayOutputStream()) {
                  try(ByteArrayInputStream bis = new ByteArrayInputStream(outerData)) {
                     encr.encrypt(bis, bOut2);
                  }
                  encrData = bOut2.toByteArray();
               }

            }
         }
            byte[] bOut2 = Base64.getMimeEncoder().encode(encrData);
            return new String(bOut2,StandardCharsets.UTF_8);

      } catch (Exception e) {
         return null;
      }
   }
}
