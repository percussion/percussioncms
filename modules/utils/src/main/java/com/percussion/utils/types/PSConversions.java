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
