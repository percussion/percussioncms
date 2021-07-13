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

import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.error.PSIllegalArgumentException;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The PSSqlLockedUpdateBuilder class is used to build locked SQL UPDATE
 * statements. A SELECT is first issued against the table which locks the
 * rows. The UPDATEs are then performed using the specified cursor.
 *
 * @see         PSQueryOptimizer
 * @see         PSQueryJoiner
 */
public class PSSqlLockedUpdateBuilder extends PSSqlUpdateBuilder
{
   /**
    * Construct a SQL builder to build an UPDATE statement.
    *
    * @param   table      the table to build the UPDATE for
    */
   PSSqlLockedUpdateBuilder(PSBackEndTable table)
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
    * @param connKeys a ConcurrentHashMap that associates opaque keys representing
     * a specific database and server, and indecies into the <code>logins</code>
     * list passed to this method, must never be <code>null</code>
     * 
     * @return an update statement for the table passed to the ctor, this
     * will never be <code>null</code>
    */
   PSUpdateStatement generate(java.util.List logins, ConcurrentHashMap connKeys)
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
      PSSqlBuilderContext queryContext = new PSSqlBuilderContext();
      PSBackEndTable table = (PSBackEndTable)m_Tables.get(0);
        Object serverKey = table.getServerKey();
        Integer iConnKey = (Integer)connKeys.get(serverKey);
        if (iConnKey == null) {
           Object[] args = { serverKey };
           throw new PSIllegalArgumentException(
              IPSBackEndErrors.SQL_BUILDER_NO_CONN_DEFINED, args);
        }

      HashMap dtHash = new HashMap();
      PSBackEndLogin login = (PSBackEndLogin)logins.get(iConnKey.intValue());

      boolean supportsLocking = false;
      java.sql.Connection conn = null;
      try {
         conn = getConnection(login);
         java.sql.DatabaseMetaData meta = conn.getMetaData();

         // can the back-end be used for positioned updates?
         supportsLocking = meta.supportsSelectForUpdate() &&
            meta.supportsPositionedUpdate();
      } catch (java.sql.SQLException e) {
         Object[] args = { login.getDataSource(), PSSqlException.toString(e) };
         throw new PSIllegalArgumentException(
            IPSBackEndErrors.DBPOOL_CONN_INIT_EXCEPTION, args);
      } finally {
         try {
            if (conn != null)
               conn.close();
         } catch (java.sql.SQLException x) { /* ignore this, we're done */ }
      }

      /* there's only one table permitted per statement */
      context.addText("UPDATE ");
      buildTableName(login, context, table);

      /* get the data types for this table */
      loadDataTypes(login, dtHash, table);

      // build the SET clause for this table
      buildSetClause(context, table, dtHash);

      // the positioned WHERE clause syntax is simple
      // the actual cursor name will be appended at run-time from the
      // result set
      if (supportsLocking)
         context.addText(" WHERE CURRENT OF ");
      else
         buildWhereClauseFromKeys(context, table, dtHash);

      // we're done building, so close the last run
      context.closeTextRun();


      // also build the SELECT statement
      queryContext.addText("SELECT ");
      buildColumnList(queryContext, table, dtHash, false);
      queryContext.addText(" FROM ");
      buildTableName(login, context, table);

      buildWhereClauseFromKeys(queryContext, table, dtHash);
      if (supportsLocking)
         queryContext.addText(" FOR UPDATE ");
      queryContext.closeTextRun();

      // and the extractors for the UPDATE data to use to compare against
      // the retrieved SELECT data
      java.util.List targetExtractors = buildComparators(table);

      try {
         return new PSLockedUpdateStatement(
            iConnKey.intValue(), context.getBlocks(), queryContext.getBlocks(),
            targetExtractors, PSUpdateStatement.TYPE_UPDATE);
      } catch (PSDataExtractionException e) {
         throw new PSIllegalArgumentException(
            e.getErrorCode(), e.getErrorArguments());
      }
   }


   /**
    * Get the list of input data extractors. The ordering matches
    * the order we will be selecting columns from the back-end in.
    * We will test that the data selected from the back-end matches the
    * input data. If so, the data will not be updated.
    *
    * @param   table      the table to get extractors for
    *
    * @return            the list of IPSDataExtractor objects
    */
   protected java.util.List buildComparators(PSBackEndTable table)
      throws PSIllegalArgumentException
   {
      java.util.ArrayList extractors = null;

      int size = m_Columns.size();
      for (int i = 0; i < size; i++) {
         IPSBackEndMapping beMap = (IPSBackEndMapping)m_Columns.get(i);
         if (!(beMap instanceof com.percussion.design.objectstore.PSBackEndColumn))
         {   // UDF's are not yet supported in UPDATEs
            PSExtensionCall call = (PSExtensionCall)beMap;
            Object[] args = { call.getExtensionRef() };
            throw new PSIllegalArgumentException(
               IPSBackEndErrors.SQL_BUILDER_UDF_NOT_SUPPORTED_IN_MOD, args);
         }

         PSBackEndColumn col = (PSBackEndColumn)beMap;
         if (table.equals(col.getTable())) {
            String columnName = col.getColumn();
            PSDataMapping map = (PSDataMapping)m_Mappings.get(columnName);
            if (map == null) {
               Object[] args = { columnName };
               throw new PSIllegalArgumentException(
                  IPSBackEndErrors.SQL_BUILDER_MOD_MAP_REQD, args);
            }

            IPSDataExtractor extractor =
               PSDataExtractorFactory.createReplacementValueExtractor(
               (IPSReplacementValue)map.getDocumentMapping());
            if (extractors == null)
               extractors = new java.util.ArrayList();
            extractors.add(extractor);
         }
      }

      return extractors;
   }
}

