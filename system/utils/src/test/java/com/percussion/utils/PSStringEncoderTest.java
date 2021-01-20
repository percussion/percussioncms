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
package com.percussion.utils;

import com.percussion.utils.jsr170.PSStringEncoder;
import junit.framework.TestCase;

public class PSStringEncoderTest extends TestCase
{
   public void testEncode() throws Exception
   {
      String enc, dec;
      
      enc = PSStringEncoder.encode("abc");
      dec = PSStringEncoder.decode(enc);
      
      assertEquals("abc", enc);
      assertEquals("abc", dec);
   }
   
   public void testEncode2() throws Exception
   {
      String enc, dec;
      
      enc = PSStringEncoder.encode("a_b'c");
      dec = PSStringEncoder.decode(enc);
      
      assertEquals(enc, "a_x005f_b&apos;c");
      assertEquals(dec, "a_b'c");
   }
   
   public void testEncode3() throws Exception
   {
      String enc, dec;
      
      enc = PSStringEncoder.encode("a b<>c");
      dec = PSStringEncoder.decode(enc);
      
      assertEquals(enc, "a_x0020_b&lt;&gt;c");
      assertEquals(dec, "a b<>c");
   }
   
   public void testEncode4() throws Exception
   {
      String enc, dec;
      
      enc = PSStringEncoder.encode("a\tb\"c");
      dec = PSStringEncoder.decode(enc);
      
      assertEquals(enc, "a_x0009_b&quot;c");
      assertEquals(dec, "a\tb\"c");
   } 
   
   public void testEmpty() throws Exception
   {
      String enc, dec;
      
      enc = PSStringEncoder.encode("");
      dec = PSStringEncoder.decode(enc);
      
      assertEquals("", enc);
      assertEquals("", dec);
   }
   
   public void testDecode1() throws Exception
   {
      String dec;
      
      dec = PSStringEncoder.decode("a_b");
      assertEquals(dec, "a_b");
      
      dec = PSStringEncoder.decode("a_xb");
      assertEquals(dec, "a_xb");
      
      dec = PSStringEncoder.decode("a_xaazz_b");
      assertEquals(dec, "a_xaazz_b");
      
      dec = PSStringEncoder.decode("a_x1234b");
      assertEquals(dec, "a_x1234b");
   }
   
     
}
