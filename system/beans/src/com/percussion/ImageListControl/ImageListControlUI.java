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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.ImageListControl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicListUI;


/** The custom UI for ImageListControl that displays cells in columns instead
  * of rows.  Thus, the list is horizontal.
*/

class ImageListControlUI extends BasicListUI
{
//
// CONSTRUCTORS
//

// using default constructor!  

//
// PUBLIC METHODS
//

/**
  * Paint the columns that intersect the Graphics objects clipRect.  This
  * method calls paintCell as necessary. (variable list is inherited
  * from parent)
  *
  * @see #paintCell
*/
  public void paint(Graphics g, JComponent c)
  {
    maybeUpdateLayoutState();

    ListCellRenderer renderer = list.getCellRenderer();
    ListModel dataModel = list.getModel();
    ListSelectionModel selModel = list.getSelectionModel();

    if ((renderer == null) || (dataModel.getSize() == 0))
    {
      return;
    }

    /* Compute the area we're going to paint in terms of the affected
     * columns (firstPaintColumn, lastPaintColumn), and the clip bounds.
     */

    Rectangle paintBounds = g.getClipBounds();
    int firstPaintColumn = convertXToColumn(paintBounds.x);
    int lastPaintColumn = convertXToColumn((paintBounds.x + paintBounds.width) - 1);

    if (firstPaintColumn == -1)
    {
      firstPaintColumn = 0;
    }
    if (lastPaintColumn == -1)
    {
      lastPaintColumn = dataModel.getSize() - 1;
    }

    Rectangle columnBounds = getCellBounds(list, firstPaintColumn, firstPaintColumn);
    if (columnBounds == null)
    {
      return;
    }

    int leadIndex = list.getLeadSelectionIndex();

    for(int column = firstPaintColumn; column <= lastPaintColumn; column++)
    {
      columnBounds.width = getColumnWidth(column);

      /* Set the clip rect to be the intersection of columnBounds
      * and paintBounds and then paint the cell.
      */

      g.setClip(columnBounds.x, columnBounds.y, columnBounds.width, columnBounds.height);
      g.clipRect(paintBounds.x, paintBounds.y, paintBounds.width, paintBounds.height);

      paintCell(g, column, columnBounds, renderer, dataModel, selModel, leadIndex);

      columnBounds.x += columnBounds.width;
    }
  }

/**
  * The preferredSize of a list is total height of the rows
  * and the maximum width of the cells.  If JList.fixedCellHeight
  * is specified then the total height of the rows is just
  * (cellVerticalMargins + fixedCellHeight) * model.getSize() where
  * rowVerticalMargins is the space we allocate for drawing
  * the yellow focus outline.  Similarly if JListfixedCellWidth is
  * specified then we just use that plus the horizontal margins.
  *
  * @param c The JList component.
  * @return The total size of the list.
  */
  public Dimension getPreferredSize(JComponent c)
  {
    maybeUpdateLayoutState();

    int lastColumn = list.getModel().getSize() - 1;
    if (lastColumn < 0)
    {
      return new Dimension(0, 0);
    }

    Insets insets = list.getInsets();

    int height = cellHeight + insets.top + insets.bottom;
    int width = convertColumnToX(lastColumn) + getColumnWidth(lastColumn) + insets.right;
    return new Dimension(width, height);
  }

/**
  * @return The bounds of the index'th cell.
  * @see javax.swing.plaf.ListUI#getCellBounds(JList,int,int)
  */
  public Rectangle getCellBounds(JList list, int index1, int index2)
  {
    maybeUpdateLayoutState();

    int minIndex = Math.min(index1, index2);
    int maxIndex = Math.max(index1, index2);
    int minX = convertColumnToX(minIndex);
    int maxX = convertColumnToX(maxIndex);

    if ((minX == -1) || (maxX == -1))
    {
      return null;
    }

    Insets insets = list.getInsets();
    int x = minX;
    int y = insets.top;
    int w = (maxX + getColumnWidth(maxIndex)) - minX;
    int h = list.getHeight() - (insets.top + insets.bottom);
    return new Rectangle(x, y, w, h);
  }

/**
  * @return The index of the cell at location, or -1.
  * @see javax.swing.plaf.ListUI#locationToIndex(JList,Point)
  */
  public int locationToIndex(JList list, Point location)
  {
    maybeUpdateLayoutState();
    return convertXToColumn(location.x);
  }


/**
  * @return The origin of the index'th cell.
  * @see javax.swing.plaf.ListUI#indexToLocation(JList,int)
  */
  public Point indexToLocation(JList list, int index)
  {
    maybeUpdateLayoutState();
    return new Point(convertColumnToX(index), 0);
  }

//
// PROTECTED METHODS
//

/** Returns the width of the specified column based on the current layout.
  *
  * @return The specified column width or -1 if column isn't valid.
  * @see #convertXToColumn
  * @see #convertColumnToX
  * @see #updateLayoutState
*/
  protected int getColumnWidth(int column)
  {
    if ((column < 0) || (column >= list.getModel().getSize()))
    {
      return -1;
    }
    return (m_cellWidths == null) ? cellWidth : ((column < m_cellWidths.length) ? m_cellWidths[column] : -1);

    // above code equals this...
    /*
    if (m_cellWidths == null)
      return cellWidth;
    else
    {
      if (column < m_cellWidths.length)
        return m_cellWidths[column];
      else
        return -1;
    }
    */
  }

/**
  * Convert the JList relative coordinate to the column that contains it,
  * based on the current layout.  If x0 doesn't fall within any column,
  * return -1.
  *
  * @param x0 The x coordinate that the column (may or may not) exists.
  * @return The column that contains x0, or -1.
  * @see #getRowHeight
  * @see #updateLayoutState
*/
  protected int convertXToColumn(int x0)
  {
    int ncols = list.getModel().getSize();
    Insets insets = list.getInsets();

    if (m_cellWidths == null)
    {
      int column = (cellWidth == 0) ? 0 : ((x0 - insets.left) / cellWidth);
      return ((column < 0) || (column >= ncols)) ? -1 : column;
    }
     else if (ncols > m_cellWidths.length)
    {
       System.out.println("ncols = " + ncols + " cellWidths.length = " + m_cellWidths.length);
       return -1;
    }
    else
    {
      int x = insets.left;
      int col = 0;

      for(int i = 0; i < ncols; i++)
      {
        if ((x0 >= x) && (x0 < x + m_cellWidths[i]))
        {
          return col;
        }
        x += m_cellWidths[i];
        col += 1;
      }
      return -1;
    }
  }


/**
  * Return the JList relative X coordinate of the origin of the specified
  * column or -1 if column isn't valid.
  *
  * @return The X coordinate of the origin of column, or -1.
  * @see #getColumnWidth
  * @see #updateLayoutState
*/
  protected int convertColumnToX(int column)
  {
    int ncols = list.getModel().getSize();
    Insets insets = list.getInsets();

    if ((column < 0) || (column >= ncols))
    {
      return -1;
    }

    if (m_cellWidths == null)
    {
      return insets.left + (cellWidth * column);
    }
     else if (column >= m_cellWidths.length)
    {
       return -1;
     }
    else
    {
      int x = insets.left;
      for(int i = 0; i < column; i++)
      {
        x += m_cellWidths[i];
      }
      return x;
    }
  }

/**
  * Recompute the value of cellWidth or cellWidths based
  * and cellHeight based on the current font and the current
  * values of fixedCellWidth, fixedCellHeight, and prototypeCellValue.
  *
  * @see #maybeUpdateLayoutState
  */
  protected void updateLayoutState()
  {
    /* If both JList fixedCellWidth and fixedCellHeight have been
     * set, then initialize cellWidth and cellHeight, and set
     * cellWidths to null.
     */

    int fixedCellHeight = list.getFixedCellHeight();
    int fixedCellWidth = list.getFixedCellWidth();

    cellHeight = (fixedCellHeight != -1) ? fixedCellHeight : -1;

    if (fixedCellWidth != -1)
    {
      cellWidth = fixedCellWidth;
      m_cellWidths = null;
    }
    else
    {
      cellWidth = -1;
      m_cellWidths = new int[list.getModel().getSize()];
    }

    /* If either of  JList fixedCellWidth and fixedCellHeight haven't
     * been set, then initialize cellHeight and m_cellWidths by
     * scanning through the entire model.  Note: if the renderer is
     * null, we just set cellHeight and m_cellWidths[*] to zero,
     * if they're not set already.
     */

    if ((fixedCellWidth == -1) || (fixedCellHeight == -1))
    {
      ListModel dataModel = list.getModel();
      int dataModelSize = dataModel.getSize();
      ListCellRenderer renderer = list.getCellRenderer();

      if (renderer != null)
      {
        for(int index = 0; index < dataModelSize; index++)
        {
          Object value = dataModel.getElementAt(index);
          Component c = renderer.getListCellRendererComponent(list, value, index, false, false);
          rendererPane.add(c);
          Dimension cellSize = c.getPreferredSize();
          if (fixedCellHeight == -1)
          {
            cellHeight = Math.max(cellSize.height, cellHeight);
          }
          if (fixedCellWidth == -1)
          {
            m_cellWidths[index] = cellSize.width;
          }
        }
      }
      else
      {
        if (cellHeight == -1)
        {
          cellHeight = 0;
        }
        for(int index = 0; index < dataModelSize; index++)
        {
          m_cellWidths[index] = 0;
        }
      }
    }

    list.invalidate();
  }


