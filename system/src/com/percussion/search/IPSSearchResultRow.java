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

package com.percussion.search;

import java.util.Map;
import java.util.Set;

/**
 * This interface represents one row of the search results. A search result row
 * is a set of result columns. Each column has a name which is the key, value
 * and display value. One can modify the value and/or display value but not the
 * name of the column in a row. Typically display value and value for a column
 * will be the same but not required to be.
 * <p>
 * Here is an example of how this interface would be used in the case of related
 * content search result processing. sys_variantid column is not part of normal
 * content editor system definition but is required in related content search
 * results. Also, each item ion the row may have more than template and hence
 * the rows have to be cloned to display additional templates. The way we do
 * this is:
 * <ol>
 * <li>Add sys_varaintid field to content editor system field definitions so
 * that the CE cataloger can catalog for configuring display format columns. The
 * value for this field can have a dummy value that can for example be defined
 * as Text Literal.</li>
 * <li>Add this column as one of the display columns for the related content
 * search display format. With this every search result row will have
 * sys_variantid column with the value specified in the content editor system
 * definition</li>
 * <li>Write a plug-in that implements
 * {@link com.percussion.search.IPSSearchResultsProcessor} to modify this column
 * in each result row. This is when you need to use this interface.</li>
 * <li>Since you need to have one row per item template, you should be able
 * clone the result row for each additional template the item has. You check for
 * the existence of this column (sys_variantid) using the method
 * {@link #hasColumn(String)} so that your plug-in is active only if a row has
 * this column and otherwise it does not modify any row.</li>
 * <li>You clone the row for each additional row per template using
 * {@link #cloneRow()} and set the correct column value for the sys_variantid
 * column using {@link #setColumnValue(String, String)} and/or
 * {@link #setColumnDisplayValue(String, String)}</li>
 * <li>You have the way specifying a display value different from internal
 * value using the above methods. The display values are meant to be displayed
 * in the client whereas internal values are for submitting to the server as
 * part of URLs.</li>
 * </ol>
 */
public interface IPSSearchResultRow
{
   /**
    * Make a new exact copy of the row and return.
    * 
    * @return new row object, never <code>null</code>.
    */
   IPSSearchResultRow cloneRow();

   /**
    * Get the display value of the specified column in the row.
    * 
    * @param colName name of the column. Must not be <code>null</code> or
    *           empty and is case sensitive.
    * @return Display value of the column, will be <code>null</code> if the
    *         column does not exist in the row or the column value is
    *         <code>null</code>.
    */
   String getColumnDisplayValue(String colName);

   /**
    * Set specified dislay value to a specified column in the row. Nothing 
    * happens if specified column does exist in the row.
    * 
    * @param colName name of the column, must not be <code>null</code> or
    *           empty and is case sensitive.
    * @param colDisplayValue display value for the column, may be
    *           <code>null</code> or empty.
    */
   void setColumnDisplayValue(String colName, String colDisplayValue);

   /**
    * Get value of the specified column in the row.
    * 
    * @param colName name of the column. Must not be <code>null</code> or
    *           empty and is case sensitive.
    * @return Value of the column, will be <code>null</code> if the column
    *         does not exist in the row or the column display value is
    *         <code>null</code>.
    */
   String getColumnValue(String colName);

   /**
    * Set specified value to a specified column in the row. Nothing 
    * happens if specified column does exist in the row.
    * 
    * @param colName name of the column, must not be <code>null</code> or
    *           empty and is case sensitive.
    * @param colValue value for the column, may be <code>null</code> or empty.
    */
   void setColumnValue(String colName, String colValue);

   /**
    * Does the row have the column with the specified name?
    * 
    * @param colName name of the column, must not be <code>null</code> or
    *           empty and is case sensitive.
    * @return <code>true</code> if the row has the specified column,
    *         <code>false</code> otherwise.
    */
   boolean hasColumn(String colName);

   /**
    * Get the set of column names in the row. Each entry in the set is a
    * <code>String</code>
    * 
    * @return set of column names in the row. Never <code>null</code> may be
    *         empty.
    */
   Set getColumnNames();

   /**
    * Get map of column values for the row. The key in the map is the name of
    * the column and the value is the internal value of the column.
    * 
    * @return map of column values as descibed above, never <code>null</code>
    *         may be empty.
    */
   Map getColumnValueMap();
}
