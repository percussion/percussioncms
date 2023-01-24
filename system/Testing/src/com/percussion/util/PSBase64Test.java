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

package com.percussion.util;

import com.percussion.error.PSRuntimeException;
import com.percussion.tools.PSCopyStream;
import junit.framework.TestCase;

import java.io.*;
import java.util.Arrays;

/**
 * Unit tests for <code>PSBase64Decoder</code> and <code>PSBase64Encoder</code>.
 */
public class PSBase64Test extends TestCase
{
   /**
    * Tests that a small string can be encoded and decoded.
    * @throws Exception if any error occurs
    */
   public void testSmallString() throws Exception
   {
      String original = "This is a string that needs to be encoded.";
      String encoded;
      String decoded;
      
      encoded = PSBase64Encoder.encode(original);
      assertTrue(!original.equals(encoded));     
      decoded = PSBase64Decoder.decode(encoded);
      assertEquals(original, decoded);
      
      // check for compatibility with existing encoded strings
      original = "demo";
      encoded = PSBase64Encoder.encode(original);
      assertEquals("ZGVtbw==", encoded);
      decoded = PSBase64Decoder.decode("ZGVtbw==");
      assertEquals(original, decoded);
   }
   
   public void testIllegalArgs() throws Exception
   {
      // null stream throws exception
      try
      {
         PSBase64Encoder.encode((InputStream) null, null);
         fail();
      }
      catch (IllegalArgumentException success) {}

      try
      {
         PSBase64Decoder.decode((InputStream) null, null);
         fail();
      }
      catch (IllegalArgumentException success) {}

      try
      {
         PSBase64Encoder.encode((byte[]) null);
         fail();
      }
      catch (IllegalArgumentException success) {}

      try
      {
         PSBase64Decoder.decode((byte[]) null);
         fail();
      }
      catch (IllegalArgumentException success) {}
   }
   
   /**
    * Tests the stream methods for encoding and decoding.
    * @throws Exception if any error occurs
    */
   public void testSmallStream() throws Exception
   {
      String original = "This is a string that needs to be encoded.";
      byte[] originalBytes = original.getBytes();    
      InputStream in = new ByteArrayInputStream(originalBytes);
      OutputStream out = new ByteArrayOutputStream();
      
      PSBase64Encoder.encode(in, out);
      String encoded = out.toString();
      assertTrue(!original.equals(encoded));
      
      byte[] encodedBytes = encoded.getBytes();
      in = new ByteArrayInputStream(encodedBytes);
      out = new ByteArrayOutputStream();
      PSBase64Decoder.decode(in, out);
      String decoded = out.toString();
      assertEquals(original, decoded);  
   }
 
   /**
    * Tests the methods for encoding and decoding a string with a specified
    * encoding.
    * @throws Exception if any error occurs
    */
   public void testEncodingName() throws Exception
   {
      String original = "This is a string that needs to be encoded.";

      // invalid encoding names throw an exception
      boolean threw = false;
      try
      {
         PSBase64Encoder.encode(original, "FOOBAR");
      }
      catch (PSRuntimeException e)
      {
         threw = true;
      }
      assertTrue(threw);
      
      String encoded = PSBase64Encoder.encode(original, "ISO-8859-1");
      assertTrue(!original.equals(encoded));     
      String decoded = PSBase64Decoder.decode(encoded);
      assertEquals(original, decoded);  
      
      encoded = PSBase64Encoder.encode(original, "US-ASCII");
      assertTrue(!original.equals(encoded));
      decoded = PSBase64Decoder.decode(encoded);
      assertEquals(original, decoded);
      
      original = "\u4eca\u65e5\u306f\u4e16\u754c"; // Hello World (Japanese)
      encoded = PSBase64Encoder.encode(original, "Shift_JIS");
      assertTrue(!original.equals(encoded));
      decoded = PSBase64Decoder.decode(encoded, "Shift_JIS");
      assertEquals(original, decoded);
   }

   /**
    * Tests that a large binary file can be encoded and decoded.
    * @throws Exception if any error occurs
    */
   public void testFile() throws Exception
   {
      InputStream file = new FileInputStream( RESOURCE_PATH + TEST_FILE );
      // copy file into a byte array so we can validate the decode
      ByteArrayOutputStream bout = new ByteArrayOutputStream(4096);
      PSCopyStream.copyStream(file, bout);
      byte[] originalBytes = bout.toByteArray();
     
      byte[] encodedBytes = PSBase64Encoder.encode(originalBytes);
      assertTrue(originalBytes.length < encodedBytes.length);
      
      byte[] decodedBytes = PSBase64Decoder.decode(encodedBytes);
      assertEquals(originalBytes.length, decodedBytes.length);     
      assertTrue(Arrays.equals(originalBytes, decodedBytes));     
   }
   
   
   /**
    * Defines the path to the files used by this unit test, relative from the
    * root.
    */
   private static final String RESOURCE_PATH =
      "UnitTestResources/com/percussion/util/";

   /**
    * Name of file to use for the file encoding test.
    */
   private static final String TEST_FILE = "base64test.pdf";

}
