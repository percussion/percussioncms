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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.Serializable;
import java.util.EventObject;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

/** This is basically a rehash of the DefaultCellEditor class, tweaked for browse
  * box cell.
  */

public class EditableListBoxBrowseEditor implements TableCellEditor, Serializable
{
//
//  Constructors; perhaps add another parameter to reference the dialog that the
//                browse button will bring up

    /**
     * Constructs a BrowseCellEditor object that uses a combo box.
     *
     * @param x  a JComboBox object.
     * @param dialogRef a <CODE>Component</CODE> object where it will reference
     * to a <CODE>JDialog</CODE> or a <CODE>JFileChooser</CODE>. A
     * <CODE>null</CODE> is an acceptable parameter. Anything other type of
     * object will be ignored.
     */
    public EditableListBoxBrowseEditor(JComboBox x, Component dialogRef)
    {
      if ( dialogRef instanceof JDialog )
         m_dialogRef = (JDialog)dialogRef;
      else if ( dialogRef instanceof JFileChooser )
         m_fchooserRef = (JFileChooser)dialogRef;

      init( x );
    }

    /**
     * Constructs a BrowseCellEditor that uses a text field.
     *
     * @param x  a JTextField object.
     * @param dialogRef a <CODE>Component</CODE> object where it will reference
     * to a <CODE>JDialog</CODE> or a <CODE>JFileChooser</CODE>. A
     * <CODE>null</CODE> is an acceptable parameter. Anything other type of
     * object will be ignored.
     */
    public EditableListBoxBrowseEditor(JTextField x, Component dialogRef)
    {
      if ( dialogRef instanceof JDialog )
         m_dialogRef = (JDialog)dialogRef;
      else if ( dialogRef instanceof JFileChooser )
         m_fchooserRef = (JFileChooser)dialogRef;

      init( x );
    }


