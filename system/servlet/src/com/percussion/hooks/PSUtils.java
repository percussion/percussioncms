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
package com.percussion.hooks;

/**
 * Utilities which might be useful for all servlets.
 */
public class PSUtils
{
   /**
    * Registers the SSL protocol handler in the system properties if it's not
    * already done.
    */
   public static void registerSSLProtocolHandler()
   {
      addSystemProperty(SYSTEM_PROP_PROTOCOL_HANDLER_PKGS, 
         SSL_PROTOCOL_HANDLER_PKG);
   }

   /**
    * Add the supplied system property. If the property already exists with the
    * supplied value, nothing is done. If the property does exist but not with 
    * the supplied value, the new value is appended to the value list using 
    * <code>|</code> as pipe delimiter. If the property does not exist, it 
    * will be added with the supplied value.
    * 
    * @param key the property key, assumed not <code>null</code> or empty.
    * @param value the property value to be set or added, assumed not 
    *    <code>null</code> or empty.
    */
   private static void addSystemProperty(String key, String value)
   {
      String propertyValue = System.getProperty(key);
      if (propertyValue == null || propertyValue.indexOf(value) == -1)
      {
         if (propertyValue != null && propertyValue.trim().length() > 0)
            propertyValue += "|";
         else
            propertyValue = "";
            
         propertyValue += value;
         System.setProperty(key, propertyValue);
      }
   }
   
   /**
    * Constant for the system property containing the protocol handler packages.
    * Used to enable protocols for use with the {@link java.net.URL} class.
    */
   private static final String SYSTEM_PROP_PROTOCOL_HANDLER_PKGS =
      "java.protocol.handler.pkgs";

   /**
    * Constant for the value to set on the system property containing the
    * protocol handler packages to enable the https protocol for use with the
    * {@link java.net.URL} class.
    */
   private static final String SSL_PROTOCOL_HANDLER_PKG =
      "com.sun.net.ssl.internal.www.protocol";
}
