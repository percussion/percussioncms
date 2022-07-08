/******************************************************************************
 *
 * [ PSTableSorter.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.guitools;

import com.percussion.cx.PSMainDisplayPanelConstants;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A sorter for <code>TableModel</code>s. The sorter has a <code>TableModel
 * </code> and itself implements <code>TableModel</code>. This does not store or 
 * copy the data in the model, instead it maintains an array of integers which 
 * it keeps the same size as the number of rows in its model. When the model 
 * changes it notifies the sorter that something has changed eg. "rowsAdded", so
 * that its internal array of integers can be reallocated. As requests are made 
 * of the sorter (like getValueAt(row, col) it redirects them to its model via 
 * the mapping array. That way this sorter appears to hold another copy of the 
 * table with the rows in a different order. The sorting algorthm used is stable 
 * which means that it does not move around rows when its comparison 
 * function returns 0 to denote that they are equivalent. Supports ascending or 
 * descending of the column data. It remembers the previous columns used for 
 * sorting and keeps the latest sorting column with the highest priority.
 * Note: Got this from Swing examples and modified.
 * <p>
 * {@link TableModel#addTableModelListener(javax.swing.event.TableModelListener) 
 * addTableModelListener(TableModelListener)} may be called on this class to be
 * notified of sorting changes.  In this case the <code>getSource()</code>
 * method of the resulting <code>TableModelEvent</code> will be an instance of
 * this class, <code>PSTableSorter</code>.  Events received with other types of
 * sources do not indicate a sorting change and should generally be ignored.
 */
public class PSTableSorter extends PSTableMap
{
   /**
    * Creates this table sorter from the supplied model.  Sorting is enabled
    * by default.
    * 
    * @param model the table model, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if model is <code>null</code>
    */
   public PSTableSorter(TableModel model) 
   {     
      super(model);
      reallocateIndexes();
   }
   
   /**
    * Creates this table sorter from the supplied model.  
    * 
    * @param model the table model, may not be <code>null</code>
    * @param isSortingEnabled if <code>true</code>, sorting will be initially
    * disabled.
    * 
    * @throws IllegalArgumentException if model is <code>null</code>
    */
   public PSTableSorter(TableModel model, boolean isSortingEnabled) 
   {     
      super(model);
      m_isSortingEnabled = isSortingEnabled;
      reallocateIndexes();
   }
   
   
   /**
    * Convenience method calls {@link #setModel(TableModel, boolean, boolean) 
    * setModel(model, isTable, false)}
    */
   public void setModel(TableModel model, boolean isTable) 
   {
      setModel(model, isTable, false);
   }

   /**
    * Sets the data model for which sorting should be supported. Sorts the rows
    * either by the current sorting columns if <code>keepSorting</code> is 
    * <code>true</code>, otherwise by initial sort column if defined, otherwise
    * by the first column, ascending.  Fires the 
    * <code>tableChanged(TableModelEvent)</code> to inform its listeners that
    * the entire table structure is changed.
    * 
    * @param model the table model, may not be <code>null</code>
    * @param isTable supply <code>true</code> to indicate entire table structure
    * changed not the number of data rows alone or <code>false</code> to 
    * indicate only number of data rows or the data in the existings rows have 
    * changed.
    * @param keepSorting <code>true</code> to keep and use the current sort
    * columns and direction, <code>false</code> to use the initial sort column
    * if set, otherwise the default sorting.  Ignored if <code>isTable</code>
    * is <code>false</code>. 
    */
   public void setModel(TableModel model, boolean isTable, boolean keepSorting) 
   {
      if(model == null)
         throw new IllegalArgumentException("model may not be null.");
      super.setModel(model);    
      if(isTable)
      {
         if (!keepSorting)
            emptySortingColumns();
         tableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
         
         if (!keepSorting && m_isSortingEnabled && model.getColumnCount() > 0 && 
            model.getRowCount() > 0)
         {
            // set up sort columns - if sort column is out of range, it is 
            // ignored and we sort by first column, ascending
            if (m_intialSortColumn < model.getColumnCount())
               sortByColumn(m_intialSortColumn, m_intialSortColumnDirection);
            else
               sortByColumn(0);
         }
      }
      else
         tableChanged(new TableModelEvent(this));
   }
   
