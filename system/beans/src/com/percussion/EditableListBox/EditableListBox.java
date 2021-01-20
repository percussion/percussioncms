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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;


/** A combination of a list and 2 buttons.  It can store any object as data and
  * dislplay the data via a toString() call.  If the elements in the 
  * EditableListBox is a wrapper object, it must implement these interfaces to
  * displace the object&apos;s text and/or image.
  * <P>
  * Cell element helper interfaces:
  * <UL>
  * <LI><B>EditableListBoxCellNameHelper</B>: methods in this interface helps 
  * with the text in the object to be displayed in the ListBox. 
  * <LI><B>ICellImageHelper</B>: methods in this interface helps with 
  * the image in the object to be displayed in the ListBox.
  * <LI><B>IDataExchange</B>: methods to set the default type of object created 
  * by the EditableListBox on &quot;insert&quot; button action. (see
  * NotifierPropertyDialog for example)
  * </UL>
  *
  * <B>NOTE</B>: the delete button of the EditableListBox has NO ActionListener
  * implementation by default. If an implementation was provide by default,
  * the ActionListener cannot be removed (at least I didn't know how). So if
  * the implementor of this bean wants to add additional functionality to the
  * delete button, the button will ALWAYS delete the selected row regardless.
  * Therefore, for a simple delete implementation, just call getRightButton()
  * and add an ActionListener implementation that uses method
  * &lt;EditableListBox instance&gt;.deleteRows() to it.
  */

public class EditableListBox extends JPanel implements ActionListener
{
//
// Constructors
//
  
  /** Default constructor that creates the most basic components of this bean.
   * However, be careful of the Dialog reference (new JDialog()) that is
   * created by default. If you want to use a BROWSEBUTTON or an
   * EditableListBoxBrowseEditor as a cell editor, call setBrowseDialog() or
   * setFileChooser to set a usable dialog. */
  public EditableListBox()
  {
    this(new String(""), null, null,
         null, EditableListBox.TEXTFIELD, EditableListBox.INSERTBUTTON);
  }

  // various constructors to use JFileChooser
  public EditableListBox(String title, Component chooserRef)
  {
    this(title, chooserRef, null,
         null, EditableListBox.TEXTFIELD, EditableListBox.INSERTBUTTON);
  }
  
  public EditableListBox(Component chooserRef, Object[] listData)
  {
    this(new String(""), chooserRef,
         listData, null, EditableListBox.TEXTFIELD, EditableListBox.INSERTBUTTON);
  }
  
  public EditableListBox(String title, Component chooserRef, Object[] listData)
  {
    this(title, chooserRef,
         listData, null, EditableListBox.TEXTFIELD, EditableListBox.INSERTBUTTON);
  }

  public EditableListBox(String title, Component chooserRef, Object[] listData,
                         int editorType)
  {
    this(title, chooserRef,
         listData, null, editorType, EditableListBox.INSERTBUTTON);
  }

  public EditableListBox(String title, Component chooserRef, Object[] listData,
                         int editorType, String buttonType)
  {
    this(title, chooserRef,
         listData, null, editorType, buttonType);
  }

  /*
  // various constructors to use JDialog
  public EditableListBox(String title, JDialog dialogRef)
  {
    this(title, dialogRef, null,
         null, EditableListBox.TEXTFIELD, EditableListBox.INSERTBUTTON);
  }

  public EditableListBox(JDialog dialogRef, Object[] listData)
  {
    this(new String(""), dialogRef,
         listData, null, EditableListBox.TEXTFIELD, EditableListBox.INSERTBUTTON);
  }

  public EditableListBox(String title, JDialog dialogRef, Object[] listData)
  {
    this(title, dialogRef,
         listData, null, EditableListBox.TEXTFIELD, EditableListBox.INSERTBUTTON);
  }

  public EditableListBox(String title, JDialog dialogRef, Object[] listData,
                         int editorType)
  {
    this(title, dialogRef,
         listData, null, editorType, EditableListBox.INSERTBUTTON);
  }

  public EditableListBox(String title, JDialog dialogRef, Object[] listData,
                         int editorType, String buttonType)
  {
    this(title, dialogRef,
         listData, null, editorType, buttonType);
  }
  */

