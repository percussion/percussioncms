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

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndJoin;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSDateLiteral;
import com.percussion.design.objectstore.PSFunctionCall;
import com.percussion.design.objectstore.PSLiteral;
import com.percussion.design.objectstore.PSLiteralSet;
import com.percussion.design.objectstore.PSNumericLiteral;
import com.percussion.design.objectstore.PSSortedColumn;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSWhereClause;
import com.percussion.util.PSIteratorUtils;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The PSSqlQueryBuilder class is used to build SQL SELECT statements.
 * It can be used to generate single table SELECTs or homogeneous
 * (same DBMS) joined SELECTs. The query optimizer is capable of building
 * heterogeneous (cross DBMS) SELECTs. It calls this class to build
 * each statement and also makes use of the PSQueryJoiner class to join
 * the returned data.
 *
 * @see         PSQueryOptimizer
 * @see         PSQueryJoiner
 */
public class PSSqlQueryBuilder extends PSSqlBuilder
{
   /**
    * Construct a SQL builder to build a SELECT which may be for a
    * single table or to do homogeneous (same DBMS) joins.
    */
   PSSqlQueryBuilder()
   {
      super();
      m_Tables = new ArrayList();
      m_Joins = new ArrayList();
      m_Columns = new ArrayList();
      m_JoinOnlyColumns = new ArrayList();
      m_Wheres = new ArrayList();
      m_Sorts = new ArrayList();
      m_isUnique = false;
   }

   /**
    * Enable or disable using SELECT DISTINCT to generate a unique
    * result set.
    */
   void setUnique(boolean isUnique)
   {
      m_isUnique = isUnique;
   }

   /**
    * Add a table to this SELECT against the specified table.
    */
   void addTable(PSBackEndTable table)
   {
      if (m_Tables.size() > 0) {
         PSBackEndTable refTab = (PSBackEndTable)m_Tables.get(0);
         String refDs = refTab.getDataSource();
         if (refDs == null) refDs = "";

         String newDs = table.getDataSource();
         if (newDs == null) newDs = "";

         // driver/server must be the same as this onlyu
         if (!refTab.isSameDatasource(table))
         {
            throw new IllegalArgumentException(
               "sql builder homogeneous join only" + refDs + " " + newDs);
         }
      }

      /* If the alias name is different, this may allow self-joining. It
       * is imperative that alias names be different as we need a way to
       * differentiate the columns from each instance of the table.
       */
      if (!m_Tables.contains(table))   // don't add the same table twice!
         m_Tables.add(table);
   }

   /**
    * Add a join across two tables (must be homogeneous).
    */
   void addJoin(PSBackEndJoin join)
   {
      // add the left/right tables
      addTable(join.getLeftColumn().getTable());
      addTable(join.getRightColumn().getTable());

      /* add the joins (since table add worked, we know we have valid
       * table info)
       */
      m_Joins.add(join);
   }

   /**
    * Add the specified columns to the SELECT column list as a join-only
    * column. This means it will be included in the SELECT to perform
    * the heterogeneous join, but it will not be included in the
    * joined result set.
    */
   void addHeterogeneousJoinColumn(PSBackEndColumn col)
   {
      // Must uniqua the column for the uniquifier later
      String alias = col.getAlias();
      if ((alias == null) || (alias.equals("")))
      {
         try {
            col.setAlias(col.getColumn());
         } catch (IllegalArgumentException e)
         {
            // We didn't want to alias that badly, so ignore this.
         }
      }

      // if this is a selectable column already, nothing to do
      if (m_Columns.contains(col)) {
         return;
      }

      // otherwise, is it in the join already?
      if (!m_JoinOnlyColumns.contains(col))
      {
         m_JoinOnlyColumns.add(col);
      }

   }

   /**
    * Add the specified columns to the SELECT column list.
    */
   void addSelectColumn(PSBackEndColumn col)
   {
      // Need to uniqua these guys too, or  we may not remove properly
      String alias = col.getAlias();
      if ((alias == null) || (alias.equals("")))
      {
         try {
            col.setAlias(col.getColumn());
         } catch (IllegalArgumentException e)
         {
            // We didn't want to alias that badly, so ignore this.
         }
      }

      if (!m_Columns.contains(col))
      {
         m_Columns.add(col);
      }

      // if this was previously defined as a join only column, remove it
      if (m_JoinOnlyColumns.contains(col))
      {
         m_JoinOnlyColumns.remove(col);
      }
   }

