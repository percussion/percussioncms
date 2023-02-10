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
package com.percussion.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A simple utility class to read makefiles.
 *
 * @author Chad Loder
 */
public class PSMakefileReader
{
   /**
    * Constructs a new Makefilereader.
    */
   public PSMakefileReader(File makefile)
      throws IOException
   {
      try(InputStreamReader is = new InputStreamReader(
              new FileInputStream(makefile))) {
         m_rdr = new BufferedReader(is);
      }
   }

   /**
    * Reads a logical line from the makefile. A logical line may
    * extend over several physical lines when the continuation
    * character (backslash) is used at the end of all but the
    * last line.
    *
    * @param   rdr The reader
    * @param   buf The buffer to which the logical line will be
    * appendend.
    *
    * @return   The logical line. If <CODE>null</CODE>, it means
    * the end of the file has been reached.
    */
   public String readLine()
      throws IOException
   {
      StringBuilder buf = new StringBuilder();
      int res = readLogicalLine(buf);
      if (res < 0)
         return null;

      return buf.toString();
   }

   /**
    * Reads a logical line from the makefile. A logical line may
    * extend over several physical lines when the continuation
    * character (backslash) is used at the end of all but the
    * last line.
    *
    * @param   rdr The reader
    * @param   buf The buffer to which the logical line will be
    * appendend.
    *
    * @return   int The number of physical lines read. If less than
    * 0, it means that the end of file was reached.
    */
   protected int readLogicalLine(StringBuilder buf)
      throws IOException
   {
      int numLines = 0;

      while (true)
      {
         String physLine = m_rdr.readLine();
         if (physLine == null)
            return -1;

         // if we're on a continued line, then collapse all leading
         // whitespace into a single space
         if (numLines > 0)
         {
            physLine = leftTrim(physLine);
         }

         if (physLine.endsWith("\\"))
         {
            buf.append(physLine.substring(0, Math.max(0, physLine.length() - 1)));
         }
         else
         {
            buf.append(physLine);
            break;
         }

         numLines++;
      }

      return numLines;
   }

   protected static String leftTrim(String line)
   {
      int realStart = 0;
      final int len = line.length();
      while (realStart < len)
      {
         if (!Character.isWhitespace(line.charAt(realStart)))
            break;

         realStart++;
      }

      if (realStart != 0)
         line = line.substring(realStart);

      return line;
   }

   private BufferedReader m_rdr;
}