  public EditableListBox(String title, Component dialog, Object[] listData,
                         Object[] comboData, int editorType, String buttonType)
  {
    super();
    setTitle(title);

    if ( dialog instanceof JDialog )
    {
      m_browseDialog = (JDialog)dialog;
      m_fChooser = null;
    }
    else if ( dialog instanceof JFileChooser )
    {
      m_fChooser = (JFileChooser)dialog;
      m_browseDialog = null;
    }
    else
    {
      m_fChooser = null;
      m_browseDialog = null;
    }
      //throw new IllegalArgumentException("The dialog reference must be either a JDialog or JFileChooser object!");

    m_comboBoxData = comboData;

    // initializes the type of editor; throws IllegalArgumentException if editor
    // type is not the specified int value
    try
    {
    // match the editor to the editorType
      initializeEditor(editorType);
    }
    catch (IllegalArgumentException e)
    {
      System.out.println("Entered incorrect Editor Type!");
      m_comboBox   = null;
      m_textField  = new JTextField();
      m_listEditor = new EditableListBoxEditor(m_textField);
    }

    // Dialog buttons implementation
    try {
        setLeftButton(buttonType);
    }
    catch (IllegalArgumentException e)
    { System.out.println("Entered incorrect button Type!"); }

    initListBox(listData);
  }



/** This constructor allows different Editors for the EditableListBox to use.
*/
  public EditableListBox(String title, Component dialog, Object[] listData,
                         Object[] comboData, TableCellEditor editor, String buttonType)
  {
    super();
    setTitle(title);

    if ( dialog instanceof JDialog )
    {
      m_browseDialog = (JDialog)dialog;
      m_fChooser = null;
    }
    else if ( dialog instanceof JFileChooser )
    {
      m_fChooser = (JFileChooser)dialog;
      m_browseDialog = null;
    }
    else
    {
      m_browseDialog = null;
      m_fChooser = null;
    }
      //throw new IllegalArgumentException("The dialog reference must be either a JDialog or JFileChooser object!");

    m_comboBoxData = comboData;

    m_listEditor = editor;

    // Dialog buttons implementation
    try
    {
      setLeftButton(buttonType);
    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }

    initListBox(listData);
  }



  /**
  *@return the index of the user selection, -1 no selection 0~n the
  *selected item
  *
  */
  public int lastSelectedItem()
  {
    int iRet=-1;
    if( m_comboBox != null )
    {
       iRet=m_comboBox.getSelectedIndex();
    }
    return(iRet);
  }
//
// Property Methods
//

/** Enables cell editing.  NOTE: does not seem to work correctly. weird.
  *
  * @param b true = cell editing on; false = cell editing off.
*/

  public void setCellEditorEnabled(boolean b)
  {
     for (int i = 0; i < m_dataModel.getColumnCount(); i++)
     {
        TableColumn column = m_list.getColumn(m_dataModel.getColumnName(i));
        TableCellEditor editor = column.getCellEditor();

        if (editor instanceof EditableListBoxEditor)
           ((EditableListBoxEditor)editor).getComponent().setEnabled(b);
        else if (editor instanceof EditableListBoxBrowseEditor)
           ((EditableListBoxBrowseEditor)editor).getComponent().setEnabled(b);
     }
  }

/** Enables cell selection.
  * @param b true = cell selection on; false = cell selection off.
*/
  public void setCellSelectionEnabled(boolean b)
  {
     m_list.setCellSelectionEnabled(b);
  }

/** Removes all the headers of the table.
*/

  public void removeTableHeaders()
  {
     m_list.setTableHeader(new JTableHeader());
  }

/** Adds new column to existing table and sets the Editor + Renderer.
  *
  * @param columnName The Object to be placed at the header of the column.
*/
  public void addColumn(Object columnName)
  {
     m_dataModel.addColumn(columnName);

     for (int i = 0; i < m_dataModel.getColumnCount(); i++)
     {
        TableColumn column = m_list.getColumn(m_dataModel.getColumnName(i));

        column.setCellEditor(m_listEditor);
        EditableListBoxCellRenderer ColumnRenderer = new EditableListBoxCellRenderer();
        column.setCellRenderer(ColumnRenderer);
     }
  }

/** Sets new column identifiers (titles).
  *
  * @param newIdentifiers An Object array of new column headers.
*/
  public void setColumnIdentifiers(Object[] newIdentifiers)
  {
     m_dataModel.setColumnIdentifiers(newIdentifiers);
  }

/** Checks if the current selected row is the last cell.
  *
  * @returns boolean true = is last cell; false = is not last cell.
*/
  public boolean isLastCell()
  {
     return m_list.getSelectedRow() == m_list.getRowCount() - 1;
  }

/** Adds Item at the end of list, before the blank cell (multi-column version).
  *
  * @param o The Object to be added.
  * @param column The column where the Object shall be added.
*/