    /**
     * Constructs a BrowseCellEditor that uses a text field.
     *
     * @param x  a JTextField object ...
     */
    private void init(JTextField x)
    {
      m_editorComponent = x;
      m_editorComponent.setBorder(new LineBorder(Color.blue, 1));
      m_button.setPreferredSize(new Dimension(18, 20));
      m_button.setMaximumSize(m_button.getPreferredSize());
      m_button.setMinimumSize(m_button.getPreferredSize());
      m_button.setToolTipText("Browse");

      JPanel buttonPanel = new JPanel(new BorderLayout());
      buttonPanel.add(m_button, BorderLayout.CENTER);

      m_panel = new JPanel();
      m_panel.setLayout(new BorderLayout());
      m_panel.add(x, BorderLayout.CENTER);
      m_panel.add(buttonPanel, BorderLayout.EAST);

      this.m_button.setActionCommand("browse");
      this.m_clickCountToStart = 2;
      this.m_buttonDelegate = new EditorDelegate();
      this.m_delegate = new EditorDelegate()
      {
         public void setValue(Object x)
         {
            super.setValue(x);
            if (x != null)
            {
               m_storage = x;
               // trims whitespaces before setting string into component
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
      m_button.addActionListener(m_buttonDelegate);
   }


    /**
     * Constructs a BrowseCellEditor object that uses a combo box.
     *
     * @param x  a JComboBox object ...
     */
    private void init(JComboBox x)
    {
      m_editorComponent = x;
      m_button.setPreferredSize(new Dimension(18, 20));
      m_button.setMaximumSize(m_button.getPreferredSize());
      m_button.setMinimumSize(m_button.getPreferredSize());
      m_button.setToolTipText("browse");

      JPanel buttonPanel = new JPanel(new BorderLayout());
      buttonPanel.add(m_button, BorderLayout.CENTER);

      m_panel = new JPanel();
      m_panel.setLayout(new BorderLayout());
      m_panel.add(x, BorderLayout.CENTER);
      m_panel.add(buttonPanel, BorderLayout.EAST);

      this.m_button.setActionCommand("browse");
      this.m_clickCountToStart = 2;
      this.m_buttonDelegate = new EditorDelegate();
      this.m_delegate = new EditorDelegate()
        {
          public void setValue(Object x)
          {
            super.setValue(x);

            if (x != null)
            {
              m_storage = x;
              // trims whitespaces before setting string into component
              ((JComboBox)m_editorComponent).setSelectedItem(x.toString().trim());
            }
            else
              ((JComboBox)m_editorComponent).setSelectedItem(new String(""));
          }

          public Object getCellEditorValue()
          {
             if (m_storage instanceof EditableListBoxCellNameHelper)
             {
               ((EditableListBoxCellNameHelper)m_storage).setName(((JComboBox)m_editorComponent).getSelectedItem().toString());
               return m_storage;
             }
             else
               return ((JComboBox)m_editorComponent).getSelectedItem().toString();
          }

          public boolean startCellEditing(EventObject anEvent)
          {
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
        m_button.addActionListener(m_buttonDelegate);
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

/** Returns a reference to the button in the cell */

    public JButton getBrowseButton() { return m_button; }

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
      // code to add new empty row!!!
      /*
      if ((m_tableRef.getRowCount() - 1) == m_currentIndex)
      {
        System.out.println("getCellEditorValue: Adding row!");
        ((DefaultTableModel)m_tableRef.getModel()).addRow(new Object[1]);
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
      if (isCellEditable(anEvent))
      {
        if (anEvent == null || ((MouseEvent)anEvent).getClickCount() >= m_clickCountToStart)
        {
          m_button.repaint();
          retValue = m_delegate.startCellEditing(anEvent);
        }
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
      //System.out.println("In fireEditingStopped...");
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
      //System.out.println("In fireEditingCanceled...");
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
          // puts value back into the selected cell (not problem here...)

          if (m_currentIndex != (m_tableRef.getRowCount() - 1))
          {
            m_tableRef.setValueAt(m_tableRef.getValueAt(m_currentIndex,
                                                        m_currentColumn),
                                                        m_currentIndex,
                                                        m_currentColumn);
          }
          else
          {
            return;
          }
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

      System.out.println( "      getTableCellEditorComponent" );
      m_delegate.setValue(value);

      return m_panel;
    }

//
//  Protected EditorDelegate class
//

    protected class EditorDelegate implements ActionListener, KeyListener,
                                              ItemListener, Serializable
    {
      protected Object value;

      public Object getCellEditorValue()
      {
        return value;
      }

      public void setValue(Object x)
      {
        value = x;
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

      // added button action implementation for referencing the browse dialog
      // Implementing ActionListener interface
      public void actionPerformed(ActionEvent e)
      {
        if (e.getActionCommand().equals("browse"))
        {
          if (null != m_dialogRef)
          {
             m_dialogRef.setVisible(true);
            if(m_dialogRef instanceof IBrowseDialog)
            {
               ((IBrowseDialog)m_dialogRef).setValue(value);
            }
          }
          else if ( null != m_fchooserRef )
          {
            //m_fchooserRef.setVisible(true);
            int returnVal = m_fchooserRef.showDialog(null, sm_res.getString("select"));
            if(returnVal == JFileChooser.APPROVE_OPTION)
            {
               fireEditingStopped();
               DefaultTableModel tempModel = (DefaultTableModel)m_tableRef.getModel();
               if ( m_fchooserRef.isMultiSelectionEnabled() )
               {
                  File[] fList = m_fchooserRef.getSelectedFiles();
                  for ( int i = 0; i < fList.length; i++ )
                  {
                     String[] list = { fList[i].getPath() };
                     System.out.println( list[0] );
                     // the first entry will replace the data in selected cell
                     if ( i == 0 )
                        tempModel.setValueAt( list[0], m_currentIndex, m_currentColumn );
                     else
                        tempModel.insertRow( m_currentIndex+i-1, list );
                  }
               }
               else
               {
                  File f = m_fchooserRef.getSelectedFile();
                  String[] list = { f.getAbsolutePath() };
                  System.out.println( list[0] );
                  tempModel.setValueAt( list[0], m_currentIndex, m_currentColumn );
               }
            }
          }
          // else do nothing
        }
        // then return value from browse dialog and fireEditingStopped
        else
        {
          //System.out.println("(Inner) Else!!!");
          fireEditingStopped();
        }
      }

      // Implementing ItemListener interface
      public void itemStateChanged(ItemEvent e)
      {
        fireEditingStopped();
      }

      public void keyPressed(KeyEvent e) {}
      public void keyTyped(KeyEvent e) {}

      public void keyReleased(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
        {
          fireEditingCanceled();
        }
      }
    }

//
//  Instance Variables
//

    /** Event listeners */
    protected EventListenerList     m_listenerList = new EventListenerList();
    transient protected ChangeEvent m_changeEvent = null;

    protected JTable     m_tableRef;
    protected JPanel     m_panel;
    protected JComponent m_editorComponent;
    // TODOat: This way to get the URL of the Image file is VERY bad. Any polymorphism
    // of objects will throw the URL off its intended path. MUST find another
    // way to manage path settings for resources.
    protected JButton    m_button = new JButton(new ImageIcon(getClass().getResource("images/optional.gif")));
    protected EditorDelegate m_delegate;
    protected EditorDelegate m_buttonDelegate;
    protected int            m_currentIndex;
    protected int            m_currentColumn;
    protected int            m_clickCountToStart = 1;
    protected Object         m_storage;

    protected JDialog       m_dialogRef;
    protected JFileChooser  m_fchooserRef;

    private static ResourceBundle sm_res = null;
    static
    {
      try
      {
         if (sm_res == null)
          sm_res = ResourceBundle.getBundle("com.percussion.EditableListBox.EditableListBoxBrowseEditorResources",
                                                           Locale.getDefault());
      }
      catch (MissingResourceException e)
      {
         System.out.println(e);
      }
    }

} // End of class EditableListBoxBrowseEditor
