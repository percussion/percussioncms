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

package com.percussion.tablefactory;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Exception class to used to report general exceptions, or may be subclassed
 * if necessary.  Handles formatting of messages stored in the
 * PSJdbcTableFactoryResources resource bundle using error codes and arguments.
 * Localization is also supported.
 */
public class PSJdbcTableFactoryException extends Exception
{
   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode The error string to load.
    *
    * @param singleArg The argument to use as the sole argument in
    *    the error message, may be <code>null</code>.
    */
   public PSJdbcTableFactoryException(int msgCode, Object singleArg)
   {
      this(msgCode, new Object[] { singleArg });
   }

   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode The error string to load.
    *
    * @param singleArg The argument to use as the sole argument in
    *    the error message, may be <code>null</code>.
    * @param t the exception which this exception encapsulates, may be
    * <code>null</code>
    */
   public PSJdbcTableFactoryException(int msgCode, Object singleArg,
      Throwable t)
   {
      this(msgCode, new Object[] { singleArg }, t);
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode The error string to load.
    *
    * @param arrayArgs The array of arguments to use as the arguments
    *    in the error message.  May be <code>null</code>, and may contain
    *    <code>null</code> elements.
    */
   public PSJdbcTableFactoryException(int msgCode, Object[] arrayArgs)
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
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode The error string to load.
    *
    * @param arrayArgs The array of arguments to use as the arguments
    *    in the error message.  May be <code>null</code>, and may contain
    *    <code>null</code> elements.
    * @param t the exception which this exception encapsulates, may be
    * <code>null</code>
    */
   public PSJdbcTableFactoryException(int msgCode, Object[] arrayArgs,
      Throwable t)
   {
      this(msgCode, arrayArgs);
      m_th = t;
   }

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode The error string to load.
    */
   public PSJdbcTableFactoryException(int msgCode)
   {
      this(msgCode, null, null);
   }

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode The error string to load.
    * @param t the exception which this exception encapsulates, may be
    * <code>null</code>
    */
   public PSJdbcTableFactoryException(int msgCode, Throwable t)
   {
      this(msgCode, null, t);
   }

   /**
    * Prints this Throwable and its backtrace to the standard error stream.
    * This method prints a stack trace for this Throwable object on the error
    * output stream that is the value of the field System.err.
    */
   public void printStackTrace()
   {
      if (m_th == null)
         super.printStackTrace();
      else
         m_th.printStackTrace();
   }

   /**
    * Prints this Throwable and its backtrace to the specified print stream.
    * @param s PrintStream to use for output
    */
   public void printStackTrace(PrintStream s)
   {
      if (m_th == null)
         super.printStackTrace(s);
      else
         m_th.printStackTrace(s);
   }

   /**
    * Prints this Throwable and its backtrace to the specified print writer.
    * @param s PrintWriter to use for output
    */
   public void printStackTrace(PrintWriter s)
   {
      if (m_th == null)
         super.printStackTrace(s);
      else
         m_th.printStackTrace(s);
   }

   /**
    * Returns the localized detail message of this exception.
    *
    * @param locale The locale to generate the message in.  If <code>null
    *    </code>, the default locale is used.
    *
    * @return  The localized detail message
    */
   public String getLocalizedMessage(Locale locale)
   {
      return createMessage(m_code, m_args, locale);
   }

   /**
    * Returns the localized detail message of this exception in the
    * default locale for this system.
    *
    * @return The localized detail message, never <code>null</code>.
    */
   public String getLocalizedMessage()
   {
      return getLocalizedMessage(Locale.getDefault());
   }

   /**
    * Returns the detail message of this exception.
    *
    * @return The detail message, never <code>null</code>.
    */
   public String getMessage()
   {
      return getLocalizedMessage();
   }

   /**
    * Returns a description of this exception. The format used is
    * "ExceptionClass: ExceptionMessage"
    *
    * @return the description
    */
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
    * @param   t         the throwable (usually an exception)
    */
   public static String getStackTraceAsString(Throwable t)
   {
      // for unknown exceptions, it's useful to log the stack trace
      StringWriter stackTrace = new StringWriter();
      PrintWriter writer = new PrintWriter(stackTrace);
      t.printStackTrace(writer);
      writer.flush();

      return stackTrace.toString();
   }

   /**
    * Create a formatted message for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode The error string to load.
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
   public static String createMessage(int msgCode, Object[] arrayArgs,
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

         msg = String.valueOf(msgCode) + ": " + sArgs;
      }

      return msg;
   }


   /**
    * Get the error text associated with the specified error code.
    *
    * @param code The error code.
    *
    * @param nullNotFound  If <code>true</code>, return <code>null</code> if the
    *    error string is not found, if <code>false</code>, return the code as
    *    a String if the error string is not found.
    *
    * @param loc The locale to use, may be <code>null</code>, in which case the
    * default locale is used.
    *
    * @return the error text, may be <code>null</code>.
    */
   public static String getErrorText(int code, boolean nullNotFound, Locale loc)
   {
      if (loc == null)
         loc = Locale.getDefault();

      try
      {
         ResourceBundle errList = getErrorStringBundle(loc);
         if (errList != null)
            return errList.getString(String.valueOf(code));
      }
      catch (MissingResourceException e)
      {
         /* use the default listed below, just don't exception */
      }

      return (nullNotFound ? null : String.valueOf(code));
   }

   /**
    * Returns a formatted string containing the test of all of the exceptions
    * contained in the supplied SQLException.
    * <p>There seems to be a bug in the Sprinta driver. We get an exception
    * for Primary key constraint violation, which has a sql warning as the
    * next exception (warning). But this next warning has a circular
    * reference to itself in the next link. So we check for this problem and
    * limit the max errors we will process.
    *
    * @param details details of the request, if <code>null</code> then
    *    this is ignored.
    * @param e The exception to process. If <code>null</code>, an empty
    *    string is returned.
    *
    * @return The string, never <code>null</code>, may be empty.
    */
   public static String formatSqlException(String details, SQLException e)
   {
      if (details != null)
      {
         return "Details: " + details + " "
            + formatSqlException(e);
      }
      else
      {
         return formatSqlException(e);
      }
   }
   
   /**
    * Returns a formatted string containing the test of all of the exceptions
    * contained in the supplied SQLException.
    * <p>There seems to be a bug in the Sprinta driver. We get an exception
    * for Primary key constraint violation, which has a sql warning as the
    * next exception (warning). But this next warning has a circular
    * reference to itself in the next link. So we check for this problem and
    * limit the max errors we will process.
    *
    * @param e The exception to process. If <code>null</code>, an empty
    *    string is returned.
    *
    * @return The string, never <code>null</code>, may be empty.
    */
   public static String formatSqlException(SQLException e)
   {
      if ( null == e )
         return "";

      StringBuffer errorText   = new StringBuffer();

      int errNo = 1;
      final int maxErrors = 20;
      for ( ; e != null && errNo <= maxErrors; )
      {
         errorText.append( "[" );
         errorText.append( errNo );
         errorText.append( "] " );
         errorText.append( e.getSQLState());
         errorText.append( ": " );
         errorText.append( e.getMessage());
         errorText.append( " " );
         SQLException tmp = e.getNextException();
         if ( e == tmp )
            break;
         else
            e = tmp;
         errNo++;
      }
      if ( errNo == maxErrors + 1 )
      {
         errorText.append( "[Maximum # of error messages (" );
         errorText.append( maxErrors );
         errorText.append(  ") exceeded. Rest truncated]" );
      }

      return errorText.toString();
   }



   /**
    * This method is used to get the string resources hash table for a
    * locale. If the resources are not already loaded for the locale,
    * they will be.
    *
    * @param loc The locale, assumed not <code>null</code>.
    */
   private static ResourceBundle getErrorStringBundle(Locale loc)
      throws MissingResourceException
   {
      if (ms_bundle == null)
      {
         ms_bundle = ResourceBundle.getBundle(
            "com.percussion.tablefactory.PSJdbcTableFactoryResources", loc);
      }

      return ms_bundle;
   }

   /**
    * stores the encapsulated exception, may be <code>null</code>
    */
   protected Throwable m_th = null;

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

   /**
    * The resource bundle containing error message formats.  <code>null</code>
    * until the first call to {@link #getErrorStringBundle(Locale)
    * getErrorStringBundle}, never <code>null</code> or modified after that
    * unless an exception occurred loading the bundle.
    */
   private static ResourceBundle ms_bundle = null;


}


