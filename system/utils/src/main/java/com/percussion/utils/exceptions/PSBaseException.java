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
package com.percussion.utils.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Abstract base class to handle loading exception messages and formatting
 * message strings, handling arguments and using error codes as keys to the 
 * bundle.  Derived exception classes need to implement 
 * {@link #getResourceBundleBaseName()} to provide the bundle class name or
 * fully qualified properties file name. 
 */
public abstract class PSBaseException extends Exception
{
   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode The code of the error string to load.
    *
    * @param arrayArgs The array of arguments to use as the arguments
    *    in the error message.  May be <code>null</code>, and may contain
    *    <code>null</code> elements.
    */
   public PSBaseException(int msgCode, Object... arrayArgs)
   {
      for (int i = 0; arrayArgs != null && i < arrayArgs.length; i++)
      {
         if (arrayArgs[i] == null)
            arrayArgs[i] = "";
      }

      m_code = msgCode;
      m_args = arrayArgs;
   }

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode The error string to load.
    */
   public PSBaseException(int msgCode)
   {
      this(msgCode, (Object[]) null);
   }

   /**
    * Same as {@link #PSBaseException(int, Object...)} but takes one additional
    * parameter to indicate the exception that caused this exception.
    * @param msgCode The code of the error string to load.
    * @param cause The original exception that caused this exception to be
    * thrown, may be <code>null</code>.
    * @param arrayArgs The array of arguments to use as the arguments
    *    in the error message.  May be <code>null</code>, and may contain
    *    <code>null</code> elements.
    */
   public PSBaseException(int msgCode, Throwable cause, Object... arrayArgs)
   {
      this(msgCode, arrayArgs);
      fillInStackTrace();
      initCause(cause);
   }

   
   /**
    * Returns the localized detail message of this exception.
    *
    * @param locale The locale to generate the message in.  If <code>null
    *    </code>, the default locale is used.
    *
    * @return  The localized detail message, never <code>null</code>, may be
    * empty.
    */
   public String getLocalizedMessage(Locale locale)
   {
      return createMessage(m_code, m_args, locale);
   }

   /**
    * Returns the localized detail message of this exception in the
    * default locale for this system.
    *
    * @return  The localized detail message, never <code>null</code>, may be
    * empty.
    */
   @Override
   public String getLocalizedMessage()
   {
      return getLocalizedMessage(Locale.getDefault());
   }

   /**
    * Returns the localized detail message of this exception in the
    * default locale for this system.
    *
    * @return  The localized detail message, never <code>null</code>, may be
    * empty.
    */
   @Override
   public String getMessage()
   {
      return getLocalizedMessage();
   }

   /**
    * Returns a description of this exception. The format used is
    * "ExceptionClass: ExceptionMessage"
    *
    * @return the description, never <code>null</code> or empty.
    */
   @Override
   public String toString()
   {
      return this.getClass().getName() + ": " + getLocalizedMessage();
   }

   /**
    * Get the parsing error code associated with this exception.
    *
    * @return The error code
    */
   public int getErrorCode()
   {
      return m_code;
   }

   /**
    * Get the parsing error arguments associated with this exception.
    *
    * @return The error arguments, may be <code>null</code>.
    */
   public Object[] getErrorArguments()
   {
      return m_args;
   }

   /**
    * Get the stack trace for the specified exception as a string.
    *
    * @param t The throwable (usually an exception), never <code>null</code>.
    */
   public static String getStackTraceAsString(Throwable t)
   {
      if (t == null)
         throw new IllegalArgumentException("t may not be null");

      // for unknown exceptions, it's useful to log the stack trace
      StringWriter stackTrace = new StringWriter();
      PrintWriter writer = new PrintWriter(stackTrace);
      t.printStackTrace(writer);
      writer.flush();
      writer.close();

      return stackTrace.toString();
   }

   /**
    * Create a formatted message for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode The code of the error string to load.
    *
    * @param arrayArgs  The array of arguments to use as the arguments
    *    in the error message, may be <code>null</code> or empty.
    *
    * @param loc The locale to use, may be <code>null</code>, in which case the
    *    default locale is used.
    *
    * @return The formatted message, never <code>null</code>. If the appropriate
    *    message cannot be created, a message is constructed from the msgCode
    *    and args and is returned.
    *
    */
   private String createMessage(int msgCode, Object[] arrayArgs,
      Locale loc)
   {
      if (arrayArgs == null)
         arrayArgs = new Object[0];


      String msg = getErrorText(msgCode, true, loc);

      if (msg != null)
      {
         try
         {
            msg = MessageFormat.format(msg, arrayArgs);
         }
         catch (IllegalArgumentException e)
         {
            // some problem with formatting
            msg = null;
         }
      }

      if (msg == null)
      {
         String sArgs = "";
         String sep = "";

         for (int i = 0; i < arrayArgs.length; i++) {
            sArgs += sep + arrayArgs[i].toString();
            sep = "; ";
         }

         msg = "" + String.valueOf(msgCode) + ": " + sArgs;
      }

      return msg;
   }


   /**
    * Get the error text associated with the specified error code.
    * 
    * @param code The error code.
    * 
    * @param nullNotFound If <code>true</code>, return <code>null</code> if
    * the error string is not found, if <code>false</code>, return the code
    * as a string if the error string is not found.
    * 
    * @param loc The locale to use, may be <code>null</code>, in which case
    * the default locale is used.
    * 
    * @return the error text, may be <code>null</code> or empty.
    */
   public String getErrorText(int code, boolean nullNotFound, Locale loc)
   {
      if (loc == null)
         loc = Locale.getDefault();

      ResourceBundle errorList = null;
      String errorMsg = null;
      try
      {
         errorList = getErrorStringBundle(loc);
         if (errorList != null)
         {
            errorMsg = errorList.getString(String.valueOf(code));
            return errorMsg;
         }
      }
      catch (MissingResourceException e)
      {
         // let the nullNotFound deal with this at the end.
      }

      return (nullNotFound ? null : String.valueOf(code));
   }   
   
   
   /**
    * Get the base name of the resource bundle, a fully qualified class name
    *
    * @return The base name of the resource bundle, never <code>null</code>
    *    or empty.
    */
   abstract protected String getResourceBundleBaseName();
   
   /**
    * Get the default resource bundle for the specified locale.
    * 
    * @param loc The locale of the resource bundle, it may be <code>null</code>.
    * 
    * @return The default resource bundle, never <code>null</code>.
    * 
    * @throws MissingResourceException if fail to load the default resource
    *    bundle.
    */
   private ResourceBundle getErrorStringBundle(Locale loc)
      throws MissingResourceException
   {
      if (m_bundle == null)
      {
         m_bundle = ResourceBundle.getBundle(
            getResourceBundleBaseName(), loc);
      }
      
      return m_bundle;
   }

   /**
    * The resource bundle containing error message formats.  <code>null</code>
    * until the first call to {@link #getErrorStringBundle(Locale)
    * getErrorStringBundle}, never <code>null</code> or modified after that
    * unless an exception occurred loading the bundle.
    */
   private ResourceBundle m_bundle = null;
   
   /**
    * The error code of this exception, set during ctor, never modified after
    * that.
    */
   private int m_code;

   /**
    * The array of arguments to use to format the message with.  Set during
    * ctor, may be <code>null</code>, never modified after that.
    */
   private Object[] m_args;
}

