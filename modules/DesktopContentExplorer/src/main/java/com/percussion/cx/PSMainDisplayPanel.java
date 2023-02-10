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

package com.percussion.cx;

import com.percussion.border.PSFocusBorder;
import com.percussion.cms.objectstore.PSDisplayColumn;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cx.error.PSContentExplorerException;
import com.percussion.cx.guitools.PSMouseAdapter;
import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.cx.objectstore.PSNode;
import com.percussion.guitools.PSTableSorter;
import com.percussion.util.PSStringOperation;
import com.percussion.utils.collections.PSIteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.CellEditor;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * The main display panel that is used to display a table that represents
 * children of a node according to the node's display format. Supports pop-up
 * menu based on selection.
 */
public class PSMainDisplayPanel extends JScrollPane
   implements DragGestureListener, DropTargetListener
{
   static Logger log = Logger.getLogger(PSMainDisplayPanel.class);
   
   /**
    * Constructs the panel with supplied parameters.
    *
    * @param view the current view of applet, must be one of the <code>
    * PSUiMode.TYPE_VIEW_xxx</code> values.
    * @param actManager the action manager to use to support context menu, may
    * not be <code>null</code>
    * @param isSorted <code>true</code> to allow the table to be sorted,
    * <code>false</code> to maintain the supplied order of the data.
    */
   public PSMainDisplayPanel(String view, PSActionManager actManager,
      boolean isSorted)
   {
      if(!PSUiMode.isValidView(view))
         throw new IllegalArgumentException("view is not valid.");
      if(actManager == null)
         throw new IllegalArgumentException("actManager may not be null.");
      
      if(actManager.getApplet() == null)
         throw new IllegalArgumentException("applet may not be null.");

      m_view = view;
      m_actManager = actManager;
      m_applet = m_actManager.getApplet();
      m_isSorted = isSorted;
      
      this.setFocusTraversalKeysEnabled(false);

      init();

      //Select the first row by default, if exists.
      if(m_childViewTable.getRowCount() > 0)
         m_childViewTable.setRowSelectionInterval(0,0);
   }

   /**
    * Determines if sorting is currently enabled.
    *
    * @return <code>true</code> if currently enabled, <code>false</code>
    * otherwise.
    */
   public boolean isSortingEnabled()
   {
      return m_childViewTableModel.isSortingEnabled();
   }

   /**
    * Enables or disables sorting.  If disabled, then the will not allow
    * sorting, if enabled, it will.
    *
    * @param isEnabled If <code>true</code>, sorting will be enabled, otherwise
    * it will be disabled.
    */
   public void setIsSortingEnabled(boolean isEnabled)
   {
      m_childViewTableModel.setIsSortingEnabled(isEnabled);
   }

   /**
    * Initializes this panel by adding a table that supports displaying data
    * using data node's display format and allows sorting of columns.
    */
   private void init()
   {
      //create table sorter model with data model and set on the table to allow
      //sorting.
      PSDisplayFormatTableModel model = new PSDisplayFormatTableModel(m_applet);
      model.setLocale(m_applet.getUserInfo().getLocale());
      m_childViewTableModel = new PSNodeTableSorter(model);
      m_childViewTableRenderer = new MainDisplayTableCellRenderer();
      m_childViewTable = new JTable(m_childViewTableModel)
      {
         @Override
         public TableCellRenderer getCellRenderer(
               @SuppressWarnings("unused") int row, 
               @SuppressWarnings("unused") int column)
         {
            return m_childViewTableRenderer;
         }
         /* Override to set the row height based on the metrics of the font
          * to be set.
          * @see javax.swing.JComponent#setFont(java.awt.Font)
          */
         @Override
         public void setFont(Font font)
         {
            LineMetrics lm = font.getLineMetrics("", new FontRenderContext(
                  null, true, true));
            int ht = (int) lm.getHeight() + 2;
            // Set the height to a minimum of 17 pixels
            ht = ht < 17 ? 17 : ht;
            setRowHeight(ht);
            super.setFont(font);
         }

      };

      keyboardHandler = new TableKeyBoardHandler();
      //Override the mouse input listener to handle multi select and drag.
      m_childViewTable.setUI(new BasicTableUI()
      {
         @Override
         protected MouseInputListener createMouseInputListener()
         {
            return new TableMouseInputHandler();
         }
          
         @Override
         protected KeyListener createKeyListener()
         {
            return keyboardHandler;
         }

      });
      
      
      m_childViewTable.setShowGrid(false);
      
      m_childViewTable.addKeyListener(keyboardHandler);     
      m_childViewTable.getTableHeader().setReorderingAllowed(false);
      m_childViewTable.getTableHeader().setFocusable(true);
      m_childViewTable.getTableHeader().setDefaultRenderer(
         new MainDisplayTableHeaderRenderer());

      disableNormalKeyFunctions();
 	 
      ListSelectionModel selectionModel = m_childViewTable.getSelectionModel();
      selectionModel.addListSelectionListener(new ListSelectionListener()
      {
         
         public void valueChanged(ListSelectionEvent e)
         {
            Iterator<PSNode> selNodes = getSelectedRowNodes();
            createPopupMenu(selNodes);
         }
      });
      
      PopupListener popupListener = new PopupListener();

      //add model as listener to mouse clicks on table.
      m_childViewTableModel.addMouseListenerToHeaderInTable(m_childViewTable);
      m_childViewTableModel.addKeyListenerToHeaderInTable(m_childViewTable);
      m_childViewTable.addMouseListener(popupListener);

      // add sort listener to save sort columns in node
      m_childViewTableModel.addTableModelListener(new TableModelListener()
      {
         public void tableChanged(TableModelEvent e)
         {
            if (e.getSource() == m_childViewTableModel)
            {
               // save sorting info if we have any 
               PSNode root = getDataModel().getRoot();
               if (root != null)
               {
                  root.setLastSortColumns(
                     m_childViewTableModel.getSortingColumns());
                  root.setLastSortedAsc(m_childViewTableModel.isAscending());
               }               
            }            
         }}
      );      

      // add a column model listener to save the column widths after adjustment
      JTableHeader th = m_childViewTable.getTableHeader();
      
      th.addMouseListener(new PSTableColumnResizeHandler());

      setViewportView(m_childViewTable);

      m_childViewTable.setBackground(PSCxUtil.getWindowBkgColor(m_applet));
      getViewport().setBackground(PSCxUtil.getWindowBkgColor(m_applet));

      getViewport().addMouseListener(popupListener);

      if(m_actManager.viewSupportsCopyPaste())
      {
         KeyStroke ksCTLCKeyRelease =
            KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK, true);
         m_childViewTable.getInputMap().put(ksCTLCKeyRelease, "ctlcAction");
         AbstractAction ctlcAction = new AbstractAction()
         {
            public void actionPerformed(
                  @SuppressWarnings("unused") ActionEvent e)
            {
               Iterator<PSNode> selNodes = getSelectedRowNodes();
               if(selNodes.hasNext())
               {
                  PSSelection sel = new PSSelection(
                     new PSUiMode(m_view, ms_mode), m_parentNode, selNodes);
                  m_actManager.getClipBoard().setClip(
                     PSClipBoard.TYPE_COPY, sel);
               }
            }
         };
         m_childViewTable.getActionMap().put("ctlcAction", ctlcAction);
         
         KeyStroke ksCTLVKeyRelease =
            KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK, true);
         AbstractAction ctlvAction = new AbstractAction()
         {
            public void actionPerformed(ActionEvent e)
            {
               Iterator<PSNode> selNodes = 
                  getSelectedNodes((Component)e.getSource(), null);
               if(selNodes != null && selNodes.hasNext())
               {
                  String pasteAction = PSMenuAction.PREFIX_COPY_PASTE +
                     IPSConstants.ACTION_PASTE;

                  PSMenuAction action = new PSMenuAction(
                     pasteAction, pasteAction,
                     PSMenuAction.TYPE_MENU, "", PSMenuAction.HANDLER_CLIENT, 0);

                  Point loc;
                  if(e.getSource() == PSMainDisplayPanel.this.getViewport())
                  {
                     Rectangle rect = PSMainDisplayPanel.this.getViewport().getBounds();
                     loc = new Point(
                        (int)(rect.getLocation().getX() + (rect.getWidth()/2)),
                        (int)(rect.getLocation().getY() + (rect.getHeight()/2)) );
                  }
                  else
                  {
                     int row = m_childViewTable.getSelectedRow();
                     loc = new Point(
                        (m_childViewTable.getX() + m_childViewTable.getWidth()/2),
                        (m_childViewTable.getRowHeight() * (row+1)));
                  }

                  displayPopupMenu((Component)e.getSource(), action, selNodes, loc);
               }
            }
         };
         m_childViewTable.getInputMap().put(ksCTLVKeyRelease, "ctlvAction");
         m_childViewTable.getActionMap().put("ctlvAction", ctlvAction);

         getViewport().getInputMap().put(ksCTLVKeyRelease, "ctlvAction");
         getViewport().getActionMap().put("ctlvAction", ctlvAction);


         DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
            m_childViewTable, DnDConstants.ACTION_COPY_OR_MOVE,
            this);

         new DropTarget(getViewport(), DnDConstants.ACTION_COPY_OR_MOVE, this);
         new DropTarget(m_childViewTable, DnDConstants.ACTION_COPY_OR_MOVE, this);
      }

      m_messageText = new JTextArea(1,80);
      m_messageText.setEditable(false);

   }

   /**
    * Sets the node whose children need to be displayed in this panel's table.
    * This refreshes the panel with new data.  Table is sorted by the first
    * column, ascending, and column widths are set based on either persisted
    * user data, or, if none have been persisted, the node's display format.
    *
    * @param node the node whose children need to be displayed, may not be
    * <code>null</code>
    * @param parent the parent of the supplied node, may be <code>null</code>,
    * need to be supplied to pop-up actions that are shown when the mouse-click
    * is on view port.
    *
    * @throws IllegalArgumentException if node is <code>null</code>.
    */
   public void setData(PSNode node, PSNode parent)
   {
      if(node == null)
         throw new IllegalArgumentException("node may not be null.");

      m_parentNode = node;
      m_parentParent = parent;
      PSDisplayFormat df = null;
      
      // see if new node has sorting info saved
      List sortingInfo = node.getLastSortColumns();
      boolean sortAscending = node.getLastSortedAsc();
      
      // if no previous sort info, try to get initial sort column
      boolean keepSorting = false;
      if (sortingInfo != null)
      {
         m_childViewTableModel.setSortingColumns(sortingInfo);
         m_childViewTableModel.setAscending(sortAscending);
         keepSorting = true;
      }
      else
      {
         try
         {
            String dfId = node.getDisplayFormatId();
            if (dfId.trim().length() > 0)
            {
               df = m_applet.getActionManager().getDisplayFormatCatalog().
                  getDisplayFormatById(dfId);
               String col = df.getPropertyValue(
                  PSDisplayFormat.PROP_SORT_COLUMN);
               String dir = df.getPropertyValue(
                  PSDisplayFormat.PROP_SORT_DIRECTION);
               if (col != null)
               {
                  int colIndex = df.getColumnIndex(col);
                  if (colIndex != -1)
                  {
                     m_childViewTableModel.setInitialSortColumn(colIndex,
                     PSDisplayFormat.SORT_ASCENDING.equals(dir));
                  }
               }
            }
            else
            {
               // no display format, so set the default to clear previous 
               // settings
               m_childViewTableModel.setInitialSortColumn(0, true);
            }
         }
         catch (PSContentExplorerException e)
         {
            /*
             * this means we couldn't load the display format, but it was needed
             * for the search, so we wouldn't be trying to recatalog, so it should
             * never be thrown here, so ignore
             */
            e.printStackTrace();  // print out for debug purposes
         }         
      }

      // Create a new model and set that, saving selection for later restore
      Iterator<PSNode> curSelIter = getSelectedRowNodes();
      Collection<PSNode> curSel = new ArrayList<PSNode>();
      while (curSelIter.hasNext())
         curSel.add(curSelIter.next());
      PSDisplayFormatTableModel tableModel = new PSDisplayFormatTableModel(m_applet);
      tableModel.setLocale(m_applet.getUserInfo().getLocale());
      tableModel.setRoot(node);
      //this clears the selection
      m_childViewTableModel.setModel(tableModel, true, keepSorting);

      /* restore the selection, the position of any node could have changed due
       * to additions or deletions
       */
      for (PSNode n : curSel)
      {
         int idx = getMatchingRowIndex(n);
         //map to the underlying model
         if (idx >= 0)
         {
            m_childViewTable.addRowSelectionInterval(idx, idx);
         }
      }
      
      // update the column widths
      resetColumnWidths(node, df);

      // Reset any search info panel
      if (node.getChildCount() != 0)
      {
         setViewportView(m_childViewTable);
      }
      else
      {
         // Display a meaningful message if we have no children
         String msg = "";

         if (node.isSearchType())
         {
            msg = m_applet.getResourceString(
               getClass().getName() + "@NoResults");
         }
         else
         {
            msg = m_applet.getResourceString(
               getClass().getName() + "@NoItems");
         }

         m_messageText.setText(msg);
         // when options were being refreshed background when no results was
         // changing to configured background of blue.  Need to set this here.
         m_messageText.setBackground(PSCxUtil.getWindowBkgColor(m_applet));
         setViewportView(m_messageText);
         getViewport().setBackground(PSCxUtil.getWindowBkgColor(m_applet));
      }
   }

   /**
    * Resets the current column widths based on the stored column widths for
    * the specified node. If the node does not have a set of stored widths, then
    * we use any widths specified by the display format of the node.
    *
    * @param node The node to get the specific column widths, this is the same
    *    node that was used to store the column widths when they were modified.
    *    Must not be <code>null</code>.
    * @param df The display format specified by the <code>node</code>, may be
    * <code>null</code> if one is not specified.  Used to supply column widths
    * if none have been stored for the supplied node.
    */
   private void resetColumnWidths(PSNode node, PSDisplayFormat df)
   {
      if (node == null)
         throw new IllegalArgumentException("node must not be null");
      
      
      List widths = m_actManager.getApplet().getColumnWidthsFromOptions(node);

      // if no stored column widths or if the number of column widths that were
      // stored is different than the current table model, check the display
      // format for any specified width
      int colIndex;
      int defaultColWidth = -1;
      int totalWidths = 0;
      int specifiedWidthCount = 0;
      int numCols = m_childViewTableModel.getColumnCount();

      if (widths == null
         || (widths.size() != numCols))
      {
         if (df != null)
         {
            widths = new ArrayList(numCols);
            colIndex = 0;
            Iterator cols = df.getColumns();
            while (cols.hasNext() && colIndex < numCols)
            {
               PSDisplayColumn dfCol = (PSDisplayColumn)cols.next();
               String strColWidth = null;  // will add null value if not specd
               int colWidth = dfCol.getWidth();
               if (colWidth != -1)
               {
                  totalWidths += colWidth;
                  specifiedWidthCount++;
                  strColWidth = String.valueOf(colWidth);
               }
               widths.add(strColWidth);
               colIndex++;
            }
         }
         else
            return;
      }

      TableColumnModel columnModel = m_childViewTable.getColumnModel();

      // if using column widths specified by display format, calculate remainder
      int remainingColCount = columnModel.getColumnCount() -
         specifiedWidthCount;
      if (remainingColCount > 0)
      {
         int allColsWidth = getWidth();
         defaultColWidth = (allColsWidth - totalWidths) / (remainingColCount);
      }

      colIndex = 0;
      Iterator i = widths.iterator();
      while (i.hasNext())
      {
         int useWidth;
         String width = (String)i.next();
         if (width == null) // using display format, no width for this col
         {
            useWidth = (defaultColWidth > MIN_COL_WIDTH) ? defaultColWidth :
               MIN_COL_WIDTH;
         }
         else
            useWidth = Integer.parseInt(width);

         TableColumn col =
            columnModel.getColumn(colIndex);
         col.setPreferredWidth(useWidth);
         colIndex++;
      }
   }

   /**
    * Store information on starting drag.
    */
   public void dragEnter(@SuppressWarnings("unused") DropTargetDragEvent dtde)
   {
      m_saveDragSelection = m_childViewTable.getSelectedRows();
   }

   /**
    * Handle drag under effects
    */
   public void dragOver(DropTargetDragEvent dtde)
   {
      Point loc = dtde.getLocation();
      int row = m_childViewTable.rowAtPoint(loc);
      // What are we over?
      if (row >= 0)
      {
         Object obj = m_childViewTableModel.getValueAt(row, 0);
         if (obj instanceof PSNode)
         {
            PSNode n = (PSNode) obj;
            if (m_actManager.canAcceptPaste(m_view, n, PSClipBoard.TYPE_DRAG))
            {
               dtde.acceptDrag(dtde.getDropAction());
               m_childViewTable.setRowSelectionInterval(row, row);
            }
            else
            {
               dtde.rejectDrag();
               m_childViewTable.clearSelection();
            }
         }
      }
      else
      {
         dtde.rejectDrag();
         m_childViewTable.clearSelection();
      }
   }

   //implements nothing
   public void dropActionChanged(
         @SuppressWarnings("unused") DropTargetDragEvent dtde)
   {
   }

   /**
    * Reset information on exit
    */
   public void dragExit(@SuppressWarnings("unused") DropTargetEvent dte)
   {
      m_childViewTable.clearSelection();
      
      if (m_saveDragSelection != null && m_saveDragSelection.length > 0)
      {
         for (int i = 0; i < m_saveDragSelection.length; i++)
         {
            int row = m_saveDragSelection[i];
            m_childViewTable.addRowSelectionInterval(row, row);
         }
      }
   }

   /**
    * Implemented to accept the drop and show context-sensitive pop-up menu for
    * pasting. See the interface for more description of the method.
    */
   public void drop(DropTargetDropEvent dtde)
   {
      Component comp = dtde.getDropTargetContext().getComponent();
      comp.setCursor(Cursor.getDefaultCursor());

      dtde.acceptDrop(dtde.getDropAction());
      dtde.dropComplete(true);

      String pasteAction = PSMenuAction.PREFIX_DROP_PASTE +
         IPSConstants.ACTION_PASTE;

      PSMenuAction action = new PSMenuAction(
         pasteAction, pasteAction,
         PSMenuAction.TYPE_MENU, "", PSMenuAction.HANDLER_CLIENT, 0);

      Iterator<PSNode> selNodes = getSelectedNodes(comp, dtde.getLocation());
      if(selNodes != null && selNodes.hasNext())
         displayPopupMenu(comp, action, selNodes, dtde.getLocation());
      
      // Reset saved selection
      m_saveDragSelection = m_childViewTable.getSelectedRows();
      dragExit(dtde);
   }
   
   /**
    * Get the icon name for the supplied display name.
    * 
    * @param displayName the display name for which to get the icon name, 
    *    assumed not <code>null</code> or empty.
    * @return the icon name from the static display name to icon name map
    *    or the supplied display name if not found. Never <code>null</code> or 
    *    empty.
    */
   private String getIconName(String displayName)
   {
      String iconName = ms_displayNameToIconName.get(displayName);
      return iconName == null ? displayName : iconName;
   }

   /**
    * The class that handles pop-up mouse clicks to show pop-up menu for the
    * current selection.
    */
   private class PopupListener extends PSMouseAdapter
   {

      /**
       * Handles the right-click mouse event. If source of event is the child
       * table and there are no selected rows, makes the row where the event
       * occurred as selected. Displays context-menu for the selected rows. If
       * the source of event the view port of this panel, then it shows context
       * menu for the parent node whose children is displayed in this table.
       * If the parent node is not yet set, does nothing.
       *
       * @param event the mouse click event, assumed not <code>null</code> as
       * this is called by Swing model.
       */
      @Override
      public void mouseWasClicked(MouseEvent event)
      {
         if(PSCxUtil.isMouseMenuGesture(event, m_applet))
         {
            Iterator<PSNode> selNodes = getSelectedNodes(
               (Component)event.getSource(), event.getPoint());

            displayPopupMenu((Component)event.getSource(), null,
               selNodes, event.getPoint());
         }
      }
      
   }

   /**
    * Class used to listen for changes to the width of columns. Basically used
    * to save the state of the widths on the original mouse press, then checks
    * to see if anything has changed the column widths on the mouse release, if
    * so calls the main applet to store the widths into the persistant storage.
    */
   private class PSTableColumnResizeHandler extends MouseAdapter
   {
      /**
       * Handle the mouse pressed on the table column header, store all
       * the current widths of the columns. Used later on the release to
       * compare to see if any of the columns have changed.
       */
      @Override
      public void mousePressed(@SuppressWarnings("unused") MouseEvent e)
      {
         TableColumnModel colModel = m_childViewTable.getColumnModel();
         if (colModel != null)
         {
            int count = colModel.getColumnCount();
            mi_saveColWidths = new int[count];
            for (int i = 0; i < count; i++)
               mi_saveColWidths[i] = colModel.getColumn(i).getWidth();
         }
      }

      /**
       * Handle the mouse released on the table column header. Checks all the
       * current column widths with the saved ones that we retrieved when the
       * mousePressed method was called. If any of the widths are different, we
       * store all the current widths along with the node to the applet
       * persistance object.
       */
      @Override
      public void mouseReleased(@SuppressWarnings("unused") MouseEvent e)
      {
         TableColumnModel colModel = m_childViewTable.getColumnModel();
         if (colModel != null && mi_saveColWidths != null)
         {
            int count = colModel.getColumnCount();
            for (int col = 0; col < count; col++)
            {
               // if any of the column widths have changed, save the widths
               if (mi_saveColWidths[col] != colModel.getColumn(col).getWidth())
               {
                  List widths = new ArrayList();
                  for (int i = 0; i < count; i++)
                     widths.add("" + colModel.getColumn(i).getWidth());

                  // save the current column widths to the applet persistance
                  m_actManager.getApplet().saveColumnWidthsToOptions(
                     m_parentNode,
                     widths);
                  break;
               }
            }
         }
      }

      /**
       * Storage for the temporary list of column widths, set when the mouse is
       * pressed in the table header. When the mouse is released we will compare
       * the current values with the saved values in this list. If different we
       * will then store the current values to our applet persistence object.
       */
      private int mi_saveColWidths[] = null;
   }

   /**
    * Gets the selected nodes based on the supplied component. If the component
    * is table and the row at the <code>loc</code> is selected, then the
    * selected nodes are currently selected nodes in the table. If the row at
    * the <code>loc</code> is not selected, then that row will be selected and
    * returned. If the component is viewport of this panel, the parent node
    * represented by this panel is selected node.
    *
    * @param source the source component, assumed not <code>null</code> and one
    * of the above explained components.
    * @param loc the loc to find out the selected node, may be <code>null
    * </code>
    *
    * @return the selected nodes, may be <code>null</code>, never empty.
    */
   private Iterator<PSNode> getSelectedNodes(Component source, Point loc)
   {
      Iterator<PSNode> selNodes = null;
      if(source == m_childViewTable) //table
      {
         //Select the row if the row at clicked point is not selected,
         //otherwise get all selected rows.
         if(loc != null)
         {
            int row = m_childViewTable.rowAtPoint(loc);
            if(!m_childViewTable.isRowSelected(row))
               m_childViewTable.setRowSelectionInterval(row, row);
         }

         selNodes = getSelectedRowNodes();
      }
      //clicked on view port, not on table.
      else if(source == getViewport())
      {
         if(m_parentNode != null)
            selNodes = Collections.singletonList(m_parentNode).iterator();
      }

      return selNodes;
   }

   /**
    * Build a the context popup menu for the currently selected item.  
    * This is done in the background and when the user selects the menu
    * it will just need to be displayed.
    * 
    * @param an iterator of the selected nodes may be <code>null
    */
   private void createPopupMenu(Iterator<PSNode> selNodes)
   {
      if (selNodes != null && selNodes.hasNext())
      {
         PSNode parent = m_parentNode;
         PSSelection selection = new PSSelection(new PSUiMode(m_view, ms_mode), parent, selNodes);

         PSMenuAction popupMenuAction = null;
         popupMenuAction = m_actManager.getContextMenu(selection);
         if( popupMenuAction !=null) {
            PSContentExplorerMenu menu = new PSContentExplorerMenu(popupMenuAction, new PSMenuSource(selection),
                    m_actManager, false);

            backgroundMenu = new FutureTask<PSContentExplorerMenu>(menu);
            backgroundService.execute(backgroundMenu);
         }
      }
   }
   
   /**
    * Displays context menu for the supplied action and list of selected nodes.
    *
    * @param action the action that controls the pop-up menu to show, if
    * <code>null</code> the pop-up menu depends only on the selected nodes alone.
    * @param selNodes the list of selected nodes, if <code>null</code> or empty
    * no pup-up menu will be shown.
    * @param loc the location where the pop-up menu should show, assumed not
    * <code>null</code>
    */
   private void displayPopupMenu(Component comp, PSMenuAction action, Iterator<PSNode> selNodes, Point loc)
   {
      try
      {
         if (selNodes != null && selNodes.hasNext())
         {

            PSNode parent = null;
            if (comp == m_childViewTable)
               parent = m_parentNode;
            else
               parent = m_parentParent;

            PSSelection selection = null;

            PSMenuAction popupMenuAction = null;
            JPopupMenu popup = null;
            PSContentExplorerMenu menu = null;
            if (action == null)
            {
               if (backgroundMenu == null)
                  createPopupMenu(selNodes);
               if (backgroundMenu != null)
                  menu = backgroundMenu.get();
            }
            else if (action.getName().startsWith(PSMenuAction.PREFIX_DROP_PASTE))
            {
               selection = new PSSelection(new PSUiMode(m_view, ms_mode), parent, selNodes);
               popupMenuAction = m_actManager.getContextMenu(action, selection, PSClipBoard.TYPE_DRAG);
               menu = new PSContentExplorerMenu(popupMenuAction, new PSMenuSource(selection), m_actManager);

            }
            else
            {
               selection = new PSSelection(new PSUiMode(m_view, ms_mode), parent, selNodes);
               popupMenuAction = m_actManager.getContextMenu(action, selection);
               menu = new PSContentExplorerMenu(popupMenuAction, new PSMenuSource(selection), m_actManager);
            }
            if (menu!=null)
               popup = menu.getPopupMenu();

            // Get menu items and show as pop-up menu
            if (popup != null && popup.getSubElements().length > 0)
            {
               popup.show(comp, (int) loc.getX(), (int) loc.getY());
               PSCxUtil.adjustPopupLocation(popup);
            }

         }
      }
      catch (InterruptedException e)
      {
         log.error("Background menu creation Interrupted");
         log.debug(e);
         Thread.currentThread().interrupt();
      }
      catch (ExecutionException e)
      {
         log.error("Background menu creation could not execute",e);
      }
   }

   /**
    * Scans all rows in the table, checking each one for a match against the
    * supplied node, using the <code>equals</code> method.
    * 
    * @param node Assumed not <code>null</code>.
    * 
    * @return The index of the node within the underlying table model, not the
    * sorted table model, that matches the supplied node using the
    * <code>equals</code> method, otherwise, -1 if no data in the table
    * matches. The value will be between 0 and tableRowCount-1, inclusive.
    */
   private int getMatchingRowIndex(PSNode node)
   {
      int rows = m_childViewTable.getRowCount();
      PSDisplayFormatTableModel model = getDataModel();
      for (int i = 0; i < rows; i++)
      {
         int modelRow = m_childViewTableModel.getModelRow(i);
         PSNode n = (PSNode) model.getData(modelRow);
         if (StringUtils.isNotBlank(n.getContentId()))
         {
            //this allows us to maintain selection if the title changes
            if (n.getContentId().equals(node.getContentId()))
               return i;
         }
         else 
         {
            if (n.getName().equalsIgnoreCase(node.getName()))
               return i;
         }
      }
      return -1;
   }
   
   /**
    * Gets the nodes represented by selected rows in this panel's table.
    * 
    * @return the Never <code>null</code>.
    */
   public Iterator<PSNode> getSelectedRowNodes()
   {
      int[] rows = m_childViewTable.getSelectedRows();

      PSDisplayFormatTableModel model = getDataModel();

      List<PSNode> selNodes = new ArrayList<PSNode>();
      if(rows != null && rows.length > 0)
      {
         for (int i = 0; i < rows.length; i++)
         {
            int modelRow = m_childViewTableModel.getModelRow(rows[i]);
            selNodes.add((PSNode) model.getData(modelRow));
         }
      }

      return selNodes.iterator();
   }

   /**
    * Gets the table component that is displayed in the panel. The model of this
    * table is <code>PSTableSorter</code> that supports sorting. To get the
    * actual data table model, call <code>getModel()</code> on the sorter model.
    * The actual data model is <code>PSDisplayFormatTableModel</code>.
    *
    * @return the table, never <code>null</code>
    */
   public JTable getTable()
   {
      return m_childViewTable;
   }

   /**
    * Gets the data model of the table in this panel.
    *
    * @return the data model, never <code>null</code>
    */
   public PSDisplayFormatTableModel getDataModel()
   {
      return (PSDisplayFormatTableModel)m_childViewTableModel.getModel();
   }

    //implements interface method
   public void dragGestureRecognized(DragGestureEvent dge)
   {
      int[] rows = m_childViewTable.getSelectedRows();

      PSDisplayFormatTableModel model = getDataModel();

      List<PSNode> draggableNodes = new ArrayList<PSNode>();
      if(rows != null && rows.length > 0)
      {
         for (int i = 0; i < rows.length; i++)
         {
            int modelRow = m_childViewTableModel.getModelRow(rows[i]);
            PSNode node = (PSNode)model.getData(modelRow);
            if(m_actManager.canCopyOrMove(node))
               draggableNodes.add(node);
            else
               m_childViewTable.removeRowSelectionInterval(rows[i], rows[i]);
         }
      }

      if(!draggableNodes.isEmpty())
      {
         try
         {
            PSClipBoard clipBoard = m_actManager.getClipBoard();
            PSSelection sel = new PSSelection(
                  new PSUiMode(m_view, ms_mode), m_parentNode,
                  draggableNodes.iterator());
            clipBoard.setClip(PSClipBoard.TYPE_DRAG, sel);
            dge.startDrag(DragSource.DefaultCopyNoDrop,
               new PSDnDTransferable(), new PSDragSourceAdapter());
         }
         catch (InvalidDnDOperationException e)
         {
            log.error("Invalid dnd operation",e);
         }
      }
   }
   
   /*
    * This method is written for the purpose to disable the normal functionality of the keyboard keys.
    * For eg: 'enter' key just moves the selection to the next row. 
    * 'right arrow' key moves the selection to right. 
    * We need to use the 'right arrow' key to show the context menu.
    * Some keyboards do not have the 'context menu' key and so we need to use 'right arrow' key.
    */
   private void disableNormalKeyFunctions() {
	   
	   KeyStroke ksRightArrowKeyRelease = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
	   KeyStroke ksEnterKeyRelease = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	   
	   AbstractAction doNothing = new AbstractAction() {
	 	    public void actionPerformed(ActionEvent e) {
	 	        //do nothing
	 	    }
	 	 };
	 	 
	 	m_childViewTable.getInputMap().put(ksRightArrowKeyRelease, "rightArrowAction");
	    m_childViewTable.getActionMap().put("rightArrowAction", doNothing);
	    
	    m_childViewTable.getInputMap().put(ksEnterKeyRelease, "enterKeyAction");
	    m_childViewTable.getActionMap().put("enterKeyAction", doNothing);

	    getViewport().getInputMap().put(ksRightArrowKeyRelease, "rightArrowAction");
	    getViewport().getActionMap().put("rightArrowAction", doNothing);
	    
	    getViewport().getInputMap().put(ksEnterKeyRelease, "enterKeyAction");
	    getViewport().getActionMap().put("enterKeyAction", doNothing);
   }

   /**
    * The table sorter to use with the table in the panel to sort the rows
    * according to the node type group. This means the rows are sorted with in
    * the groups of node types). The groups are sorted according to their sort
    * order. See {@link com.percussion.guitools#PSTableSorter} for more info.
    * about sorting.
    */
   private class PSNodeTableSorter extends PSTableSorter
   {
      /**
       * Constructs this table sorter model with the supplied model as data
       * model. See super class for more info.
       *
       * @param model the data model, may not be <code>null</code>
       */
      public PSNodeTableSorter(PSDisplayFormatTableModel model)
      {
         super(model, m_isSorted);
      }

      /**
       * Overriden to sort the rows by grouping them based on node type and
       * these are sorted based on their sort order. See super's method
       * description for more info.
       */
      @Override
      public int compareRowsByColumn(int row1, int row2, int column)
      {
         int result = super.compareRowsByColumn(row1, row2, column);

         PSDisplayFormatTableModel model = (PSDisplayFormatTableModel)getModel();
         int nodeOrder1 = ((PSNode)model.getData(row1)).getSortOrder();
         int nodeOrder2 = ((PSNode)model.getData(row2)).getSortOrder();
         int nodeResult =
            ( nodeOrder1 < nodeOrder2 ? -1 : (nodeOrder1 == nodeOrder2 ? 0 : 1));

         if(nodeResult == 0)
            return result;
         else
            return nodeResult;
      }
   }


   /**
    * The table cell renderer that supports rendering of all columns of the
    * main display table. It renders the cell whose value is <code>PSNode</code>
    * to show the icon based on node type in additional to its label. All other
    * cells rendering is delegated to super to provide default rendering as a
    * label.
    */
   private class MainDisplayTableCellRenderer extends DefaultTableCellRenderer
   {
      //overridden method to display an icon for the node if the value
      //represents a PSNode.

      /**
       * Overridden method to apply display options if they are available in
       * <code>UIManager</code> defaults. See super for more description about
       * this method.
       */
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value,
         boolean isSelected, boolean hasFocus, int row, int column)
      {
        
         if (value==null)
            value="";
         
         super.getTableCellRendererComponent(table, value,
               isSelected, hasFocus, row, column);
         
         //no tool tip by default
         setToolTipText(null);
         if (value != null && value instanceof PSNode)
         {
            PSNode data = (PSNode)value;
            String iconKey = data.getIconKey();
            if(PSCxUtil.shouldFolderBeMarked(data, null, false, m_applet))
            {
               iconKey += "Marked";
            }
            setIcon(PSImageIconLoader.loadIcon(
               getIconName(iconKey), false, m_applet));
           
            getAccessibleContext().setAccessibleName(data.getName());
         }
         else
         {
            Iterator cols = m_parentNode.getChildrenDisplayFormat();
            String colType = null;
            //If the column is not default column (column data loaded from server)
            //check whether this represents an image data and display icon
            //accordingly. '0' column is default column.
            if(cols != null)
            {
               //Get list of columns and get the definition of the column
               //corresponding to the column index - 1 (reduce index because
               //default column is not in the list.
               List colDefs = PSIteratorUtils.cloneList(cols);

               // this is a hack for now, what we really need to do is to
               // be sure that folders always have the display format
               // id, it may not, in that case use the default.
               if(PSDisplayFormatTableModel.getSysTitleIndex(m_parentNode,m_actManager) < 0
                  && column > 0)
                     column--;

               Map.Entry columnDef = (Map.Entry)colDefs.get(column);
               colType = (String)columnDef.getValue();
            }
            
            //Show tool tip if there is an image to explain that image.
            if (colType != null
               && colType.equalsIgnoreCase(PSNode.DATA_TYPE_IMAGE)
               && value != null && value.toString().length() > 0)
            {
               Icon icon = PSImageIconLoader.loadIcon(
                  getIconName(value.toString()), false, m_applet);
               if(table.getRowHeight(row) < icon.getIconHeight())
                  table.setRowHeight(row, icon.getIconHeight());
               setIcon(icon);
               setToolTipText(value.toString());
               getAccessibleContext().setAccessibleName(value.toString());
               setText("");
            }
            else if (colType != null
               && colType.equalsIgnoreCase(PSNode.DATA_TYPE_DATE)
               && value != null && value instanceof Date)
            {
               Date d = (Date) value;
               String dateStr = PSStringOperation.dateFormat(null, d,
                  getDataModel().getLocale());
               setText(dateStr);
               setIcon(null);
            }
            else
               setIcon(null);
         }

         PSDisplayOptions dispOptions =
            (PSDisplayOptions)UIManager.getDefaults().get(
               PSContentExplorerConstants.DISPLAY_OPTIONS);

         if(dispOptions != null)
         {
            setFont(dispOptions.getFont());
            if(isSelected)
            {
               setForeground(dispOptions.getHighlightTextColor());
               setBackground(dispOptions.getHighlightColor());
            }
            else
            {
               setForeground(dispOptions.getForeGroundColor());
               setBackground(table.getBackground());
            }
         }

         if (hasFocus)
         {
            setBorder(new PSFocusBorder(1, dispOptions, true));
         }
         else
         {
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
         }


         return this;
      }
   }
   
   /*
    * This is a hack to get hold of the currently focused headercolumn Should be
    * able to access focused column with
    * tableView.getColumnModel().getSelectionModel().getLeadSelectionIndex();
    * This does not seem to work in this case, the column returned does not pick
    * up changes to column with left and right keys after focus is gained with
    * F8.To get around this we are setting a static variable on
    * PSMainDisplayPanel from the renderer MainDisplayTableHeaderRenderer which
    * has access to the column and a hasFocus() method while the header cell is
    * being rendered.
    * @param the currently focused column
    */
   public static synchronized void setFocusColumn(int column) {
      focusedColumn=column;
   }
   /*
    * This is a hack to get hold of the currently focused header column Should be
    * able to access focused column with
    * tableView.getColumnModel().getSelectionModel().getLeadSelectionIndex();
    * This does not seem to work in this case, the column returned does not pick
    * up changes to column with left and right keys after focus is gained with
    * F8.To get around this we are setting a static variable on
    * PSMainDisplayPanel from the renderer MainDisplayTableHeaderRenderer which
    * has access to the column and a hasFocus() method while the header cell is
    * being rendered.
    * @return the currently focused column
    */
   public static synchronized int getFocusColumn() {
      return focusedColumn;
   }
   
   /**
    * The renderer used with header of main display table.
    */
   private class MainDisplayTableHeaderRenderer extends DefaultTableCellRenderer
   {
      

      /**
       * Overridden method to apply display options if they are available in
       * <code>UIManager</code> defaults. See super for more description about
       * this method.
       */
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column)
      {
         if (hasFocus)
            setFocusColumn(column);

         super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
         
         PSDisplayOptions dispOptions = (PSDisplayOptions) UIManager.getDefaults().get(
               PSContentExplorerConstants.DISPLAY_OPTIONS);

         String name = "Table Header column "+ column + " " + value;
         if (dispOptions != null)
         {
            if (hasFocus)
            {

               setBackground(dispOptions.getHighlightColor());
               setForeground(dispOptions.getHighlightTextColor());
               setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
               getAccessibleContext().setAccessibleName("Table header "+ value);
            }
            else
            {
               setBackground(dispOptions.getBackGroundColor());
               setForeground(dispOptions.getHeadingForeGroundColor());
               setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
               getAccessibleContext().setAccessibleName("Table header raised " +value);
            }

            setFont(dispOptions.getFont());

         }
         else
         {
            JTableHeader header = table.getTableHeader();
            if (header != null)
            {
               if (hasFocus)
               {
                  setForeground(UIManager.getColor("TableHeader.focusCellForeground"));
                  setBackground(UIManager.getColor("TableHeader.focusCellBackground"));
                  setBorder(UIManager.getBorder("TableHeader.focusCellBorder"));
               }
               else
               {
                  setForeground(header.getForeground());
                  setBackground(header.getBackground());
                  setBorder(UIManager.getBorder("TableHeader.cellBorder"));
               }
            }
         }

         // is this the column that is being sorted?
         if (table.convertColumnIndexToModel(column) == m_childViewTableModel.getLatestSortingColumn())
         {
            if (m_childViewTableModel.isAscending())
            {
               setIcon(PSImageIconLoader.loadIcon("up", false, m_applet));
               name += " sort ascending";
            }
            else
            {
               setIcon(PSImageIconLoader.loadIcon("down", false, m_applet));
               name += " sort descending";
            }
         }
         else
         {
            setIcon(null);
         }
       
         
         getAccessibleContext().setAccessibleName(name);
  
         setText((value == null) ? "" : value.toString());
         setHorizontalAlignment(LEFT);
         setHorizontalTextPosition(LEFT);
         setVerticalAlignment(BOTTOM);
         setOpaque(true);
         return this;
      }
   }
  
 
   /**
    * A keyboard handler is attached to the table so that when the user tabs or
    * uses directional keys, we can navigate the table much the same as 
    * we do with a MouseEvent. This fixes the issue with KeyBoard selection in 
    * the table is not setting context menus correctly.(It was setting the last 
    * selected node in the navigation tree.
    * @author vamsinukala
    *
    */
   private class TableKeyBoardHandler extends KeyAdapter
   {
	  private Logger log = Logger.getLogger(TableKeyBoardHandler.class);
	  private boolean isPopup = false;
	  private int enter=0;
	  
      @Override
      public void keyReleased(KeyEvent e)
      {
    	 int code = e.getKeyCode();
    	 
         JTable table = (JTable) e.getSource();
         if ( table == null ) {
            return;
         }

         if (code == KeyEvent.VK_ENTER)
         {
        	 enter++;
        	/* A boolean enter is placed so that the logic for enter key is executed only once. 
        	 * For some reason it is getting called twice. 
        	 * May be it is required to look into all the related classes and correct the keyboard handling.
        	 * Possibly the event is getting triggered from two places.
        	 * 
        	 * Something to work on if time permits.
        	 */
        	 if(enter == 1){
        		 /* There is a global JPopupMenu object, but it does not seem to be getting used.
        		  * Implementing key listener on the local instance of popup menu is not working correctly.
        		  * A boolean isPopup is used to avoid this 'enter' key event when user clicks enter on an option in the popup menu.
        		  * Without this boolean, the application opens the user's popup menu selection and also opens the item for edit.
        		  */
            	 if(!isPopup) {
    	             PSNode selNode = null;
    	             PSNode root = getDataModel().getRoot();
    	             PSNavigationTree m_navTree = new PSNavigationTree(root, m_view, m_actManager);
    					
    	             //This will always have only one entry because, double-click removes
    	             //the previous selection and makes the clicked row alone as selected.
    	             //even if multiple rows comes, take the first one.
    	             Iterator<PSNode> selNodes = getSelectedRowNodes();
    	
    	             if (selNodes.hasNext()) {
    	                 selNode = selNodes.next();
    	             } else {
    	                 return;
    	             }
    	
    	             if (selNode.isContainer()) {
    	                 m_navTree.selectNode(selNode);
    	             } else //execute the default action for the node if applicable
    	              {
    	                 //parent node a node selected in display panel is the node
    	                 //selected in the navigation tree.
    	                 PSNode parentNode = m_navTree.getSelectedNode();
    	                 PSUiMode mode = new PSUiMode(m_view, ms_mode);
    	                 PSSelection selection = new PSSelection(mode, parentNode, PSIteratorUtils.iterator(selNode));
    	                 PSMenuAction defAction = m_actManager.findDefaultAction(selection);
    	
    	                 if (defAction != null) {
    	                	 m_actManager.executeAction(defAction, selection);
    	                 }
    	             }
            	 } else {
            		 isPopup = false;
            	 }
        	 }
        	 else {
        		 enter = 0;
        	 }
        	 
         }
         else if (code == KeyEvent.VK_CONTEXT_MENU || code == KeyEvent.VK_RIGHT)
         {
        	 Iterator<PSNode> selNodes = getSelectedRowNodes();
        	 
        	 PSNode selNode = null;
        	 if (selNodes.hasNext()) {
                 selNode = selNodes.next();
             } else {
            	 return;
             }

        	 int row= getMatchingRowIndex(selNode);
	         int col = table.getSelectedColumn();
        	 displayPopupMenu((Component)e.getSource(), null, getSelectedRowNodes(), new Point(row + 150, col + (row*17)));
        	 isPopup = true;
         }
            
         else if (code == KeyEvent.VK_TAB)
         {
	         int row= table.getSelectedRow();
	         int col = table.getSelectedColumn();
	         if ( row == -1 && col == -1 )
	         {
	            row =0;
	            col =0;
	         }
	         table.requestFocusInWindow();
	         CellEditor editor = table.getCellEditor();
	         if (editor == null || editor.shouldSelectCell(e))
	         {
	            table.getSelectionModel().setValueIsAdjusting(true);
	            table.getColumnModel().getSelectionModel().setValueIsAdjusting(
	                  true);
	            table.changeSelection(row, col, e.isControlDown(), e.isShiftDown());
	         }
         }
      }
   }

   /**
    * The mouse input handler that handles the mouse events on the table in this
    * panel. This differs from <code>BasicTableUI</code>'s <code>
    * MouseInputHandler</code> in the following ways.
    * <ol>
    * <li>Selects the row or cell upon mouse click, not on mouse press.
    * <li>In mouse dragged, if the cell at drag origin (mouse press location) is
    * not currently selected, makes that as selected, otherwise keeps the
    * current selection.
    * </ol>
    * This behavior is similar to windows explorer behavior in right panel.
    */
   private class TableMouseInputHandler extends PSMouseAdapter 
     implements MouseInputListener
   {
      /**
       * Implements interface method to make the clicked cell as selected. If
       * the control key is down, the cell is selected/deselected based on
       * current selection state of that row not affecting any other selection.
       * If shift key is down, the range of cells between last selected row in
       * the direction (up/down) are selected. See the interface for more
       * description.
       */
      @Override
      public void mouseWasClicked(MouseEvent e)
      {
         if (shouldIgnore(e))
            return;

         JTable table = (JTable) e.getSource();
         Point p = e.getPoint();
         int row = table.rowAtPoint(p);
         int column = table.columnAtPoint(p);
         // The autoscroller can generate drag events outside the Table's range.
         if ((column == -1) || (row == -1))
            return;

         if (table.editCellAt(row, column, e))
         {
            setDispatchComponent(e);
            repostEvent(e);
         }
         else
         {
            table.requestFocus();
         }

         CellEditor editor = table.getCellEditor();
         if (editor == null || editor.shouldSelectCell(e))
         {
            setValueIsAdjusting(table, true);
            table.changeSelection(
               row, column, e.isControlDown(), e.isShiftDown());
         }
      }

      /**
       * Sets the component that should get this event.
       *
       * @param e the mouse event recieved by this listener, assumed not
       * <code>null</code>
       */
      private void setDispatchComponent(MouseEvent e)
      {
         JTable table = (JTable) e.getSource();
         Component editorComponent = table.getEditorComponent();
         Point p = e.getPoint();
         Point p2 = SwingUtilities.convertPoint(table, p, editorComponent);
         m_dispatchComponent = SwingUtilities.getDeepestComponentAt(
            editorComponent, p2.x, p2.y);
      }

      /**
       * Posts the supplied event to the component (editor component) that
       * should recieve the event if it exists.
       *
       * @param e the mouse event recieved by this listener, assumed not
       * <code>null</code>
       *
       * @return <code>true</code> if the component exists, otherwise <code>
       * false</code>
       */
      private boolean repostEvent(MouseEvent e)
      {
         if (m_dispatchComponent == null)
            return false;

         JTable table = (JTable) e.getSource();
         MouseEvent e2 = SwingUtilities.convertMouseEvent(
            table, e, m_dispatchComponent);
         m_dispatchComponent.dispatchEvent(e2);

         return true;
      }

      /**
       * Sets the selection value is adjusting on the supplied table.
       *
       * @param table the table whose selection changes, assumed not <code>
       * null</code>
       * @param flag supply <code>true</code> if caller is about to change the
       * selection, otherwise <code>false</code>
       */
      private void setValueIsAdjusting(JTable table, boolean flag)
      {
         table.getSelectionModel().setValueIsAdjusting(flag);
         table.getColumnModel().getSelectionModel().setValueIsAdjusting(flag);
      }

      /**
       * Checks whether the event can be ignored. A mouse event can be ignored
       * if the event source table is not enabled or it is not a left mouse
       * button or event id is mouse dragged.
       *
       * @param e the event recieved by this listener, assumed not <code>null
       * </code>
       *
       * @return <code>true</code> if it can be ignored, otherwise <code>false
       * </code>
       */
      private boolean shouldIgnore(MouseEvent e)
      {
         JTable table = (JTable) e.getSource();
         return !(
               SwingUtilities.isLeftMouseButton(e)
               && table.isEnabled()
               && e.getID() != MouseEvent.MOUSE_DRAGGED
               );
      }

      /**
       * Implemented to remember the mouse pressed location to use with mouse
       * drag event. See {@link #mouseDragged(MouseEvent)} for more
       * description. See interface for more description about this method.
       */
      @Override
      public void mouseWasPressed(MouseEvent e)
      {
         m_pressedLoc = e.getPoint();
      }

      /**
       * Implemented to remember the mouse pressed location to use with mouse
       * drag event. See {@link #mouseDragged(MouseEvent)} for more
       * description. See interface for more description about this method.
       */
      @Override
      public void mouseWasReleased(MouseEvent e)
      {
         if (shouldIgnore(e))
            return;

         JTable table = (JTable) e.getSource();
         repostEvent(e);
         m_dispatchComponent = null;
         setValueIsAdjusting(table, false);
      }
      //implements to do nothing
      @Override
      public void mouseEntered(@SuppressWarnings("unused") MouseEvent e) {}

      //implements to do nothing
      @Override
      public void mouseExited(@SuppressWarnings("unused") MouseEvent e) {}

      //  The Table's mouse motion listener methods.
      //implements to do nothing
      public void mouseMoved(@SuppressWarnings("unused") MouseEvent e) {}

      /**
       * Handles the mouse drag event to select the cell at the mouse press
       * location clearing the previous selection if it is not. See the interface
       * for this method description.
      */
      public void mouseDragged(MouseEvent e)
      {
         if (shouldIgnore(e))
            return;
         JTable table = (JTable) e.getSource();

         repostEvent(e);

         CellEditor editor = table.getCellEditor();
         if (editor == null || editor.shouldSelectCell(e))
         {
            //use the pressed location to get drag origin
            if(m_pressedLoc == null)
               m_pressedLoc = e.getPoint();

            int row = table.rowAtPoint(m_pressedLoc);
            int column = table.columnAtPoint(m_pressedLoc);

            // The autoscroller can generate drag events outside the Table's range.
            if ((column == -1) || (row == -1))
               return;

            //select the cell at drag origin if that cell is not selected by
            //clearing the previous selection
            if(!table.isCellSelected(row, column))
               table.changeSelection(row, column, false, false);
         }
      }
      
      

      /**
       * The current location of mouse press event, initialized to <code>null
       * </code> and gets updated with actual value when <code>
       * mousePressed(MouseEvent)</code> is called.
       */
      private Point m_pressedLoc = null;

      /**
       * The component recieving mouse events during editing. May not be
       * editor component. Used to dispatch events when the mouse event occurs
       * on the table while editing. This gets modified in each mouse listener
       * method to repost the event.
       */
      private Component m_dispatchComponent;
   }

   private static volatile int focusedColumn = -1;
   
   /**
    * The parent node whose children are the data of this panel's table, may be
    * <code>null</code> until a call to <code>setData(PSNode, PSNode)</code>.
    */
   private PSNode m_parentNode;

   /**
    * The parent of the <code>m_parentNode</code>, may be <code>null</code>.
    * Modified through a call to <code>setData(PSNode, PSNode)</code>.
    */
   private PSNode m_parentParent;

   /**
    * The table to show children of a node, initialized in <code>init()</code>
    * and never <code>null</code> after that. The table structure gets changed
    * through calls to <code>setData(PSNode, PSNode)</code>.
    */
   private JTable m_childViewTable;
   
   /**
    * This holds the current drag selection on drag enter. On drag exit the
    * selection is restored and this is nulled out. May contain <code>null</code>
    * if there is no current select.
    */
   private int[] m_saveDragSelection = null;  

   /**
    * The table model used with <code>m_childViewTable</code> to allow sorting.
    * Initialized in <code>init()</code> and never <code>null</code> after that.
    * The internal table model gets changed through calls to <code>
    * setData(PSNode, PSNode)</code>.
    */
   PSTableSorter m_childViewTableModel;

   /**
    * The table cell renderer to use with <code>m_childViewTable</code> to render
    * the cells of the table, initialized in <code>init(PSNode)</code> and
    * never <code>null</code> or modified after that.
    */
   private MainDisplayTableCellRenderer m_childViewTableRenderer;

   /**
    * The action manager to use to provide context-sensitive menu, initialized
    * in the ctor and never <code>null</code> or modified after that.
    */
   private PSActionManager m_actManager;
   
   /**
    * The parent applet container that holds action manager and never <code>null</code>
    */
   private PSContentExplorerApplet m_applet;

   /**
    * A text area that is used to present information to the user when
    * necessary. It takes the place of the table in the viewpoint under
    * those circumstances. Never <code>null</code> after initialization in
    * the {@link #init()} method.
    */
   private JTextArea m_messageText;

   /**
    * Holds the current popup menu.  This is created on selection of items and is populated
    * in the background.  The popop is then shown on menu click etc.
    */
   private JPopupMenu m_currentContextMenu;
   
   /**
    * The current view of UI, initialized in the constructor and never <code>
    * null</code>, empty or modified after that.
    */
   private String m_view;

   /**
    * If <code>true</code>, the table will be sortable, if <code>false</code>,
    * it will not.  Set during the ctor, never modified after that.
    */
   private boolean m_isSorted;

   /**
    * The mode (Main Display) that is represented by this panel. See {@link
    * PSUiMode#TYPE_MODE_MAIN} for more info.
    */
   public static final String ms_mode = PSUiMode.TYPE_MODE_MAIN;
   
   private TableKeyBoardHandler keyboardHandler;

   /**
    * Minimum value used to specified the preferred width of a table column.
    */
   private static final int MIN_COL_WIDTH = 15;
   
   /**
    * A map of display names to icon names. Map keys and values are of type 
    * <code>String</code>.
    */
   private static final Map<String, String> ms_displayNameToIconName = 
      new HashMap<String, String>();
   static
   {
      ms_displayNameToIconName.put(
         com.percussion.cms.IPSConstants.CHECKOUT_STATUS_NOBODY, 
         "CheckedIn");
      ms_displayNameToIconName.put(
         com.percussion.cms.IPSConstants.CHECKOUT_STATUS_SOMEONEELSE, 
         "CheckedOut");
      ms_displayNameToIconName.put(
         com.percussion.cms.IPSConstants.CHECKOUT_STATUS_MYSELF, 
         "CheckedOutByMe");
   }
   
   private final ExecutorService backgroundService =   
         Executors.newFixedThreadPool(1);  
   private FutureTask<PSContentExplorerMenu> backgroundMenu;

}
