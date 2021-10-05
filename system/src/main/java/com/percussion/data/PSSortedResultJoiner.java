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
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.PSApplicationHandler;

import java.sql.ResultSet;

/**
 * The PSSortedResultJoiner class is used internally by the E2 server to merge
 * the results of heterogenous queries into a single, joined result set.
 * <p>
 * The Query Optimizer (PSQueryOptimizer) chooses an execution plan which
 * will optimally access the data from each back-end. It also defines
 * the join requirements. The Query Joiner takes the result sets from each
 * query along with the join requirements and produces the merged result set.
 *
 * @see        PSQueryOptimizer
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSSortedResultJoiner extends PSQueryJoiner
{
   /**
    * Construct a joiner to handle the speciifed result sets.
    *
    * @param      ah         the application handler containing this join
    *
    * @param      join      the join condition
    *
    * @param      lCols      the columns to select from the left side table
    *
    * @param      lOmitCols   the left columns for join only (will be
    *                        omitted from the result set)
    *
    * @param      rCols      the columns to select from the right side table
    *
    * @param      rOmitCols   the right columns for join only (will be
    *                        omitted from the result set)
    *
    * @param      expectedSelectivity
    *                        the number of rows we expect to be in a typical
    *                        result set
     */
   public PSSortedResultJoiner(
      PSApplicationHandler ah, PSBackEndJoin join,
      String[] lCols, String[] lOmitCols,
      String[] rCols, String[] rOmitCols,
      int expectedSelectivity
      )
      throws PSNotFoundException, PSExtensionException
   {
      super(ah, join, lCols, lOmitCols, rCols, rOmitCols, expectedSelectivity);
   }


   /* ************  IPSExecutionStep Interface Implementation ************ */

   /**
    * Execute the join of two or more result sets as a step in the
    * execution plan. Each join may act on a subset of the result sets
    * defined in the execution data. It will then remove the result sets
    * and store the merged result set.
    *
    * @param   data     execution data is a container for the input data
    *                   as well as a collection of result sets generated
    *                   by queries.
    *
    * @exception   SQLException
    *                     if a SQL error occurs
    */
   public void execute(PSExecutionData data)
      throws java.sql.SQLException,
         com.percussion.data.PSDataExtractionException,
         com.percussion.error.PSErrorException
   {
      /* there had better be at least two result sets on the stack */
      java.util.Stack stack = data.getResultSetStack();
      if (stack.size() < 2) {
         throw new PSSqlException(
            IPSDataErrors.SORTED_JOINER_2_RESULT_SETS_REQD,
            new Integer(stack.size()), "25000");
      }

      // pop off the result sets (right is on top, then left)
      ResultSet rRS = (ResultSet)stack.pop();
      ResultSet lRS = (ResultSet)stack.pop();

      // find index of left join column
      m_leftColumnIndex = PSBackEndColumnExtractor.getColumnOrdinal(m_leftColumn, lRS.getMetaData());
      if (m_leftColumnIndex < 1) {
         throw new PSSqlException(
            IPSDataErrors.SORTED_JOINER_LCOL_NOT_FOUND, m_leftColumn, "33000");
      }
      else
         m_leftColumnIndex--;   // need this 0 based

      // find index of right join column
      m_rightColumnIndex = PSBackEndColumnExtractor.getColumnOrdinal(m_rightColumn, rRS.getMetaData());
      if (m_rightColumnIndex < 1) {
         throw new PSSqlException(
            IPSDataErrors.SORTED_JOINER_RCOL_NOT_FOUND, m_rightColumn, "33000");
      }
      else {
         m_rightColumnIndex
            = lRS.getMetaData().getColumnCount() + (m_rightColumnIndex - 1);
      }

      try {
         PSJoinedRowDataBuffer rowData = new PSJoinedRowDataBuffer(
            lRS, rRS, m_columnHash, m_omitColumns,
            java.lang.Math.max(m_expectedSelectivity, m_averageSelectivity));

         // verify that we have the appropriate column count
         if (rowData.getColumnCount() != m_columnCount) {
            Object [] args = { String.valueOf(m_columnCount),
               String.valueOf(rowData.getColumnCount()) };
            throw new PSSqlException(
               IPSDataErrors.SORTED_JOINER_COL_COUNT_MISMATCH, args, "07008");
         }

         // in case we're using UDFs, set the ResultSetMetaData and current row
         if (m_translator != null) {
            data.setCurrentResultSetMetaData(lRS.getMetaData());
            data.setCurrentResultRowData(rowData.getCurrentRow());
         }

         // do the logic to process all the row data
         processData(data, rowData);

         // we're done, push the result set on the stack
         stack.push(rowData.getJoinedResultSet());
      } finally {
         if (m_closeInputs)
         {
            // close the result sets, we're done with them
            lRS.close();
            rRS.close();
         }
      }
   }


   private void processData(
      PSExecutionData execData, PSJoinedRowDataBuffer rowData)
      throws java.sql.SQLException,
         com.percussion.data.PSDataExtractionException
   {
      /* now's the tough stuff. we need to go through each row of data
       * and see whether or not we have a match, if we need special
       * joining, etc.
       */

      // get the data buffer of the current row for fast access
      Object[] curRow = rowData.getCurrentRow();

      // flag when we've hit end of set
      boolean readLeft = rowData.readLeftRow();
      boolean readRight = rowData.readRightRow();

      while (readLeft || readRight) {
         /* are we done prematurely due to one side having no data? */
         if ((!readLeft) && (m_joinType == LEFT_OUTER_JOIN))
            break;
         else if ((!readRight) && (m_joinType == RIGHT_OUTER_JOIN))
            break;
         else if ((!readLeft || !readRight) && (m_joinType == INNER_JOIN))
            break;

         /* now's the tough stuff. we need to go through each row of data
          * and see whether or not we have a match, if we need special
          * joining, etc.
          */
         int comparator = compareKeys(execData, curRow, readLeft, readRight);
         if (comparator == 0) {
            // this will store our current row's data, and also deal with
            // a many:many case
            processManyToMany(execData, rowData);
         }
         else if (comparator < 0) {
            // if the left side is smaller
            // see if we're doing an outer join on it
            if (   (m_joinType == LEFT_OUTER_JOIN) ||
                  (m_joinType == FULL_OUTER_JOIN) ) {
               rowData.addRowNullRight(curRow);   // add the row we're on
            }
         }
         else {   // comparator > 0
            // if the right side is smaller
            // see if we're doing an outer join on it
            if (   (m_joinType == RIGHT_OUTER_JOIN) ||
                  (m_joinType == FULL_OUTER_JOIN) ) {
               rowData.addRowNullLeft(curRow);   // add the row we're on
            }
         }

         /* if we had a match, we need to see if position the left or
          * right side to the next row will still be a match
          */
//         if (comparator == 0)
//            comparator = readWhat(execData, rowData);

         /* if there's still data on one side and we had rows which
          * favor a read of that side, read the next row from the result set
          */
         if (readLeft && (comparator <= 0))
            readLeft = rowData.readLeftRow();
         if (readRight && (comparator >= 0))
            readRight = rowData.readRightRow();
      }

      /* get the count of rows we've processed, which we keep track of
       * to dynamically adjust for selectivity estimation errors
       */
      updateAverageSelectivity(rowData.getRowsAdded());
   }


   /**
    * compare the peek ahead rows with the current rows to determine which
    * side should be repositioned. If we need to read the next left side
    * row, -1 is returned; right side row 1 is returned; both rows 0.
    */
   private int readWhat(PSExecutionData execData, PSJoinedRowDataBuffer buf)
      throws com.percussion.data.PSDataExtractionException,
      java.sql.SQLException
   {
      Object[] curRow = buf.getCurrentRow();
      Object[] nextRow = buf.getPeekAheadRow();

      /* if there's no next for both sides, move both to the next row
       * to end the query
       */
      boolean lHasNext = buf.hasLeftPeekAheadRow();
      boolean rHasNext = buf.hasRightPeekAheadRow();
      if (!lHasNext && !rHasNext)
         return 0;

      /* we'll check the following cases:
       *
       *   - 1:many => does the current left row = the next right row?
       *
       * - many:1 => does the next left row = the current right row?
       *
       * - 1:1 => does the next left row = the next right row?
       *
       * - many:many => this case was handled elsewhere, don't worry about it
       */
      int comparator = 0;
      Object lValue = null;   // used for later comparison

      if (rHasNext) {
         if (m_translator != null)
            lValue = m_translator.extract(execData);
         else
            lValue = curRow[m_leftColumnIndex];

         comparator = compareKeyValues(lValue, nextRow[m_rightColumnIndex]);
         if (comparator == 0)   // this is the 1:many case
            return 1;
      }

      if (lHasNext) {
         if (m_translator != null) {
            // we always want it on the current row, so do this temporarily
             execData.setCurrentResultRowData(nextRow);
            lValue = m_translator.extract(execData);
             execData.setCurrentResultRowData(curRow);
         }
         else
            lValue = nextRow[m_leftColumnIndex];

         comparator = compareKeyValues(lValue, curRow[m_rightColumnIndex]);
         if (comparator == 0)   // this is the many:1 case
            return -1;   // read the next left row
      }

      /* now see if we have the 1:1 case on the next row. If so, read
       * both. If not, read the next row from the side with the
       * smallest value
       */
      if (lHasNext && rHasNext)
         comparator = compareKeyValues(lValue, nextRow[m_rightColumnIndex]);
      else
         comparator = 0;   // move both forward

      return comparator;
   }

   /**
    * after a match has been found, we check if we're in a many:many case
    * so we can set the mark on the result set appropriately.
    *
    * @return      the number of rows processed
    *
    * @see         PSQueryJoiner#compareKeys
    */
   private int processManyToMany(
      PSExecutionData execData, PSJoinedRowDataBuffer buf)
      throws PSDataExtractionException, java.sql.SQLException
   {
      int rowsProcessed = 0;
      Object[] curRow = buf.getCurrentRow();
      Object[] nextRow = buf.getPeekAheadRow();

      /* The logic below will process 1:many and many:many cases. It does
       * this by setting the mark to the current row, then going through
       * all the matching right rows, adding them to the result set.
       * Once the right side matches have been accumulated, it goes through
       * the left side matches reprocessing the right side data.
       */
      Object lValue = null;   // used for later comparison
      if (m_translator != null)
         lValue = m_translator.extract(execData);
      else
         lValue = curRow[m_leftColumnIndex];

      buf.setBeginMark();   // mark this as if we're on many:many
      int comparator = 0;
      boolean hasNext = buf.hasRightPeekAheadRow();
      while (comparator == 0)
      {
         // add the current match (we can only be here on a match)
         buf.addRow(curRow);
         rowsProcessed++;

         if (!hasNext)   // no more data, stop processing the right side
            break;

         comparator = compareKeyValues(lValue, nextRow[m_rightColumnIndex]);
         if (comparator == 0) {
            // position to the next row, so the next iteration will add it
            buf.readRightRow();
            hasNext = buf.hasRightPeekAheadRow();
         }
      }

      // see if we're in a many:many or many:1 case, in which case we'll
      // reprocess the right rows for the left rows
      int leftMatches = 1;
      hasNext = buf.hasLeftPeekAheadRow();
      comparator = 0;
      Object nextLValue = null;
      while ((comparator == 0) && hasNext) {
         if (m_translator != null) {
            // we always want it on the current row, so do this temporarily
             execData.setCurrentResultRowData(nextRow);
            nextLValue = m_translator.extract(execData);
             execData.setCurrentResultRowData(curRow);
         }
         else
            nextLValue = nextRow[m_leftColumnIndex];

         comparator = compareKeyValues(lValue, nextLValue);
         if (comparator == 0)
         {   // we're still in the many condition, process the next row
            leftMatches++;
            buf.readLeftRow();
            buf.reprocessMarkedRows();
            hasNext = buf.hasLeftPeekAheadRow();
         }

         // no reason to extract twice for next lValue
         lValue = nextLValue;
      }

      buf.clearMark();   // done with this set of rows

      return (rowsProcessed * leftMatches);
   }
}

