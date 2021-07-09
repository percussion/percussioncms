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

import com.percussion.design.objectstore.PSConditional;
import com.percussion.util.PSIteratorUtils;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.jdbc.PSConnectionInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;

/**
 * This class wraps the backend connection functionality required for backend
 * security providers and backend directory catalogers.
 */
public class PSBackEndConnection
{
   /**
    * Construct a backend connection for the supplied properties.
    *
    * @param properties the properties with all connection and user attribute
    *    information. A copy of the supplied properties is made for local
    *    usage and the original properties will not be changed in any way.
    *    May not be <code>null</code> or empty, the following properties
    *    are expected:
    *    <TABLE BORDER="1">
    *    <TH><TD>Property</TD>
    *        <TD>Description</TD>
    *        <TD>Required?</TD>
    *    </TH>
    *    <TR><TD>datasourceName</TD>
    *        <TD>The datasource to connect to, may be omitted to use the 
    *        repository.</TD>
    *        <TD>N</TD>
    *    </TR>
    *    <TR>
    *        <TD>tableName</TD>
    *        <TD>The name of the table containing the user info.</TD>
    *        <TD>Y</TD>
    *    </TR>
    *    <TR>
    *        <TD>uidColumn</TD>
    *        <TD>The column in the table containing user ids.</TD>
    *        <TD>Y</TD>
    *    </TR>
    *    <TR>
    *        <TD>passwordColumn</TD>
    *        <TD>The column in the table containing passwords.</TD>
    *        <TD>Y</TD>
    *    </TR>
    *    <TR>
    *        <TD>passwordFilter</TD>
    *        <TD>The fully qualified extension name for the extension implementing
    *           IPSPasswordFilter</TD>
    *        <TD>N</TD>
    *    </TR>
    *    </TABLE>
    */
   public PSBackEndConnection(Properties properties) 
   {
      if (properties == null)
         throw new IllegalArgumentException("properties cannot be null");

      if (properties.isEmpty())
         throw new IllegalArgumentException("properties cannot be empty");

      // create a local copy, we don't want to change the original
      Properties localProps = new Properties();
      localProps.putAll(properties);

      // initialize connection properties
      m_datasource = PSJndiUtils.getProperty(localProps, PROPS_DATASOURCE_NAME, 
         false);
      m_table = PSJndiUtils.getProperty(localProps, PROPS_TABLE_NAME, true);
      m_uidColumn = PSJndiUtils.getProperty(localProps, PROPS_UID_COLUMN, true);
      
      // just remove the system crdential property if available
      PSJndiUtils.getProperty(localProps, PROPS_SYSTEM_CREDENTIALS, false);

      // initialize password filter
      String filterName = PSJndiUtils.getProperty(localProps, 
         PROPS_PW_FILTER, false);
      if ((filterName != null) && (filterName.length() > 0))
         m_filter = PSJndiUtils.initPasswordFilter(filterName);

      /**
       * Save the properties that remain - this is the password column, plus
       * any user defined attributes.
       */
      m_columns = new ArrayList<>();
      m_columns.addAll(localProps.values());
      
      /**
       * We don't want to return the password column as a user attribute, that's
       * why we need to remove it here from the localProps.
       */
      m_pwColumn = PSJndiUtils.getProperty(localProps, PROPS_PW_COLUMN, false);

      // store the requested user attribute names
      m_userAttributes = localProps;
   }

   /**
    * Get the connection properties required for this backend connection.
    *
    * @return all connection properties with the property name as
    *    key and the description as property value, never <code>null</code>.
    */
   public static Properties getConnectionProperties()
   {
      Properties props = new Properties();

      props.put(PROPS_DATASOURCE_NAME,
         "The datasource to connect with for this provider");
      props.put(PROPS_TABLE_NAME,
         "The name of the table to use to authenticate through and catalog");
      props.put(PROPS_UID_COLUMN,
         "The column which contains the used id");
      props.put(PROPS_PW_COLUMN,
         "The column which contains the user credentials (password)");
      props.put(PROPS_PW_FILTER,
         "The classname to use to filter or encrypt passwords");

      return props;
   }

