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

import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.error.PSIllegalArgumentException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The PSOracleUpdateInsertBuilder.java class is used to build SQL UPDATE and
 * INSERT statements. It first attempts to process the UPDATE. If this
 * processes no rows, an INSERT is then attempted.
 *
 * @see         PSUpdateOptimizer
 */
public class PSOracleUpdateInsertBuilder extends PSOracleUpdateBuilder
{
   /**
    * Construct a SQL builder to build an UPDATE statement.
    *
    * @param   table      the table to build the UPDATE for
    *
    * @throws PSIllegalArgumentException  when the superclass throws this
    *    exception
    */
   PSOracleUpdateInsertBuilder(PSBackEndTable table)
      throws PSIllegalArgumentException
   {
      super(table);
   }

   /**
    * Generate the statement using the specified connection keys.
    *
    * @param   logins        The list of logins.
    *
    * @param   connKeys      The hash table containing the driver:server
    *                        as the key and the conn number as the value.
    *
    * @return  The PSUpdate-derived statement to process this update
    *          and insert combination.
    *
    * @throws PSIllegalArgumentException If there are multiple tables 
    * or a PSDataExtractionException occurs.
    */
   PSUpdateStatement generate(List logins, ConcurrentHashMap connKeys)
      throws PSIllegalArgumentException
   {
      HashMap dtHash = new HashMap();

      int iConnKey = validateBuilderConnection(dtHash, connKeys, logins);

      PSBackEndLogin login = (PSBackEndLogin)logins.get(iConnKey);

      /* check datatypes for LOB types */
      if (m_lobColumnInitializer == null)
      {
         return generateUpdateInsert(dtHash, iConnKey, 
            login);
      }

      PSBackEndTable table = (PSBackEndTable)m_Tables.get(0);
         
      // for the INSERT statement, we need all key and update columns
      // in the column list
      ArrayList columnList = new ArrayList();
      columnList.addAll(m_Keys);
      columnList.addAll(m_Columns);

      // Build the INSERT context
      PSSqlBuilderContext insertContext = 
         getInsertContext( table, login, dtHash, 
         columnList);

      // Build the UPDATE context
      PSSqlBuilderContext updateContext = 
         getSingleRowidUpdateContext(table, login, dtHash);

      try {
         return new PSOracleUpdateInsertStatement( iConnKey,
            updateContext.getBlocks(), insertContext.getBlocks(),
            getRowRetrievalByRowidContext(table, login, dtHash).getBlocks(),
            getRowIdsFromKeysContext(table, login, dtHash).getBlocks()
            );
      } catch (PSDataExtractionException e) {
         throw new PSIllegalArgumentException(
            e.getErrorCode(), e.getErrorArguments());
      }
   }
}


