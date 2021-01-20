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

import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.error.PSErrorException;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The PSOracleUpdateInsertStatement class is used to execute UPDATE and 
 * optionally INSERT statements. It first attempts to process the UPDATE.
 * If this processes no rows, an INSERT is then attempted.
 * This class is expressly for use with Oracle LOB containing update
 * with insert statement execution.
 *
 * @see        com.percussion.design.objectstore.PSUpdatePipe
 * @see        PSOracleUpdateStatement
 */
public class PSOracleUpdateInsertStatement extends PSOracleUpdateStatement
{
   /**
    * Construct an update statement with an optional insert statement 
    * which can be executed as part of the update execution plan.  Should
    * the update fail to affect any rows, the insert will be executed.
    *   
    * @param   connKey           The connection key to use to get the db conn.
    *
    * @param   updateBlocks      The statement blocks for the initial update
    *                            for the update statement.
    *                            Never <code>null</code>.
    *
    * @param   insertBlocks      The statement blocks for the initial insert
    *                            for the insert statement.
    *                            Never <code>null</code>.
    *
    * @param   lobUpdateBlocks   The statement blocks for LOB updates based on
    *                            oracle rowid used for both the update and
    *                            insert statements.  Never <code>null</code>.
    *
    * @param   rowIdQueryBlocks  The statement blocks for rowid retrieval
    *                            used for the update statement.  Never
    *                            <code>null</code>.
    *
    * @throws  PSDataExtractionException if a data extraction exception
    *          occurs creating the update or insert statements.
    *
    * @throws  IllegalArgumentException if any argument is invalid.
    */
   public PSOracleUpdateInsertStatement(
      int connKey, IPSStatementBlock[] updateBlocks,
      IPSStatementBlock[] insertBlocks,
      IPSStatementBlock[] lobUpdateBlocks, 
      IPSStatementBlock[] rowIdQueryBlocks)
      throws PSDataExtractionException
   {
      /* The following calls handle checking the arguments' validity */
      super(connKey, updateBlocks, lobUpdateBlocks, 
         rowIdQueryBlocks, TYPE_UPDATE);

      m_insertStatement   = new PSOracleUpdateStatement(connKey, insertBlocks, 
            lobUpdateBlocks, null, TYPE_INSERT);
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
      super.setIteratorNode(nodeName);
      m_insertStatement.setIteratorNode(nodeName);
   }

   /**
    * Get the data extractors used to get the replacement values which will
    * be used to execute the statement.
    *
    * @return            the list of replacement values
    */
   public List getReplacementValueExtractors()
   {
      ArrayList retList = new ArrayList();

      retList.addAll(super.getReplacementValueExtractors());
      retList.addAll(m_insertStatement.getReplacementValueExtractors());

      return retList;
   }

   /* ************  IPSExecutionStep Interface Implementation ************ */

   /**
    * Execute the data modification statement as a step in the execution
    * plan. Attempt an update, if the update does not affect any rows in
    * the table, then proceed with an insert.
    *
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
      throws SQLException,
         PSDataExtractionException,
         PSErrorException
   {
      // get the updated row count so we can see if this update affects
      // any rows
      PSRequest req = data.getRequest();
      PSRequestStatistics stats = req.getStatistics();
      int rowsUpdated = stats.getRowsUpdated();

      super.execute(data);      // process the update statement
      if (rowsUpdated == stats.getRowsUpdated()) {
         int level = req.getCurrentApplicationAccessLevel();
         if ((level & PSAclEntry.AACE_DATA_CREATE) ==
            PSAclEntry.AACE_DATA_CREATE)
         {  // no new updates and we're allowed to do inserts.
            // we need to execute the insert statement now.
            m_insertStatement.execute(data);
         }
      }
   }


   /**
    * The insert statement associated with this update/insert
    * statement.  This statement will be created at construction 
    * time and will be used when an update statement fails to affect
    * any rows in the database.
    */
   protected PSOracleUpdateStatement  m_insertStatement;
}


