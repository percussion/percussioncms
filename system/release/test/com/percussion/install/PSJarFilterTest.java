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
package com.percussion.install;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import junit.framework.TestCase;

/**
 * JUnit test for JarFilter
 */
public class PSJarFilterTest extends TestCase
{
   private static final File ms_input = 
      new File("UnitTestResources/com/percussion/install/test.jar");
   private static final File ms_output = 
      new File("UnitTestResources/com/percussion/install/test-output.jar");

   private void filterJar() throws IOException
   {
      List<String> files = new ArrayList<String>();
      Map<String, String> env = new HashMap<String, String>();

      files.add("x/bletch");
      files.add("bar");
      env.put("x", "xyzzy");
      env.put("y", "plough");
      env.put("e", "new york");
      env.put("d", "massachusetts");

      PSJarFilter.filter(ms_input, ms_output, files, env);
   }

   public void testCheckEntriesExist() throws IOException
   {
      filterJar();
      JarFile testfile = new JarFile(ms_output);
      String bar = getStringEntry("bar", testfile);
      String foo = getStringEntry("foo", testfile);
      String bletch = getStringEntry("x/bletch", testfile);
      
      assertEquals("aaa\nbbb massachusetts eee\nfff\n", bar);
      assertEquals("aaa\r\nbbb\r\nccc\r\n", foo);
      assertEquals("abc\ndef new york\n", bletch);
   }

   public void testErrorExpansion() throws IOException
   {
      List<String> files = new ArrayList<String>();
      Map<String, String> env = new HashMap<String, String>();

      files.add("error");
      env.put("foo", "massachusetts");

      try
      {
         PSJarFilter.filter(ms_input, ms_output, files, env);
         fail("Failed to throw expected exception");
      }
      catch(IOException success) {}
   }

   private String getStringEntry(String entryName, JarFile file)
      throws IOException
   {
      final ZipEntry entry = file.getEntry(entryName);
      try(final Reader is = new InputStreamReader(file.getInputStream(entry), StandardCharsets.UTF_8)) {
         final char[] chars = new char[1000];
         final int count = is.read(chars);
         assert (count < chars.length && count > 0);
         return new String(chars, 0, count);
      }
   }

   /**
    * @see junit.framework.TestCase#tearDown()
    */
   @Override
   protected void tearDown() throws Exception
   {
      ms_output.delete();
      super.tearDown();
   }
}
