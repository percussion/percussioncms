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


import com.percussion.log.PSLogInformation;

import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;


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
public class PSErrorManager {

   public static final String ERROR_MGR_CLASS = "com.percussion.error.PSErrorManagerImpl";
   private static IPSErrorManager errorManagerInstance = new PSErrorManagerDefaultImpl();

   /**
    * Initialize the error manager to use the specified locale.
    * Called from server only.
    *
    * @param      loc      the locale to use
    */
   public synchronized static void init()
      throws MissingResourceException, NumberFormatException
   {
      try {
         errorManagerInstance = Thread.currentThread().getContextClassLoader().loadClass(ERROR_MGR_CLASS)
                 .asSubclass(IPSErrorManager.class).newInstance();

      } catch (InstantiationException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
      }
      errorManagerInstance.init();

   }


   /**
    * Shut down the error manager. This discards all the error page and
    * error string information. Use the {@link #init() init}
    * method to re-initialize the error facility.
    */
   public synchronized static void close()
   {
      errorManagerInstance.close();
   }


   /**
    * Get the error text associated with the specified error code.
    *
    * @param   code     the error code
    *
    * @return           the error text
    */
   public static java.lang.String getErrorText(int code)
   {
      return errorManagerInstance.getErrorText(code);
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
   public static java.lang.String getErrorText( int code,
                                                boolean nullNotFound) {
      return errorManagerInstance.getErrorText(code, nullNotFound);
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
   public static String getErrorText(int code,
                                                boolean nullNotFound,
                                                Locale loc)
   {

      return errorManagerInstance.getErrorText(code,nullNotFound,loc);

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
   public static java.lang.String getErrorText( int code,
         boolean nullNotFound,
         String language)
   {
      return errorManagerInstance.getErrorText(code,nullNotFound,language);
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
   public static java.lang.String createMessage(int msgCode,
                                                Object singleArg)
   {
      return errorManagerInstance.createMessage(msgCode,singleArg);
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
   public static java.lang.String createMessage(int msgCode,
                                                Object[] arrayArgs)
   {
      return errorManagerInstance.createMessage(msgCode,arrayArgs);
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
   public static java.lang.String createMessage(int msgCode,
                                                Object[] arrayArgs,
                                                Locale loc)
   {
     return errorManagerInstance.createMessage( msgCode,arrayArgs,loc);
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
   public static java.lang.String createMessage(int msgCode,
         Object[] arrayArgs,
         String language)
   {
      return errorManagerInstance.createMessage(msgCode,arrayArgs,language);
   }


   public static URL getErrorURL(PSLogInformation err, Locale loc) {
      return errorManagerInstance.getErrorURL(err,loc);
   }
}