  public void maybeUpdateLayoutState()
  {
    super.maybeUpdateLayoutState();
  }

    /**
     * The ListDataListener that's added to the JLists model at
     * installUI time, and whenever the JList.model property changes.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     *
     * @see JList#getModel
     * @see #maybeUpdateLayoutState
     * @see #installUI
     */

  protected ListDataListener createListDataListener()
  {

    return new ListDataListener()
    {
      public void intervalAdded(ListDataEvent e)
      {
        setUpdateLayoutStateNeeded(ImageListControlUI.this.modelChanged);

        int minIndex = Math.min(e.getIndex0(), e.getIndex1());
        int maxIndex = Math.max(e.getIndex0(), e.getIndex1());

        /* Sync the SelectionModel with the DataModel.
         */

        ListSelectionModel sm = getList().getSelectionModel();
        if (sm != null)
        {
          sm.insertIndexInterval(minIndex, maxIndex - minIndex, true);
        }

        /* Repaint the entire list, from the origin of
         * the first added cell, to the bottom of the
         * component.
         */

        int x = Math.max(convertColumnToX(minIndex), 0);
        int w = getList().getWidth() - x;
        getList().revalidate();
        getList().repaint(x, 0, w, getList().getHeight());
      }

      public void intervalRemoved(ListDataEvent e)
      {
        setUpdateLayoutStateNeeded(ImageListControlUI.this.modelChanged);

        /* Sync the SelectionModel with the DataModel.
         */

        ListSelectionModel sm = getList().getSelectionModel();
        if (sm != null)
        {
          sm.removeIndexInterval(e.getIndex0(), e.getIndex1());
        }

        /* Repaint the entire list, from the origin of
         * the first removed cell, to the bottom of the
         * component.
         */

        int minIndex = Math.min(e.getIndex0(), e.getIndex1());
        int x = Math.max(convertColumnToX(minIndex), 0);
        int w = getList().getWidth() - x;
        getList().revalidate();
        getList().repaint(x, 0, w, getList().getHeight());
      }


      public void contentsChanged(ListDataEvent e)
      {
        setUpdateLayoutStateNeeded(ImageListControlUI.this.modelChanged);
         getList().revalidate();
        getList().repaint();
      }
    };
  }

