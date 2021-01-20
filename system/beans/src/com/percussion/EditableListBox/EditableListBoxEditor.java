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

package com.percussion.EditableListBox;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.EventObject;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;

/** This is basically a rehash of the DefaultCellEditor class, tweaked for browse
  * box cell.
  */

public class EditableListBoxEditor implements TableCellEditor, Serializable
{

//
//  Constructors; perhaps add another parameter to reference the dialog that the
//                browse button will bring up

    /**
     * Constructs a CellEditor that uses a text field.
     *
     * @param x  a JTextField object ...
     */
  public EditableListBoxEditor(JTextField x)
  {
    m_editorComponent = x;
      m_editorComponent.setBorder(new LineBorder(Color.blue, 1));
    m_clickCountToStart = 2;
    m_delegate = new EditorDelegate()
    {
      // setting the String into the CellEditorComponent (aka: JTextField)
      public void setValue(Object x)
      {
        super.setValue(x);
        if (x != null)
        {
          m_storage = x;
          // trims whitespaces before setting text into the component
          ((JTextField)m_editorComponent).setText(x.toString().trim());
        }
        else
          ((JTextField)m_editorComponent).setText("");
      }

    
      public Object getCellEditorValue()
      {
            if (m_storage instanceof EditableListBoxCellNameHelper)
        {
          ((EditableListBoxCellNameHelper)m_storage).setName(((JTextField)m_editorComponent).getText());
          return m_storage;
        }
        else
          return ((JTextField)m_editorComponent).getText();
      }

      public boolean startCellEditing(EventObject anEvent)
         {
            if (anEvent == null)
               m_editorComponent.requestFocus();

        return true;
      }

      public boolean stopCellEditing()
      {
        return true;
      }
    };

    ((JTextField)m_editorComponent).addKeyListener(m_delegate);

    ((JTextField)m_editorComponent).addActionListener(m_delegate);
    
    ((JTextField)m_editorComponent).addFocusListener(m_delegate);
  }


    /**
     * Constructs a CellEditor object that uses a combo box.
       *
     * @param x  a JComboBox object ...
     */
  public EditableListBoxEditor(JComboBox x)
  {
    this.m_editorComponent = x;

    this.m_clickCountToStart = 2;
    this.m_delegate = new EditorDelegate()
    {
         // setting the String into the CellEditorComponent (aka: JComboBox)
      public void setValue(Object x)
      {
        super.setValue(x);

        if (x != null)// && !(x.toString().equals("")))
        {
          m_storage = x;
          
        }
        else
        {
          return;
          //m_storage = new String("");
        }  
        // trims whitespaces before setting string into component
        ((JComboBox)m_editorComponent).setSelectedItem(m_storage.toString().trim());
        
        // if ComboBox is a DropDownList... and if the selectedItem is not a part of the
        // comboBox list yet...

            /*
        if (!((JComboBox)m_editorComponent).isEditable() && !m_isDropListSet)
        {
            Object selectedItem = ((JComboBox)m_editorComponent).getSelectedItem();
          if (selectedItem != null)
          {
            String item = selectedItem.toString();
            //if (item != ((EditableListBoxCellNameHelper)m_storage).getName())
               if ( null != m_storage && item != m_storage.toString())
            {
                     ((JComboBox)m_editorComponent).insertItemAt(m_storage.toString(), 0);
              m_isDropListSet = true;
            }
          }
        }
            */
         }

         public Object getCellEditorValue()
         {
            if (m_storage instanceof EditableListBoxCellNameHelper)
            {
               ((EditableListBoxCellNameHelper)m_storage).setName(((JComboBox)m_editorComponent).getSelectedItem().toString());
               return m_storage;
            }
            else
               return ((JComboBox)m_editorComponent).getSelectedItem();
         }

         public boolean startCellEditing(EventObject anEvent)
         {
            m_editorComponent.requestFocus();
            if (anEvent instanceof AWTEvent)
            {
               return true;
            }

            return false;

         }

         public boolean stopCellEditing()
         {
            return true;
         }
      };

      ((JComboBox)m_editorComponent).getEditor().getEditorComponent().addKeyListener(m_delegate);
      ((JComboBox)m_editorComponent).addItemListener(m_delegate);
      ((JComboBox)m_editorComponent).addActionListener(m_delegate);
   }

