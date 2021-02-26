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

public class TraceResultCompare implements ICustomCompare
{

   /**
    * Compares a given trace results file with an expected results file,
    * ignoring anything dynamic content that is not predictable such as
    * timestamps.  May be run as an application from the command line, with
    * an option to create the expected results file from a trace output file.
    * @see com.percussion.autotest.empire.script.ICustomCompare#compare(InputStream, String, InputStream, String)
    */
   public void compare(InputStream expectedResult,
                        String expectedCharSet,
                        InputStream actualResult,
                        String actualCharSet)
                  throws Exception
   {
      BufferedReader actualReader = new BufferedReader(
         new InputStreamReader(actualResult, actualCharSet));

      BufferedReader expectedReader = new BufferedReader(
         new InputStreamReader(expectedResult, expectedCharSet));

      int lineNum = 1;
      String expectedLine = null;
      String actualLine = null;
      String previousExpected = null;
      String previousActual = null;
      do
      {
         expectedLine = expectedReader.readLine();
         actualLine = readFilteredLine(actualReader);
         if (expectedLine == null || actualLine == null)
         {
            if (actualLine != null)
            {
               throw new ScriptTestFailedException(
               "Expected end of data on line " + lineNum + ", actually got \"" + actualLine + "\"");
            }

            String nextExpected;
            String nextActual;
            if (expectedLine != null)
            {
               throw new ScriptTestFailedException(
               "Reached end of actual data on line " + lineNum + ", expected \"" + expectedLine + "\"");
            }
            else
               break; // we reached both ends at the same time, which is good
         }

         if (!expectedLine.equalsIgnoreCase(actualLine))
         {
            String nextExpected = expectedReader.readLine();
            String nextActual = readFilteredLine(actualReader);
            if (previousExpected != null && previousActual != null)
            {
               if (nextExpected != null && nextActual != null)
               {
                  throw new ScriptTestFailedException("Expected data \r\n\"" + previousExpected + "\r\n" + expectedLine + "\r\n" + nextExpected + "\"\r\n on line " + lineNum + ", actually got \r\n\"" + previousActual + "\r\n" + actualLine + "\r\n" + nextActual + "\"");
               }
               else
               {
                  throw new ScriptTestFailedException("Expected data \r\n\"" + previousExpected + "\r\n" + expectedLine + "\"\r\n on line " + lineNum + ", actually got \r\n\"" + previousActual + "\r\n" + actualLine + "\"");
               }
            }

            if (nextExpected != null && nextActual != null)
            {
               throw new ScriptTestFailedException("Expected data \r\n\"" + expectedLine + "\r\n" + nextExpected + "\"\r\n on line " + lineNum + ", actually got \r\n\"" + actualLine + "\r\n" + nextActual + "\"");
            }
            else
            {
               throw new ScriptTestFailedException("Expected data \r\n\"" + expectedLine + "\"\r\n on line " + lineNum + ", actually got \r\n\"" + actualLine + "\"");
            }
         }

         lineNum++;
         previousExpected = expectedLine;
         previousActual = actualLine;
      }
      while(true);

   }

   /**
    * Reads a line and checks to see if it is a header line.  If so, removes
    * the timestamp from the line and returns the line.
    * Assumes format of header line is:
    *
    * MM/dd hh:mm:ss.SSS  HeaderText
    *
    * Also removes all lines beginning with '#' to clear out start/stop/restart
    * messages
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

      // be sure the end of the stream has not been reached
      line = reader.readLine();

      // avoid start/stop/restart messages
      while ((line != null) && (line.length() > 0))
      {
         if (line.charAt(0) != '#')
            break;

         line = reader.readLine();
      }

      if (line == null)
         return line;

      // now filter the line - assume the timestamp is at the start of the line
      int nextCharPos = TS_FORMAT.length();

      // see if line is at least as long as the timestamp plus the next char
      if (line.length() < nextCharPos + 1)
         return line;

      /* so far it looks good, be sure we can parse the timestamp into a date
       * to validate its format
       */
      try
      {
         FastDateFormat formatter = FastDateFormat.getInstance (TS_FORMAT);
         Date newDate = formatter.parse(line.substring(0, nextCharPos - 1));
      }
      catch(java.text.ParseException e)
      {
         // wasn't really a timestamp match
         return line;
      }

      // this is a match, so return everything after the timestamp
      return line.substring(nextCharPos);

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
         TraceResultCompare compareResult = new TraceResultCompare();

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

   private static final String TS_FORMAT = "MM/dd hh:mm:ss.SSS";

}
