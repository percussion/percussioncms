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

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JList;

/** A List that has a different Scrollable interface implementation for
  * horizontal display.
*/

public class ImageListControlList extends JList
{
//
// CONSTRUCTORS
//
  
  public ImageListControlList()
  {
    super();
  }

  public ImageListControlList(Object[] array)
  {
    super(array);
  }

//
// PUBLIC METHODS
//

/** This is called by the ImageListControlRenderer and should not be called by
  * the user.
*/
  public void setMaxPreferredHeight(int height)
  {
    if (height > m_maxPreferredHeight)
    {
      System.out.println("height = " + height + "; maxPreferredHeight: " + m_maxPreferredHeight);
      m_maxPreferredHeight = height;
      System.out.println("max height: " + m_maxPreferredHeight);
    }
    else
      return;
  }

/** Returns the maximum cell height in the horizontal list for
  * ImageListControl&apos;s preferredSize.
*/
  public int getMaxPreferredHeight()
  {
    int maxHeight = 0;

    for (int i = 0; i < getModel().getSize(); i++)
    {
      int totalHeight;
      ImageListItem item = (ImageListItem)getModel().getElementAt(i);

      String text = item.getText();
      FontMetrics fm = this.getFontMetrics(getFont());

      // calculate image height
      int imageHeight = item.getImage().getIconHeight();

      // calculate text height
      int textHeight;

      int nChars;
      int totalWidth = 0;
      for(nChars = 0; nChars < text.length(); nChars++)
      {
        totalWidth += fm.charWidth(text.charAt(nChars));
        if (totalWidth > item.getImage().getIconWidth() * 2)
        {
          break;
        }
      }
      if (nChars >= 10)
        nChars = nChars - (nChars/2 - 1);

      int numLines = 1;

      if (numLines > 0)
        numLines = text.length()/nChars;

      if (numLines <= 3)
        textHeight = numLines * 17;
      else
        textHeight = 51;  // 17 * 3

      totalHeight = imageHeight + textHeight + 18; // 18 is for borders

      if (totalHeight > maxHeight)
        maxHeight = totalHeight;
    }

    return maxHeight;
  }

/** Shows if this ImageListControl uses visibleRowCount to display the elements.
  *
  * @returns boolean true = uses visibleRowCount for display; false = uses
  *                  preferredSize for display.
*/
  public boolean useVisibleRowCount()
  {
    return m_useVisibleRowCount;
  }

/** Allows the programmer to use either the visibleRowCount method (takes the
  * width of the first element and duplicates it for the number of
  * visibleRowCount) or the preferredSize method (setting the preferredSize to
  * dictate viewable width of list) for display.
  *
  * @returns boolean true = uses visibleRowCount for display; false = uses
  *                  preferredSize for display.
*/
  public void setUseVisibleRowCount(boolean b)
  {
    m_useVisibleRowCount = b;
  }


  public Dimension getViewportSize()
  {
    return m_viewSize;
  }

