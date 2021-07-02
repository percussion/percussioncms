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
package com.percussion.fastforward.sfp;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.extension.services.PSDatabasePool;
import com.percussion.server.IPSInternalRequest;
import com.percussion.util.PSPreparedStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Prepares and executes a simple SQL Query using the standard Rhythmyx
 * repository database. Queries can be provided in two ways:
 * <ol>
 * <li>Using a SQL Statement, that may contain parameter placeholders (usually
 * "?"). In this case, an array of parameter objects must be supplied. If no
 * parameters are needed, the list must be <code>empty</code></li>
 * <li>Using an Internal Request. In this case, the request must have already
 * been created. All parameters are passed as part of the create.</li>
 * </ol>
 * <p>
 * The Objects returned from these queries are mapped using the default JDBC
 * type mapping, which differs based on database driver implementation. For
 * example, SQL Server may return an <code>Integer</code> object for a numeric
 * column where Oracle returns a <code>BigDecimal</code>. Since both of these
 * classes are subclasses of <code>Number</code>, callers should assume that
 * numeric values are <code>Number</code> objects unless they wish to perform
 * other special handling, using reflection or similar methods.
 * <p>
 * This class makes no attempt to handle complex datatypes. Longs, CLOBs, Blobs,
 * etc. are not handled.
 * <p>
 * 
 * @author DavidBenua
 */
public class PSSimpleSqlQuery
{
   /**
    * static methods only, never constructed.
    */
   private PSSimpleSqlQuery()
   {

   }

   /**
    * Perform a query with multiple rows.
    * 
    * @param query the SQL statement.
    * @param params a list of Objects that represent the placeholders in the
    *           SQL. Never <code>null</code>. May be <code>empty</code>.
    * @return a List of Object[] representing the rows of the result set.
    * @throws SQLException
    */
   public static List doQuery(String query, List params) throws SQLException
   {
      List resultList = new ArrayList();
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      Object[] resultRow = null;
      try
      {
         conn = ms_pool.getConnection();
         stmt = PSPreparedStatement.getPreparedStatement(conn, query);
         setInternalParams(stmt, params);
         rs = stmt.executeQuery();
         boolean valid = rs.next();
         while (valid)
         {
            resultRow = buildRowResult(rs);
            resultList.add(resultRow);
            valid = rs.next();
         }
      }
      catch (SQLException ex)
      {
         ms_log.error(PSSimpleSqlQuery.class.getName(), ex);
         throw (SQLException) ex.fillInStackTrace();
      }
      finally
      {
         cleanupResult(rs, stmt, conn);
      }
      return resultList;
   }

   /**
    * Perform a query based on an Internal Request. The query associated with
    * the request is processed and returned as a list of Object[];
    * 
    * @param ir the internal request.
    * @return a List of Object[] representing the rows of the result set.
    * @throws PSInternalRequestCallException
    * @throws SQLException
    */
   public static List doQuery(IPSInternalRequest ir)
         throws PSInternalRequestCallException, SQLException
   {
      List resultList = new ArrayList();
      ResultSet rs = null;
      Object[] resultRow = null;
      try
      {
         rs = ir.getResultSet();
         boolean valid = rs.next();
         while (valid)
         {
            resultRow = buildRowResult(rs);
            resultList.add(resultRow);
            valid = rs.next();
         }
      }
      catch (SQLException ex)
      {
         ms_log.error(PSSimpleSqlQuery.class.getName(), ex);
         throw (SQLException) ex.fillInStackTrace();
      }
      catch (PSInternalRequestCallException ex2)
      {
         ms_log.error(PSSimpleSqlQuery.class.getName(), ex2);
         throw (PSInternalRequestCallException) ex2.fillInStackTrace();
      }
      finally
      {
         ir.cleanUp();
      }
      return resultList;
   }

   /**
    * Perform a query that returns only one row. If the query provided returns
    * only one row, only the first row will be examined.
    * 
    * @param query the SQL query.
    * @param params a List of Objects that represent the parameters.
    * @return an Object[] that represents the first row returned from the query.
    *         May be <code>null</code>.
    * @throws SQLException
    */
   public static Object[] doSingleRowQuery(String query, List params)
         throws SQLException
   {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      Object[] resultRow = null;
      try
      {
         conn = ms_pool.getConnection();
         stmt = PSPreparedStatement.getPreparedStatement(conn, query);
         setInternalParams(stmt, params);
         rs = stmt.executeQuery();
         boolean valid = rs.next();
         if (valid)
         {
            resultRow = buildRowResult(rs);
         }
      }
      catch (SQLException ex)
      {
         ms_log.error(PSSimpleSqlQuery.class.getName(), ex);
         throw (SQLException) ex.fillInStackTrace();
      }
      finally
      {
         cleanupResult(rs, stmt, conn);
      }
      return resultRow;
   }

