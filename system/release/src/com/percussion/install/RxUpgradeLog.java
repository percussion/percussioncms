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
package com.percussion.install;

import org.apache.commons.lang3.time.FastDateFormat;
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
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      catch (SAXException e)
      {
         e.printStackTrace();
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
