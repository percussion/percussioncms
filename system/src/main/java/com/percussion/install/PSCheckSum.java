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

import com.percussion.error.PSExceptionUtils;
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
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