   /**
    * Convenience method for {@link #setModel(TableModel, boolean) 
    * setModel(model, false)}. See the link for more information.
    */
   public void setModel(TableModel model) 
   {
      if(model == null)
         throw new IllegalArgumentException("model may not be null.");
         
      setModel(model, false);
   }
   
   
   /**
    * Compares cell values for the supplied rows for the supplied column. Uses 
    * the <code>Class</code> object supported by the column for comparison. 
    * Supports <code>Number</code>, <code>String</code>, <code>Boolean</code> 
    * and <code>Date</code> classes. If the <code>Class</code> of the column is
    * not one of the supported classes, calls <code>toString()</code> on the 
    * value objects to be compared and makes <code>String</code> case sensitive
    * comparison. Assumes <code>null</code> value is always less than other 
    * value. An empty number value will always be the lesser value when 
    * compared with a valid number.
    * 
    * @param row1 the first row to compare, must be >= 0 and less than 
    * {@link #getRowCount() rowcount} of this model.
    * @param row2 the second row to compare, must be >= 0 and less than 
    * {@link #getRowCount() rowcount} of this model.
    * @param column the column of the cell to compare, must be >= 0 and less 
    * than {@link #getColumnCount() columncount} of this model.
    * 
    * @return <code>0</code> if both cell values are equal or <code>-1</code> if
    * cell value of <code>row1</code> is less than cell value of <code>row2
    * </code> or <code>1</code> if cell value of <code>row1</code> is greater 
    * than cell value of <code>row2</code>.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public int compareRowsByColumn(int row1, int row2, int column) 
   {   
      int rowCount = getRowCount();
      
      if(row1 < 0 || row1 >= rowCount)
         throw new IllegalArgumentException("row1 must be between 0 and " + 
            (rowCount-1) + "inclusive");   
            
       if(row2 < 0 || row2 >= rowCount)
         throw new IllegalArgumentException("row2 must be between 0 and " + 
            (rowCount-1) + "inclusive");         
            
      int columnCount = getColumnCount();
      
      if(column < 0 || column >= columnCount)
         throw new IllegalArgumentException("column must be between 0 and " + 
            (columnCount-1) + "inclusive"); 
            
      Class type = m_model.getColumnClass(column);
      TableModel data = m_model;
      
      // Check for nulls.
      
      Object o1 = data.getValueAt(row1, column);
      Object o2 = data.getValueAt(row2, column); 
      
      // If both values are null, return 0.
      if (o1 == null && o2 == null) 
         return 0; 
      else if (o1 == null) // Define null less than everything. 
         return -1; 
      else if (o2 == null)
         return 1; 
   
      /*
      * We copy all returned values from the getValue call in case
      * an optimised model is reusing one object to return many
      * values.  The Number subclasses in the JDK are immutable and
      * so will not be used in this way but other subclasses of
      * Number might want to do this to save space and avoid
      * unnecessary heap allocation.
      */
         
