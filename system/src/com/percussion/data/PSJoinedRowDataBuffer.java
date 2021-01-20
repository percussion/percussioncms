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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;


/* JDBC sucks. You can only load data from a row once, and in the
 * appropriate left-right order. As such, we must store the row data
 * by reading it all into our array which will act as the row
 * buffer. Furthermore, we need to peek ahead so we can tell whether
 * or not to move ahead. As such we're also creating a next row buffer.
 */
class PSJoinedRowDataBuffer {
   /**
    * Construct a data buffer for storing joined result sets using the
    * specified left and right result sets.
    *
    * @param      lRS            the left side result set
    *
    * @param      rRS            the right side result set
    *
    * @param      columnMap      the column name to index map for use
    *                              in building the result set
    *
    * @param      omitColumns      set to <code>true</code> for columns to
    *                              be omitted from the result set
    *
    * @param      expectedSelectivity
    *                        the number of rows we expect to be in a typical
    *                        result set
    *
    * @exception   SQLException   if a SQL error occurs
    */
   PSJoinedRowDataBuffer(ResultSet lRS, ResultSet rRS, HashMap columnMap,
      boolean[] omitColumns, int expectedSelectivity)
      throws java.sql.SQLException
   {
      // this sets up the left side info
      this(lRS, columnMap, omitColumns, expectedSelectivity);

      // and this will setup the right side info
      setRightSideResultSet(rRS);
   }

   /**
    * Construct a data buffer for storing joined result sets using the
    * only the left side result set. This is used by the
    * PSIndexedLookupJoiner.
    *
    * @param      lRS            the left side result set
    *
    * @param      columnMap      the column name to index map for use
    *                              in building the result set
    *
    * @param      expectedSelectivity
    *                        the number of rows we expect to be in a typical
    *                        result set
    *
    * @exception   SQLException   if a SQL error occurs
    */
   PSJoinedRowDataBuffer(ResultSet lRS, HashMap columnMap,
      boolean[] omitColumns, int expectedSelectivity)
      throws java.sql.SQLException
   {
      super();

      m_expectedSelectivity = expectedSelectivity;
      m_columnHash = columnMap;

      // get the omit list and count the actual number of columns
      // we'll be skipping over
      m_omitColumns = omitColumns;
      m_omitColumnCount = 0;
      for (int i = 0; i < m_omitColumns.length; i++) {
         if (m_omitColumns[i])
            m_omitColumnCount++;
      }

      ResultSetMetaData lMeta = lRS.getMetaData();
      m_lCols = lMeta.getColumnCount();

      // build the meta data struct for this
      m_joinedRSMetaData = new PSResultSetMetaData(lMeta);

      m_joinedColumnCount = m_lCols;
      m_curRow  = new Object[m_joinedColumnCount];
      m_nextRow = new Object[m_joinedColumnCount];

      m_lRS = lRS;
      m_lHasData = true;
      m_lHasNext = true;

      // need the column types so we can determine how the read works
      m_lColTypes = new int[m_lCols];
      for (int i = 0; i < m_lCols; i++) {
         m_lColTypes[i] = lMeta.getColumnType(i+1);
      }

      // just reset the right side for now
      m_rRS = null;
      m_rCols = 0;
      m_rHasData = false;
      m_rHasNext = false;

      /* to get the buffers set up appropriately, we now read once
       * to get the data into next. when the caller calls read, the next
       * data will be pushed to cur.
       */
      readLeftRow();
   }

