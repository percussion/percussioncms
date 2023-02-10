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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The PSSqlInsertBuilder class is used to build SQL SELECT statements.
 * It can be used to generate single table SELECTs or homogeneous
 * (same DBMS) joined SELECTs. The query optimizer is capable of building
 * heterogeneous (cross DBMS) SELECTs. It calls this class to build
 * each statement and also makes use of the PSQueryJoiner class to join
 * the returned data.
 *
 * @see        PSQueryOptimizer
 * @see        PSQueryJoiner
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSSqlInsertBuilder extends PSSqlUpdateBuilder
{
   /**
    * Construct a SQL builder to build a SELECT which may be for a
    * single table or to do homogeneous (same DBMS) joins.
    *
    * @throws  PSIllegalArgumentException  if this exception is thrown
    *          by the superclass
    */
   PSSqlInsertBuilder(PSBackEndTable table)
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
    *
    * @throws  PSIllegalArgumentException  if this exception is thrown
    *          by the superclass' addUpdateColumn method
    */
   void addKeyColumn(PSBackEndColumn col)
      throws PSIllegalArgumentException
   {
      addUpdateColumn(col);
   }

   /**
    * Generate the statement using the specified connection keys.
    *
    * @param   logins   The list of back end logins, indexed by 
    *                   connection key.
    *
    * @param   connKeys The map of connection keys, keyed on driver:server
    *
    * @return  the appropriate update statement
    *
    * @throws  PSIllegalArgumentException  if this exception is thrown
    *          by any of the superclass' methods
    */
   PSUpdateStatement generate(List logins, ConcurrentHashMap connKeys)
      throws PSIllegalArgumentException
   {
      HashMap dtHash = new HashMap();

      int iConnKey = validateBuilderConnection(dtHash, connKeys, logins);

      return generateInsert(dtHash, iConnKey, 
         (PSBackEndLogin)logins.get(iConnKey));
   }
}