   /**
    * Get the table name to connect with.
    * 
    * @return the table to connect to, may be <code>null</code> or empty.
    */
   public String getTable()
   {
      return m_table;
   }

   /**
    * Get the datasource name to connect with.
    * 
    * @return the datasource to connect to, may be <code>null</code> or empty to
    * indicate the repository.
    */
   public String getDatasource()
   {
      return m_datasource;
   }

   /**
    * @return the column name that holds the rhythmyx user, not
    *    <code>null</code> or empty.
    */
   public String getUserColumn()
   {
      return m_uidColumn;
   }

   /**
    * @return the column name that holds the rhythmyx user password, may
    *    be <code>null</code> or empty.
    */
   public String getPasswordColumn()
   {
      return m_pwColumn;
   }

   /**
    * @return the password filter used to decrypt the rhythmyx password, may
    *    be <code>null</code>.
    */
   public IPSPasswordFilter getPasswordFilter()
   {
      return m_filter;
   }

   /**
    * Return a connection from the database pool for this backend connection.
    *
    * @return the connection to the database, never <code>null</code>. The
    *    caller owns the connection and must close it.
    *    
    * @throws SQLException for any error getting the connection.
    */
   public Connection getDbConnection() throws SQLException
   {
      try
      {
         return PSConnectionHelper.getDbConnection(new PSConnectionInfo(
            m_datasource));
      }
      catch (NamingException e)
      {
         throw new SQLException(e.getLocalizedMessage());
      }
   }

   /**
    * Prepares and binds the prepared statement string to get a complete
    * row for the specified user from the user login table.
    *
    * @param user the user name to get the entries for, not <code>null</code>
    *    or empty.
    * @param connection a valid connection to the database from where to get
    *    the data, not <code>null</code>.
    * @return the prepared statement, never <code>null</code>. The caller owns
    *    the returned statement and is responsible for closing it.
    * @throws SQLException for any database error.
    */
   public PreparedStatement getPreparedStatement(String user,
      Connection connection) throws SQLException
   {
      if (user == null)
         throw new IllegalArgumentException("user cannot be null");

      user = user.trim();
      if (user.length() == 0)
         throw new IllegalArgumentException("user cannot be empty");

      if (connection == null)
         throw new IllegalArgumentException("connection cannot be null");
      
      if (m_preparedStatement == null)
      {
         PSConnectionDetail detail;
         try
         {
            detail = PSConnectionHelper.getConnectionDetail(
               new PSConnectionInfo(m_datasource));
         }
         catch (NamingException e)
         {
            throw new SQLException(e.getLocalizedMessage());
         }
         
         String tableName = PSSqlHelper.qualifyTableName(
            m_table, detail.getDatabase(), detail.getOrigin(), 
            detail.getDriver());
         m_preparedStatement = prepareStatement(m_columns, m_uidColumn, 
            tableName);         
      }
      
      PreparedStatement stmt = PSPreparedStatement.getPreparedStatement(
            connection, m_preparedStatement);
      stmt.setString(1, user);

      return stmt;
   }

