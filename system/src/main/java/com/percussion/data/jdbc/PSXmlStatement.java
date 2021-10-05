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

package com.percussion.data.jdbc;

import com.percussion.data.jdbc.sqlparser.SimpleNode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * The PSXmlStatement class extends the File System driver's
 * statement handling for XML file support.
 *
 * @author   Tas Giakouminakis
 * @version   1.0
 * @since   1.0
 */
public class PSXmlStatement extends PSFileSystemStatement {

   /**
    * Package access no-arg constructor. Generally, the only way
    * to get a PSXmlStatement object is to get one from a
    * PSXmlConnection object.
    *
    * @see PSXmlConnection#createStatement
    */
   PSXmlStatement(PSXmlConnection conn)
   {
      super(conn);
   }

   /**
    * Execute a SQL statement that returns a single ResultSet.
    * <p>
    * Limited search capabilities are supported against XML files
    * using SQL constructs. In particular, queries can be generated
    * using the syntax:
    * <pre><code>
    *  SELECT FieldList FROM "XmlFile"
    * </code></pre>
    * where:
    * <ul>
    * <li>FieldList - one or more comma delimited fields (elements or
    *   attributes) defined in the XML document. Use the full object
    *   hierarchy when selecting columns. For instance,
    *   <code>SELECT Products/manufacturer, Products/partnumber</code></li>
    * <li>XmlFile - the name of the XML file(s) to search. Use the XML file's
    *   URL as the name of the file. Only file based URLs (file://) are
    *   currently supported.
    * </ul>
    *
    * @param   sql        typically this is a static SQL SELECT statement
    * @return   returns a ResultSet that contains the data produced
    *               by the query; never null
    * @exception   SQLException   if an error occurs
    */
   public java.sql.ResultSet executeQuery(java.lang.String sql)
      throws SQLException
   {
      try
      {
         SimpleNode statement = parse(sql);
         return doXmlFileQuery(statement);
      }
      catch (Exception e)
      {
         throw new SQLException(e.getMessage());
      }
   }

   /**
    * Execute a SQL INSERT, UPDATE or DELETE statement. In addition,
    * SQL statements that return nothing such as SQL DDL statements
    * can be executed.
    *
    * @param   sql        a SQL INSERT, UPDATE or DELETE statement or a SQL
    *               statement that returns nothing
    * @return   either the row count for INSERT, UPDATE or DELETE
    *               or 0 for SQL statements that return nothing
    * @exception   SQLException   if an error occurs
    */
   public int executeUpdate(java.lang.String sql)
      throws SQLException
   {
      return 0;
   }

   /**
    * In many cases, it is desirable to immediately release a Statements's
    * database and JDBC resources instead of waiting for this to happen
    * when it is automatically closed; the close method provides this
    * immediate release.
    * <p>
    * Note: A Statement is automatically closed when it is garbage
    * collected. When a Statement is closed, its current ResultSet, if
    * one exists, is also closed.
    *
    * @exception   SQLException   if an error occurs
    */
   public void close()
      throws SQLException
   {
   }

   /**
    * The maxFieldSize limit (in bytes) is the maximum amount of data
    * returned for any column value; it only applies to BINARY, VARBINARY,
    * LONGVARBINARY, CHAR, VARCHAR, and LONGVARCHAR columns. If the limit
    * is exceeded, the excess data is silently discarded.
    *
    * @return   the current max column size limit; zero means unlimited
    * @exception   SQLException   if an error occurs
    */
   public int getMaxFieldSize()
      throws SQLException
   {
      return 0;
   }

   /**
    * The maxFieldSize limit (in bytes) is set to limit the size of data
    * that can be returned for any column value; it only applies to
    * BINARY, VARBINARY, LONGVARBINARY, CHAR, VARCHAR, and LONGVARCHAR
    * fields. If the limit is exceeded, the excess data is silently
    * discarded. For maximum portability use values greater than 256.
    *
    * @param   max            the new max column size limit; zero means
    *                        unlimited
    * @exception   SQLException   if an error occurs
    */
   public void setMaxFieldSize(int max)
      throws SQLException
   {
   }

   /**
    * The maxRows limit is the maximum number of rows that a ResultSet
    * can contain. If the limit is exceeded, the excess rows are silently
    * dropped.
    *
    * @return   the current max row limit; zero means unlimited
    * @exception   SQLException   if an error occurs
    */
   public int getMaxRows()
      throws SQLException
   {
      return 0;
   }

   /**
    * The maxRows limit is set to limit the number of rows that any
    * ResultSet can contain. If the limit is exceeded, the excess rows
    * are silently dropped.
    *
    * @param   max            the new max rows limit; zero means unlimited
    * @exception   SQLException   if an error occurs
    */
   public void setMaxRows(int max)
      throws SQLException
   {
   }

