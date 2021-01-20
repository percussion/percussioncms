/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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

package com.percussion.data;

public class PSBinaryData
{
   /**
    * Constructs a new binary data object
    *
    */
   public PSBinaryData(byte[] binaryData)
   {
      super();
      m_byte = binaryData;
   }

   /**
    * Return the binary data in default string 
    *   (currently hexadecimal) format
    *
    */
   public String toString()
   {
      /* this used to be a hex string, but since we're primarily using this
       * to send data to the web, changing this to use base64 which is the
       * binary data standard.
       */
      return toBase64String();
   }

   /**
    * Return the binary data in base64 format (String)
    *
    */
   public String toBase64String()
   {
      if (m_byte != null)
      {
         java.io.ByteArrayInputStream in
            = new java.io.ByteArrayInputStream(m_byte);
         java.io.ByteArrayOutputStream out
            = new java.io.ByteArrayOutputStream();

         try {
            com.percussion.util.PSBase64Encoder.encode(in, out);
         } catch (java.io.IOException e) {
            throw new RuntimeException(e.toString());
         }

         return new String(out.toByteArray());
      } else
         return null;
   }

   /**
    * Return the binary data in hexadecimal format (String)
    *
    */
   public String toHexString()
   {
      if (m_byte != null)
      {
         StringBuffer my_StringBuffer = new StringBuffer();
         for (int i = 0; i < m_byte.length; i++)
         {
            my_StringBuffer.append(getLeftHexNibble(m_byte[i]));
            my_StringBuffer.append(getRightHexNibble(m_byte[i]));
         }

         return my_StringBuffer.toString();
      } else
         return null;
   }

   /**
    * Return the binary data in Octal format (String)
    *
    */
   public String toOctalString()
   {
      if (m_byte != null)
      {
         StringBuffer my_StringBuffer = new StringBuffer();
         for (int i = 0; i < m_byte.length; i++)
         {
            my_StringBuffer.append(getLeftOctalNibble(m_byte[i]));
            my_StringBuffer.append(getRightOctalNibble(m_byte[i]));
         }

         return my_StringBuffer.toString();
      } else
         return null;
   }

   /**
    * Return the binary data in Binary format (String)
    *
    */
   public String toBinaryString()
   {
      if (m_byte != null)
      {
         StringBuffer my_StringBuffer = new StringBuffer();
         for (int i = 0; i < m_byte.length; i++)
         {
            my_StringBuffer.append(getLeftBinaryNibble(m_byte[i]));
            my_StringBuffer.append(getRightBinaryNibble(m_byte[i]));
         }

         return my_StringBuffer.toString();
      } else
         return null;
   }

   /**
    * Return the binary data byte array
    *
    */
   public byte[] getByteArray()
   {
      return m_byte;
   }

   byte[] m_byte = null;

   static String[] octalMap   = new String[16];

   static String[] hexMap      = new String[16];

   static String[] binaryMap   = new String[16];

   /*
    * Set up the static string maps for octal, binary, and hexadecimal
    *   representations of the full range of 1 Nibble
    */
   static {
      octalMap[0]      = "00";
      hexMap[0]      = "0";
      binaryMap[0]   = "0000";

      octalMap[1]      = "01";
      hexMap[1]      = "1";
      binaryMap[1]   = "0001";

      octalMap[2]      = "02";
      hexMap[2]      = "2";
      binaryMap[2]   = "0010";

      octalMap[3]      = "03";
      hexMap[3]      = "3";
      binaryMap[3]   = "0011";

      octalMap[4]      = "04";
      hexMap[4]      = "4";
      binaryMap[4]   = "0100";

      octalMap[5]      = "05";
      hexMap[5]      = "5";
      binaryMap[5]   = "0101";

      octalMap[6]      = "06";
      hexMap[6]      = "6";
      binaryMap[6]   = "0110";

      octalMap[7]      = "07";
      hexMap[7]      = "7";
      binaryMap[7]   = "0111";

      octalMap[8]      = "10";
      hexMap[8]      = "8";
      binaryMap[8]   = "1000";

      octalMap[9]      = "11";
      hexMap[9]      = "9";
      binaryMap[9]   = "1001";

      octalMap[10]   = "12";
      hexMap[10]      = "A";
      binaryMap[10]   = "1010";

      octalMap[11]   = "13";
      hexMap[11]      = "B";
      binaryMap[11]   = "1011";

      octalMap[12]   = "14";
      hexMap[12]      = "C";
      binaryMap[12]   = "1100";

      octalMap[13]   = "15";
      hexMap[13]      = "D";
      binaryMap[13]   = "1101";

      octalMap[14]   = "16";
      hexMap[14]      = "E";
      binaryMap[14]   = "1110";

      octalMap[15]   = "17";
      hexMap[15]      = "F";
      binaryMap[15]   = "1111";
   }


   /*
    * Nibble processing routines
    *
    */
   static String getRightHexNibble(byte b)
   {
      return hexMap[b & 0x0F];   
   }

   static String getLeftHexNibble(byte b)
   {
      return hexMap[ (b>>>4) & 0x0f];
   }

   static String getRightOctalNibble(byte b)
   {
      return octalMap[b & 0x0F];   
   }

   static String getLeftOctalNibble(byte b)
   {
      return octalMap[ (b>>>4) & 0x0f];
   }

   static String getRightBinaryNibble(byte b)
   {
      return hexMap[b & 0x0F];   
   }

   static String getLeftBinaryNibble(byte b)
   {
      return hexMap[ (b>>>4) & 0x0f];
   }
}

