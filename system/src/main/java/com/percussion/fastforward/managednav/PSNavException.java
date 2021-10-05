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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.fastforward.managednav;

import com.percussion.error.PSExceptionUtils;
import com.percussion.error.PSRuntimeException;

/**
 * A general purpose exception for the Navigation package.
 * 
 * @author DavidBenua
 *  
 */
public class PSNavException extends PSRuntimeException
{
   /**
    * Creates an exception with a specific message.
    * 
    * @param msg
    */
   @Deprecated
   public PSNavException(String msg)
   {
      super(-1, msg);
      this.m_msg.append(msg);
   }

   /**
    * Creates an exception that encapsulates a runtime exception.
    * 
    * @param ex the exception to encapsulate
    */
   @Deprecated
   public PSNavException(Exception ex)
   {
      super(-1, null,ex);

      if (ex instanceof PSNavException)
      { // just clone
         this.m_parentException = ((PSNavException) ex).m_parentException;
         this.m_msg = ((PSNavException) ex).m_msg;
      }
      else
      {
         this.m_parentException = ex;
         this.m_msg.append(handleException(ex));
      }

   }

   /**
    * Creates an exception that encapsulates an exception and a label for the
    * calling routine.
    * 
    * @param routine the routine where the exception occurred.
    * @param ex the exception to encapsulate.
    */
   @Deprecated
   public PSNavException(String routine, Exception ex)
   {
      this(ex);
      this.m_msg.append("Caught in " + routine);
   }

   /**
    * A convenience method
    * 
    * @param clazz the class where the exception occurred.
    * @param ex the exception to encapsulate.
    */
   @Deprecated
   public PSNavException(Class clazz, Exception ex)
   {
      this(clazz.getName(), ex);
   }

   /**
    * Creates a standard message string for an exception.
    * 
    * @param ex the exception.
    * @return the error message. Never <code>null</code>.
    */
   public static String handleException(Exception ex)
   {
      return PSExceptionUtils.getMessageForLog(ex);
   }

   /***
    * Create a new exception with the specified error code.
    * @param code A valid code from IPSErrorCatalog
    */
   public PSNavException(int code){
      super(code);
   }

   /***
 * Create a new exception withe specific error code
 * and message parameters.
 * @param code A valid code from IPSErrorCatalog
 * @param arrayArgs An array of objects to be used in error message rendering
 * @param cause The underlying cause for this exception to be thrown.
 */
public PSNavException(int code, Object[] arrayArgs, Throwable cause){
   super(code, arrayArgs, cause);
}

   /***
    * Create a new exception withe specific error code
    * and message parameters.
    * @param code A valid code from IPSErrorCatalog
    * @param arrayArgs An array of objects to be used in error message rendering
    */
   public PSNavException(int code, Object[] arrayArgs){
      super(code, arrayArgs);
   }

   /***
    * Create a new exception withe specific error code
    * and message parameters.
    * @param code A valid code from IPSErrorCatalog
    * @param arrayArgs An array of objects to be used in error message rendering
    */
   public PSNavException(int code, Object arg){
      super(code, arg);
   }

   /**
    * The underlying exception that caused this exception.
    */
   @Deprecated
   Exception m_parentException = null;

   /**
    * The message buffer for this exception.
    */
   @Deprecated
   StringBuilder m_msg = new StringBuilder();

}

