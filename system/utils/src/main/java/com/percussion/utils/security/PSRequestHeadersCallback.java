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

package com.percussion.utils.security;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;

/**
 * Callback to allow a login module access to the request headers.  
 */
public class PSRequestHeadersCallback implements Callback
{
   /**
    * Default ctor
    */
   public PSRequestHeadersCallback()
   {

   }

   /**
    * Get the request header values set on this callback.
    * 
    * @return A map where the key is the header name and the value is the 
    * header value, never <code>null</code>, may be empty (although not likely).
    */
   public Map<String, String> getHeaders()
   {
      return m_headers;
   }

   /**
    * Set the headers on the callback, used by the callback handler to provide
    * the requested information to the login module.
    * 
    * @param headers A map of request header info, never <code>null</code>, may 
    * be empty.  See {@link #getHeaders()} for details.
    */
   public void setHeaders(Map<String, String> headers)
   {
      if (headers == null)
         throw new IllegalArgumentException("headers may not be null");
      
      m_headers = headers;
   }

   /**
    * Map of headers, see {@link #setHeaders(Map)} for more info.
    */
   private Map<String, String> m_headers = new HashMap<String, String>();
}