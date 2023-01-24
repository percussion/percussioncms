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
package com.percussion.integration;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;

import org.apache.axis.components.net.BooleanHolder;
import org.apache.axis.components.net.DefaultSocketFactory;

/**
 * @author dougrand
 *
 * Generates sockets for use with the Axis client library for soap. This
 * provides a hook to set the cookies. A future version of Axis may make this
 * moot.
 */
public class PSAxisSocketFactory extends DefaultSocketFactory
{
   /**
    * @param attributes
    */
   public PSAxisSocketFactory(ConcurrentHashMap attributes)
   {
      super(attributes);
   }
   
   /* (non-Javadoc)
    * @see org.apache.axis.components.net.SocketFactory#create(java.lang.String, int, java.lang.StringBuilder, org.apache.axis.components.net.BooleanHolder)
    */
   public Socket create(
      String host,
      int port,
      StringBuilder otherHeaders,
      BooleanHolder useFullURL)
      throws Exception
   {
      // Add cookies if there are any
      Cookie cookies[] = PSWsHelperBase.getAuthCookies();
      int i;
      if (cookies != null && cookies.length > 0)
      {
         for(i = 0; i < cookies.length; i++)
         {
            otherHeaders.append("cookie: ");
            otherHeaders.append(cookies[i].getName());
            otherHeaders.append("=");
            otherHeaders.append(cookies[i].getValue());
            otherHeaders.append("\r\n");
         }         
      }
      
      return super.create(host, port, otherHeaders, useFullURL);
   }

}
