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

import com.percussion.log.PSLogInformation;
import com.percussion.util.PSMapClassToObject;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *   A mapping from PSLogError-derived objects to HTTP error codes
 */
public class PSErrorHttpCodes
{
   
   /**
    *   Get the HTTP code for the given error, or if this error
    *   does not specifically have an error code, then get the error
    *   code for its most immediate superclass. A valid HTTP
    *   code is guaranteed to be returned.
    *
    *   @param   A PSLogError whose HTTP code code you want to get,
    *   or <CODE>null</CODE> to get the default HTTP code.
    *
    *   @param err
    * @return   A valid HTTP error code (e.g, <CODE>404</CODE> for not found)
    */
   public static synchronized int getHttpCode(PSLogInformation err, Locale loc)
   {
      if (!ms_areCodesLoaded)
         initErrorHttpCodeMaps(loc);

      if (err == null)
      {
         return 500;
      }
      Integer i = (Integer)ms_codes.getMapping(err.getClass());
      if (i == null)
      {
         com.percussion.server.PSConsole.printMsg("Error",
            "ERROR: no default HTTP error code for class "
            + err.getClass().toString());
         return 500;
      }
      return i.intValue();
   }

   /**
    * This method is used to get the error page map hash table for a
    * locale. If the page maps are not already loaded for the locale,
    * they will be.
    *
    * @param      loc         the locale
    *
    * @param                  a hash table containing the error page maps
    */
   private static void initErrorHttpCodeMaps(Locale loc)
      throws MissingResourceException, NumberFormatException
   {
      synchronized  (ms_codes) {
         if (!ms_areCodesLoaded) {
            // load the error class -> Http code mappings
            ms_areCodesLoaded = true;

            ResourceBundle bun = ResourceBundle.getBundle(
                           "com.percussion.error.PSErrorHttpCodeBundle", loc);

            String key = null;
            for (Enumeration e = bun.getKeys(); e.hasMoreElements() ;)
            {
               key = (String)e.nextElement();
               try {
                  ms_codes.addReplaceMapping(   Class.forName(key),
                                             new Integer(bun.getString(key)));
               } catch (java.lang.NumberFormatException nfe) {
                  /* log this!!! */
                  Object[] args = { key,
                     com.percussion.error.PSException.getStackTraceAsString(nfe) };
                  com.percussion.log.PSLogManager.write(
                     new com.percussion.log.PSLogServerWarning(
                     com.percussion.server.IPSHttpErrors.HTTP_NOT_FOUND, args,
                     true, "HttpErrorCodesLoader"));
               } catch (ClassNotFoundException cnf) {
                  /* log this!!! */
                  Object[] args = { key,
                     com.percussion.error.PSException.getStackTraceAsString(cnf) };
                  com.percussion.log.PSLogManager.write(
                     new com.percussion.log.PSLogServerWarning(
                     com.percussion.design.objectstore.IPSObjectStoreErrors.APP_NOT_FOUND,
                     args, true, "HttpErrorCodesLoader"));
               }
            }
         }
      }
   }

   private static PSMapClassToObject ms_codes = new PSMapClassToObject();
   private static boolean ms_areCodesLoaded = false;
}
