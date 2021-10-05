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

