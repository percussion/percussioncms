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
import com.percussion.security.IPSSecretKey;

import javax.crypto.SecretKey;

/**
 * The PSDESKey class is used to store the specified key as a DES key.
 * DES uses a 64-bit key, which is encoded into a 56-bit value used
 * in the algorithm for encryption/decryption. For security, the original
 * (non-encoded) version is not stored with this class.
 * <P>
 * DES is described in
 * <A HREF="http://www.itl.nist.gov/fipspubs/fip46-2.htm">FIPS 46-2</A>.
 * The DES modes of operation are described in
 * <A HREF="http://www.itl.nist.gov/fipspubs/fip81.htm">FIPS 81</A>.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 * @deprecated
 */
@Deprecated
public class PSDESKey implements IPSSecretKey
{
   /**
    * Construct a DES key with no associated secret. Be sure to call
    * setSecret when using this constructor.
    */
   public PSDESKey()
   {
      super();
   }

   /**
    * Construct a DES key with the specified secret value.
    *
    * @param      secret   a 64-bit (8 byte) value to generate the key
    *
    * @exception   IllegalArgumentException
    *                        if <code>secret</code> is <code>null</code> or not
    *                        a 64-bit value.
    */
   public PSDESKey(byte[] secret) throws IllegalArgumentException
   {
      super();
      setSecret(secret);
   }


   /* **************  IPSSecretKey Interface Implementation ************** */

   /**
    * Get the number of bits required for this secret key.
    *
    * @return               the number of bits to use in setSecret
    */
   public int getSecretSizeInBits()
   {
      return 64;   // 64 bit secret
   }


   /**
    * Set the secret to the specified byte array. It must have the
    * appropriate number of bytes to match the bit count returned by
    * getSecretSizeInBits.
    *
    * @param      secret   the secret to use to generate the key
    *
    * @exception   IllegalArgumentException
    *                        if the secret is invalid for this object
    */
   public void setSecret(byte[] secret) throws IllegalArgumentException
   {
      if ((secret == null) || (secret.length != 8))
         throw new IllegalArgumentException(
               "SECRET_KEY_INVALID_SIZE: expected 8, received " +
               ((secret == null) ? 0 : secret.length) );


      // convert to a bit array which stores these 64 bits in sequence
      byte[] maskArray =  {(byte)0x7F, (byte)0x40, (byte)0x20, (byte)0x10,
                           (byte)0x08, (byte)0x04, (byte)0x02, (byte)0x01};

      int[] bitArray = new int[(maskArray.length)*(secret.length)];

      int element = 0;
      for (int j = 0; j < secret.length; j++){
         for (int k = 0; k < maskArray.length; k++){
            if (k == 0){
               if ((secret[j] | maskArray[k]) == maskArray[k])
                  bitArray[element] = 0;
               else
                  bitArray[element] = 1;
               element++;
            }
            else{
               if ((secret[j] & maskArray[k]) == 0)
                  bitArray[element] = 0;
               else
                  bitArray[element] = 1;
               element++;
            }
         }
      }

      // transform secret into 56-bit version
      for (int i = 0; i < permutedChoiceOneLen; i++){
         m_permutedChoiceOne[i] = bitArray[permutedChoiceOneSetting[i]-1];
      }

      // store key for later use
      int temp = 0;
      int temp0 = 0;
      Integer oneInteger;
      for (int i = 0; i < 7; i++){
         temp = 8*i;
         temp0 = ((m_permutedChoiceOne[temp]  <<7)+(m_permutedChoiceOne[temp+1]<<6) +
                  (m_permutedChoiceOne[temp+2]<<5)+(m_permutedChoiceOne[temp+3]<<4) +
                  (m_permutedChoiceOne[temp+4]<<3)+(m_permutedChoiceOne[temp+5]<<2) +
                  (m_permutedChoiceOne[temp+6]<<1)+ m_permutedChoiceOne[temp+7]);
         oneInteger = new Integer(temp0);
         m_encodedKey[i] = oneInteger.byteValue();
      }

      arrangeCDArrayElements();
   }

   /**
    * Generates a new key
    *
    * @return a new SecretKey or null
    */
   @Override
   public SecretKey generateKey() {
      return null;
   }


   /* *****************  IPSKey Interface Implementation ***************** */