    /**
     * Mouse input, and focus handling for JList.  An instance of this
     * class is added to the appropriate java.awt.Component lists
     * at installUI() time.  Note keyboard input is handled with JComponent
     * KeyboardActions, see installKeyboardActions().
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     *
     * @see #createMouseInputListener
     * @see #installKeyboardActions
     * @see #installUI
     */

  protected MouseInputListener createMouseInputListener()
  {
    return new MouseInputListener()
    {
      public void mouseClicked(MouseEvent e) {}

      public void mouseEntered(MouseEvent e) {}

      public void mouseExited(MouseEvent e) {}

      public void mousePressed(MouseEvent e)
      {
         if (!SwingUtilities.isLeftMouseButton(e))
        {
           return;
        }
         if (!(getList().isEnabled()))
        {
            return;
        }

         /* Request focus before updating the list selection.  This implies
          * that the current focus owner will see a focusLost() event
          * before the lists selection is updated IF requestFocus() is
          * synchronous (it is on Windows).  See bug 4122345
          */
        if (!getList().hasFocus())
        {
          getList().requestFocus();
        }

        int column = convertXToColumn(e.getX());
        if (column != -1)
        {
          getList().setValueIsAdjusting(true);
          int anchorIndex = getList().getAnchorSelectionIndex();
          if (e.isControlDown())
          {
            if (getList().isSelectedIndex(column))
            {
              getList().removeSelectionInterval(column, column);
            }
            else
            {
              getList().addSelectionInterval(column, column);
            }
          }
          else if (e.isShiftDown() && (anchorIndex != -1))
          {
            getList().setSelectionInterval(anchorIndex, column);
          }
          else
          {
            getList().setSelectionInterval(column, column);
          }
        }
      }

      public void mouseDragged(MouseEvent e)
      {
         if (!SwingUtilities.isLeftMouseButton(e))
        {
           return;
        }

         if (!getList().isEnabled())
        {
            return;
        }

        if (e.isShiftDown() || e.isControlDown())
        {
          return;
        }

        int column = convertXToColumn(e.getX());
        if (column != -1)
        {
          Rectangle cellBounds = getCellBounds(getList(), column, column);
          if (cellBounds != null)
          {
            getList().scrollRectToVisible(cellBounds);
            getList().setSelectionInterval(column, column);

            /*
            if (column != 0)
              ((ImageListControlList)getList()).setLeftButtonEnabled(true);
            else
              ((ImageListControlList)getList()).setLeftButtonEnabled(false);

            if (column != getList().getModel().getSize() - 1)
              ((ImageListControlList)getList()).setRightButtonEnabled(true);
            else
              ((ImageListControlList)getList()).setRightButtonEnabled(false);
            */
          }
        }
      }

      public void mouseMoved(MouseEvent e) {}

      public void mouseReleased(MouseEvent e)
      {
         if (!SwingUtilities.isLeftMouseButton(e))
        {
           return;
        }

        getList().setValueIsAdjusting(false);
      }
    };
  }


/**
  * The ListSelectionListener that's added to the JLists selection
  * model at installUI time, and whenever the JList.selectionModel property
  * changes.  When the selection changes we repaint the affected rows.
  * <p>
  * <strong>Warning:</strong>
  * Serialized objects of this class will not be compatible with
  * future Swing releases.  The current serialization support is appropriate
  * for short term storage or RMI between applications running the same
  * version of Swing.  A future release of Swing will provide support for
  * long term persistence.
  *
  * @see #createListSelectionListener
  * @see #getCellBounds
  * @see #installUI
*/
  protected ListSelectionListener createListSelectionListener()
  {
    return new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        maybeUpdateLayoutState();

        int minX = convertColumnToX(e.getFirstIndex());
        int maxX = convertColumnToX(e.getLastIndex());

        if ((minX == -1) || (maxX == -1))
        {
          getList().repaint(0, 0,
                                               getList().getWidth(),
                                               getList().getHeight());
        }
        else
        {
          maxX += getColumnWidth(e.getLastIndex());
          getList().repaint(minX, 0, maxX - minX,
                                      getList().getHeight());
        }
      }
    };
  }

//
// PRIVATE METHODS
//

  private JList getList()
  {
    return list;
  }

/** Method to prevent future errors.
*/
  private void setUpdateLayoutStateNeeded(int i)
  {
    updateLayoutStateNeeded = i;
  }

//
// MEMBER VARIABLES
//

  protected int[] m_cellWidths = null;

}

 
