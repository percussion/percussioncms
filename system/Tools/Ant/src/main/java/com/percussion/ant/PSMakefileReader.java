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
package com.percussion.ant;

import java.io.*;

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
      StringBuffer buf = new StringBuffer();
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
   protected int readLogicalLine(StringBuffer buf)
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