  public void addRowValue(Object o, int column)
  {
     Object[] row = new Object[column];
     row[0] = o;

     //((DefaultTableModel)m_list.getModel()).insertRow(m_list.getRowCount() - 1, row);
     ((DefaultTableModel)m_list.getModel()).addRow(row);
  }

/** Adds Item at the end of list, before the blank cell (single-column version).
  *
  * @param o The Object to be added.
*/

  public void addRowValue(Object o)
  {
     Object[] row = new Object[1];
     row[0] = o;

     //((DefaultTableModel)m_list.getModel()).insertRow(m_list.getRowCount() - 1, row);
     ((DefaultTableModel)m_list.getModel()).addRow(row);
  }

/** Removes item at specified index.  It removes the row across all columns.
  *
  * @param index The int index of the row to be removed.
*/

  public void removeItemAt(int index)
  {
     int i = m_list.getRowCount() - 1;

     ((DefaultTableModel)m_list.getModel()).removeRow(index);
    // ((DefaultTableModel)m_list.getModel()).moveRow(i, i, m_list.getRowCount() - 1);
  }

/** Sets value at specified row index, multi-column version.
  *
  * @param o The Object to be stored at the indices.
  * @param index The int index of the row.
  * @param column The int index of the column.
*/
  public void setItemAt(Object o, int index, int column)
  {
     m_list.setValueAt(o, index, column);
  }

/** Sets value at specified row index, single-column version.
  *
  * @param o The Object to be stored at the index.
  * @param index The int index of the row.
*/
  public void setItemAt(Object obj, int index)
  {
     m_list.setValueAt(obj, index, 0);
  }

/** gets the length of the EditableListBox list. */
  public int getItemCount() { return m_list.getRowCount(); }


  public JButton getCellBrowseButton()
  {
     if (m_listEditor instanceof EditableListBoxBrowseEditor)
        return ((EditableListBoxBrowseEditor)m_listEditor).getBrowseButton();
     else
        return null;
  }

/** gets the cellRenderer used for the list in EditableListBox. */
  public TableCellRenderer getCellRenderer()
  {
    return m_list.getColumn(PLUG).getCellRenderer();
  }

  public void setCellRenderer(TableCellRenderer renderer)
  {
     m_list.getColumn(PLUG).setCellRenderer(renderer);
  }

  public TableCellRenderer getCellRenderer(int column)
  {
    return m_list.getColumn(m_dataModel.getColumnName(column)).getCellRenderer();
  }

