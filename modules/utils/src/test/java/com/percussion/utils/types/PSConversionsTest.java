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

import junit.framework.TestCase;

public class PSConversionsTest extends TestCase
{
   public void testToArr() throws Exception
   {
      byte[] rval = PSConversions.longToByteArray(0x0102030405060708L);
      for(int i = 0; i < 8; i++)
      {
         assertEquals(i+1,rval[i]);
      }
      
      rval = PSConversions.longToByteArray(0xF1F2F3F4F5F6F7F8L);
      for(int i = 0; i < 8; i++)
      {
         long v = ((long) rval[i]) & 0xFF;
         
         assertEquals(i+0xF1,v);
      }

   }
   
   public void testToLong() throws Exception
   {
      byte[] test = new byte[16];
      
      for(int i = 0; i < 16; i++)
      {
         test[i] = (byte) (i + 1);
      }
      
      long rval = PSConversions.byteArrayToLong(test, 0);
      // System.out.println("Value: " + Long.toHexString(rval));
      assertEquals(rval, 0x0102030405060708L);
      
      rval = PSConversions.byteArrayToLong(test, 8);
      // System.out.println("Value: " + Long.toHexString(rval));
      assertEquals(rval, 0x090A0B0C0D0E0F10L);
      
      for(int i = 0; i < 16; i++)
      {
         test[i] = (byte) (i + 0xF0);
      }
      
      rval = PSConversions.byteArrayToLong(test, 0);
      // System.out.println("Value: " + Long.toHexString(rval));
      assertEquals(rval, 0xF0F1F2F3F4F5F6F7L);
      
      rval = PSConversions.byteArrayToLong(test, 8);
      // System.out.println("Value: " + Long.toHexString(rval));
      assertEquals(rval, 0xF8F9FAFBFCFDFEFFL);
   }
}
