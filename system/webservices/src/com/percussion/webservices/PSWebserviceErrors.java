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
package com.percussion.webservices;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class with helper methods to lookup and format web service error
 * messages. 
 */
public class PSWebserviceErrors
{
   /**
    * Gets the localized message from the supplied exception and all causes,
    * appending them into a single string with newlines between. If an exception
    * has no text, its name is used as the text.
    * 
    * @param t May be <code>null</code>.
    * @return Never <code>null</code>. May be empty if <code>t</code> is
    * <code>null</code>.
    */
   public static String appendMessages(Throwable t)
   {
      if (t == null)
         return StringUtils.EMPTY;
      Throwable next = t;
      StringBuilder buf = new StringBuilder();
      while (next != null && next != next.getCause())
      {
         String msg = next.getLocalizedMessage();
         if (StringUtils.isBlank(msg))
            msg = next.getClass().getName();
         if (buf.length() > 0)
            buf.append("\r\n");
         buf.append(msg);
         next = next.getCause();
      }
      return buf.toString();
   }
   
   /**
    * Convenience method that calls {@link #createErrorMessage(int, Locale, 
    * Object[]) createErrorMessage(code, Locale.getDefault(), args)}.
    */
   public static String createErrorMessage(int code, Object... args)
   {
      return createErrorMessage(code, Locale.getDefault(), args);
   }
   
   /**
    * Create the error message for the supplied code, arguments and locale.
    * 
    * @param code the code of the message for which to create the error message.
    * @param locale the locale for which to get the error message, may be 
    *    <code>null</code> in which case the default locale will be used.
    * @param args an array with all message arguments, may be <code>null</code>
    *    or empty.
    * @return the requested message string, never <code>null</code> or empty.
    *    The message code will be returned as string if no message for the
    *    specified code is defined in the resource bundle.
    */
   public static String createErrorMessage(int code, Locale locale, 
      Object... args)
   {
      if (args == null)
         args = new Object[0];

      String msg = getErrorText(code, locale);
      if (msg != null)
      {
         try
         {
            msg = MessageFormat.format(msg, args);
         }
         catch (IllegalArgumentException e)
         {
            // some problem with formatting
            msg = null;
         }
      }

      /*
       * Return a simple format if the error message was not found or could 
       * not be formatted:
       * String(code): arg1; arg2; ...
       */
      if (msg == null)
      {
         String sArgs = "";
         String sep = "";

         for (int i = 0; i < args.length; i++)
         {
            sArgs += sep + args[i].toString();
            sep = "; ";
         }

         msg = String.valueOf(code) + ": " + sArgs;
      }

      return msg;
   }

   /**
    * Get the error text for the specified error code and locale.
    * 
    * @param code the code of the error to get the text for.
    * @param loc the locale for which to get the error text, may be
    *    <code>null</code> on which case the default locale will be used.
    * @return the requested error text, may be <code>null</code> or empty if
    *    no bundle or error text was found for the specified code.
    * @throws MissingResourceException if no error string bundle was found 
    *    for the specified locale or the default locale.
    */
   private static String getErrorText(int code, Locale loc) 
      throws MissingResourceException
   {
      String errorText = null;
      
      // get the bundle for the requested locale
      ResourceBundle bundle = getErrorStringBundle(
         loc == null ? Locale.getDefault() : loc);
      
      // get the default bundle if no bundle was found for the specified locale
      if (bundle == null && loc != null)
         bundle = getErrorStringBundle(Locale.getDefault());
      
      if (bundle != null)
         errorText = bundle.getString(String.valueOf(code));
      
      return errorText;
   }
   
   /**
    * Get the error string bundle for the specified locale.
    * 
    * @param loc the locale for which to get the error string bundle, assumed
    *    not <code>null</code>.
    * @return the requested error string bundle, never <code>null</code>.
    * @throws MissingResourceException if the requested error string bundle was 
    *    not found.
    */
   private static ResourceBundle getErrorStringBundle(Locale loc)
      throws MissingResourceException
   {
      if (ms_bundle == null)
         ms_bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, loc);

      return ms_bundle;
   }

   /**
    * The resource bundle containing error message formats, <code>null</code>
    * until the first call to {@link #getErrorStringBundle(Locale)
    * getErrorStringBundle}, never <code>null</code> or modified after that.
    */
   private static ResourceBundle ms_bundle = null;
   
   /**
    * The name of the resource bundle used to generate the web service error
    * messages.
    */
   private static final String RESOURCE_BUNDLE_NAME = 
      "com.percussion.webservices.PSWebserviceErrorStringBundle";
}