  public void setCellRenderer(TableCellRenderer renderer, int column)
  {
     m_list.getColumn(m_dataModel.getColumnName(column)).setCellRenderer(renderer);
  }

/** @returns <CODE>true</CODE> if empty last cell is enabled; <CODE>false</CODE>
  * if it is not. */
  public boolean isEmptyEndCellEnabled() { return m_emptyEndCellEnabled; }

/** @returns The browse dialog brought up by the browseButton. */
  public JDialog getBrowseDialog()  { return m_browseDialog; }

/** @returns The browse file chooser brought up by the browseButton. */
  public JFileChooser getFileChooser()  { return m_fChooser; }

/** @params d A JDialog brought up by the browseButton. */
  public void setBrowseDialog(JDialog d)
  {
     m_fChooser = null;
     m_browseDialog = d;
  }

/** @params d A JFileChooser brought up by the browseButton. */
  public void setFileChooser(JFileChooser d)
  {
     m_browseDialog = null;
     m_fChooser = d;
  }

/** Get the value of cell, multi-column version.
  *
  * @param row The row index.
  * @param column The column index.
  *
  * @returns Object The Object at the specified indices.
*/
  public Object getRowValue(int row, int column)
  { return m_list.getValueAt(row, column); }

/** Get the value of cell, single-column version.
  *
  * @param row The row index.
  *
  * @returns Object The Object at the specified index.
*/
  public Object getRowValue(int row)
  { return m_list.getValueAt(row, 0); }

/** Get the value of cell, single-column version.
  *
  * @param o The Object to be stored in Table.
  * @param column The column index.
*/
  public void setRowValue(Object o, int index)
  {
     m_list.removeEditor();
     m_list.setValueAt(o, index, 0);
  }

/** Set the value of cell, multi-column version.
  *
  * @param o The Object to be stored at the specified indices.
  * @param index The row index.
  * @param column The column index.
*/
  public void setRowValue(Object o, int index, int column)
  {
     m_list.removeEditor();
     m_list.setValueAt(o, index, column);
  }

/** Inserts the value of cell, single-column version.
  *
  * @param o The Object to be stored at the specified indices.
  * @param index The row index.
*/
  public void insertRowValue(int index, Object o)
  {
     Object[] oArray = new Object[1];
     oArray[0] = o;
     ((DefaultTableModel)m_list.getModel()).insertRow(index, oArray);
  }

/** Inserts the value of cell, multi-column version.
  *
  * @param o The Object to be stored at the specified indices.
  * @param index The row index.
  * @param column The column index.
*/
  public void insertRowValue(int index, int column, Object o)
  {
     Object[] oArray = new Object[column];
     oArray[0] = o;
     ((DefaultTableModel)m_list.getModel()).insertRow(index, oArray);
  }

  // get/set the tableModel.
  public TableModel getListModel() { return m_list.getModel(); }
  public void setListModel(TableModel tm) { m_list.setModel(tm); }

  // get/set the JTabel (list).
  public JTable getList() {return m_list;}
  public void setList(JTable t) {m_list = t;}

  // add/remove methods for additional ListSelectionListeners used by containers
  // of the EditableListBox
  public void addListSelectionListener (ListSelectionListener l)
  { m_list.getSelectionModel().addListSelectionListener(l); }
  public void removeListSelectionListener (ListSelectionListener l)
  { m_list.getSelectionModel().removeListSelectionListener(l); }

  // accessor/mutator pair for ListSelectionModel
  public ListSelectionModel getSelectionModel()  { return m_list.getSelectionModel(); }
  public void setSelectionModel(ListSelectionModel m)
  { m_list.setSelectionModel(m); }

  // accessor/mutator pair for Title
  public String getTitle() { return m_titleString; }
  public void setTitle(String title)  // need to be changed for adding "..." only
  {                                   // when Label space is not enough.
     m_titleString = title;
     if (title.length() >= 30)
        title = title.substring(0, 30) + "...";

     m_title.setText(title);
  }

  public JButton getRightButton() { return m_rightButton; }

