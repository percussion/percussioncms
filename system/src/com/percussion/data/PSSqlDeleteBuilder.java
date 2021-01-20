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

import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.error.PSIllegalArgumentException;

import java.util.HashMap;
import java.util.Hashtable;


/**
 * The PSSqlDeleteBuilder class is used to build SQL SELECT statements.
 * It can be used to generate single table SELECTs or homogeneous
 * (same DBMS) joined SELECTs. The query optimizer is capable of building
 * heterogeneous (cross DBMS) SELECTs. It calls this class to build
 * each statement and also makes use of the PSQueryJoiner class to join
 * the returned data.
 *
 * @see         PSQueryOptimizer
 * @see         PSQueryJoiner
 */
public class PSSqlDeleteBuilder extends PSSqlUpdateBuilder
{
   /**
    * Construct a SQL builder to build a DELETE statement.
    */
   PSSqlDeleteBuilder(PSBackEndTable table)
      throws PSIllegalArgumentException
   {
      super(table);
   }

   /**
    * Generate the statement using the specified connection keys.
     * 
     * @param logins a list of logins, one per connection index in the values
     * contained within <code>connKeys</code>, must never be <code>null</code>
     *  
     * @param connKeys a hashtable that associates opaque keys representing
     * a specific database and server, and indecies into the <code>logins</code>
     * list passed to this method, must never be <code>null</code>
     * 
     * @return an update statement that deletes the table specified in
     * the ctor for this object, this will never return <code>null</code>
    */
   PSUpdateStatement generate(java.util.List logins, Hashtable connKeys)
      throws PSIllegalArgumentException
   {
        if (logins == null)
        {
           throw new IllegalArgumentException("logins must never be null");
        }
        if (connKeys == null)
        {
           throw new IllegalArgumentException("connKeys must never be null");
        }
      int size = m_Tables.size();
      
      // this is not multi-table ready!!!
      if (size == 0) {
         throw new PSIllegalArgumentException(
            IPSBackEndErrors.SQL_BUILDER_MOD_TABLE_REQD);
      }
      else if (size > 1) {
         throw new PSIllegalArgumentException(
            IPSBackEndErrors.SQL_BUILDER_MOD_SINGLE_TAB_ONLY);
      }

      PSSqlBuilderContext context = new PSSqlBuilderContext();
      PSBackEndTable table = (PSBackEndTable)m_Tables.get(0);
        Object serverKey = table.getServerKey();
      Integer iConnKey = (Integer)connKeys.get(serverKey);
      if (iConnKey == null) {
         Object[] args = { serverKey };
         throw new PSIllegalArgumentException(
            IPSBackEndErrors.SQL_BUILDER_NO_CONN_DEFINED, args);
      }

      /* there's only one table here */
      context.addText("DELETE FROM ");

      HashMap dtHash = new HashMap();
      PSBackEndLogin login = (PSBackEndLogin)logins.get(iConnKey.intValue());
      buildTableName(login, context, table);

      /* get the data types for this table */
      loadDataTypes(login, dtHash, table);

      // build the WHERE
      buildWhereClauseFromKeys(context, table, dtHash);

      // we're done building, so close the last run
      context.closeTextRun();

      try {
         return new PSUpdateStatement(
            iConnKey.intValue(), context.getBlocks(),
            PSUpdateStatement.TYPE_DELETE);
      } catch (PSDataExtractionException e) {
         throw new PSIllegalArgumentException(
            e.getErrorCode(), e.getErrorArguments());
      }
   }
}