   /**
    * Perform an Internal Request query that returns only one row. If the query
    * provided returns only one row, only the first row will be examined.
    * 
    * @param ir the Internal Request.
    * @return an Object[] representing the columns returned by the query. May be
    *         <code>null</code>
    * @throws SQLException
    * @throws PSInternalRequestCallException
    */
   public static Object[] doSingleRowQuery(IPSInternalRequest ir)
         throws SQLException, PSInternalRequestCallException
   {
      ResultSet rs = null;
      Object[] resultRow = null;
      try
      {
         rs = ir.getResultSet();
         boolean valid = rs.next();
         if (valid)
         {
            resultRow = buildRowResult(rs);
         }
      }
      catch (SQLException ex)
      {
         ms_log.error(PSSimpleSqlQuery.class.getName(), ex);
         throw (SQLException) ex.fillInStackTrace();
      }
      catch (PSInternalRequestCallException ex2)
      {
         ms_log.error(PSSimpleSqlQuery.class.getName(), ex2);
         throw (PSInternalRequestCallException) ex2.fillInStackTrace();
      }
      finally
      {
         ir.cleanUp();
      }
      return resultRow;
   }

   /**
    * Clean up after a query. Closes the result sete, statement and connection,
    * handles any errors and releases the connection back to the pool. Intended
    * to be called in the <code>finally</code> clause.
    * 
    * @param rs the result set. May be <code>null</code>
    * @param stmt the statement. May be <code>null</code>
    * @param conn the connection. May be <code>null</code>
    * @throws SQLException if any errors occur.
    */
   private static void cleanupResult(ResultSet rs, Statement stmt,
         Connection conn) throws SQLException
   {
      if (rs != null)
      {
         rs.close();
      }
      if (stmt != null)
      {
         stmt.close();
      }
      if (conn != null)
      {
         try
         {
            conn.close();
         }
         catch (SQLException e)
         {
            ms_log.error(PSSimpleSqlQuery.class.getName(), e);
         }
         try
         {
            ms_pool.releaseConnection(conn);
         }
         catch (SQLException e1)
         {
            ms_log.error(PSSimpleSqlQuery.class.getName(), e1);
         }
      }

   }

   /**
    * Set the parameter list on a prepared statement. The mapping is done using
    * the default JDBC Type/Object mapping. Note that no checking is done to
    * assure that the required number of parameters are supplied.
    * 
    * @param stmt the prepared statement. Must not be <code>null</code>
    * @param params a List of Object suitable for the parameters. Must not be
    *           <code>null</code> but may be <code>empty</code>
    * @throws SQLException
    */
   private static void setInternalParams(PreparedStatement stmt, List params)
         throws SQLException
   {
      Iterator parm = params.iterator();
      int i = 1;
      while (parm.hasNext())
      {
         stmt.setObject(i, parm.next());
         i++;
      }
   }

   /**
    * build a result row as an Object[]. The size of the row is determined from
    * the result set metadata. The object types are determined by the default
    * JDBC type / object mapping.
    * 
    * @param rs the result set. Must not be <code>null</code>
    * @return the array of objects representing the current row of the result
    *         set.
    * @throws SQLException
    */
   private static Object[] buildRowResult(ResultSet rs) throws SQLException
   {
      ResultSetMetaData meta = rs.getMetaData();
      int colcount = meta.getColumnCount();
      Object[] resultRow = new Object[colcount];
      for (int i = 0; i < colcount; i++)
      {
         if (ms_log.isDebugEnabled())
         {
            int j = i + 1;
            Object[] msg = new Object[4];
            msg[0] = new Integer(j);
            msg[1] = meta.getColumnName(j);
            msg[2] = meta.getColumnTypeName(j);
            msg[3] = meta.getColumnClassName(j);
            ms_log.debug(MessageFormat.format(LOG_MSG_ROW, msg));
         }
         resultRow[i] = rs.getObject(i + 1);
         //ms_log.debug("Classname " + resultRow[i].getClass().getName());
         if (rs.wasNull())
         {
            ms_log.debug("column is null");
            resultRow[i] = null;

         }
      }
      return resultRow;
   }

   private static final String LOG_MSG_ROW = 
      "Column {0} named {1} typename {2} classname {3}";

   /**
    * Database pool from Rhythmyx Server
    */
   static PSDatabasePool ms_pool = PSDatabasePool.getDatabasePool();

   /**
    * Logger for diagnostic messages.
    */
   private static final Logger ms_log = LogManager.getLogger(PSSimpleSqlQuery.class);

}
