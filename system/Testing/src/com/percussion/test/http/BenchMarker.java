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

import com.percussion.test.io.*;

import java.net.URL;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.*;

import java.util.Random;

/**
 * The BenchMarker class will run a given test series
 * and collect statistics on the performance of the
 * server.
 */
public class BenchMarker implements Runnable, LogSink
{
   public BenchMarker()
   {
   }
   
   public void setTimes(Date startTime, Date endTime)
      throws IllegalArgumentException
   {
      // sanity check the start and end time
      if (startTime.getTime() < (System.currentTimeMillis() + 5))
      {
         throw new IllegalArgumentException(
            "Error: start time has already passed or will pass within 5 seconds.");
      }
      
      long duration = endTime.getTime() - startTime.getTime();
      
      if (duration < 0)
      {
         throw new IllegalArgumentException(
            "Error: start time is after end time");
      }
      
      log("Test will last " + ((double)duration / (double)60000L) + " minutes.");
      
      m_startTime = startTime;
      m_endTime = endTime;
   }
   
   public void waitForStart() throws InterruptedException
   {
      // now sleep until the start time
      long sleepTime = m_startTime.getTime() - System.currentTimeMillis();
      if (sleepTime > 0)
      {
         log("Sleeping for " + ((double)sleepTime / (double)60000L) + " minutes...");
         Thread.sleep(sleepTime);
      }
      
      log("Finished sleeping.");
   }
   
   public void addURL(URL url)
   {
      if (url == null)
         throw new IllegalArgumentException("Null URL specified");
      m_urls.add(url);
   }

   /**
    * Runs the test sequence.
    *
    */
   public void run()
   {
      if (m_urls.size() == 0)
      {
         log("There are no URLs to test.");
         return;
      }

      Random rand = null;
      if (m_seed != 0)
         rand = new Random(m_seed);
      else
         rand = new Random();

      ms_stats = new HttpRequestStatistics();
      ms_stats.startedTimingAt(System.currentTimeMillis());
      while (System.currentTimeMillis() < m_endTime.getTime())
      {
         URL url = (URL)m_urls.get(rand.nextInt(m_urls.size()));
         log("Getting " + url.toString());
         HttpRequest request = null;
         InputStream stream = null;            
         HttpRequestTimings timings = null;

         try
         {
            request = new HttpRequest(url);

            request.enableTrace(this);
            request.sendRequest();
            timings = request.getTimings();
            stream = request.getResponseContent();
            long bytes = readStream(stream, 8192);
            timings.afterContent(System.currentTimeMillis());
            timings.contentBytes(bytes);
         }
         catch (Throwable t)
         {
            log(t);
         }
         finally
         {
            try { if (request != null) request.disconnect(); } catch (Throwable e) { /* ignore */ }
            try { if (stream != null) stream.close(); } catch (IOException e) { /* ignore */ }
         }
         ms_stats.add(timings);
      }
      ms_stats.finishedTimingAt(System.currentTimeMillis());

      log("Finished testing.");

//       Properties props = new Properties();
//       DataLoader ldr = new PropsLoader(props, this);
   }

   public void writeResults(OutputStream out) throws IOException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      DataLoader ldr = new XmlDataLoader(doc, "HttpBench", this);
      ms_stats.store(ldr, "Stats");
      PSXmlDocumentBuilder.write(doc, out);
   }

   public void writeResults(PrintStream out) throws IOException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      DataLoader ldr = new XmlDataLoader(doc, "HttpBench", this);
      ms_stats.store(ldr, "Stats");
      PSXmlDocumentBuilder.write(doc, out);
   }
   
   public long readStream(InputStream in, int bufSize) throws IOException
   {
      byte[] buf = new byte[bufSize];
      long bytesSent = 0L;

      while (true)
      {
         int read = in.read(buf);
         
         if (read < 0)
            break; // end of input stream reached

         bytesSent += read;
      }

      return bytesSent;
   }

   /**
    * Logs the mesage.
    * @author   chadloder
    * 
    * @version 1.2 1999/08/20
    * 
    * 
    * @param   message
    * 
    */
   public void log(String message)
   {
      System.out.print(message + "\n");
   }

   /**
    * Logs the exception, including a stack trace.
    *
    * @author   chadloder
    * 
    * @version 1.2 1999/08/20
    * 
    * @param   t
    * 
    */
   public void log(Throwable t)
   {
      t.printStackTrace();
   }

   /**
    * Logs the exception, including a stack trace, and a message.
    * If the message is null, it will not be logged.
    *
    * @author   chadloder
    * 
    * @version 1.2 1999/08/20
    * 
    * @param   message
    * @param   t
    * 
    */
   public void log(String message, Throwable t)
   {
      log(message);
      log(t);
   }

   public void setRandomSeed(int seed)
   {
      m_seed = seed;
   }

   private int m_seed = 0;
   private List m_urls = new ArrayList();
   private Date m_startTime;
   private Date m_endTime;

   private HttpRequestStatistics ms_stats;
}
