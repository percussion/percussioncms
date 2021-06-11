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
package com.percussion.test.http;

import com.percussion.test.io.LogSink;
import com.percussion.test.util.ClockSync;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class HttpBench
{

   private static final Logger log = LogManager.getLogger(HttpBench.class);

   public static void main(String[] args)
   {
      try
      {
         BenchMarker bench = new BenchMarker();
         parseArguments(bench, args);
         bench.waitForStart();
         bench.run();
         if (ms_out != null)
            bench.writeResults(ms_out);
         else
            bench.writeResults(System.out);
      }
      catch (Throwable t)
      {
         log.error(t.getMessage());
         log.debug(t.getMessage(), t);
      }
      finally
      {
         if (ms_out != null)
            try { ms_out.close(); } catch (IOException e) { /* ignore */ }
      }
   }
   
   public static void parseArguments(BenchMarker bench, String[] args)
      throws ParseException, IllegalArgumentException, IOException,
      MalformedURLException
   {
      Date startTime = null;
      Date endTime = null;
      
      int numThreads = 1;
      double minutes = 10.0;

      // first, synchronize the time if necessary
      for (int i = 0; i < args.length; i++)
      {
         String arg = args[i];
         if (arg.equals("-sync"))
         {
            try
            {
               ClockSync.syncTimeDOS();
            }
            catch (IOException e)
            {
               System.out.println("Could not sync time: " + e.toString());
            }
         }
      }
      
      // then parse the rest of the args
      for (int i = 0; i < args.length; i++)
      {
         String arg = args[i];
         if (arg.startsWith("-stime"))
         {
            String subArg = arg.substring(6);
            if (subArg.startsWith("\"") && subArg.endsWith("\""))
               subArg = subArg.substring(1, subArg.length());

            Date stime = parseDateTime(subArg);
            if (stime != null)
               startTime = stime;
         }
         else if (arg.startsWith("-etime"))
         {
            String subArg = arg.substring(6);
            if (subArg.startsWith("\"") && subArg.endsWith("\""))
               subArg = subArg.substring(1, subArg.length());

            Date etime = parseDateTime(subArg);
            if (etime != null)
               endTime = etime;
         }
         else if (arg.startsWith("-file"))
         {
            String subArg = arg.substring(5);
            if (subArg.startsWith("\"") && subArg.endsWith("\""))
               subArg = subArg.substring(1, subArg.length());

            File f = new File(subArg);

            readURLs(bench, f);
         }
         else if (arg.startsWith("-threads"))
         {
            numThreads = Integer.parseInt(arg.substring(8));
         }
         else if (arg.startsWith("-minutes"))
         {
            minutes = Double.parseDouble(arg.substring(8));
         }
         else if (arg.startsWith("-out"))
         {
            ms_out = new FileOutputStream(arg.substring(4));
         }
      }

      if (startTime == null)
         startTime = new Date(System.currentTimeMillis() + 5000L);

      if (endTime == null)
      {
         endTime = new Date(startTime.getTime() + (long)(minutes * 60000L));
      }

      System.out.println("Start time: " + startTime.toString());
      System.out.println("End time  : " + endTime.toString());

      bench.setTimes(startTime, endTime);

      // bench.setThreads(numThreads);
   }

   public static void readURLs(BenchMarker bench, File URLfile)
      throws IOException, MalformedURLException
   {
      if (!URLfile.canRead())
         throw new IOException("Cannot read " + URLfile.toString());

      InputStream in = new FileInputStream(URLfile);
      try
      {
         BufferedReader reader = new BufferedReader(
            new InputStreamReader(in));

         int lineNum = 0;
         String line = reader.readLine();
         while (line != null)
         {
            ++lineNum;
            line = line.trim();
            if (!line.startsWith("#") && line.length() != 0)
            {
               try
               {
                  URL u = new URL(line);
                  bench.addURL(u);
               }
               catch (MalformedURLException e)
               {
                  System.out.println("Error on line " + lineNum + ": " + e.getMessage());
               }
            }
            line = reader.readLine();
         }
      }
      finally
      {
         try { in.close(); } catch (IOException e) { /* ignore */ }
      }
   }

   public static Date parseDateTime(String dateTime)
   {
      // date+time patterns
      for (int i = 0; i < ms_datePatternArray.length; i++)
      {
         SimpleDateFormat dateFormat
            = new java.text.SimpleDateFormat(ms_datePatternArray[i]);
         try
         {
            Date d = dateFormat.parse(dateTime);
            // System.out.println("Used format " + dateFormat.toPattern());
            return d;
         }
         catch (java.text.ParseException e)
         {
            continue;
         }
      }

      // System.out.println("Time-only patterns...");

      // try time-only patterns (assume current date)
      for (int i = 0; i < ms_timePatternArray.length; i++)
      {
         SimpleDateFormat dateFormat
            = new java.text.SimpleDateFormat(ms_timePatternArray[i]);
         try
         {
            Date timePart = dateFormat.parse(dateTime);
            Date datePart = new Date(); // assume current date
            datePart.setHours(timePart.getHours());
            datePart.setMinutes(timePart.getMinutes());
            datePart.setSeconds(timePart.getSeconds());
            return datePart;
         }
         catch (java.text.ParseException e)
         {
            continue;
         }
      }

      return null;
   }   
   
   /**
    * An array of pre-set date pattern string to be used to determine whether a given
    * string/text is recognizable as a date. In order to be recognized as a date more
    * efficiently, it is better for a string to include year, month, and date. Some
    * popular date patterns are NOT supported here, such as "MM/dd/yyyy" and "dd/MM/yyyy".
    * That's because in JAVA, for example, "03/30/1999" and "03/30/99" would be recognized
    * recognized respectively as March 30, 1999 AD and March 30, 99 AD. But in daily life,
    * people tend to regard both expression as the same.
    */
   private static String[] ms_datePatternArray =
   {
      "MM/dd/yy 'at' hh:mm:ss aaa",
      "MM/dd/yy hh:mm:ss aaa",
      "MM/dd/yy 'at' hh:mm:ssaaa",
      "MM/dd/yy hh:mm:ssaaa",

      "MM/dd/yy 'at' HH:mm:ss",
      "MM/dd/yy HH:mm:ss",
      "MM/dd/yy 'at' HH:mm:ss",
      "MM/dd/yy HH:mm:ss",

      "MM/dd/yyyy 'at' hh:mm:ss aaa",
      "MM/dd/yyyy hh:mm:ss aaa",
      "MM/dd/yyyy 'at' hh:mm:ssaaa",
      "MM/dd/yyyy hh:mm:ssaaa",

      "MM/dd/yyyy 'at' HH:mm:ss",
      "MM/dd/yyyy HH:mm:ss",
      "MM/dd/yyyy 'at' HH:mm:ss",
      "MM/dd/yyyy HH:mm:ss",

      "yyyy-MMMM-dd 'at' hh:mm:ss aaa",
      "yyyy-MMMM-dd 'at' hh:mm:ssaaa",
      "yyyy-MMMM-dd HH:mm:ss",
      "yyyy.MMMM.dd 'at' hh:mm:ss aaa",
      "yyyy.MMMM.dd 'at' hh:mm:ssaaa",
      "yyyy.MMMM.dd HH:mm:ss",
      "yyyyMMdd HH:mm:ss",
      "yyyy-MM-dd G 'at' HH:mm:ss",
      "yyyy-MM-dd HH:mm:ss.SSS",
      "yyyy-MM-dd HH:mm:ss",
      "yyyy.MM.dd G 'at' HH:mm:ss",
      "yyyy.MM.dd HH:mm:ss.SSS",
      "yyyy.MM.dd HH:mm:ss",
      "yyyy/MM/dd G 'at' HH:mm:ss",
      "yyyy/MM/dd HH:mm:ss.SSS",
      "yyyy/MM/dd HH:mm:ss"
   };

   // time-only formats (assume current date)
   private static String[] ms_timePatternArray =
   {
      "hh:mm:ss aaa",
      "hh:mm:ssaaa",
      "HH:mm:ss",
      "hh:mm aaa",
      "hh:mmaaa",
      "HH:mm",
   };

   /** where we write the results to */
   private static OutputStream ms_out;
}
