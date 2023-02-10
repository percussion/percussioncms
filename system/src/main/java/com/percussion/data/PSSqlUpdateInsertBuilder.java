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

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The PSSqlUpdateInsertBuilder class is used to build SQL UPDATE and
 * INSERT statements. It first attempts to process the UPDATE. If this
 * processes no rows, an INSERT is then attempted.
 *
 * @see        PSUpdateOptimizer
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSSqlUpdateInsertBuilder extends PSSqlUpdateBuilder
{
   /**
    * Construct a SQL builder to build an UPDATE statement.
    *
    * @param   table      the table to build the UPDATE for
    *
    * @throws  PSIllegalArgumentException  if this exception is thrown
    *          by the superclass
    */
   PSSqlUpdateInsertBuilder(PSBackEndTable table)
      throws PSIllegalArgumentException
   {
      super(table);
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

      return generateUpdateInsert(dtHash, iConnKey, 
         (PSBackEndLogin)logins.get(iConnKey));
   }
}

