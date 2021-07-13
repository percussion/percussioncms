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

package com.percussion.data;

import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * The PSSqlBuilder class is used to build SQL SELECT statements.
 * It can be used to generate single table SELECTs or homogeneous
 * (same DBMS) joined SELECTs. The query optimizer is capable of building
 * heterogeneous (cross DBMS) SELECTs. It calls this class to build
 * each statement and also makes use of the PSQueryJoiner class to join
 * the returned data.
 *
 * @see         PSQueryOptimizer
 * @see         PSQueryJoiner
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public abstract class PSSqlBuilder
{
   protected PSSqlBuilder()
   {
      super();
   }

   /**
    * Get the columns which will be used to locate records when executing
    * the associated statement.
    *
    * @return      an array of columns (may be null)
    */
   public abstract PSBackEndColumn[] getLookupColumns();


   protected void addTableName(
      PSBackEndLogin login, PSBackEndTable curTable,
      PSSqlBuilderContext context, HashMap dtHash)
   {
      /* use "TABLE ALIAS" syntax (if an alias exists) */

      String tableName = getTableName(login, curTable, false);
      context.addText(tableName);

      String sTemp = curTable.getAlias();
      if ((sTemp != null) && !tableName.equals(sTemp)) {
         context.addText(" ");
         context.addText(sTemp);   /* and the table alias */
      }

      /* get the data types for this table */
      loadDataTypes(login, dtHash, curTable);
   }

   /**
    * Get the table name from the PSBackEndTable object, optionally
    * using the alias in lieu of the actual table name.
    *
    * @param   login         the login definition which can be used to
    *                        access the meta data
    *
    * @param   table         the table to get the name of
    *
    * @param   useAlias      <code>true</code> to use the alias name if one
    *                        has been defined; origin.table is used otherwise
    */
   protected String getTableName(
      PSBackEndLogin login, PSBackEndTable table, boolean useAlias)
   {
      return getTableName(login, table, useAlias, this, m_expandedTableNames);
   }

   /**
    * Get the table name from the PSBackEndTable object, optionally
    * using the alias in lieu of the actual table name.
    *
    * @param   login         the login definition which can be used to
    *                        access the meta data
    *
    * @param   table         the table to get the name of
    *
    * @param   useAlias      <code>true</code> to use the alias name if one
    *                        has been defined; origin.table is used otherwise
    *
    * @param   builder     The PSSqlBuilder utilizing this method,
    *                      Never <code>null</code>
    *
    * @param   expandedTableNames The list of expanded table names to work
    *                      with, never <code>null</code>
    */
   static protected String getTableName(
      PSBackEndLogin login, PSBackEndTable table, boolean useAlias,
      PSSqlBuilder builder, HashMap expandedTableNames)
   {
      String tableName;

      // if they don't want alias only, see if we've previously built this
      if (!useAlias) {
         tableName = (String)expandedTableNames.get(table);
         if (tableName != null)
            return tableName;
      }
      else   // use the alias, if it's been defined
         tableName = table.getAlias();

      // otherwise, build it now
      if ((tableName == null) || (tableName.length() == 0)) {
         /* Check DatabaseMetaData to see if names must use
          * catalog, schema and table. Furthermore, what order are these
          * in. For instance, MS SQL uses catalog.schema.table whereas
          * Oracle uses schema.table@catalog
          */
         Connection conn = null;
            PSConnectionDetail detail = null;
         try {
            conn = getConnection(login);
                detail = PSConnectionHelper.getConnectionDetail(login);
            DatabaseMetaData meta = conn.getMetaData();

            StringBuilder buf = new StringBuilder();
            String catalog = detail.getDatabase();
            boolean addedCatalog = false;
            if ((catalog != null) && (catalog.length() != 0)) {
               try {
                  if (meta.supportsCatalogsInDataManipulation()) {
                     if (meta.isCatalogAtStart()) {
                        buf.append(catalog);
                        buf.append(meta.getCatalogSeparator());
                        addedCatalog = true;
                     }
                     else catalog = meta.getCatalogSeparator() + catalog;
                  }
                  else catalog = null;   // not supported
               } catch (Exception cate)  {
                  // this usually means it's not supported, so skip it
                  catalog = null;

                  Object[] args = { table.getDataSource(), 
                            detail.getOrigin(), table.getTable(),
                     "supportsCatalogsInDataManipulation", 
                            cate.toString() };
                     com.percussion.log.PSLogManager.write(
                        new com.percussion.log.PSLogServerWarning(
                        IPSBackEndErrors.LOAD_META_DATA_EXCEPTION,
                        args, false, "SqlBuilder"));
               }
            }
            else catalog = null; // so we don't try tacking it on to the end

            /* if we have an origin, see if it's permitted
             * if we've already written the catalog info to the front,
             * we then need to add the schema, even if it's empty,
             * to avoid catalog.table from being treated as
             * schema.table.
             */
            String origin = detail.getOrigin();
            if (origin == null) origin = "";
            if ((origin.length() != 0) || addedCatalog) {
               try {
                  if (meta.supportsSchemasInDataManipulation()) {
                     builder.supportsSchemaInName = true;
                     buf.append(origin);
                     buf.append('.');
                  }
               } catch (Exception oe) {
                  // this usually means it's not supported, so skip it
                  Object[] args = { table.getDataSource(), 
                            detail.getOrigin(), table.getTable(),
                     "supportsSchemasInDataManipulation", oe.toString()};
                     com.percussion.log.PSLogManager.write(
                        new com.percussion.log.PSLogServerWarning(
                        IPSBackEndErrors.LOAD_META_DATA_EXCEPTION,
                        args, false, "SqlBuilder"));
               }
            }

            buf.append(table.getTable());   // this has to be there

            // if catalog belongs on the end, take care of it now
            if (!addedCatalog && (catalog != null))
               buf.append(catalog);

            tableName = buf.toString();
            expandedTableNames.put(table, tableName);
         } catch (Exception e) 
            {
            // we couldn't get at the meta data, so fall through to the
            // default logic
                String datasource = "";
                String origin = "";
                if (detail != null)
                {
                   datasource = detail.getDatasourceName();
                   origin = detail.getOrigin();
                }
            Object[] args = { datasource, origin, table.getTable(), 
                  "getMetaData", e.toString() };
               com.percussion.log.PSLogManager.write(
                  new com.percussion.log.PSLogServerWarning(
                  IPSBackEndErrors.LOAD_META_DATA_EXCEPTION,
                  args, false, "SqlBuilder"));
         } finally {
            try {
               if (conn != null) conn.close();
            } catch (SQLException e) { /* don't bother with this */ }
         }

         if (tableName == null)
            tableName = builder.getTableOwner(table);

         // we can save this name (don't save alias only, so keep it in here)
         expandedTableNames.put(table, tableName);
      }

      return tableName;
   }

   
   protected String getTableOwner(PSBackEndTable table)
   {
      try
      {
         PSMetaDataCache.loadConnectionDetail(table);
      }
      catch (SQLException e)
      {
         // fatal exception, normally set elsewhere
         throw new RuntimeException(PSSqlException.toString(e));
      }
      
      String tableName = table.getConnectionDetail().getOrigin();
      if ((tableName == null) || (tableName.length() == 0))
         tableName = table.getTable();
      else
         tableName += '.' + table.getTable();

      return tableName;
   }

   /**
    * Expand the column name to use the appropriate syntax.
    * <P>
    * Syntaxes permitted by SQL92 are:
    * <UL>
    *    <LI>tablealias.column</LI>
    *    <LI>schema.table.column</LI>
    *    <LI>table.column</LI>
    * </UL>
    */
   protected String getExpandedColumnName(
      PSBackEndTable table, boolean useAlias, String columnName)
   {
      String tableName = null;

      if (useAlias)
         tableName = table.getAlias();

      if ((tableName == null) || (tableName.length() == 0)) {
         if (supportsSchemaInName)
            tableName = getTableOwner(table);
         else
            tableName = table.getTable();
      }

      return tableName + "." + columnName;
   }

   /**
    * Get the data types for each column in the specified table.
    *
    * @param   login       the login definition which can be used to
    *                      access the meta data
    *
    * @param   dtHash      the hash map into which table.column will be
    *                        used as the key and its java.sql.Types.xxx
    *                      data type will be stored as the value
    *
    * @param   table       the table to catalog
    */
   protected void loadDataTypes(
         PSBackEndLogin login, HashMap dtHash, PSBackEndTable table)
   {
      try
      {
         PSTableMetaData tmd = PSMetaDataCache.getTableMetaData(login, table);
         dtHash.putAll(tmd.loadDataTypes(table.getAlias()));
      }
      catch (SQLException e)
      {
         /* if we couldn't store the data type, String will be used
          * which may cause conversion exceptions, depending upon the
          * back-end. LOG THIS!
          */
         Object[] args = {table.getDataSource(), table.getTable(),
            e.toString()};
         com.percussion.log.PSLogManager.write(
               new com.percussion.log.PSLogServerWarning(
               IPSBackEndErrors.SQL_BUILDER_GET_DATATYPE_EXCEPTION,
               args, false, "SQLBuilder"));
      }
   }

   /**
    * Get a connection associated with the specified login and database.
    *
    * @param   login            the login definition which can be used to
    *                           make the connection
    *
    * @return                  the connection
    */
   protected static java.sql.Connection getConnection(
      PSBackEndLogin login)
      throws java.sql.SQLException
   {
      try
      {
         return PSConnectionHelper.getDbConnection(login);
      }
      catch (NamingException e)
      {
         throw new SQLException(e.getLocalizedMessage());
      }
   }


   /**
    * Determines if the specified type is a known JDBC datatype.
    *
    * @param type The type to check.
    *
    * @return <code>true</code> if it is a JDBC datatype, <code>false</code>
    * if not.
    */
   public static boolean isJdbcDataType(int type)
   {
      return PSTableMetaData.isJdbcDataType(type);
   }


   /**
    *    Guess the corresponding native type's jdbc counterpart.
    *    This will be based on information stored when we kludge
    *    datatypes for misbehaving DBMS drivers (MS-SQL and Access...)
    *    Bug Id: -00-01-0001
    */
   public static short guessNativeDataTypeConversion(short nativeType)
   {
      return PSDatabaseMetaData.guessNativeDataTypeConversion(nativeType);
   }

   /**
    * Stores the expanded table names using the PSBackEndTable object as
    * the key and the expanded table name String as the value.
    */
   protected HashMap m_expandedTableNames = new HashMap();

   protected boolean supportsSchemaInName = false;

}