  public JButton getLeftButton() { return m_leftButton; }
  public void setLeftButton(String buttonType)    // use constants
  {
     JButton oldButton = m_leftButton;

     if (m_leftButton != null)
        m_leftButton.removeActionListener(this);

     if (buttonType.equals(INSERTBUTTON))
     {
        m_leftButton = new JButton(new ImageIcon(getClass().getResource("images/insert.gif")));
        m_leftButton.setPreferredSize(new Dimension(20, 20));
        m_leftButton.setMinimumSize(m_leftButton.getPreferredSize());
        m_leftButton.setActionCommand("insert");
        m_leftButton.setToolTipText("Insert new entry");
        m_leftButton.addActionListener(this);
     }
     else if (buttonType.equals(BROWSEBUTTON))
     {
        m_leftButton = new JButton(new ImageIcon(getClass().getResource("images/optional.gif")));
        m_leftButton.setPreferredSize(new Dimension(20, 20));
        m_leftButton.setMinimumSize(m_leftButton.getPreferredSize());
        m_leftButton.setActionCommand("insertbrowse");
        m_leftButton.setToolTipText("Browse for entry");
        m_leftButton.addActionListener(this);
     }
     else
        throw new IllegalArgumentException();
  }

/** @returns Component Retrieves the cell editor&apos;s editing component
  * (JTextField or JComboBox).
*/
  public Component getCellEditorComponent()
  {
    
    if (m_listEditor instanceof EditableListBoxEditor)
      return ((EditableListBoxEditor)m_listEditor).getComponent();
    else
      return ((EditableListBoxBrowseEditor)m_listEditor).getComponent();
  }


/** @param columnIdentifier The string that identifies the columns.
  * @returns Component Retrieves the cell editor&apos;s editing component
  * (JTextField or JComboBox).
*/
  public Component getCellEditorComponent(String columnIdentifier)
  {
    TableCellEditor columnEditor = m_list.getColumn(columnIdentifier).getCellEditor();

    if (columnEditor instanceof EditableListBoxEditor)
      return ((EditableListBoxEditor)columnEditor).getComponent();
    else
      return ((EditableListBoxBrowseEditor)columnEditor).getComponent();
  }  
  

/** 
  * @param int The editorType component, either JTextField or JComboBox.
  * 
*/
  public void setCellEditorComponent(int editorType)
  {
    initializeEditor(editorType);

    m_list.getColumn(PLUG).setCellEditor(m_listEditor);
  }


/** @param String columnIdentifier The string that identifies the columns.
  * @param int The editorType component, either JTextField or JComboBox.
*/
  public void setCellEditorComponent(String columnIdentifier, int editorType)
  {
    initializeEditor(editorType);

    m_list.getColumn(columnIdentifier).setCellEditor(m_listEditor);
  }
     

/** @returns TableCellEditor The current editor used by the EditableListBox.
*/
  public TableCellEditor getCellEditor()
  {
    return m_listEditor;
  }

/** @param editor The new editor that the EditableListBox wants to use.
*/
  public void setCellEditor(TableCellEditor editor)
  {
    m_listEditor = editor;
  }

/** Should only be used to get m_comboBoxData member variable.  If you want
  * to get the data list in the comboBox editor, use getCellEditor().
*/
  /*
  public Object[] getComboBoxData() { return m_comboBoxData; }
  public void setComboBoxData(Object[] data) { m_comboBoxData = data; }
  */

  public Color getGridColor() { return m_list.getGridColor(); }
  public void setGridColor(Color color) { m_list.setGridColor(color); }

/** Sees if the dialog is read-only
*/

  public boolean isReadOnly() { return m_readOnly; }

/** Sets the read-only option on the dialog.
*/

  public void setReadOnly(boolean readOnly)
  {
    if (readOnly)
    {
      if (m_listEditor instanceof EditableListBoxEditor)
      {
         ((EditableListBoxEditor)m_listEditor).getComponent().setEnabled(false);
      }
      else if (m_listEditor instanceof EditableListBoxBrowseEditor)
      {
         ((EditableListBoxBrowseEditor)m_listEditor).getComponent().setEnabled(false);
         ((EditableListBoxBrowseEditor)m_listEditor).setClickCountToStart(Integer.MAX_VALUE);
      }
      m_list.setEnabled(false);
      m_leftButton.setEnabled(false);
      m_rightButton.setEnabled(false);
    }
    else {
     if (m_listEditor instanceof EditableListBoxEditor)
      {
         ((EditableListBoxEditor)m_listEditor).getComponent().setEnabled(true);
      }
      else if (m_listEditor instanceof EditableListBoxBrowseEditor)
      {
         ((EditableListBoxBrowseEditor)m_listEditor).getComponent().setEnabled(true);
         ((EditableListBoxBrowseEditor)m_listEditor).setClickCountToStart(2);
      //   ((EditableListBoxBrowseEditor)m_listEditor).getComponent().setEditable(true);
      }
      m_list.setEnabled(true);
      m_leftButton.setEnabled(true);
      m_rightButton.setEnabled(true);
    }
    m_readOnly = readOnly;
  }

  /**
    * Sets the EditableListBox's selection mode to allow only single selections, a single
    * contiguous interval, or multiple intervals.    
    *    
    * NOTE:<br>    
    * EditableListBox provides all the methods for handling column and row selection.    
    * When setting states, such as setSelectionMode, it not only    
    * updates the mode for the row selection model but also sets similar    
    * values in the selection model of the columnModel.    
    * If you want to have the row and column selection models operating    
    * in different modes, set them both directly.    
    * <p>    
    * See setSelectionMode() in JList for details about the modes.    
    *    
    * @see javax.swing.JList#setSelectionMode
    * @beaninfo    
    * description: The selection mode used by the row and column selection models.    
    *        enum: SINGLE_SELECTION            ListSelectionModel.SINGLE_SELECTION    
    *              SINGLE_INTERVAL_SELECTION   ListSelectionModel.SINGLE_INTERVAL_SELECTION    
    *              MULTIPLE_INTERVAL_SELECTION ListSelectionModel.MULTIPLE_INTERVAL_SELECTION    
    */
  public void setSelectionMode(int selectionMode) 
  {        
     m_list.clearSelection();
     m_list.getSelectionModel().setSelectionMode(selectionMode);       
     m_list.getColumnModel().getSelectionModel().setSelectionMode(selectionMode);
  }

/** Implements the ActionListener interface and activates the response to the
  * 3 different buttons possible on the dialog.
  *
*/

