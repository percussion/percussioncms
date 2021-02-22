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
package com.percussion.data;

import com.percussion.error.PSErrorException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.SecureStringUtils;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestStatistics;
import com.percussion.util.IOTools;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSDataTypeConverter;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.util.PSSQLStatement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oracle.jdbc.OracleTypes;

/**
 * The PSOracleUpdateStatement class is the super-class for Oracle-specific
 * update logic (currently updates containing LOBs).  The statement is
 * built from the PSUpdatePipe definitions. When a request is received,
 * the statement can built from the request data and executed with it.
 *
 * Note:  This class is extended from PSStatement directly, and not from
 * PSUpdateStatement, as it needs to be a statement which is built at
 * execution time, and not built statically and prepared, as PSUpdateStatement
 * does.
 * 
 * @see        PSOracleUpdateBuilder
 *
 */
public class PSOracleUpdateStatement extends PSUpdateStatement
{
   /**
    * Construct a LOB based update or insert statement which can be executed
    * as part of the update execution plan. This constructor must construct a
    * statement which can be built on the fly, so that oracle-specific
    * blob initialization functions can be added to the statement as 
    * needed at runtime.  The lob update will be generated based on
    * the return from the initial insert (a callable statement returning
    * ROWID) or the initial update(s) (updates executed from a set of rowids
    * returned from a query of the keys) and the value of each LOB columns 
    * defined for this update.
    *   
    * @param   connKey          the connection key to use to get the db conn
    *
    * @param   updateBlocks     the statement blocks for the initial update
    *                           or insert
    *
    * @param   lobUpdateBlocks  the statement blocks for LOB updates based on
    *                           oracle rowid, never <code>null</code>
    *
    * @param   rowIdQueryBlocks the statement blocks for rowid retrieval
    *                           (TYPE_UPDATE only, ignored otherwise)
    *
    * @param   type             the TYPE_xxx statement type,but never TYPE_DELETE,
    *                           as this is handled by a PSUpdateStatement
    *
    * @throws PSDataExtractionException   If a data extraction exception occurs.
    *
    * @throws IllegalArgumentException If any parameter is invalid.
    */
   public PSOracleUpdateStatement(
      int connKey, 
      IPSStatementBlock[] updateBlocks,
      IPSStatementBlock[] lobUpdateBlocks,
      IPSStatementBlock[] rowIdQueryBlocks,
      int type)
      throws PSDataExtractionException
   {
      // Use the statement-on-the-fly constructor (false for buildSql)
      super(connKey, updateBlocks, false, type);

      if (lobUpdateBlocks == null)
         throw new IllegalArgumentException("Lob update blocks must be supplied");

      if (type != TYPE_INSERT && type != TYPE_UPDATE)
      {
         throw new IllegalArgumentException(
            "Update type must be either TYPE_UPDATE or TYPE_INSERT for this statement.");
      }

      m_lobQueryStatement
         = new PSQueryStatement(connKey, lobUpdateBlocks);
      if (rowIdQueryBlocks != null) // don't have this for insert statement!
         m_rowIdQueryStatement
            = new PSQueryStatement(connKey, rowIdQueryBlocks);
      else if (type == TYPE_UPDATE)
         throw new IllegalArgumentException(
            "rowIdQueryBlocks must be supplied for statements of TYPE_UPDATE");
   }


   // see base class
   public void setIteratorNode(String nodeName)
   {
      // need to include fields from the query statements so they are fixed up
      // appropriately by the transaction set when it initializes.
      super.setIteratorNode(nodeName);
      m_lobQueryStatement.setIteratorNode(nodeName);
      if (m_rowIdQueryStatement != null)
         m_rowIdQueryStatement.setIteratorNode(nodeName);
   }

   // see base class
   public List getReplacementValueExtractors()
   {
      // need to include fields from the query statements so they are fixed up
      // appropriately by the transaction set when it initializes.
      ArrayList retList = new ArrayList();

      retList.addAll(super.getReplacementValueExtractors());
      retList.addAll(m_lobQueryStatement.getReplacementValueExtractors());
      if (m_rowIdQueryStatement != null)
         retList.addAll(m_rowIdQueryStatement.getReplacementValueExtractors());

      return retList;
   }