      Object obj1 = data.getValueAt(row1, column);
      Object obj2 = data.getValueAt(row2, column);
      if (type.getSuperclass() == java.lang.Number.class) 
      {
         // This handles the case where one or both of the
         // row values is an empty Number
         if(obj1 instanceof String && obj2 instanceof Number)
            return -1;   
         if(obj1 instanceof String && obj2 instanceof String)   
            return 0;
         if(obj1 instanceof Number && obj2 instanceof String)
            return 1;
         
         Number n1 = (Number)obj1;
         double d1 = n1.doubleValue();
         Number n2 = (Number)obj2;
         double d2 = n2.doubleValue();
         
         if (d1 < d2) 
            return -1;
         else if (d1 > d2) 
            return 1;
         else 
            return 0;
      }
      else if (type == Date.class) 
      {
         //dates can be empty strings.         
         long n1=0;
         long n2=0;
         
         if (obj1 instanceof Date)
            n1 = ((Date)obj1).getTime();
         if (obj2 instanceof Date)
            n2 = ((Date)obj2).getTime();
         
         if (n1 < n2) 
            return -1;
         else if (n1 > n2) 
            return 1;
         else 
            return 0;
      }
      else if (type == String.class) 
      {
         String s1 = (String)obj1;
         String s2 = (String)obj2;
         int result = s1.compareToIgnoreCase(s2);
         
         if (result < 0) 
            return -1;
         else if (result > 0) 
            return 1;
         else 
            return 0;
      }
      else if (type == Boolean.class) 
      {
         Boolean bool1 = (Boolean)obj1;
         boolean b1 = bool1.booleanValue();
         Boolean bool2 = (Boolean)obj2;
         boolean b2 = bool2.booleanValue();
         
         if (b1 == b2) 
            return 0;   
         else if (b1)  // Define false < true
            return 1;
         else 
            return -1;
      }
      else {
         String s1 = obj1.toString();
         String s2 = obj2.toString();
         int result = s1.compareToIgnoreCase(s2);
         
         if (result < 0) 
            return -1;
         else if (result > 0) 
            return 1;
         else 
            return 0;
     }
   }
   
   /**
    * Compares the supplied rows for all the sorting columns defined in the
    * order of priority of sorting.
    * 
    * @param row1 the first row to compare, assumed to be >= 0 and less than 
    * total row count of the model.
    * @param row2 the second row to compare, assumed to be >= 0 and less than 
    * total row count of the model.
    * 
    * @return <code>0</code> or <code>1</code> or <code>-1</code>. The following
    * table describes in what conditions each result is returned.
    * <table border=1>
    * <tr><th>Rows comparison</th><th>Sorting order</th><th>Result</th>
    * <tr><td>equal values</td><td>Ascending or Descending</td><td>0</td></tr>
    * <tr><td>row1 &lt; row2</td><td>Ascending</td><td>-1</td></tr>
    * <tr><td>row1 &lt; row2</td><td>Descending</td><td>1</td></tr>
    * <tr><td>row1 &gt; row2</td><td>Ascending</td><td>1</td></tr>
    * <tr><td>row1 &gt; row2</td><td>Descending</td><td>-1</td></tr>
    * </table>
    */
   private int compare(int row1, int row2) 
   {
      for (int level = 0; level < m_sortingColumns.size(); level++) 
      {
         Integer column = (Integer)m_sortingColumns.get(level);
         int result = compareRowsByColumn(row1, row2, column.intValue());
         if (result != 0) 
            return m_bAscending ? result : -result;
      }
      return 0;
   }
   
   /**
    * Reallocates the indexes from the model's row count. Should be called in 
    * the constructor or whenever the model changed. Initializes each index is
    * mapped to model's row.
    */
   private void reallocateIndexes() 
   {
      int rowCount = m_model.getRowCount();
      
      // Set up a new array of indexes with the right number of elements
      // for the new data model.
      m_indexes = new int[rowCount];
      
      // Initialise with the identity mapping.
      for (int row = 0; row < rowCount; row++) 
      {
         m_indexes[row] = row;
      }
   }
   
   //implements the interface method.
   public void tableChanged(TableModelEvent e) 
   {
      reallocateIndexes();
      sort();
      super.tableChanged(e);
   }
   
   /**
    * Checks whether model has changed it's row count after it is set with this
    * sorter and the sorter is informed of the change or not.
    * 
    * @throws IllegalStateException if change is not informed.
    */
   private void checkModel() 
   {
      if (m_indexes.length != m_model.getRowCount()) 
         throw new IllegalStateException(
            "Sorter not informed of a change in model.");
   }
   
   /**
    * Sorts the rows in the model using the <code>shuttlesort</code> method and 
    * updates this sorter's indexes accordingly. 
    * 
    * @throws IllegalStateException if the model's row count is changed and the
    * sorter is not informed of that change.
    */
   private void sort() 
   {
      checkModel();
      shuttlesort((int[])m_indexes.clone(), m_indexes, 0, m_indexes.length);
   }
   
   // This is a home-grown implementation which we have not had time
   // to research - it may perform poorly in some circumstances. It
   // requires twice the space of an in-place algorithm and makes
   // NlogN assigments shuttling the values between the two
   // arrays. The number of compares appears to vary between N-1 and
   // NlogN depending on the initial order but the main reason for
   // using it here is that, unlike qsort, it is stable.
   /**
    * Sorts the indexes in <code>from</code> array and updates the <code>to
    * </code> array with the sorted indexes. Assumes both arrays are of the same
    * length and start with exact copies. 
    * 
    * @param from the array to be sorted, assumed not to be <code>null</code> or
    * empty.
    * @param to the array to hold sorted indexes, assumed not to be <code>null
    * </code> or empty.
    * @param low the low index of the arrays from which the soring should start,
    * assumed to be >= 0.
    * @param high the high index of the arrays up to which sorting should be 
    * done, assumed to be >= 0.
    */
   private void shuttlesort(int from[], int to[], int low, int high) 
   {
      if (high - low < 2) 
      {
         return;
      }
      int middle = (low + high)/2;
      shuttlesort(to, from, low, middle);
      shuttlesort(to, from, middle, high);
      
      int p = low;
      int q = middle;
      
      /* This is an optional short-cut; at each recursive call,
      check to see if the elements in this subset are already
      ordered.  If so, no further comparisons are needed; the
      sub-array can just be copied.  The array must be copied rather
      than assigned otherwise sister calls in the recursion might
      get out of sinc.  When the number of elements is three they
      are partitioned so that the first set, [low, mid), has one
      element and and the second, [mid, high), has two. We skip the
      optimisation when the number of elements is three or less as
      the first compare in the normal merge will produce the same
      sequence of steps. This optimisation seems to be worthwhile
      for partially ordered lists but some analysis is needed to
      find out how the performance drops to Nlog(N) as the initial
      order diminishes - it may drop very quickly.  */
      
      if (high - low >= 4 && compare(from[middle-1], from[middle]) <= 0) 
      {
         for (int i = low; i < high; i++) 
         {
            to[i] = from[i];
         }
         return;
      }
         
      // A normal merge. 
      
      for (int i = low; i < high; i++) 
      {
         if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) 
         {
            to[i] = from[p++];
         }
         else 
         {
            to[i] = from[q++];
         }
      }
   }
   
   /**
    * Gets the value at specified row and column by converting the row index to
    * actual data model row index.
    * 
    * @param row the row index of value to get, must be >= 0 and less than 
    * {@link #getRowCount() rowcount} of this model.
    * @param col the column index of value to get, must be >= 0 and less than  
    * {@link #getColumnCount() columncount} of this model.
    * 
    * @return the value, may be <code>null</code> if the internal table model
    * does not have a value set.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws IllegalStateException if the model's row count is changed and the
    * sorter is not informed of that change.
    */
   public Object getValueAt(int row, int col) 
   {
      checkModel();
      int rowCount = getRowCount();
      
      if(row < 0 || row >= rowCount)
         throw new IllegalArgumentException("row (" + row
               + ") must be between 0 and " + (rowCount - 1) + " inclusive");   
            
      int columnCount = getColumnCount();
      
      if(col < 0 || col >= columnCount)
         throw new IllegalArgumentException("col (" + col
               + ") must be between 0 and " + (columnCount - 1) + " inclusive"); 
            
      return m_model.getValueAt(m_indexes[row], col);
   }
   
   /**
    * Empties sorting columns list. Should be called if the model on this sorter
    * has changed its table structure.
    */
   private void emptySortingColumns()
   {
      m_sortingColumns.clear();
   }
   
   /**
    * Gets the actual model row index represented by this sorter model as 
    * <code>row</code>
    * 
    * @param row the row index of value to get, must be >= 0 and less than 
    * {@link #getRowCount() rowcount} of this model.
    * 
    * @return the row index, must be >= 0 and less than row count of the model.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws IllegalStateException if the model's row count is changed and the
    * sorter is not informed of that change.
    */
   public int getModelRow(int row)
   {
      checkModel();
      
      int rowCount = getRowCount();
      
      if(row < 0 || row >= rowCount)
         throw new IllegalArgumentException("row must be between 0 and " + 
            (rowCount-1) + "inclusive");  
      
      return m_indexes[row];
   }
   
   /**
    * Checks whether this sorter is currently sorting for ascending/descending
    * order.
    * 
    * @return <code>true</code> if it is ascending, otherwise <code>false</code>
    */
   public boolean isAscending()
   {
      return m_bAscending;
   }
   
   /**
    * Set the new value for the sorter as to whether it is sorting in an 
    * ascending or descending order
    * @param newvalue <code>true</code> if ascending
    */
   public void setAscending(boolean newvalue)
   {
      m_bAscending = newvalue;
   }
   
   /**
    * Determines if sorting is currently enabled. 
    * 
    * @return <code>true</code> if currently enabled, <code>false</code>
    * otherwise.
    */
   public boolean isSortingEnabled()
   {
      return m_isSortingEnabled;
   }
   
   /**
    * Enables or disables sorting.  If disabled, then the model will ignore any
    * events that would cause it to sort.  Calling this method after a model is
    * sorted will prevent further sorting, and will return the model to its
    * original order.
    * 
    * @param isEnabled If <code>true</code>, sorting will be enabled, otherwise
    * it will be disabled.  If <code>isEnabled</code> equals 
    * <code>isSortingEnabled()</code>, then this call will have no effect.
    */
   public void setIsSortingEnabled(boolean isEnabled)
   {
      if (m_isSortingEnabled != isEnabled)
      {
         m_isSortingEnabled = isEnabled;
         emptySortingColumns();
         tableChanged(new TableModelEvent(this));
      }
   }
   
   /**
    * Gets the latest sorting column, the column that has the highest priority.
    * 
    * @return the column index, may be <code>-1</code> if the sorter is not yet
    * used for sorting.
    */
   public int getLatestSortingColumn()
   {
      if(m_sortingColumns.isEmpty())
         return -1;
      else
         return ((Integer)m_sortingColumns.get(0)).intValue();
   }
   
   /**
    * Sets the value at specified row and column of the model by converting the 
    * row index to actual data model row index.
    * 
    * @param value the value to set, may be <code>null</code>
    * @param row the row index of value to get, must be >= 0 and less than 
    * {@link #getRowCount() rowcount} of this model.
    * @param col the column index of value to get, must be >= 0 and less than  
    * {@link #getColumnCount() columncount} of this model.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws IllegalStateException if the model's row count is changed and the
    * sorter is not informed of that change.
    */
   public void setValueAt(Object value, int row, int col) 
   {
      checkModel();
      
      int rowCount = getRowCount();
      
      if(row < 0 || row >= rowCount)
         throw new IllegalArgumentException("row must be between 0 and " + 
            (rowCount-1) + "inclusive");   
            
      int columnCount = getColumnCount();
      
      if(col < 0 || col >= columnCount)
         throw new IllegalArgumentException("col must be between 0 and " + 
            (columnCount-1) + "inclusive"); 
            
      m_model.setValueAt(value, m_indexes[row], col);
   }
   
   /**
    * Sorts the rows of the model with the supplied column having the highest 
    * priority and sets the order as ascending.
    * 
    * @param column the column to have the highest priority, assumed to be >= 0
    * and less than rowcount of the model.
    */
   public void sortByColumn(int column)
   {
      sortByColumn(column, true);
   }
   
   /**
    * Adds the supplied column to the front of the sorting columns list to have
    * the highest priority in sorting and sorts the rows in the model based on 
    * the order specified. If the column is already in the current sorting 
    * columns list it moves its position to the front. After sorting it informs
    * the listeners that this sorter has changed.
    * 
    * @param column the column to have the highest priority, assumed to be >= 0
    * and less than rowcount of the model.
    * @param ascending if <code>true</code> sorts in ascending order, otherwise
    * in descending order.
    */
   public void sortByColumn(int column, boolean ascending)
   {
      m_bAscending = ascending;

      Integer sortColIndex = new Integer(column);
      if(m_sortingColumns.indexOf(sortColIndex) != -1)
         m_sortingColumns.remove(sortColIndex);
      m_sortingColumns.add(0, sortColIndex);
      
      sort();
      
      super.tableChanged(new TableModelEvent(this)); 
   }
      
   /**
    * Get the sorting information for this model. The information in the 
    * {@link List} is a copy of the original list and modifications will
    * not affect the behavior of the sorter. This method is being supplied 
    * to allow the sorting information to be stored and restored. 
    * 
    * @return A {@link List} of sorting column indices, never <code>null</code>
    * but could be empty. This is a copy of the actual sorting information.
    */
   public List getSortingColumns()
   {
      return new ArrayList(m_sortingColumns);
   }

   /**
    * A new value for the sorting columns. The list should have been obtained
    * by an earlier column to {@link #getSortingColumns()}.
    * @param sortColumns A new value for the sorting columns, may be empty, but
    * never <code>null</code>. This information is copied from the incoming
    * {@link List}.
    */
   public void setSortingColumns(List sortColumns)
   {
      m_sortingColumns = new ArrayList(sortColumns);
   }   
      
   /**
    * Allow sorting of columns by keyboard shortcuts. Focus to the table header
    * must be gained first using F8. Then left and right buttons can be used to
    * select column. a is used for Ascending and d is used for desending. Access
    * to table can be restored with tab key.
    * 
    * @param table the table to which the sorting should be applied, may not be
    *           <code>null</code>
    * 
    * @throws IllegalArgumentException if table is <code>null</code>
    */
   public void addKeyListenerToHeaderInTable(JTable table)
   {
      if (table == null)
         throw new IllegalArgumentException("table may not be null");

      final JTable tableView = table;

      tableView.setColumnSelectionAllowed(false);
      KeyAdapter listKeyListener = new KeyAdapter()
      {
         public void keyReleased(KeyEvent e)
         {

            if (e.getKeyChar() == 'a' || e.getKeyChar() == 'd')
            {
               // HACK: Should be able to access focused column with
               // tableView.getColumnModel().getSelectionModel().getLeadSelectionIndex();
               // This does not seem to work in this case, the column returned
               // does not pick up changes to column
               // with left and right keys after focus is gained with F8.
               // To get around this we are setting a static variable on
               // PSMainDisplayPanel from the renderer
               // MainDisplayTableHeaderRenderer
               // which has access to the column and a hasFocus() method while
               // the header cell is being rendered.

               int column = PSMainDisplayPanelConstants.getFocusColumn();
               reorderColumn(tableView, column, e.getKeyChar() == 'a');
               tableView.getTableHeader().repaint();
            }

         }
      };
      JTableHeader th = tableView.getTableHeader();
      th.addKeyListener(listKeyListener);
   }
   

   /**
    * Adds a mouse listener to the supplied table header to sort the clicked
    * column. Sorts in ascending order when the column header is clicked for 
    * first time. Clicking the same header second time immediately will sort 
    * the rows in descending order. Each time the header is clicked 
    * consecutively, the sorting toggles between ascending and descending.
    * 
    * @param table the table to which the sorting should be applied, may not be
    * <code>null</code>
    * 
    * @throws IllegalArgumentException if table is <code>null</code>
    */
   public void addMouseListenerToHeaderInTable(JTable table)
   {
      if (table == null)
         throw new IllegalArgumentException("table may not be null");

      final JTable tableView = table;
      tableView.setColumnSelectionAllowed(false);
      MouseAdapter listMouseListener = new MouseAdapter()
      {
         public void mouseClicked(MouseEvent e)
         {
            TableColumnModel columnModel = tableView.getColumnModel();

            int viewColumn = columnModel.getColumnIndexAtX(e.getX());

            if (e.getClickCount() == 1)
            {
               boolean ascending = true;
               int column = tableView.convertColumnIndexToModel(viewColumn);
               if (isAscending() && getLatestSortingColumn() == column)
               {
                  ascending = false;
               }
               reorderColumn(tableView, viewColumn, ascending);
               tableView.getTableHeader().repaint();
            }
         }

      };
      JTableHeader th = tableView.getTableHeader();
      th.addMouseListener(listMouseListener);
   }

   /**
    * Reorder the specified column with the specified sort order
    * 
    * @param table the table to which the sorting should be applied, may not be
    * <code>null</code>
    * @param viewColumn the column, this is the column as seen not the model column.
    * @param ascending If true ascending order else descending
    */
   private void reorderColumn(final JTable tableView, int viewColumn, boolean ascending)
   {
      if (isSortingEnabled())
      {

         int column = tableView.convertColumnIndexToModel(viewColumn);
         if (column != -1)
         {

            // save "real" selected row indexes from the model
            int[] sortSelected = tableView.getSelectedRows();
            int[] trueSelected = new int[sortSelected.length];
            for (int i = 0; i < sortSelected.length; i++)
            {
               trueSelected[i] = m_indexes[sortSelected[i]];
            }

            // sort
            sortByColumn(column, ascending);

            // now select the same rows based on the sorted indexes
            for (int i = 0; i < trueSelected.length; i++)
            {
               int sortedRowIndex = -1;
               for (int j = 0; j < m_indexes.length && sortedRowIndex < 0; j++)
               {
                  if (m_indexes[j] == trueSelected[i])
                     sortedRowIndex = j;
               }
               tableView.addRowSelectionInterval(sortedRowIndex, sortedRowIndex);
            }
         }

      }
   }

   /**
    * Sets the initial sorting columns to use to sort the table after 
    * {@link #setModel(TableModel, boolean)} is called.  
    * 
    * @param index The column index, -1 to clear the initial sort column,
    * otherwise it must be >= 0.  Ignored when table is sorted if it is not a 
    * valid index.  If ignored or -1 is specified, the first column is sorted 
    * ascending.
    * @param ascending <code>true</code> to indicate an ascending sort,
    * <code>false</code> for a descending sort.
    */
   public void setInitialSortColumn(int index, boolean ascending)
   {
      if (index == -1)
      {
         index = 0;
         ascending = true;
      }
      
      m_intialSortColumn = index;
      m_intialSortColumnDirection = ascending;
   }

   /**
    * The array of row indexes which holds the mapping of this sorter model row
    * to the actual data model row that is set with the object, inialized in the
    * constructor and modified when the data model is changed or when the 
    * sorting happens. Never <code>null</code> after construction.
    */
   private int m_indexes[];
   
   /**
    * The list to hold the sorting columns in the order of priority. Initialized
    * to empty list, and updated with calls to <code>sortByColumn(int, boolean)
    * </code> method. Never <code>null</code> after construction. Reset to an 
    * empty list through a call to <code>emptySortingColumns()</code>
    */
   private List m_sortingColumns = new ArrayList();
   
   /**
    * The flag to indicate the latest sorting order used by the sorter. if 
    * <code>true</code> sorts the rows in ascending order, otherwise in 
    * descending order. Initialized to <code>true</code>, modified through call
    * to the {@link #sortByColumn(int, boolean)} or 
    * {@link #setAscending(boolean)} method.
    */
   private boolean m_bAscending = true;
   
   /**
    * Flag to indicate if sorting is currently enabled. Initially 
    * <code>true</code>, may be modified during construction or by calls to 
    * {@link #setIsSortingEnabled(boolean)}.  
    */
   private boolean m_isSortingEnabled = true;
   
   /**
    * Intial sort column indices, intially 0 to indicate the first column.
    * Modified by calls to {@link #setInitialSortColumn(int, boolean)}.
    */
   private int m_intialSortColumn = 0;

   /**
    * Initial sort column direction, <code>true</code> to indicate ascending,
    * <code>false</code> to indicate descending.  Initially <code>true</code>,
    * modified by calls to {@link #setInitialSortColumn(int, boolean)}.
    */
   private boolean m_intialSortColumnDirection = true;

}
