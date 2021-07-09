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

import com.percussion.debug.PSDebugLogHandler;
import com.percussion.debug.PSTraceMessageFactory;
import com.percussion.log.PSLogHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;


/**
 * The PSStatement class defines a statement (query or update) which can
 * be executed as part of a request handler's execution plan.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public abstract class PSStatement implements IPSExecutionStep {

   /**
    * Construct a statement which can be executed as part of an
    * execution plan. The statement may contain place holders, which
    * must be filled prior to execution.
    *   
    * @param   connKey      the connection key to use to get the db conn
    *
    * @param   blocks      the statement blocks comprising this statement
    *
    *   @param   buildSql      <code>true</code> to build the internal SQL
    *                        string immediately
    */
   protected PSStatement(
      int connKey, IPSStatementBlock[] blocks, boolean buildSql)
      throws com.percussion.data.PSDataExtractionException
   {
      super();
      m_connKey   = connKey;
      m_blocks      = blocks;

      if (buildSql)
         m_sqlString = buildSqlString(null);   // build the sql statement once
      else
         m_sqlString = null;   // must be building it on the fly each time
   }

   /**
    * Construct a statement which can be executed as part of an
    * execution plan. The statement may contain place holders, which
    * must be filled prior to execution. The SQL statement will be 
    * built immediately using this method.
    *   
    * @param   connKey      the connection key to use to get the db conn
    *
    * @param   blocks      the statement blocks comprising this statement
    */
   protected PSStatement(int connKey, IPSStatementBlock[] blocks)
      throws com.percussion.data.PSDataExtractionException
   {
      this(connKey, blocks, true);
   }

   /**
    * Set the name of the node which we will iterate over executing this
    * statement.
    * As long as a node of the specified name exists, this statement should
    * be executed. This object does not actually make use of this
    * information. It will only execute once in the context specified to
    * the execute call. This is primarily used as the storage are so that
    * the PSTransactionSet object calling this knows what to iterate on.
    *
    * @param   nodeName      the name of the node to iterate on
    */
   public void setIteratorNode(String nodeName)
   {
      // if we're iterating to the current node, use null which means don't
      // iterate
      if (".".equals(nodeName))
         nodeName = null;

      m_iteratorNode = nodeName;
   }

   /**
    * Get the name of the node which we will iterate over executing this
    * statement.
    * As long as a node of the specified name exists, this statement should
    * be executed. This object does not actually make use of this
    * information. It will only execute once in the context specified to
    * the execute call. This is primarily used as the storage are so that
    * the PSTransactionSet object calling this knows what to iterate on.
    *
    * @return               the name of the node to iterate on
    */
   public String getIteratorNode()
   {
      return m_iteratorNode;
   }

   /**
    * Build the query string using the specified data for context info.
    *
    * @param   data      the execution context
    *
    * @return            the SQL string which can be used in a jdbc
    *                     PreparedStatement object
    */
   public java.lang.String buildSqlString(PSExecutionData data)
      throws com.percussion.data.PSDataExtractionException
   {
      return buildSqlString(data, false);
   }

   /**
    * Build the query string using the specified data for context info.
    *
    * @param   data            the execution context
    *
    * @param   forceRebuild   <code>true</code> to rebuild the SQL
    *                           statement, even if one has already been
    *                           stored
    *
    * @return                  the SQL string which can be used in a jdbc
    *                           PreparedStatement object
    */
   public java.lang.String buildSqlString(
      PSExecutionData data, boolean forceRebuild)
      throws com.percussion.data.PSDataExtractionException
   {
      String s = "";

      if ((m_sqlString != null) && !forceRebuild)
         s = m_sqlString;
      else {
         StringBuffer buf = new StringBuffer();
         for (int i = 0; i < m_blocks.length; i++) {
            m_blocks[i].buildStatement(buf, data);
         }
         s = buf.toString();
      }

      return s;
   }

   public java.lang.String toString()
   {
      try {
         return buildSqlString(null, false);
      } catch (com.percussion.data.PSDataExtractionException e) {
         // this is an invalid statement, return the exception as the text
         return e.toString();
      }
   }

   /**
    * Get the data extractors used to get the replacement values which will
    * be used to execute the statement.
    *
    * @return            the list of replacement values
    */
   public java.util.List getReplacementValueExtractors()
   {
      java.util.ArrayList retList = new java.util.ArrayList();
      java.util.List curList;
      for (int i = 0; i < m_blocks.length; i++) {
         curList = m_blocks[i].getReplacementValueExtractors();
         retList.addAll(curList);
      }

      return retList;
   }


   protected void joinSqlExceptions(
      Connection conn, PreparedStatement stmt,
      java.sql.SQLException e)
   {
      SQLWarning wConn = null;
      SQLWarning wStmt = null;

      if (conn != null) {
         try { wConn = conn.getWarnings(); }
         catch (java.sql.SQLException ex) { /* ignore at this point */ }
      }

      if (stmt != null) {
         try { wStmt = stmt.getWarnings(); }
         catch (java.sql.SQLException ex) { /* ignore at this point */ }
      }

      if ((wStmt != null) || (wConn != null))
      {
         /* now add the warnings to the exception */
         SQLException cur = e;
         while (cur.getNextException() != null)
            cur = cur.getNextException();

         if (wStmt != null)
         {
            cur.setNextException(wStmt);
            cur = wStmt;
         }
            
         if (wConn != null)
         {
            while (cur.getNextException() != null)
               cur = cur.getNextException();
            cur.setNextException(wConn);
         }
      }
   }

   protected void logPreparedStatement(
      PSExecutionData data, String statementText)
   {
      PSLogHandler lh = data.getLogHandler();
      lh.logFullUserActivityAction(
         data.getRequest(),
         IPSBackEndErrors.LOG_PREPARED_STMT,
         new Object[] { statementText },
         false);   // don't force logging, do it if permitted

      // trace as well
      PSDebugLogHandler dh = (PSDebugLogHandler)lh;
      if (dh.isTraceEnabled(PSTraceMessageFactory.RESOURCE_HANDLER_FLAG))
      {
         Object[] args = {statementText, "traceResourceHandler_sqlStmt"};
         dh.printTrace(
               PSTraceMessageFactory.RESOURCE_HANDLER_FLAG, args);
      }

   }


   /* ************  IPSExecutionStep Interface Implementation ************ */

   /**
    * Execute the statement as a step in the execution plan.
    *
    * @param   data     the execution data associated with this plan
    *
    * @exception   SQLException
    *                     if a SQL error occurs
    */
   public abstract void execute(PSExecutionData data)
      throws java.sql.SQLException,
         com.percussion.error.PSIllegalArgumentException,
         com.percussion.data.PSDataExtractionException,
         com.percussion.error.PSErrorException;


   protected IPSStatementBlock[] m_blocks;
   protected int                  m_connKey;
   protected String                m_sqlString;
   protected String               m_iteratorNode;
}