   /* ************  IPSExecutionStep Interface Implementation ************ */
      
   /**
    * Execute the data modification statement as a step in the execution
    * plan. A count of the rows affected will be tallied as updates (or
    * an insert) are made. The resulting count will be added to the
    * execution data.  Any Oracle JDBC extensions for LOB routines will
    * be utilized by this method.
    *
    * <p>Algorithm for insert:</p>
    * <p>1) Execute insert (callable statement) which initializes all LOB
    * columns and inserts all non-LOB values and returns a ROWID</p>
    * <p>2) Execute LOB update, this is a query based on the ROWID which can
    * then be used to directly update the LOB values</p>
    * <p>Algorithm for update:</p>
    * <p>Execute initial ROWID query</p>
    * <p>&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp; For each ROWID:</p>
    * <p>&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; perform
    * initial update with non-LOB values and LOB initializers</p>
    * <p>&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; perform
    * LOB update, this is a query based on the ROWID which can be used to
    * directly update the LOB values</p>
    * @param   data     the execution data associated with this plan
    *
    * @throws   SQLException
    *                     if a SQL error occurs
    *
    * @throws   PSDataExtractionException
    *                     if a data extraction error occurs
    *
    * @throws   PSErrorException
    *                     if an error exception occurs
    */
   public void execute(PSExecutionData data)
      throws SQLException,   PSDataExtractionException,   PSErrorException
   {
      /* execute the statement through the db pool
       * and create a result set with the update statistics
       */

      PreparedStatement stmt   = null;
      Connection conn = null;
      PSRequest req = data.getRequest();
      PSRequestStatistics stats = req.getStatistics();

      //Sets the action(insert/update/delete) on table change data object
      PSTableChangeData tableChangeData = data.getTableChangeData();
      if (tableChangeData != null)
         initTableChangeAction(tableChangeData);
      
      try
      {
         try
         {
            conn = data.getDbConnection(m_connKey);
         }
         catch (PSIllegalArgumentException ie)
         {
            throw new SQLException(ie.getMessage());
         }

         int rowCount = 0;
         
         if (m_statementType == TYPE_INSERT)
         {
            rowCount = executeLobInsert(data, conn);
         } else
         {
            rowCount = executeLobUpdate(data, conn);
         }

         if(tableChangeData != null && rowCount > 0)
            tableChangeData.notifyListeners();
         
         // store the statistics in the appropriate place based upon type
         switch (m_statementType) {
            case TYPE_UPDATE:
               stats.incrementRowsUpdated(rowCount);
               traceRowAction(req, "traceResourceHandler_updateUpdated",
                                 rowCount);
               break;

            case TYPE_INSERT:
               stats.incrementRowsInserted(rowCount);
               traceRowAction(req, "traceResourceHandler_updateInserted",
                                 rowCount);
               break;
         }
      }
      catch (SQLException e) {
         joinSqlExceptions(conn, stmt, e);
         throw e;
      } finally {
         if (stmt != null) {
            try { stmt.close(); }
            catch (Exception e) { /* we're done anyway */ }
         }
      }
   }