   /**
    * Set the right side result set for this data buffer.
    * This is used by the PSIndexedLookupJoiner.
    *
    * @param      rRS            the right side result set
    *
    * @exception   SQLException   if a SQL error occurs
    */
   void setRightSideResultSet(ResultSet rRS)
      throws java.sql.SQLException
   {
      ResultSetMetaData rMeta = rRS.getMetaData();
      int rCols = rMeta.getColumnCount();

      if (m_rCols != 0) {
         // if we're overwriting an existing result set, it had better
         // be of the same structure!
         if (rCols != m_rCols) {
             Object[] args = { String.valueOf(m_rCols), String.valueOf(rCols) };
            throw new PSSqlException(
               IPSDataErrors.JOINED_ROW_BUF_RCOL_COUNT_MISMATCH, args, "07008");
         }
      }
      else {
         // add the meta data struct for this
         m_joinedRSMetaData.addResultSetMetaData(rMeta);

         m_rCols = rCols;
         m_joinedColumnCount += rCols;

         // need the column types so we can determine how the read works
         m_rColTypes = new int[m_rCols];
         for (int i = 0; i < m_rCols; i++) {
            m_rColTypes[i] = rMeta.getColumnType(i+1);
         }

         // resize the current row and next row buffers
         Object[] newBuf = new Object[m_joinedColumnCount];
         System.arraycopy(m_curRow, 0, newBuf, 0, m_lCols);
         for (int i = m_lCols; i < m_joinedColumnCount; i++)
            newBuf[i] = null;
         m_curRow = newBuf;

         newBuf = new Object[m_joinedColumnCount];
         System.arraycopy(m_nextRow, 0, newBuf, 0, m_lCols);
         for (int i = m_lCols; i < m_joinedColumnCount; i++)
            newBuf[i] = null;
         m_nextRow = newBuf;

         // build the data array for this if we haven't yet
         if (m_joinedRSData == null) {
            int colCount = m_joinedColumnCount - m_omitColumnCount;
            m_joinedRSData = new ArrayList[colCount];
            for (int i = 0; i < colCount; i++) {
               m_joinedRSData[i] = new ArrayList(
                  java.lang.Math.max(m_expectedSelectivity, 25));
            }
         }
      }

      m_rRS = rRS;
      m_rHasData = true;
      m_rHasNext = true;

      /* to get the buffers set up appropriately, we now read once
       * to get the data into next. when the caller calls read, the next
       * data will be pushed to cur.
       */
      readRightRow();
   }

   /**
    * get the number of columns in the combined result set.
    */
   int getColumnCount() {
      return m_joinedColumnCount;
   }

   /**
    * get the current row's data.
    */
   Object[] getCurrentRow() {
      return m_curRow;
   }

   /**
    * get the peek ahead (next) row's data.
    */
   Object[] getPeekAheadRow() {
      return m_nextRow;
   }

   /**
    * does the left side have a next row?
     */
   boolean hasLeftPeekAheadRow()
   {
      return m_lHasNext;
   }

   /**
    * does the right side have a next row?
     */
   boolean hasRightPeekAheadRow()
   {
      return m_rHasNext;
   }

   /**
    * read the next row from the left side result set.
     *   
     * @exception   SQLException   if a SQL error occurs
     */
   boolean readLeftRow()
      throws java.sql.SQLException
   {
      if (!m_lHasData && !m_lHasNext)
         return false;

      if (m_lHasData && (m_lHasData = m_lRS.next())) {
         for (int i = 0; i < m_lCols; i++) {
            m_curRow[i] = m_nextRow[i];
            m_nextRow[i] = readColumnData(true, i+1);
            if (m_lRS.wasNull())
               m_nextRow[i] = null;
         }
      }
      else {
         m_lHasNext = false;
         for (int i = 0; i < m_lCols; i++) {
            m_curRow[i] = m_nextRow[i];
            m_nextRow[i] = null;
         }
      }

      return true;
   }

   /**
    * read the next row from the right side result set.
     *   
     * @exception   SQLException   if a SQL error occurs
     */
   boolean readRightRow()
      throws java.sql.SQLException
   {
      if (!m_rHasData && !m_rHasNext)
         return false;
      
      if (m_rHasData && (m_rHasData = m_rRS.next())) {
         for (int i = 0; i < m_rCols; i++) {
            m_curRow[m_lCols + i] = m_nextRow[m_lCols + i];
            m_nextRow[m_lCols + i] = readColumnData(false, i+1);
            if (m_rRS.wasNull())
               m_nextRow[m_lCols + i] = null;
         }
      }
      else {
         m_rHasNext = false;
         for (int i = 0; i < m_rCols; i++) {
            m_curRow[m_lCols + i] = m_nextRow[m_lCols + i];
            m_nextRow[m_lCols + i] = null;
         }
      }

      return true;
   }

