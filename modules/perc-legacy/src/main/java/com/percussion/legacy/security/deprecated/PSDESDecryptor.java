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

package com.percussion.legacy.security.deprecated;

import com.percussion.security.IPSDecryptor;
import com.percussion.security.PSEncryptionException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


/**
 * The PSDESDecryptor class is used to decrypt data using the specified
 * DES key. DES uses a 64-bit key, which is encoded into a 56-bit value used
 * in the algorithm for encryption/decryption.
 * <P>
 * DES is described in
 * <A HREF="http://www.itl.nist.gov/fipspubs/fip46-2.htm">FIPS 46-2</A>.
 * The DES modes of operation are described in
 * <A HREF="http://www.itl.nist.gov/fipspubs/fip81.htm">FIPS 81</A>.
 *
 * @see        PSDESKey
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
@Deprecated
public class PSDESDecryptor implements IPSDecryptor
{
   /**
    * Construct a DES decryptor using the specified DES key.
    *
    * @param      key      the DES key to use for decryption
    * @throws IllegalArgumentException if <code>key</code> is <code>null</code>
    */
   public PSDESDecryptor(PSDESKey key) throws IllegalArgumentException
   {
      if (key == null)
         throw new IllegalArgumentException( "key cannot be null" );

      // store key for later use
      m_key = key;
   }

   /**
    * Decrypt the data in the specified input stream.
    *
    * @param      in          the stream containing the encrypted data
    *
    * @param      out         the stream to store the plain text
    *                                     representation of the data
    *
    * @exception  IOException if an I/O exception occurs
    */
   public void decrypt(InputStream in, OutputStream out) throws PSEncryptionException
   {
      int byteAsInt;

      // in case this is a subsequent call, clear the list
      m_dataList.clear();

      try {
         while ((byteAsInt = in.read()) != -1){  // read in next byte
            m_dataList.add(new Integer(byteAsInt));
         }

         int dataListSize = m_dataList.size();
         if (dataListSize == 0)
            return;

         if ((dataListSize%8) != 0)
            throw new PSEncryptionException("Total bytes read in is NOT a multiple of 8");

         int dataBlock = dataListSize/8;
         for (int block = 0; block < dataBlock; block++){ // decrypt dataBlock one by one
            decryptOneDataBlock(block);
            out.write(m_oneDecodedDataBlock);
         }
         out.flush();
      } catch (IOException e) {
         throw new PSEncryptionException(e.getMessage(),e);
      } finally {
         // clean up on exit so we don't consume memory
         m_dataList.clear();
      }
   }

   /**
    * A convenidece method to decrypt data into a String.
    *
    * @param      in          the stream containing the encrypted data
    *
    * @return                 a string containing the plain text
    *                                     representation of the data
    *
    * @throws IOException if an I/O exception occurs
    */
   public java.lang.String decrypt(InputStream in) throws PSEncryptionException {
      String empty = "";
      if (in == null){
         return empty;
      }

      // we'll use a byte array for the output stream
      try(ByteArrayOutputStream oBuf = new ByteArrayOutputStream()) {

         // do the decryption
         decrypt(in, oBuf);

         // and get the result as a string
         return (oBuf.toString()).trim();
      }// remove the trailing white spaces
      catch (IOException e) {
         throw new PSEncryptionException(e.getMessage(),e);
      }
   }

   /**
    * A convenidece method to decrypt data from a byte array into a String.
    *
    * @param      in          the byte array containing the encrypted data
    *
    * @return                 a string containing the plain text
    *                                     representation of the data
    *
    * @throws PSEncryptionException if an exception occurs
    */
   public java.lang.String decrypt(byte[] in) throws PSEncryptionException
   {
      String empty = "";
      if ((in == null) || (in.length == 0)){
         return empty;
      }

      // convert the input byte array to a stream
      try(ByteArrayInputStream iBuf = new ByteArrayInputStream(in)) {

         // and do the string decryption
         return decrypt(iBuf);
      } catch (IOException e) {
         throw new PSEncryptionException(e.getMessage(),e);
      }
   }

   @Override
   public String decryptWithPassword(String in, String password) throws PSEncryptionException {
      throw new PSEncryptionException("Not implemented");
   }

   /**
    * Decrypt one data block based on its input index.
    * @param   blockIndex     the data block index
    */
   private void decryptOneDataBlock(int blockIndex)
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

      // obtain L[16] and R[16], step 2.3
      int   initialPermLen = PSDESEncryptor.initialPermLen;
      int[] InitialPermutation = PSDESEncryptor.InitialPermutation;
      int[] LArray = new int[initialPermLen/2];   // 32 elements
      int[] RArray = new int[initialPermLen/2];
      for (int i = 0; i < initialPermLen/2; i++){
         RArray[i] = bitArray[InitialPermutation[i]-1];
         LArray[i] = bitArray[InitialPermutation[i+initialPermLen/2]-1];
      }