   public void actionPerformed(ActionEvent e)
   {

      if (e.getActionCommand().equals(INSERTBUTTON))
      {
         int lastCellIndex = getListModel().getRowCount() - 1;

         // check for an "empty end cell" setting or an empty-end cell already
         // exists. (checks the stored item's toString() returned is an empty
         // String. This way, no matter what kind of object may be stored, a
         // consistent method of checking exists for all cases.)
         if (0 <= lastCellIndex) // if rows exist...
         {
            if (!getCellEditorComponent().isShowing())
            {
               Object value = getRowValue(lastCellIndex);
               if (isEmptyEndCellEnabled() || null == value || 
                  0 == value.toString().length())
               {
                  m_list.editCellAt(lastCellIndex, 0);
                  m_list.getSelectionModel().setSelectionInterval(lastCellIndex, 
                     lastCellIndex);
               }
               else
               {
                  if (null != m_dataExchange)
                     addRowValue(m_dataExchange.createNewInstance());
                  else // default to String
                     addRowValue(null);
                  lastCellIndex = getListModel().getRowCount() - 1;

                  m_list.editCellAt(lastCellIndex, 0);
                  m_list.getSelectionModel().setSelectionInterval(lastCellIndex, 
                     lastCellIndex);
               }
            }
            else
               return;
         }
         else // no rows exists yet
         {
            if (null != m_dataExchange)
               addRowValue(m_dataExchange.createNewInstance());
            else // default to String
               addRowValue(null);
            lastCellIndex = getListModel().getRowCount() - 1;

            m_list.editCellAt(lastCellIndex, 0);
            m_list.getSelectionModel().setSelectionInterval(lastCellIndex, 
               lastCellIndex);
         }
         // set focus to the CellRenderer
         // TODOph: this should be polymorphic on the cell editor
         if ( null != m_comboBox )
            m_comboBox.getEditor().getEditorComponent().requestFocusInWindow();
         else if ( null != m_textField )
            m_textField.requestFocusInWindow();
         
         // shouldn't need to do this later, but that's the only way it seems
         // to work
         final int scrollRow = lastCellIndex;
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               m_pane.getVerticalScrollBar().setValue(
                  m_pane.getVerticalScrollBar().getMaximum());
               // once the scroll bar appears, sometimes this stops the cell
               // editing
               if (!m_list.isEditing())
                  m_list.editCellAt(scrollRow, 0);
            }
         });
      }
      else if (e.getActionCommand().equals("insertbrowse"))
      {
         // need to add browse reference
         //System.out.println("InsertBrowse button hit!");
         if (null != m_browseDialog)
            m_browseDialog.setVisible(true);
         else if (null != m_fChooser)
            m_fChooser.setVisible(true);
      }
      else if (e.getActionCommand().equals(BROWSEBUTTON))
      {
         //System.out.println("(Outer) Browse Button was Hit!");
      }
   }

/** Adds empty end cells to all available columns.
*/
   public void addEmptyEndCell()
   {
       int column = m_dataModel.getColumnCount();

       Object[] obj = new Object[column];
       for (int i = 0; i < column; i++)
        obj[i] = new String("");

     m_dataModel.addRow(obj);

     m_emptyEndCellEnabled = true;
  }


/** @returns IDataExchange The object that was implemented in setDataExchange.
  * This may be <CODE>null</CODE>.  If <CODE>null</CODE>, the editor will create
  * a String object by default.
*/
  public IDataExchange getDataExchange()
  {
    return m_dataExchange;
  }

  
/** Sets the specified data object implemented in 
  * IDataExchange.createNewInstance() method.  If <CODE>null</CODE>, a String 
  * will be created on default.
*/
  public void setDataExchange(IDataExchange datax)
  {
    m_dataExchange = datax;
  }
  

