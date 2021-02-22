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
package com.percussion.utils.timing;

import org.apache.commons.logging.Log;
import org.apache.logging.log4j.Logger;

/**
 * A utility class used to log elapsed time.
 */
public class PSTimer
{
   PSStopwatch m_watch = new PSStopwatch();
   Log m_log;
   Logger log;

   public PSTimer(Log log)
   {
      m_log = log;
      m_watch.start();
   }

   public PSTimer(Logger log){
      this.log = log;
      m_watch.start();
   }
   /**
    * Log the message along with the elapsed time.
    * @param msg the log message, assumed not <code>null</code>.
    */
   public void logElapsed(String msg)
   {
      m_watch.stop();
      if(m_log!=null)
         m_log.debug(msg + " " + m_watch.toString());
      else
         log.debug(msg +" " +m_watch.toString());
   }
}
