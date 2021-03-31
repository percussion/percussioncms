/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * com.percussion.pso.utils PSOSimpleSqlQuery.java 
 *
 */
package com.percussion.pso.utils;
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

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.utils.jdbc.PSConnectionHelper;

/**
 * Prepares and executes a simple SQL Query using the standard Percussion CMS repository 
 * database.  Queries can be provided in two ways:
 * <ol>
 * <li>
 * Using a SQL Statement, that may contain parameter placeholders (usually "?"). 
 * In this case, an array of parameter objects must be supplied. If no parameters
 * are needed, the list must be <code>empty</code>
 * </li>
 * <li>
 * Using an Internal Request. In this case, the request must have already been 
 * created. All parameters are passed as part of the create.
 * </li>
 * </ol> 
 * <p> 
 * The Objects returned from these queries are mapped using the default 
 * JDBC type mapping, which differs based on database driver implementation.
 * For example,  SQL Server may return an <code>Integer</code> object for 
 * a numeric column where Oracle returns a <code>BigDecimal</code>.  
 * Since both of these classes are subclasses of <code>Number</code>, 
 * callers should assume that numeric values are <code>Number</code> objects
 * unless they wish to perform other special handling, using reflection or 
 * similar methods. 
 * <p>
 * This class makes no attempt to handle complex datatypes. Longs, CLOBs, 
 * Blobs, etc. are not handled. 
 * <p>
 * @author DavidBenua
 */
public class PSOSimpleSqlQuery
{
   /**
    * Logger for this class
    */
   private static final Log ms_log = LogFactory.getLog(PSOSimpleSqlQuery.class);

   /**
    * Database pool from Percussion CMS Server
    */
   //static PSDatabasePool ms_pool = PSDatabasePool.getDatabasePool();
   
   
 
   /**
    * static methods only, never constructed.   
    */
   private PSOSimpleSqlQuery()
   {
      
   }
   
   /**
    * Perform a query with multiple rows. 
    * @param query the SQL statement. 
    * @param params a list of Objects that represent the placeholders in the SQL.  
    * Never <code>null</code>. May be <code>empty</code>. 
    * @return a List of Object[] representing the rows of  the result set.  
    * @throws SQLException
    * @throws NamingException 
    */
   public static List<Object[]> doQuery(String query, List<? extends Object> params) throws SQLException, NamingException 
   {
      List<Object[]> resultList = new ArrayList<Object[]>(); 
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      Object[] resultRow = null;
      try
      {
         conn = PSConnectionHelper.getDbConnection();  
         stmt = conn.prepareStatement(query); 
         setInternalParams(stmt, params);
         rs = stmt.executeQuery(); 
         boolean valid = rs.next(); 
         while(valid)
         {
            resultRow = buildRowResult(rs);
            resultList.add(resultRow); 
            valid = rs.next(); 
         }                   
      }
      catch (SQLException ex)
      {
         ms_log.error(PSOSimpleSqlQuery.class.getName(), ex);
         throw ex;  
      } catch (NamingException ex2)
      {
         ms_log.error("Unexpected Exception " + ex2,ex2);
         throw ex2;
      }
      finally
      {
         cleanupResult(rs,stmt,conn); 
      } 
      return resultList;
   }
   
   /**
    * Perform a query based on an Internal Request. The query associated with 
    * the request is processed and returned as a list of Object[]; 
    * @param ir the internal request.
    * @return a List of Object[] representing the rows of the result set. 
    * @throws PSInternalRequestCallException
    * @throws SQLException
    */
   public static List<Object[]> doQuery(IPSInternalRequest ir) 
    throws PSInternalRequestCallException, SQLException
   {
      List<Object[]> resultList = new ArrayList<Object[]>(); 
      ResultSet rs = null;
      Object[] resultRow = null;
      try
      {
         rs = ir.getResultSet(); 
         boolean valid = rs.next(); 
         while(valid)
         {
            resultRow = buildRowResult(rs);
            resultList.add(resultRow); 
            valid = rs.next(); 
         }                   
      }
      catch (SQLException ex)
      {
         ms_log.error(PSOSimpleSqlQuery.class.getName(), ex);
         throw (SQLException)ex.fillInStackTrace();  
      }
      catch (PSInternalRequestCallException ex2)
      {
         ms_log.error(PSOSimpleSqlQuery.class.getName(), ex2);
         throw (PSInternalRequestCallException)ex2.fillInStackTrace(); 
      }
      finally
      {
         ir.cleanUp();  
      } 
      return resultList;
   }
   
