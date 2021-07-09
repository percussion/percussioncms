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
package com.percussion.integration;

import java.net.Socket;
import java.util.Hashtable;

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
   public PSAxisSocketFactory(Hashtable attributes)
   {
      super(attributes);
   }
   
   /* (non-Javadoc)
    * @see org.apache.axis.components.net.SocketFactory#create(java.lang.String, int, java.lang.StringBuffer, org.apache.axis.components.net.BooleanHolder)
    */
   public Socket create(
      String host,
      int port,
      StringBuffer otherHeaders,
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