   /**
    * Add a row of data to the result set.
    *
    * @param   rowData         an array of objects representing the row
    *                           of data to add to the result set
    */
   void addRow(Object[] rowData)
   {
      // add the row data to the result set
      int rsCol = 0;
      for (int i = 0; i < m_joinedColumnCount; i++) {
         if (!m_omitColumns[i]) {
            m_joinedRSData[rsCol].add(rowData[i]);
            rsCol++;
         }
      }
   }

   /**
    * Add a row of data to the result set, using NULL values for the
    * right hand side (for full or left outer joins).
    *
    * @param   rowData         an array of objects representing the row
    *                           of data to add to the result set
    */
   void addRowNullRight(Object[] rowData)
   {
      // add the real row data to the left side of the result set
      int rsCol = 0;
      int onCol = 0;
      for (; onCol < m_lCols; onCol++) {
         if (!m_omitColumns[onCol]) {
            m_joinedRSData[rsCol].add(rowData[onCol]);
            rsCol++;
         }
      }

      // and nulls to the right side of the result set
      for (; onCol < m_joinedColumnCount; onCol++) {
         if (!m_omitColumns[onCol]) {
            m_joinedRSData[rsCol].add(null);
            rsCol++;
         }
      }
   }

   /**
    * Add a row of data to the result set, using NULL values for the
    * left hand side (for full or right outer joins).
    *
    * @param   rowData         an array of objects representing the row
    *                           of data to add to the result set
    */
   void addRowNullLeft(Object[] rowData)
   {
      // add nulls to the left side of the result set
      int rsCol = 0;
      int onCol = 0;
      for (; onCol < m_lCols; onCol++) {
         if (!m_omitColumns[onCol]) {
            m_joinedRSData[rsCol].add(null);
            rsCol++;
         }
      }

      // and the real row data to the right side of the result set
      for (; onCol < m_joinedColumnCount; onCol++) {
         if (!m_omitColumns[onCol]) {
            m_joinedRSData[rsCol].add(rowData[onCol]);
            rsCol++;
         }
      }
   }

   /**
    * Get the joined result set created through this object.
    */
   PSResultSet getJoinedResultSet()
   {
      if (!m_adjustedResultSetMeta) {  // this had better happen only once
         for (int i = m_omitColumns.length; i > 0; i--) {
            // Make sure it's something we have!
            if (i > m_joinedColumnCount)
               continue;
               
            if (m_omitColumns[i-1]) {
               m_joinedRSMetaData.removeColumnMetaData(i);
            }
         }
         m_adjustedResultSetMeta = true;
      }

      if (m_joinedRSData == null)  // empty RS
         return new PSResultSet(m_joinedRSMetaData);
         
      // now return the joined rs
      return new PSResultSet(
         m_joinedRSData, m_columnHash, m_joinedRSMetaData);
   }

   /**
    * Get the number of rows we've added to the result set.
    */
   public int getRowsAdded()
   {
      if ((m_joinedRSData == null) || (m_joinedRSData.length == 0))
         return 0;

      return m_joinedRSData[0].size();
   }

   /**
    * Do we have a mark set so we can reprocess rows?
     */
   boolean isMarked()
   {
      return (m_rowBeginMark != NO_MARK);
   }

   /**
    * Mark the current row as the first row in a set of rows we will
    * be repeating.
    * <P>
    * When performing many:many joins, we match
    * the left side result set against each relation on the right side.
    * When we detect we're in a many:many situation, we'll process each
    * right side row, keeping track of the first row that matched using
    * this method. When we're out of right side rows but we still have
    * a matching left side key, we'll call
    * {@link #reprocessMarkedRows reprocessMarkedRows}
    * to apply the new left values with the remembered right side row
    * values.
     */
   void setBeginMark()
   {
      // set the mark to the number of rows in the buf
      // this relates to the row we're about to add
      m_rowBeginMark = m_joinedRSData[0].size();
   }

