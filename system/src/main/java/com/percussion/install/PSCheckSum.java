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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

/**
 * This program reads a file and generates the checksum using Adler32 algorithm
 */
public class PSCheckSum
{

   private static final Logger log = LogManager.getLogger(PSCheckSum.class);
   /**
    * Run this program to obtain the checksum of any file. Usage is
    * java com.percussion.install.PSCheckSum <input file>
    * @param args a string array containing a single string representing
    * the absolute path to the file whose checksum is to be calculated.
    */
   public static void main(String[] args)
   {
      if (args.length != 1)
      {
         System.err.println("Usage: PSCheckSum <input file>");
         System.exit(0);
      }
      try
      {
         long chkSum = getChecksum(args[0]);
         System.out.println("The checksum of the input file is " + chkSum);
      }
      catch (IOException e)
      {
         log.error("IOException : {}", e.getMessage());
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }

   /**
    * Returns the checksum of the file represented by the filePath parameter.
    * @param filePath the absolute path to the file whose checksum is to be
    * calculated, may not be <code>null</code> or empty
    * @return the checksum of the file represented by the filePath parameter
    * @throws IOException if it fails to read the file
    * @throws IllegalArgumentException if filePath is <code>null</code> or
    * empty or the file does not exist
    */
   public static long getChecksum(String filePath)
      throws IOException
   {
      if ((filePath == null) || (filePath.trim().length() == 0))
         throw new IllegalArgumentException("filePath may not be null or empty");

      File f = new File(filePath);
      if (!f.isFile())
         throw new IllegalArgumentException("File does not exist : " + filePath);

      CheckedInputStream inFile = null;
      Checksum cs = new Adler32();

      try
      {
         inFile = new CheckedInputStream(new FileInputStream(filePath), cs);
         byte[] buf = new byte[4096];
         while (inFile.read(buf) >= 0)
         {
         }
         return inFile.getChecksum().getValue();
      }
      finally
      {
         if (inFile != null)
            inFile.close();
      }
   }
}