    /**
     * Returns the a reference to the editor component.
     *
     * @return the editor Component
     */
  public Component getComponent()
  {
    return m_editorComponent;
  }

//
//  Modifying
//

    /**
     * Specifies the number of clicks needed to start editing.
     *
     * @param count  an int specifying the number of clicks needed to start editing
     * @see #getClickCountToStart
     */
  public void setClickCountToStart(int count)
  {
      m_clickCountToStart = count;
  }

    /**
     *  clickCountToStart controls the number of clicks required to start
     *  editing if the event passed to isCellEditable() or startCellEditing() is
     *  a MouseEvent.  For example, by default the clickCountToStart for
     *  a JTextField is set to 2, so in a JTable the user will need to
     *  double click to begin editing a cell.
     */
  public int getClickCountToStart()
  {
    return m_clickCountToStart;
  }

//
//  Implementing the CellEditor Interface
//

    // implements javax.swing.CellEditor
  public Object getCellEditorValue()
  {
    /*
    if ((m_tableRef.getRowCount() - 1) == m_currentIndex)
    {
      Object[] temp = new Object[1];
      temp[0] = new String("");
      ((DefaultTableModel)m_tableRef.getModel()).addRow(temp);
    }
    */

      return m_delegate.getCellEditorValue();
  }

    // implements javax.swing.CellEditor
  public boolean isCellEditable(EventObject anEvent)
  {
    if (anEvent instanceof MouseEvent)
    {
      if (((MouseEvent)anEvent).getClickCount() < m_clickCountToStart)
        return false;
    }
    return m_delegate.isCellEditable(anEvent);
  }
    
  // implements javax.swing.CellEditor
  public boolean shouldSelectCell(EventObject anEvent)
  {
    boolean retValue = false;

    if (this.isCellEditable(anEvent))
    {
      if (anEvent == null || ((MouseEvent)anEvent).getClickCount() >= m_clickCountToStart /*|| ((KeyEvent)anEvent).isActionKey()*/)
        retValue = m_delegate.startCellEditing(anEvent);
    }

     // By default we want the cell the be selected so we return true
    return retValue;
  }

  // implements javax.swing.CellEditor
  public boolean stopCellEditing()
   {
    boolean stopped = m_delegate.stopCellEditing();

    if (stopped)
    {
      fireEditingStopped();
    }

     return stopped;
   }

   // implements javax.swing.CellEditor
   public void cancelCellEditing()
   {
     m_delegate.cancelCellEditing();
     fireEditingCanceled();
   }

//
//  Handle the event listener bookkeeping
//
   // implements javax.swing.CellEditor
   public void addCellEditorListener(CellEditorListener l)
   {
     m_listenerList.add(CellEditorListener.class, l);
   }

