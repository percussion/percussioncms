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