/* test block
      byte[] keyBytes = new byte[8];
      for (int i = 0; i < 8; i++){
         keyBytes[i] = 0x02;
      }
      try{
         PSDESKey desKey = new PSDESKey(keyBytes);
         PSDESEncryptor encryptor = new PSDESEncryptor(desKey);
         RArray = encryptor.m_R16;
         LArray = encryptor.m_L16;
      } catch (com.percussion.error.PSIllegalArgumentException e1){
         System.err.println("Caught PSIllegalArgumentException. " + e1.getMessage());
      }
*/
      // apply 16 subkeys to the data block, step 2.4
      applySubKeys(RArray, LArray);
   }

   /**
    * Apply all 16 subkeys (kArray) to the data block.
    * @param   RArray   bit array R[16]
    * @param   LArray   bit array L[16]
    */
   private void applySubKeys(int[] RArray, int[] LArray)
   {
      int initialPermLen = PSDESEncryptor.initialPermLen;
      int eArrayLen = PSDESEncryptor.eArrayLen;
      int pArrayLen = PSDESEncryptor.pArrayLen;
      int[] inverseIPArray = PSDESEncryptor.inverseIPArray;
      int[] EArray = PSDESEncryptor.EArray;
      int[] PArray = PSDESEncryptor.PArray;

      int[] decryptedBlock = new int[initialPermLen];

      int  iteLimit = m_key.IterationSetting;
      int[] ELArray = new int[eArrayLen];    // 48 elements
      int[] KArray  = new int[eArrayLen];

      int[] bOneToEight = new int[pArrayLen];  // 32 elements
      int[] permutedB   = new int[pArrayLen];

      int[] row = new int[8];
      int[] col = new int[8];

      int rCount = 0;
      int lCount = 0;
      int boxGives;

      int[] tempLArray = new int[initialPermLen/2];   // 32 elements

      // iteration index starts from 1
      for (int ite = iteLimit; ite >= 1; ite--){
         KArray = m_key.getKArray(ite);

         // expand 32-bit L[ite] into 48 bits, step 2.4.1
         // exclusive-or ELArray with KArray, step 2.4.2
         for (int i = 0; i < eArrayLen; i++){
            ELArray[i] = LArray[EArray[i]-1];   // note the index!!!
            ELArray[i] = (ELArray[i] == KArray[i]) ? 0 : 1;  // E(L[ite]) xor K[ite]
         }

         // step 2.4.3, step 2.4.4.1,  and step 2.4.4.2
         rCount = 0;
         lCount = 0;
         for (int j = 0; j < eArrayLen; j++){
            switch(j){
            // multiply by 2
            case  0: case  3: case  6: case  9: case 12: case 15: case 18: case 21:
            case 24: case 27: case 30: case 33: case 36: case 39: case 42: case 45:
               ELArray[j] <<= 1;
               break;
            // multiply by 4
            case  2: case  8: case 14: case 20: case 26: case 32: case 38: case 44:
               ELArray[j] <<= 2;
               break;
            // multiply by 8
            case  1: case  7: case 13: case 19: case 25: case 31: case 37: case 43:
               ELArray[j] <<= 3;
               break;
            // add results to store column
            case  4: case 10: case 16: case 22: case 28: case 34: case 40: case 46:
               col[lCount] = ELArray[j] + ELArray[j-1] + ELArray[j-2] + ELArray[j-3];
               lCount++;
               break;
            case  5: case 11: case 17: case 23: case 29: case 35: case 41: case 47:
               row[rCount] = ELArray[j] + ELArray[j-5];
               rCount++;
               break;
            }
         }

         // step 2.4.4.3 and step 2.4.4.4
         for (int box = 0; box < 8; box++){
            switch(box){
            case 0:
               boxGives = PSDESEncryptor.substitutionBox1[row[box]][col[box]];
               fillInB(boxGives, box+1, bOneToEight);
               break;
            case 1:
               boxGives = PSDESEncryptor.substitutionBox2[row[box]][col[box]];
               fillInB(boxGives, box+1, bOneToEight);
               break;
            case 2:
               boxGives = PSDESEncryptor.substitutionBox3[row[box]][col[box]];
               fillInB(boxGives, box+1, bOneToEight);
               break;
            case 3:
               boxGives = PSDESEncryptor.substitutionBox4[row[box]][col[box]];
               fillInB(boxGives, box+1, bOneToEight);
               break;
            case 4:
               boxGives = PSDESEncryptor.substitutionBox5[row[box]][col[box]];
               fillInB(boxGives, box+1, bOneToEight);
               break;
            case 5:
               boxGives = PSDESEncryptor.substitutionBox6[row[box]][col[box]];
               fillInB(boxGives, box+1, bOneToEight);
               break;
            case 6:
               boxGives = PSDESEncryptor.substitutionBox7[row[box]][col[box]];
               fillInB(boxGives, box+1, bOneToEight);
               break;
            case 7:
               boxGives = PSDESEncryptor.substitutionBox8[row[box]][col[box]];
               fillInB(boxGives, box+1, bOneToEight);
               break;
            }
         }

         for (int i = 0; i < pArrayLen; i++)
            tempLArray[i] = LArray[i];

         // permute and do exclusive-or with RArray[ite], step 2.4.5 and 2.4.6
         for (int i = 0; i < pArrayLen; i++){   // 32 times
            permutedB[i] = bOneToEight[PArray[i]-1];  // note the index!!!
            LArray[i] = (permutedB[i] == RArray[i]) ? 0 : 1;
         }

         // step 2.4.7, R[ite-1] = L[ite]
         for (int i = 0; i < pArrayLen; i++)
            RArray[i] = tempLArray[i];
      }  // end of the iteration for loop, finish step 2.4.8

      // set up L[0]R[0] block
      int[] finalPerm = new int[initialPermLen];  // 64 elements
      for (int i = 0; i < pArrayLen; i++){        // 32 times
         finalPerm[i] = LArray[i];
         finalPerm[i+pArrayLen] = RArray[i];
      }

      // perform final permutation on the block L[0]R[0], step 2.5
      for (int i = 0; i < initialPermLen; i++){
         decryptedBlock[i] = finalPerm[inverseIPArray[i]-1];
      }

      // convert to 8 bytes
      int temp = 0;
      int temp0 = 0;
      Integer oneInteger;
      for (int i = 0; i < 8; i++){
         temp = 8*i;
         temp0 = ((decryptedBlock[temp]  <<7) +
                  (decryptedBlock[temp+1]<<6) +
                  (decryptedBlock[temp+2]<<5) +
                  (decryptedBlock[temp+3]<<4) +
                  (decryptedBlock[temp+4]<<3) +
                  (decryptedBlock[temp+5]<<2) +
                  (decryptedBlock[temp+6]<<1) +
                   decryptedBlock[temp+7]);
         oneInteger = new Integer(temp0);
         m_oneDecodedDataBlock[i] = oneInteger.byteValue();
      }
   }

   /**
    * Fill in 4 bits into the B array based on the Substitution Box index
    * and the value obtained from that box. After the 8th filling in, this B
    * array becomes a ready-to-use 32-bit-array.
    * @param   boxGives    the number obtained from the Substitution Box
    * @param   boxIndex    the Substitution Box being used
    * @param   bOneToEight the 32-bit-array to be filled in
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

   // the key to use for decryption
   private PSDESKey  m_key;
   private ArrayList m_dataList = new ArrayList();
   private byte[] m_oneDecodedDataBlock = new byte[8];
}