   /**
    * Generate an IPSEncryptor object which can make use of this key.
    *
    * @return            the associated encryptor
    */
   public IPSEncryptor getEncryptor()
   {
      try {
         return new PSDESEncryptor(this);
      } catch (IllegalArgumentException e)   {
         // only throws on null key, which clearly can't happen here
         return null;
      }
   }

   /**
    * Generate an IPSDecryptor object which can make use of this key.
    *
    * @return            the associated decryptor
    */
   public IPSDecryptor getDecryptor()
   {
      try {
         return new PSDESDecryptor(this);
      } catch (IllegalArgumentException e)   {
         // only throws on null key, which clearly can't happen here
         return null;
      }
   }

   /**
    * Returns a byte aray containing the secret key
    *
    * @return
    */
   @Override
   public byte[] getSecret() {
      return new byte[0];
   }


   /**
    * Permuted Choice 1 (PC-1) bit position setting array.
    */
   static final int[] permutedChoiceOneSetting = {  // 56 elements
            57, 49, 41, 33, 25, 17,  9,
             1, 58, 50, 42, 34, 26, 18,
            10,  2, 59, 51, 43, 35, 27,
            19, 11,  3, 60, 52, 44, 36,
            63, 55, 47, 39, 31, 23, 15,
             7, 62, 54, 46, 38, 30, 22,
            14,  6, 61, 53, 45, 37, 29,
            21, 13,  5, 28, 20, 12,  4
   };   // position starts from 1, not 0

   /**
    * Permuted Choice 2 (PC-2) bit position setting array.
    */
   static final int[] permutedChoiceTwoSetting = {  // 48 elements
            14, 17, 11, 24,  1,  5,
             3, 28, 15,  6, 21, 10,
            23, 19, 12,  4, 26,  8,
            16,  7, 27, 20, 13,  2,
            41, 52, 31, 37, 47, 55,
            30, 40, 51, 45, 33, 48,
            44, 49, 39, 56, 34, 53,
            46, 42, 50, 36, 29, 32
   };   // position starts from 1, not 0

   /**
    * The length of the Permuted Choice 1 (PC-1) array.
    */
   static final int permutedChoiceOneLen = permutedChoiceOneSetting.length;

   /**
    * The length of the Permuted Choice 2 (PC-2) array.
    */
   static final int permutedChoiceTwoLen = permutedChoiceTwoSetting.length;

   /**
    * The required iteration number.
    */
   static final int IterationSetting = 16;

   /**
    * Get the encoded key value -- that is, the 56-bit version used by this
    * algorithm.
    *
    * @return      the encoded key
    */
   public byte[] getEncodedKey()
   {
      return m_encodedKey;
   }

   /**
    * Get the array storing each bit of the 56-bit encoded key
    *
    * @return      the array storing the bits
    */
   public int[] getEncodedKeyBitArray()
   {
      return m_permutedChoiceOne;
   }

   /**
    * Get the K array based on the iteration index. The index starts from 1
    * and ends at the limit of IterationSetting
    *
    * @param   index      the iteration index
    *
    * @return            the array K
    *
    * @exception         IllegalArgumentException
    */
   int[] getKArray(int index) throws IllegalArgumentException
   {
      if ((index < 1) || (index > IterationSetting))
         throw new IllegalArgumentException("index out of bounds");

      switch(index){
         case 1:
            return m_K1;
         case 2:
            return m_K2;
         case 3:
            return m_K3;
         case 4:
            return m_K4;
         case 5:
            return m_K5;
         case 6:
            return m_K6;
         case 7:
            return m_K7;
         case 8:
            return m_K8;
         case 9:
            return m_K9;
         case 10:
            return m_K10;
         case 11:
            return m_K11;
         case 12:
            return m_K12;
         case 13:
            return m_K13;
         case 14:
            return m_K14;
         case 15:
            return m_K15;
         case 16:
            return m_K16;
      }

      return m_K1;  // won't happen here
   }

