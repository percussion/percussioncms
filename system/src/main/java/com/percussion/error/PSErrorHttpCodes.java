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
