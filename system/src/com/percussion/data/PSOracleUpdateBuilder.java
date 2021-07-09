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
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.error.PSIllegalArgumentException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The PSOracleUpdateBuilder class is used to build SQL UPDATE statements.
 * It can be used to generate single table UPDATEs.  This class will be used
 * to utilize Oracle-specific extensions to JDBC to allow the update of
 * LOB columns when LOB columns are present.  When LOB columns are not
 * present, this builder will utilize its base class (PSSqlUpdateHandler)
 * to create normal JDBC-based statements.
 *
 * @see         PSUpdateOptimizer
 * @see         PSSqlUpdateBuilder
 */
public class PSOracleUpdateBuilder extends PSSqlUpdateBuilder
{
   /**
    * Construct an update builder to build the statements
    * to do Oracle-specific updates when required.
    *
    * @param table  The table to insert into.
    *
    * @throws PSIllegalArgumentException  When the super-class throws
    *    this exception.
    */
   PSOracleUpdateBuilder(PSBackEndTable table)
      throws PSIllegalArgumentException
   {
      super(table);
   }

   /**
    * Build the column and placeholder list based upon the specified
    * parameters. This can be used in an UPDATE statement's SET clause
    * or in a DELETE or UPDATE's WHERE clause.  For Oracle we override
    * this method so that we can check for LOB columns during processing.
    * If none are found, we will simply call up to PSSqlUpdateBuilder.
    *
    * @param   context        the builder context
    *
    * @param   table          the table the statement is being built for
    *
    * @param   datatypes      the data type hash map containing the 
    *                         table.column to data type mapping
    *
    * @param   columnList     the columns to build into the statement
    *
    * @param   delimiter      the delimiter to use, such as ", " for SET
    *                         clauses or " AND " for WHERE clauses
    *
    * @param   ignoreAutoIncrements
    *                           <code>true</code> to omit columns which are
    *                           auto-incremented by the server
    *
    * @return                   <code>true</code> if at least one column
    *                           was used; <code>false</code> otherwise
    */
   protected boolean buildColumnAndPlaceholderList(
      PSSqlBuilderContext context, PSBackEndTable table, HashMap dataTypes,
      ArrayList columnList, String delimiter, boolean ignoreAutoIncrements)
      throws PSIllegalArgumentException
   {
      /* call the super with our lob column initializer */
      return super.buildColumnAndPlaceholderList(context, table, dataTypes,
         columnList, delimiter, ignoreAutoIncrements, m_lobColumnInitializer);
   }

   /**
    * Build a column list of the form col1, col2, ..., coln. Any columns
    * which are not updatable (eg, MS SQL identity columns) are not included
    * in the list.   This overrides PSSqlUpdateBuilder functionality to
    * allow for IPSLobColumnInitializers when Lob columns are present.
    *
    * @param context The builder context for the specified statement,
    *    never <code>null</code>.
    *
    * @param table   The back end table for the specified statement,
    *    never <code>null</code>.
    *
    * @param dataTypes The map of data types.  Never <code>null</code>.
    *
    * @param usePlaceHolder <code>true</code> indicates this is a placeholder
    *    pass for the statement, <code>false</code> indicates this is a column
    *    name pass.
    *
    * @param columns The column list to add to the specified context.
    *    Never <code>null</code>.
    *
    * @throws  PSIllegalArgumentException If a udf is found in the column list,
    *    a specified column is not mapped, or no columns were found.
    */
   protected void buildColumnList(
      PSSqlBuilderContext context, PSBackEndTable table, HashMap datatypes,
      boolean usePlaceHolder, List columns)
      throws PSIllegalArgumentException
   {
      /* call the super with our lob column initializer */
      super.buildColumnList(context, table, datatypes,
         usePlaceHolder, columns, m_lobColumnInitializer);
   }

