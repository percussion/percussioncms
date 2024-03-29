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
package com.percussion.error;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This is an independent exception for use within the beans portion of the
 * system.  There are no dependencies on anything Rhythmyx.
 */
public class PSBeansException extends Exception
{

   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode The code of the error string to load.
    *
    * @param singleArg The argument to use as the sole argument in
    *    the error message, may be <code>null</code>.
    */
   public PSBeansException(int msgCode, Object singleArg)
   {
      this(msgCode, new Object[] { singleArg });
   }

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
   public PSBeansException(int msgCode, Object[] arrayArgs)
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
   public PSBeansException(int msgCode)
   {
      this(msgCode, null);
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
   public String getMessage()
   {
      return getLocalizedMessage();
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

      String msg = null;

      ResourceBundle errList = getErrorStringBundle(loc);

      if (errList != null)
         msg = errList.getString(String.valueOf(msgCode));

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

            msg = "";
         msg += String.valueOf(msgCode) + ": " + sArgs;
      }

      return msg;
   }

   /**
    * This method is used to get the string resources hash table for a
    * locale. If the resources are not already loaded for the locale,
    * they will be.
    *
    * @param loc The locale, assumed not <code>null</code>.
    *
    * @return the bundle, never <code>null</code>.
    */
   private ResourceBundle getErrorStringBundle(Locale loc)
      throws MissingResourceException
   {
      if (ms_bundle == null)
      {
         ms_bundle =
           ResourceBundle.getBundle(getResourceBundleBaseName(), loc);
      }

      return ms_bundle;
   }

   /**
    * Get the base name of the resource bundle, a fully qualified class name
    *
    * @return The base name of the resource bundle, never <code>null</code>
    *    or empty.
    */
   private String getResourceBundleBaseName()
   {
      return "com.percussion.error.PSBeansStringBundle";
   }

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



