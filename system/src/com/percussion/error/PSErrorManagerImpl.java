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

package com.percussion.error;

import com.percussion.i18n.PSI18nUtils;
import com.percussion.log.PSLogInformation;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.util.PSMapClassToObject;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The PSErrorManager class is used to load the error string resources and
 * default error pages based upon the E2 server's locale.
 * <p>
 * Error messages are broken down into ranges, assigned to the various
 * components. The ranges we are using are as follows:
 * <table border="1">
 *    <tr><th>Range</th>      <th>Component</th></tr>
 *    <tr><td>0001 - 1000</td><td>HTML - this is HTML's range of errors</td></tr>
 *    <tr><td>1001 - 2000</td><td>Server Core (including hooks)</td></tr>
 *    <tr><td>2001 - 3000</td><td>Object Store</td></tr>
 *    <tr><td>3001 - 4000</td><td>Connectivity</td></tr>
 *    <tr><td>4001 - 5000</td><td>Cataloger</td></tr>
 *    <tr><td>5001 - 6000</td><td>Back-end (DBMS) Data Processing</td></tr>
 *    <tr><td>6001 - 7000</td><td>XML Data Processing</td></tr>
 *    <tr><td>7001 - 8000</td><td>Exit Processing</td></tr>
 *    <tr><td>8001 - 9000</td><td>Server Admin</td></tr>
 * </table>
 * <p>
 * All error messages are stored using the format defined in
 * the java.text.MessageFormat class. The message string contains curly
 * braces around parameters, which are 0 based. The error manager provides
 * two utility methods which take advantage of the MessageFormat.format
 * method. The following example uses an array of arguments to generate the
 * appropriate string:
 * <pre><code>
 *    String msg = PSErrorManager.getErrorText(999);
 *
 *    // let's assume the returned message is:
 *    //    "param 1={0}, param 2 date={1,date}, param 2 time={1,time}"
 *
 *    Object[] args = { new Integer(1), new Date() };
 *
 *    String displayMsg = PSErrorManager.createMessage(msg, args);
 *
 *    // displayMsg is returned containing:
 *    //    "param1=1, param 2 date=Jan 6, 1999, param 2 time=4:50 PM"
 * </code></pre>
 *
 * This model is excellent for internationalization as the position of the
 * parameters may change based upon the target language.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSErrorManagerImpl implements IPSErrorManager {


   /**
    * Initialize the error manager to use the specified locale.
    * Called from server only.
    *
    * @param      loc      the locale to use
    */
   public  static synchronized void init(Locale loc)
      throws MissingResourceException, NumberFormatException
   {
      // Inject this error manager.
      PSException.setErrorManager(new PSErrorManagerImpl());

      ms_isServerSide = true;
      /* load the error string resource bundle for the specified locale
       * NOTE: this is done first so any errors in our init can be logged
       * properly
       */
      ResourceBundle strBundle = getErrorStringBundle(loc);
      m_errorStrings.put("", strBundle);  /* mark it the default */

      /* now we can load the error class -> URL page mappings
       */
      PSMapClassToObject pageMap = getErrorPageMaps(loc);
      m_errorURLs.put("", pageMap);       /* mark it the default */
   }

   /**
    * Initialize the error manager to use the default locale for this system.
    * Called from server only.
    */
   public synchronized void init()
   {
      init(m_defaultLocale);
   }

   /**
    * Shut down the error manager. This discards all the error page and
    * error string information. Use the {@link #init() init}
    * method to re-initialize the error facility.
    */
   public synchronized void close()
   {
      m_errorURLs.clear();    /* we never want this null, just empty */
      m_errorStrings.clear(); /* we never want this null, just empty */
   }

   /**
    * Get the error URL to use for the specified error code.
    *
    * @param   error  the error code
    *
    * @return  the URL to use for the
    * specified error, or <code>null</code> if the error
    * code is not supported.
    */
   public URL getErrorURL(PSLogInformation error)
   {
      return getErrorURL(error, m_defaultLocale);
   }

   /**
    * Get the error URL to use for the specified error code.
    *
    * @param   error  the error code
    * @param   loc the locale of the error.
    *
    * @return  the URL to use for the
    * specified error, or <code>null</code> if the error
    * code is not supported.
    */
   public URL getErrorURL(PSLogInformation error, Locale loc)
   {
      PSMapClassToObject pageMap = getErrorPageMaps(loc);
      if (pageMap != null)
         return (URL)pageMap.getMapping(error.getClass());

      return null;
   }

   /**
    * Get the error text associated with the specified error code.
    *
    * @param   code     the error code
    *
    * @return           the error text
    */
   public String getErrorText(int code)
   {
      return getErrorText(code, false, m_defaultLocale);
   }

   /**
    * Get the error text associated with the specified error code.
    *
    * @param   code           the error code
    *
    * @param   nullNotFound   return <code>null</code> if the error string
    *                         is not found
    *
    * @return                 the error text
    */
   public String getErrorText( int code,
                                                boolean nullNotFound)
   {
      return getErrorText(code, nullNotFound, m_defaultLocale);
   }

   /**
    * Get the error text associated with the specified error code.
    *
    * @param   code           the error code
    *
    * @param   nullNotFound   return <code>null</code> if the error string
    *                         is not found
    *
    * @param   loc            the locale to use

    * @return                 the error text
    */
   public String getErrorText( int code,
                                                boolean nullNotFound,
                                                Locale loc)
   {
      try
      {
         //first look in the i18n resource
         PSRequest req = null;

         if (ms_isServerSide)
         {
            String lang = null;
            if (req!=null)
            {
               IPSRequestContext ctx = new PSRequestContext(req);
               lang = ctx.getUserLocale();
            }

            if (null == lang)
            {
               lang = PSI18nUtils.DEFAULT_LANG;
            }

            String key = String.valueOf(code);
            String i18nMsg = PSI18nUtils.getString(key, lang);
            if (i18nMsg!=null && !i18nMsg.equals(key))
               return i18nMsg;
         }

         /* if not found then look in the resource bundle
          * TODO: move all the resource bundle messages into the TMX file!
          */

         ResourceBundle errList = getErrorStringBundle(loc);

         if (errList != null)
            return errList.getString(String.valueOf(code));
      }
      catch (MissingResourceException e) {
         /* use the default listed below, just don't exception */
      }

      return (nullNotFound ? null : String.valueOf(code));
   }

   /**
    * Get the error text associated with the specified error code.
    *
    * @param   code           the error code
    *
    * @param   nullNotFound   return <code>null</code> if the error string
    *                         is not found
    *
    * @param   language       the language string to use

    * @return                 the error text
    */
   public String getErrorText( int code,
         boolean nullNotFound,
         String language)
   {
      String key = null;
      try
      {
         key = Integer.toString(code);
      }
      catch(Exception e)
      {
         //do nothing
      }
      if(key == null)
         return null;
      //Get the string from TMX resource bundle
      String result = PSI18nUtils.getString(key, language);

      if(result.equals(key) && nullNotFound) //resource not found
         return null;

      return result;
   }

   /**
    * Create a formatted message for messages taking only a single
    * argument.
    *
    * @param   msgCode        the error string to load
    *
    * @param   singleArg      the argument to use as the sole argument in
    *                         the error message
    *
    * @return                 the formatted message
    */
   public String createMessage(int msgCode,
                                                Object singleArg)
   {
      Object[] args = { singleArg };
      return createMessage(msgCode, args, m_defaultLocale);
   }

   /**
    * Create a formatted message for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   msgCode        the error string to load
    *
    * @param   arrayArgs      the array of arguments to use as the arguments
    *                         in the error message
    *
    * @return                 the formatted message
    */
   public String createMessage(int msgCode,
                                                Object[] arrayArgs)
   {
      return createMessage(msgCode, arrayArgs, m_defaultLocale);
   }

   /**
    * Create a formatted message for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   msgCode        the error string to load
    *
    * @param   arrayArgs      the array of arguments to use as the arguments
    *                         in the error message
    *
    * @param   loc            the locale to use
    *
    * @return                 the formatted message
    */
   public String createMessage(int msgCode,
                                                Object[] arrayArgs,
                                                Locale loc)
   {
      if (arrayArgs == null)
         arrayArgs = new Object[0];

      String msg = getErrorText(msgCode, true, loc);

      if (msg != null) {
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
         StringBuilder sArgs = new StringBuilder();
         String comma = "";

         for (int i = 0; i < arrayArgs.length; i++) {
            sArgs.append(comma).append(arrayArgs[i].toString());
            comma = "; ";
         }

         msg = sArgs.toString();
      }

      return msg;
   }


   /**
    * Create a formatted message for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   msgCode        the error string to load
    *
    * @param   arrayArgs      the array of arguments to use as the arguments
    *                         in the error message
    *
    * @param   language       the language string to use
    *
    * @return                 the formatted message
    */
   public String createMessage(int msgCode,
         Object[] arrayArgs,
         String language)
   {
      if (arrayArgs == null)
         arrayArgs = new Object[0];

      String msg = getErrorText(msgCode, true, language);

      if (msg != null) {
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
         StringBuilder sArgs = new StringBuilder();
         String comma = "";

         for (int i = 0; i < arrayArgs.length; i++) {
            sArgs.append(comma).append(arrayArgs[i].toString());
            comma = "; ";
         }

         msg = sArgs.toString();
      }

      return msg;
   }

   /**
    * This method is used to get the string resources hash table for a
    * locale. If the resources are not already loaded for the locale,
    * they will be.
    *
    * @param      loc         the locale
    *
    */
   private static ResourceBundle getErrorStringBundle(Locale loc)
      throws MissingResourceException
   {
      ResourceBundle strBundle = null;

         strBundle = m_errorStrings.get(loc.toString());
         if (strBundle != null)
            return strBundle;

         strBundle = ResourceBundle.getBundle(
                           "com.percussion.error.PSErrorStringBundle", loc);
         m_errorStrings.put(loc.toString(), strBundle);

      return strBundle;
   }

   /**
    * This method is used to get the error page map hash table for a
    * locale. If the page maps are not already loaded for the locale,
    * they will be.
    *
    * @param      loc         the locale
    *
    */
   private static PSMapClassToObject getErrorPageMaps(Locale loc)
      throws MissingResourceException, NumberFormatException
   {
      PSMapClassToObject pageMap = null;

         pageMap = m_errorURLs.get(loc.toString());
         if (pageMap != null)
            return pageMap;

         // load the error class -> URL page mappings
         pageMap = new PSMapClassToObject();

         ResourceBundle bun = ResourceBundle.getBundle(
                        "com.percussion.error.PSErrorPagesBundle", loc);

         String key = null;
         for (Enumeration e = bun.getKeys(); e.hasMoreElements() ;)
         {
            key = (String)e.nextElement();
            try {
               pageMap.addReplaceMapping( Class.forName(key),
                                          new URL(bun.getString(key)));
            } catch (java.net.MalformedURLException mue) {
               /* log this!!! */
               Object[] args = { key,
                  PSException.getStackTraceAsString(mue) };
               com.percussion.log.PSLogManager.write(
                  new com.percussion.log.PSLogServerWarning(
                  com.percussion.server.IPSHttpErrors.HTTP_NOT_FOUND, args,
                  true, "ErrorManager"));
            } catch (ClassNotFoundException cnf) {
               /* log this!!! */
               Object[] args = { key,
                  PSException.getStackTraceAsString(cnf) };
               com.percussion.log.PSLogManager.write(
                  new com.percussion.log.PSLogServerWarning(
                  com.percussion.design.objectstore.IPSObjectStoreErrors.APP_NOT_FOUND,
                  args, true, "ErrorManager"));
         }

         m_errorURLs.put(loc.toString(), pageMap);
      }

      return pageMap;
   }


   /**
    * This is a hash table using the locale as the key and the
    * PSMapClassToObject object as the value.
    *
    * The PSMapClassToObject object is a hash table using the
    * PSLogError subclass as the key and the error URL (URL) as the value.
    */
   private static ConcurrentHashMap<String, PSMapClassToObject> m_errorURLs =
         new ConcurrentHashMap<>();

   /**
    * This is a hash table using Locale.toString as the key and the
    * ResourceBundle object as the value.
    *
    * The ResourceBundle object is a property file backed string
    * resource bundle containing our error strings in the specified locale
    */
   private static  ConcurrentHashMap<String, ResourceBundle>   m_errorStrings =
         new ConcurrentHashMap<>();

   /**
    * The default locale to use when one is not specified.
    */
   private static  Locale      m_defaultLocale   = Locale.getDefault();


   /**
    * Flag that indicates if this class is used on the server side.
    */
   private static  boolean     ms_isServerSide;
}