   /**
    * If escape scanning is on (the default), the driver will do escape
    * substitution before sending the SQL to the database. Note: Since
    * prepared statements have usually been parsed prior to making this
    * call, disabling escape processing for prepared statements will like
    * have no affect.
    *
    * @param   enable         <code>true</code> to enable;
    *                        <code>false</code> to disable
    * @exception   SQLException   if an error occurs
    */
   public void setEscapeProcessing(boolean enable)
      throws SQLException
   {
   }

   /**
    * The queryTimeout limit is the number of seconds the driver will
    * wait for a Statement to execute. If the limit is exceeded, a
    * SQLException is thrown.
    *
    * @return   the current query timeout limit in seconds;
    *                        zero means unlimited
    * @exception   SQLException   if an error occurs
    */
   public int getQueryTimeout()
      throws SQLException
   {
      return 0;
   }

   /**
    * The queryTimeout limit is the number of seconds the driver will
    * wait for a Statement to execute. If the limit is exceeded, a
    * SQLException is thrown.
    *
    * @param   seconds        the new query timeout limit in seconds;
    *                        zero means unlimited
    * @exception   SQLException   if an error occurs
    */
   public void setQueryTimeout(int seconds)
      throws SQLException
   {
   }

   /**
    * Cancel can be used by one thread to cancel a statement that is
    * being executed by another thread.
    *
    * @exception   SQLException   if an error occurs
    */
   public void cancel()
      throws SQLException
   {
   }

   /**
    * The first warning reported by calls on this Statement is returned.
    * A Statment's execute methods clear its SQLWarning chain. Subsequent
    * Statement warnings will be chained to this SQLWarning.
    * <p>
    * The warning chain is automatically cleared each time a statement is
    * (re)executed.
    * <p>
    * Note: If you are processing a ResultSet then any warnings associated
    * with ResultSet reads will be chained on the ResultSet object.
    *
    * @return   the first SQLWarning or null
    * @exception   SQLException   if an error occurs
    */
   public SQLWarning getWarnings()
      throws SQLException
   {
      return null;
   }

   /**
    * After this call, getWarnings returns null until a new warning is
    * reported for this Statement.
    *
    * @exception   SQLException   if an error occurs
    */
   public void clearWarnings()
      throws SQLException
   {
   }

   /**
    * setCursorname defines the SQL cursor name that will be used by
    * subsequent Statement execute methods. This name can then be used
    * in SQL positioned update/delete statements to identify the current
    * row in the ResultSet generated by this statement. If the database
    * doesn't support positioned update/delete, this method is a noop.
    * <p>
    * Note: By definition, positioned update/delete execution must be
    * done by a different Statement than the one which generated the
    * ResultSet being used for positioning. Also, cursor names must be
    * unique within a Connection.
    *
    * @param   name           the new cursor name.
    * @exception   SQLException   if an error occurs
    */
   public void setCursorName(java.lang.String name)
      throws SQLException
   {
   }

   /**
    * Execute a SQL statement that may return multiple results.
    * Under some (uncommon) situations a single SQL statement may return
    * multiple result sets and/or update counts. Normally you can ignore
    * this, unless you're executing a stored procedure that you know may
    * return multiple results, or unless you're dynamically executing an
    * unknown SQL string. The "execute", "getMoreResults", "getResultSet"
    * and "getUpdateCount" methods let you navigate through multiple
    * results. The "execute" method executes a SQL statement and indicates
    * the form of the first result. You can then use getResultSet or
    * getUpdateCount to retrieve the result, and getMoreResults to move
    * to any subsequent result(s).
    *
    * @param   sql            any SQL statement
    * @return   <code>true</code> if the next result is a
    *                        ResultSet; <code>false</code> if it is an
    *                        update count or there are no more results
    * @exception   SQLException   if an error occurs
    * @see   #getResultSet
    * @see   #getUpdateCount
    * @see   #getMoreResults
    */
   public boolean execute(java.lang.String sql)
      throws SQLException
   {
      return false;
   }

   /**
    * getResultSet returns the current result as a ResultSet. It should
    * only be called once per result.
    *
    * @return   the current result as a ResultSet;
    *                        <code>null</code> if the result is an
    *                        update count or there are no more results
    * @exception   SQLException   if an error occurs
    * @see   #execute
    */
   public ResultSet getResultSet()
      throws SQLException
   {
      return null;
   }

   /**
    * getUpdateCount returns the current result as an update count; if
    * the result is a ResultSet or there are no more results, -1 is
    * returned. It should only be called once per result.
    *
    * @return   the current result as an update count;
    *                        -1 if it is a ResultSet or there are no
    *                        more results
    * @exception   SQLException   if an error occurs
    * @see   #execute
    */
   public int getUpdateCount()
      throws SQLException
   {
      return -1;
   }

