/*[ IPSTableModel.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
 
package com.percussion.guitools;

import javax.swing.table.TableModel;
import java.util.Iterator;

/**
 * The table model which extends the functionality of <code>TableModel</code>
 * to support moving, deletion of rows and set some description for each row.
 */
public interface IPSTableModel extends TableModel
{
   /**
    * Gets the description for the specified row.
    * 
    * @param row the row index, must be >= 0 and less than row count of 
    * this model.
    * 
    * @return the description, may be <code>null</code> or empty.
    * 
    * @throws IndexOutOfBoundsException if row index is not valid.
    */
   public String getDescription(int row);
   
   /**
    * Tests whether the description for any row can be modified.
    * 
    * @return <code>true</code> if it can be modified, otherwise <code>false
    * </code>
    */
   public boolean allowChangeDescription();
   
   /**
    * Sets the description for the row.
    * 
    * @param row the row index, must be >= 0 and less than row count of 
    * this model.
    * @param desc the description to set, may be <code>null</code> or empty. 
    * Implementors may decide its behavoir.
    * 
    * @throws IndexOutOfBoundsException if row index is not valid.
    */
   public void setDescription(int row, String desc);
   
   /**
    * Gets this table model data.
    * 
    * @return the iterator over zero or more elements each representing a row
    * data, never <code>null</code>. The elements can be any <code>Object</code>
    * as defined by the implementor.
    */
   public Iterator getData();
   
   /**
    * Gets the object representing supplied row.
    * 
    * @param row the row index, must be >= 0 and less than row count of 
    * this model.
    * 
    * @return the data, may be <code>null</code>.
    * 
    * @throws IndexOutOfBoundsException if row index is not valid.
    */
   public Object getData(int row);
   
   /**
    * Tests whether data rows can be removed.
    * 
    * @return <code>true</code> if the data rows can be removed, otherwise
    * <code>false</code>
    */
   public boolean allowRemove();
   
   /**
    * Tests whether data rows can be moved.
    * 
    * @return <code>true</code> if the data rows can be moved, otherwise
    * <code>false</code>
    */
   public boolean allowMove();
   
   /**
    * Removes the data rows for the specified array of rows if the model is 
    * allowed to remove. Adds empty rows at the end.
    * 
    * @param rows the array of row indices to remove, may not be <code>null
    * </code>, if empty none removed.
    * 
    * @throws IllegalArgumentException if rows is <code>null</code>
    * @throws UnsupportedOperationException if it does not allow removal of 
    * rows.
    * @throws IndexOutOfBoundsException for illegal row indices.
    */
   public void removeRows(int[] rows);
   
   /**
    * Moves the rows specified from start to end (inclusive) to the destination. 
    * 
    * <pre>
    *  Examples of moves:<p>
    *  1. moveRow(1,3,5);<p>
    *          a|B|C|D|e|f|g|h|i|j|k   - before
    *          a|e|B|C|D|f|g|h|i|j|k   - after<p>
    *  2. moveRow(6,7,1);<p>
    *          a|b|c|d|e|f|G|H|i|j|k   - before
    *          a|G|H|b|c|d|e|f|i|j|k   - after
    * </pre>
    * 
    * @param start the start index, must be >= 0 and less than row count of 
    * this model and end index.
    * @param end the end index, must be >= 0 and start index and less than row 
    * count of this model. 
    * @param dest the destination index, must be >= 0 and <= the row count of 
    * this model and it should not in between start and end (inclusive).
    * 
    * @throws IndexOutOfBoundsException for illegal row indices.
    * @throws UnsupportedOperationException if it does not allow moving of rows.
    */
   public void moveRow(int start, int end, int dest);
}