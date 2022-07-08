/******************************************************************************
 *
 * [ PropertyTablePanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.guitools;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * A resuable panel containing a two column table.
 */
public class PropertyTablePanel
   extends JPanel
   implements KeyListener, FocusListener
{
   /**
    * The index of the column within the table that contains the property name 
    * data.
    */
   public static final int NAME_COLUMN = 0;

   /**
    * The index of the column within the table that contains the property value 
    * data.
    */
   public static final int VALUE_COLUMN = 1;

   /**
    * Convenience ctor that calls {@link #PropertyTablePanel(String[], int, 
    * boolean) PropertTablePanel(colNames, rows, <code>false</code>)}.
    */
   public PropertyTablePanel(String[] colNames, int rows)
   {
      this(colNames, rows, false);
   }

   /**
    * Convenience ctor that calls {@link #PropertyTablePanel(String[], int, 
    * boolean) PropertyTablePanel(DEFAULT_HEADERS, 1, <code>true</code>)}.
    */
   public PropertyTablePanel()
   {
      this(DEFAULT_HEADERS, 1, true);  
   }

   /**
    * Convenience ctor that calls {@link #PropertyTablePanel(String[], int, 
    * boolean) PropertyTablePanel(DEFAULT_HEADERS, rows, <code>true</code>)}.
    */
   public PropertyTablePanel(int rows)
   {
      this(DEFAULT_HEADERS, rows, true);  
   }

   /**
    * Constructs a panel containing a 2 column table wrapped in a scroll pane.
    *
    * @param colNames, a string array of column names, may not be <code>null
    * </code> or empty and should have only two entries spcifying the two
    * column names. Each header must be a non-<code>null</code>, non-empty
    * string.
    *
    * @param rows, number of rows in the table to start, if < 1, 1 is used
    * 
    * @param isEditable flag to indicate whether the end-user should be allowed to 
    * change the properties. Note that this does not affect the {@link 
    * #getData()} or {@link #setData(Map)} methods.
    */
   public PropertyTablePanel(String[] colNames, int rows, boolean isEditable)
   {
      //this(colNames, rows);
      if (colNames == null || colNames.length != 2)
      {
         throw new IllegalArgumentException(
               "The array of column names may be empty");
      }
      else
      {
         m_firstCol = colNames[0];
         m_secondCol = colNames[1];
         if (m_firstCol == null
            || m_firstCol.length() == 0
            || m_secondCol == null
            || m_secondCol.length() == 0)
         {
            throw new IllegalArgumentException(
                  "None of the column names can be left unsupplied");
         }
      }
      m_isEditable = isEditable;
      init(rows);
   }

   /**
    * Initializes the panel.
    * 
    * @param initialRows How many rows to show in the table.
    */
   private void init(int initialRows)
   {
      setLayout(new BorderLayout());
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      setBorder(emptyBorder);
      DefaultTableModel model =
         new DefaultTableModel(
            new Object[] { m_firstCol, m_secondCol },
            initialRows);
      final FocusListener fl = this;
      m_table = new JTable(model)
      {
         public boolean isCellEditable(int row, int column)
         {
            return m_isEditable;
         }

         /**
          * See comment in <code>prepareEditor</code> for the reason this was
          * overridden. Sets the focus in the editor component when it is
          * first activated.
          */
         protected boolean processKeyBinding(
            KeyStroke ks,
            KeyEvent e,
            int condition,
            boolean pressed)
         {
            boolean isEditing = isEditing();
            boolean result = super.processKeyBinding(ks, e, condition, pressed);
            if (!isEditing && isEditing())
            {
               getEditorComponent().requestFocus();
            }
            return result;
         }

         public Component prepareEditor(TableCellEditor ed, int row, int col)
         {
            JComponent c = (JComponent) super.prepareEditor(ed, row, col);
            if (!m_listenerSet)
            {
               m_listenerSet = true;
               /* The purpose of adding this listener is to stop the editor
                  whenever the user goes to some other component. However,
                  there appears to be a bug in swing that prevents this
                  technique from working in all cases. If you tab to a cell
                  and just start typing, the editor is activated and you can
                  type just as if you had activated the cell by double-clicking,
                  but the cell doesn't appear to really have the focus (the
                  blinking caret is not present and the focus listeners are
                  not fired). So in this case, the editor will remain activated
                  even though focus goes to some other component. I fixed this
                  by overriding processKeyBinding (see above). */
               c.addFocusListener(fl);
            }
            return c;
         }
         
         // Override super.valueChanged(ListSelectionEvent). 
         // Add a new row when selecting the last row.
         public void valueChanged(ListSelectionEvent e)
         {
            if (m_isEditable && getSelectedRow() != -1
                  && getSelectedRow() == getRowCount() - 1)
            {
               addRow();
            }
            super.valueChanged(e);
         }

         /**
          * Latch to record that we have set the focus listener. We need this
          * because there is no way to ask a component if a particular listener
          * has already been set. It is set the first time a cell is edited.
          */
         private boolean m_listenerSet = false;
      };
      m_table.setEnabled(m_isEditable);

      MouseListener ml = new MouseAdapter()
      {
         public void mousePressed(MouseEvent e)
         {
            if (SwingUtilities.isRightMouseButton(e))
            {
               addRow();
            }
         }
      };
      m_table.getTableHeader().addMouseListener(ml);
      m_jsp = new JScrollPane(m_table);

      m_jsp.getViewport().setBackground(m_table.getBackground());
      add(m_jsp, BorderLayout.NORTH);
      m_table.addKeyListener(this);

      String actionName = "stopedit";
      KeyStroke ksEnterRelease =
         KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true);
      m_table.getInputMap().put(ksEnterRelease, actionName);
      AbstractAction stopEditing = new AbstractAction()
      {
         /**
          * This action does 2 things. First, it stops any cell editing when
          * the Enter key is pressed. Secondly, it prevents the enter key
          * press from activating the default button of the main dialog, which
          * is extremely annoying when in a cell editor and enter dismisses
          * the whole dialog rather than just stopping editing.
          *
          * @param e unused
          */
         public void actionPerformed(ActionEvent e)
         {
            if (m_table.isEditing())
               stopExtTableEditing();
         }
      };
      m_table.getActionMap().put(actionName, stopEditing);

      //Create the popup menu.
      addPopupMenu();
   }

   //see FocusListener interface
   public void focusGained(FocusEvent e)
   {
      //noop
   }

   /**
    * Implements the mouse adapter interface for bringing up the popup menu on
    * right click.
    */
   private class PopupListener extends MouseAdapter
   {
      /**
       * MouseAdapter interface implementation.
       *
       * @param e, never <code>null</code>, supplied by Swing event handling
       * mechanism for mouse.
       */
      public void mousePressed(MouseEvent e)
      {
      }

      /**
       * MouseAdapter interface implementation. Calls {@link#maybeShowPopup(
       * MouseEvent)}, which brings up the popup menu.
       *
       * @param e, never <code>null</code>, supplied by Swing event handling
       * mechanism for mouse events.
       */
      public void mouseReleased(MouseEvent e)
      {
         maybeShowPopup(e);
      }

      /**
       * Shows the popup menu.
       *
       * @param e, mouse event may not be <code>null</code>.
       */
      private void maybeShowPopup(MouseEvent e)
      {
         if (e.isPopupTrigger())
            m_popup.show(e.getComponent(), e.getX(), e.getY());
      }
   }

   /**
    * Adds popup menu to the panel. Menu gets displayed on right click of the
    * mouse. The menu provides actions appropriate for rows.
    */
   private void addPopupMenu()
   {
      m_popup = new JPopupMenu();

      JMenuItem menuItem = new JMenuItem(ms_res.getString("menu.label.delete"));
      menuItem.setAccelerator(KeyStroke.getKeyStroke("Del"));

      menuItem.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            removeRows();
         }
      });

      m_popup.add(menuItem);
      m_table.addMouseListener(new PopupListener());
   }

   /**
    * If someone clicks outside the cell with the mouse, this will catch it
    * and stop editing for the cell.
    *
    * @param e unused
    */
   public void focusLost(FocusEvent e)
   {
      stopExtTableEditing();
   }

   /**
    * Gets the model underlying this table. In general, the {@link #getData()}
    * and {@link #setData(Map)} methods should be used. The model should only
    * be accessed directly if these 2 methods are insufficient for your 
    * purposes.
    * 
    * @return The model of this table. This table retains ownership of the 
    * model; any changes affect this object. Never <code>null</code>.
    */
   public TableModel getTableModel()
   {
      return m_table.getModel();
   }

   /**
    * @param model
   public void setTableModel(TableModel model)
   {
      if (model != null)
      {
         m_table.setModel(model);
         if (model instanceof DefaultTableModel)
             ((DefaultTableModel) model).fireTableDataChanged();
      }
   }
    */

   /**
    * Get the properties from the table. {@link #validateData()} should be
    * called before this method to make sure all property names are valid
    * (see that method for details of what valid means).
    *
    * @return the table data as name/value pairs, never <code>null</code>,
    * may be empty. Each entry has a <code>String<code> key and a <code>
    * String</code> value. The key is never <code>null</code> or empty.
    * The caller takes ownership of the returned map.
    */
   public Map getData()
   {
      Map data = new HashMap();
      DefaultTableModel model = (DefaultTableModel) getTableModel();

      int rowCount = model.getRowCount();
      for(int row = 0; row < rowCount; row++)
      {
         String key = (String) model.getValueAt(row, NAME_COLUMN);
         if (key == null || key.trim().length() == 0)
            continue;

         data.put(key, model.getValueAt(row, VALUE_COLUMN));
      }

      return data;
   }

   /**
    * Set the new table data. Clears all existing table data and then adds
    * the data supplied to the map.
    *
    * @param props The data to set the table with, may be <code>null</code>
    *    or empty to clear all existing rows.
    */
   public void setData(Map props)
   {
      clearAllRows();
      if (props == null)
         props = new HashMap();

      DefaultTableModel model = (DefaultTableModel) getTableModel();

      Iterator keys = props.keySet().iterator();
      while (keys.hasNext())
      {
         String key = (String) keys.next();
         Vector row = new Vector(2);
         /* put placeholders in the vector so we can set the elements to their
          * proper row
          */
         row.add(key);
         row.add(props.get(key));
         //now set the elements at their proper position
         row.set(NAME_COLUMN, key);
         row.set(VALUE_COLUMN, props.get(key));

         model.addRow(row);
      }

      // make sure that we have at least 1 empty row
      int rows = model.getRowCount();
      int emptyCount = 0;
      for (int i=0; i<rows; i++)
      {
         Object o = model.getValueAt(i, 0);
         if (null == o || o.toString().trim().length() == 0)
            emptyCount++;
      }
      if (emptyCount < 1)
         addRow();
   }
   
   /**
    * Return reference to the table used in this panel.
    * 
    * @return reference to the table. Never <code>null</code>. Any changes to
    * the returned object will affect this panel.
    */
   public JTable getTable()
   {
      return m_table;
   }

   /**
    * If the cell is editable, activates the editor for the cell located at
    * the specified location and requests the focus for it. If the editor is
    * a JTextField, all text in the cell is selected.
    *
    * @param row Must be between 0 and the row count -1, inclusive.
    *
    * @param col Must be between 0 and the column count -1, inclusive.
    */
   public void editCellAt(int row, int col)
   {
      //validation of params performed by called methods
      if (!m_table.isCellEditable(row, col))
         return;

      m_table.editCellAt(row, col);
      Component c = m_table.getEditorComponent();
      if (c instanceof JTextField)
          ((JTextField) c).selectAll();
      c.requestFocus();
   }

   //An empty implementation for the {@link java.awt.event.KeyListener}
   //interface
   public void keyTyped(KeyEvent e)
   {
   }

   /**
    * Implementation for the accelerator keys for popup menu items.
    *
    * @param e, never <code>null</code>, provided by Swing event handling
    * model.
    */
   public void keyPressed(KeyEvent e)
   {
      int code = e.getKeyCode();

      if (code == KeyEvent.VK_DELETE)
      {
         removeRows();
      }
   }

   /**
    * Deletes all selected rows from the table and model. 
    */
   public void removeRows()
   {
      DefaultTableModel dtm = ((DefaultTableModel) m_table.getModel());
      ListSelectionModel lsm = m_table.getSelectionModel();
      Vector data = dtm.getDataVector();
      int minIndex = lsm.getMinSelectionIndex();
      int maxIndex = lsm.getMaxSelectionIndex();
      int k = 0;
      for (int i = minIndex; i <= maxIndex; i++)
      {
         if (lsm.isSelectedIndex(i - k))
         {
            data.removeElementAt(i - k);
            k++;
         }
      }
      if (dtm.getRowCount() == 0)
         addRow();
      dtm.fireTableDataChanged();
   }


   public void setScrollPaneSize(Dimension d)
   {
      m_jsp.setPreferredSize(d);
   }

   /**
    * Removes all rows from this table. Any properties currently present in
    * this table are removed.
    */
   public void clearAllRows()
   {
      ((DefaultTableModel) m_table.getModel()).setNumRows(0);
   }

   /**
    * Appends a blank row to the table.
    */
   public void addRow()
   {
      int cols = m_table.getModel().getColumnCount();
      ((DefaultTableModel) m_table.getModel()).addRow(new Object[cols]);
   }

   /**
    * If any cell is currently being edited, the data in the editor will be
    * accepted and a couple additional steps will be performed.
    * <p>If a name is cleared, the associated value will be cleared as well.
    * <p>If there isn't at least 1 empty row, an empty row will be appended.
    * <p>If no cell is being edited, nothing is done.
    */
   protected void stopExtTableEditing()
   {
      if (m_table.isEditing())
      {
         int row = m_table.getEditingRow();
         m_table
            .getCellEditor(m_table.getEditingRow(), m_table.getEditingColumn())
            .stopCellEditing();

         //if the user cleared the name, then auto clear the value
         String name = (String) m_table.getValueAt(row, NAME_COLUMN);
         if (null != name && name.trim().length() == 0)
            m_table.setValueAt("", row, VALUE_COLUMN);

         //add an empty row if there aren't at least 2
         TableModel props = getTableModel();
         int rows = props.getRowCount();
         int emptyCount = 0;
         for (int i = 0; i < rows; i++)
         {
            Object o = props.getValueAt(i, NAME_COLUMN);
            if (null == o || o.toString().trim().length() == 0)
               emptyCount++;
         }
         if (emptyCount < 1)
            addRow();
      }
   }

   /**
    * Is the data that is in the model valid?  Property name must be unique,
    * contain no spaces and have a value.
    *
    * @return  <code>true</code> if it is, otherwise <code>false</code>.
    */
   public boolean validateData()
   {
      stopExtTableEditing();
      DefaultTableModel model = (DefaultTableModel) getTableModel();
      int rows = model.getRowCount();
      String name = null;
      String value = null;
      Map propNames = new HashMap();
      for (int k = 0; k < rows; k++)
      {
         name = (String) model.getValueAt(k, NAME_COLUMN);
         value = (String) model.getValueAt(k, VALUE_COLUMN);
         if (name != null && name.trim().length() > 0)
         {
            if (value == null || value.trim().length() == 0)
            {
               String msg = ms_res.getString("error.msg.missingvalue");
               msg = MessageFormat.format(msg, new Object[] { name });
               ErrorDialogs.showErrorDialog(
                  this,
                  msg,
                  ms_res.getString("error.title.validationerror"),
                  JOptionPane.ERROR_MESSAGE);
               editCellAt(k, VALUE_COLUMN);
               return false;
            }

            //check for dupes
            if (propNames.containsKey(name.toLowerCase()))
            {
               String msg = ms_res.getString("error.msg.duplicateprop");
               msg = MessageFormat.format(msg, new Object[] { name });
               ErrorDialogs.showErrorDialog(
                  this,
                  msg,
                  ms_res.getString("error.title.validationerror"),
                  JOptionPane.ERROR_MESSAGE);
               editCellAt(k, NAME_COLUMN);
               return false;
            }
            else
               propNames.put(name.toLowerCase(), null);

            // Attempt to tokenize the internal name
            // to detect any spaces if not allowed
            if (!m_allowSpaceInPropName)
            {
               StringTokenizer st = new StringTokenizer(name);
               if (st.countTokens() > 1)
               {
                  ErrorDialogs.showErrorDialog(
                     this,
                     ms_res.getString("error.msg.nospacesallowedpropertyname"),
                     ms_res.getString("error.title.validationerror"),
                     JOptionPane.ERROR_MESSAGE);

                  editCellAt(k, NAME_COLUMN);
                  return false;
               }               
            }

         }
      }
      return true;
   }

   //An empty implementation for the {@link java.awt.event.KeyListener}
   //interface
   public void keyReleased(KeyEvent e)
   {
   }
   
   /**
    * Set whether property names are allowed to contain spaces.  By default they
    * are not allowed.
    *  
    * @param isAllowed <code>true</code> to allow spaces, <code>false</code>
    * if not.  Setting is enforced by {@link #validateData()}.
    */
   public void setAllowSpaceInPropName(boolean isAllowed)
   {
      m_allowSpaceInPropName = isAllowed;
   }
   
   /**
    * Determine whether property names are allowed to contain spaces.  See
    * {@link #setAllowSpaceInPropName(boolean)} for more info.
    * 
    * @return <code>true</code> if allowed, <code>false</code> if not.
    */
   public boolean isSpaceAllowedInPropName()
   {
      return m_allowSpaceInPropName;
   }

   /**
    * The table is placed in a scroll pane that is invisible until the number
    * of rows exceeds the size of the panel, then it 'activates' and displays
    * a scroll bar on the right side of the table. Set in {@link #init(int)}.
    */
   private JScrollPane m_jsp;
   
   /**
    * The UI component that represents the data to be viewed/modified. Set in
    * {@link #init(int)}.
    */
   private JTable m_table;
   
   /**
    * A flag set in the ctor to indicate whether the data in a row can be 
    * edited.
    */
   protected boolean m_isEditable;

   /**
    * Specifies the name of the first column. Initialized in the ctor, never
    * <code>null</code> or empty after that.
    */
   private String m_firstCol;

   /**
    * Specifies the name of the second column. Initialized in the ctor, never
    * <code>null</code> or empty after that.
    */
   private String m_secondCol;

   /**
    * Pop up menu containing 'Delete' as the menu item, initialized in 
    * {@link#addPopupMenu() addPopupMenu()}, never <code>null</code> or
    * modified after that.
    */
   private JPopupMenu m_popup;
   
   /**
    * Determines if spaces are allowed in property names.  Intially 
    * <code>false</code>.  See {@link #setAllowSpaceInPropName(boolean)} for
    * more info.
    */
   private boolean m_allowSpaceInPropName = false;

   /**
    * Resource bundle for this class. Initialised on class load.
    * It's not modified after that. May be <code>null</code> if it could not
    * load the resource properties file.
    */
   private static ResourceBundle ms_res = null;

   /**
    * Contains the names of the columns used by the default ctor. Initialized
    * in static initializer.
    */   
   private static final String[] DEFAULT_HEADERS; 

   static 
   {
      ms_res = ResourceHelper.getResources();
      DEFAULT_HEADERS = new String[2];
      DEFAULT_HEADERS[NAME_COLUMN] = ms_res.getString("defaultNameHeader");
      DEFAULT_HEADERS[VALUE_COLUMN] = ms_res.getString("defaultValueHeader");
   }
}
