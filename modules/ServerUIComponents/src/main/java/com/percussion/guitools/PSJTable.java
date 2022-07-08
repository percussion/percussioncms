/*
 * Created on Sep 29, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.percussion.guitools;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.util.Vector;

/**
 * @author DougRand
 *
 * A useful common subclass of JTable taht provides additional
 * helper methods to avoid the use of the embedded models for columns,
 * rows and such.
 */
public class PSJTable extends JTable
{

   /**
    * @see javax.swing.JTable#JTable()
    */
   public PSJTable()
   {

      // XXX Auto-generated constructor stub
   }

   /**
    * @see javax.swing.JTable#JTable(java.util.Vector, java.util.Vector)
    */
   public PSJTable(Vector rowData, Vector columnNames)
   {
      super(rowData, columnNames);
   }

   /**
    * @see javax.swing.JTable#JTable(javax.swing.table.TableModel, javax.swing.table.TableColumnModel, javax.swing.ListSelectionModel)
    */
   public PSJTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm)
   {
      super(dm, cm, sm);
   }

   /**
    * @see javax.swing.JTable#JTable(javax.swing.table.TableModel, javax.swing.table.TableColumnModel)
    */
   public PSJTable(TableModel dm, TableColumnModel cm)
   {
      super(dm, cm);
   }

   /**
    * @see javax.swing.JTable#JTable(javax.swing.table.TableModel)
    */
   public PSJTable(TableModel dm)
   {
      super(dm);
   }

   /**
    * @see javax.swing.JTable#JTable(java.lang.Object[][], java.lang.Object[])
    */
   public PSJTable(Object[][] rowData, Object[] columnNames)
   {
      super(rowData, columnNames);
   }

   /**
    * @see javax.swing.JTable#JTable(int, int)
    */
   public PSJTable(int numRows, int numColumns)
   {
      super(numRows, numColumns);
   }

   /**
    * Add new column to table model by name
    * 
    * @param columnName for the new column, may not be <code>null</code>
    * or empty. Method will throw exception for an invalid column name.
    * The value forms the displayed text for the column unless the table
    * model overrides it.
    * 
    * @return the index of the column within the model
    */
   public int addColumn(String columnName)
   {
      if (columnName == null || columnName.trim().length() == 0)
      {
         throw new IllegalArgumentException("columnName may not be null or empty");
      }

      TableColumn col = new TableColumn();
      col.setHeaderValue(columnName);
      addColumn(col);
      return col.getModelIndex();
   }

   /**
    * Appends a row to the model. The model will notify all listeners.
    * The argument may not be <code>null</code> or an exception will
    * be thrown.
    * 
    * @param newRow
    */
   public void addRow(Object newRow)
   {
      if (newRow == null)
      {
         throw new IllegalArgumentException("newRow may not be null");
      }

      PSEditTableModel model = (PSEditTableModel) getModel();
      model.appendRow(newRow);
   }

   /**
    * Remove the row that is currently selected. If no row is selected, 
    * do nothing.
    */
   public void removeSelectedRow()
   {
      PSEditTableModel model = (PSEditTableModel) getModel();
      int sel = getSelectedRow();
      if (sel >= 0)
      {
         model.removeRow(sel);
      }
   }

   /**
    * Add a selection listener to the list of listeners. The behavior
    * of this method can be divined by reading the documentation
    * on the list selection model.
    * @see javax.swing.ListSelectionModel#addListSelectionListener
    * 
    * @param listener a list selection listener, may not be <code>null</code>
    * or an exception will be thrown.
    */
   public void addSelectionListener(ListSelectionListener listener)
   {
      if (listener == null)
      {
         throw new IllegalArgumentException("listener may not be null");
      }

      ListSelectionModel model = getSelectionModel();
      model.addListSelectionListener(listener);
   }

   /**
    * Add a new cell editor for the specified column. Note that 
    * default cell editor can wrap a number of components. Add 
    * more methods like this if you need to extend the components.
    * 
    * @param i index of column, must be &gt;= 0
    * @param editor editor to add, must not be <code>null</code>
    */
   public void setColumnEditor(int i, JComboBox editor)
   {
      if (i < 0)
      {
         throw new IllegalArgumentException("index must be zero or positive");
      }
      if (editor == null)
      {
         throw new IllegalArgumentException("editor may not be null");
      }

      TableColumnModel col_model = getColumnModel();
      col_model.getColumn(i).setCellEditor(new DefaultCellEditor(editor));
   }

   /**
    * Set the width of the specified column.
    * 
    * @param col the column to modify, must be &gt;= 0
    * @param width the width in pixels, must be &gt; 0
    */
   public void setColumnWidth(int col, int width)
   {
      if (col < 0)
      {
         throw new IllegalArgumentException("col must be zero or positive");
      }
      if (width < 1)
      {
         throw new IllegalArgumentException("width must be greater that 0");
      }

      TableColumnModel col_model = getColumnModel();
      col_model.getColumn(col).setMaxWidth(width);
   }

   /**
    * Select a specific row in the table.
    * @param row which must be from zero, inclusive, to the row
    * count for the table, exclusive. Exceptions are thrown for invalid
    * values.
    */
   public void setSelectedRow(int row)
   {
      if (row < 0)
      {
         throw new IllegalArgumentException("row must be zero or positive");
      }
      if (row >= this.getRowCount())
      {
         throw new IllegalArgumentException("row must be less than the row count");
      }

      ListSelectionModel model = getSelectionModel();
      model.setSelectionInterval(row, row);
   }

}