  public void setViewportSize(Dimension d)
  {
    m_viewSize = d;
  }


/**
  * --- The Scrollable Implementation ---  (override the JList implementation)
  */

/**
  * Compute the size of the viewport needed to display visibleRowCount
  * rows.  This is trivial if fixedCellWidth and fixedCellHeight
  * were specified.  Note that they can specified implicitly with
  * the prototypeCellValue property.  If fixedCellWidth wasn't specified,
  * it's computed by finding the widest list element.  If fixedCellHeight
  * wasn't specified then we resort to heuristics:
  * <ul>
  * <li>
  * If the model isn't empty we just multiply the height of the first row
  * by visibleRowCount.
  * <li>
  * If the model is empty, i.e. JList.getModel().getSize() == 0, then
  * we just allocate 16 pixels per visible row, and 256 pixels
  * for the width (unless fixedCellWidth was set), and hope for the best.
  * </ul>
  *
  * @see #getPreferredScrollableViewportSize
  * @see #setPrototypeCellValue
*/
  public Dimension getPreferredScrollableViewportSize()
  {
    Insets insets = getInsets();
    int dx = insets.left + insets.right;
    int dy = insets.top + insets.bottom;

    int visibleRowCount = getVisibleRowCount();
    int fixedCellWidth = getFixedCellWidth();
    int fixedCellHeight = getFixedCellHeight();

    if ((fixedCellWidth > 0) && (fixedCellHeight > 0))
    {
      int width = (visibleRowCount * fixedCellWidth) + dx;
      int height = fixedCellHeight + dy;
      return new Dimension(width, height);
    }
    else if (getModel().getSize() > 0)
    {

      if (!m_useVisibleRowCount)
      {
          // *** problem here!!!!!
        if (m_viewSize != null)
        {
          int width = getViewportSize().width + dx;
          int height = getViewportSize().height;
          return new Dimension(width, height);
        }
        else
        {
          int width = getPreferredSize().width + dx;
          int height = getPreferredSize().height;
          return new Dimension(width, height);
        }
      }
      else
      {
        Rectangle r = getCellBounds(0, 0);
        int width = (visibleRowCount * r.width) + dx;
        int height = getPreferredSize().height;
        return new Dimension(width, height);
      }
    }
    else
    {
      fixedCellHeight = (fixedCellHeight > 0) ? fixedCellHeight : 150;
      fixedCellWidth = (fixedCellWidth > 0) ? fixedCellWidth : 30;
      return new Dimension(fixedCellWidth * visibleRowCount, fixedCellHeight);
    }
  }


/**
  * Horizontal scrolling: return the lists font size or 1 if the font is null.
  * We're using the font size instead of the width of some canonical string,
  * e.g. "m", because it's cheaper.
  * <p>
  * Vertical scrolling: if we're scrolling downwards (<code>direction</code> is
  * greater than 0), and the first row is completely visible with respect
  * to <code>visibleRect</code>, then return its height.  If
  * we're scrolling downwards and the first row is only partially visible,
  * return the height of the visible part of the first row.  Similarly
  * if we're scrolling upwards we return the height of the row above
  * the first row, unless the first row is partially visible.
  *
  * @return The distance to scroll to expose the next or previous row.
  * @see javax.swing.Scrollable#getScrollableUnitIncrement
*/
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
  {
    int column = getFirstVisibleIndex();

    if (column == -1)
    {
      return 0;
    }
    else
    {
      /* Scroll Left */
      if (direction > 0)
      {
        Rectangle r = getCellBounds(column, column);

          int lastIndex = getLastVisibleIndex();
          // checking last visible index is the actual last index of List.
          Rectangle lastRect = getCellBounds(lastIndex, lastIndex);

          if (lastIndex == (getModel().getSize() - 1))
          {

            if ((lastRect.x + lastRect.width <= visibleRect.x + visibleRect.width))
            {
              return 0;
            }
            else
              return r.width - (visibleRect.x - r.x);

          }
          else
            return r.width - (visibleRect.x - r.x);
      }
      /* Scroll Right */
      else
      {
        Rectangle r = getCellBounds(column, column);

        /* The first row is completely visible and it's row 0.
         * We're done.
         */
        if ((r.x == visibleRect.x) && (column == 0))
        {
          return 0;
        }
        /* The first row is completely visible, return the
         * width of the previous row.
         */
        else if (r.x == visibleRect.x)
        {
          Rectangle prevR = getCellBounds(column - 1, column - 1);
          return (prevR== null) ? 0 : prevR.width;
        }
        /* The first row is partially visible, return the
         * height of hidden part.
         */
        else
        {
          return visibleRect.x - r.x;
        }
      }
    }
  }

//
// MEMBER VARIABLES
//

  private boolean m_useVisibleRowCount = false;
  private Dimension m_viewSize = null;
  private int m_maxPreferredHeight = 0;
}

