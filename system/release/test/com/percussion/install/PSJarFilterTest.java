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