   /**
    * Add a WHERE condition.
    */
   void addWhereClause(PSWhereClause clause)
   {
      if (!m_Wheres.contains(clause))
         m_Wheres.add(clause);
   }

   /**
    * Add a sorted column (ORDER BY).
    */
   void addSortedColumn(PSSortedColumn sort)
   {
      if (!m_Sorts.contains(sort))
         m_Sorts.add(sort);
   }

   /**
    * Get the list of columns which are only for the join (should not
    * be in the returned result set).
    */
   String[] getJoinOnlyColumns()
   {
      return columnArrayToStringArray(m_JoinOnlyColumns);
   }

   /**
    * Generate the statement for the given logins and connection
    * keys.
    *
    * 
    * @param logins a list of logins, one per connection index in the values
    * contained within <code>connKeys</code>, must never be <code>null</code>
    * 
    * @param connKeys a ConcurrentHashMap that associates opaque keys representing
    * a specific database and server, and indecies into the <code>logins</code>
    * list passed to this method, must never be <code>null</code>
    *
    * @return   PSQueryStatement A statement that will execute
    * the query, will never return <code>null</code>
    */
   PSQueryStatement generate(List logins, ConcurrentHashMap connKeys)
   {
      int tableCount = m_Tables.size();
      if (tableCount == 0) {
         throw new IllegalArgumentException("sql builder no back-end tables");
      }

      int joinCount = m_Joins.size();
      if ((tableCount > 1) && (joinCount == 0)) {
         // can't join tables without join conditions
         throw new IllegalArgumentException("sql builder no joins");
      }

      // check that each table participates in a join
      PSQueryOptimizer.validateJoins(m_Tables, m_Joins);

      PSSqlBuilderContext context = new PSSqlBuilderContext();

      /* build the SELECT statement's column list */
      buildSelectColumnList(context);

      /* there's only one table here */
      context.addText(" FROM ");

      // get the driver/server which is used to get our connection key
      PSBackEndTable table = (PSBackEndTable)m_Tables.get(0);
      Object connectionKey = table.getServerKey();
      Integer iConnKey = (Integer)connKeys.get(connectionKey);
      if (iConnKey == null) {
         throw new IllegalArgumentException("sql builder no conn defined for " +
            connectionKey);
      }

      PSBackEndLogin login = (PSBackEndLogin)logins.get(iConnKey.intValue());
      HashMap dtHash = new HashMap();
      if (joinCount > 0)
         buildMultiTableFrom(login, context, dtHash);
      else
         buildSingleTableFrom(login, context, dtHash);

      // build the WHERE
      buildWhereClauses(context, dtHash, 0);

      // and the ORDER BY
      buildOrderBy(context);

      try {
         return new PSQueryStatement(iConnKey.intValue(), context.getBlocks());
      } catch (PSDataExtractionException e) {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }

   /**
    * Get the columns to be used in the SELECT column list as an array.
    */
   String[] getSelectColumnArray()
   {
      int totalSize = m_Columns.size() + m_JoinOnlyColumns.size();
      String[] cols = new String[totalSize];
      PSBackEndColumn col;

      List colArray = m_JoinOnlyColumns;
      int size = colArray.size();
      int colNo = 0;

      for (int i = 0; i < size; i++) {
         col = (PSBackEndColumn)colArray.get(i);
         cols[colNo++] = col.getColumnsForSelect()[0];
      }

      colArray = m_Columns;
      size = colArray.size();
      for (int i = 0; i < size; i++) {
         col = (PSBackEndColumn)colArray.get(i);
         cols[colNo++] = col.getColumnsForSelect()[0];
      }

      return cols;
   }

   /**
    * Get the columns to be used in the SELECT column list as an array.
    */
   String[] columnArrayToStringArray(List colArray)
   {
      int size = colArray.size();
      String[] cols = new String[size];
      PSBackEndColumn col;
      for (int i = 0; i < size; i++) {
         col = (PSBackEndColumn)colArray.get(i);
         cols[i] = col.getColumnsForSelect()[0];
      }

      return cols;
   }

   /**
    * Get the columns which will be used to locate records when executing
    * the associated statement.
    *
    * @return      an array of columns (may be null)
    */
   public PSBackEndColumn[] getLookupColumns()
   {
      /* go through the where clauses to see which columns are the
       * lookup keys
       */
      int size;
      if ((m_Wheres != null) && ((size = m_Wheres.size()) != 0))
      {
         java.util.List colArray = new java.util.ArrayList();

         IPSReplacementValue   replValue;
         for (int i = 0; i < size; i++) {
            PSWhereClause curClause = (PSWhereClause)m_Wheres.get(i);

            replValue = curClause.getVariable();
            if (replValue instanceof com.percussion.design.objectstore.PSBackEndColumn)
            {
               if (!colArray.contains(replValue))
                  colArray.add(replValue);
            }

            replValue = curClause.getValue();
            if (replValue instanceof com.percussion.design.objectstore.PSBackEndColumn)
            {
               if (!colArray.contains(replValue))
                  colArray.add(replValue);
            }
         }

         size = colArray.size();
         if (size > 0) {
            PSBackEndColumn[] ret = new PSBackEndColumn[size];
            colArray.toArray(ret);
            return ret;
         }
      }

      return null;
   }


   private void buildSingleTableFrom(
      PSBackEndLogin login, PSSqlBuilderContext context, HashMap dtHash)
   {
      // single table must be in element 0
      addTableName(login, (PSBackEndTable)m_Tables.get(0), context, dtHash);

      // make sure there's at least once space char after the FROM info
      context.addText(" ");
   }

   /**
    * Are there any outer joins in the join definitions for this builder?
    *
    * @return         <code>true</code> if there are
    */
   public boolean hasOuterJoins()
   {
      int joinCount = m_Joins.size();
      for (int j = 0; j < joinCount; j++) {
         PSBackEndJoin curJoin = (PSBackEndJoin)m_Joins.get(j);

         if (curJoin.isLeftOuterJoin() || curJoin.isRightOuterJoin() ||
            curJoin.isFullOuterJoin())
         {
            return true;
         }
      }

      return false;
   }

   /**
    * Are there any inner joins in the join definitions for this builder?
    *
    * @return         <code>true</code> if there are
    */
   public boolean hasInnerJoins()
   {
      int joinCount = m_Joins.size();
      for (int j = 0; j < joinCount; j++) {
         PSBackEndJoin curJoin = (PSBackEndJoin)m_Joins.get(j);

         if (curJoin.isInnerJoin())
         {
            return true;
         }
      }

      return false;
   }

   private void buildMultiTableFrom(
      PSBackEndLogin login, PSSqlBuilderContext context, HashMap dtHash)
   {
      /* when performing multi-table queries, we have a more complex task
       * to build the statement. We need to see if we have outer joins or
       * only inners. When only inners exist, the syntax is similar to
       * singe table in that we use:
       *
       *    FROM tabl1, table2, ... WHERE join1 AND join2 ...
       *
       * when outers exist, we must use the syntax:
       *
       *    FROM outertab1 OUTER JOIN outertab2 ON join2 OUTER JOIN
       *         outertab3 ON join2, innertab1, innertab2, ...
       *      *      WHERE innerjoin1 AND ...
       */

      /* to accomplish this task, we will create a new sorting of the join
       * columns. In particular, we will move all the outer joins to the
       * front. We must also take care to place all joins using the same
       * tables in the same order. This is so we can create a big chained
       * join (t1 outer join t2 outer join t3 ...). When there's no
       * relationship between the join parts, we then use separate
       * chains (t1 outer join t2, t3 outer join t4, ...)
       */
      m_Joins = PSJoinFormatter.getReorderedJoins(m_Joins);

      /* get the formatting object to help us build the appropriate
       * join syntax for the specified DBMS. This has been added,
       * rather than assuming SQL-92 syntax, to fix bug id Rx-99-11-0048
       */
      java.sql.Connection conn = null;
      try {
         conn = getConnection(login);
         m_joinFormatter = PSJoinFormatter.getJoinFormatter(conn);
      }
      catch (java.sql.SQLException e) 
      {
         // ignore this, though we may want to log it some day
         if (m_joinFormatter == null)   // use the default join formatter
            m_joinFormatter = PSJoinFormatter.getJoinFormatter(null);
      }
      finally
      {
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e1)
            {
               // Ignore
            }
         }
      }

