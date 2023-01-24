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
package com.percussion.util;

import com.percussion.utils.server.IPSCgiVariables;
import com.percussion.server.IPSRequestContext;

import java.util.Map;

public class PSHttpUtils extends PSBaseHttpUtils
{
   
   /**
    * Store a set of cookies by parsing the cookie header value. The
    * cookies will be stored in the map with the lowercased param
    * names as the key to the param values.
    *
    * @param      value      the cookie header field value
    * @param      cookies      used to store cookies in
    *
    * @return The number of cookie values parsed.
    */
   @SuppressWarnings("unchecked")
   public static int parseCookies(String value, Map cookies)
   {
      return parseHttpParamsString(value, cookies);
   }



   /**
    * Gets the HTTP header: USER_AGENT and searches, ignoring case, for
    * indications that the operating system is Windows, Macintosh, or other.
    *
    *
    * @param request The request context, may not be <code>null</code>.
    *
    * @return The os, as one of the OS_XXX constants.  If it cannot be
    * determined, OS_OTHER is returned.  Never <code>null</code> or empty.
    */
   public static String getRequestorOS(IPSRequestContext request)
   {
      if (request == null)
         throw new IllegalArgumentException("Request may not be null");

      String returnValue = null;

      Object userAgentObj = 
         request.getCgiVariable(IPSCgiVariables.CGI_REQUESTOR_SOFTWARE);
      if (userAgentObj != null)
      {
         String userAgent = userAgentObj.toString();
         userAgent = userAgent.toLowerCase(); //For our purposes, we ignore case

         if(userAgent.indexOf("windows") != -1 ||
            userAgent.indexOf("winnt") != -1)
         {
            returnValue = OS_WINDOWS;
         }
         else if(userAgent.indexOf("macintosh") != -1)
         {
            returnValue = OS_MACINTOSH;
         }
         else if(userAgent.indexOf("unix") != -1
                  || userAgent.indexOf("sunos") != -1
                  || userAgent.indexOf("solaris") != -1)
         {
            returnValue = OS_UNIX;
         }
      }

      if (returnValue == null)
         returnValue = OS_OTHER;

      return returnValue;
   }

   /**
    * Returns the directory separator to use based on the OS as determined from
    * the USER_AGENT cgi variable.
    *
    * @param request The request context, may not be <code>null</code>.
    *
    * @return The path separator to use.  Never <code>null</code> or emtpy.
    */
   public static String getRequestorDirectorySeperator(
      IPSRequestContext request)
   {
      if (request == null)
         throw new IllegalArgumentException("Request may not be null");

      String returnValue = null;
      String operatingSystem = getRequestorOS(request);

      if(operatingSystem.equals(OS_WINDOWS))
      {
         returnValue = SEP_WINDOWS;
      }
      else if(operatingSystem.equals(OS_MACINTOSH))
      {
         returnValue = SEP_MACINTOSH;
      }
      else if(operatingSystem.equals(OS_UNIX))
      {
         returnValue = SEP_UNIX;
      }
      else
      {
         returnValue = SEP_OTHER;
      }

      return returnValue;
   }


}
