/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.data;

import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.error.PSIllegalArgumentException;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The PSOracleInsertBuilder class is used to build SQL INSERT statements.
 * It is responsible for creating Oracle specific update statements when
 * LOB columns are present.  When no LOBs are present, this builder will
 * use the generate method defined in PSSqlInsertBuilder.
 *
 * @see         PSUpdateOptimizer
 * @see         PSOracleUpdateBuilder
 */
public class PSOracleInsertBuilder extends PSOracleUpdateBuilder
{
   /**
    * Construct a SQL builder to build an INSERT for a
    * single table.
    *
    * @param table  The table to insert into.
    */
   PSOracleInsertBuilder(PSBackEndTable table)
      throws PSIllegalArgumentException
   {
      super(table);
   }

   /**
    * Add an INSERT column which is part of the lookup key. Since INSERTs
    * do not use WHERE clauses for lookups, this is the same as calling
    * addUpdateColumn.
    *
    * @param   col      the column to add
    */
   void addKeyColumn(PSBackEndColumn col)
      throws PSIllegalArgumentException
   {
      addUpdateColumn(col);
   }

   /**
    * Generate the statement using the specified connection keys.
    * If no LOB columns are present, this will call the default
    * generateInsert method in PSSqlUpdateBuilder, otherwise create
    * the Oracle-specific PSOracleUpadeStatement to process the insert.
    *
    * @param   logins   The list of logins.
    *
    * @param   connKeys The map of connection keys.
    *
    * @return  The PSUpdate-derived statement to process this insert.
    *
    * @throws PSIllegalArgumentException If there are multiple tables 
    * or a PSDataExtractionException occurs.
    */
   PSUpdateStatement generate(java.util.List logins, ConcurrentHashMap connKeys)
      throws PSIllegalArgumentException
   {
      HashMap dtHash = new HashMap();

      int iConnKey = validateBuilderConnection(dtHash, connKeys, logins);

      PSBackEndLogin login = (PSBackEndLogin)logins.get(iConnKey);

      /* check datatypes for LOB types */
      if (m_lobColumnInitializer == null)
      {
         return generateInsert(dtHash, iConnKey, 
            login);
      }

      PSBackEndTable table = (PSBackEndTable)m_Tables.get(0);

      try {
         return new PSOracleUpdateStatement(
            iConnKey,
            getInsertContext(table, login, dtHash, 
               m_Columns).getBlocks(),
            getRowRetrievalByRowidContext(table, login, dtHash).getBlocks(),
            null,
            PSUpdateStatement.TYPE_INSERT);
      } catch (PSDataExtractionException e) {
         throw new PSIllegalArgumentException(
            e.getErrorCode(), e.getErrorArguments());
      }
   }
}


