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

import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestStatistics;
import com.percussion.util.PSPreparedStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * The PSLockedUpdateStatement class is the super-class for all data
 * modification statement (updates or deletes) which first lock the target
 * row(s) then apply the changes. The
 * statement is built from the PSUpdatePipe definitions. When a
 * request is received, the statement can be handed the request data and 
 * executed.
 * 
 * @see        com.percussion.design.objectstore.PSUpdatePipe
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSLockedUpdateStatement extends PSUpdateStatement
{
   /**
    * Construct an update statement which can be executed as part of the
    * update execution plan. The query may contain place holders, which
    * must be filled prior to execution.
    *   
    * @param   connKey         the connection key to use to get the db conn
    *
    * @param   updateBlocks   the statement blocks comprising the UPDATE or
    *                           DELETE statement
    *
    * @param   queryBlocks      the statement blocks comprising the SELECT
    *                           statement
    *
    * @param   targetExtractors   a list of IPSDataExtractor objects to use
    *                           when comparing the SELECT data with the
    *                           data submitted for UPDATE
    *
    * @param   type            the TYPE_xxx statement type
    */
   public PSLockedUpdateStatement(
      int connKey, IPSStatementBlock[] updateBlocks,
      IPSStatementBlock[] queryBlocks,
      java.util.List targetExtractors, int type)
      throws com.percussion.data.PSDataExtractionException
   {
      super(connKey, updateBlocks, type);
      m_queryStatement = new PSQueryStatement(connKey, queryBlocks);
      m_isPositioned = m_sqlString.endsWith("WHERE CURRENT OF ");
      m_targetExtractors = targetExtractors;
   }


   /* ************  IPSExecutionStep Interface Implementation ************ */
      
   /**
    * Execute the data modification statement as a step in the execution
    * plan. A result set will be generated containing the number of rows
    * effected by the execution of this statement. The result set will be
    * added to the execution data.
    *
    * @param   data     the execution data associated with this plan
    *
    * @exception   SQLException
    *                     if a SQL error occurs
    */
   public void execute(PSExecutionData data)
      throws java.sql.SQLException,
         com.percussion.data.PSDataExtractionException,
         com.percussion.error.PSErrorException
   {
      /* execute the statement through the db pool
       * and create a result set with the update statistics
       */

      PreparedStatement stmt   = null;
      Connection conn = null;
      PSRequest req = data.getRequest();
      PSRequestStatistics stats = req.getStatistics();
      ResultSet rs = null;

      // first execute the query which will lock matching rows
      m_queryStatement.execute(data);
      java.util.Stack rsStack = data.getResultSetStack();

      // if this returned no data, we're done
      if (rsStack.size() < 1)
         return;

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

         // get the result set so we can get the SELECT's cursor name
         rs = (ResultSet)rsStack.pop();

         // append the SELECT's cursor name to the positioned UPDATE text
         String sqlText;
         if (m_isPositioned)
            sqlText = m_sqlString + rs.getCursorName();
         else
            sqlText = m_sqlString;

         // log the statement (if full user activity is on)
         logPreparedStatement(data, sqlText);
         /* workaround for JDK Bug Id  4423012 */
         boolean preparedStatementHasParams = (sqlText.indexOf("?") >= 0);
         stmt = PSPreparedStatement.getPreparedStatement(conn, sqlText);
         data.addPreparedStatement(stmt);

         int colCount = rs.getMetaData().getColumnCount();

         while (rs.next())
         {
            /* *TODO* (future)
             *
             * do the data comparisons here to see if what was selected
             * matches what was passed in (in which case we can skip
             * the update
             */
            // for (int i = 0; i < colCount; i++) {
            //    IPSDataExtractor target
            //       = (IPSDataExtractor)m_targetExtractors.get(i);
            // 
            // }

            //Sets the action(insert/update/delete) on table change data object
            PSTableChangeData tableChangeData = data.getTableChangeData();
            if (tableChangeData != null)
               initTableChangeAction(tableChangeData);
            
            if (preparedStatementHasParams)
               stmt.clearParameters();
            int bindStart = 1;
            for (int i = 0; i < m_blocks.length; i++) {
               bindStart = m_blocks[i].setColumnData(data, stmt, bindStart);
            }

            // execute the statement and store the result count
            int rowCount = stmt.executeUpdate();

            // Notify Table Change Listeners
            if(tableChangeData != null && rowCount > 0)
               tableChangeData.notifyListeners();

            // store the statistics in the appropriate place based upon type
            switch (m_statementType) {
               case TYPE_UPDATE:
                  stats.incrementRowsUpdated(rowCount);
                  break;

               case TYPE_INSERT:
                  stats.incrementRowsInserted(rowCount);
                  break;

               case TYPE_DELETE:
                  stats.incrementRowsDeleted(rowCount);
                  break;
            }
         }
      }
      catch (java.sql.SQLException e)
      {
         joinSqlExceptions(conn, stmt, e);
         if (stmt != null) {
            stmt.close();
            data.removePreparedStatement(stmt);
         }
            
         throw e;
      }
   }

   public java.lang.String toString()
   {
      try {
         return m_queryStatement.toString() + "\r\n" + buildSqlString(null);
      } catch (com.percussion.data.PSDataExtractionException e) {
         // this is an invalid statement, return the exception as the text
         return e.toString();
      }
   }


   /**
    * The SQL statement being used for the SELECT.
    */
   protected PSQueryStatement      m_queryStatement;

   /**
    * Is this using a positioned SQL statement, in which case the cursor
    * name must be appended to the SELECT's text?
    */
   protected boolean               m_isPositioned;

   /**
    * The extractors to use to get the data for comparison. These
    * are IPSDataExtractor objects.
    */
   protected java.util.List      m_targetExtractors;
}

