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