   /**
    * Mark the current row as the last row in a set of rows we will
    * be repeating.
    * <P>
    * When performing many:many joins, we match
    * the left side result set against each relation on the right side.
    * When we detect we're in a many:many situation, we'll process each
    * right side row, keeping track of the first row that matched using
    * this method. When we're out of right side rows but we still have
    * a matching left side key, we'll call
    * {@link #reprocessMarkedRows reprocessMarkedRows}
    * to apply the new left values with the remembered right side row
    * values.
     */
   void setEndMark()
   {
      // set the mark to the number of rows in the buf
      // this relates to the row we're about to add
      m_rowEndMark = m_joinedRSData[0].size();
   }

   /**
    * Remove the mark, allowing rows to be processed as normal.
     */
   void clearMark()
   {
      m_rowBeginMark = NO_MARK;
      m_rowEndMark = NO_MARK;
   }

   /**
    * Reprocess the data from the right side of the marked rows against
    * the left side of the current row.
    * When performing many:many joins, we match
    * the left side result set against each relation on the right side.
    * When we detect we're in a many:many situation, we'll process each
    * right side row, keeping track of the first row that matched using
    * this method. When we're out of right side rows but we still have
    * a matching left side key, we'll call
    * {@link #reprocessMarkedRows reprocessMarkedRows}
    * to apply the new left values with the remembered right side row
    * values.
     */
   void reprocessMarkedRows()
   {
      if (m_rowBeginMark == NO_MARK)   // no mark, skip this
         return;

      // if we haven't set an end mark, set the current size as the end mark
      if (m_rowEndMark == NO_MARK)
         m_rowEndMark = m_joinedRSData[0].size();

      int rowCount = m_rowEndMark;
      int colCount = m_joinedRSData.length;

      // add the row data to the result set
      for (int i = m_rowBeginMark; i < rowCount; i++) {
         // first we'll add the left side columns
         int rsCol = 0;
         for (int j = 0; j < m_lCols; j++) {
            if (!m_omitColumns[j]) {
               m_joinedRSData[rsCol].add(m_curRow[j]);
               rsCol++;
            }
         }

         // then we can add in the right side columns
         for ( ; rsCol < colCount; rsCol++) {
            java.util.ArrayList colList = m_joinedRSData[rsCol];
            colList.add(colList.get(i));
         }
      }
   }


   private Object readColumnData(boolean isLeft, int index)
      throws java.sql.SQLException
   {
      Object colData = null;
      ResultSet rs;
      int colType;

      if (isLeft) {
         rs = m_lRS;
         colType = m_lColTypes[index-1];
      }
      else {
         rs = m_rRS;
         colType = m_rColTypes[index-1];
      }

      colData = PSRowDataBuffer.getResultColumn(rs, colType, index);

      return colData;
   }


   private Object[]      m_curRow;
   private Object[]      m_nextRow;

   private ResultSet      m_lRS;
   private int            m_lCols;
   private boolean      m_lHasData;
   private boolean      m_lHasNext;
   private int[]         m_lColTypes;      // java.sql.Types col type

   private ResultSet      m_rRS;
   private int            m_rCols;
   private boolean      m_rHasData;
   private boolean      m_rHasNext;
   private int[]         m_rColTypes;      // java.sql.Types col type

   private int                     m_expectedSelectivity;
   private int                     m_joinedColumnCount;   // m_lCols + m_rCols
   private ArrayList[]            m_joinedRSData = null;
   private PSResultSetMetaData   m_joinedRSMetaData;
   private HashMap               m_columnHash;
   private boolean[]               m_omitColumns;
   private int                     m_omitColumnCount;

   /**
    * flag to guarantee we only remove omit columns once from the
    * ResultSetMetaData we return.
    */
   private boolean               m_adjustedResultSetMeta = false;


   private static final int      NO_MARK = -1;

   /*
    * When performing many:many joins, we match
    * the left side result set against each relation on the right side.
    * When we detect we're in a many:many situation, we'll process each
    * right side row, then iterate the left and reprocess the right side
    * rows.
    */
   private int m_rowBeginMark   = NO_MARK;
   private int m_rowEndMark   = NO_MARK;
}

