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

package com.percussion.security;

import com.percussion.data.PSResultSet;


/**
 * The PSWebServerProviderMetaData class implements cataloging for
 * the Web Server security provider.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSWebServerProviderMetaData extends PSSecurityProviderMetaData
{
   /**
    * Construct a web server meta data object for the specified provider
    * instance.
    *
    * @param      inst            the provider instance
    */
   PSWebServerProviderMetaData(PSWebServerProvider inst)
   {
      super();
      m_instance = inst;
   }

   /**
    * Default constructor to find connection properties, etc.
    */
   public PSWebServerProviderMetaData()
   {
      this(null);
   }

   /**
    * Get the name of this security provider.
    *
    * @return      the provider's name
    */
   public String getName()
   {
      return PSWebServerProvider.SP_NAME;
   }

   /**
    * Get the full name of this security provider.
    *
    * @return      the provider's full name
    */
   public String getFullName()
   {
      return "Web Server Security Provider";
   }

   /**
    * Get the descritpion of this security provider.
    *
    * @return      the provider's description
    */
   public String getDescription()
   {
      return "Authentication through the Web Server's native mechanisms.";
   }

   /**
    * Get the connection properties required for logging into this provider.
    *
    * @return      the connection properties (may be null)
    */
   public java.util.Properties getConnectionProperties()
   {
      return null;
   }

   /**
    * Get the attributes associated with objects of the specified type.
    * <p>
    * The result set contains:
    * <OL>
    * <LI><B>OBJECT_TYPE</B> String => the type of object</LI>
    * <LI><B>ATTRIBUTE_NAME</B> String => the attribute name</LI>
    * <LI><B>ATTRIBUTE_DESC</B> String => the description of the attribute
    *      (may be <code>null</code>)</LI>
    * </OL>
    *
    * @param      objectTypes      the type(s) of objects to retrieve or
    *                              <code>null</code> to retrieve all objects
    *
    * @return     a result set containing one object description per row
    */
   public java.sql.ResultSet getAttributes(java.lang.String[] objectTypes)
   {
      // the list of supported attributes is:
      String attribs[] = { "Client/Subject",   "Client/CN",   "Client/O",
                           "Client/OU",      "Client/S",      "Client/L",
                           "Client/C",         "Client/E",
                           "CA/Subject",       "CA/CN",       "CA/O",
                           "CA/OU",          "CA/S",          "CA/L",
                           "CA/C",
                           "keyStrength"
                         };

      java.util.ArrayList obType      = new java.util.ArrayList();
      java.util.ArrayList attribName = new java.util.ArrayList();
      java.util.ArrayList attribDesc = new java.util.ArrayList();

      java.util.HashMap   columnNames = new java.util.HashMap();

      for (int onAttrib = 0; onAttrib < attribs.length; onAttrib++) {
         obType.add("");
         attribName.add(attribs[onAttrib]);
         attribDesc.add("");
      }

      columnNames.put("OBJECT_TYPE", new Integer(1));
      columnNames.put("ATTRIBUTE_NAME", new Integer(2));
      columnNames.put("ATTRIBUTE_DESC", new Integer(3));

      return new PSResultSet(
         new java.util.ArrayList[] {obType, attribName, attribDesc},
         columnNames, ms_GetAttributesRSMeta);
   }

   /**
    * Are calls to {@link #getAttributes <code>getAttributes</code>}
    * supported?
    *
    * @return                  <code>true</code> if so
    */
   public boolean supportsGetAttributes()
   {
      return true;   // this is supported
   }

   
   private PSWebServerProvider      m_instance;
}

