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

import com.percussion.security.PSAbstractEncryptor;

import com.percussion.security.IPSEncryptor;
import com.percussion.security.IPSKey;
import com.percussion.security.IPSSecretKey;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility class to encrypt and decrypt strings using the Blowfish cipher with a 
 * secret shared key.
 */
@Deprecated
public class PSLegacyEncrypter extends PSAbstractEncryptor {

   private String keyLocation;

   @Deprecated
   public String OLD_SECURITY_KEY(){
      try {
         return PSEncryptor.getInstance("AES",keyLocation).decryptLegacyKey(
                 "yDn5JmFMP1SaPwBl/SxFwuPyBJw/wBcCC9p0FOdvbsaz9cKI0LqeEGumslkOr3yA7aZ/r+o3lLhSWukw");
      } catch (PSEncryptionException e) {
         return "";
      }
   }

   @Deprecated
   public String OLD_SECURITY_KEY2(){
      try {
         return PSEncryptor.getInstance("AES",keyLocation).decryptLegacyKey(
                 "aMzoXMZx1Aqt82TlwLYVAZiBIUbZJdFA4RtWj5a76WXKXxiXLRf2VAO3JmkorXDKn/Sug+L6isxeQOG7Zduytg6jRCuJwaVK");
      } catch (PSEncryptionException e) {
         return "";
      }
   }
   /**
    * Private ctor to force static use.
    */
   private PSLegacyEncrypter(String keyLocation)
   {
      this.keyLocation = keyLocation;
   }

   private static PSLegacyEncrypter instance;

   /**
    *  Simgleton accessor to get a legacy encryptor
    * @return Instance of PSLegacyEncrypter
    */
   public static PSLegacyEncrypter getInstance(String keyLocation){
      synchronized (PSLegacyEncrypter.class) {
         if (instance == null) {
            instance = new PSLegacyEncrypter(keyLocation);
         }
         return instance;
      }
   }

   /**
    * Sizes the key to be correct for the cipher being used.  For Blowfish, it
    * must be a multiple of 8, and can only range from 32 to 448, inclusive.
    *   
    * @param keyBytes The key to size, assumed not <code>null</code>.
    * 
    * @return The sized key.
    */
   @Deprecated
   private static byte[] sizeKey(byte[] keyBytes)
   {
      byte[] newKey;

      int keyLen = keyBytes.length;
      if (keyLen < KEY_LEN)
      {
         // pad using the supplied key bytes as many times as necessary
         int newLen = KEY_LEN;
         newKey = new byte[newLen];
         int i = 0;
         while (i < newLen)
         {
            int len = keyLen;
            int remainder = newLen - i;
            if (remainder < len)
               len = remainder;
            System.arraycopy(keyBytes, 0, newKey, i, len);
            i += len;
         }
      }
      else if (keyLen > KEY_LEN)
      {
         // truncate
         newKey = new byte[KEY_LEN];
         System.arraycopy(keyBytes, 0, newKey, 0, newKey.length);
      }
      else
         newKey = keyBytes;
      
      return newKey;
   }

   /**
    * Returns the key value to use as part one with the Rhythmyx encryption
    * algorithm.
    *
    * @return The key, never <code>null</code> or empty.
    */
   public String getPartOneKey()
   {
      // get the encrypted constant and decrypt.
      return rot13(PART_ONE());
   }
   
   /**
    * Returns the key value to use as part two with the Rhythmyx encyrption
    * algorithm.
    *
    * @return The key, never <code>null</code> or empty.
    */
   public String getPartTwoKey()
   {
      // get the encrypted constant and decrypt.
      return rot13(PART_TWO());
   }

   /**
    * Encrypts the supplied String using the rot13 algorithm on each character.
    *
    * @param val The value to encrypt.  Assumed not <code>null</code> or empty.
    *
    * @return The encrypted string, never <code>null</code> or empty.
    */
   @Deprecated
   private static String rot13(String val)
   {
      StringBuffer buf = new StringBuffer(val);
      for (int i = 0; i < buf.length(); i++)
      {
         buf.setCharAt(i, rot13(buf.charAt(i)));
      }

      return buf.toString();
   }
   
   /**
    * Encrypts the supplied char using the rot13 algorithm
    * @param ch The char to encrypt.
    * @return The encrypted char.
    */
   @Deprecated
   private static char rot13(char ch)
   {
      char encrypted = ch;
      if (Character.isLetter(ch))
      {
         if (Character.isUpperCase(ch))
            encrypted =  (char) (((ch - 'A') + 13) % 26 + 'A');
         else
            encrypted = (char) (((ch - 'a') + 13) % 26 + 'a');
      }
      return encrypted;
   }
   
   /**
    * The name of the cipher used for encryption.
    */
   @Deprecated
   private static final String CIPHER = "Blowfish";
      
   /**
    * Radix to use for String to int conversion.
    */
   private static final int RADIX = 16;
   
