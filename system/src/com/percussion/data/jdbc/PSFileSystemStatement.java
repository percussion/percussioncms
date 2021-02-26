/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.data.jdbc;

import com.percussion.data.PSResultSet;
import com.percussion.data.PSResultSetColumnMetaData;
import com.percussion.data.PSResultSetMetaData;
import com.percussion.data.jdbc.sqlparser.ASTColumnReference;
import com.percussion.data.jdbc.sqlparser.ASTDerivedColumn;
import com.percussion.data.jdbc.sqlparser.ASTDirectSQLDataStatement;
import com.percussion.data.jdbc.sqlparser.ASTDirectSelectStatementMultipleRows;
import com.percussion.data.jdbc.sqlparser.ASTFileSpec;
import com.percussion.data.jdbc.sqlparser.ASTFromClause;
import com.percussion.data.jdbc.sqlparser.ASTQuerySpecification;
import com.percussion.data.jdbc.sqlparser.ASTSelectList;
import com.percussion.data.jdbc.sqlparser.ASTStatementRoot;
import com.percussion.data.jdbc.sqlparser.ASTTableExpression;
import com.percussion.data.jdbc.sqlparser.ASTTableReference;
import com.percussion.data.jdbc.sqlparser.ASTWhereClause;
import com.percussion.data.jdbc.sqlparser.Node;
import com.percussion.data.jdbc.sqlparser.ParseException;
import com.percussion.data.jdbc.sqlparser.SQLParser;
import com.percussion.data.jdbc.sqlparser.SimpleNode;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Date;
import java.util.Vector;

