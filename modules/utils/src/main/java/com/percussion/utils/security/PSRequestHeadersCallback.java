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

package com.percussion.utils.security;

import javax.security.auth.callback.Callback;
import java.util.HashMap;
import java.util.Map;

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