   /**
    * Build the LOB column list.  This list will be used to create the
    * queries that are required to update the Oracle LOB types.
    *
    * @param   context        the builder context
    *
    * @param   table          the table the statement is being built for
    *
    * @param   datatypes      the data type hash map containing the 
    *                         table.column to data type mapping
    *
    * @param   columnList     the columns to build into the statement
    *
    * @param   delimiter      the delimiter to use, such as ", " for SET
    *                         clauses or " AND " for WHERE clauses
    *
    * @return                   <code>true</code> if at least one column
    *                           was used; <code>false</code> otherwise
    * @throws PSIllegalArgumentException  If a back-end mapping is found
    *          which is not a PSBackEndColumn.
    */
   protected boolean buildLobColumnList(
      PSSqlBuilderContext context, PSBackEndTable table, HashMap dataTypes,
      ArrayList columnList, String delimiter)
      throws PSIllegalArgumentException
   {
      int colCount = 0;   // keep track of the number of cols we set
      int size = columnList.size();
      for (int i = 0; i < size; i++) {
         IPSBackEndMapping beMap = (IPSBackEndMapping)columnList.get(i);
         if (!(beMap instanceof PSBackEndColumn))
         {   // UDF's are not yet supported in UPDATEs
            PSExtensionCall call = (PSExtensionCall)beMap;
            Object[] args = { call.getExtensionRef() };
            throw new PSIllegalArgumentException(
               IPSBackEndErrors.SQL_BUILDER_UDF_NOT_SUPPORTED_IN_MOD, args);
         }

         PSBackEndColumn col = (PSBackEndColumn)beMap;
         if (!table.equals(col.getTable()))
            continue;

         String columnName = col.getColumn();

         Integer jdbcType = (Integer)dataTypes.get(columnName.toLowerCase());
         if (jdbcType != null) {
            int colType = jdbcType.intValue();
            if (colType == java.sql.Types.BLOB || 
                colType == java.sql.Types.CLOB)
            {
               if (colCount > 0)
                  context.addText(delimiter);

               context.addText(columnName);

               colCount++;
            } else
               continue;
         }
      }

      return (colCount > 0);   // true if at least 1 col; false if 0
   }

   /**
    * Does the specified type map contain Lob types?
    *
    * @param dataTypes The map of data types for this builder.  Can
    *                   be <code>null</code>.
    *
    * @return <code>true</code> if so, <code>false</code> if not
    */
   boolean typeMapContainsLobs(HashMap dataTypes)
   {
      boolean foundLob = false;
      Set colNames = m_Mappings.keySet();
      Iterator iter = colNames.iterator();
      while ( iter.hasNext() && !foundLob )
      {
         String colName = (String) iter.next();
         Integer jdbcType = (Integer) dataTypes.get( colName.toLowerCase());
         if ( null != jdbcType && (jdbcType.equals(BLOB_TYPE_VALUE)
               || jdbcType.equals(CLOB_TYPE_VALUE)))
         {
            foundLob = true;
         }

      }
      return foundLob;
   }

   /**
    * Return the Oracle-specific context for selecting the ROWIDs based
    * on the supplied keys.
    *
    * @param table   The table to insert into.  Never <code>null</code>.
    *
    * @param login   The back end login.  Never <code>null</code>.
    *
    * @param dtHash  The data type map for this table.  Never <code>null</code>.
    *
    * @return  The builder context for retrieving the LOB columns for a single
    *   row in this table.  Never <code>null</code>.
    *
    * @throws  PSIllegalArgumentException If any support method throws this
    *    exception.
    */
   PSSqlBuilderContext getRowIdsFromKeysContext(
      PSBackEndTable table, PSBackEndLogin login, HashMap dtHash)
      throws PSIllegalArgumentException
   {
      /* Build the context for querying ROWIDs based on the keys */
      PSSqlBuilderContext getRowidQueryContext = new PSSqlBuilderContext();

      getRowidQueryContext.addText("SELECT ROWID FROM ");

      buildTableName(login, getRowidQueryContext, table);
      
      // build the WHERE clauses for this table
      buildWhereClauseFromKeys(getRowidQueryContext, table, dtHash);

      // we're done building, so close the last run
      getRowidQueryContext.closeTextRun();

      return getRowidQueryContext;
   }
   
