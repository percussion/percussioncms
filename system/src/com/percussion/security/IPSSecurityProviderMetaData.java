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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;


/**
 * The IPSSecurityProviderMetaData interface defines the mechanism by which
 * the names of of users and groups defined in the security provider
 * can be cataloged.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSSecurityProviderMetaData
{
   /**
    * One of the possible object types supported by the provider. This string
    * should be used when an object type needs to reference the Subject that
    * can be authenticated. See {@link #getObjectTypes() getObjectTypes} for
    * more info. It is expected all providers will support this type, although
    * not all providers will necessarily be able to catalog them.
    */
   public static final String OBJECT_TYPE_USER = "user";

   /**
    * One of the possible object types supported by the provider. This string
    * should be used when an object type needs to reference a group, which is
    * a named collection of users (or Subjects). See {@link #getObjectTypes()
    * getObjectTypes} for more info. Not all providers support the concept of
    * groups.
    */
   public static final String OBJECT_TYPE_GROUP = "group";

   /**
    * The character to use in the pattern matcher when filtering objects to
    * match any single character.
    */
   public static final char FILTER_MATCH_ONE = '_';

   /**
    * The character to use in the pattern matcher when filtering objects to
    * match any sequence of characters.
    */
   public static final char FILTER_MATCH_MANY = '%';

   /**
    * Get the name of this security provider.
    *
    * @return The provider's name, never <code>null</code> or empty.
    */
   public String getName();

   /**
    * Get the full name of this security provider.
    *
    * @return The provider's full name, never <code>null</code> or empty.
    */
   public String getFullName();

   /**
    * Get the descritpion of this security provider.
    *
    * @return The provider's description, never <code>null</code> or empty.
    */
   public String getDescription();

   /**
    * Get the connection properties required for logging into this provider.
    *
    * @return     the connection properties (may be null)
    */
   public Properties getConnectionProperties();

   /**
    * Get the names of servers available to authenticate users. An empty
    * result set is returned if this feature is not supported.
    * <p>
    * The result set contains:
    * <OL>
    *    <LI><B>SERVER_NAME</B> String => server name</LI>
    * </OL>
    *
    * @return     a result set containing one server name per row.
    *    Possibly empty, never <code>null</code>. The caller is responsible
    *    for closing the result set when they are finised.
    *
    * @throws SQLException If any problems occur.
    */
   public ResultSet getServers() throws SQLException;

   /**
    * Get the types of objects available through this provider. An empty
    * result set is returned if this feature is not supported. If cataloging
    * is supported, the object type names specified by the OBJECT_TYPE_XXX
    * variables must be supported. The types returned by this method can be
    * used in the {@link #getObjects(String[]) getObjects} and {@link
    * #getAttributes(String[]) getAttributes} methods. It is possible that a
    * particular object type will return values when passed to one of these
    * methods, but not the other. {@link #supportsGetObjectTypes()} can be
    * called to determine if this method is supported.
    * <p>
    * The result set contains:
    * <OL>
    *    <LI><B>OBJECT_TYPE</B> String => the object type name</LI>
    * </OL>
    *
    * @return     a result set containing one object name per row.
    *    Possibly empty, never <code>null</code>. The caller is responsible
    *    for closing the result set when they are finised.
    *
    * @throws SQLException If any problems occur.
    */
   public ResultSet getObjectTypes()
      throws SQLException;

   /**
    * Attempts to catalog objects of the specified type(s). If objects of the
    * specified type(s) are not available or this method is not supported, an
    * empty result set is returned. You can call {@link #supportsGetObjects()}
    * before calling this method to determine if it is supported.
    * <p>
    * The result set contains:
    * <OL>
    *    <LI><B>OBJECT_TYPE</B> String => the type of object</LI>
    *    <LI><B>OBJECT_ID</B> String => the id of the object - usually the
    *       objects distinguished name (DN)</LI>
    *    <LI><B>OBJECT_NAME</B> String => the name associated with the
    *       object</LI>
    * </OL>
    *
    * @param      objectTypes    the type(s) of objects to retrieve or
    *                            <code>null</code> to retrieve all objects
    *
    * @param      filterPatterns An optional set of strings that specify
    *                            which objects to return. Uses SQL syntax, a %
    *                            represents any 0 or more characters and an
    *                            _ represents any single character. An empty
    *                            string or <code>null</code> returns no
    *                            objects. A <code>null</code> array returns
    *                            all objects. The same filter is applied to all
    *                            object types. Derived classes should use the
    *                            FILTER_MATCH_XXX constants for these chars.
    *
    * @return     a result set containing one object per row.
    *    Possibly empty, never <code>null</code>. The caller is responsible
    *    for closing the result set when they are finised.
    *
    * @throws SQLException If any problems occur.
    */
   public ResultSet getObjects(String[] objectTypes, String [] filterPatterns)
      throws SQLException;

   /**
    * A convenience method for the 2 parameter version of this method. Just
    * calls the sibling method with a <code>null</code> filter, which causes
    * all objects of the specified types to be returned. See {@link
    * #getObjects(String[],String[]) here} for a complete description.
    */
   public ResultSet getObjects(String[] objectTypes)
      throws SQLException;

   /**
    * Attempts to catalog the attributes of the specified type(s) of objects.
    * If attributes of the specified type(s) are not available, not present or
    * this method is not supported, an empty result set is returned. You can
    * call {@link #supportsGetAttributes() supportsGetAttributes} before
    * calling this method to determine if it is supported.
    * <p>
    * The result set contains:
    * <OL>
    *    <LI><B>OBJECT_TYPE</B> String => the type of object</LI>
    *    <LI><B>ATTRIBUTE_NAME</B> String => the attribute name</LI>
    *    <LI><B>ATTRIBUTE_DESC</B> String => the description of the attribute
    *       (may be <code>null</code>)</LI>
    * </OL>
    *
    * @param      objectTypes    the type(s) of objects to retrieve or
    *                            <code>null</code> to retrieve all objects
    *
    * @return A result set containing one attribute per row.
    *    Possibly empty, never <code>null</code>. The caller is responsible
    *    for closing the result set when they are finised.
    *
    * @throws SQLException If any problems occur.
    */
   public ResultSet getAttributes(String[] objectTypes)
      throws SQLException;

   /**
    * Are calls to {@link #getServers <code>getServers</code>}
    * supported?
    *
    * @return <code>true</code> if so
    */
   public boolean supportsGetServers();

   /**
    * Are calls to {@link #getObjectTypes <code>getObjectTypes</code>}
    * supported?
    *
    * @return <code>true</code> if so
    */
   public boolean supportsGetObjectTypes();

   /**
    * Are calls to {@link #getObjects <code>getObjects</code>}
    * supported?
    *
    * @return <code>true</code> if so
    */
   public boolean supportsGetObjects();

   /**
    * Are calls to {@link #getAttributes <code>getAttributes</code>}
    * supported?
    *
    * @return <code>true</code> if so
    */
   public boolean supportsGetAttributes();
}

