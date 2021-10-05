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
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.PSApplicationHandler;

import java.util.HashMap;


/**
 * The PSQueryJoiner class is used internally by the E2 server to merge
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
public abstract class PSQueryJoiner implements IPSExecutionStep
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
    *
     */
   protected PSQueryJoiner(
      PSApplicationHandler ah, PSBackEndJoin join,
      String[] lCols, String[] lOmitCols,
      String[] rCols, String[] rOmitCols,
      int expectedSelectivity
      )
      throws PSNotFoundException,
         PSExtensionException
   {
      super();

      m_appHandler = ah;

      m_expectedSelectivity = expectedSelectivity;
      m_columnCount = lCols.length + rCols.length;

      /* build the column name to column index (1-based) mapping
       * as well as the col array which may be required by the optimizer
       */
      m_columnHash = new HashMap(m_columnCount);

      // we want the index of the join columns so get their names
      // for comparison
      m_leftColumn = join.getLeftColumn().getValueText();
      m_leftColumnIndex = -1;
      m_rightColumn = join.getRightColumn().getValueText();
      m_rightColumnIndex = -1;

      HashMap lMap = getColumnMap(lCols, 1);   // left is 1 based
      HashMap rMap = getColumnMap(rCols, lCols.length + 1);   // right includes left count (+1 base)

      // now store the columns we'll be skipping from the SELECT
      m_omitColumns = new boolean[m_columnCount];
      for (int i = 0; i < m_columnCount; i++)
         m_omitColumns[i] = false;   // default to keep the col

      int omitColCount = 0;
      Integer colPos = null;
      if (lOmitCols != null)
      {
         for (int i = 0; i < lOmitCols.length; i++) {
            colPos = (Integer)lMap.get(lOmitCols[i]);
            if (colPos != null) {
               omitColCount++;
               m_omitColumns[colPos.intValue()-1] = true;
               lMap.remove(lOmitCols[i]);
            }
         }
      }
      if (rOmitCols != null)
      {
         for (int i = 0; i < rOmitCols.length; i++) {
            colPos = (Integer)rMap.get(rOmitCols[i]);
            if (colPos != null) {
               omitColCount++;
               m_omitColumns[colPos.intValue()-1] = true;
               rMap.remove(rOmitCols[i]);
            }
         }
      }

      m_columnNames = new String[m_columnCount - omitColCount];
      int iOnCol = 0;
      int i = 0;
      for (; i < lCols.length; i++)
      {
         if (!m_omitColumns[i])
            m_columnNames[iOnCol++] = lCols[i];
      }
      for (; i < m_columnCount; i++)
      {
         if (!m_omitColumns[i])
            m_columnNames[iOnCol++] = rCols[i - lCols.length];
      }

      // these no longer contain non-SELECT columns, so set the hash
      // for the result set
      // we need to do one more run to build the final hash
      int colNo = 1;
      for (i = 0; i < lCols.length; i++)
         if (lMap.get(lCols[i]) != null) {
            m_columnHash.put(lCols[i], new Integer(colNo));
            colNo++;
         }
      for (i = 0; i < rCols.length; i++)
         if (rMap.get(rCols[i]) != null) {
            m_columnHash.put(rCols[i], new Integer(colNo));
            colNo++;
         }

      /* create the data translator for the left side which will be used
       * to do any manipulation prior to the comparison
       */
      PSExtensionCall extCall = join.getTranslator();
      if (extCall != null)
      {
         m_translator = new PSUdfCallExtractor(extCall);
      }

      // and finally, we need to save the join type
      if (join.isFullOuterJoin())
         m_joinType = FULL_OUTER_JOIN;
      else if (join.isLeftOuterJoin())
         m_joinType = LEFT_OUTER_JOIN;
      else if (join.isRightOuterJoin())
         m_joinType = RIGHT_OUTER_JOIN;
      else
         m_joinType = INNER_JOIN;
   }

   /**
    * Get the names of the columns associated with the result set this
    * joiner will generate.
    *
    * @return         a String array containing the column names in their
    *                  appropriate sequence (1st col is 1st, etc.)
    */
   public String[] getColumnNames()
   {
      return m_columnNames;
   }

   public void closeInputResultsAfterJoin(boolean close)
   {
      m_closeInputs = close;
   }

   public java.lang.String toString()
   {
      StringBuilder buf = new StringBuilder(128);

      switch (m_joinType) {
         case FULL_OUTER_JOIN:
            buf.append("[FULL OUTER JOIN ");
            break;

         case LEFT_OUTER_JOIN:
            buf.append("[LEFT OUTER JOIN ");
            break;

         case RIGHT_OUTER_JOIN:
            buf.append("[RIGHT OUTER JOIN ");
            break;

         // case INNER_JOIN:
         default:
            buf.append("[INNER JOIN ");
            break;
      }

      buf.append(m_leftColumn);
      buf.append(" = ");
      buf.append(m_rightColumn);
      buf.append("] SELECT ");
      for (int i = 0; i < m_columnNames.length; i++) {
         if (i > 0)
            buf.append(", ");
         buf.append(m_columnNames[i]);
      }

      return buf.toString();
   }


   /**
    * Compare the keys.
    *
    * <EM>NOTE: anyone extending this class (and thus using this method)
    * must be sure to set the left side meta data and current row object
    * in the PSExecutionData!!!</EM>
    *
    * @param   rowData         the data area to get the values from
    *
    * @param   leftHasData      <code>true</code> if the left side table
    *                           still has rows of data (end of set not reached)
    *
    * @param   rightHasData   <code>true</code> if the right side table
    *                           still has rows of data (end of set not reached)
    *
    * @return                  -1 if the left value is smaller;
    *                           0 if they're equal;
    *                         1 if right value is smaller
    */
   protected int compareKeys(
      PSExecutionData execData, Object[] rowData,
      boolean leftHasData, boolean rightHasData)
      throws com.percussion.data.PSDataExtractionException
   {
      // if only one side has data, make that the "winner"
      if (!leftHasData)
         return 1;   // right wins
      else if (!rightHasData)
         return -1;   // left wins

      // see if we need to use a translator to get at the left side's data
      Object lValue;
      if (m_translator != null)
         lValue = m_translator.extract(execData);
      else
         lValue = rowData[m_leftColumnIndex];

      Object rValue = rowData[m_rightColumnIndex];

      return compareKeyValues(lValue, rValue);
   }

   /**
    * Compare the keys.
    *
    * @param   lKey            the left side value to compare
    *
    * @param   rKey            the right side value to compare
    *
    * @return                  -1 if the left value is smaller;
    *                           0 if they're equal;
    *                         1 if right value is smaller
    */
   protected int compareKeyValues(Object lKey, Object rKey)
      throws com.percussion.data.PSDataExtractionException
   {
      if (lKey == null)
         return (rKey == null) ? 0 : -1;
      else if (rKey == null)
         return (lKey == null) ? 0 : 1;
      else if (lKey.equals(rKey))
         return 0;

      /* if these are of the same type but they're not equal,
       * figure out why
       *
       * 1. check if rKey is the same or subclasses lKey. If so, see if
       *    Comparable is implemented and use it.
       *
       * 2. check if lKey is the same or subclasses rKey. If so, see if
       *    Comparable is implemented and use it.
       *
       * 3. convert to a "common" data type and check the values.
       */
      if (lKey.getClass().isInstance(rKey)) {
         if (lKey instanceof Comparable)
            return ((Comparable)lKey).compareTo(rKey);
         else if (lKey.equals(rKey))
            return 0;
      }
      else if (rKey.getClass().isInstance(lKey)) {
         if (rKey instanceof Comparable)
            return ((Comparable)rKey).compareTo(lKey);
         else if (rKey.equals(lKey))
            return 0;
      }

      try {
         return PSDataConverter.compare(lKey, rKey);
      } catch (IllegalArgumentException e) {
         throw new PSDataExtractionException(0, e.getLocalizedMessage());
      }
   }

   private HashMap getColumnMap(String[] cols, int base)
   {
      HashMap colMap = new HashMap();
      for (int i = 0; i < cols.length; i++)
         colMap.put(cols[i], new Integer(i+base));
      return colMap;
   }

   /**
    * Update the average selectivity for this joiner by taking into
    * consideration the number of matching rows for the join which was
    * just completed.
    */
   protected void updateAverageSelectivity(int rowCount)
   {
      // we ignore 0 row counts as this does not help us determine what to
      // expect on a hit, which is what we're really interested in
      if (rowCount == 0)
         return;

      // now bump the statistical information
      synchronized (m_lockStatistics) {
         // what we're doing here is clearly not perfect, but it should
         // give us a fairly good view

         m_SelectRequests++;
         if (m_SelectRequests == Integer.MAX_VALUE) {
            m_SelectRequests /= 2;
            m_rowsSelected /= 2;      // also need to have the row count
         }

         // will this cause a wrap?
         if ((m_rowsSelected + rowCount) < m_rowsSelected) {
            m_rowsSelected /= 2;
            m_SelectRequests /= 2;   // also need to have the request count
         }
         m_rowsSelected += rowCount;

         // need to consider this causing a wrap, but that would require
         // a huge avg result set (2 billion rows)
         m_averageSelectivity = (int)(m_rowsSelected / m_SelectRequests);
      }
   }

   public long getSelectedRowCount()
   {
      return m_rowsSelected;
   }

   public static final int            FULL_OUTER_JOIN   = 1;
   public static final int            LEFT_OUTER_JOIN   = 2;
   public static final int            RIGHT_OUTER_JOIN   = 3;
   public static final int            INNER_JOIN         = 4;


   // this is the selectivity we've seen so far, which is constantly
   // updated to get the best possible performance
   protected Object                  m_lockStatistics = new Object();
   protected int                     m_averageSelectivity = 0;
   protected long                     m_rowsSelected = 0;
   protected int                     m_SelectRequests = 0;

   protected int                     m_expectedSelectivity;
   protected int                     m_joinType;
   protected String                  m_leftColumn;
   protected int                     m_leftColumnIndex;
   protected String                  m_rightColumn;
   protected int                     m_rightColumnIndex;
   protected int                     m_columnCount;
   protected HashMap                  m_columnHash;
   protected String[]               m_columnNames;
   protected PSUdfCallExtractor      m_translator;
   protected boolean[]               m_omitColumns;
   protected boolean                  m_closeInputs = true;

   protected PSApplicationHandler m_appHandler;
}