//
// Private Methods
//

/** Filters the input array into a two dimensional array (required input for
  * JTable).  This method is developed for easing programmer's understanding
  * of how this List works.
*/
  private Object[][] filterListData(Object[] data)
  {
     Object[][] filter = new Object[data.length][1];
     for (int i = 0; i < data.length; i++)
         filter[i][0] = data[i];

     return filter;
  }

/** Performs the delete functionality of the delete button.
*/
  public void deleteRows()
  {
     int[] selectedRows = m_list.getSelectedRows();

     if (null != m_list.getCellEditor())
       m_list.getCellEditor().stopCellEditing();
     
     // loop backwards to prevent "changed vector deletion error"...
     for (int i = (selectedRows.length - 1); i >= 0; i--)
     {
        Object item = m_dataModel.getValueAt(selectedRows[i], 0);

        // prevents the deletion of last row
        /*
        if ((m_list.getRowCount() - 1) != selectedRows[i])
        {
        */
           if (item instanceof EditableListBoxCellEditHelper)  // checking if cell is editable
           {
              if (((EditableListBoxCellEditHelper)item).isCellEditable())
                 m_dataModel.removeRow(selectedRows[i]);
           }
           else
              m_dataModel.removeRow(selectedRows[i]);
        /*
        }
        */
     }
  }

/** Private method to initialize the particular editor with the constructor input
  * integer.  Checks for illegal integer entry.
  *
  * @throws IllegalArgumentException
*/
  private void initializeEditor(int componentType) throws IllegalArgumentException
  {
    if (componentType == TEXTFIELD)
    {
      m_comboBox = null;
      m_textField = new JTextField();
      m_textField.setEditable(true);
      m_listEditor = new EditableListBoxEditor(m_textField);
    }
    else if (componentType == COMBOBOX)
    {
      m_textField = null;

      if (m_comboBoxData != null)
        m_comboBox = new JComboBox(m_comboBoxData);
      else
        m_comboBox = new JComboBox();

      m_comboBox.setEditable(true);
      m_listEditor = new EditableListBoxEditor(m_comboBox);
    }
    else if (componentType == DROPDOWNLIST)
    {
      m_textField = null;

      if (m_comboBoxData != null)
        m_comboBox = new JComboBox(m_comboBoxData);
      else
        m_comboBox = new JComboBox();

      m_comboBox.setEditable(false);
      m_listEditor = new EditableListBoxEditor(m_comboBox);
    }
    else if (componentType == BROWSEBOXEDIT)
    {
      m_comboBox = null;
      m_textField = new JTextField();
      m_textField.setEditable(true);

      if ( null != m_browseDialog )
         m_listEditor = new EditableListBoxBrowseEditor(m_textField, m_browseDialog);
      else if ( null != m_fChooser )
         m_listEditor = new EditableListBoxBrowseEditor(m_textField, m_fChooser);
      else
         m_listEditor = new EditableListBoxBrowseEditor(m_textField, null);
     // ((EditableListBoxBrowseEditor)m_listEditor).getBrowseButton().addActionListener(this);
    }
    else if (componentType == BROWSEBOXLIST)
    {
      m_textField = null;

      if (m_comboBoxData != null)
        m_comboBox = new JComboBox(m_comboBoxData);
      else
        m_comboBox = new JComboBox();
        
      m_comboBox.setEditable(true);
      if ( null != m_browseDialog )
         m_listEditor = new EditableListBoxBrowseEditor(m_textField, m_browseDialog);
      else if ( null != m_fChooser )
         m_listEditor = new EditableListBoxBrowseEditor(m_textField, m_fChooser);
      else
         m_listEditor = new EditableListBoxBrowseEditor(m_textField, null);
     // ((EditableListBoxBrowseEditor)m_listEditor).getBrowseButton().addActionListener(this);
    }
    else
        throw new IllegalArgumentException();
  }