   /**
    * getMoreResults moves to a Statement's next result. It returns
    * <code>true</code> if this result is a ResultSet. getMoreResults
    * also implicitly closes any current ResultSet obtained with
    * getResultSet. There are no more results when
    * <code>(!getMoreResults() && (getUpdateCount() == -1)</code>
    *
    * @return   <code>true</code> if the next result is
    *                        a ResultSet; false if it is an update
    *                        count or there are no more results
    *                        more results
    * @exception   SQLException   if an error occurs
    * @see   #execute
    */
   public boolean getMoreResults()
      throws SQLException
   {
      return false;
   }

   /**
    * JDBC 2.0 Gives the driver a hint as to the direction in which the
    * rows in a result set will be processed. The hint applies only to
    * result sets created using this Statement object. The default value
    * is ResultSet.FETCH_FORWARD.
    *
    * Note that this method sets the default fetch direction for result
    * sets generated by this Statement object. Each result set has its
    * own methods for getting and setting its own fetch direction.
    *
    * @param   direction      the initial direction for processing rows
    *
    * @exception   SQLException   if a database access error occurs or the
    *                        given direction is not one of
    *                        ResultSet.FETCH_FORWARD,
    *                        ResultSet.FETCH_REVERSE, or
    *                        ResultSet.FETCH_UNKNOWN
    */
   public void setFetchDirection(int direction)
      throws SQLException
   {
   }

   /**
    * JDBC 2.0 Retrieves the direction for fetching rows from database
    * tables that is the default for result sets generated from this
    * Statement object. If this Statement object has not set a fetch
    * direction by calling the method setFetchDirection, the return
    * value is implementation-specific.
    *
    * @return   the default fetch direction for result sets generated
    *         from this Statement object
    *
    * @exception   SQLException   if a database access error occurs
    */
   public int getFetchDirection()
      throws SQLException
   {
      return 0;
   }

   /**
    * JDBC 2.0 Gives the JDBC driver a hint as to the number of rows
    * that should be fetched from the database when more rows are needed.
    * The number of rows specified affects only result sets created using
    * this statement. If the value specified is zero, then the hint is
    * ignored. The default value is zero.
    *
    * @param   rows           the number of rows to fetch
    *
    * @exception   SQLException   if a database access error occurs,
    *                        or the condition 0 <= rows <=
    *                        this.getMaxRows() is not satisfied.
    */
   public void setFetchSize(int rows)
      throws SQLException
   {
   }

   /**
    * JDBC 2.0 Retrieves the number of result set rows that is the
    * default fetch size for result sets generated from this Statement
    * object. If this Statement object has not set a fetch size by
    * calling the method setFetchSize, the return value is
    * implementation-specific.
    *
    * @return   the default fetch size for result sets generated from
    *         this Statement object
    *
    * @exception   SQLException   if a database access error occurs
    */
   public int getFetchSize()
      throws SQLException
   {
      return 0;
   }

   /**
    * JDBC 2.0 Retrieves the result set concurrency.
    *
    * @return   the result set concurrency
    *
    * @exception   SQLException   if a database access error occurs
    */
   public int getResultSetConcurrency()
      throws SQLException
   {
      return 0;
   }

   /**
    * JDBC 2.0 Determine the result set type.
    *
    * @return   the result set type
    *
    * @exception   SQLException   if a database access error occurs
    */
   public int getResultSetType()
      throws SQLException
   {
      return 0;
   }

   /**
    * JDBC 2.0 Adds a SQL command to the current batch of commmands for
    * the statement. This method is optional.
    *
    * @param   sql            typically this is a static SQL INSERT or
    *                        UPDATE statement
    *
    * @exception   SQLException   if a database access error occurs, or
    *                        the driver does not support batch statements
    */
   public void addBatch(java.lang.String sql)
      throws SQLException
   {
   }

   /**
    * JDBC 2.0 Makes the set of commands in the current batch empty.
    * This method is optional.
    *
    * @exception   SQLException   if a database access error occurs or
    *                        the driver does not support batch statements
    */
   public void clearBatch()
      throws SQLException
   {
   }

   /**
    * JDBC 2.0 Submits a batch of commands to the database for execution.
    * This method is optional.
    *
    * @return   an array of update counts containing one element for each
    *         command in the batch. The array is ordered according to
    *         the order in which commands were inserted into the batch.
    *
    * @exception   SQLException   if a database access error occurs or
    *                        the driver does not support batch statements
    */
   public int[] executeBatch()
      throws SQLException
   {
      return null;
   }

   /**
    * JDBC 2.0 Returns the Connection object that produced this Statement
    * object.
    *
    * @return   the connection that produced this statement
    *
    * @exception   SQLException   if a database access error occurs
    */
   public java.sql.Connection getConnection()
      throws SQLException
   {
      return null;
   }

   /**
    * @author   chadloder
    *
    * Private utility method to execute a parsed statement.
    *
    * @param   statement     The root of the parse tree.
    *
    * @since   1.2 1999/5/7
    *
    */
   private ResultSet doXmlFileQuery(SimpleNode node)
      throws java.io.IOException, SQLException
   {
      PSXmlDocumentQuery query = new PSXmlDocumentQuery(node);
      return query.run();
   }
}

