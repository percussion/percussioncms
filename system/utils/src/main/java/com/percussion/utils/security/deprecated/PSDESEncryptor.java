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

import com.percussion.utils.security.IPSEncryptor;
import com.percussion.utils.security.PSEncryptionException;
import com.percussion.utils.security.ToDoVulnerability;
import com.percussion.util.PSCharSetsConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;

/**
 * The PSDESEncryptor class is used to encrypt data using the specified
 * DES key. DES uses a 64-bit key, which is encoded into a 56-bit value used
 * in the algorithm for encryption/decryption.
 * <P>
 * DES is described in
 * <A HREF="http://www.itl.nist.gov/fipspubs/fip46-2.htm">FIPS 46-2</A>.
 * The DES modes of operation are described in
 * <A HREF="http://www.itl.nist.gov/fipspubs/fip81.htm">FIPS 81</A>.
 *
 * @see         PSDESKey
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
@Deprecated
@ToDoVulnerability
public class PSDESEncryptor implements IPSEncryptor
{
   /**
    * Construct a DES encryptor using the specified DES key.
    *
    * @param      key      the DES key to use for encryption
    *
    * @throws IllegalArgumentException if <code>key</code> is <code>null</code>
    */
   public PSDESEncryptor(PSDESKey key) throws IllegalArgumentException
   {
      if (key == null)
         throw new IllegalArgumentException("key cannot be null");

      // store key for later use
      m_key = key;
   }

   /**
    * The initial permutation (IP) bit position setting array.
    */
   public static final int[] InitialPermutation = {  // 64 elements
         58, 50, 42, 34, 26, 18, 10,  2,
         60, 52, 44, 36, 28, 20, 12,  4,
         62, 54, 46, 38, 30, 22, 14,  6,
         64, 56, 48, 40, 32, 24, 16,  8,
         57, 49, 41, 33, 25, 17,  9,  1,
         59, 51, 43, 35, 27, 19, 11,  3,
         61, 53, 45, 37, 29, 21, 13,  5,
         63, 55, 47, 39, 31, 23, 15,  7
   };  // position index starts from 1, not 0

   /**
    * The final permutation (IP inverse) bit position setting array.
    */
   public static final int[] inverseIPArray = {  // 64 elements
         40,  8, 48, 16, 56, 24, 64, 32,
         39,  7, 47, 15, 55, 23, 63, 31,
         38,  6, 46, 14, 54, 22, 62, 30,
         37,  5, 45, 13, 53, 21, 61, 29,
         36,  4, 44, 12, 52, 20, 60, 28,
         35,  3, 43, 11, 51, 19, 59, 27,
         34,  2, 42, 10, 50, 18, 58, 26,
         33,  1, 41,  9, 49, 17, 57, 25
   };  // position index starts from 1, not 0

   /**
    * The Expansion (E) bit position setting array.
    */
   public static final int[] EArray = {  // 48 elements
         32,  1,  2,  3,  4,  5,
          4,  5,  6,  7,  8,  9,
          8,  9, 10, 11, 12, 13,
         12, 13, 14, 15, 16, 17,
         16, 17, 18, 19, 20, 21,
         20, 21, 22, 23, 24, 25,
         24, 25, 26, 27, 28, 29,
         28, 29, 30, 31, 32,  1
   };  // position index starts from 1, not 0

   /**
    * The Permutation (P) bit position setting array.
    */
   public static final int[] PArray = {  // 32 elements
         16,  7, 20, 21,
         29, 12, 28, 17,
          1, 15, 23, 26,
          5, 18, 31, 10,
          2,  8, 24, 14,
         32, 27,  3,  9,
         19, 13, 30,  6,
         22, 11,  4, 25
   };  // position index starts from 1, not 0

   public static final int initialPermLen = InitialPermutation.length;
   public static final int eArrayLen = EArray.length;
   public static final int pArrayLen = PArray.length;

   public static final int[][] substitutionBox1 = {    // S[1]
      {14,  4, 13,  1,  2, 15, 11,  8,  3, 10,  6, 12,  5,  9,  0,  7},
      { 0, 15,  7,  4, 14,  2, 13,  1, 10,  6, 12, 11,  9,  5,  3,  8},
      { 4,  1, 14,  8, 13,  6,  2, 11, 15, 12,  9,  7,  3, 10,  5,  0},
      {15, 12,  8,  2,  4,  9,  1,  7,  5, 11,  3, 14, 10,  0,  6, 13},
   };

   public static final int[][] substitutionBox2 = {    // S[2]
      {15,  1,  8, 14,  6, 11,  3,  4,  9,  7,  2, 13, 12,  0,  5, 10},
      { 3, 13,  4,  7, 15,  2,  8, 14, 12,  0,  1, 10,  6,  9, 11,  5},
      { 0, 14,  7, 11, 10,  4, 13,  1,  5,  8, 12,  6,  9,  3,  2, 15},
      {13,  8, 10,  1,  3, 15,  4,  2, 11,  6,  7, 12,  0,  5, 14,  9},
   };

   public static final int[][] substitutionBox3 = {    // S[3]
      {10,  0,  9, 14,  6,  3, 15,  5,  1, 13, 12,  7, 11,  4,  2,  8},
      {13,  7,  0,  9,  3,  4,  6, 10,  2,  8,  5, 14, 12, 11, 15,  1},
      {13,  6,  4,  9,  8, 15,  3,  0, 11,  1,  2, 12,  5, 10, 14,  7},
      { 1, 10, 13,  0,  6,  9,  8,  7,  4, 15, 14,  3, 11,  5,  2, 12},
   };

   public static final int[][] substitutionBox4 = {    // S[4]
      { 7, 13, 14,  3,  0,  6,  9, 10,  1,  2,  8,  5, 11, 12,  4, 15},
      {13,  8, 11,  5,  6, 15,  0,  3,  4,  7,  2, 12,  1, 10, 14,  9},
      {10,  6,  9,  0, 12, 11,  7, 13, 15,  1,  3, 14,  5,  2,  8,  4},
      { 3, 15,  0,  6, 10,  1, 13,  8,  9,  4,  5, 11, 12,  7,  2, 14},
   };

   public static final int[][] substitutionBox5 = {    // S[5]
      { 2, 12,  4,  1,  7, 10, 11,  6,  8,  5,  3, 15, 13,  0, 14,  9},
      {14, 11,  2, 12,  4,  7, 13,  1,  5,  0, 15, 10,  3,  9,  8,  6},
      { 4,  2,  1, 11, 10, 13,  7,  8, 15,  9, 12,  5,  6,  3,  0, 14},
      {11,  8, 12,  7,  1, 14,  2, 13,  6, 15,  0,  9, 10,  4,  5,  3},
   };

   public static final int[][] substitutionBox6 = {    // S[6]
      {12,  1, 10, 15,  9,  2,  6,  8,  0, 13,  3,  4, 14,  7,  5, 11},
      {10, 15,  4,  2,  7, 12,  9,  5,  6,  1, 13, 14,  0, 11,  3,  8},
      { 9, 14, 15,  5,  2,  8, 12,  3,  7,  0,  4, 10,  1, 13, 11,  6},
      { 4,  3,  2, 12,  9,  5, 15, 10, 11, 14,  1,  7,  6,  0,  8, 13},
   };

   public static final int[][] substitutionBox7 = {    // S[7]
      { 4, 11,  2, 14, 15,  0,  8, 13,  3, 12,  9,  7,  5, 10,  6,  1},
      {13,  0, 11,  7,  4,  9,  1, 10, 14,  3,  5, 12,  2, 15,  8,  6},
      { 1,  4, 11, 13, 12,  3,  7, 14, 10, 15,  6,  8,  0,  5,  9,  2},
      { 6, 11, 13,  8,  1,  4, 10,  7,  9,  5,  0, 15, 14,  2,  3, 12},
   };

   public static final int[][] substitutionBox8 = {    // S[8]
      {13,  2,  8,  4,  6, 15, 11,  1, 10,  9,  3, 14,  5,  0, 12,  7},
      { 1, 15, 13,  8, 10,  3,  7,  4, 12,  5,  6, 11,  0, 14,  9,  2},
      { 7, 11,  4,  1,  9, 12, 14,  2,  0,  6, 10, 13, 15,  3,  5,  8},
      { 2,  1, 14,  7,  4, 10,  8, 13, 15, 12,  9,  0,  3,  5,  6, 11},
   };

   /**
    * Encrypt the data in the specified input stream.
    *
    * @param      in            the stream containing the plain text
    *                           representation of the data
    *
    * @param      out         the stream to store the encrypted data
    *
    * @exception   IOException   if an I/O exception occurs
    */
   public void encrypt(InputStream in, OutputStream out) throws PSEncryptionException
   {
      int byteAsInt;

      // in case this is a subsequent call, clear the list
      m_dataList.clear();

      try {
         while ((byteAsInt = in.read()) != -1) {  // read in next byte
            m_dataList.add(new Integer(byteAsInt));
         }

         int dataListSize = m_dataList.size();
         if (dataListSize == 0)
            return;

         int dataBlock = 0;
         if (dataListSize < 8) {  // should have at least 64 bits = 8 bytes
            addPaddings(8 - dataListSize);
            dataBlock = 1;
         } else {
            dataBlock = dataListSize / 8;
            int remain = dataListSize % 8;
            if (remain != 0) {
               dataBlock += 1;
               addPaddings(8 - remain);
            }
         }

         // System.out.println("dataBlock = " + dataBlock);

         for (int block = 0; block < dataBlock; block++) { // encrypt dataBlock one by one
            encryptOneDataBlock(block);
            out.write(m_oneEncodedDataBlock);
         }
         out.flush();
      }catch(IOException e){
         throw new PSEncryptionException(e.getMessage(),e);
      } finally {
         // clean up on exit so we don't consume memory
         m_dataList.clear();
      }
   }

   /**
    * A convenience method to encrypt a String.
    *
    * @param      in            the string containing the plain text
    *                           representation of the data
    *
    * @param      out         the stream to store the encrypted data into
    *
    * @exception   IOException   if an I/O exception occurs
    */
   public void encrypt(String in, OutputStream out) throws PSEncryptionException
   {
      try {
         if (StringUtils.isEmpty(in)) {
            return;
         }

         // convert the input string to a stream of bytes
         ByteArrayInputStream iBuf = new ByteArrayInputStream(
                 in.getBytes(PSCharSetsConstants.rxJavaEnc()));

         // do the encryption
         encrypt(iBuf, out);
      } catch (IOException e) {
        throw new PSEncryptionException(e.getMessage(), e);
      }
   }

   /**
    * A convenience method to encrypt a String and retrieve the
    * resulting byte array.
    *
    * @param      in            the string containing the plain text
    *                           representation of the data
    *
    * @return                  an array of bytes containing the encrypted data
    *

    */
   public byte[] encrypt(String in) throws PSEncryptionException
   {
      try {
         if (StringUtils.isEmpty(in)) {
            return new byte[0];
         }

         // convert the input string to a stream of bytes
         ByteArrayInputStream iBuf = new ByteArrayInputStream(
                 in.getBytes(PSCharSetsConstants.rxJavaEnc()));

         // we'll use a byte array for the output stream
         ByteArrayOutputStream oBuf = new ByteArrayOutputStream();

         // do the encryption
         encrypt(iBuf, oBuf);

         // and return the resulting byte array
         return oBuf.toByteArray();
      }catch(IOException e){
         throw new PSEncryptionException(e.getMessage(),e);
      }
   }

   @Override
   public byte[] encryptWithPassword(String in, String password) throws PSEncryptionException {
      return new byte[0];
   }

   /**
    * Add paddings into the end of data block.
    * @param   needs      number to pad
    */
   private void addPaddings(int needs)
   {
      for (int i = 0; i < needs; i++)
         m_dataList.add(new Integer(0));  // pad zeros
   }

   /**
    * Encrypt one data block based on its input index.
    * @param   blockIndex      the data block index
    */
   private void encryptOneDataBlock(int blockIndex)
   {
      // take out the stored data
      int count = 0;
      byte[] eightBytes = new byte[8];
      for (int i = 8*blockIndex; i < 8*blockIndex+8; i++){
         eightBytes[count] = ((Integer)(m_dataList.get(i))).byteValue();
         count++;
      }

      // convert to a bit array which stores these 64 bits in sequence
      byte[] maskArray =  {(byte)0x7F, (byte)0x40, (byte)0x20, (byte)0x10,
                           (byte)0x08, (byte)0x04, (byte)0x02, (byte)0x01};

      int[] bitArray = new int[(maskArray.length)*(eightBytes.length)];

      int element = 0;
      for (int j = 0; j < eightBytes.length; j++){
         for (int k = 0; k < maskArray.length; k++){
            if (k == 0){
               if ((eightBytes[j] | maskArray[k]) == maskArray[k])
                  bitArray[element] = 0;
               else
                  bitArray[element] = 1;
               element++;
            }
            else{
               if ((eightBytes[j] & maskArray[k]) == 0)
                  bitArray[element] = 0;
               else
                  bitArray[element] = 1;
               element++;
            }
         }
      }

      // obtain L[0] and R[0], step 2.3
      int[] LArray = new int[initialPermLen/2];   // 32 elements
      int[] RArray = new int[initialPermLen/2];
      for (int i = 0; i < initialPermLen/2; i++){
         LArray[i] = bitArray[InitialPermutation[i]-1];
         RArray[i] = bitArray[InitialPermutation[i+initialPermLen/2]-1];
      }

      // apply 16 subkeys to the data block, step 2.4
      applySubKeys(LArray, RArray);
   }

   /**
    * Apply all 16 subkeys (kArray) to the data block.
    * @param   LArray   L[0] bit array
    * @param   RArray   R[0] bit array
    */
   private void applySubKeys(int[] LArray, int[] RArray)
   {
      int[] encryptedBlock = new int[initialPermLen];

      int  iteLimit = m_key.IterationSetting;
      int[] ERArray = new int[eArrayLen];    // 48 elements
      int[] KArray  = new int[eArrayLen];

      int[] bOneToEight = new int[pArrayLen];  // 32 elements
      int[] permutedB   = new int[pArrayLen];

      int[] row = new int[8];
      int[] col = new int[8];

      int rCount = 0;
      int lCount = 0;
      int boxGives;

      int[] tempRArray = new int[initialPermLen/2];   // 32 elements

      // iteration index starts from 1
      for (int ite = 1; ite <= iteLimit; ite++){
         KArray = m_key.getKArray(ite);

         // expand 32-bit R[ite-1] into 48 bits, step 2.4.1
         // exclusive-or ERArray with KArray, step 2.4.2
         for (int i = 0; i < eArrayLen; i++){
            ERArray[i] = RArray[EArray[i]-1];   // note the index!!!
            ERArray[i] = (ERArray[i] == KArray[i]) ? 0 : 1;  // E(R[ite-1]) xor K[ite]
         }

         // step 2.4.3, step 2.4.4.1,  and step 2.4.4.2
         rCount = 0;
         lCount = 0;
         for (int i = 0; i < eArrayLen; i++){
            switch(i){
            // multiply by 2
            case  0: case  3: case  6: case  9: case 12: case 15: case 18: case 21:
            case 24: case 27: case 30: case 33: case 36: case 39: case 42: case 45:
               ERArray[i] <<= 1;
               break;
            // multiply by 4
            case  2: case  8: case 14: case 20: case 26: case 32: case 38: case 44:
               ERArray[i] <<= 2;
               break;
            // multiply by 8
            case  1: case  7: case 13: case 19: case 25: case 31: case 37: case 43:
               ERArray[i] <<= 3;
               break;
            // add results
            case  4: case 10: case 16: case 22: case 28: case 34: case 40: case 46:
               col[lCount] = ERArray[i] + ERArray[i-1] + ERArray[i-2] + ERArray[i-3];
               lCount++;
               break;
            case  5: case 11: case 17: case 23: case 29: case 35: case 41: case 47:
               row[rCount] = ERArray[i] + ERArray[i-5];
               rCount++;
               break;
            }
         }

         // step 2.4.4.3 and step 2.4.4.4
         for (int box = 0; box < 8; box++){
            switch(box){
            case 0:
               boxGives = substitutionBox1[row[box]][col[box]];
               fillInB(boxGives, box+1, bOneToEight);
               break;
            case 1:
               boxGives = substitutionBox2[row[box]][col[box]];
               fillInB(boxGives, box+1, bOneToEight);
               break;
            case 2:
               boxGives = substitutionBox3[row[box]][col[box]];
               fillInB(boxGives, box+1, bOneToEight);
               break;
            case 3:
               boxGives = substitutionBox4[row[box]][col[box]];
               fillInB(boxGives, box+1, bOneToEight);
               break;
            case 4:
               boxGives = substitutionBox5[row[box]][col[box]];
               fillInB(boxGives, box+1, bOneToEight);
               break;
            case 5:
               boxGives = substitutionBox6[row[box]][col[box]];
               fillInB(boxGives, box+1, bOneToEight);
               break;
            case 6:
               boxGives = substitutionBox7[row[box]][col[box]];
               fillInB(boxGives, box+1, bOneToEight);
               break;
            case 7:
               boxGives = substitutionBox8[row[box]][col[box]];
               fillInB(boxGives, box+1, bOneToEight);
               break;
            }
         }

         for (int i = 0; i < pArrayLen; i++)
            tempRArray[i] = RArray[i];

         // permute and do exclusive-or with LArray[ite-1], step 2.4.5 and 2.4.6
         for (int i = 0; i < pArrayLen; i++){   // 32 times
            permutedB[i] = bOneToEight[PArray[i]-1];  // note the index!!!
            RArray[i] = (permutedB[i] == LArray[i]) ? 0 : 1;
         }

         // step 2.4.7, L[ite] = R[ite-1]
         for (int i = 0; i < pArrayLen; i++)
            LArray[i] = tempRArray[i];
      }  // end of the iteration for loop, finish step 2.4.8

      // set up R[16]L[16] block
      int[] finalPerm = new int[initialPermLen];   // 64 elements
      for (int i = 0; i < pArrayLen; i++){         // 32 times
         finalPerm[i] = RArray[i];
         finalPerm[i+pArrayLen] = LArray[i];
      }

      // perform final permutation on the block R[16]L[16], step 2.5
      for (int i = 0; i < initialPermLen; i++){
         encryptedBlock[i] = finalPerm[inverseIPArray[i]-1];
      }

      // convert to 8 bytes
      int temp = 0;
      int temp0 = 0;
      Integer oneInteger;
      for (int i = 0; i < 8; i++){
         temp = 8*i;
         temp0 = ((encryptedBlock[temp]  <<7) +
                  (encryptedBlock[temp+1]<<6) +
                  (encryptedBlock[temp+2]<<5) +
                  (encryptedBlock[temp+3]<<4) +
                  (encryptedBlock[temp+4]<<3) +
                  (encryptedBlock[temp+5]<<2) +
                  (encryptedBlock[temp+6]<<1) +
                   encryptedBlock[temp+7]);
         oneInteger = new Integer(temp0);
         m_oneEncodedDataBlock[i] = oneInteger.byteValue();
      }
   }

   /**
    * Fill in 4 bits into the B array based on the Substitution Box index
    * and the value obtained from that box. After the 8th filling in, this B
    * array becomes a ready-to-use 32-bit-array.
    * @param   boxGives      the number obtained from the Substitution Box
    * @param   boxIndex      the Substitution Box being used
    * @param   bOneToEight   the 32-bit-array to be filled in
    * @throws IllegalArgumentException if any parameter is out of bounds
    */
   private void fillInB(int boxGives, int boxIndex, int[] bOneToEight)
      throws IllegalArgumentException
   {
      if ((boxGives < 0) || (boxGives > 15))
         throw new IllegalArgumentException("boxGives out of bounds");

      if ((boxIndex < 1) || (boxIndex > 8))
         throw new IllegalArgumentException("boxIndex out of bounds");

      if ((bOneToEight == null) || (bOneToEight.length != 32))
         throw new IllegalArgumentException("bOneToEight is null or not 32");

      int[] fourBits = {0, 0, 0 ,0};
      switch(boxGives){
      case 1:
         fourBits[3] = 1;
         break;
      case 2:
         fourBits[2] = 1;
         break;
      case 3:
         fourBits[2] = 1;
         fourBits[3] = 1;
         break;
      case 4:
         fourBits[1] = 1;
         break;
      case 5:
         fourBits[1] = 1;
         fourBits[3] = 1;
         break;
      case 6:
         fourBits[1] = 1;
         fourBits[2] = 1;
         break;
      case 7:
         fourBits[1] = 1;
         fourBits[2] = 1;
         fourBits[3] = 1;
         break;
      case 8:
         fourBits[0] = 1;
         break;
      case 9:
         fourBits[0] = 1;
         fourBits[3] = 1;
         break;
      case 10:
         fourBits[0] = 1;
         fourBits[2] = 1;
         break;
      case 11:
         fourBits[0] = 1;
         fourBits[2] = 1;
         fourBits[3] = 1;
         break;
      case 12:
         fourBits[0] = 1;
         fourBits[1] = 1;
         break;
      case 13:
         fourBits[0] = 1;
         fourBits[1] = 1;
         fourBits[3] = 1;
         break;
      case 14:
         fourBits[0] = 1;
         fourBits[1] = 1;
         fourBits[2] = 1;
         break;
      case 15:
         fourBits[0] = 1;
         fourBits[1] = 1;
         fourBits[2] = 1;
         fourBits[3] = 1;
         break;
      }

      int shift = 4*(boxIndex - 1);
      for (int i = 0; i < 4; i++)
         bOneToEight[i+shift] = fourBits[i];
   }

   // The key to use for encryption
   private PSDESKey   m_key;
   private ArrayList m_dataList = new ArrayList();
   private byte[] m_oneEncodedDataBlock = new byte[8];
}

