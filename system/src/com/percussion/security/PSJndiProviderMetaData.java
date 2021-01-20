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

package com.percussion.security;

import com.percussion.data.PSResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * PSJndiProviderMetaData is an abstract base class meant to be
 * extended by meta data classes which correspond to directory
 * providers that extend PSJndiProvider.
 */
abstract class PSJndiProviderMetaData extends PSSecurityProviderMetaData
{
   /**
    * Constructs a meta data object for the specified provider
    * instance.
    *
    * @param   inst The provider instance. Can be <CODE>null</CODE>,
    * in which case not all of the information will be available.
    */
   protected PSJndiProviderMetaData(PSJndiProvider inst)
   {
      super();
      m_instance = inst;
   }


   /**
    * Gets the name of this security provider.
    *
    * @return   the provider's name
    */
   public abstract String getName();


   /**
    * Get the full name of this security provider.
    *
    * @return   the provider's full name
    */
   public abstract String getFullName();

   /**
    * Get the descritpion of this security provider.
    *
    * @return   the provider's description
    */
   public abstract String getDescription();


   /**
    * Get the connection properties required for logging into this provider.
    *
    * @return   The connection properties (may be null).
    */
   public Properties getConnectionProperties()
   {
      Properties props = null;
      if (m_instance != null)
      {
         props = m_instance.getConnectionProperties();
      }

      return props;
   }


   /**
    * An empty result set is returned as this feature is not supported.
    * <p>
    * The result set contains:
    * <OL>
    * <LI><B>SERVER_NAME</B> String => domain or server name</LI>
    * </OL>
    *
    * @return   an empty result set
    */
   public ResultSet getServers()
      throws SQLException
   {
      ArrayList serverNameCol = new ArrayList(1);
      HashMap   columnNames   = new HashMap();

      columnNames.put("SERVER_NAME", new Integer(1));
      return new PSResultSet(new ArrayList[]{serverNameCol},
         columnNames, ms_GetServerRSMeta);
   }


   /**
    * Get the types of objects available through this provider.
    * <p>
    * The result set contains:
    * <OL>
    * <LI><B>OBJECT_TYPE</B> String => the object type name</LI>
    * </OL>
    * Will always include {@link IPSSecurityProviderMetaData#OBJECT_TYPE_USER}
    * and will include {@link IPSSecurityProviderMetaData#OBJECT_TYPE_GROUP} if
    * there is at least one group provider.
    *
    * @return   a result set containing one object type per row
    */
   public ResultSet getObjectTypes()
   {
      List objectTypeCol = getSupportedTypes();

      HashMap   columnNames   = new HashMap();
      columnNames.put("OBJECT_TYPE", new Integer(1));

      return new PSResultSet(new List[]{objectTypeCol},
         columnNames, ms_GetObjectTypesRSMeta);
   }


   // see IPSSecurityProviderMetaData interface for description
   public ResultSet getObjects(String[] objectTypes)
      throws SQLException
   {
      return getObjects(objectTypes, null);
   }

   /**
    * Get the attributes associated with objects of the specified type.
    * <p>
    * The result set contains:
    * <OL>
    * <LI><B>OBJECT_TYPE</B> String => the type of object</LI>
    * <LI><B>ATTRIBUTE_NAME</B> String => the attribute name</LI>
    * <LI><B>ATTRIBUTE_DESC</B> String => the description of the attribute
    *     (may be <code>null</code>)</LI>
    * </OL>
    *
    * @param   objectTypes    the type(s) of objects to retrieve or
    *                             <code>null</code> to retrieve all objects
    *
    * @return   @return an empty result set, as this provider cannot enumerate
    * its attributes.
    */
   public ResultSet getAttributes(String[] objectTypes)
      throws SQLException
   {
      // there is no way to get the "standard" or required attributes for an
      // object type, so return an emtpy result set.

      return new PSResultSet();
   }

   /**
    * Are calls to {@link #getServers <code>getServers</code>}
    * supported?
    *
    * @return   <code>true</code> if so
    */
   public boolean supportsGetServers()
   {
      return false;   // this is not supported
   }


   /**
    * Are calls to {@link #getObjectTypes <code>getObjectTypes</code>}
    * supported?
    *
    * @return <code>true</code> if this provider can enumerate the types
    * of objects it supports; <code>false</code> otherwisew
    */
   public boolean supportsGetObjectTypes()
   {
      return true;   // this is supported
   }


   /**
    * Are calls to <code>getObjects()</code> supported?
    *
    * @return   <code>true</code> if so
    */
   public boolean supportsGetObjects()
   {
      return true;   // this is supported
   }


   /**
    * Are calls to {@link #getAttributes <code>getAttributes</code>}
    * supported?
    *
    * @return   <code>true</code> if so
    */
   public boolean supportsGetAttributes()
   {
      return false; // for now, this is not supported
   }

   /**
    * Returns a list of supported types as Strings.
    *
    * @return The list, never <code>null</code> or emtpy. Will always include
    * {@link IPSSecurityProviderMetaData#OBJECT_TYPE_USER} and will include
    * {@link IPSSecurityProviderMetaData#OBJECT_TYPE_GROUP} if there is at least
    * one group provider.
    */
   protected List getSupportedTypes()
   {
      List objectTypes = new ArrayList(2);
      objectTypes.add(OBJECT_TYPE_USER);
      if (m_instance.getGroupProviders().hasNext())
         objectTypes.add(OBJECT_TYPE_GROUP);

      return objectTypes;
   }

   /** The instance associated with this meta data, or <CODE>null</CODE> if
     * this metadata has no instance. */
   protected PSJndiProvider m_instance = null;
}