   /**
    * Return the Oracle-specific context for selecting LOB-columns from rows
    * based on ROWID.  The ROWID for this statement will not be bound, but
    * is to be appended to the generated statement at execution time.
    *
    * <I>Example:<I>
    *    <code>
    *    SELECT mydata from mytable WHERE ROWID = 
    *    </code>
    *
    * @param table   The table to insert into.  Never <code>null</code>.
    *
    * @param login   The back end login.  Never <code>null</code>.
    *
    * @param dtHash  The data type map for this table.  Never <code>null</code>.
    *
    * @return  The builder context for retrieving the LOB columns for a single
    *   row in this table.  Never <code>null</code>.
    *
    * @throws  PSIllegalArgumentException If any support method throws this
    *    exception.
    */
   PSSqlBuilderContext getRowRetrievalByRowidContext(
      PSBackEndTable table, PSBackEndLogin login, HashMap dtHash)
      throws PSIllegalArgumentException
   {
      /* Build the context for selecting a row based on a ROWID, 
         which will be used to update the LOB columns */
      PSSqlBuilderContext selectRowForLobUpdateContext = 
         new PSSqlBuilderContext();

      selectRowForLobUpdateContext.addText("SELECT ");
      
      // build column list of lob columns...
      buildLobColumnList(selectRowForLobUpdateContext, table, dtHash, 
         m_Columns, ", ");
         
      selectRowForLobUpdateContext.addText(" FROM ");
      buildTableName(login, selectRowForLobUpdateContext, table);

      // add the where clause for a single rowid, and close the last run
      selectRowForLobUpdateContext.addText(" WHERE ROWID = ", true);

      return selectRowForLobUpdateContext;
   }
   
   /**
    * Return the Oracle-specific context for updating a row based on 
    * rowid.  This will initialize all LOBs.
    *
    * <I>Example:<I>
    *    <code>
    *    UPDATE myTable set mydata=empty_blob() WHERE ROWID = ?
    *    </code>
    *
    * @param table   The table to insert into.  Never <code>null</code>.
    *
    * @param login   The back end login.  Never <code>null</code>.
    *
    * @param dtHash  The data type map for this table.  Never <code>null</code>.
    *
    * @return  The builder context for updating a row in this table.  Never
    *    <code>null</code>.
    *
    * @throws  PSIllegalArgumentException If any support method throws this
    *    exception.
    */
   PSSqlBuilderContext getSingleRowidUpdateContext(
      PSBackEndTable table, PSBackEndLogin login, HashMap dtHash)
      throws PSIllegalArgumentException
   {
      /* Build the context for updating a single row with the rowid */      
      PSSqlBuilderContext singleRowidUpdateContext = 
         new PSSqlBuilderContext();

      /* there's only one table permitted per statement */
      singleRowidUpdateContext.addText("UPDATE ");

      buildTableName(login, singleRowidUpdateContext, table);

      // build the SET clause for this table
      buildSetClause(singleRowidUpdateContext, table, dtHash);

      // this is the WHERE clause for a ROWID
      singleRowidUpdateContext.addText(" WHERE ROWID = ?");
      
      // we're done building, so close the last run
      singleRowidUpdateContext.closeTextRun();

      return singleRowidUpdateContext;
   }
   
   /**
    * Return the Oracle-specific context for inserting rows containing
    * LOBs which can return ROWID.  This will be a callable statement
    * with input parameters for all columns being set and an output
    * parameter which will return the rowid for the inserted row.<B>
    * <I>Example:<I>
    *    <code>
    *    DECLARE rid ROWID; BEGIN insert into mytable (myid, mydata)
    *    VALUES (?, empty_blob()) RETURNING ROWID into rid; ? := rid; EMD;
    *    </code>
    *
    * @param table   The table to insert into.  Never <code>null</code>.
    *
    * @param login   The back end login.  Never <code>null</code>.
    *
    * @param dtHash  The data type map for this table.  Never <code>null</code>.
    *
    * @param columnList The list of columns to insert.  Never <code>null</code>.
    *
    * @return  The builder context for inserting a row into this table.  Never
    *    <code>null</code>.
    *
    * @throws  PSIllegalArgumentException If any support method throws this
    *    exception.
    */
   PSSqlBuilderContext getInsertContext(
      PSBackEndTable table, PSBackEndLogin login, HashMap dtHash,
      List columnList)
      throws PSIllegalArgumentException
   {
      PSSqlBuilderContext context = new PSSqlBuilderContext();

      /* there's only one table here */
      context.addText("DECLARE rid ROWID; BEGIN ");
      context.addText("insert into ");

      buildTableName(login, context, table);

      /* build: (column list) VALUES (place holders) */
      context.addText(" (");

      // go through the columns and keys
      buildColumnList(context, table, dtHash, false, /* use colname */
         columnList);

      context.addText(") VALUES (");

      buildColumnList(context, table, dtHash, true, /* use placeholder */
         columnList);

      context.addText(")");

      context.addText(" RETURNING ROWID into rid; ? := rid; END;");
      
      // we're done building, so close the last run
      context.closeTextRun();

      return context;
   }

