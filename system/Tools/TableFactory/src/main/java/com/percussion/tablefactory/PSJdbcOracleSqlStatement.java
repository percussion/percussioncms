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

package com.percussion.tablefactory;

import com.percussion.util.PSCollection;
import com.percussion.util.PSDataTypeConverter;
import com.percussion.util.PSOutputEscaping;
import com.percussion.util.PSPreparedStatement;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oracle.jdbc.OracleResultSet;
import oracle.sql.BLOB;
import oracle.sql.CLOB;

/**
 * Wraps Prepared Sql Statement for Oracle and contains bound parameters.
 * Supports Blob and Clob data inserts and updates.
 */
public class PSJdbcOracleSqlStatement extends PSJdbcPreparedSqlStatement
{
  /**
    * Constructs Sql statement to execute, including the parameter values.
    *
    * @param statementType the type of statement that this object represents
    * @param statement The statement to execute, may have bound parameter
    * tokens ("?").  May not be <code>null</code> or empty.
    * @param values A list of {@link PSJdbcStatementColumn} objects, may be
    * <code>null</code> but not empty.
    * @param selLobString the select statement for getting a lock on clob/lob,
    * contains "FOR UPDATE" clause, never <code>null</code> or empty
    * @param keyValues A list of {@link PSJdbcStatementColumn} objects, may be
    * <code>null</code> but not empty, contains the values of key columns for
    * selLobString.
    * @param lobTypes an array of Integer objects indicating the type of
    * lob object, may not be <code>null</code>
    * @param lobValues an array of LOB objects, may not be <code>null</code>
    * @param lobValuesEncoding an array of encoding types corresponding to
    * each LOB data contained in lobValues, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if statement is <code>null</code> or
    * empty, or if values does not contain at least one object, or does not
    * contain object of the correct type, or if selLobString is
    * <code>null</code> or empty, or if keyValues does not contain
    * at least one object, or does not contain object of the correct type,
    * or if lobTypes is <code>null<code> or if lobValues is <code>null<code>
    * or if lobValuesEncoding is <code>null<code>
    * or if the statementType is invalid or if the size of lobTypes,
    * lobValues and lobValuesEncoding are not equal.
    */
   public PSJdbcOracleSqlStatement(int statementType, String statement,
      PSCollection values, String selLobString, PSCollection keyValues,
      List lobTypes, List lobValues, List lobValuesEncoding)
   {
      super(statement, values);

      if (!validStatementType(statementType))
         throw new IllegalArgumentException("invalid statement type");

      m_statementType = statementType;

      if ((selLobString == null) || (selLobString.trim().length() == 0))
         throw new IllegalArgumentException(
            "selLobString may not be null or empty");
      m_selLobString = selLobString;

      if (keyValues != null)
      {
         if (keyValues.isEmpty())
            throw new IllegalArgumentException(
               "keyValues must contain at least one object");

         if (!keyValues.getMemberClassType().getName().equals(
            PSJdbcStatementColumn.class.getName()))
         {
            throw new IllegalArgumentException(
               "keyValues must contain instances of PSJdbcStatementColumn");
         }
      }
      m_keyValues = keyValues;

      if (lobTypes == null)
         throw new IllegalArgumentException("lobTypes may not be null");
      m_lobTypes = lobTypes;

      if (lobValues == null)
         throw new IllegalArgumentException("lobTypes may not be null");
      m_lobValues = lobValues;

      if (lobValuesEncoding == null)
         throw new IllegalArgumentException("lobValuesEncoding may not be null");
      m_lobValuesEncoding = lobValuesEncoding;

      if ((lobTypes.size() != lobValues.size()) ||
         (lobTypes.size() != lobValuesEncoding.size()))
         throw new IllegalArgumentException(
            "size of lobTypes, lobValues and lobValuesEncoding should be equal");
   }

