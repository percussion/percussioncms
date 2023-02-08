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
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

/**
 * RxUpgrade logger.
 */
public class RxUpgradeLog
{

   private static final Logger log = LogManager.getLogger(RxUpgradeLog.class);

   /**
    * Log object into a upgrade print stream.
    * @param o any object to log, may be <code>null</code>.
    */
   public static void logIt(Object o)
   {
      try
      {
        
         //prepend with date&time, so that we could figure out when it was logged..  
         String time = ms_dateFormat.format(new Date());         

         if (o instanceof Throwable)
         {
            String s = getStackTraceAsString((Throwable)o);

            getPrintStream().println(time + " " + s);
         }
         else if (o instanceof String)
         {
            getPrintStream().println(time + " " + o.toString());
         }
      } catch (SAXException | IOException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }
   }
   
   /**
    * Get the stack trace for the specified exception as a string.
    *
    * @param   t        the throwable (usually an exception)
    */
   private static String getStackTraceAsString(java.lang.Throwable t)
   {
      // for unknown exceptions, it's useful to log the stack trace
      java.io.StringWriter stackTrace = new java.io.StringWriter();
      java.io.PrintWriter writer = new java.io.PrintWriter(stackTrace);
      t.printStackTrace(writer);
      writer.flush();

      return stackTrace.toString();
   }

   /**
    * Returns current print stream.
    * 
    * @return print stream, never <code>null</code>.
    * @throws FileNotFoundException
    * @throws IOException
    * @throws SAXException
    */
   private static PrintStream getPrintStream()
      throws FileNotFoundException, IOException, SAXException
   {
      if (m_ps!=null)
         return m_ps;

      String logFile = RxUpgrade.getLogFileDir() + "rxupgrade.log";
      try(FileOutputStream fo = new FileOutputStream(logFile, true)) {
         m_ps = new PrintStream(fo);
      }

      return m_ps;
   }

   /**
    * Print stream holder.
    */
   private static PrintStream m_ps = null;
   
   /**
    * Time stamp format.
    * i.e.: MM/dd/yy HH:mm:ss -> 09/29/04 18:31:28
    */
   private static FastDateFormat ms_dateFormat =
       FastDateFormat.getInstance("MM/dd/yy HH:mm:ss"); //ie: 09/29/04 18:31:28

}
