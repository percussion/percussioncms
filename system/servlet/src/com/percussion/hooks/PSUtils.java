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