   // implements javax.swing.CellEditor
   public void removeCellEditorListener(CellEditorListener l)
   {
     m_listenerList.remove(CellEditorListener.class, l);
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     * @see EventListenerList
     */
    protected void fireEditingStopped()
    {
       // Guaranteed to return a non-null array
       Object[] listeners = m_listenerList.getListenerList();
       // Process the listeners last to first, notifying
       // those that are interested in this event
       for (int i = listeners.length-2; i>=0; i-=2)
       {
          if (listeners[i]==CellEditorListener.class)
          {
             // Lazily create the event:
             if (m_changeEvent == null)
                m_changeEvent = new ChangeEvent(this);

             ((CellEditorListener)listeners[i+1]).editingStopped(m_changeEvent);
          }

       }
    }

      /*
       * Notify all listeners that have registered interest for
       * notification on this event type.  The event instance
       * is lazily created using the parameters passed into
     * the fire method.
     * @see EventListenerList
     */
   protected void fireEditingCanceled()
   {
     // Guaranteed to return a non-null array
     Object[] listeners = m_listenerList.getListenerList();
     // Process the listeners last to first, notifying
     // those that are interested in this event
     for (int i = listeners.length-2; i>=0; i-=2)
     {
       if (listeners[i]==CellEditorListener.class)
       {
         // Lazily create the event:
         if (m_changeEvent == null)
           m_changeEvent = new ChangeEvent(this);

         ((CellEditorListener)listeners[i+1]).editingCanceled(m_changeEvent);

         //System.out.println(m_currentIndex);
         //System.out.println(m_tableRef.getRowCount() - 1);
/*
             if (m_currentIndex != (m_tableRef.getRowCount() - 1))
             {
                //System.out.println(m_stringStorage);
                m_tableRef.setValueAt(m_storage, m_currentIndex, m_currentColumn);
             }
             else
             {
                return;
                //m_tableRef.setValueAt(new String(""), m_currentIndex, m_currentColumn);
             }
*/
       }
     }
   }

//
//  Implementing the CellEditor Interface
//

   // implements javax.swing.table.TableCellEditor
   public Component getTableCellEditorComponent(JTable table, Object value,
                                                      boolean isSelected,
                                                      int row, int column)
   {
     m_tableRef = table;
     m_currentColumn = column;
     m_currentIndex = row;

     m_delegate.setValue(value);
     return m_editorComponent;
   }

//
//  Protected EditorDelegate class
//

   protected class EditorDelegate implements KeyListener, ItemListener, 
      ActionListener, Serializable, FocusListener
   {
      protected Object value;
      
      public Object getCellEditorValue()
      {
         return value;
      }
      
      public void setValue(Object x)
      {
         this.value = x;
      }
      
      public boolean isCellEditable(EventObject anEvent)
      {
         if (m_storage instanceof EditableListBoxCellEditHelper)
            return ((EditableListBoxCellEditHelper)m_storage).isCellEditable();
         else
            return true;
      }
      
      public boolean startCellEditing(EventObject anEvent)
      {
         return true;
      }
      
      public boolean stopCellEditing()
      {
         return true;
      }
      
      public void cancelCellEditing() {}
      
      // Implementing ActionListener interface
      public void actionPerformed(ActionEvent e)
      {
         fireEditingStopped();
      }
      
      // Implementing ItemListener interface
      public void itemStateChanged(ItemEvent e)
      {
         if ( null == m_tableRef )
            return;
         
         // getSelectedRow() has to change; irrelevant now
         if (m_tableRef.getSelectedRow() != m_currentIndex)
         {
            //System.out.println("selected Row != currentIndex");
            fireEditingCanceled();
         }
         else
            fireEditingStopped();
      }
      
      public void keyPressed(KeyEvent e) {}
      public void keyReleased(KeyEvent e) {}
      
      public void keyTyped(KeyEvent e)
      {
         char c = e.getKeyChar();
         if ( '\u001b' == c )   // unicode escape
         {
            //System.out.println("Escape hit!");
            fireEditingCanceled();
         }
      }
      
      public void focusGained(@SuppressWarnings("unused") FocusEvent e)
      {
         // noop
      }
      
      public void focusLost(@SuppressWarnings("unused") FocusEvent e)
      {
         fireEditingStopped();
      }
   }

//
//  Instance Variables
//

    /** Event listeners */
    protected EventListenerList     m_listenerList = new EventListenerList();
    transient protected ChangeEvent m_changeEvent = null;

    protected JTable     m_tableRef;
    protected JComponent m_editorComponent;
    protected EditorDelegate m_delegate;
    protected int            m_currentColumn;
    protected int            m_currentIndex;
    protected int            m_clickCountToStart = 1;

    /** Used for components implementing EditableListBoxCellNameHelper interface.
    */
    protected Object         m_storage;

    protected boolean    m_isDropListSet;

} // End of class EditableListBoxEditor