   /**
    * Length in bytes of the secret key byte array. 
    */
   private static final int KEY_LEN = 15;
   
   /**
    * The constant for the partone key for the Rx encryption algorithm.  The
    * constant is encrypted by the {@link #rot13(char)} method.
    */
   @Deprecated
   public String PART_ONE(){
      try {
         return PSEncryptor.getInstance("AES",keyLocation).decryptLegacyKey(
                 "o4yH4E0BDOYjzPe4zQTLd26W9DGDzYWGVZpjEPqpBM9Na8XRwSBHgz7bMMvvttjUrg/+XcnPuGIsuFBe");
      } catch (PSEncryptionException e) {
         return "";
      }
   }
   
   /**
    * The constant for the parttwo key for the Rx encryption algorithm.  The
    * constant is encrypted by the {@link #rot13(char)} method.
    */
   @Deprecated
   public String PART_TWO(){
      try {
         return PSEncryptor.getInstance("AES",keyLocation).decryptLegacyKey(
                 "UnZzJip+dJlzilT55AjJSVceUEXCcqsIz+OPnpCawnGJBkCGaASrw1JbxXLEIhnjB4Q6cdyuKqRVZmuo/qZt5vXYhYz1GAzx");
      } catch (PSEncryptionException e) {
         return "";
      }
   }

   /**
    * The length of the byte array passed to the cipher encryption object for
    * decryption in {@link #decrypt(String, String, PSAbstractEncryptor)} must be a multiple of this
    * value.  Used in {@link #toByteArray(BigInteger)}.
    */
   public static int BYTE_ARRAY_MULTIPLE = 8;

