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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;


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
   PSUpdateStatement generate(List logins, Hashtable connKeys)
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