   /**
    * Update a blob column from resultset rs at offset index.
    *
    * @param rs   The resultset from querying for Lobs.
    *    Never <code>null</code>.
    *
    * @param index   The Column index.
    *
    * @param col  The associated statement column for this Blob update.
    *    Never <code>null</code>.
    *
    * @param data The execution data.
    * 
    * @throws  SQLException   If a SQL exception occurs during processing.
    * 
    * @throws  PSDataExtractionException   If any data transfer-related 
    *          error occurs.
    *
    * @throws  IllegalArgumentException if any argument is invalid.
    */
   public void setBlob(ResultSet rs, int index, PSStatementColumn col,
      PSExecutionData data)
      throws SQLException, PSDataExtractionException
   {                           
      if (rs == null)
         throw new IllegalArgumentException("Result set must be specified.");

      if (col == null)
         throw new IllegalArgumentException("Statement column must be specified.");

      if (col.isNull(data))
         return;
         
      Object ob = col.getPreparedValue(data, null);
      if (ob == null)
         return;
         
      oracle.sql.BLOB b = (oracle.sql.BLOB) rs.getBlob(index);

      // now set the sucker!      
      try {
         OutputStream os = b.getBinaryOutputStream();

         if (ob instanceof URL)
         {
            ob = new File(((URL) ob).getFile());
         }
         
         if (ob instanceof String)
         {
            byte[] binData;

            if (PSDataTypeConverter.isHexData((String) ob))
               binData = PSDataTypeConverter.getBinaryFromHex((String) ob);
            else
               binData = PSStatementColumn.getBinaryFromBase64((String) ob);

            os.write(binData);
            os.close();
         } else if (ob instanceof byte[])
         {
            os.write((byte[]) ob);
            os.close();
         } else if (ob instanceof File)
         {
            FileInputStream fis = new FileInputStream((File) ob);
            IOTools.copyStream(fis, os);
            os.close();
            fis.close();
         }
         else
         {
            Object[] obs = {ob.getClass().getName(), "Blob"};
            throw new PSDataExtractionException(
               IPSDataErrors.DATA_INVALID_CONVERSION, obs);
         }
      } catch (IOException ioE)
      {
         Object[] obs = {ob.getClass().getName(), "Blob", ioE.toString()};
         // throw a data extraction exception
         throw new PSDataExtractionException(
            IPSDataErrors.DATA_CANNOT_CONVERT_WITH_REASON, obs);
      }
   }
   
   /**
    * Update a clob column from resultset rs at offset index.
    *
    * @param rs   The resultset from querying for Lobs.
    *    Never <code>null</code>.
    *
    * @param index   The Column index.
    *
    * @param col  The associated statement column for this Clob update.
    *    Never <code>null</code>.
    *
    * @param data The execution data.
    * 
    * @throws  SQLException   If a SQL exception occurs during processing.
    * 
    * @throws  PSDataExtractionException   If any data transfer-related 
    *    error occurs.
    *
    * @throws  IllegalArgumentException if any argument is invalid.
    */
   public void setClob(ResultSet rs, int index, PSStatementColumn col,
      PSExecutionData data)
      throws SQLException, PSDataExtractionException
   {
      if (rs == null)
         throw new IllegalArgumentException("Result set must be specified.");

      if (col == null)
         throw new IllegalArgumentException("Statement column must be specified.");

      if (col.isNull(data))
         return;

      Object ob = col.getPreparedValue(data, null);
      
      if (ob == null)
         return;

      oracle.sql.CLOB c = (oracle.sql.CLOB) rs.getClob(index);
      
      Writer w = null;
      Reader r = null;

      // now set the sucker!
      try {
         w = c.getCharacterOutputStream();
         if (ob instanceof URL)
         {
            ob = new File(((URL) ob).getFile());
         }
         
         String charSetEncoding = null;
         if (ob instanceof PSPurgableTempFile)
         {
            PSPurgableTempFile f = (PSPurgableTempFile) ob;
            charSetEncoding = f.getCharacterSetEncoding();
         }
         
         if (charSetEncoding == null || charSetEncoding.length() == 0)
            charSetEncoding = data.getRequest().getFileCharacterSet();
         
         if (ob instanceof String)
         {
            w.write((String) ob);
            return;
         } else if (ob instanceof byte[])
         {
            String str;
            if (charSetEncoding == null || charSetEncoding.length() == 0)
               str = new String((byte[]) ob);
            else
               str = new String((byte[]) ob, charSetEncoding);
            w.write(str);
         } else if (ob instanceof File)
         {
            if ((charSetEncoding == null) || (charSetEncoding.length() == 0))
               r = new FileReader((File) ob);
            else
            {   
               r = new InputStreamReader( new FileInputStream((File) ob),
                  PSCharSets.getJavaName(charSetEncoding));
            }
               
            IOTools.writeStream(r, w);
         } else // if (ob instanceof Number)
         {
            // Hope this suffices for all other cases, including number.
            w.write(ob.toString());
         }// else  {     }
      } catch (IOException ioE)
      {
         // throw a data extraction exception
         Object[] obs = {ob.getClass().getName(), "Clob", ioE.toString()};

         throw new PSDataExtractionException(
            IPSDataErrors.DATA_CANNOT_CONVERT_WITH_REASON, obs);
      } finally
      {
         try {
            if (w != null)
               w.close();
            if (r != null)
               r.close();
         } catch (IOException e) { ;/*ignore*/ }
      }
   }
   