      /* since we can't include the alias name twice in the FROM clause,
       * we need to deal with this by checking if we've already used it.
       */
      List knownTables = new ArrayList();

      final int joinCount = m_Joins.size();
      for (int j = 0; j < joinCount; j++)
      {
         PSBackEndJoin curJoin = (PSBackEndJoin)m_Joins.get(j);

         PSBackEndColumn lCol = curJoin.getLeftColumn();
         PSBackEndColumn rCol = curJoin.getRightColumn();

         PSBackEndTable lTable = lCol.getTable();
         PSBackEndTable rTable = rCol.getTable();

         /* we don't support translators in heterogeneous join conditions
          * the query optimizer should have caught this and done the
          * job through a homogeneous join. we'll throw an exception here
          * just in case we ever hit this end condition.
          */
         if (curJoin.getTranslator() != null) {
            throw new IllegalArgumentException("sql builder xlator unsupported in homogeneous" +
               lTable.getAlias() + " " + rTable.getAlias());
         }

         if (curJoin.isInnerJoin()) {
            addInnerJoinCondition(
               login, context, dtHash, knownTables, curJoin);
            continue;
         }

         if (knownTables.contains(lTable)) {
            if (knownTables.contains(rTable)) {
               /* there is no real construct in SQL92 for dealing with
                * two outer joins across the same set of tables. If both
                * outers are the same type (eg, both left outer) then
                * we may be able to create an ON statement with both
                * conditions. We'll worry about this in the future.
                */
               throw new IllegalArgumentException("sql builder multiple outers not supported" +
                  lTable.getAlias() + " " + rTable.getAlias());
            }

            knownTables.add(rTable);

            m_joinFormatter.addRightTablePrefix(context, curJoin, false);
            addTableName(login, rTable, context, dtHash);
            m_joinFormatter.addRightTableSuffix(context, curJoin, false);
         }
         else if (knownTables.contains(rTable)) {
            knownTables.add(lTable);

            // since we're using RIGHT <outer-join-type> LEFT we need
            // to inverse the definition
            m_joinFormatter.addRightTablePrefix(context, curJoin, true);
            addTableName(login, lTable, context, dtHash);
            m_joinFormatter.addRightTableSuffix(context, curJoin, true);
         }
         else {
            knownTables.add(lTable);
            knownTables.add(rTable);

            // neither is in. is this a disjoint outer?
            if (j > 0)
               context.addText(", ");

            m_joinFormatter.addLeftTablePrefix(context, curJoin, false);
            addTableName(login, lTable, context, dtHash);
            m_joinFormatter.addLeftTableSuffix(context, curJoin, false);

            m_joinFormatter.addRightTablePrefix(context, curJoin, false);
            addTableName(login, rTable, context, dtHash);
            m_joinFormatter.addRightTableSuffix(context, curJoin, false);
         }

         if (m_joinFormatter.usesOnClauseInFrom())
         {
            context.addText(" ON ");
            context.addText(getExpandedColumnName(lTable, true, lCol.getColumn()));
            context.addText(" = ");
            context.addText(getExpandedColumnName(rTable, true, rCol.getColumn()));
         }
      }

