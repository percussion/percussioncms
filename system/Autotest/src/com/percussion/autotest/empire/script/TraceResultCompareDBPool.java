/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.autotest.empire.script;

import java.io.*;
import java.util.Date;

public class TraceResultCompareDBPool extends TraceResultCompare
{

   /**
    * Reads a line and filters it for DB Pool tracing info.  First passes line
    * to readFilteredLine in parent class in case it is a timestamp line
    *
    * @param reader The BufferReader for the actual results input stream.  May not be
    *                <code>null</code>.
    * @return the filtered line.  May be <code>null</code> if the end of the stream has been
    *          reached.
    *
    * @throws IOException if there is a problem reading from the input stream
    */
   public String readFilteredLine(BufferedReader reader) throws java.io.IOException
   {
      String line = null;

      // be sure we have a reader
      if (reader == null)
         throw new IllegalArgumentException("Reader supplied to readFilteredLine must not be null.");

      // read line filtered by parent in case it is a header line)
      line = super.readFilteredLine(reader);

      // be sure the end of the stream has not been reached
      if (line == null)
         return line;

      // now we filter it ourselves
      int pos = line.toLowerCase().indexOf("connection");
      pos = line.toLowerCase().indexOf("driver", pos);
      pos = line.toLowerCase().indexOf("server", pos);

      // if pos is > -1, this is a match
      if (pos > -1)
      {
         // read till we get a blank line
         while ((line = reader.readLine()) != null)
         {
            // see if first char is a carriage return
            if ((line.length() == 0) || (line.charAt(0) == '\n') ||
               (line.charAt(0) == '\r'))
               break;
         }
      }


      return line;

   }


   /**
    * Either runs a test comparing two files or will create a file that has
    * had the timestamps removed to use when running autotests.  CmdLine
    * format is:
    *
    * java TraceResultCompare <cmd> <file1> <file2>
    *
    * cmd = "C"  the first file supplied is used to create the second file,
    * stripping out the timestamps.  If the second file exists, it will
    * be overwritten
    *
    * cmd = "T" the two files are compared, assuming that the
    * first one is the actual file (with the timestamps) and the second one
    * is the expected result.
    *
    */
   public static void main(String[] args)
   {

      FileInputStream expected = null;
      FileInputStream actual = null;
      BufferedReader expectedReader = null;
      BufferedWriter actualWriter = null;


      try
      {
         // see if we are testing or creating
         boolean dispUsage = false;
         String cmd = null;
         if (args.length != 3)
            dispUsage = true;
         else
         {
            cmd = args[0].toUpperCase();
            if (!(cmd.equals("T") || cmd.equals("C")))
               dispUsage = true;
         }

         // give instructions and exit
         if (dispUsage)
         {
            System.out.println("Usage:");
            System.out.println("java TraceResultCompare <cmd> <file1> <file2>");
            System.out.println();
            System.out.println("cmd = \"C\":  the first file supplied is used to create the second file, " +
            "stripping out the timestamps.  If the second file exists, it will be overwritten");
            System.out.println();
            System.out.println("cmd = \"T\": the two files are compared, assuming that the " +
            "first one is the actual file (with the timestamps) and the second one is the " +
            "expected result.");
            System.out.println();
            System.exit(0);
         }

         // create the object
         TraceResultCompareDBPool compareResult = new TraceResultCompareDBPool();

         if (cmd.equals("T"))
         {
            // testing
            System.out.println("Comparing the two files....");
            actual = new FileInputStream(new File(args[1]));
            expected = new FileInputStream(new File(args[2]));
            String charSet = System.getProperty("file.encoding");
            compareResult.compare(expected, charSet, actual, charSet);
            System.out.println("Compare successful");
         }
         else
         {
            // creating expected test file
            System.out.println("Creating the test file....");
            expectedReader = new BufferedReader(new FileReader(new File(args[1])));
            File actualFile = new File(args[2]);
            actualWriter = new BufferedWriter(new FileWriter(actualFile));

            String line = null;
            while( (line = compareResult.readFilteredLine(expectedReader)) != null)
            {
               actualWriter.write(line);
               actualWriter.newLine();
            }
            System.out.println("Create successful");

         }
      }
      catch(ScriptTestFailedException e)
      {
         System.out.println("Compare failed: " + e.toString());
      }
      catch(Exception e)
      {
         System.out.println("Error: " + e.toString());
      }
      finally
      {
         if (expected != null)
            try {expected.close();} catch(Exception e){}
         if (actual != null)
            try {actual.close();} catch(Exception e){}
         if (expectedReader != null)
            try {expectedReader.close();} catch(Exception e){}
         if (actualWriter != null)
            try {actualWriter.close();} catch(Exception e){}
      }

   }

}