   /**
    * Execute an Oracle-specific, LOB-based insert.  This will be a
    * CallableStatement which will execute the insert with lob initialization
    * calls and return the ROWID so that the row can be re-fetched to do 
    * LOB updates.
    *
    * @param   data     The execution data associated with this plan.
    *
    * @param   conn     The database pool connection, never <code>null</code>.
    *
    * @throws   SQLException
    *                     if a SQL error occurs
    *
    * @throws   PSDataExtractionException
    *                   if any data retrieval related error occurs
    *
    * @throws   IllegalArgumentException if any argument is invalid
    *
    * @return  The number of rows inserted, this will always be <code>1</code>
    *          when the insert is successful.
    */
   private int executeLobInsert(PSExecutionData data,
         Connection conn)
      throws SQLException, PSDataExtractionException
   {
      if (conn == null)
         throw new IllegalArgumentException("Connection must be specified.");

      boolean autoOn = false;
      boolean succeeded = false;
            
      if (conn.getAutoCommit())
      {
         autoOn = true;
         conn.setAutoCommit(false);
      }

      try {
         String sqlString = buildSqlString(data, true);

         CallableStatement stmt = conn.prepareCall(sqlString);

         stmt.clearParameters();
         

         int bindStart = 1;
         
         for (int i = 0; i < m_blocks.length; i++) {
            bindStart = m_blocks[i].setColumnData(data, stmt, bindStart);
         }

         stmt.registerOutParameter(bindStart, OracleTypes.ROWID);

         stmt.executeQuery();

         String rowid = stmt.getString(bindStart);

         stmt.close();

         /* Now do the query for update... 
            Since this is not a "standard" query, we will not
            be calling its execute() method directly, but only
            using it for statement generation */
         String query = m_lobQueryStatement.buildSqlString(data) + 
            "'" + rowid + "'";

         Statement s = PSSQLStatement.getStatement(conn);
         ResultSet rs = s.executeQuery( query );

         // Now Update the dang lobs!
         if (!rs.next())
         {
            throw new SQLException(
               "Unable to retrieve inserted column for LOB update");
         }

         int colNum = 1;
         /* Traverse LOB columns and update the LOBs by locator */
         for (int i = 0; i < m_blocks.length; i++)
         {
            List lobCols = m_blocks[i].getLobStatementColumns();
            if (lobCols.size() > 0)
            {
               for (int j = 0; j < lobCols.size(); j++)
               {
                  PSStatementColumn col = (PSStatementColumn) lobCols.get(j);

                  if (col.getType() == java.sql.Types.BLOB)  // Blob
                     setBlob(rs, colNum, col, data );
                  else  // Clob
                     setClob(rs, colNum, col, data );
                  colNum++;
               }   
            }
         }
         
         rs.close();
         s.close();
         
         succeeded = true;
         conn.commit();         
      } finally {
         if (!succeeded)  // If we didn't flag succeed, we failed!
            conn.rollback();

         /* Revert the change we made to the autocommit behavior of this 
            connection */
         if (autoOn)
            conn.setAutoCommit(true);
      }

      return succeeded ? 1 : 0;
   }

   
   /**
    * Execute an Oracle-specific, LOB-based update.  This will be a
    * Query which will retrieve all ROWIDs based on the supplied keys, 
    * followed by an update of each row, followed by a query of each row
    * to update the LOB columns.
    *
    * @param   data     the execution data associated with this plan
    *
    * @param   conn     the database pool connection
    *
    * @throws   SQLException
    *                     if a SQL error occurs
    * @throws   PSDataExtractionException
    *                   if any data-transfer related error occurs
    * @throws  PSErrorException
    *                   if an error exception is thrown during processing
    */
   private int executeLobUpdate(PSExecutionData data,
         Connection conn)
      throws SQLException, PSDataExtractionException, PSErrorException
   {
      boolean autoOn = false;
      boolean succeeded = false;
      int count = 0;
            
      if (conn.getAutoCommit())
      {
         autoOn = true;
         conn.setAutoCommit(false);
      }

      try {
         /* Execute the statement to retrieve ROWIDs */
         m_rowIdQueryStatement.execute(data);

         /* Get the result set from the stack and retrieve the rowids*/
         ResultSet rs = (ResultSet) data.getResultSetStack().pop();
         
         ArrayList rowIds = new ArrayList();
         
         while(rs.next())
         {
            rowIds.add(rs.getString(1));
         }
         rs.close();
                  
         /* Now close the statement.  The query adds it to 
            the end of a list in the executiondata */
         List stmts = data.getPreparedStatements();
         if ((stmts != null) && (stmts.size() > 0))
         {
            PreparedStatement stmt = 
               (PreparedStatement) stmts.get(stmts.size() - 1);
            stmt.close();
         }
                  
         if (rowIds.size() == 0)
            return 0;     // Nothing to update!

         String sqlString = buildSqlString(data, true);

         PreparedStatement stmt =
            PSPreparedStatement.getPreparedStatement(conn, sqlString);

         int bindStart = 1;
      
         for (int i = 0; i < m_blocks.length; i++) {
            bindStart = m_blocks[i].setColumnData(data, stmt, bindStart);
         }

         /* Now traverse the rowid list, doing the updates w/ initializers */
         for (Iterator i = rowIds.iterator(); i.hasNext();)
         {
            String rowid = (String) i.next();

            stmt.setString(bindStart, rowid);

            stmt.executeUpdate();
         }
         
         stmt.close();
         

         /* Now do the query for update... */
         String queryBegin = m_lobQueryStatement.buildSqlString(data); 

         for (Iterator i = rowIds.iterator(); i.hasNext();)
         {
            String rowid = (String) i.next();

            String query = queryBegin + "'" + SecureStringUtils.sanitizeStringForSQLStatement(rowid) + "'";

            Statement s = PSSQLStatement.getStatement(conn);
            rs = s.executeQuery( query );

            // Now Update the dang lobs!
            if (!rs.next())
            {
               throw new SQLException(
                  "Unable to retrieve updated column for LOB update");
            }

            int colNum = 1;
            /* Traverse LOB columns and update the LOBs by locator */
            for (int blockCnt = 0; blockCnt < m_blocks.length; blockCnt++)
            {
               List lobCols = m_blocks[blockCnt].getLobStatementColumns();
               if (lobCols.size() > 0)
               {
                  for (int j = 0; j < lobCols.size(); j++)
                  {
                     PSStatementColumn col = (PSStatementColumn) lobCols.get(j);

                     if (col.getType() == java.sql.Types.BLOB)  // Blob
                        setBlob(rs, colNum, col, data );
                     else  // Clob
                        setClob(rs, colNum, col, data );
                     colNum++;
                  }   
               }
            }
            rs.close();
            s.close();
         }
         

         count = rowIds.size();

         succeeded = true;
         conn.commit();
      } finally {
         if (!succeeded)  // If we didn't flag succeed, we failed!
            conn.rollback();

         /* Revert the change we made to the autocommit behavior of this 
            connection */
         if (autoOn)
            conn.setAutoCommit(true);
      }
      
      return count;
   }


   /**
    *  The query statement required to do Oracle-specific Blob and Clob
    *  updates.  This will be created when this class is constructed.
    */
   private   PSQueryStatement m_lobQueryStatement;


   /**
    *  The query statement required to do Oracle-specific retrieval of
    *  the rowids associated with this statement, this will be used to
    *  properly identify the row which needs to be queried for Blob and Clob
    *  updates.  This will be created when this class is constructed
    *  (for statements of TYPE_UPDATE only, otherwise this will remain 
    *  <code>null</code>).
    */
   private   PSQueryStatement m_rowIdQueryStatement;

}


