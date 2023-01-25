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
package com.percussion.utils.io;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

/**
 * Test reader input stream. In particular this test will try all unicode
 * characters to see that going from characters to input stream and back result
 * in an identical string.
 * 
 * @author dougrand
 * 
 */
public class PSReaderInputStreamTest extends TestCase
{
   /**
    * Create a string with all 2^16 code points. Move from a reader to a stream
    * and back to a reader. Use the result and compare with the input.
    * 
    * @throws Exception
    */
   public void testRoundTrip() throws Exception
   {
      char arr[] = new char[0xd800];
      for (int i = 0; i < 0xd7ff; i++)
      {
         arr[i] = (char) (i + 1);
      }
      
      String input = new String(arr);
      doTest(input);
   }
   
   /**
    * Where the first test checked all code points, the second just tests a
    * small string.
    * 
    * @throws Exception
    */
   public void testSmallerString() throws Exception
   {
      doTest("The quick brown fox");
   }
   
   /**
    * Check mid sized block that doesn't work out evenly into a multiple of 
    * the buffer size.
    * 
    * @throws Exception
    */
   public void testBlock() throws Exception
   {
      char arr[] = new char[6000];
      for (int i = 0; i < 6000; i++)
      {
         arr[i] = (char) (i + 1);
      }
      
      String input = new String(arr);
      doTest(input);
   }

   /**
    * Run the data through the input strea <-> reader plumbing.
    * @param input the input string, assumed never <code>null</code> or empty.
    * @throws UnsupportedEncodingException
    * @throws IOException
    */
   private void doTest(String input) throws UnsupportedEncodingException, IOException
   {
      Reader r = new StringReader(input);
      InputStream is = new PSReaderInputStream(r);
      Reader cr = new InputStreamReader(is, "UTF8");
      StringWriter w = new StringWriter();
      IOUtils.copy(cr, w);
      String output = w.toString();
      assertEquals(input.length(), output.length());
      for (int i = 0; i < input.length(); i++)
      {
         char c1 = input.charAt(i);
         char c2 = output.charAt(i);
         if (c1 != c2)
         {
            fail("Unequal at index " + i + " found " + Integer.toHexString(c2)
                  + " instead of " + Integer.toHexString(c1));
         }
      }
   }
}
