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

package com.percussion.error;

import java.util.Locale;
import org.apache.commons.lang.StringUtils;


/**
 * The PSException class is the base class for all internal exceptions.
 * It contains the basic constructors for building a language
 * independent error message from an error code and its expected parameters.
 * All error messages are stored using the format defined in
 * the java.text.MessageFormat class. The message string contains curly
 * braces around parameters, which are 0 based. The error manager provides
 * two utility methods which take advantage of the MessageFormat.format
 * method.
 * <p>
 * Let's assume we have a message with code 100. It's defined in the
 * error string properties file as:
 * <PRE><CODE>
 *    100=param 1={0}, param 2 date={1,date}, param 2 time={1,time}
 * </CODE></PRE>
 *
 * To populate the message with an integer as the first parameter and the
 * current date as the second parameter, we need to build the following
 * Object array:
 * <PRE><CODE>
 *    Object[] args = { new Integer(1), new Date() };
 * </CODE></PRE>
 *
 * When the message is retrieved from the exception, it will look as follows:
 * <PRE><CODE>
 *    param1=1, param 2 date=Jan 6, 1999, param 2 time=4:50 PM
 * </CODE></PRE>
 *
 * This model is excellent for internationalization as the position of the
 * parameters may change based upon the target language. The exception
 * class also allows the message from a single exception to be retrieved
 * with different locales. This is essential for logging to the server
 * in one locale and displaying the text for a client using a different
 * locale.
 * <P>
 * To perform customized error message generation, the only method
 * that needs to be overriden is
 * {@link #getLocalizedMessage(java.util.Locale) getLocalizedMessage}.
 * <p>
 *
 * Note:As part of of partial i18n, three new constructors are added in which
 * the first parameter is a language string in the syntax
 * [language]-[country]-[variant]
 * In this language, country and variant have the same meaning as in Java, the
 * only difference is that it should all be in lower case. If the exception is
 * instantiated with any of these three constructors, the message string text is
 * looked up from a TMX resource bundle not the java resource bundle.
 *
 * Note for derived classes: the final message is not actually created until a
 * call to one of the get... methods has been made. This allows passing in an
 * Object[] that has not been fully defined in the beginning of the ctor.
 *
 * @see com.percussion.error.IPSErrorManager
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSException extends java.lang.Exception
   implements IPSException
{

   private static IPSErrorManager errorManager = new PSErrorManagerDefaultImpl();

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   /**
    * Default constructor
    */
   public PSException()
   {
      super();
   }

   /**
    * Constructor that takes the error message
    * @param msg should not be <code>null</code>
    */
   public PSException(String msg)
   {
      super(msg);
   }

   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param   msgCode         the error string to load
    *
    * @param   singleArg      the argument to use as the sole argument in
    *   the error message. Passing <code>null</code> is equivalent to calling 
    * {@link #PSException(int, Object[]) PSException(msgCode, null)}. If
    * <code>singleArg</code> is an instance of <code>Throwable</code>,
    * then the argument is converted to message with the following format:
    * <p>[exception Name]: exception text
    */
   public PSException(int msgCode, Object singleArg)
   {
      this(msgCode, null == singleArg ? null : new Object[] { singleArg });
      if (singleArg instanceof Throwable)
      {
         Throwable t = (Throwable) singleArg;
         String arg = "[" + t.getClass().getName() + "]: " 
               + t.getLocalizedMessage();
         if (null == m_args)
            m_args = new Object[1];
         m_args[0] = arg;
      }
   }

   /**
    * Convenience ctor that calls {@link #PSException(String,int,Object[]) 
    * PSException(language, msgCode, null == singleArg ? null : 
    * new Object[] { singleArg })}.
    */
   public PSException(String language, int msgCode, Object singleArg)
   {
      this(language, msgCode, 
            null == singleArg ? null : new Object[] { singleArg });
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   msgCode         the error string to load
    *
    * @param   arrayArgs      the array of arguments to use as the arguments
    *   in the error message. May be <code>null</code>. <code>null</code> entries
    * are treated as "".
    */
   public PSException(int msgCode, Object[] arrayArgs)
   {
      this(msgCode, arrayArgs, null);
   }
   
   /**
    * Same as {@link #PSException(int, Object[])}, but allows to specify the
    * cause of the exception.
    * 
    * @param msgCode the error string to load.
    * @param arrayArgs the array of arguments to use as the arguments in the
    *           error message. May be <code>null</code>. <code>null</code>
    *           entries are treated as "".
    * @param cause The cause of the exception. May be <code>null</code>, in
    * that case it means the cause is unknown.
    */
   public PSException(int msgCode, Object[] arrayArgs, Throwable cause)
   {
      super(cause);
      m_code = msgCode;
      //this method validates our contract
      setArgs(arrayArgs);
   }

   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   language         language string, e.g. 'en-us', may be
    * <code>null</code> or <code>empty</code>.
    *
    * @param   msgCode         the error string to load
    *
    * @param   arrayArgs      the array of arguments to use as the arguments
    * in the error message. May be <code>null</code>. <code>null</code> entries
    * are treated as "".
    *
    */
   public PSException(String language, int msgCode, Object[] arrayArgs)
   {
      this(language, msgCode, arrayArgs, null);
   }
   
   /**
    * Same as {@link #PSException(String, int, Object[])} but allows to specify
    * the cause of the exception.
    * 
    * @param language language string, e.g. 'en-us', may be <code>null</code> or
    *           <code>empty</code>.
    * @param msgCode the error string to load.
    * @param arrayArgs the array of arguments to use as the arguments in the
    *           error message. May be <code>null</code>. <code>null</code>
    *           entries are treated as "".
    * @param cause The cause of the exception. May be <code>null</code>, in that
    *           case it means the cause is unknown.
    */
   public PSException(String language, int msgCode, Object[] arrayArgs, Throwable cause)
   {
      super(cause);
      m_code = msgCode;
      //method validates our contract
      setArgs(arrayArgs);
      m_lang = language;
   }

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param   msgCode         the error string to load
    */
   public PSException(int msgCode)
   {
      this(msgCode, null);
   }

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param   language         language string, e.g. 'en-us', may be
    * <code>null</code> or <code>empty</code>.
    *
    * @param   msgCode         the error string to load
    */
   public PSException(String language, int msgCode)
   {
      this(language, msgCode, null);
   }

   /**
    * Create a chained exception with a specific message
    * @param message message to use in exception. If 
    * <code>null</code> then use the localized message from the original
    * exception
    * @param e the original exception, never <code>null</code>
    */
   public PSException(String message, Throwable e) {
      super(message, e);
      if (e == null)
      {
         throw new IllegalArgumentException("e must never be null");
      }
      setErrorCode(0);
   }
   
   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   msgCode         the error string to load
    * @param   cause           the causal exception
    * @param   arrayArgs      the array of arguments to use as the arguments
    *   in the error message. May be <code>null</code>. <code>null</code> entries
    * are treated as "".
    */
   public PSException(int msgCode, Throwable cause, Object... arrayArgs)
   {
      super(cause);
      m_code = msgCode;
      //this method validates our contract
      setArgs(arrayArgs);
   }

   /**
    * Constructs an instance from the specified PSException.
    * 
    * @param e the source exception, it may not be <code>null</code>.
    */
   public PSException(PSException e)
   {
      super(e);
      if (e == null)
         throw new IllegalArgumentException("e may not be null.");
      
      m_code = e.m_code;
      m_args = e.m_args;
      m_lang = e.m_lang;
      m_overridingMessage = e.m_overridingMessage;
   }

   public static void setErrorManager(IPSErrorManager errorManager) {
      PSException.errorManager = errorManager;
   }

   /**
    * Returns the localized detail message of this exception.
    *
    * @param   locale      the locale to generate the message in
    *
    * @return               the localized detail message
    */
   public java.lang.String getLocalizedMessage(java.util.Locale locale)
   {
      if (m_overridingMessage != null)
      {
         return m_overridingMessage;
      }
      return errorManager.createMessage(m_code, m_args, locale);
   }

   /**
    * Returns the localized detail message of this exception.
    *
    * @param   language      the locale to generate the message in
    *
    * @return               the localized detail message
    */
   public java.lang.String getLocalizedMessage(String language)
   {
      if (m_overridingMessage != null)
      {
         return m_overridingMessage;
      }
      return errorManager.createMessage(m_code, m_args, language);
   }

   /**
    * Returns the localized detail message of this exception in the
    * default locale for this system.
    *
    * @return               the localized detail message
    */
   public java.lang.String getLocalizedMessage()
   {
      if (m_overridingMessage != null)
      {
         return m_overridingMessage;
      }
      else if (m_lang == null)
         return getLocalizedMessage(Locale.getDefault());
      else
         return getLocalizedMessage(m_lang);

   }

   /**
    * Returns the detail message of this exception.
    *
    * @return               the detail message
    */
   public java.lang.String getMessage()
   {
      return getLocalizedMessage();
   }

   /**
    * Returns a description of this exception. The format used is
    * "ExceptionClass: ExceptionMessage"
    *
    * @return               the description
    */
   public java.lang.String toString()
   {
      return this.getClass().getName() + ": " + getLocalizedMessage();
   }

   /**
    * Set the parsing error code associated with this exception.
    */
   public void setErrorCode(int code)
   {
      m_code = code;
   }

   /**
    * Get the parsing error code associated with this exception.
    *
    * @return   the error code
    */
   public int getErrorCode()
   {
      return m_code;
   }

   /**
    * Get the parsing error arguments associated with this exception.
    *
    * @return   May be <code>null</code>, but no entry is <code>null</code>.
    */
   public Object[] getErrorArguments()
   {
      return m_args;
   }

   /**
    * Convenience method that calls {@link #setArgs(int, Object[])
    * setArgs(msgCode, null == errorArg ? null : new Object[] { errorArg })}.
    */
   public void setArgs(int msgCode, Object errorArg)
   {
      setArgs(msgCode, null == errorArg ? null : new Object[] { errorArg } );
   }

   /**
    * Set the arguments for this exception.
    *
    * @param   msgCode         the error string to load
    *
    * @param   errorArgs      the array of arguments to use as the arguments
    *   in the error message. May be <code>null</code>, but no entry may be 
    * <code>null</code>.
    */
   public void setArgs(int msgCode, Object[] errorArgs)
   {
      m_code = msgCode;
      setArgs(errorArgs);
   }

   /**
    * Set the language string for this exception.
    *
    * @param   language language string used to locate the language message text,
    * may be <code>null</code> or <code>empty</code>.
    */
   public void setLanguageString(String language)
   {
      m_lang = language;
   }

   /**
    * Get the language string for this exception.
    *
    * @return   language string may be <code>null</code> or <code>empty</code>.
    */
   public String getLanguageString()
   {
      return m_lang;
   }

   /**
    * Get the stack trace for the specified exception as a string.
    *
    * @param   t         the throwable (usually an exception)
    */
   public static String getStackTraceAsString(java.lang.Throwable t)
   {
      // for unknown exceptions, it's useful to log the stack trace
      java.io.StringWriter stackTrace = new java.io.StringWriter();
      java.io.PrintWriter writer = new java.io.PrintWriter(stackTrace);
      t.printStackTrace(writer);
      writer.flush();

      return stackTrace.toString();
   }

   /**
    * Validates the supplied arg and sets the local variable if valid.
    * @param args May be <code>null</code>. Any <code>null</code> entry will
    * be replaced with "".
    * </code>.
    */
   private void setArgs(Object[] args)
   {
      if (null != args)
      {
         //check that no entries are null
         for (int i=0; i < args.length; i++)
         {
            if (null == args[i])
            {
               args[i] = "";
            }
         }
      }
      m_args = args;
   }

   /**
    * Specifies the string overriding message returned by the exception.
    * This method should only be used when normal approach to composing message
    * does not work. Such situation can exist, for example, in distributed
    * system if a node receives an exception with error message
    * from other node and does not have access to the message
    * resources to reconstruct the error message.
    * Make sure that locale of overriding message corresponds to the locale
    * normally used to construct the message.
    *
    * @param overridingMessage if not-blank this value will be returned
    * as exception message overriding default message composing behavior.  
    * Can be <code>null</code>.
    */
   public void setOverridingMessage(String overridingMessage)
   {
      m_overridingMessage = StringUtils.isBlank(overridingMessage)
            ? null : overridingMessage;
   }

   protected int        m_code;
   protected Object[]   m_args;
   protected String     m_lang;

   /**
    * @see #setOverridingMessage(String)
    */
   protected String     m_overridingMessage;
}

