/******************************************************************************
 *
 * [ PSTableMap.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.guitools;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/** 
 * This class provides most of the common behaviour of a table model by routing 
 * all requests to its internal table model that is set, and implements the 
 * <code>TableModelListener</code> by routing all events to its listeners. 
 * Note:Got this class from Swing examples and modified according to coding 
 * standards.
 */
public class PSTableMap extends AbstractTableModel implements TableModelListener 
{
   /**
    * Constructs the object with supplied model as the model to route the 
    * requests and adds itself as listener to the internal model to know about
    * its changes.
    * 
    * @param model the model may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if any model is <code>null</code>
    */
   public PSTableMap(TableModel model)
   {
      if(model == null)
         throw new IllegalArgumentException("model may not be null.");
      
      m_model = model;
      m_model.addTableModelListener(this); 
   }
   
   /**
    * Gets the internal table model that is wrapped in this model.
    * 
    * @return the model, never <code>null</code>
    */
   public TableModel getModel() 
   {
      return m_model;
   }
   
   /**
    * Sets the internal table model used to route the requests came to this 
    * model. Removes itself as the listener from previous model and sets itself
    * as the listener to this new model.
    * 
    * @param model the model may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if any model is <code>null</code>
    */
   public void setModel(TableModel model) 
   {
      if(model == null)
         throw new IllegalArgumentException("model may not be null.");
      
      m_model.removeTableModelListener(this);
      m_model = model; 
      m_model.addTableModelListener(this); 
   }
   
   // By default, implement TableModel by forwarding all messages 
   // to the internal model.    
   /**
    * Gets the value at specified row and column of the internal table model.
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
    */
   public Object getValueAt(int row, int col) 
   {
      int rowCount = getRowCount();
      
      if(row < 0 || row >= rowCount)
         throw new IllegalArgumentException("row must be between 0 and " + 
            (rowCount-1) + "inclusive");   
            
      int columnCount = getColumnCount();
      
      if(col < 0 || col >= columnCount)
         throw new IllegalArgumentException("col must be between 0 and " + 
            (columnCount-1) + "inclusive"); 
            
      return m_model.getValueAt(row, col); 
   }
   
   /**
    * Sets the value at specified row and column of the internal table model.
    * 
    * @param value the value to set, may be <code>null</code>
    * @param row the row index of value to get, must be >= 0 and less than 
    * {@link #getRowCount() rowcount} of this model.
    * @param col the column index of value to get, must be >= 0 and less than  
    * {@link #getColumnCount() columncount} of this model.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public void setValueAt(Object value, int row, int col) 
   {
      int rowCount = getRowCount();
      
      if(row < 0 || row >= rowCount)
         throw new IllegalArgumentException("row must be between 0 and " + 
            (rowCount-1) + "inclusive");   
            
      int columnCount = getColumnCount();
      
      if(col < 0 || col >= columnCount)
         throw new IllegalArgumentException("col must be between 0 and " + 
            (columnCount-1) + "inclusive"); 
            
      m_model.setValueAt(value, row, col); 
   }
   
   /**
    * Gets the row count of the internal model.
    * 
    * @return the row count, may be zero.
    */
   public int getRowCount() 
   {
      return m_model.getRowCount(); 
   }

   /**
    * Gets the column count of the internal model.
    * 
    * @return the column count, may be zero.
    */   
   public int getColumnCount() 
   {
      return m_model.getColumnCount(); 
   }
   
   /**
    * Gets the column name of the specified column in its internal model.
    * 
    * @param col the column index of value to get, must be >= 0 and less than  
    * {@link #getColumnCount() columncount} of this model.
    * 
    * @return the column name, depends on the internal table model 
    * implementation, generally never <code>null</code>, may be empty.
    * 
    * @throws IllegalArgumentException if column index is not valid.
    */
   public String getColumnName(int col) 
   {
      int columnCount = getColumnCount();
      
      if(col < 0 || col >= columnCount)
         throw new IllegalArgumentException("col must be between 0 and " + 
            (columnCount-1) + "inclusive"); 
            
      return m_model.getColumnName(col); 
   }
   
   /**
    * Returns the most specific superclass for all the cell values in the column
    * of the internal model. 
    * 
    * @param col the column index of value to get, must be >= 0 and less than  
    * {@link #getColumnCount() columncount} of this model.
    * 
    * @return the column class, may be <code>null</code> if the column cells has 
    * <code>null</code> data.
    * 
    * @throws IllegalArgumentException if column index is not valid.
    */
   public Class getColumnClass(int col) 
   {
      int columnCount = getColumnCount();
      
      if(col < 0 || col >= columnCount)
         throw new IllegalArgumentException("col must be between 0 and " + 
            (columnCount-1) + "inclusive"); 
            
      return m_model.getColumnClass(col); 

   }
   
   /**
    * Finds out whether the cell specified by row and column is editable or not.
    * Delegates the call to internal model.
    * 
    * @param row the row index of value to get, must be >= 0 and less than 
    * {@link #getRowCount() rowcount} of this model.
    * @param col the column index of value to get, must be >= 0 and less than  
    * {@link #getColumnCount() columncount} of this model.
    * 
    * @return <code>true</code> if editable, otherwise <code>false</code>
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public boolean isCellEditable(int row, int col) 
   { 
      int rowCount = getRowCount();
      // Exception was preventing F8 key from assigning focus to table header.     
      // An invalid entry just needs to return that the cell is not editable. 
      if(row < 0 || row >= rowCount)
         return false;
            
      int columnCount = getColumnCount();
      
      if(col < 0 || col >= columnCount)
         return false; 
               
      return m_model.isCellEditable(row, col); 
   }
   
   //
   // Implementation of the TableModelListener interface, 
   //
   // By default forward all events to all the listeners. 
   public void tableChanged(TableModelEvent e) 
   {
      fireTableChanged(e);
   }
   
   /**
    * The table model which represents the data model to which the requests need
    * to be routed, initialized in the constructor and never <code>null</code> 
    * after that. May be modified through <code>setModel(TableModel)</code>.
    */ 
   protected TableModel m_model; 
}