   /**
    * Perform a query that returns only one row.  If the query provided returns only
    * one row, only the first row will be examined. 
    * @param query the SQL query. 
    * @param params a List of Objects that represent the parameters. 
    * @return an Object[] that represents the first row returned from the query. May be 
    * <code>null</code>. 
    * @throws SQLException
    * @throws NamingException 
    */
   public static Object[] doSingleRowQuery(String query, List<? extends Object> params) throws SQLException, NamingException
   {
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      Object[] resultRow = null;
      try
      {
         conn = PSConnectionHelper.getDbConnection();  
         stmt = conn.prepareStatement(query); 
         setInternalParams(stmt,params);
         rs = stmt.executeQuery(); 
         boolean valid = rs.next(); 
         if(valid)
         {
            resultRow = buildRowResult(rs);
         }                   
      }
      catch (SQLException ex)
      {
         ms_log.error(PSOSimpleSqlQuery.class.getName(), ex);
         throw ex;  
      } catch (NamingException ex2)
      {
         ms_log.error("Unexpected Exception " + ex2,ex2);
         throw ex2;
      }
      finally
      {
         cleanupResult(rs,stmt,conn); 
      } 
      return resultRow;    
   }

   /**
    * Perform an Internal Request query that returns only one row.
    * If the query provided returns only one row, only the first row 
    * will be examined.  
    * @param ir the Internal Request.
    * @return an Object[] representing the columns returned by the query. 
    * May be <code>null</code> 
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
         if(valid)
         {
            resultRow = buildRowResult(rs);
         }                   
      }
      catch (SQLException ex)
      {
         ms_log.error(PSOSimpleSqlQuery.class.getName(), ex);
         throw (SQLException)ex.fillInStackTrace();  
      }
      catch (PSInternalRequestCallException ex2)
      {
         ms_log.error(PSOSimpleSqlQuery.class.getName(), ex2);
         throw (PSInternalRequestCallException)ex2.fillInStackTrace(); 
      }      
      finally
      {
         ir.cleanUp();  
      } 
      return resultRow;    
   }
   
   /**
    * Clean up after a query. Closes the result sete, statement and connection, 
    * handles any errors and releases the connection back to the pool.  Intended
    * to be called in the <code>finally</code> clause. 
    * @param rs the result set. May be <code>null</code>
    * @param stmt the statement. May be <code>null</code>
    * @param conn the connection. May be <code>null</code>
    * @throws SQLException if any errors occur. 
    */
   private static void cleanupResult(ResultSet rs, Statement stmt, Connection conn) 
   throws SQLException
   {
      if(rs != null)
      {
         rs.close(); 
      }
      if(stmt != null)
      {
         stmt.close(); 
      }
      if(conn != null)
      {
         try
         {
            conn.close();
         } catch (SQLException e)
         {
            ms_log.error(PSOSimpleSqlQuery.class.getName(), e);
         } 
      }
      
   }

   /**
    * Set the parameter list on a prepared statement. 
    * The mapping is done using the default JDBC Type/Object mapping. 
    * Note that no checking
    * is done to assure that the required number of parameters are supplied.  
    * @param stmt the prepared statement. Must not be <code>null</code>
    * @param params a List of Object suitable for the parameters. Must not be
    * <code>null</code> but may be <code>empty</code>
    * @throws SQLException
    */
   private static void setInternalParams(PreparedStatement stmt, List<? extends Object> params) 
      throws SQLException
   {
      Iterator<? extends Object> parm = params.iterator();
      int i = 1; 
      while(parm.hasNext())
      {
         stmt.setObject(i, parm.next()); 
         i++; 
      }
   }
   
   /**
    * build a result row as an Object[]. The size of the row is determined
    * from the result set metadata.  The object types are determined by the 
    * default JDBC type / object mapping. 
    * @param rs the result set. Must not be <code>null</code>
    * @return the array of objects representing the current row of the result
    * set. 
    * @throws SQLException
    */
   private static Object[] buildRowResult(ResultSet rs) 
      throws SQLException
   {
      ResultSetMetaData meta = rs.getMetaData();
      int colcount = meta.getColumnCount(); 
      Object[] resultRow = new Object[colcount]; 
      for(int i=0; i < colcount; i++)
      {
         if(ms_log.isDebugEnabled())
         { 
            int j = i+1; 
            Object[] msg = new Object[4];
            msg[0] = new Integer(j);
            msg[1] = meta.getColumnName(j);
            msg[2] = meta.getColumnTypeName(j);
            msg[3] = meta.getColumnClassName(j);
            ms_log.debug(MessageFormat.format(LOG_MSG_ROW, msg)); 
         }
         resultRow[i] = rs.getObject(i+1); 
         //ms_log.debug("Classname " + resultRow[i].getClass().getName()); 
         if(rs.wasNull())
         {
            ms_log.debug("column is null"); 
            resultRow[i] = null;
             
         }
      }
      return resultRow; 
   }
   
   private static final String LOG_MSG_ROW = "Column {0} named {1} typename {2} classname {3}";
   


}
