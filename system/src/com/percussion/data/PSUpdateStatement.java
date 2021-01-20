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

import com.percussion.debug.PSDebugLogHandler;
import com.percussion.debug.PSTraceMessageFactory;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestStatistics;
import com.percussion.util.PSPreparedStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * The PSUpdateStatement class is the super-class for all data
 * modification statement (updates, inserts or deletes) handlers. The
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
public class PSUpdateStatement extends PSStatement
{
   /**
    * This is an INSERT statement.
    */
   public static final int TYPE_INSERT      = 0x01;

   /**
    * This is an UPDATE statement.
    */
   public static final int TYPE_UPDATE      = 0x02;

   /**
    * This is an DELETE statement.
    */
   public static final int TYPE_DELETE      = 0x04;

   /**
    * Construct an update statement which can be executed as part of the
    * update execution plan. The query may contain place holders, which
    * must be filled prior to execution.
    *
    * @param   connKey      the connection key to use to get the db conn
    *
    * @param   blocks      the statement blocks comprising this statement
    *
    * @param   type         the TYPE_xxx statement type
    */
   public PSUpdateStatement(
      int connKey, IPSStatementBlock[] blocks, int type)
      throws com.percussion.data.PSDataExtractionException
   {
      this(connKey, blocks, true, type);
   }

   /**
    * Construct an update statement which can be executed as part of the
    * update execution plan. The query may contain place holders, which
    * must be filled prior to execution.
    *
    * @param   connKey      the connection key to use to get the db conn
    *
    * @param   blocks      the statement blocks comprising this statement
    *
    *   @param   buildSql      <code>true</code> to build the internal SQL
    *                        string immediately
    *
    * @param   type         the TYPE_xxx statement type
    */
   public PSUpdateStatement(
      int connKey, IPSStatementBlock[] blocks, boolean buildSql, int type)
      throws com.percussion.data.PSDataExtractionException
   {
      super(connKey, blocks, buildSql);
      m_statementType = type;
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

         // log the statement (if full user activity is on)
         /* workaround for JDK Bug Id  4423012 */
         boolean preparedStatementHasParams = (m_sqlString.indexOf("?") >= 0);
         logPreparedStatement(data, m_sqlString);
         stmt = PSPreparedStatement.getPreparedStatement(conn, m_sqlString);

         if (preparedStatementHasParams)
            stmt.clearParameters();
         int bindStart = 1;
         for (int i = 0; i < m_blocks.length; i++) {
            bindStart = m_blocks[i].setColumnData(data, stmt, bindStart);
         }

         // execute the statement and store the result count
         int rowCount = stmt.executeUpdate();

         // collect table change events
         if (tableChangeData != null)
            tableChangeData.collectTableChangeEvent(rowCount);

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

            case TYPE_DELETE:
               stats.incrementRowsDeleted(rowCount);
               traceRowAction(req, "traceResourceHandler_updateDeleted",
                                 rowCount);
               break;
         }
      }
      catch (java.sql.SQLException e) {
         joinSqlExceptions(conn, stmt, e);
         throw e;
      } 
      finally
      { 
         if (stmt != null) {
            try {stmt.close();}
            catch (Exception e) { /* we're done anyway */ }
         }
         
         // release the data which were set by setColumnData() above
         for (int i = 0; i < m_blocks.length; i++) {
            m_blocks[i].releaseColumnData();
         }
      }
   }

   /**
    * Trace the rowcount and request type for this data modification
    * statement.
    *
    * @param   request  the PSRequest from which to obtain the LogHandler
    * @param   type     the type of action that was performed
    * @param   count    the number of rows affected by the action
    *
    */
   protected void traceRowAction(PSRequest request, String type, int count)
   {
      PSDebugLogHandler dh = (PSDebugLogHandler)request.getLogHandler();
      if (dh.isTraceEnabled(PSTraceMessageFactory.RESOURCE_HANDLER_FLAG))
      {
         Object[] args = {new Integer(count), type};
         dh.printTrace(PSTraceMessageFactory.RESOURCE_HANDLER_FLAG, args);
      }
   }

   /**
    * Initializes the action in the supplied <code>PSTableChangeData</code> 
    * based on this statement's type.  
    * 
    * @param tableChangeData The table change data, may not be 
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>tableChangeData</code> is 
    * <code>null</code>.
    */
   protected void initTableChangeAction(PSTableChangeData tableChangeData)
   {
      if (tableChangeData == null)
         throw new IllegalArgumentException("tableChangeData may not be null");
         
      if( tableChangeData != null)
      {
         tableChangeData.clearData();
         int action = PSTableChangeEvent.ACTION_UNDEFINED;
         switch (m_statementType) {
            case TYPE_UPDATE:
               action = PSTableChangeEvent.ACTION_UPDATE;
               break;

            case TYPE_INSERT:
               action = PSTableChangeEvent.ACTION_INSERT;
               break;

            case TYPE_DELETE:
               action = PSTableChangeEvent.ACTION_DELETE;
               break;
         }
         tableChangeData.setActionType( action );
      }
         
   }

   protected int m_statementType;
}

