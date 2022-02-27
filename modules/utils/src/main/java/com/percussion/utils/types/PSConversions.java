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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.utils.types;

/**
 * Conversions to and from various types
 * 
 * @author dougrand
 */
public class PSConversions
{
   /**
    * Convert a long value to a byte array. The first byte of the array is the
    * high order byte of the long.
    * 
    * @param value long value to convert
    * @return an 8 byte array
    */
   public static byte[] longToByteArray(long value)
   {
      byte[] rval = new byte[8];
      for (int i = 0; i < 8; i++)
      {
         rval[7 - i] = (byte) (value & 0x00FF);
         value = value >>> 8;
      }
      return rval;
   }

   /**
    * Convert 8 bytes from a byte array to a long value
    * 
    * @param arr the array, never <code>null</code> and must have at least
    *           offset + 8 elements
    * @param offset the offset to start the conversion
    * @return a long value
    */
   public static long byteArrayToLong(byte[] arr, int offset)
   {
      long rval = 0;
      for (int i = 0; i < 8; i++)
      {
         rval = rval << 8;
         rval |= (long) arr[offset + i] & 0x00FF;
      }
      return rval;
   }
}
