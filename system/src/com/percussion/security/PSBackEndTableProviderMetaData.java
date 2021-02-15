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
import com.percussion.data.PSResultSetWrapper;
import com.percussion.data.PSSqlException;
import com.percussion.data.jdbc.PSOdbcDriverMetaData;
import com.percussion.log.PSLogManager;
import com.percussion.log.PSLogServerWarning;
import com.percussion.util.PSIteratorUtils;
import com.percussion.util.PSSQLStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Properties;

/**
 * The PSBackEndTableProviderMetaData class implements cataloging for
 *    the JDBC (back-end) table security provider.
 *
 */
public class PSBackEndTableProviderMetaData
   extends PSSecurityProviderMetaData
{
   /**
    * Construct a meta data object for the specified provider
    *    instance.
    *
    *
    * @param      inst            The provider instance.
    *                             (must not be <code>null</code>)
    *
    * @param      uidCol          The column to query for uids.
    *                             (must not be <code>null</code> &
    *                              must not be empty string)
    *
    * @param      tableName       The table to query for uids.
    *                             (must not be <code>null</code> &
    *                              must not be empty string)
    *
    * @param      userAttributes The names of all attributes defined for this
    *                             provider. May be <code>null</code>.
    *
    * @throws     IllegalArgumentException
    *                             If any parameter is invalid.
    */
   PSBackEndTableProviderMetaData(PSBackEndTableProvider inst, String uidCol,
                                  String tableName, String [] userAttributes )
   {
      if (inst == null)
      {
         throw new IllegalArgumentException("An instance must be provided");
      }

      if ((uidCol != null) && (uidCol.length() > 0) &&
          (tableName != null) && (tableName.length() > 0))
      {
         /* Create the query statement to get uids */
         m_uidSelect = "SELECT 'user' AS OBJECT_TYPE, " + uidCol +
         " AS OBJECT_ID, " + uidCol + " AS OBJECT_NAME FROM " + tableName;
      } else
      {
         throw new IllegalArgumentException(
            "Table name and uid column must be specified.");
      }

      m_instance = inst;
      if ( null != userAttributes )
         m_userAttributes = userAttributes;

      m_uidCol = uidCol;
   }

   /**
    * Construct a meta data object for the specified provider
    *    instance.  This default constructor is used by the
    *    security provider pool.
    */
   public PSBackEndTableProviderMetaData()
   {
      // Nada!
   }

    /**
    * Get the name of this security provider.
    *
    * @return      the provider's name
    */
   public String getName()
   {
      return PSBackEndTableProvider.SP_NAME;
   }

   /**
    * Get the full name of this security provider.
    *
    * @return      the provider's full name
    */
   public String getFullName()
   {
      return SP_FULLNAME;
   }

   /**
    * Get the descritpion of this security provider.
    *
    * @return      the provider's description
    */
   public String getDescription()
   {
      return SP_DESCRIPTION;
   }

   /**
    * Get the connection properties required for logging into this provider.
    *
    * @return      the connection properties (never <code>null</code>)
    */
   public Properties getConnectionProperties()
   {
      return PSBackEndConnection.getConnectionProperties();
   }

   /**
    * Get the names of servers available to authenticate users.
    *    The caller is responsible for closing the result set.
    *
    * <p>
    * The result set contains:
    * <OL>
    * <LI><B>SERVER_NAME</B> String => server name</LI>
    * </OL>
    *
    * @return     a result set containing one server per row
    *
    * @throws     SQLException   If a SQL exception occurs.
    */
   @Override
   public ResultSet getServers()
      throws SQLException
   {
      PSOdbcDriverMetaData odbcMeta = new PSOdbcDriverMetaData();

      try {
         return odbcMeta.getServers();
      } catch (SQLException e) {
         /* Log that a catalog exception occurred */
         Object[] args = {
               PSBackEndTableProvider.SP_NAME,
               m_instance,
               PSSqlException.toString(e) };

         PSLogManager.write(
            new PSLogServerWarning(
               IPSSecurityErrors.PROVIDER_INIT_CATALOG_DISABLED, args,
               true, "Odbc Driver - getServers"));

         throw e;
      }
   }

   /**
    * Get the types of objects available through this provider.
    *    The caller must close the result set when finished with it.
    *
    * <p>
    * The result set contains:
    * <OL>
    * <LI><B>OBJECT_TYPE</B> String => the object type name</LI>
    * </OL>
    *
    * @return     a result set containing one object type per row
    *
    * @throws     SQLException Never thrown
    */
   @Override
   public ResultSet getObjectTypes()
         throws SQLException
   {
      PSResultSet rs = super.getEmptyObjectTypes();
      String [] objectTypes = { USER_OBJECT_NAME };
      Iterator iter = PSIteratorUtils.iterator( objectTypes );
      Object [] row = new String[1];
      while ( iter.hasNext())
      {
         row[0] = iter.next();
         rs.addRow( row );
      }
      rs.beforeFirst();
      return rs;
   }

   // see interface for description
   @Override
   public ResultSet getObjects( String[] objectTypes, String [] filterPattern )
         throws SQLException
   {
      Connection conn = null;
      Statement stmt = null;

      try {
         boolean getUsers = false;
         if (objectTypes != null)
         {
            for (int i = 0; i < objectTypes.length; i++)
            {
               if ((objectTypes[i] != null) &&
                  (objectTypes[i].equals(USER_OBJECT_NAME)))
               {
                  getUsers = true;
                  break;
               }
            }
         } else
         {
            getUsers = true;
         }

         if (getUsers && (m_uidSelect != null))
         {
            conn = m_instance.getDbConnection();

            // Get an appropriate result set to come back for the caller
            stmt =  PSSQLStatement.getStatement(conn);

            StringBuilder uidSelect = new StringBuilder(m_uidSelect);
            if (filterPattern != null && filterPattern.length > 0)
            {
               uidSelect.append(" WHERE ");
               for (int i = 0; i < filterPattern.length; i++)
               {
                  uidSelect.append(m_uidCol).append(" LIKE '");
                  uidSelect.append(filterPattern[i]);
                  uidSelect.append("'");
                  if (i < filterPattern.length - 1)
                     uidSelect.append(" OR ");
               }
            }

            ResultSet rs = stmt.executeQuery(uidSelect.toString());

            // Fill the new kind of result set here (conn, stmt, etc)
            return new PSResultSetWrapper(rs, stmt, conn);
         } else
         {
            return new PSResultSet();
         }
      } catch (SQLException e) {
         if (stmt != null)
            try
            {
               stmt.close();
            } catch (SQLException stmtCloseE)
            {
               e.setNextException(stmtCloseE);
            }
         if (conn != null)
            try
            {
               conn.close();
            } catch (SQLException connCloseE)
            {
               e.setNextException(connCloseE);
            }
         throw e;
      }
   }

   /**
    * @see
    * @param objectTypes
    * @return
    * @throws SQLException
    */
   @Override
   public ResultSet getAttributes( String[] objectTypes )
         throws SQLException
   {
      PSResultSet rs = super.getEmptyAttributes();
      if ( m_userAttributes.length > 0 )
      {
         Iterator<String> types = PSIteratorUtils.iterator( objectTypes );
         Iterator attribs = PSIteratorUtils.iterator( m_userAttributes );
         while ( types.hasNext())
         {
            String type = (String) types.next();
            if ( !type.equalsIgnoreCase(USER_OBJECT_NAME))
               continue;
            Object [] row = new String[3];
            row[0] = type;
            row[2] = "";
            while ( attribs.hasNext())
            {
               row[1] = attribs.next();
               rs.addRow( row );
            }
         }
         rs.beforeFirst();
      }
      return rs;
   }

   /**
    * Are calls to {@link #getServers <code>getServers</code>}
    *    supported?
    *
    * @return                  <code>true</code>
    */
   @Override
   public boolean supportsGetServers()
   {
      return true;
   }

   /**
    * Are calls to {@link #getObjects <code>getObjects</code>}
    *    supported?
    *
    * @return                  <code>true</code>
    */
   @Override
   public boolean supportsGetObjects()
   {
      return true;
   }

   /**
    * Are calls to {@link #getObjectTypes <code>getObjectTypes</code>}
    *    supported?
    *
    * @return                  <code>true</code>
    */
   @Override
   public boolean supportsGetObjectTypes()
   {
      return true;
   }

   /**
    * The name of the column in the back end table which contains the
    * user id.
    *    (created by constructor & can be <code>null</code>, but never empty)
    */
   private String m_uidCol = null;

   /**
    * Store the query statement so that a query can be executed.
    *    (created by constructor & can be <code>null</code>, but never empty)
    */
   private String m_uidSelect = null;

   /** The description for this provider.
    */
   private static final String SP_DESCRIPTION =
      "Authentication using a back-end table as a user directory.";

   /** The full name for this provider
    */
   public static final String SP_FULLNAME =
      "Back-end Table Security Provider";

   /** The name of the user object supported for cataloging.
    */
   private static final String USER_OBJECT_NAME = "user";

   /** The BackEndTableProvider instance associate with this metadata.
    *    (created by constructor & can be <code>null</code>)
    */
   private PSBackEndTableProvider      m_instance;

   /**
    * An array containing all of the names of the attributes supported
    * by this provider. Initialized with data when the non-default ctor is
    * called. Never <code>null</code>, may be empty.
    */
   private String [] m_userAttributes = new String[0];
}

