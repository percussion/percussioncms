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


/**
 * Join formatter for Sql92.
 *
 * @see         PSJoinFormatter
 * @see         PSSqlQueryBuilder
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSSql92JoinFormatter extends PSJoinFormatter
{
   /**
    * Construct a join formatter for Sql92.
    */
   public PSSql92JoinFormatter()
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
      if (join.isFullOuterJoin())
         context.addText(" FULL OUTER JOIN ");
      else if (inverseColumnOrder)
      {
         if (join.isLeftOuterJoin())
            context.addText(" RIGHT OUTER JOIN ");
         else if (join.isRightOuterJoin())
            context.addText(" LEFT OUTER JOIN ");
         else
            context.addText(", ");
      }
      else
      {
         if (join.isLeftOuterJoin())
            context.addText(" LEFT OUTER JOIN ");
         else if (join.isRightOuterJoin())
            context.addText(" RIGHT OUTER JOIN ");
         else
            context.addText(", ");
      }
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
      // this driver uses the FROM clause for the column joining info
      return true;
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
      // nothing to do here for this driver
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
      // nothing to do here for this driver
   }

   /*
    * Functions to retrieve join-based information, for non-
    * builder related query creation
    */

   public String getRightColumnPrefix(
      int joinType)
   {
      return "";
   }

   public String getLeftColumnPrefix(
      int joinType)
   {
      return "";
   }

   public String getRightColumnSuffix(
      int joinType)
   {
      return "";
   }

   public String getLeftColumnSuffix(
      int joinType)
   {
      return "";
   }

   public String getLeftTablePrefix(
      int joinType)
   {
      return "";
   }

   public String getRightTablePrefix(
      int joinType)
   {
      if (joinType == JOIN_TYPE_FULL_OUTER)
         return " FULL OUTER JOIN ";
      else if (joinType == JOIN_TYPE_LEFT_OUTER)
         return " LEFT OUTER JOIN ";
      else if (joinType == JOIN_TYPE_RIGHT_OUTER)
         return " RIGHT OUTER JOIN ";
      else
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

