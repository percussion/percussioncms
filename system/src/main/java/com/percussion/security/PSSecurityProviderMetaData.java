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

package com.percussion.security;

import com.percussion.data.PSResultSet;
import com.percussion.data.PSResultSetColumnMetaData;
import com.percussion.data.PSResultSetMetaData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * The PSSecurityProviderMetaData abstract class is used as the base class
 * for all security provider meta data classes. A default implementation
 * which returns empty result sets is implemented in this class.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public abstract class PSSecurityProviderMetaData
   implements IPSSecurityProviderMetaData
{
   /**
    * Default constructor to find connection properties, etc.
    */
   protected PSSecurityProviderMetaData()
   {
      super();
   }

   /**
    * Creates an empty result set that can be used by derived classes when
    * responding to the {@link #getServers() similarly named
    * method} in the interface. {@link PSResultSet#addRow(Object[]) addRow}
    * can be called on the returned object for each item to be returned.
    * <p>
    * The result set contains:
    * <OL>
    * <LI><B>SERVER_NAME</B> String => server name</LI>
    * </OL>
    *
    * @return     an empty result set with the above mentioned column, never
    *    <code>null</code>.
    */
   protected PSResultSet getEmptyServers()
   {
      HashMap  columnNames = new HashMap();
      columnNames.put("SERVER_NAME", new Integer(1));
      return createEmptyResultSet( columnNames, ms_GetServerRSMeta );
   }

   /**
    * See the {@link IPSSecurityProviderMetaData#getServers() interface} for a
    * full description.
    *
    * @return Always returns an empty result set.
    *
    * @throws SQLException Never thrown.
    */
   public ResultSet getServers()
         throws SQLException
   {
      return getEmptyServers();
   }

   /**
    * Creates an empty result set that can be used by derived classes when
    * responding to the {@link #getObjectTypes() similarly named
    * method} in the interface. {@link PSResultSet#addRow(Object[]) addRow}
    * can be called on the returned object for each item to be returned.
    * <p>
    * The result set contains:
    * <OL>
    * <LI><B>OBJECT_TYPE</B> String => the object type name</LI>
    * </OL>
    *
    * @return     an empty result set with the above mentioned column.
    */
   protected PSResultSet getEmptyObjectTypes()
   {
      HashMap  columnNames = new HashMap();
      columnNames.put("OBJECT_TYPE", new Integer(1));
      return createEmptyResultSet( columnNames, ms_GetObjectTypesRSMeta );
   }

   /**
    * See the {@link IPSSecurityProviderMetaData#getObjectTypes() interface}
    * for a full description.
    *
    * @return Always returns an empty result set.
    *
    * @throws SQLException Never thrown.
    */
   public ResultSet getObjectTypes()
         throws SQLException
   {
      return getEmptyObjectTypes();
   }

   /**
    * Creates an empty result set that can be used by derived classes when
    * responding to the {@link #getObjects(String[],String[]) similarly named
    * method} in the interface. {@link PSResultSet#addRow(Object[]) addRow}
    * can be called on the returned object for each item to be returned.
    * <p>
    * The result set contains:
    * <OL>
    * <LI><B>OBJECT_TYPE</B> String => the type of object</LI>
    * <LI><B>OBJECT_ID</B> String => the id of the object - usually the
    *    objects distinguished name (DN)</LI>
    * <LI><B>OBJECT_NAME</B> String => the name associated with the
    *    object</LI>
    * </OL>
    *
    * @return     an empty result set with the above mentioned columns.
    */
   protected PSResultSet getEmptyObjects()
   {
      HashMap  columnNames = new HashMap();
      columnNames.put("OBJECT_TYPE", new Integer(1));
      columnNames.put("OBJECT_ID",  new Integer(2));
      columnNames.put("OBJECT_NAME", new Integer(3));
      return createEmptyResultSet( columnNames, ms_GetObjectsRSMeta );
   }

   // see interface for description
   public ResultSet getObjects( String [] objectTypes )
         throws SQLException
   {
      return getObjects( objectTypes, null );
   }

   /**
    * See the {@link IPSSecurityProviderMetaData#getObjects(String[],
    * String[]) interface} for a full description.
    *
    * @return Always returns an empty result set.
    *
    * @throws SQLException Never thrown.
    */
   public ResultSet getObjects( String[] objectTypes, String[] filterPattern )
         throws SQLException
   {
      return getEmptyObjects();
   }


   /**
    * Creates an empty result set with 1 column for each entry in the supplied
    * map. For each entry, the key is the name of the column as a String and
    * the value is an Integer object representing the position of the column.
    * The positions should range from 1 to N, where N is the number of columns
    * in the map.
    *
    * @param columnNames A map of all column names to be included in the
    *    result set. Assumed not <code>null</code>.
    *
    * @param rsMeta A meta data class that describes the columns in the
    *    supplied map. Assumed not <code>null</code>.
    *
    * @return A empty result set containing the columns in the map in the
    *    order identified in the map.
    */
   private PSResultSet createEmptyResultSet( HashMap columnNames,
         PSResultSetMetaData rsMeta )
   {
      int cols = columnNames.size();
      ArrayList [] colSet = new ArrayList[cols];
      for ( int i = 0; i < cols; ++i )
         colSet[i] = new ArrayList();
      return new PSResultSet( colSet, columnNames, rsMeta );
   }

   /**
    * Creates an empty result set that can be used by derived classes when
    * responding to the {@link #getAttributes(String[]) similarly named method}
    * in the interface. {@link PSResultSet#addRow(Object[]) addRow} can be
    * called on the returned object for each item to be returned.
    * <p>
    * The result set contains:
    * <OL>
    * <LI><B>OBJECT_TYPE</B> String => the type of object</LI>
    * <LI><B>ATTRIBUTE_NAME</B> String => the attribute name</LI>
    * <LI><B>ATTRIBUTE_DESC</B> String => the description of the attribute
    *    (may be <code>null</code>)</LI>
    * </OL>
    *
    * @return     an empty result set with the above mentioned columns.
    */
   protected PSResultSet getEmptyAttributes()
   {
      HashMap  columnNames = new HashMap();
      columnNames.put("OBJECT_TYPE", new Integer(1));
      columnNames.put("ATTRIBUTE_NAME", new Integer(2));
      columnNames.put("ATTRIBUTE_DESC", new Integer(3));
      return createEmptyResultSet( columnNames, ms_GetAttributesRSMeta );
   }

   /**
    * See the {@link IPSSecurityProviderMetaData#getAttributes(String[])
    * interface} for a full description.
    *
    * @return Always returns an empty result set, never <code>null</code>.
    *
    * @throws SQLException Never thrown.
    */
   public ResultSet getAttributes( String [] objectTypes )
      throws SQLException
   {
      return getEmptyAttributes();
   }


   /**
    * Are calls to {@link #getServers <code>getServers</code>} supported?
    *
    * @return                 <code>true</code> if so
    */
   public boolean supportsGetServers()
   {
      return false;  // this is not supported
   }

   /**
    * Are calls to {@link #getObjectTypes <code>getObjectTypes</code>}
    * supported?
    *
    * @return                 <code>true</code> if so
    */
   public boolean supportsGetObjectTypes()
   {
      return false;  // this is not supported
   }

   /**
    * Are calls to {@link #getObjects <code>getObjects</code>} supported?
    *
    * @return                 <code>true</code> if so
    */
   public boolean supportsGetObjects()
   {
      return false;  // this is not supported
   }

   /**
    * Are calls to {@link #getAttributes <code>getAttributes</code>}
    * supported?
    *
    * @return                 <code>true</code> if so
    */
   public boolean supportsGetAttributes()
   {
      return false;  // this is not supported
   }

   /**
    * Defines the meta data for the result set created by the {@link
    * #getEmptyServers()} method. Initialized in static block.
    * @todo Make this private.
    */
   protected static final PSResultSetMetaData ms_GetServerRSMeta;

   /**
    * Defines the meta data for the result set created by the {@link
    * #getEmptyObjectTypes()} method. Initialized in static block.
    * @todo Make this private.
    */
   protected static final PSResultSetMetaData ms_GetObjectTypesRSMeta;

   /**
    * Defines the meta data for the result set created by the {@link
    * #getEmptyObjects()} method. Initialized in static block.
    * @todo Make this private.
    */
   protected static final PSResultSetMetaData ms_GetObjectsRSMeta;

   /**
    * Defines the meta data for the result set created by the {@link
    * #getEmptyAttributes()} method. Initialized in static block.
    * @todo Make this private.
    */
   protected static final PSResultSetMetaData ms_GetAttributesRSMeta;

   static{
      PSResultSetColumnMetaData col;

      ms_GetServerRSMeta = new PSResultSetMetaData();
      ms_GetObjectTypesRSMeta = new PSResultSetMetaData();
      ms_GetObjectsRSMeta = new PSResultSetMetaData();
      ms_GetAttributesRSMeta = new PSResultSetMetaData();

      // for method getServer()
      col = new PSResultSetColumnMetaData("SERVER_NAME", Types.VARCHAR,255);
      ms_GetServerRSMeta.addColumnMetaData(col);

      // for method getObjectTypes()
      col = new PSResultSetColumnMetaData("OBJECT_TYPE", Types.VARCHAR,255);
      ms_GetObjectTypesRSMeta.addColumnMetaData(col);
      ms_GetObjectsRSMeta.addColumnMetaData(col);  // also used here as col 1
      ms_GetAttributesRSMeta.addColumnMetaData(col);  // also used here as col 1

      // for method getObjects()
      col = new PSResultSetColumnMetaData("OBJECT_ID", Types.VARCHAR,255);
      ms_GetObjectsRSMeta.addColumnMetaData(col);
      col = new PSResultSetColumnMetaData("OBJECT_NAME", Types.VARCHAR,255);
      ms_GetObjectsRSMeta.addColumnMetaData(col);

      // for method getAttributes()
      col = new PSResultSetColumnMetaData("ATTRIBUTE_NAME", Types.VARCHAR,255);
      ms_GetAttributesRSMeta.addColumnMetaData(col);
      col = new PSResultSetColumnMetaData("ATTRIBUTE_DESC", Types.VARCHAR,255);
      ms_GetAttributesRSMeta.addColumnMetaData(col);
   }
}

