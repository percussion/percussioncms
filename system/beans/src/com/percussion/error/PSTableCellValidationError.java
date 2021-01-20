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
package com.percussion.error;


/**
 * Used to return error information about an invalid entry in the rule
 * table model. A string describing the problem and the row/col# of the cell
 * containing the error. This info can be used to hilite/activate the offending
 * cell to aid the user. It is a wrapper around a data structure.
 */
public class PSTableCellValidationError
{
   /**
    * Constructor
    *
    * @param errorText the text string to be used as the displayed error message
    * @param row the table cell's row index
    * @param row the table cell's column index
    */
   public PSTableCellValidationError( String errorText, int row, int col )
   {
      m_errorText = errorText;
      m_errorRow = row;
      m_errorCol = col;
   }


   /**
    * Returns the row index for this error.
    */
   public int getErrorRow()
   {
      return m_errorRow;
   }

   /**
    * Returns the column index for this error.
    */
   public int getErrorCol()
   {
      return m_errorCol;
   }

   /**
    * Returns the error text string for this error.
    * May be <code>null</code> or empty.
    */
   public String getErrorText()
   {
      return m_errorText;
   }

   /**
    * The table cells row index for this error.
    * Defaults to -1. Modified in ctor.
    */
   private int m_errorRow = -1;

   /**
    * The table cells column index for this error.
    * Defaults to -1. Modified in ctor.
    */
   private int m_errorCol = -1;

   /**
    * The table cells text message for this error.
    * May be <code>null</code> or empty.
    */
   private String m_errorText = null;
}