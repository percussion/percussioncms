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
package com.percussion.install;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * This class allows the installer to accept a jar file that has component
 * text files that need to be localized for the installation. 
 */
public class PSJarFilter
{
   /**
    * Read entries from the input jar. For each entry, compare it against the
    * list of files that should be filtered using the passed environment. For
    * files that are filtered, run the file, assumed to be text, through the
    * filtering code. For all files, place the result, changed or unchanged, in
    * the output jar.
    * 
    * Variables in the input files that should be replaced should be delimited
    * with ${ and }.
    * 
    * @param inputJar
    *           A file to open as a jar file. Must not be <code>null</code>
    *           and must exist.
    * @param outputJar
    *           A file to create to hold the files and modified files from the
    *           input file. Must not be <code>null</code>. If it exists it
    *           will be overwritten.
    * @param filesToFilter
    *           A list of files that should be filtered. May not be <code>null</code>,
    *           but may be empty.
    * @param environment
    *           A map of variable names to values. The keys in the map must be
    *           {@link java.lang.String}, but the values may be of any type.
    *           The value used will be the result of the
    *           {@link Object#toString()}method. May not be <code>null</code>.
    */
   public static void filter(
      File inputJar,
      File outputJar,
      List filesToFilter,
      Map environment)
      throws IOException
   {
      if (inputJar == null)
      {
         throw new IllegalArgumentException("Input jar may not be null");
      }
      if (inputJar.exists() == false)
      {
         throw new IllegalArgumentException("Input jar file must exist");
      }
      if (outputJar == null)
      {
         throw new IllegalArgumentException("Output jar must be specified");
      }
      if (filesToFilter == null)
      {
         throw new IllegalArgumentException("There must be a list of files to filter");
      }
      if (environment == null)
      {
         throw new IllegalArgumentException("You must specify an environment");
      }

      JarFile inputJarFile = new JarFile(inputJar);
      Enumeration entries = inputJarFile.entries();
      FileOutputStream output = new FileOutputStream(outputJar);
      JarOutputStream outputJarStream = new JarOutputStream(output);
      while (entries.hasMoreElements())
      {
         JarEntry entry = (JarEntry) entries.nextElement();
         InputStream is = inputJarFile.getInputStream(entry);
         if (filesToFilter.contains(entry.getName()))
         {
            copyFilterBytes(entry, is, outputJarStream, environment);
         }
         else
         {
            copyBytes(entry, is, outputJarStream);
         }
      }
      outputJarStream.close();
   }

   /**
    * Copy bytes from the input entry to the output stream. In doing this look
    * for variables, and substitute values from the environment.
    * 
    * @param entry
    *           The entry to copy, assumed to be not <code>null</code>
    * @param inputStream
    *           The input stream from the entry, assumed to be not <code>null</code>
    * @param outputJarStream
    *           The output stream, assumed to be not <code>null</code>
    * @param environment
    *           The environment, assumed to be not <code>null</code>
    */
   private static void copyFilterBytes(
      JarEntry entry,
      InputStream inputStream,
      JarOutputStream outputJarStream,
      Map environment)
      throws IOException
   {
      Reader reader = new InputStreamReader(inputStream);
      StringWriter writer = new StringWriter();
      BufferedReader buffered = new BufferedReader(reader);
      String line;

      while ((line = buffered.readLine()) != null)
      {
         // Filter line
         String outputline = expand(line, environment);
         writer.write(outputline);
         writer.write('\n');
      }
      writer.flush();
      String output = writer.toString();
      byte bytes[] = output.getBytes("UTF8");

      JarEntry outentry = new JarEntry(entry.getName());
      // Copy information from original entry
      outentry.setExtra(entry.getExtra());
      outentry.setMethod(entry.getMethod());
      outentry.setComment(entry.getComment());
      outentry.setTime(entry.getTime());

      outputJarStream.putNextEntry(outentry);
      outputJarStream.write(bytes);
      outputJarStream.closeEntry();
   }

   /**
    * Expand variable references
    * 
    * @param line
    *           input line, assumed never <code>null</code>
    * @param environment
    *           contains values for expansion, assumed not <code>null</code>
    * @return expanded string
    */
   private static String expand(String line, Map environment)
      throws IOException
   {
      StringBuffer output = new StringBuffer(line.length());
      int len = line.length();
      int lenm1 = len - 1;

      for (int i = 0; i < len; i++)
      {
         char ch = line.charAt(i);
         if (ch == '$' && i < lenm1 && line.charAt(i + 1) == '{')
         {
            // Get var, reposition at end
            int end = line.indexOf('}', i);
            if (end < 0)
            {
               throw new IOException(
                  "Invalid replacement variable found on line: " + line);
            }
            String var = line.substring(i + 2, end);
            i = end;
            Object val = environment.get(var);
            if (val != null)
            {
               output.append(val.toString());
            }
         }
         else
         {
            output.append(ch);
         }
      }

      return output.toString();
   }

   /**
    * Copy bytes from the input entry to the output stream.
    * 
    * @param entry
    *           The entry to copy, assumed to be not <code>null</code>
    * @param inputStream
    *           The input stream from the entry, assumed to be not <code>null</code>
    * @param outputJarStream
    *           The output stream, assumed to be not <code>null</code>
    */
   private static void copyBytes(
      JarEntry entry,
      InputStream inputStream,
      JarOutputStream outputJarStream)
      throws IOException
   {
      outputJarStream.putNextEntry(entry);
      int count;
      byte buffer[] = new byte[2048];
      while ((count = inputStream.read(buffer)) >= 0)
      {
         outputJarStream.write(buffer, 0, count);
      }
      outputJarStream.closeEntry();
   }
}
