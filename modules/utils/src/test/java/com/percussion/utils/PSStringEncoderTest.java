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
