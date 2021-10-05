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

import com.percussion.design.objectstore.PSBackEndJoin;
import com.percussion.design.objectstore.PSBackEndTable;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * The PSJoinFormatter class is used for building
 * joins in SQL SELECT statements. Join syntax, such as for outer joins,
 * can be complex and differ across drivers.
 * <p>Derivitives of this class can also be used to help you create your own
 * join clause from pieces. For example, a SELECT statement containing a join
 * clause could be created as follows. By using the appropriate join formatter,
 * this statement would be valid on any platform.<p>
 * Example<p>
 * <pre>
 *          queryString.append(
 *             "SELECT t2.log_appl, t2.log_type, t2.log_id_high, t2.log_id_low,");
 *        queryString.append(
 *             " t2.log_seq, t2.log_subseq, t2.log_subt, t2.log_data from ");
 *
 *        queryString.append(
 *           joinFormatter.getLeftTablePrefix(joinType) +
 *           "Table1 t1" +
 *           joinFormatter.getLeftTableSuffix(joinType) +
 *           joinFormatter.getRightTablePrefix(joinType) +
 *           "Table2 t2" +
 *           joinFormatter.getRightTableSuffix(joinType));
 *
 *        String firstColJoin =
 *           joinFormatter.getLeftColumnPrefix(joinType) +
 *           "t1.log_id_high" +
 *           joinFormatter.getLeftColumnSuffix(joinType) +
 *           "=" +
 *           joinFormatter.getRightColumnPrefix(joinType) +
 *           "t2.log_id_high" +
 *           joinFormatter.getRightColumnSuffix(joinType);
 *
 *        String secondColJoin =
 *           joinFormatter.getLeftColumnPrefix(joinType) +
 *           "t1.log_id_low" +
 *           joinFormatter.getLeftColumnSuffix(joinType) +
 *           "=" +
 *           joinFormatter.getRightColumnPrefix(joinType) +
 *           "t2.log_id_low" +
 *           joinFormatter.getRightColumnSuffix(joinType);
 *
 *        if (joinFormatter.useOnClauseInFrom())
 *            queryString.append(
 *             " ON " + firstColJoin + " AND " + secondColJoin);
 *        else
 *        {
 *             queryString.append(" WHERE ");
 *           andClause = " AND ";
 *             queryString.append(firstColJoin + andClause + secondColJoin);
 *        }
 * </pre>
 * When building your statement, clause combiners (such as WHERE and AND)
 * should be surrounded by spaces, the table names (with optional aliases), 
 * operators, and column names must have <em>NO</em> spaces around them.
 *
 * @see         PSSqlQueryBuilder
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public abstract class PSJoinFormatter
{
   /**
    * Get the appropriate join formatter for the specified connection.
    * The Connection object is used only within the constructor to
    * determine the DBMS type. The caller can release this when the
    * constructor returns without concern for problems with this class.
    */
   public static PSJoinFormatter getJoinFormatter(java.sql.Connection dbConn)
   {
      if (dbConn != null)
      {
         try {
            java.sql.DatabaseMetaData dbMeta = dbConn.getMetaData();
            String dbmsType = dbMeta.getDatabaseProductName();
            if (dbmsType == null) dbmsType = "";
            dbmsType = dbmsType.toUpperCase();

            if (dbmsType.indexOf("ORACLE") != -1)
            {
               return new PSOracleJoinFormatter();
            }
         } catch (java.sql.SQLException e) {
            // treat this is a warning, not an error
         }
      }

      // by default, we use the SQL-92 syntax
      return new PSSql92JoinFormatter();
   }

   public static java.util.List getReorderedJoins(java.util.List joins)
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
       *      WHERE innerjoin1 AND ...
       */

      /* to accomplish this task, we will create a new sorting of the join
       * columns. In particular, we will move all the outer joins to the
       * front. We must also take care to place all joins using the same
       * tables in the same order. This is so we can create a big chained
       * join (t1 outer join t2 outer join t3 ...). When there's no
       * relationship between the join parts, we then use separate
       * chains (t1 outer join t2, t3 outer join t4, ...)
       */
      HashMap tableIndex = new HashMap();
      int joinCount = joins.size();
      ArrayList reorderedJoins = new ArrayList();
      int lastOuterJoin = 0;
      for (int j = 0; j < joinCount; j++) {
         PSBackEndJoin join = (PSBackEndJoin)joins.get(j);
         if (join.isLeftOuterJoin() || join.isRightOuterJoin() ||
            join.isFullOuterJoin())
         {
            PSBackEndTable lTable = join.getLeftColumn().getTable();
            Integer lPos = (Integer)tableIndex.get(lTable);

            PSBackEndTable rTable = join.getRightColumn().getTable();
            Integer rPos = (Integer)tableIndex.get(rTable);

            int storeAtPos;
            if (lPos != null)
               storeAtPos = lPos.intValue() + 1;
            else if (rPos != null)
               storeAtPos = rPos.intValue() + 1;
            else
               storeAtPos = lastOuterJoin;

            reorderedJoins.add(storeAtPos, join);
            lastOuterJoin++;

            if (lPos == null)
               tableIndex.put(lTable, new Integer(storeAtPos));
            if (rPos == null)
               tableIndex.put(rTable, new Integer(storeAtPos));
         }
         else   // always add inner joins to the end
            reorderedJoins.add(join);
      }

      return reorderedJoins;
   }

   /**
    * Construct a join formatter.
    */
   protected PSJoinFormatter()
   {
      super();
   }

   /**
    * Add any prefix required in the FROM clause for the left side table.
    *
    * @param   context               the context to write to
    *
    * @param   join                  the join to operate on
    *
    * @param   inverseColumnOrder   if <code>true</code>, treat the left
    *                                 column as the right and vice versa
    */
   public abstract void addLeftTablePrefix(
      PSSqlBuilderContext context,
      PSBackEndJoin join,
      boolean inverseColumnOrder);

   /**
    * Add any suffix required in the FROM clause for the left side table.
    *
    * @param   context               the context to write to
    *
    * @param   join                  the join to operate on
    *
    * @param   inverseColumnOrder   if <code>true</code>, treat the left
    *                                 column as the right and vice versa
    */
   public abstract void addLeftTableSuffix(
      PSSqlBuilderContext context,
      PSBackEndJoin join,
      boolean inverseColumnOrder);

   /**
    * Add any prefix required in the FROM clause for the right side table.
    *
    * @param   context               the context to write to
    *
    * @param   join                  the join to operate on
    *
    * @param   inverseColumnOrder   if <code>true</code>, treat the left
    *                                 column as the right and vice versa
    */
   public abstract void addRightTablePrefix(
      PSSqlBuilderContext context,
      PSBackEndJoin join,
      boolean inverseColumnOrder);

   /**
    * Add any suffix required in the FROM clause for the right side table.
    *
    * @param   context               the context to write to
    *
    * @param   join                  the join to operate on
    *
    * @param   inverseColumnOrder   if <code>true</code>, treat the left
    *                                 column as the right and vice versa
    */
   public abstract void addRightTableSuffix(
      PSSqlBuilderContext context,
      PSBackEndJoin join,
      boolean inverseColumnOrder);

   /**
    * Does this store the column joining information using an ON clause
    * in the FROM clause? This is the syntax used by SQL-92 compliant
    * drivers. If this is not the case, the column joining information
    * is stored in the WHERE clause.
    *
    * @return <code>true</code> if ON clauses are used in the FROM clause
    */
   public abstract boolean usesOnClauseInFrom();

   /**
    * Add any prefix required in the WHERE clause for the left side column.
    *
    * @param   context               the context to write to
    *
    * @param   join                  the join to operate on
    *
    * @param   inverseColumnOrder   if <code>true</code>, treat the left
    *                                 column as the right and vice versa
    */
   public abstract void addLeftColumnPrefix(
      PSSqlBuilderContext context,
      PSBackEndJoin join,
      boolean inverseColumnOrder);

   /**
    * Add any suffix required in the WHERE clause for the left side column.
    *
    * @param   context               the context to write to
    *
    * @param   join                  the join to operate on
    *
    * @param   inverseColumnOrder   if <code>true</code>, treat the left
    *                                 column as the right and vice versa
    */
   public abstract void addLeftColumnSuffix(
      PSSqlBuilderContext context,
      PSBackEndJoin join,
      boolean inverseColumnOrder);

   /**
    * Add any prefix required in the WHERE clause for the right side column.
    *
    * @param   context               the context to write to
    *
    * @param   join                  the join to operate on
    *
    * @param   inverseColumnOrder   if <code>true</code>, treat the left
    *                                 column as the right and vice versa
    */
   public abstract void addRightColumnPrefix(
      PSSqlBuilderContext context,
      PSBackEndJoin join,
      boolean inverseColumnOrder);

   /**
    * Add any suffix required in the WHERE clause for the right side column.
    *
    * @param   context               the context to write to
    *
    * @param   join                  the join to operate on
    *
    * @param   inverseColumnOrder   if <code>true</code>, treat the left
    *                                 column as the right and vice versa
    */
   public abstract void addRightColumnSuffix(
      PSSqlBuilderContext context,
      PSBackEndJoin join,
      boolean inverseColumnOrder);

   /*
    * Functions to retrieve join-based information, for non-
    * builder related query creation.  Addresses bug Id: Rx-00-10-0003
    */

   /**
    * Get the Prefix for the right column, based on the join type
    *
    * @param   joinType    The type of join being performed. One of the
    *          <code>PSJoinFormatter.JOIN_TYPE...</code> types.
    *
    * @return  The right column prefix to be placed prior to the
    *          right column in the query, with appropriate spacing.
    *          Never <code>null</code>.
    *
    * @throws  IllegalArgumentException if the join type is required,
    *          but invalid
    */
   public abstract String getRightColumnPrefix(
      int joinType);

   /**
    * Get the Prefix for the left column, based on the join type
    *
    * @param   joinType    The type of join being performed. One of the
    * <code>PSJoinFormatter.JOIN_TYPE...</code> types.
    *
    * @return  The left column prefix to be placed prior to the
    *          left column in the query, with appropriate spacing.
    *          Never <code>null</code>
    *
    * @throws  IllegalArgumentException if the join type is required,
    *          but invalid
    */
   public abstract String getLeftColumnPrefix(
      int joinType);

   /**
    * Get the Suffix for the right column, based on the join type
    *
    * @param   joinType    The type of join being performed. One of the
    * <code>PSJoinFormatter.JOIN_TYPE...</code> types.
    *
    * @return  The right column suffix to be placed prior to the
    *          right column in the query, with appropriate spacing.
    *          Never <code>null</code>
    *
    * @throws  IllegalArgumentException if the join type is required,
    *          but invalid
    */
   public abstract String getRightColumnSuffix(
      int joinType);

   /**
    * Get the Suffix for the left column, based on the join type
    *
    * @param   joinType    The type of join being performed. One of the
    * <code>PSJoinFormatter.JOIN_TYPE...</code> types.
    *
    * @return  The left column suffix to be placed prior to the
    *          left column in the query, with appropriate spacing.
    *          Never <code>null</code>
    *
    * @throws  IllegalArgumentException if the join type is required,
    *          but invalid
    */
   public abstract String getLeftColumnSuffix(
      int joinType);

   /**
    * Get the Prefix for the left table, based on the join type
    *
    * @param   joinType    The type of join being performed. One of the
    * <code>PSJoinFormatter.JOIN_TYPE...</code> types.
    *
    * @return  The left table prefix to be placed prior to the
    *          left table in the query, with appropriate spacing.
    *          Never <code>null</code>
    *
    * @throws  IllegalArgumentException if the join type is required,
    *          but invalid
    */
   public abstract String getLeftTablePrefix(
      int joinType);

   /**
    * Get the Prefix for the right table, based on the join type
    *
    * @param   joinType    The type of join being performed. One of the
    * <code>PSJoinFormatter.JOIN_TYPE...</code> types.
    *
    * @return  The right table prefix to be placed prior to the
    *          right table in the query, with appropriate spacing.
    *          Never <code>null</code>
    *
    * @throws  IllegalArgumentException if the join type is required,
    *          but invalid
    */
   public abstract String getRightTablePrefix(
      int joinType);

   /**
    * Get the Suffix for the left table, based on the join type
    *
    * @param   joinType    The type of join being performed. One of the
    * <code>PSJoinFormatter.JOIN_TYPE...</code> types.
    *
    * @return  The left table suffix to be placed prior to the
    *          left table in the query, with appropriate spacing.
    *          Never <code>null</code>
    *
    * @throws  IllegalArgumentException if the join type is required,
    *          but invalid
    */
   public abstract String getLeftTableSuffix(
      int joinType);

   /**
    * Get the Suffix for the right table, based on the join type
    *
    * @param   joinType    The type of join being performed. One of the
    * <code>PSJoinFormatter.JOIN_TYPE...</code> types.
    *
    * @return  The right table suffix to be placed prior to the
    *          right table in the query, with appropriate spacing.
    *          Never <code>null</code>
    *
    * @throws  IllegalArgumentException if the join type is required,
    *          but invalid
    */
   public abstract String getRightTableSuffix(
      int joinType);

   /* The join types supported for the string-based query
      creation methods */

   /**
    * Used to specify the join type when getting various pieces of a join
    * clause. This value is used for an Inner join. See the <code>get...Prefix
    * </code> and <code>get...Suffix</code> methods.
    */
   public static int JOIN_TYPE_INNER = 1;

   /**
    * Used to specify the join type when getting various pieces of a join
    * clause. This value is used for a left outer join. See the <code>get...Prefix
    * </code> and <code>get...Suffix</code> methods.
    */
   public static int JOIN_TYPE_LEFT_OUTER = 2;

   /**
    * Used to specify the join type when getting various pieces of a join
    * clause. This value is used for a right outer join. See the <code>get...Prefix
    * </code> and <code>get...Suffix</code> methods.
    */
   public static int JOIN_TYPE_RIGHT_OUTER = 3;

   /**
    * Used to specify the join type when getting various pieces of a join
    * clause. This value is used for a full outer join. See the <code>get...Prefix
    * </code> and <code>get...Suffix</code> methods.
    */
   public static int JOIN_TYPE_FULL_OUTER = 4;
}

