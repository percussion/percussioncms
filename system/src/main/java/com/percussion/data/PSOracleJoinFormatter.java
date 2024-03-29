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

import com.percussion.design.objectstore.PSBackEndJoin;


/**
 * Join formatter for Oracle.
 *
 * @see         PSJoinFormatter
 * @see         PSSqlQueryBuilder
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSOracleJoinFormatter extends PSJoinFormatter
{
   /**
    * Construct a join formatter for Oracle.
    */
   public PSOracleJoinFormatter()
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
   public void addLeftTablePrefix(
      PSSqlBuilderContext context,
      PSBackEndJoin join,
      boolean inverseColumnOrder)
   {
      // nothing to do here for this driver
   }

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
   public void addLeftTableSuffix(
      PSSqlBuilderContext context,
      PSBackEndJoin join,
      boolean inverseColumnOrder)
   {
      // nothing to do here for this driver
   }

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
   public void addRightTablePrefix(
      PSSqlBuilderContext context,
      PSBackEndJoin join,
      boolean inverseColumnOrder)
   {
      // the "prefix" for the right table is a comma to separate it from
      // the left column
      context.addText(", ");
   }

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
   public void addRightTableSuffix(
      PSSqlBuilderContext context,
      PSBackEndJoin join,
      boolean inverseColumnOrder)
   {
      // nothing to do here for this driver
   }

   /**
    * Does this store the column joining information using an ON clause
    * in the FROM clause? This is the syntax used by SQL-92 compliant
    * drivers. If this is not the case, the column joining information
    * is stored in the WHERE clause.
    *
    * @return      <code>true</code> if ON clauses are used in the FROM clause
    */
   public boolean usesOnClauseInFrom()
   {
      // this driver uses the WHERE clause for the column joining info
      return false;
   }

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
   public void addLeftColumnPrefix(
      PSSqlBuilderContext context,
      PSBackEndJoin join,
      boolean inverseColumnOrder)
   {
      // nothing to do here for this driver
   }

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
   public void addLeftColumnSuffix(
      PSSqlBuilderContext context,
      PSBackEndJoin join,
      boolean inverseColumnOrder)
   {
      // if we're doing a right or full outer join, we need to add the marker
      if (   join.isFullOuterJoin() ||
            (inverseColumnOrder && join.isLeftOuterJoin()) ||
            (!inverseColumnOrder && join.isRightOuterJoin()) )
         context.addText(" (+) ");
      else
         context.addText(" ");
   }

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
   public void addRightColumnPrefix(
      PSSqlBuilderContext context,
      PSBackEndJoin join,
      boolean inverseColumnOrder)
   {
      // nothing to do here for this driver
      context.addText(" ");
   }

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
   public void addRightColumnSuffix(
      PSSqlBuilderContext context,
      PSBackEndJoin join,
      boolean inverseColumnOrder)
   {
      // if we're doing a left or full outer join, we need to add the marker
      if (   join.isFullOuterJoin() ||
            (!inverseColumnOrder && join.isLeftOuterJoin()) ||
            (inverseColumnOrder && join.isRightOuterJoin()) )
         context.addText(" (+) ");
   }

   /*
    * Functions to retrieve join-based information, for non-
    * builder related query creation
    */

   public String getRightColumnPrefix(
      int joinType)
   {
      return " ";
   }

   public String getLeftColumnPrefix(
      int joinType)
   {
      return "";
   }

   public String getRightColumnSuffix(
      int joinType)
   {
      // if we're doing a left or full outer join, we need to add the marker
      if (   joinType == JOIN_TYPE_FULL_OUTER ||
            joinType == JOIN_TYPE_LEFT_OUTER)
         return " (+) ";

      return "";
   }

   public String getLeftColumnSuffix(
      int joinType)
   {
      // if we're doing a right or full outer join, we need to add the marker
      if (   joinType == JOIN_TYPE_FULL_OUTER ||
            joinType == JOIN_TYPE_RIGHT_OUTER )
         return " (+) ";
      else
         return " ";
   }

   public String getLeftTablePrefix(
      int joinType)
   {
      return "";
   }

   public String getRightTablePrefix(
      int joinType)
   {
      return ", ";
   }

   public String getLeftTableSuffix(
      int joinType)
   {
      return "";
   }

   public String getRightTableSuffix(
      int joinType)
   {
      return "";
   }
}

