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