   /**
    * Store all the C[0] and D[0] elements.
    */
   private void arrangeCDArrayElements()
   {
      int len = permutedChoiceOneLen/2;  // 28 elements
      for (int i = 0; i < len; i++){
         m_cArray[i] = m_permutedChoiceOne[i];
         m_dArray[i] = m_permutedChoiceOne[i+len];
      }

      int[] cdArray = new int[permutedChoiceOneLen];     // 56 elements

      int[] tempDArray = new int[len];
      int[] tempCArray = new int[len]; // 28 elements

      int[] srcCArray = m_cArray;
      int[] srcDArray = m_dArray;
      int[] dstCArray = tempCArray;
      int[] dstDArray = tempDArray;

      for (int index = 1; index <= 16; index++){

           for (int i = 0; i < len-1; i++){
              dstDArray[i] = srcDArray[i+1];
              dstCArray[i] = srcCArray[i+1];
           }
           dstDArray[len-1] = srcDArray[0];
         dstCArray[len-1] = srcCArray[0];

         srcCArray = dstCArray;
           dstCArray = (srcCArray == m_cArray) ? tempCArray : m_cArray;
         srcDArray = dstDArray;
           dstDArray = (srcDArray == m_dArray) ? tempDArray : m_dArray;

         switch(index){
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
              for (int i = 0; i < len-1; i++){
                 dstDArray[i] = srcDArray[i+1];
                 dstCArray[i] = srcCArray[i+1];
              }
              dstDArray[len-1] = srcDArray[0];
            dstCArray[len-1] = srcCArray[0];

            srcCArray = dstCArray;
              dstCArray = (srcCArray == m_cArray) ? tempCArray : m_cArray;
            srcDArray = dstDArray;
              dstDArray = (srcDArray == m_dArray) ? tempDArray : m_dArray;

            break;
         }

         for (int i = 0; i < len; i++){
            cdArray[i]       = srcCArray[i];
            cdArray[i+len] = srcDArray[i];
         }

         int[] kArray = new int[permutedChoiceTwoLen];
         for (int i = 0; i < permutedChoiceTwoLen; i++)
            kArray[i] = cdArray[permutedChoiceTwoSetting[i]-1];

         switch(index){
         case 1:
            m_K1 = kArray;
            break;
         case 2:
            m_K2 = kArray;
            break;
         case 3:
            m_K3 = kArray;
            break;
         case 4:
            m_K4 = kArray;
            break;
         case 5:
            m_K5 = kArray;
            break;
         case 6:
            m_K6 = kArray;
            break;
         case 7:
            m_K7 = kArray;
            break;
         case 8:
            m_K8 = kArray;
            break;
         case 9:
            m_K9 = kArray;
            break;
         case 10:
            m_K10 = kArray;
            break;
         case 11:
            m_K11 = kArray;
            break;
         case 12:
            m_K12 = kArray;
            break;
         case 13:
            m_K13 = kArray;
            break;
         case 14:
            m_K14 = kArray;
            break;
         case 15:
            m_K15 = kArray;
            break;
         case 16:
            m_K16 = kArray;
            break;
         }
      }
   }

   // the 56-bit version of the key
   private byte[] m_encodedKey = new byte[permutedChoiceOneLen/8];

   // the bit array storing bits of m_encodedKey byte array
   private int[] m_permutedChoiceOne = new int[permutedChoiceOneLen];
   private int[] m_cArray = new int[permutedChoiceOneLen/2];  // 28 elements
   private int[] m_dArray = new int[permutedChoiceOneLen/2];  // 28 elements

   private int[] m_K1   = new int[permutedChoiceTwoLen];     // 48 elements
   private int[] m_K2   = new int[permutedChoiceTwoLen];
   private int[] m_K3   = new int[permutedChoiceTwoLen];
   private int[] m_K4   = new int[permutedChoiceTwoLen];
   private int[] m_K5   = new int[permutedChoiceTwoLen];
   private int[] m_K6   = new int[permutedChoiceTwoLen];
   private int[] m_K7   = new int[permutedChoiceTwoLen];
   private int[] m_K8   = new int[permutedChoiceTwoLen];
   private int[] m_K9   = new int[permutedChoiceTwoLen];
   private int[] m_K10  = new int[permutedChoiceTwoLen];
   private int[] m_K11  = new int[permutedChoiceTwoLen];
   private int[] m_K12  = new int[permutedChoiceTwoLen];
   private int[] m_K13  = new int[permutedChoiceTwoLen];
   private int[] m_K14  = new int[permutedChoiceTwoLen];
   private int[] m_K15  = new int[permutedChoiceTwoLen];
   private int[] m_K16  = new int[permutedChoiceTwoLen];
}