   /**
    * Encrypts the provided string using the supplied secret key
    *
    * @param str The string to encrypt, may not be <code>null</code>, may be
    * empty.
    * @param key The secret key to encrypt the string, may not be
    * <code>null</code> or empty.
    *
    * @return The encrypted string, never <code>null</code>, may be empty.
    */
   @Deprecated
   @Override
   public String encrypt(String str, String key)
   {
      if (StringUtils.isBlank(key))
         throw new IllegalArgumentException(
                 "key may not be null or empty");

      if (str == null)
         throw new IllegalArgumentException("str may not be null");

      byte[] kbytes = PSLegacyEncrypter.sizeKey(key.getBytes());

      Cipher cipher;
      try
      {
         SecretKeySpec secretKey = new SecretKeySpec(kbytes, PSLegacyEncrypter.CIPHER);
         cipher = Cipher.getInstance(PSLegacyEncrypter.CIPHER);
         cipher.init(Cipher.ENCRYPT_MODE, secretKey);
         byte[] encoding = cipher.doFinal(str.getBytes());
         BigInteger n = new BigInteger(encoding);
         return n.toString(PSLegacyEncrypter.RADIX);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   } /**
    * Decrypts the provided string using the supplied secret key.
    *
    * @param str The string to decrypt, may not be <code>null</code>, may be
    * empty.
    * @param key The secret key that was used to encrypt the string, may not
    * be <code>null</code> or empty.
    *
    * @param legacyDecryptor
     * @return The decrypted string, never <code>null</code>, may be empty.
    */
   @Deprecated
   @Override
   public String decrypt(String str, String key, PSAbstractEncryptor legacyDecryptor)
   {
      if (key == null)
         throw new IllegalArgumentException("key may not be null");

      if (StringUtils.isBlank(key))
         throw new IllegalArgumentException("key may not be null or empty");
      try
      {
         byte[] kbytes = PSLegacyEncrypter.sizeKey(key.getBytes());
         SecretKeySpec secretKey = new SecretKeySpec(kbytes, PSLegacyEncrypter.CIPHER);
         BigInteger n = new BigInteger(str, PSLegacyEncrypter.RADIX);
         byte[] encoding = toByteArray(n);
         Cipher cipher = Cipher.getInstance(PSLegacyEncrypter.CIPHER);
         cipher.init(Cipher.DECRYPT_MODE, secretKey);
         byte[] decode = cipher.doFinal(encoding);
         return new String(decode);
      } catch (NumberFormatException ex) {
         // Assuming not an encrypted password returning original string
         // Caller can check original equals result to see if it was not encrypted
         return str;
      }
      catch (Exception e)
      {

         throw new RuntimeException(e);
      }
   } /**
    * Converts a <code>BigInteger</code> to a byte array whose size is a
    * multiple of {@link #BYTE_ARRAY_MULTIPLE}.  For positive values or a value
    * of zero, the byte array is padded with leading 0 byte values if necessary.
    * For negative values, the byte array is padded with leading -1 byte values
    * if necessary.
    *
    * @param bigInt The <code>BigInteger</code> to convert, may not be
    * <code>null</code>.
    *
    * @return A valid byte array form of a <code>BigInteger</code> with a size
    * which is a multiple of BYTE_ARRAY_MULTIPLE.
    */
   @Deprecated
   @Override
   public byte[] toByteArray(BigInteger bigInt)
   {
      if (bigInt == null)
      {
         throw new IllegalArgumentException("bigInt may not be null");
      }

      int signum = bigInt.signum();
      byte[] bytes = bigInt.toByteArray();
      byte pad;

      if (signum >= 0)
         pad = (byte) 0x00;
      else
         pad = (byte) 0xFF;

      while ((bytes.length % BYTE_ARRAY_MULTIPLE) != 0)
      {
         byte[] temp = new byte[bytes.length + 1];
         temp[0] = pad;
         System.arraycopy(bytes, 0, temp, 1, bytes.length);
         bytes = temp;
      }

      return bytes;
   }

   @Deprecated
   @Override
   public String encryptCredentials(String uid, String pw) {
      if ((pw == null) || (pw.equals("")))
         return "";

      try {
         IPSKey key = new PSDESKey();

         byte[] encrData = pw.getBytes(StandardCharsets.UTF_8);

         if ((key != null) && (key instanceof IPSSecretKey))
         {
            IPSSecretKey secretKey = (IPSSecretKey)key;

            int partone = OLD_SECURITY_KEY().hashCode();
            int parttwo;
            if (uid == null || uid.equals(""))
               parttwo = OLD_SECURITY_KEY2().hashCode();
            else
               parttwo = uid.hashCode();

            partone /= 7;
            parttwo /= 13;

            long time = new java.util.Date().getTime();
            byte[] baInner = new byte[8];
            for (int i = 0; i < 8; i++)
               baInner[i] = (byte)((time >> i) & 0xFF);
            baInner[0] = (byte)(8 - (encrData.length % 8));

            secretKey.setSecret(baInner);

            IPSEncryptor encr = secretKey.getEncryptor();
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            encr.encrypt(new ByteArrayInputStream(encrData), bOut);
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
               baOuter[i] = (byte)((partone >> i) & 0xFF);
            for (int i = 4; i < 8; i++)
               baOuter[i] = (byte)((parttwo >> (i-4)) & 0xFF);

            secretKey.setSecret(baOuter);
            bOut = new ByteArrayOutputStream();
            encr.encrypt(new ByteArrayInputStream(outerData), bOut);

            encrData = bOut.toByteArray();
            bOut.close();
         }

         /* Base 64 encode and return ... */
         ByteArrayOutputStream bOut2 = null;
         try {
            return  Base64.getEncoder().encodeToString(encrData);
         } catch (Exception e) {
            return null;
         }
      } catch (Exception e) {
         return null;
      }
   }

   /**
    * Decrypts the specified credentials using the supplied password.
    *
    * @param encrypted The encrypted credentials
    * @param pw        The password to use for decryption.
    * @return
    * @throws PSEncryptionException
    */
   @Override
   public String decryptCredentials(String encrypted, String pw) throws PSEncryptionException {
      throw new PSEncryptionException("Not implemented");
   }

   /**
    * Encryption seed key.
    */
   @Deprecated
   public String CRYPT_KEY() {
      try {
         return PSEncryptor.getInstance("AES",keyLocation).decryptLegacyKey("RapTr6hOh1KUXM5I0gvXYnaQKJDcO9AqlswePsbjnCHGQLk3H9ubfJU4VUIIvg==");
      } catch (PSEncryptionException e) {
         return "";
      }
   }

   @Deprecated
   public String EMAIL_CRYPT_KEY(){
      try {
         return PSEncryptor.getInstance("AES", keyLocation).decryptLegacyKey("zDLBBy28QpbQ1Mjy2J+9MCJ/tFDkUydWTM9EuPM+MpXAWO1sKQR5");
      } catch (PSEncryptionException e) {
         return "";
      }
   }

   @Deprecated
   public String DEFAULT_KEY(){
      try {
         return PSEncryptor.getInstance("AES",keyLocation).decryptLegacyKey(
                 "sFBQwCU1XnWZy8W16PSwpu9fe0/XVTmSLsj4HTFfV57dyY8c0zWN");
      } catch (PSEncryptionException e) {
         return "";
      }
   }

   @Deprecated
   public String INVALID_DRIVER() {
      try {
         return PSEncryptor.getInstance("AES",keyLocation).decryptLegacyKey(
                 "gQaiSMUCDZXMTYvS/vdGZz51pixCaAFG2rJxHXssWv9wNIR/z3Z52fZa9K7ddkZx5upOGV+Qjp9zV+Sk+Y5Yz0etpmPa/Ges");
      } catch (PSEncryptionException e) {
         return "";
      }
   }

   @Deprecated
   public String INVALID_CRED() {
      try {
         return PSEncryptor.getInstance("AES", keyLocation).decryptLegacyKey(
                 "cmMJ9SGt/S4gV5nUhN/c+BCZKiJyekTJhpHeKW4ISnTmuBVcbrR6nmVf7ELk6EUKnm64splClClCBRET");
      } catch (PSEncryptionException e) {
         return "";
      }
   }

}