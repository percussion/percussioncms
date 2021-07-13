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

