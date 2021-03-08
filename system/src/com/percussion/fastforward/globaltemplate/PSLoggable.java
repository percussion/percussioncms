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
package com.percussion.fastforward.globaltemplate;


import org.apache.logging.log4j.Logger;

/**
 * Base class for the classes that need basic {@link Logger logging} 
 * functionality of Apache log4j. Has easy methods to log info, 
 * warnings and errors. 
 * 
 * @author RammohanVangapalli
 */
public abstract class PSLoggable
{
   /**
    * Constructor that takes an existing instance of the logger object.
    * @param logger if <code>null</code> an instance will be created for the 
    * category name with this class name.
    */
   PSLoggable(Logger logger)
   {
      m_Logger = logger;
   }

   /**
    * Log the supplied message as an error. Delegates to {@link org.apache
    * .log4j.Logger#error(Object)}
    * @see org.apache.log4j.Logger#error(Object)
    * @param obj
    */
   protected void logError(Object obj)
   {
      if (m_Logger != null)
         m_Logger.error(obj);
   }

   /**
    * Log the supplied message as a warning. Delegates to {@link org.apache
    * .log4j.Logger#warn(Object)}
    * @see org.apache.log4j.Logger#warn(Object)
    * @param obj
    */
   protected void logWarning(Object obj)
   {
      if (m_Logger != null)
         m_Logger.warn(obj);
   }

   /**
    * Log the supplied message as information. Delegates to {@link org.apache
    * .log4j.Logger#info(Object)}
    * @see org.apache.log4j.Logger#info(Object)
    * @param obj
    */
   protected void logInfo(Object obj)
   {
      if (m_Logger != null)
         m_Logger.info(obj);
   }

   /**
    * Logger object to log the information, initialized in the ctor and may 
    * be <code>null</code> in which case the info will not be logged.
    */
   protected Logger m_Logger = null;
}
