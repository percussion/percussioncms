/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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

package com.percussion.guitools;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Default implementation for <code>IPSTableModel</code>.
 */
public abstract class PSTableModel extends DefaultTableModel 
   implements IPSTableModel
{
   //implements interface method to return false always.   
   public boolean allowChangeDescription()
   {
      return false;
   }
      
   //implements interface method to throw UnsupportedOperationException
   public void setDescription(int row, String desc)
   {
      throw new UnsupportedOperationException(
         "description can not be modified");
   }
   
   //implements interface method to return null.
   public String getDescription(int row)
   {
      return null;
   }
   
   //implements interface method to return false always.   
   public boolean allowRemove()
   {
      return false;
   }
      
   //default implementation for removing rows.
   public void removeRows(int[] rows)
   {
      if(allowRemove())
      {
         for (int i = 0; i < rows.length; i++) 
         {
            checkRow(rows[i]);
            removeRow(rows[i]);
            addRow(new Vector()); //add an empty row
         }   
      }
      else
         throw new UnsupportedOperationException(
            "model does not allow removing rows");
   }
   
   //implements interface method to return false always.   
   public boolean allowMove()
   {
      return false;
   }
   
   //implements interface method.
   public void moveRow(int start, int end, int dest)
   {
      if(!allowMove())
         throw new UnsupportedOperationException("rows are not allowed to move");
      
      checkRow(start);
      checkRow(end);
      
      if(dest < 0 || dest > getRowCount())
         throw new IndexOutOfBoundsException(
            "dest can not be less than 0 or more than row count (" + 
            getRowCount() + ")");
      
      if(start > end)
         throw new IndexOutOfBoundsException(
            "start can not be greater than end");
            
      if(start <= dest  && dest <= end)
         throw new IndexOutOfBoundsException(
            "dest can not be in between start and end (inclusive)");
      
      int insertPos = dest;

      /* If destination is after end as we remove the elements from the list
       * and add it later the index of destination is going to change, so
       * calculate the position where to insert.
       */
      if(dest > end)
         insertPos = dest - (end-start+1);

      Vector data = getDataVector();
      List removedElements = new ArrayList();
      for(int i = start; i <= end; i++)
      {
         removedElements.add(data.remove(start));
      }
      
      data.addAll(insertPos, removedElements);
      
      fireTableDataChanged();
   }
   
   //implements interface method to return row <code>Vector</code>
   public Object getData(int row)
   {
      checkRow(row);
      
      return getDataVector().get(row);
   }
      
   /**
    * Checks that the supplied row exists in this model.
    * 
    * @param row the row index to check, must be >= 0 and less than 
    * {@link #getRowCount() rowcount} of this model.
    * 
    * @throws IndexOutOfBoundsException if row index is invalid.
    */
   protected void checkRow(int row)
   {
      if(row < 0 || row >= getRowCount())
         throw new IndexOutOfBoundsException("row must be between 0 and " + 
            (getRowCount()-1) + "inclusive");   
   }
   
   /**
     * Checks that the supplied column exists in this model.
     * 
     * @param col the column index to check, must be >= 0 and less than 
     * {@link #getColumnCount() colcount} of this model.
     * 
     * @throws IndexOutOfBoundsException if column index is invalid.
     */
   protected void checkColumn(int col)
   {
      if(col < 0 || col >= getColumnCount())
         throw new IndexOutOfBoundsException("col must be between 0 and " + 
            (getColumnCount()-1) + "inclusive");     
   }
}