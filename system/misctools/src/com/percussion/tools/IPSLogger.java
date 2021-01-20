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

package com.percussion.tools;


/**
 * This interface defines a few methods to log the messages so that any plugin
 * can use.
 */
public interface IPSLogger
{
   /**
    * Logs the message to whatever output the Logger object was initialized with
    * no matter whether the Logger is in debug mode or not. This method is
    * normally used to log a mandatory message.
    *
    * @param msg - the message to be logged, must not be <code>null</code>.
    *
    * @throws IllegalArgumentException if the msg is <code>null</code> or
    * <code>empty</code>.
    *
    * @deprecated use log4j methods.
    */
   void logMessage(String msg);

   /**
    * Logs the message to whatever output the Logger object was initialized with
    * only if Logger is in debug mode. This method is normally used to log a
    * extra information for debug purpose.
    *
    * @param msg - the message to be logged, must not be <code>null</code>.
    *
    * @throws IllegalArgumentException if the msg is <code>null</code> or
    * <code>empty</code>.
    *
    * @deprecated use log4j methods.
    */
   void logDebugMessage(String msg);

   /**
    * If an <code>IOException</code> occurs while logging, the exception will
    * be available through this method.
    * @return the exception message or <code>null</code> if no exception
    * occurred.
    *
    * @deprecated use log4j methods.
    */
   String getLoggerError();

   /**
    * Resets the logger exception string to <code>null</code>.
    * @deprecated use log4j methods.
    */
   void resetLoggerError();
}