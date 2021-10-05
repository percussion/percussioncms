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
package com.percussion.util;

import com.percussion.server.IPSCgiVariables;
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
