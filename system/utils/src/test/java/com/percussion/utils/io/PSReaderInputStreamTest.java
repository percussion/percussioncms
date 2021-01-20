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
package com.percussion.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

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