   /**
    * executes an Insert statement for oracle database
    * @param conn the connection to use for executing this insert statement,
    * never <code>null</code>
    * @return the number of rows inserted by this insert statement
    * @throws SQLException if any error occurs while executing the insert
    * statement
    */
   private int executeInsert(Connection conn) throws SQLException
   {
      int updateCount = 0;
      CallableStatement cstmt = null;
      ResultSet insRs = null;

      String callStmtString = "begin ";
      callStmtString += m_statement;
      callStmtString += " RETURNING ROWID INTO ? ; end; ";

      PSJdbcExecutionStepLog stepLogData = getStepLogData();
      stepLogData.setConnectionString(conn);
      stepLogData.setSqlQuery(m_statement);

      try
      {
         cstmt = conn.prepareCall(callStmtString);
         int outParam = 1;
         if (m_values != null)
         {
            bindValues(cstmt, m_values);
            outParam = m_values.size() + 1;
         }
         cstmt.registerOutParameter(outParam, Types.VARCHAR);
         updateCount = cstmt.executeUpdate();
         String rowid = cstmt.getString(outParam);
         m_rowID = rowid;

         stepLogData.setSuccess(true);
         stepLogData.setUpdateCount(updateCount);
         if (updateCount == 0)
         {
            stepLogData.setErrorMessage(
               PSJdbcExecutionStepLog.NO_ROWS_UPDATED_MSG);
         }
         else
         {
            stepLogData.setErrorMessage(PSJdbcExecutionStepLog.SUCCESS_MSG);
         }
      }
      catch (SQLException e)
      {
         updateCount = 0;
         stepLogData.setSuccess(false);
         stepLogData.setUpdateCount(0);
         String errMsg = PSJdbcTableFactoryException
            .formatSqlException(callStmtString, e);
         stepLogData.setErrorMessage(errMsg);
         handleSqlException(e);
      }
      finally
      {
         try
         {
            if (insRs != null)
               insRs.close();
            if (cstmt != null)
               cstmt.close();
         }
         catch (SQLException ex)
         {
            /* no-op*/
         }
      }
      return updateCount;
   }

   /**
    * Executes the step against the provided Connection.
    *
    * @param conn The connection, may not be <code>null</code>.
    *
    * @return numbers of rows in the database which were updated
    * by execution of this step. If sql statement execution
    * returns false then returns the update count else returns 0.
    *
    * @throws IllegalArgumentException if conn is <code>null</code>.
    * @throws SQLException if any errors occur.
    */
   public int execute(Connection conn) throws SQLException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      // Here the Connection is converted into an instance of
      // <code>oracle.jdbc.OracleConnection</code> class.
      // In <code>createTempBLOB</code> and <code>createTempCLOB</code> methods,
      // when a call to create a temporary BLOB/CLOB is made, Oracle driver
      // casts the connection to <code>oracle.jdbc.OracleConnection</code>
      // class. So if the connection is not an instance of this class,
      // <code>ClassCastException</code> is thrown.
      // The conversion is done here to ensure that <code>ClassCastException</code>
      // is never thrown if the Oracle driver does class casting when called
      // from any method of this class.      
      conn = conn.getMetaData().getConnection();
      
      int updateCount = 0;
      switch (m_statementType)
      {
         case ORACLE_DUMMY_UPDATE:
         case ORACLE_UPDATE:
            updateCount = super.execute(conn);
            break;

         case ORACLE_INSERT:
            updateCount = executeInsert(conn);
            break;

         default:
            return 0;
      }

      if (!(updateCount > 0))
         return 0;

