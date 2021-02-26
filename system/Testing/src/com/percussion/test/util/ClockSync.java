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
package com.percussion.test.util;

import com.percussion.test.statistics.AverageAccumulator;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Date;

/**
 * This class retrieves the exact time and date according to a server
 * implementing a NIST-style atomic clock service.
 *
 * Since there is no way to set the date from Java, you should use this
 * class to obtain the offset between the Java time and the official time.
 */
public class ClockSync
{
   /**
    * Test the class
    */
   public static void main(String[] args)
   {
      try
      {
         ClockSync.syncTimeDOS();
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
   }

   /**
    * Construct a clock sync that connects to the default server and port
    */
   public ClockSync()
   {
   }

   /**
    * Construct a clock sync that connects to the specified server
    * and port.
    */
   public ClockSync(String timeServerAddress, int timePort)
   {
      m_timeSrvAddr = timeServerAddress;
      m_timeSrvPort = timePort;
   }

   public static void syncTimeDOS() throws IOException
   {
      System.out.println("Querying time...");
      ClockSync sync = new ClockSync();
      sync.measure();

      Date correctedTime = sync.correctDate();
      double offsetSec = sync.getOffsetSec();

      if (Math.abs(offsetSec) < 0.8)
      {
         System.out.println("Your clock was only off by " + offsetSec + " seconds. Unchanged.");
         return;
      }

      FastDateFormat dosFmt = FastDateFormat.getInstance("HH:mm:ss.SS");
      String dosTime = dosFmt.format(correctedTime);
      String timeCmd = "command";
      String os = System.getProperty("os.name");
      if (os.equals("Windows NT"))
         timeCmd = "cmd";

      timeCmd = timeCmd + " /c time " + dosTime;
      System.out.println("Executing command: " + timeCmd);
      Runtime.getRuntime().exec(timeCmd);

      // print out a message
      String suffix = (offsetSec < 0) ? "fast" : "slow";
      if (offsetSec < 0)
         offsetSec = -offsetSec;

      System.out.println("Your system clock was " + offsetSec +
         " seconds too " + suffix + ".");
   }

   /**
    * Measures the time difference between this machine's clock
    * and the server clock.
    *
    * The algorithm takes several samples, taking the average of
    * each sample's difference.
    */
   public void measure() throws IOException
   {
      // take the average of six runs
      AverageAccumulator avger = new AverageAccumulator();
      for (int i = 0; i < 6; i++)
      {
         avger.accumulate(getTimeDelta());
      }

      m_correctionMsec = (long)(avger.average());

      // System.out.println("Correction msec: " + m_correctionMsec);
   }

   /**
    * Gets the difference between the server clock and this computer's
    * clock. If this value is negative, the computer's clock is too
    * fast. If this value is positive, this computer's clock is too
    * slow.
    */
   public long getOffsetMsec()
   {
      return m_correctionMsec;
   }

   public double getOffsetSec()
   {
      return (double)getOffsetMsec() / (double)1000;
   }

   /**
    * Return the given date, corrected by the offset.
    */
   public void correctDate(Date d)
   {
      d.setTime(d.getTime() + getOffsetMsec());
   }

   public Date correctDate()
   {
      Date d = new Date();
      correctDate(d);
      return d;
   }


   /**
    * Get the msec delta between this computer's time and the
    * server time. Don't worry about time zones, because we
    * correct for this later.
    */
   private long getTimeDelta() throws IOException
   {
      long beforeRefresh = System.currentTimeMillis();
      String timeStr = refreshTime();
      long afterRefresh = System.currentTimeMillis();
      
      // System.out.println("Local machine: " + afterRefresh);

      long time = Long.parseLong(timeStr);

      // System.out.println("Server reports: " + time);

      // deltaT is the difference between the current time
      // (averaged over the socket request) and the server time
      long deltaT = time - ((afterRefresh + beforeRefresh) / 2L);

      // System.out.println("The time delta is " + deltaT + "ms");

      return deltaT;
   }

   private String refreshTime() throws IOException
   {
      Socket socket = new Socket(m_timeSrvAddr, m_timeSrvPort);
      try
      {
         BufferedReader reader = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
         
         String timeStr = reader.readLine();

         if (timeStr == null || timeStr.length() == 0 || timeStr.equals("error"))
         {
            throw new IOException("Error getting time from server: " + timeStr);
         }

         return timeStr;
      }
      finally
      {
         try { socket.close(); } catch (IOException e) { /* ignore */ }
      }
   }

   /** the time server address */
   private String m_timeSrvAddr = "www.boulder.nist.gov";

   /** the time server port */
   private int m_timeSrvPort = 8013; // the default port
   
   /**
    * The measured difference between this computer's clock
    * and the server's clock, in milliseconds.
    */
   private long m_correctionMsec = 0L;

}
