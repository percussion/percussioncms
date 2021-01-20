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


package com.percussion.installerbot;

/**
 * Indicates an error during driving a console application. 
 * 
 * @see IPSConsoleAppDriver 
 * @author Andriy Palamarchuk
 */
public class PSConsoleAppDriverException extends Exception
{
   /**
    * @see #setTimeOut(boolean)
    */
   private boolean m_timeOut;

   /**
    * Creates exception with specified message.
    */
   public PSConsoleAppDriverException(final String message) {
      super(message);
   }

   /**
    * Creates exception with specified root cause.
    */
   public PSConsoleAppDriverException(final Throwable rootCause) {
      super(rootCause);
   }

   /**
    * @see #setTimeOut(boolean)
    */
   public boolean isTimeOut()
   {
      return m_timeOut;
   }

   /**
    * Indicates whether exception was thrown because an expected pattern was not
    * found during specified period of time.
    * Is not set by the exceptions thrown by {@link IPSConsoleAppDriver}.
    * Instead can be used in upper-level logic constructing the exception to
    * indicate timeout/pattern mismatch.  
    */
   public void setTimeOut(boolean timeOut)
   {
      this.m_timeOut = timeOut;
   }
}
