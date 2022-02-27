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

package com.percussion.tablefactory;

import java.sql.Connection;

/**
 * IPSJdbcRowMapping defines a mapping from a row of source table to a row of
 * destination table. The <code>processRow()</code> method performs the actual
 * mapping. It takes a row from the source table and modifies the column names
 * based on this mapping rule. It can also add new columns to the tranformed row.
 */
public interface IPSJdbcRowMapping
{
   /**
    * This method sets the table data for which <code>processRow()</code> will
    * be called for each row of data. This method should be called before the
    * sequence of calls to <code>processRow()</code> is made, if the table data
    * may be utilized by the implementation of <code>processRow()</code>.
    * This allows for doing some preliminary processing of data such as finding
    * the maximum value of a column.
    *
    * @param tblData contains all the rows of data obtained from the source table,
    * may not be <code>null</code>, may not contain any row of data
    *
    * @throws IllegalArgumentException if <code>tblData</code> is <code>null</code>
    */
   public void setTableData(PSJdbcTableData tblData);

   /**
    * Sets the row action that should be set for the transformed rows for the
    * destination table.
    *
    * @param rowAction the action to be set for the transformed rows, should be
    * one of the following values:
    * <code>PSJdbcRowData.ACTION_INSERT</code> or
    * <code>PSJdbcRowData.ACTION_UPDATE</code> or
    * <code>PSJdbcRowData.ACTION_REPLACE</code> or
    * <code>PSJdbcRowData.ACTION_DELETE</code> or
    * <code>PSJdbcRowData.ACTION_INSERT_IF_NOT_EXIST</code>
    *
    * @throws IllegalArgumentException if <code>rowAction</code> is not one
    * of the following values:
    * <code>PSJdbcRowData.ACTION_INSERT</code> or
    * <code>PSJdbcRowData.ACTION_UPDATE</code> or
    * <code>PSJdbcRowData.ACTION_REPLACE</code> or
    * <code>PSJdbcRowData.ACTION_DELETE</code> or
    * <code>PSJdbcRowData.ACTION_INSERT_IF_NOT_EXIST</code>
    */
   public void setRowAction(int rowAction);

   /**
    * Adds a mapping between source table column and destination table column
    *
    * @param srcTblColName source table column name, may not be
    * <code>null</code> or empty
    * @param destTblColName destination table column name, may not be
    * <code>null</code> or empty
    *
    * @throws IllegalArgumentException if <code>srcTblColName</code> or
    * <code>destTblColName</code> is <code>null</code> or empty
    */
   public void addColumnMapping(String srcTblColName, String destTblColName);

   /**
    * Adds the specified column to each transformed row for the
    * destination table. This can be used to add columns which are
    * not present in the source row (that is, the row which is passed as
    * parameter to <code>processRow()</code> method).
    *
    * @param destTblColData column to be added to each transformed row
    * for the destination table, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>destTblColData</code> is
    * <code>null</code>
    */
   public void addColumn(PSJdbcColumnData destTblColData);

   /**
    * Processes the row obtained from the source table, applies this mapping
    * and returns the transformed row.
    *
    * @param conn the database connection to use, may not be <code>null</code>
    * @param srcRow a row of data from the source table, may not be
    * <code>null</code>
    *
    * @return transformed row obtained by transforming the row from the
    * source table (<code>srcRow</code>) using this mapping,
    * may be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>conn</code> or
    * <code>srcRow</code> is <code>null</code>
    * @throws PSJdbcTableFactoryException if any column specified in this mapping
    * is not found in srcRow.
    */
   public PSJdbcRowData processRow(Connection conn, PSJdbcRowData srcRow)
      throws PSJdbcTableFactoryException;

}



