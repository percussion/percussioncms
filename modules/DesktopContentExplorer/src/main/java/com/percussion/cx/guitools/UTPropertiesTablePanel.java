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
package com.percussion.cx.guitools;

import com.percussion.border.PSFocusBorder;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * A reusable panel containing a table with two or more columns.
 * The first two columns are required, the third one is optional.
 * For example it allows to create a table with Name and Value columns;
 * or a table with Name, Value and Description columns.
 */
public class UTPropertiesTablePanel extends JPanel
   implements KeyListener, FocusListener
{
   /**
    * Inner, private class that provides a table cell renderer which
    * shows a focus highlight when configured to do so.
    */
   class UTTablePanelCellRenderer extends DefaultTableCellRenderer
   {
      /**
       * Border to use when cell has focus
       */
      private final Border mi_focusBorder =
         new PSFocusBorder(1, m_tableCellFocusColor, true);
         
      /* (non-Javadoc)
       * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
       */
      public Component getTableCellRendererComponent(
         JTable table,
         Object value,
         boolean isSelected,
         boolean hasFocus,
         int row,
         int column)
      {
         // XXX Auto-generated method stub
         JComponent renderer = (JComponent) 
            super.getTableCellRendererComponent(table, value, isSelected, 
               hasFocus, row, column);
         if (isTableUseFocusHighlight())
         {
            if (hasFocus)
            {
               renderer.setBorder(mi_focusBorder);
            }
            else
            {
               renderer.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
            }
         }
            
         return renderer;
      }

}
   
   /**
    * default ctor, does nothing, init must be called.
    */
   public UTPropertiesTablePanel()
   {
   }

   /**
    * Constructs the panel.
    *
    * @param colNames, a string array of column names, may not be <code>null
    * </code> or empty ands should have only two entries spcifying the two
    * column names.
    *
    * @param rows, number of rows in the table
    *
    * @throws IllegalArgumentException if the argument is invalid.
    */
   public UTPropertiesTablePanel(String[] colNames, int rows)
   {
      this(colNames, rows, false);
   }

   /**
    * Constructs the panel.
    *
    * @param colNames, a string array of column names, may not be <code>null
    * </code> or empty ands should have only two entries spcifying the two
    * column names.
    *
    * @param rows, number of rows in the table
    *
    * @param isEditable <code>false</code> makes table read-only.
    *
    * @throws IllegalArgumentException if the argument is invalid.
    */
   public UTPropertiesTablePanel(String[] colNames, int rows, boolean isEditable)
   {
      init(colNames, rows, isEditable);
   }

   /**
    * Initializes the panel.
    *
    * @param colNames, a string array of column names, may not be <code>null
    * </code> or empty ands should have only two entries spcifying the two
    * column names.
    *
    * @param rows, number of rows in the table
    *
    * @param isEditable <code>false</code> makes table read-only.
    *
    * @throws IllegalArgumentException if the argument is invalid.
    */
   public void init(String[] colNames, int rows, boolean isEditable)
   {
      if (m_bIsInited)
         return;

      if (colNames == null || colNames.length < VALUE_COLUMN)
         throw new IllegalArgumentException(
            "The array of column names must contain at least two columns");

      m_colNames = colNames;
      for (int c = 0; c < m_colNames.length && c <= VALUE_COLUMN; c++)
         if (m_colNames[c] == null || m_colNames[c].length() == 0)
            throw new IllegalArgumentException(
               "Neither First, nor Second column names can be null or empty");

      m_bIsInited = true;

      m_rows = rows;
      m_isEditable = isEditable;

      setLayout(new BorderLayout());
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      setBorder(emptyBorder);

      DefaultTableModel model = new DefaultTableModel(m_colNames, m_rows);

      final FocusListener fl = this;
      m_table = new JTable(model)
      {
         TableCellRenderer mi_renderer = new UTTablePanelCellRenderer();
         
         public TableCellRenderer getCellRenderer(int row, int column)  
         {
            return mi_renderer;
         }
         
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
    * mouse.
    */
   private void addPopupMenu()
   {
      m_popup = new JPopupMenu();

      JMenuItem menuItem = new JMenuItem(getResourceString("Delete"));

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

   public TableModel getTableModel()
   {
      return m_table.getModel();
   }

   public void setTableModel(TableModel model)
   {
      if (model != null)
      {
         m_table.setModel(model);
         if (model instanceof DefaultTableModel)
             ((DefaultTableModel) model).fireTableDataChanged();
      }
   }

   /**
    * Return reference to the table
    * @return reference to the table. Never <code>null</code>.
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

   public void clearAllRows()
   {
      ((DefaultTableModel) m_table.getModel()).setNumRows(0);
   }

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

         //if the user cleared the name, then auto clear the rest of the columns
         String name = (String) m_table.getValueAt(row, NAME_COLUMN);

         if (null != name && name.trim().length() == 0)
            for (int i = VALUE_COLUMN; i < m_colNames.length; i++)
               m_table.setValueAt("", row, i);

      }

      //add an empty row if there aren't at least 2
      TableModel props = getTableModel();

      int rows = props.getRowCount();

      int emptyCount = 0;

      for (int r = 0; r < rows; r++)
      {
         Object o = props.getValueAt(r, NAME_COLUMN);
         if (null == o || o.toString().trim().length() == 0)
            emptyCount++;
      }

      if (emptyCount < 1)
         for (int i=0; i < 3; i++)
            addRow(); //add several empty rows
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
               String msg = getResourceString("Property <{0}> must have a value.");
               msg = MessageFormat.format(msg, new Object[] { name });
               ErrorDialogs.showErrorDialog(
                  this,
                  msg,
                  getResourceString("Validation Error"),
                  JOptionPane.ERROR_MESSAGE);
               editCellAt(k, VALUE_COLUMN);
               return false;
            }

            //check for dupes
            if (propNames.containsKey(name.toLowerCase()))
            {
               String msg =
                getResourceString("Found duplicate property name: <{0}>.");

               msg = MessageFormat.format(msg, new Object[] { name });
               ErrorDialogs.showErrorDialog(
                  this,
                  msg,
                  getResourceString("Validation Error"),
                  JOptionPane.ERROR_MESSAGE);
               editCellAt(k, NAME_COLUMN);
               return false;
            }
            else
               propNames.put(name.toLowerCase(), null);

            // Attempt to tokenize the internal name
            // to detect any spaces
            StringTokenizer st = new StringTokenizer(name);
            if (st.countTokens() > 1)
            {
               ErrorDialogs.showErrorDialog(
                  this,
                  getResourceString("Property names cannot contain spaces."),
                  getResourceString("Validation Error"),
                  JOptionPane.ERROR_MESSAGE);

               editCellAt(k, NAME_COLUMN);
               return false;
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
    * Gets the resource string identified by the specified key.  If the
    * resource cannot be found, the key itself is returned.
    *
    * @param key identifies the resource to be fetched; may not be <code>null
    * </code> or empty.
    *
    * @return String value of the resource identified by <code>key</code>, or
    * <code>key</code> itself.
    *
    * @throws IllegalArgumentException if key is <code>null</code> or empty.
    */
   private String getResourceString(String key)
   {
      String clazzName = getClass().getName();

      if(key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty");

      if(key.startsWith("@"))
         key = clazzName + key;
      else
         key = clazzName + "@" + key;

      String resourceValue =
            PSI18NTranslationKeyValues.getInstance().getTranslationValue(key);

      // if the resourceValue is empty, return key as specified in the contract:
      if (resourceValue.trim().length() == 0)
         resourceValue = key;

      return resourceValue;
   }
   
   /**
    * Get the focus color
    * @return the focus color, never <code>null</code>.
    */
   public Color getTableCellFocusColor()
   {
      return m_tableCellFocusColor;
   }

   /**
    * @return <code>true</code> if the table cells should show keyboard focus.
    */
   public boolean isTableUseFocusHighlight()
   {
      return m_tableUseFocusHighlight;
   }

   /**
    * Set a new focus color
    * @param color New focus color to show, must never be <code>null</code>.
    */
   public void setTableCellFocusColor(Color color)
   {
      if (color == null)
      {
         throw new IllegalArgumentException("color must never be null");
      }
      m_tableCellFocusColor = color;
   }

   /**
    * Set the use focus state
    * @param b New value for state.
    */
   public void setTableUseFocusHighlight(boolean b)
   {
      m_tableUseFocusHighlight = b;
   }   

   public static void main(String[] args)
   {
      PSDialog dlg = new PSDialog("test");

      String cols[] = {"Name", "Value", "Description"};
      UTPropertiesTablePanel panel = new UTPropertiesTablePanel(cols, 5, true);

      panel.setSize(new Dimension(300, 400));

      dlg.setContentPane(panel);

      dlg.pack();
      dlg.setVisible(true);

      dlg.dispose();
   }

   private JScrollPane m_jsp;
   private JTable m_table;
   protected boolean m_isEditable;

   /**
    * header column names, initialized in ctor, never <code>null</code> after that.
    */
   private String[] m_colNames;


   /**
    * Specifies the number of rows. Initialized in the ctor.
    */
   private int m_rows;

   /**
    * Pop up menu containing 'Delete' as the menu item, initialized in
    * {@link#addPopupMenu() addPopupMenu()}, never <code>null</code> or
    * modified after that.
    */
   private JPopupMenu m_popup;
   
   /**
    * Color to use for focus highlights on table cells. Initialized to
    * a default value here, overriden by the accessors. This is a no-op 
    * unless the field {@link #m_tableUseFocusHighlight} is 
    * <code>true</code>  
    */
   private Color      m_tableCellFocusColor = Color.red;

   /**
    * If this is <code>true</code>, then use a focus border on the table
    * cells. This can be set via the accessors for this field.
    */
   private boolean    m_tableUseFocusHighlight = false;
   
   /**
    * These fields are used to reference the Property Name and Property Value
    * and optionaly Property Description columns respectively.
    */
   public static final int NAME_COLUMN = 0;
   public static final int VALUE_COLUMN = 1;

   private boolean m_bIsInited = false;



}