   /**
    * Prepare a query statement based on a single key column. This will be
    * keyed off the uid column and it will be used to lookup password
    * information for authentication and all specified user attributes.
    *
    * @param columns the list of columns to query, not <code>null</code> or 
    *    empty.
    * @param key the key column to use for lookups, not
    *    <code>null</code> or empty.
    * @param table the name of the table to do the lookups in, not
    *    <code>null</code> or empty.
    * @return the query statement string, never <code>null</code>.<p>
    *    Note: There will be one bound parameter, which is the key for
    *    the query.
    */
   public String prepareStatement(List<Object> columns, String key, 
      String table)
   {
      if (columns == null)
         throw new IllegalArgumentException("columns cannot be null");
         
      if (columns.isEmpty())
         throw new IllegalArgumentException("columns cannot be empty");

      if (key == null)
         throw new IllegalArgumentException("key cannot be null");

      key = key.trim();
      if (key.length() == 0)
         throw new IllegalArgumentException("key cannot be empty");

      if (table == null)
         throw new IllegalArgumentException("table cannot be null");

      table = table.trim();
      if (table.length() == 0)
         throw new IllegalArgumentException("table cannot be empty");


      String comma = ",";
      boolean firstIn = true;
      StringBuffer buff = new StringBuffer();
      buff.append ("SELECT");

      Iterator cols = columns.iterator();
      while (cols.hasNext())
      {
         if (!firstIn)
            buff.append(comma);
         else
            firstIn = false;

         buff.append(" ");
         buff.append((String) cols.next());
      }

      buff.append(" FROM ");
      buff.append(table);
      buff.append(" WHERE ");
      buff.append(key);
      buff.append("=?");

      return buff.toString();
   }
   
   /**
    * Prepare a query statement based on the supplied criteria and attributes
    * to return.
    *
    * @param criteria the conditions used to build the where clause.
    *    If <code>null</code>, all users are returned. This should be done
    *    with care as this could return a result of many thousands of entries.
    *    Only <code>PSLiteral</code> types are allowed for variable and value 
    *    and only the <code>OPTYPE_EQUALS</code> and <code>OPTYPE_LIKE</code> 
    *    operators are allowed.
    * @param attributeNames the set of attributes used to determine the columns
    *    to query. If a supplied attribute is not defined, it is not included
    *    in the results.
    * @param connection a valid connection to the database from where to get
    *    the data, not <code>null</code>.
    *        
    * @return the prepared statement, <code>null</code> if criteria is supplied
    *    and the variable name is not a defined attribute name. The caller owns
    *    the returned statement and is responsible for closing it.
    *    
    * @throws SQLException for any database error.
    */
   public PreparedStatement getPreparedStatement(PSConditional criteria,
      Collection attributeNames, Connection connection) 
      throws SQLException 
   {
      if (connection == null)
         throw new IllegalArgumentException("connection may not be null");
      
      // check valid criteria
      String whereCol = null;
      if (criteria != null)
      {
         if (!(criteria.getOperator().equals(
         PSConditional.OPTYPE_EQUALS) || criteria.getOperator().equals(
            PSConditional.OPTYPE_LIKE)))
         {
            return null;
         }
         
         String var = criteria.getVariable().getValueText();
         if (var.equals(getUserColumn()))
            whereCol = var;
         else
            whereCol = (String) m_userAttributes.get(var);
         
         if (StringUtils.isBlank(whereCol))
            return null;
      }
      
      PSConnectionDetail detail;
      try
      {
         PSConnectionInfo connInfo = new PSConnectionInfo(m_datasource);
         detail = PSConnectionHelper.getConnectionDetail(connInfo);
      }
      catch (NamingException e)
      {
         throw new SQLException(e.getLocalizedMessage());
      }      
      String tableName = PSSqlHelper.qualifyTableName(
         m_table, detail.getDatabase(), detail.getOrigin(), 
         detail.getDriver());      
      
      String comma = ",";

      StringBuffer buff = new StringBuffer();
      buff.append ("SELECT " + m_uidColumn);

      if (attributeNames != null)
      {
         Iterator<String> attrs = attributeNames.iterator();
         while (attrs.hasNext())
         {
            String attr = attrs.next();
            String col = (String) m_userAttributes.get(attr);
            if (StringUtils.isBlank(col))
               continue;

            buff.append(comma);

            buff.append(" ");
            buff.append(col);
         }         
      }


      buff.append(" FROM ");
      buff.append(tableName);
      
      String whereVal = null;
      if (criteria != null)
      {
         whereVal = criteria.getValue().getValueText();
         buff.append(" WHERE ");
         buff.append(whereCol);
         if (criteria.getOperator().equals(PSConditional.OPTYPE_EQUALS))
            buff.append("=?");
         else
         {
            buff.append(" LIKE ?");
         }
      }

      PreparedStatement stmt = PSPreparedStatement.getPreparedStatement(
         connection, buff.toString());
      if (whereVal != null)
         stmt.setString(1, whereVal);
      
      return stmt;
   }   