      try
      {
         insertLOBs(conn);
      }
      catch (IOException ioe)
      {
         PSJdbcTableFactory.logMessage(ioe.getMessage());
         throw new SQLException(ioe.getLocalizedMessage());
      }
      return updateCount;
   }

   /**
    * inserts LOB data in oracle database
    *
    * @param conn the connection to use to insert LOB data in database, never
    * <code>null</code>
    *
    * @throws SQLException if any error occurs while executing the insert
    * statement
    * @throws IOException if any error occurs while writing the LOB data to the
    * temporary LOB created in the <code>createTempBLOB</code> and
    * <code>createTempBLOB</code> methods.
    */
   private void insertLOBs(Connection conn) throws SQLException, IOException
   {
      PreparedStatement lobStmt = null;
      ResultSet lobRs = null;
      boolean commit = conn.getAutoCommit();
      List tempClobsList = new ArrayList();
      List tempBlobsList = new ArrayList();
      try
      {
         lobStmt =
            PSPreparedStatement.getPreparedStatement(
               conn,
               m_selLobString,
               ResultSet.TYPE_FORWARD_ONLY,
               ResultSet.CONCUR_UPDATABLE);
         switch (m_statementType)
         {
            case ORACLE_UPDATE:
            case ORACLE_DUMMY_UPDATE:
               if (m_keyValues != null)
                  bindValues(lobStmt, m_keyValues);
               break;

            case ORACLE_INSERT:
               lobStmt.setString(1, m_rowID);
               break;

            default:
               break;
         }

         // if autocommit is on set it to off or else this query will
         // always raise exception
         if (commit)
            conn.setAutoCommit(false);
         lobRs = lobStmt.executeQuery();
         Iterator lobTypesIterator = m_lobTypes.iterator();
         Iterator lobValuesIterator = m_lobValues.iterator();
         Iterator lobValuesEncodingIterator = m_lobValuesEncoding.iterator();
         int counter = 1;
         if (lobRs == null)
            return;
         if (!lobRs.next())
            return;

         int size = m_lobTypes.size();
         while (counter <= size)
         {
            int coltype = ((Integer)lobTypesIterator.next()).intValue();
            String colValue = (String)lobValuesIterator.next();
            int colValueEncoding = PSJdbcColumnData.ENC_TEXT;
            if (lobValuesEncodingIterator != null)
               colValueEncoding = ((Integer)
                  lobValuesEncodingIterator.next()).intValue();

            byte[] binData = null;
            switch (coltype)
            {
               case Types.CLOB :
               case Types.LONGVARCHAR :
                  if (colValue != null)
                  {
                     switch (colValueEncoding)
                     {
                        case PSJdbcColumnData.ENC_ESCAPED :
                           colValue = PSOutputEscaping.unEscape(colValue);
                           break;

                        case PSJdbcColumnData.ENC_BASE64 :
                           try
                           {
                              binData =
                                 PSDataTypeConverter.getBinaryFromString(
                                    colValue);
                           }
                           catch (IOException ioe)
                           {
                              binData = null;
                           }
                           if (binData != null)
                              colValue = new String(binData);
                           break;

                        default :
                           break;
                     }
                     
                     CLOB clob = createTempCLOB(conn, colValue);
                     tempClobsList.add(clob);
                     ((OracleResultSet)lobRs).updateCLOB(counter, clob);
                  }
                  else
                  {
                     lobRs.updateNull(counter);
                  }
                  break;

               case Types.BLOB :
               case Types.LONGVARBINARY :
                  if (colValue != null)
                  {
                     if (colValueEncoding == PSJdbcColumnData.ENC_ESCAPED)
                     {
                        // un-escape
                        colValue = PSOutputEscaping.unEscape(colValue);
                     }

                     // now treat all source as possible binary representation
                     // to be consistent with core non-Oracle behavior
                     try
                     {
                        binData =
                           PSDataTypeConverter.getBinaryFromString(
                              colValue);
                     }
                     catch (IOException ioe)
                     {
                        binData = null;
                     }
                  }
                  
                  if (binData != null)
                  {
                     BLOB blob = createTempBLOB(conn, binData);
                     tempBlobsList.add(blob);
                     ((OracleResultSet)lobRs).updateBLOB(counter, blob);
                  }
                  else
                  {
                     lobRs.updateNull(counter);
                  }
                  break;
            }
            counter++;
         }
         lobRs.updateRow();
      }
      finally
      {
         closeTempLOBs(tempClobsList, tempBlobsList);
         try
         {
            if (commit)
               conn.commit();
            conn.setAutoCommit(commit);
            if (lobRs != null)
               lobRs.close();
            if (lobStmt != null)
               lobStmt.close();
         }
         catch (SQLException sqle)
         {
            /* no-op */
         }
      }
   }

   /**
    * Creates a temporary BLOB from the given binary data.
    * @param conn database connection, assumed not <code>null</code>
    * @param binData binary data to be inserted into the BLOB column, assumed
    * not <code>null</code>
    * @return the temporary BLOB, never <code>null</code>
    * @throws SQLException if any error occurs while creating the temporary BLOB
    * @throws IOException if any error occurs while writing the BLOB data to the
    * temporary BLOB
    */
   private BLOB createTempBLOB(Connection conn, byte[] binData)
      throws SQLException, IOException
   {
      // Create a temporary BLOB with duration session
      
      BLOB tempBlob= null;

      //try with cache flag set to 'false' if fails retry once with 'true',
      //see more details above it next to the ms_tempLobCacheFlag.
      try
      {
         tempBlob = BLOB.createTemporary(conn, ms_tempLobCacheFlag, BLOB.DURATION_SESSION);
      }
      catch(SQLException sqlEx)
      {
         ms_tempLobCacheFlag = !ms_tempLobCacheFlag;
         tempBlob = BLOB.createTemporary(conn, ms_tempLobCacheFlag, BLOB.DURATION_SESSION);
      }
      
      // Open the temporary BLOB in readwrite mode to enable writing
      tempBlob.open(BLOB.MODE_READWRITE);
      OutputStream tempBlobOStream = null;
      try
      {
         // Get the output stream for writing into the BLOB
         tempBlobOStream = tempBlob.getBinaryOutputStream();
         tempBlobOStream.write(binData);
      }
      catch (IOException ioe)
      {
         if (tempBlob != null)
            closeTempBLOB(tempBlob);
         throw ioe;
      }
      finally
      {
         // Flush and close the streams
         if (tempBlobOStream != null)
         {
            try
            {
               tempBlobOStream.close();
            }
            catch (IOException ioe)
            {
               //no-op
            }
         }
      }
      return tempBlob;
  }

   /**
    * Creates a temporary CLOB from the given string.
    * @param conn database connection, assumed not <code>null</code>
    * @param colValue string to be inserted into the CLOB column, assumed
    * not <code>null</code>, may be empty
    * @return the temporary CLOB, never <code>null</code>
    * @throws SQLException if any error occurs while creating the temporary CLOB
    * @throws IOException if any error occurs while writing the BLOB data to the
    * temporary BLOB
    */
   private CLOB createTempCLOB(Connection conn, String colValue)
      throws SQLException, IOException
   {
      
      // Create a temporary CLOB with duration session
      CLOB tempClob= null;
      //try with cache flag set to 'false' if fails retry once with 'true',
      //see more details above it next to the ms_tempLobCacheFlag.
      try
      {
         tempClob = CLOB.createTemporary(conn, ms_tempLobCacheFlag, CLOB.DURATION_SESSION);
      }
      catch(SQLException sqlEx)
      {
         ms_tempLobCacheFlag = !ms_tempLobCacheFlag;
         tempClob = CLOB.createTemporary(conn, ms_tempLobCacheFlag, CLOB.DURATION_SESSION);
      }
          
      // Open the temporary CLOB in readwrite mode to enable writing
      tempClob.open(CLOB.MODE_READWRITE);
      Writer tempClobWriter = null;
      try
      {
         // Get the output stream for writing into the BLOB
         tempClobWriter = tempClob.getCharacterOutputStream();
         tempClobWriter.write(colValue);
      }
      catch (IOException ioe)
      {
         if (tempClob != null)
            closeTempCLOB(tempClob);
         throw ioe;
      }
      finally
      {
         // Flush and close the streams
         if (tempClobWriter != null)
         {
            try
            {
               tempClobWriter.close();
            }
            catch (IOException ioe)
            {
               //no-op
            }
         }
      }
      return tempClob;
  }

   /**
    * Closes the temporary CLOB. Sets the input clob parameter to
    * <code>null</code> after closing it.
    * @param clob the temporary CLOB, may be <code>null</code>
    */
   private void closeTempCLOB(CLOB clob)
   {
      if (clob == null)
         return;
      try
      {
         if (clob.isOpen())
            clob.close();
         // Free the memory used by this CLOB
         clob.freeTemporary();
      }
      catch (SQLException sqle)
      {
         // no-op
      }
      clob = null;
   }

   /**
    * Closes the temporary BLOB. Sets the input blob parameter to
    * <code>null</code> after closing it.
    * @param blob the temporary CLOB, may be <code>null</code>
    */
   private void closeTempBLOB(BLOB blob)
   {
      if (blob == null)
         return;
      try
      {
         if (blob.isOpen())
            blob.close();
         // Free the memory used by this BLOB
         blob.freeTemporary();
      }
      catch (SQLException sqle)
      {
         // no-op
      }
      blob = null;
   }

   /**
    * Closes all the temporary LOBs in the two lists.
    *
    * @param clobsToCloseList list containing temporary CLOBs, assumed
    * not <code>null</code>, may be empty list.
    * @param blobsToCloseList list containing temporary BLOBs, assumed
    * not <code>null</code>, may be empty list.
    */
   private void closeTempLOBs(List tempclobsList, List tempBlobsList)
   {
      Iterator it = tempclobsList.iterator();
      while (it.hasNext())
      {
         Object obj = it.next();
         if ((obj != null ) && (obj instanceof CLOB))
         {
            // If the CLOB is open, close it
            CLOB clob = (CLOB)obj;
            closeTempCLOB(clob);
         }
      }
      it = tempBlobsList.iterator();
      while (it.hasNext())
      {
         Object obj = it.next();
         if ((obj != null ) && (obj instanceof BLOB))
         {
            // If the CLOB is open, close it
            BLOB blob = (BLOB)obj;
            closeTempBLOB(blob);
         }
      }
   }


   /**
    * Validates the supplied statement type, should be one of
    * ORACLE_INSERT or ORACLE_UPDATE or ORACLE_DUMMY_UPDATE
    *
    * @param statementType the statement type to validate
    *
    * @return <code>false</code> if the statement type is invalid,
    *    <code>true</code> otherwise.
    */
   private boolean validStatementType(int statementType)
   {
      return ((statementType == ORACLE_INSERT) ||
               (statementType == ORACLE_UPDATE) ||
               (statementType == ORACLE_DUMMY_UPDATE));
   }
   

   /**
    * the select statement for getting a lock on clob/blob, contains
    * "FOR UPDATE" clause, never <code>null</code> or empty, initialized in
    * constructor.
    */
   private String m_selLobString;

   /**
    * List of {@link PSJdbcStatementColumn} objects, used to bind values into
    * the m_selLobString statement at runtime.  May be <code>null</code> if
    * the statement does not require any parameters, but never emtpy if not
    * <code>null</code>.
    */
   private PSCollection m_keyValues;

   /**
    * an array of Integer objects indicating the type of lob object in the
    * m_lobValues array at the same index, may not be <code>null</code>,
    * intialized in the constructor
    */
   private List m_lobTypes;

   /**
    * an array of LOB objects to be inserted/updated in the database,
    * may not be <code>null</code>, intialized in the constructor
    */
   private List m_lobValues;

   /**
    * an array of encodings of LOB values,
    * may not be <code>null</code>, intialized in the constructor
    */
   private List m_lobValuesEncoding;

   /**
    * the ROWID of the row inserted using the <code>executeInsert<code> method,
    * may be <code>null</code>, but never empty if not <code>null</code>
    */
   private String m_rowID;
   
   /**
    * the type of statement that this object represents
    */
   int m_statementType = ORACLE_INSERT;

   /**
    * constant for Oracle Insert statements
    */
   public static final int ORACLE_INSERT = 0;

   /**
    * constant for Oracle Update statements
    */
   public static final int ORACLE_UPDATE = 1;

   /**
    * constant for Oracle Dummy Update statements
    * For Oracle, update statements are split into two - one containing
    * clob-blob columns and other containing non-lob columns. If the original
    * update statement contains only clob-blob columns then after splitting
    * it into two the non-lob update statement does not have any columns and so
    * it results in a statement like
    * "UPDATE CMS_ARTICLE SET WHERE ARTICLE_ID = ?"
    * We will call such statements dummy update statements. These will
    * not be send to the database for execution. Instead the execute will
    * return 1 for such statements.
    *
    */
   public static final int ORACLE_DUMMY_UPDATE = 2;

   /**
    * xLOB.createTemporary, cache flag parameter, that happens to be different
    * depending on the ORA JDBC driver used. For instance an older ORA jar likes
    * to see 'false' while the newer jar expects 'true'. If ORA driver doesn't
    * get the 'right' mix of parameters in the xLOB.CreateTemporary method it
    * throws SQL exception. So, to work this cross driver incompatibility issue
    * around we expect a SQLException and retry once with this flag flipped.   
    */
   private static boolean ms_tempLobCacheFlag = false; 
}