/**
 * The PSFileSystemStatement class implements statement handling for
 * the File System driver.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSFileSystemStatement implements Statement {

   public PSFileSystemStatement(PSFileSystemConnection conn)
   {
      m_columnNames = new java.util.HashMap();
      m_conn = conn;
      m_metaData = new PSResultSetMetaData();
   }

   /**
    * Execute a SQL statement that returns a single ResultSet.
    * <p>
    * Limited search capabilities are supported against the file system
    * using SQL constructs. In particular, queries can be generated
    * using the syntax:
    * <pre><code>
    *    SELECT FieldList FROM SearchPath
    *                     WHERE SearchConditions
    *                     ORDER BY FieldList
    * </code></pre>
    * where:
    * <ul>
    * <li>FieldList - one or more comma delimited fields, as defined
    *     in the Fields in the File System Result Set section. For
    *     example, <code>SELECT fullname, name, modified, length</code></li>
    * <li>SearchPath - the directory(ies) to search in. To search multiple
    *     directories, separate entries with commas. To perform a search in
    *     a directory plus all its sub-directories, append the path
    *     separator and * to the path name. E2 defines the forward slash
    *     (/) as the path separator. To search the directory c:\mydir and
    *     the directory c:\mydir2 plus all its sub-directories, use:
    *     <code>FROM c:/mydir, c:/mydir2/*</code></li>
    * <li>SearchConditions - we support limited search capabilities using
    *     the syntax <code>Field SearchOperator Value</code>. To use
    *     multiple search conditions, use <code>AND</code> to require both
    *     conditions be true and <code>OR</code> to require that at least
    *     one of the conditions is true. For instance, use
    *     <code>SearchCondition AND SearchCondition</code> to check that
    *     both conditions are met. Use <code>SearchCondition OR
    *     SearchCondition</code> to check that at least one of the
    *     conditions is met. Supported search operators are:
    *     <ul>
    *     <li>=, >, >=, <, <= - standard operators to test equality, etc.
    *         such as length <= 1024 to check for files which are no larger
    *         than 1K. These operations are supported against character,
    *         time/date and integer fields.</li>
    *     <li>LIKE - perform character string pattern matching, following
    *         SQL string rules. Namely, it must be surrounded by single
    *         quotes, % is the wildcard character and the string comparison
    *         is case sensitive. This option is only supported against the
    *         path, name and fullname fields. At this time, searching the
    *         contents field is not supported. For example, to test for
    *         files ending with .xml use: <code>name LIKE '%.xml'</code></li>
    *     </ul></li>
    * </ul>
    *
    * @param   sql      typically this is a static SQL SELECT statement
    * @return           returns a ResultSet that contains the data produced
    *                   by the query; never null
    * @exception  SQLException   if an error occurs
    */
   public java.sql.ResultSet executeQuery(java.lang.String sql)
   throws SQLException
   {
      try
      {
         SimpleNode statement = parse(sql);
         doFileQuery(statement);
      }
      catch (Exception e)
      {
         throw new SQLException(e.getMessage());
      }

      PSResultSet result = new PSResultSet(m_results, m_columnNames, m_metaData);
      return result;
   }
 
   /**
    * Execute a SQL INSERT, UPDATE or DELETE statement. In addition,
    * SQL statements that return nothing such as SQL DDL statements
    * can be executed.
    *
    * @param   sql      a SQL INSERT, UPDATE or DELETE statement or a SQL
    *                   statement that returns nothing 
    * @return           either the row count for INSERT, UPDATE or DELETE
    *                   or 0 for SQL statements that return nothing
    * @exception  SQLException   if an error occurs
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
    * @exception  SQLException   if an error occurs
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
    * @return     the current max column size limit; zero means unlimited
    * @exception  SQLException   if an error occurs
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
    * @param      max            the new max column size limit; zero means
    *                            unlimited
    * @exception  SQLException   if an error occurs
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
    * @return     the current max row limit; zero means unlimited
    * @exception  SQLException   if an error occurs
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
    * @param      max            the new max rows limit; zero means unlimited
    * @exception  SQLException   if an error occurs
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
    * @param      enable         <code>true</code> to enable;
    *                            <code>false</code> to disable
    * @exception  SQLException   if an error occurs
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
    * @return                    the current query timeout limit in seconds;
    *                            zero means unlimited
    * @exception  SQLException   if an error occurs
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
    * @param      seconds        the new query timeout limit in seconds;
    *                            zero means unlimited
    * @exception  SQLException   if an error occurs
    */
   public void setQueryTimeout(int seconds)
      throws SQLException
   {
   }

   /**
    * Cancel can be used by one thread to cancel a statement that is
    * being executed by another thread.
    *
    * @exception  SQLException   if an error occurs
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
    * @return                    the first SQLWarning or null
    * @exception  SQLException   if an error occurs
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
    * @exception  SQLException   if an error occurs
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
    * @param      name           the new cursor name.
    * @exception  SQLException   if an error occurs
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
    * <p>
    * This driver does not support multiple result sets. See the
    * {@link #executeQuery(java.lang.String) executeQuery} and 
    * {@link #executeUpdate(java.lang.String) executeUpdate} methods for
    * a description of the SQL syntax supported by this driver.
    *
    * @param      sql            any SQL statement
    * @return                    <code>true</code> if the next result is a
    *                            ResultSet; <code>false</code> if it is an
    *                            update count or there are no more results
    * @exception  SQLException   if an error occurs
    * @see        #getResultSet
    * @see        #getUpdateCount
    * @see        #getMoreResults
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
    * @return                    the current result as a ResultSet; 
    *                            <code>null</code> if the result is an
    *                            update count or there are no more results
    * @exception  SQLException   if an error occurs
    * @see        #execute
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
    * @return                    the current result as an update count;
    *                            -1 if it is a ResultSet or there are no
    *                            more results
    * @exception  SQLException   if an error occurs
    * @see        #execute
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
    * @return                    <code>true</code> if the next result is
    *                            a ResultSet; false if it is an update
    *                            count or there are no more results
    *
    * @exception  SQLException   if an error occurs
    * @see        #execute
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
    * @param      direction      the initial direction for processing rows
    *
    * @exception  SQLException   if a database access error occurs or the
    *                            given direction is not one of
    *                            ResultSet.FETCH_FORWARD,
    *                            ResultSet.FETCH_REVERSE, or
    *                            ResultSet.FETCH_UNKNOWN
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
    * @return     the default fetch direction for result sets generated
    *             from this Statement object
    *
    * @exception  SQLException   if a database access error occurs
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
    * @param      rows           the number of rows to fetch
    *
    * @exception  SQLException   if a database access error occurs,
    *                            or the condition 0 <= rows <= 
    *                            this.getMaxRows() is not satisfied.
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
    * @return     the default fetch size for result sets generated from
    *             this Statement object
    *
    * @exception  SQLException   if a database access error occurs
    */
   public int getFetchSize()
      throws SQLException
   {
      return 0;
   }

   /**
    * JDBC 2.0 Retrieves the result set concurrency.
    *
    * @return     the result set concurrency
    *
    * @exception  SQLException   if a database access error occurs
    */
   public int getResultSetConcurrency()
      throws SQLException
   {
      return 0;
   }

   /**
    * JDBC 2.0 Determine the result set type.
    *
    * @return     the result set type
    *
    * @exception  SQLException   if a database access error occurs
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
    * @param      sql            typically this is a static SQL INSERT or
    *                            UPDATE statement
    *
    * @exception  SQLException   if a database access error occurs, or
    *                            the driver does not support batch statements
    */
   public void addBatch(java.lang.String sql)
      throws SQLException
   {
   }

   /**
    * JDBC 2.0 Makes the set of commands in the current batch empty.
    * This method is optional.
    *
    * @exception  SQLException   if a database access error occurs or
    *                            the driver does not support batch statements
    */
   public void clearBatch()
      throws SQLException
   {
   }

   /**
    * JDBC 2.0 Submits a batch of commands to the database for execution.
    * This method is optional.
    *
    * @return     an array of update counts containing one element for each
    *             command in the batch. The array is ordered according to
    *             the order in which commands were inserted into the batch.
    *
    * @exception  SQLException   if a database access error occurs or
    *                            the driver does not support batch statements
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
    * @return     the connection that produced this statement
    *
    * @exception  SQLException   if a database access error occurs
    */
   public java.sql.Connection getConnection()
      throws SQLException
   {
      return null;
   }

   /**
    * A utility function to parse a string
    */
   protected SimpleNode parse(String sql) throws ParseException
   {
      sql = sql.trim();
      if (!sql.endsWith(";"))
         sql = sql + ";";

      InputStream stream = new ByteArrayInputStream(sql.getBytes());
      if (m_parser == null)
         m_parser = new SQLParser(stream);
      else
         m_parser.ReInit(stream);

      SimpleNode statement = m_parser.StatementRoot();
      return statement;
   }

   /**
   * A utility function to execute the file query and store the result data
   *
   */
   private void doFileQuery(Node node) throws java.io.IOException, SQLException
   {
      ASTStatementRoot root = 
         (ASTStatementRoot)node;

      ASTDirectSQLDataStatement statement
         = (ASTDirectSQLDataStatement)root.jjtGetChild(0);

      ASTDirectSelectStatementMultipleRows select =
         (ASTDirectSelectStatementMultipleRows)statement.jjtGetChild(0);

      ASTQuerySpecification query =
         (ASTQuerySpecification)select.jjtGetChild(0);

      ASTSelectList selectList =
         (ASTSelectList)query.jjtGetChild(0);

      ASTDerivedColumn col;
      ASTColumnReference ref;

      int[] columnIndexes = new int[selectList.jjtGetNumChildren()];

      for (int i = 0; i < columnIndexes.length; i++)
      {
         col = (ASTDerivedColumn)selectList.jjtGetChild(i);
         ref = (ASTColumnReference)col.jjtGetChild(0);
         String colName = ref.getColumn();
         m_columnNames.put(colName, new Integer(i + 1));

         int colConst = getColumnConstant(colName);
         columnIndexes[i] = colConst;
   
         // add some info to the result set meta data
         m_metaData.addColumnMetaData(new PSResultSetColumnMetaData(
            colName,
            getColumnType(colConst),
            getColumnSize(colConst)));

      }

      ASTTableExpression tableEx =
         (ASTTableExpression)query.jjtGetChild(1);

      ASTWhereClause where = null;
      if (tableEx.jjtGetNumChildren() > 1)
      {
         where = (ASTWhereClause)tableEx.jjtGetChild(1);
      }

      ASTFromClause from =
         (ASTFromClause)tableEx.jjtGetChild(0);
      
      ASTTableReference tableRef;
      ASTFileSpec fileSpec = null;

      File[] files = new File[from.jjtGetNumChildren()];

      for (int i = 0; i < from.jjtGetNumChildren(); i++)
      {
         tableRef = (ASTTableReference)from.jjtGetChild(i);
         if (tableRef == null)
         {
            throw new SQLException("Error: A table reference must be specified.");
         }

         if (tableRef.jjtGetNumChildren() == 0)
         {
            throw new SQLException("Error: A file specification must be specified.");
         }

         fileSpec = (ASTFileSpec)tableRef.jjtGetChild(0);

         if (fileSpec == null)
         {
            throw new SQLException("Error: A file specification must be specified");
         }
         //System.out.print("Table path: " + fileSpec.getValue());
         //if (fileSpec.isRecursive())
         //   System.out.print(" (recursive)");
         //System.out.print("\n");
         files[i] = new File(fileSpec.getValue()); 
      }

      FileList fileList =
         new FileList(files, null, true, fileSpec.isRecursive(), columnIndexes);

      m_results = fileList.getResults();
   }

   /**
    * A utility function to return the appropriate column constant for
    * a SQL column name
    */
   private static int getColumnConstant(String name) throws SQLException
   {
      int colIdx;
      // ensure that name is one of: path, name, fullname, modified, length, contents
      if (name.equalsIgnoreCase("path"))
      {
         colIdx = FileList.PATH;
      }
      else if (name.equalsIgnoreCase("name"))
      {
         colIdx = FileList.NAME;
      }
      else if (name.equalsIgnoreCase("fullname"))
      {
         colIdx = FileList.FULLNAME;
      }
      else if (name.equalsIgnoreCase("modified"))
      {
         colIdx = FileList.MODIFIED;
      }
      else if (name.equalsIgnoreCase("length"))
      {
         colIdx = FileList.LENGTH;
      }
      else if (name.equalsIgnoreCase("contents"))
      {
         colIdx = FileList.CONTENTS;
      }
      else
         throw new SQLException("Column name invalid: " + name);

      return colIdx;
   }

   /**
    * A utility function to return the appropriate column type for
    * a given column constant
    */
   private static short getColumnType(int columnConstant) throws SQLException
   {
      short colType = java.sql.Types.VARCHAR; // default
      switch (columnConstant)
      {
      case FileList.PATH:
      case FileList.NAME:
      case FileList.FULLNAME:
      case FileList.MODIFIED:
      case FileList.CONTENTS:
         break;
      case FileList.LENGTH:
         colType = java.sql.Types.BIGINT;
      }

      return colType;
   }

   /**
    * A utility function to return the appropriate column size for
    * a given column constant
    */
   private static int getColumnSize(int columnConstant) throws SQLException
   {
      int colSize = 1024; // default
      switch (columnConstant)
      {
      case FileList.PATH:
      case FileList.FULLNAME:
         colSize = 2048;
         break;
      case FileList.NAME:
         colSize = 256;
         break;
      case FileList.MODIFIED:
         colSize = 64;
         break;
      case FileList.CONTENTS:
         colSize = 0;
         break;
      case FileList.LENGTH:
         colSize = 8;
      }

      return colSize;
   }

   // for internal use -- a helper class to build a recursive file list
  // into a sequence of columns
  class FileList
  {
     public static final int PATH = 0;
     public static final int NAME = 1;
      public static final int FULLNAME = 2;
     public static final int MODIFIED = 3;
     public static final int LENGTH = 4;
     public static final int CONTENTS = 5;


     /**
      * Constructor - build a file list starting in the specified
      * directory, including all and only those files which are
      * accepted by the given filter. Can be recursive.
      *
      * @param   rootDirs   The starting directories of the file list.
      * The directories themselves will not be incldued in the list.
      * Every directory will be processed in order.
      *
      * @param   fileFilter   The filter that will be used to select
      * the files for the list. Specify <CODE>null</CODE> to select
      * all files.
      *
      * @param   addDirs   If <CODE>true</CODE>, will add any dirs to
      * the list that match the filter. If <CODE>false</CODE>, will
       * not add any dirs to the list (even though it may recurse into
      * those dirs).
      *
      * @param   recursive   If <CODE>true</CODE>, will recurse into
      * all directories which pass through the filter. If <CODE>false</CODE>,
      * will only add matching files/dirs from the root dir, and will not
      * recurse into any directories.
      *
      * @param   columnConstants   The definition of the columns. Each element
      * is an integer constant representing the particular file data that
      * will go in that column. The number of columns is equal to the length
      * of the columnConstants array
      */
     public FileList(File[] rootDirs, FilenameFilter fileFilter, boolean addDirs,
        boolean recursive, int[] columnConstants) throws java.io.IOException
     {
        m_columnConstants = columnConstants;
        m_fileColumns = new Vector[m_columnConstants.length];

        for (int i = 0; i < m_fileColumns.length; i++)
           m_fileColumns[i] = new Vector();

        if (fileFilter == null)
           fileFilter = new FileFilterAcceptAll();

        for (int i = 0; i < rootDirs.length; i++)
        {
           if (!rootDirs[i].isDirectory())
              throw new java.io.IOException(
              "Directory does not exist: " + rootDirs[i].getCanonicalPath());
           doList(rootDirs[i], fileFilter, addDirs, recursive);
        }

        for (int i = 0; i < m_fileColumns.length; i++)
           m_fileColumns[i].trimToSize();
     }

     private void doList(File rootDir, FilenameFilter fileFilter, boolean addDirs,
        boolean recursive) throws java.io.IOException
     {
        File[] files = rootDir.listFiles(fileFilter);
        for (int i = 0; i < files.length; i++)
        {
           if (files[i].isFile())
              addFileData(files[i]);
           else if (files[i].isDirectory())
           {
              if (addDirs)
                 addFileData(files[i]);
              if (recursive)
                 doList(files[i], fileFilter, addDirs, recursive);
           }
        }
     }

     private void addFileData(File f) throws java.io.IOException
     {
        for (int i = 0; i < m_columnConstants.length; i++)
        {
           switch (m_columnConstants[i])
           {
           case FileList.PATH:
              m_fileColumns[i].add(f.getPath());
              break;
           case FileList.NAME:
              m_fileColumns[i].add(f.getName());
              break;
           case FileList.FULLNAME:
              m_fileColumns[i].add(f.getCanonicalPath());
              break;
           case FileList.MODIFIED:
              StringBuffer buf = new StringBuffer();

              buf = m_df.format(new Date(f.lastModified()),
                 buf, new java.text.FieldPosition(0));

              m_fileColumns[i].add(buf.toString());
              break;
           case FileList.LENGTH:
              m_fileColumns[i].add(new Long(f.length()));
              break;
           case FileList.CONTENTS:
           default:
              m_fileColumns[i].add(null);
           }
        }
     }

     public Vector[] getResults()
     {
        return m_fileColumns;
     }

     private FastDateFormat m_df = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss:SS00");

     private Vector[] m_fileColumns;
     private int[] m_columnConstants;

     // for internal use -- a file filter that always returns true
     class FileFilterAcceptAll implements FilenameFilter
     {
        public FileFilterAcceptAll()
        {
        }

        public boolean accept(File dir, String name)
        {
           return true;
        }
     }
  }

   protected java.util.HashMap m_columnNames;
   
   private PSResultSetMetaData m_metaData;
   private Vector[] m_results;
   private SQLParser m_parser;

   protected PSFileSystemConnection m_conn;
   
   /* (non-Javadoc)
    * @see java.sql.Statement#execute(java.lang.String, int)
    */
   public boolean execute(String sql, int autoGeneratedKeys)
      throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#execute(java.lang.String, int[])
    */
   public boolean execute(String sql, int[] columnIndexes) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])
    */
   public boolean execute(String sql, String[] columnNames) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#executeUpdate(java.lang.String, int)
    */
   public int executeUpdate(String sql, int autoGeneratedKeys)
      throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
    */
   public int executeUpdate(String sql, int[] columnIndexes)
      throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#executeUpdate(java.lang.String, java.lang.String[])
    */
   public int executeUpdate(String sql, String[] columnNames)
      throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#getGeneratedKeys()
    */
   public ResultSet getGeneratedKeys() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#getMoreResults(int)
    */
   public boolean getMoreResults(int current) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   /* (non-Javadoc)
    * @see java.sql.Statement#getResultSetHoldability()
    */
   public int getResultSetHoldability() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public boolean isClosed() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public boolean isPoolable() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public void setPoolable(boolean poolable) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public boolean isWrapperFor(Class<?> iface) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   public <T> T unwrap(Class<T> iface) throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   @Override
   public void closeOnCompletion() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   @Override
   public boolean isCloseOnCompletion() throws SQLException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

}