   /**
    * Get the user attribute for the supplied key.
    * 
    * @return the user attribute column name for the supplied key, may be
    *    <code>null</code> if not found, never empty.
    */
   public String getUserAttribute(String key)
   {
      return (String) m_userAttributes.get(key);
   }

   /**
    * Get a list over all user attribute names.
    * 
    * @return a list of user attribute names, never <code>null</code> may be
    *    empty.
    */
   public Iterator getUserAttributeNames()
   {
      if (m_userAttributes == null || m_userAttributes.isEmpty())
         return PSIteratorUtils.emptyIterator();

      return m_userAttributes.keySet().iterator();
   }

   /**
    * The property key to store the system credential string if a system
    * credential is used.
    */
   public static final String PROPS_SYSTEM_CREDENTIALS = "systemCredentials";
   
   /**
    * The properties keyword representing the name of the datasource to use
    * for the backend connection. 
    */
   public static final String PROPS_DATASOURCE_NAME = "datasourceName";
   
   /**
    * The proeprties keyword representing the database table containing the
    * user information.
    */
   public static final String PROPS_TABLE_NAME = "tableName";

   /**
    * The properties keyword representing the name of the column containing
    * the rhythmyx user name. This column must be defined as the primary key,
    * or it must be defined as the only key in a unique index. If this column
    * does not guarantee uniqueness, rhythmyx will not allow the column to be
    * mapped as the login id.
    */
   public static final String PROPS_UID_COLUMN = "uidColumn";

   /**
    * The propertiesng keyword representing the name of the column
    * containing the rhythmyx user password.
    */
   public static final String PROPS_PW_COLUMN = "passwordColumn";

   /**
    * The properties keyword representing the password filter to use.
    * Storing clear-text passwords in the back-end table is usually undesirable.
    * To allow an application to encrypt/decrypt passwords, a password filter
    * can be defined. The default password filter supplied with rhythmyx can
    * be used or the designer can create a password filter, as described in
    * the 'Writing a Credential Filter' section. The filter is used only to 
    * encrypt a provided password to compare it against an already encrypted 
    * stored password.
    * 
    * @see {@link IPSPasswordFilter.encrypt(String)}
    */
   public static final String PROPS_PW_FILTER = "passwordFilter";

   /**
    * The value used to specify the default password filter.
    */
   public static final String DEFAULT_FILTER = "DEFAULT";

   /**
    * The datsource used, initialized in constructor, never
    * <code>null</code>, empty or modified after that.
    */
   private String m_datasource = null;

   /**
    * The backend table to connect with, initialized in constructor, may
    * be <code>null</code> or empty, never modified.
    */
   private String m_table = null;

   /**
    * The rhythmyx user identifier column used as key for our lookups.
    * Initialized in constructor, never <code>null</code>, empty or modified 
    * after that.
    */
   private String m_uidColumn = null;

   /**
    * The rhythmyx user password column. Initialized in constructor, may be
    * <code>null</code> or empty, never modified.
    */
   private String m_pwColumn = null;

   /**
    * The password filter to be used to authenticate against the back end.
    * Initialized in constructor, may be <code>null</code>, never modified.
    */
   private IPSPasswordFilter m_filter = null;

   /**
    * The prepared statement to use for authentication and backend directory
    * requests. Initialized in {@link #getPreparedStatement(String, Connection)}
    * and never <code>null</code>, empty or modified after that.
    */
   private String m_preparedStatement = null;

   /**
    * A map with all defined user attributes. The key is a <code>String</code>
    * with the attribute name while the value is the backend column name as
    * <code>String</code> that contains the attribute value. Initialized
    * in constructor, never <code>null</code> after that, may be empty, never
    * modified.
    */
   private Map m_userAttributes = null;
   
   /**
    * List of columns to query.   
    */
   private List<Object> m_columns;
}