   /**
    * Validate the builder and its connection, returning the connection
    * key and filling in the datatypes for the table member on success.
    * 
    * @param   dtHash   The hashmap to fill in with the data types for
    *                   the table which this builder is associated with.
    *                   Never <code>null</code>
    *
    * @param   connKeys The map of connection keys, keyed on driver:server
    *
    * @param   logins   The list of back end logins, indexed by 
    *                   connection key.
    *
    * @return  The valid connection key for this builder.
    *
    * @throws PSIllegalArgumentException  If the builder does not have
    *          one table defined, any argument is invalid or the connection
    *          key is undefined.
    */
   int validateBuilderConnection(HashMap dtHash, Hashtable connKeys, List logins)
      throws PSIllegalArgumentException
   {
      int connKey = super.validateBuilderConnection(dtHash, connKeys, logins);
      
      if (typeMapContainsLobs(dtHash))
      {
         /* Indicate we have Lobs to deal with for override methods */
         m_lobColumnInitializer = PSOracleLobColumnInitializer.getInstance();
      }

      return connKey;
   }

   /**
    * Generate the statement using the specified connection keys.  For
    * Oracle, an update is the same as any other SQL update EXCEPT when
    * there are LOB columns present.  In the case of LOB columns, we must
    * first query the table based on the key columns to retrieve the ROWIDs
    * for the columns we are interested in.  Each row identified by the
    * specified ROWIDs must then be updated once to set the non-LOB columns
    * and to initialize the LOB columns, and then queried so that the
    * LOB columns can be updated with the values we have received to update
    * them with.
    *
    * @param  logins    The list of logins.
    *
    * @param  connKeys  the hash table containing the driver:server
    *                   as the key and the conn number as the value
    *
    * @return The PSUpdate-derived statement to process this update.
    *
    * @throws PSIllegalArgumentException If there are multiple tables 
    * or a PSDataExtractionException occurs.
    */
   PSUpdateStatement generate(java.util.List logins, Hashtable connKeys)
      throws PSIllegalArgumentException
   {
      HashMap dtHash = new HashMap();

      int iConnKey = validateBuilderConnection(dtHash, connKeys, logins);

      PSBackEndLogin login = (PSBackEndLogin)logins.get(iConnKey);

      /* If don't we have lob types to deal with return a normal 
         update statement */
      if (m_lobColumnInitializer == null)
      {
         /* Normal Update */
         return super.generateUpdate(dtHash, iConnKey, 
            login);
      }

      PSBackEndTable table = (PSBackEndTable)m_Tables.get(0);

      try {
         return new PSOracleUpdateStatement(
            iConnKey,
            getSingleRowidUpdateContext(table, login, dtHash).getBlocks(),
            getRowRetrievalByRowidContext(table, login, dtHash).getBlocks(),
            getRowIdsFromKeysContext(table, login, dtHash).getBlocks(),
            PSUpdateStatement.TYPE_UPDATE);
      } catch (PSDataExtractionException e) {
         throw new PSIllegalArgumentException(
            e.getErrorCode(), e.getErrorArguments());
      }
   }

   /**
    * The lob column initializer for this builder.
    * <code>null</code> indicates that this builder has no lob
    * columns.  Set in generate() prior to calling any of the 
    * get***Context methods in PSOracleUpdateBuilder if 
    * the column/placeholder build methods are to supply lob
    * column initializers when creating replacement fields.
    */
   protected IPSLobColumnInitializer m_lobColumnInitializer = null;

   /**
    * The value representing a Blob type value in a data type map.
    */
   private static Integer BLOB_TYPE_VALUE = new Integer(java.sql.Types.BLOB);

   /**
    * The value representing a Clob type value in a data type map.
    */
   private static Integer CLOB_TYPE_VALUE = new Integer(java.sql.Types.CLOB);
}


