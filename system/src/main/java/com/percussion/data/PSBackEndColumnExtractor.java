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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * The PSBackEndColumnExtractor class is used to extract data from the
 * back-end column associated with the current result row.
 *
 * @author   Tas Giakouminakis
 * @version   1.0
 * @since   1.0
 */
public class PSBackEndColumnExtractor extends PSDataExtractor
{
   /**
    * Construct an object from its object store counterpart.
    *
    * @param   source       the object defining the source of this value
    */
   public PSBackEndColumnExtractor(
      com.percussion.design.objectstore.PSBackEndColumn source)
   {
      super(source);
      m_columnName = source.getColumn();
      String alias = source.getAlias();
      if (alias != null && alias.length() > 0)
         m_columnName = alias;
      m_columnIndex = -1;
   }

   /**
    * Extract a data value using the run-time data.
    *
    * @param   data    the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
    *
    * @return   the associated value; <code>null</code> if a
    *                       value is not found
    *
    * @exception   PSDataExtractionException
    *                        if an error condition causes the extraction to
    *                        fail. This is not thrown if the requested data
    *                        does not exist.
    */
   public Object extract(PSExecutionData data)
      throws PSDataExtractionException
   {
      return extract(data, null);
   }

   /**
    * Extract a data value using the run-time data.
    * 
    * @param data the execution data associated with this request. This includes
    * all context data, result sets, etc.
    * 
    * @param defValue the default value to use if a value is not found
    * 
    * @return the associated value; <code>defValue</code> if a value is not
    * found
    * 
    * @exception PSDataExtractionException is thrown in two conditions
    * <ol>
    * <li>the column does not exist in the result set in which case the error
    * code will be {@link IPSDataErrors#BE_COL_EXTR_INVALID_COL}</li>
    * <li>column exists but fails to extract the data from the result set in
    * which case the erro code will be
    * {@link IPSDataErrors#BE_COL_EXTR_EXCEPTION}</li>
    * </ol>
    */
   public Object extract(PSExecutionData data, Object defValue)
      throws PSDataExtractionException
   {
      /* If we are processing a fake result set for a null result set,
         return the default value */
      if (data.forceNullRowForNullResultSet())
         return defValue;
         
      Object value = null;

      if (m_columnIndex == -1)
      {
         initColumnIndex(data);
      }

      // get the result row
      Object[] rowData = data.getCurrentResultRowData();

      // and locate the column
      try
      {
         if (m_columnIndex >= 0 && m_columnIndex < rowData.length)
            value = rowData[m_columnIndex];
         else
            throw new IllegalStateException("m_columnIndex (" + m_columnIndex
                  + ") is out of the range of rowData[" + rowData.length + "]");
      }
      catch (Throwable t)
      {   // most likely the index was not found (out of bounds)
         Object[] args = { m_columnName, t.toString() };
         throw new PSDataExtractionException(
            IPSDataErrors.BE_COL_EXTR_EXCEPTION, args);
      }

      return (value == null) ? defValue : value;
   }

   private synchronized void initColumnIndex(PSExecutionData data) throws PSDataExtractionException
   {
      if (m_columnIndex == -1)
      {
         m_columnIndex = getColumnOrdinal(
               m_columnName, data.getCurrentResultSetMetaData());
            if (m_columnIndex == -1) {
               throw new PSDataExtractionException(
                  IPSDataErrors.BE_COL_EXTR_INVALID_COL, m_columnName);
            }

            m_columnIndex -= 1;               
      }
   }

   /**
    * Looks in the supplied metadata to find the column that matches the
    * supplied column name (in a case insensitive manner). The index of this
    * column is returned.
    *
    * @param colName The name of the column to find.  May not be <code>null
    * </code>.
    *
    * @param meta The metadata in which to locate the column.  May not be <code>
    * null</code>.
    *
    * @return The column index, or -1 if not found.
    *
    * @throws PSDataExtractionException if there is an error.
    * @throws IllegalArgumentException if any parameter is <code>null</code>.
    */
   public static int getColumnOrdinal(String colName, ResultSetMetaData meta)
      throws PSDataExtractionException
   {
      if (colName == null || meta == null)
         throw new IllegalArgumentException("colName and meta may not be null");
         
      try
      {
         int colCount = meta.getColumnCount();
         for (int i = 1; i <= colCount; i++) // 1-based
         {
            // The colName is actualy the alias of the column, so we need to
            // use the column-label to compare with it
            if (colName.equalsIgnoreCase(meta.getColumnLabel(i)))
            {
               return i;
            }
         }

         for (int i = 1; i <= colCount; i++)
         {
            String tableName = null;
            try {
               /* some (rather stupid) drivers (like Sybase) throw an exception
                * as they do not support this rather than a nice simple
                * null like most drivers
                * See bug id Rx-99-11-0046
                */
               tableName = meta.getTableName(i);
            }
            catch (SQLException e) {
               // guess we're ignoring these due to above reasoning
            }
            catch (UnsupportedOperationException e) {
               // ignore these due to above reasoning
            }

            if (tableName != null && tableName.length() != 0) 
            {
               String tableCol = tableName + "." + colName;
               if (tableCol.equalsIgnoreCase(meta.getColumnName(i)))
               {
                  return i;
               }                  
            }
         }

         int pos = colName.indexOf('.');
         if (pos != -1) {
            String tableCol = colName.substring(pos+1);
            for (int i = 1; i <= colCount; i++)
            {
               if (tableCol.equalsIgnoreCase(meta.getColumnName(i)))
               {
                  return i;
               }                  
            }
         } else
         {
            /* Might as well try stripping down the returned colname,
               we've tried everything else! */
            for (int i = 1; i <= colCount; i++)
            {
               String metaCol = meta.getColumnName(i);
               int metaPos = metaCol.indexOf('.');
               if (metaPos != -1)
               {
                  metaCol = metaCol.substring(metaPos+1);
                  if (colName.equalsIgnoreCase(metaCol))
                  {
                     return i;
                  }
               }                  
            }
         }

      }
      catch (Exception e)
      {
         Object[] args = { colName, e.toString() };
         throw new PSDataExtractionException(
            IPSDataErrors.BE_COL_GET_INDEX_EXCEPTION, args);
      }

      return -1;
   }


   private String      m_columnName;
   private int         m_columnIndex;
}