      // make sure there's at least once space char after the FROM info
      context.addText(" ");
   }

   private void addInnerJoinCondition(
      PSBackEndLogin login, PSSqlBuilderContext context, HashMap dtHash,
      java.util.List knownTables, PSBackEndJoin join)
   {
      boolean needComma = (knownTables.size() > 0);

      PSBackEndColumn lCol = join.getLeftColumn();
      PSBackEndColumn rCol = join.getRightColumn();

      PSBackEndTable lTable = lCol.getTable();
      PSBackEndTable rTable = rCol.getTable();

      /* we don't support translators in heterogeneous join conditions
       * the query optimizer should have caught this and done the
       * job through a homogeneous join. we'll throw an exception here
       * just in case we ever hit this end condition.
       */
      if (join.getTranslator() != null) {
         throw new IllegalArgumentException("sql builder xlator unsupported in homogeneous" +
            lTable.getAlias() + " " + rTable.getAlias());
      }

      if (!knownTables.contains(lTable)) {
         if (needComma)
            context.addText(", ");
         addTableName(login, lTable, context, dtHash);
         knownTables.add(lTable);
         needComma = true;
      }

      if (!knownTables.contains(rTable)) {
         if (needComma)
            context.addText(", ");
         addTableName(login, rTable, context, dtHash);
         knownTables.add(rTable);
         needComma = true;
      }
   }

   private void buildSelectColumnList(PSSqlBuilderContext context)
   {
      /* OPEN TO INTERPRETATION:
       *
       * we need to know if the mapper is empty due to no columns
       * being selected or all columns being selected. then we don't
       * need to check m_JoinOnlyColumns. In the current implementation,
       * we will assume nothing in m_Columns but something in
       * m_JoinOnlyColumns means the table is being used for joining only
       */
      int size = m_Columns.size() + m_JoinOnlyColumns.size();

      /* if there are no columns, this is a "SELECT *" */
      if (size == 0) {
         if (m_isUnique)
            context.addText("SELECT DISTINCT * ");
         else
            context.addText("SELECT * ");
         return;
      }

      if (m_isUnique)
         context.addText("SELECT DISTINCT ");
      else
         context.addText("SELECT ");

      /* go through the columns and build the appropriate SELECT list */
      addSelectColumnToText(context, m_JoinOnlyColumns, false);
      addSelectColumnToText(context, m_Columns, (m_JoinOnlyColumns.size() != 0));

      // add the columns in the ORDER BY clause to the SELECT list.
      // On Oracle if the query is a SELECT DISTINCT query, then the ORDER BY
      // columns must be included in the SELECT list, otherwise Oracle
      // throws SQL Exception with error code ORA-01791
      //
      // ORA-01791 not a SELECTed expression
      // Cause: There is an incorrect ORDER BY item.
      // The query is a SELECT DISTINCT query with an ORDER BY clause.
      // In this context, all ORDER BY items must be either constants,
      // SELECT list expressions, or expressions whose operands are constants
      // or SELECT list expressions.
      boolean hasCols =
         ((m_JoinOnlyColumns.size() == 0) && (m_Columns.size() == 0)) ?
         false : true;
      List sortedColumns = getSortedColumnsSelectList();
      addSelectColumnToText(context, sortedColumns, hasCols);
   }

   /**
    * Returns the list of columns from the sorted columns list which are not
    * in the list of SELECT columns.
    *
    * @return list of <code>PSSortedColumn</code> objects, never
    * <code>null</code>, may be empty
    */
   private List getSortedColumnsSelectList()
   {
      List sortedCols = new ArrayList();
      Iterator it = m_Sorts.iterator();
      while (it.hasNext())
      {
         PSSortedColumn col = (PSSortedColumn)it.next();
         // cannot use Collections.contains() method since PSSortedColumn
         // returns false if the object is not an instance of PSSortedColumn
         // and both the lists (m_Columns and m_JoinOnlyColumns) contains
         // PSBackEndColumn objects
         Iterator selCols = PSIteratorUtils.joinedIterator(
            m_Columns.iterator(), m_JoinOnlyColumns.iterator());
         boolean found = false;
         while (selCols.hasNext() && !found)
         {
            PSBackEndColumn bakCol = (PSBackEndColumn)selCols.next();
            // Compare the backend columns. Compare the backend tables and
            // the column names. Ignore the column alias for this comparison.
            if (bakCol.isSameColumn(col))
               found = true;
         }
         if (!found)
            sortedCols.add(col);
      }
      return sortedCols;
   }

   private void addSelectColumnToText(
      PSSqlBuilderContext context, List colArray, boolean hasCols)
   {
      boolean supportsColumnAliases = true;
      // todo: get this from the back-end driver
      // dmd.supportsColumnAliasing();

      PSBackEndColumn col;
      String[] colList;
      int size = colArray.size();
      for (int i = 0; i < size; i++) {
         col = (PSBackEndColumn)colArray.get(i);
         colList = col.getColumnsForSelect();

         // this really is impossible, as it must always return 1 column
         // but it can't hurt to check
         if ( (colList == null) || (colList.length == 0) )
            throw new IllegalArgumentException("sql builder no select cols in becol");

         if (hasCols)
            context.addText(", ");
         else
            hasCols = true;

         context.addText(colList[0]);

         // see if we need to alias this guy
         String alias = col.getAlias();
         if ( (alias != null) && (alias.length() > 0) &&
            !alias.equals(col.getColumn()) )
         {
            if (supportsColumnAliases)
            {
               context.addText(" AS " + alias + " ");
            }
            else
            {
               // this is an error, as we have the col name duplicated
               PSBackEndTable t = col.getTable();
               throw new IllegalArgumentException(
                  "sql builder alias unsupported" +
                  t.getDataSource() == null ? "" : t.getDataSource() + " " +  
                     t.getTable() + " " + col.getColumn());
            }
         }
      }
   }

   private void addJoinsToWhereClause(PSSqlBuilderContext context)
   {
      boolean needAnd = false;

      /* we must follow the same logic as the FROM builder. This means we
       * we need to know if we reversed statements, etc.
       */
      final int joinCount = m_Joins.size();

      // we will create a new block for each where clause
      // this provides maximum flexibility for building statements
      // with interspersed omit-when-null conditions
      // We need to add this group if we don't use on clause or
      // we come across an inner join.  Bug Id: Rx-00-07-0007
      if (!m_joinFormatter.usesOnClauseInFrom())
         context.newGroupBlock(true, " AND ");
      else
         for (int j = 0; j < joinCount; j++)
         {
            PSBackEndJoin curJoin = (PSBackEndJoin)m_Joins.get(j);
            if (curJoin.isInnerJoin())
            {
               context.newGroupBlock(true, " AND ");
               break;
            }
         }

      for (int j = 0; j < joinCount; j++)
      {
         PSBackEndJoin curJoin = (PSBackEndJoin)m_Joins.get(j);

         // only inner joins are written here for SQL-92 compliant drivers
         if (m_joinFormatter.usesOnClauseInFrom() && !curJoin.isInnerJoin())
            continue;

         PSBackEndColumn lCol = curJoin.getLeftColumn();
         PSBackEndColumn rCol = curJoin.getRightColumn();

         if (needAnd)
            context.addText(" AND ");
         else
            needAnd = true;

         m_joinFormatter.addLeftColumnPrefix(context, curJoin, false);
         context.addText(lCol.getValueText());
         m_joinFormatter.addLeftColumnSuffix(context, curJoin, false);

         context.addText("=");

         m_joinFormatter.addRightColumnPrefix(context, curJoin, false);
         context.addText(rCol.getValueText());
         m_joinFormatter.addRightColumnSuffix(context, curJoin, false);
      }

      // make sure there's at least once space char after the FROM info
      context.addText(" ");
   }

   /**
    * Adds statement blocks to the specified sql builder context
    * <code>context</code> for each clause following the WHERE keyword in
    * a SELECT statament. If the clause contains database function call, then
    * adds functions block (<code>PSFunctionBlock</code> object) otherwise
    * adds statement block (<code>PSStatementBlock</code> object) to the
    * context.
    * <p>
    * If there is no WHERE clause, then this method simply returns.
    *
    * @param context the sql builder context to which statement blocks are to
    * be added corresponding to each clause following the WHERE keyword in
    * a SELECT statament, assumed not <code>null</code>
    *
    * @param datatypes a map of column names (<code>String</code>) to jdbc
    * data type of the column (<code>Integer</code>), assumed not
    * <code>null</code>, may be empty
    *
    * @param colStart not used
    */
   protected void buildWhereClauses(
      PSSqlBuilderContext context, HashMap datatypes, int colStart)
   {
      if (colStart > 0);
      
      if (m_Wheres == null)
         return;

      final int size = m_Wheres.size();
      final int joinCount = m_Joins.size();

      if ((size == 0) && ((joinCount == 0) ||
         (m_joinFormatter.usesOnClauseInFrom() && !hasInnerJoins())))
      {
         return;   /* we're done if there are no clauses */
      }
      /* the positioning of WHERE and AND conditions is
       * not always set when omitWhenNull is enabled. This is handled
       * by using PSStatementGroup objects to deal with positioning.
       */
      context.newGroup(" WHERE ");

      if (joinCount != 0)
         addJoinsToWhereClause(context);

      int bindColCount = 0;
      for (int i = 0; i < size; i++)
      {
         // now go through the where clauses
         PSWhereClause curClause = (PSWhereClause)m_Wheres.get(i);
         IPSReplacementValue curVar = curClause.getVariable();
         IPSReplacementValue replValue = curClause.getValue();

         // we will create a new block for each where clause
         // this provides maximum flexibility for building statements
         // with interspersed omit-when-null conditions
         int blockType = PSSqlBuilderContext.BLOCK_TYPE_STATEMENT;
         if (((curVar != null) && (curVar instanceof PSFunctionCall)) ||
            ((replValue != null) && (replValue instanceof PSFunctionCall)))
         {
            blockType = PSSqlBuilderContext.BLOCK_TYPE_FUNCTION;
         }
         context.newGroupBlock(
            blockType,
            !curClause.isOmittedWhenNull(),
            " " + curClause.getBoolean() + " ");// need spaces around separator

         String colName = null;

         // flag this as unknown in case it's not found to fix bug id Rx-99-12-0001
         int jdbcType = PSStatementColumn.UNKNOWN_JDBC_TYPE;

         if (curVar instanceof PSFunctionCall)
         {
            context.addReplacementField(curVar, null);
         }
         else
         {
            colName = curVar.getValueText();
            Integer iJdbcType = (Integer)datatypes.get(colName.toLowerCase());
            if (iJdbcType != null)
               jdbcType = iJdbcType.intValue();
            context.addText(colName);
            context.addText(" ");
         }

         // there are some special cases based upon operator, so get it now
         String opCode = curClause.getOperator();

         context.addText(opCode);
         context.addText(" ", true);

         if (curClause.isUnary())
            continue;

         boolean isBetween = false;
         boolean isIn = false;
         if (   opCode.equals(PSConditional.OPTYPE_BETWEEN) ||
               opCode.equals(PSConditional.OPTYPE_NOTBETWEEN) )
         {
            isBetween = true;
         }
         else if (opCode.equals(PSConditional.OPTYPE_IN) ||
                  opCode.equals(PSConditional.OPTYPE_NOTIN) )
         {
            isIn = true;
         }

         if (replValue instanceof PSBackEndColumn)
         {
            // this is not a bound col so don't bump bindColCount
            context.addText(replValue.getValueText());
         }
         else if (replValue instanceof PSFunctionCall)
         {
            // this is not a bound col so don't bump bindColCount
            context.addReplacementField(replValue, null);
         }
         else if (replValue instanceof PSLiteral)
         {
            // this is also not a bound col, but we need to get the
            // formatting right
            PSLiteral lit = (PSLiteral)replValue;

            if (isIn)
            {
               // did this use the correct syntax: "(val1, val2, ...)"
               boolean addedLiteral = false;
               if (lit instanceof PSTextLiteral)
               {
                  String litText = lit.getValueText();
                  if (litText.startsWith("(") && litText.endsWith(")"))
                  {
                     context.addText(litText);
                     addedLiteral = true;
                  }
               }

               if (!addedLiteral)   // needs to be formatted
               {
                  context.addText("(");
                  addLiteralText(context, lit, jdbcType);
                  context.addText(")");
               }
            }
            else if (isBetween)
            {
               // did this use the correct syntax: "val1 AND val2"
               String litText = lit.getValueText();
               // Rx-00-09-0018 - allow mixed case of "And"
               if (litText.toUpperCase().indexOf(" AND ") == -1)
               {
                  // this doesn't have two items, can't be correct!
                  throw new IllegalArgumentException("sql builder litset reqd for op" +
                     opCode + " " + litText);
               }

               context.addText(litText);
            }
            else
            {
               addLiteralText(context, lit, jdbcType);
            }
         }
         else if (replValue instanceof PSLiteralSet)
         {
            // this is also not a bound col, but we need to get the
            // formatting right

            // this had better be an IN or BETWEEN
            if (!isBetween && !isIn)
            {
               throw new IllegalArgumentException("sql builder litset invalid for op" + opCode);
            }

            PSLiteralSet replValueSet = (PSLiteralSet)replValue;
            int literalCount = replValueSet.size();
            if ( isBetween && (literalCount != 2) )
            {
               throw new IllegalArgumentException("sql builder litset wrong size" + opCode +
                  2 + " " + literalCount);
            }
            else if (literalCount == 0)
            {
               // this is not permitted
               throw new IllegalArgumentException("sql builder litset empty" + opCode);
            }

            if (isIn)
            {
               // in uses "(val1, val2, ...)" syntax
               context.addText("(");
            }

            for (int l = 0; l < literalCount; l++)
            {
               if ( l > 0 )
               {
                  // need to add in the separator
                  if (isBetween)
                     context.addText(" AND ");
                  else
                     context.addText(" , ");
               }

               addLiteralText(context, (PSLiteral)replValueSet.get(l), jdbcType);
            }

            if (isIn)
            {
               // in uses "(val1, val2, ...)" syntax
               context.addText(")");
            }
         }
         else
         {
            /* Need a PSBackEndColumn here! */
            PSBackEndColumn col = null;
            if (curVar instanceof PSBackEndColumn)
               col = (PSBackEndColumn) curVar;
            bindColCount++;
            context.addReplacementField(replValue, jdbcType, col);
         }
      }
      context.closeGroup();
   }

   protected void addLiteralText(
      PSSqlBuilderContext context, PSLiteral replValue, int jdbcType)
   {
      /* as per bug id Rx-99-11-0030, now using the Java escape
       * syntax for date, time and timestamp data
       */
      if (replValue instanceof PSDateLiteral)
      {
         // JDBC/ODBC have a set format for date literals
         final String typeString;

         java.lang.Object o;   // use this to store the date
         if (jdbcType == Types.DATE)
         {
            o = new java.sql.Date(
               ((PSDateLiteral)replValue).getDate().getTime());
            typeString = "d";
         }
         else if (jdbcType == Types.TIME)
         {
            o = new java.sql.Time(
               ((PSDateLiteral)replValue).getDate().getTime());
            typeString = "t";
         }
         else   // assume it's Types.TIMESTAMP
         {
            o = new java.sql.Timestamp(
               ((PSDateLiteral)replValue).getDate().getTime());
            typeString = "ts";
         }

         context.addText("{");
         context.addText(typeString);
         context.addText(" '");
         context.addText(o.toString());
         context.addText("'}");
      }
      else if (replValue instanceof PSTextLiteral)
      {
         String strText = replValue.getValueText();
         if (strText.startsWith("'") && strText.endsWith("'"))
         {
            // assume they've properly formatted the string
            context.addText(strText);
         }
         else
         {
            // we need to quote it, and fixup all quote references
            context.addText("'");
            int lastPos = 0;
            int lastValidPos = strText.length() - 1;
            for (   int pos = strText.indexOf('\'');;
                  pos = strText.indexOf('\'', lastPos) )
            {
               if (pos == -1)
               {
                  if (lastPos == 0)
                     context.addText(strText);
                  else
                     context.addText(strText.substring(lastPos));
                  break;
               }

               if (pos == lastValidPos)
               {
                  context.addText(strText.substring(lastPos));
                  context.addText("\'");   // only need one quote for this
                  break;
               }

               pos++;   // include the quote char (also good for next run's lastPos)
               context.addText(strText.substring(lastPos, pos - lastPos));
               context.addText("\'");
               lastPos = pos;
            }
            context.addText("'");
         }
      }
      else // PSNumericLiteral - these go through unquoted, etc.
      {
         /* Need to quote these when the comparison is against a
            text-based column, otherwise, we need to avoid using
            getValueText() as it can add commas, etc. to the format
            which will cause problems when comparing against a
            numeric type column... (Bug Id: Rx-99-10-0197)*/
         switch (jdbcType)
         {
            case java.sql.Types.CHAR:
            case java.sql.Types.CLOB:
            case java.sql.Types.LONGVARCHAR:
            case java.sql.Types.VARCHAR:
               context.addText("\'");
               context.addText(replValue.getValueText());
               context.addText("\'");
            break;
            default:
               PSNumericLiteral numLit = (PSNumericLiteral) replValue;
               context.addText(numLit.getNumber().toString());
            break;
         }
      }
   }

   private void buildOrderBy(PSSqlBuilderContext context)
   {
      int size = m_Sorts.size();
      String sTemp;

      /* now go through the sorted columns */
      if (size != 0) {
         /* always start a new block for sort orders,
          * in case there were conditional where clauses
          */
         context.newBlock(true);
         context.addText(" ORDER BY ");

         PSSortedColumn sort;
         for (int i = 0; i < size; ) {
            sort = (PSSortedColumn)m_Sorts.get(i);
            sTemp =
               getExpandedColumnName(sort.getTable(), true, sort.getColumn()) +
               (sort.isAscending() ? " ASC" : " DESC");
            context.addText(sTemp);

            i++; /* bump it now to simplify the compare */
            if (i < size) {   /* add the delimiter */
               context.addText(", ", true);
            }
         }
      }

      context.closeTextRun();
   }


   private List               m_Tables;
   private List               m_Joins;
   private List               m_JoinOnlyColumns;
   private List               m_Columns;
   private List               m_Wheres;
   private List               m_Sorts;
   private boolean            m_isUnique;
   private PSJoinFormatter      m_joinFormatter = null;
}