/** Initialization method for the constructor.
*/
  private void initListBox(Object[] listData)
  {
    m_rightButton = new JButton(new ImageIcon(getClass().getResource("images/delete.gif")));
    m_rightButton.setPreferredSize(new Dimension(20, 20));
    m_rightButton.setMinimumSize(m_rightButton.getPreferredSize());
    //m_rightButton.setActionCommand("delete");
    m_rightButton.setToolTipText("Delete");
    m_rightButton.addActionListener(this);

    // Create a model of the data.
    m_dataModel = new DefaultTableModel();   //filterListData(listData), header);

    // *** add columns here
    m_dataModel.addColumn("");

    // *** then add row data for each column
    if (listData != null)
    {
      for (int i = 0; i < listData.length; i++)
      {
        // ADDING rows, like appending. So only need an array each time.
        Object[] obj = new Object[1];
        obj[0] = listData[i];
        m_dataModel.addRow(obj);
      }
    }

    //addEmptyEndCell();     // adds empty row at end of list

    m_list = new JTable(m_dataModel);

    // Table Header disabled; therefore no column dragging or column resizing
    m_list.getTableHeader().setReorderingAllowed(false);
    m_list.getTableHeader().setResizingAllowed(false);
    m_list.setCellSelectionEnabled(true);

    // Customizing the column rendering of all columns to
    // EditableListBoxCellRenderer
    for (int i = 0; i < m_dataModel.getColumnCount(); i++)
    {
       TableColumn column = m_list.getColumn(m_dataModel.getColumnName(i));

       column.setCellEditor(m_listEditor);
       EditableListBoxCellRenderer ColumnRenderer = new EditableListBoxCellRenderer();
       column.setCellRenderer(ColumnRenderer);
    }

    // Layout panel for the 2 buttons next to the title bar
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.add(m_leftButton);
    buttonPanel.add(m_rightButton);
    buttonPanel.add(Box.createHorizontalGlue());

    // Layout panel for the Title bar + 2 buttons
    m_title.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED,
                                                         Color.white,
                                                         Color.gray,
                                                         Color.darkGray,
                                                         Color.black),
                                         new EmptyBorder(0, 5, 0, 0)));
    m_panel.setLayout(new BorderLayout());
    m_panel.add(m_title, BorderLayout.CENTER);
    m_panel.add(buttonPanel, BorderLayout.EAST);

    // putting the ComboList onto a scrollable pane
    m_list.setOpaque(true);
    m_list.setBackground(Color.white);
    m_pane = new JScrollPane(m_list);
    
   if(m_list.getParent() != null)
      m_list.getParent().setBackground(Color.white);
    
    // setting layout for the dialog
    JPanel mainLayout = new JPanel(new BorderLayout());

    mainLayout.add(m_panel, BorderLayout.NORTH);
    mainLayout.add(m_pane, BorderLayout.CENTER);
    mainLayout.setBackground(Color.white);

    this.setLayout(new BorderLayout());
    this.add(mainLayout, BorderLayout.CENTER);

    // setup the dialog preference
    setReadOnly(false);

    setGridColor(Color.white);
    setBackground(Color.white);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

  }

//
// Variables
//
  private static final String PLUG = ""; // used to "plug" parameters for num of Columns

  public static final int COMBOBOX      = 0;
  public static final int TEXTFIELD     = 1;
  public static final int DROPDOWNLIST  = 2; // ComboBox made unEditable
  public static final int BROWSEBOXEDIT = 3; // TextField with button
  public static final int BROWSEBOXLIST = 4; // ComboBox with button

  public static final String INSERTBUTTON  = "insert";
  public static final String BROWSEBUTTON  = "browse";

  private DefaultTableModel  m_dataModel;
  private TableCellEditor    m_listEditor;
  private JTable             m_list; // using a table with a single column to simulate list

  private JLabel      m_title = new JLabel();
/** Button to the right; The delete button */
  private JButton     m_rightButton;

/** Button to the left; Either the insert or browse button */
  private JButton     m_leftButton;

  private JPanel      m_panel = new JPanel();

  private JTextField  m_textField;
  private JComboBox   m_comboBox;

  private IDataExchange m_dataExchange = null;

  private JDialog     m_browseDialog = null;
  private JFileChooser m_fChooser = null;

  private Object[]    m_comboBoxData;

  private boolean     m_insertStatus = true;
  private boolean     m_readOnly = false;
  private boolean     m_emptyEndCellEnabled = false;

  private String      m_titleString;

  private int         m_width;
  private int         m_height;

  /**
   * The Scroll pane for the list box, not <code>null</code> after construction.
   */
  private JScrollPane m_pane;

}    // end class ListExample

