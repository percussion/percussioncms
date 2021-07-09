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

package com.percussion.layout;

import java.awt.AWTError;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SizeRequirements;

/**
 * The layout manager which combines the features of GridLayout and BoxLayout.
 * The number of rows and cells for all components are calculated according to
 * GridLayout and uses the BoxLayout mechanism for laying out components for
 * each row and column.
 */
public class PSGridBoxLayout extends GridLayout implements LayoutManager2
{

   /**
    * Convenience constructor for {@link
    * #PSGridBoxLayout(Container, int, int, int, int)
    * PSGridBoxLayout(target, rows, cols, 0, 0) }.
    */
   public PSGridBoxLayout(Container target, int rows, int cols)
   {
      this(target, rows, cols, 0, 0);
   }

   /**
    * Creates GridBoxLayout with specified number of rows and columns.
    * Please see <code>GridLayout(rows, cols, hgap, vgap)</code> for description
    * of how the components set in the cells. The components are laid out
    * according to their preferred, maximum and minimum sizes and according to
    * their x and y alignments.
    *
    * @param target the target container to be laid out, may not be
    * <code>null</code>
    * @param rows the rows, with the value equal to zero meaning any number of
    * rows.
    * @param cols the columns, with the value equal to zero meaning any number
    * of columns.
    * @param     hgap   the horizontal gap.
    * @param     vgap   the vertical gap.
    *
    * @throws IllegalArgumentException if both rows and columns are zero or if
    * the target is <code>null</code>
    */
   public PSGridBoxLayout(Container target,
      int rows, int cols, int hgap, int vgap)
   {
      super(rows, cols, hgap, vgap);
      if(target == null)
         throw new IllegalArgumentException("target can not be null");
      m_target = target;
   }

   //see interface for description, not implemented by this layout
   public void addLayoutComponent(Component comp, Object constraints)
   {
   }

   /**
    * Gets the maximum layout size for the specified target.
    *
    * @param target the target to get the size, may not be <code>null</code>
    *
    * @return the maximum size, never <code>null</code>. If no components are
    * added to the target it gets the maximum possible dimension.
    *
    * @throws AWTError if the target is not the target set with this layout.
    */
   public Dimension maximumLayoutSize(Container target)
   {
      checkContainer(target);

      if(!calculateSizeRequirements(target))
         return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

      Dimension size = new Dimension(m_totalWidth.maximum,
         m_totalHeight.maximum);
      Insets insets = target.getInsets();
      size.width = (int) Math.min((long) size.width + (long) insets.left +
         (long) insets.right + (m_cols-1)*getHgap(), Integer.MAX_VALUE);
      size.height = (int) Math.min((long) size.height + (long) insets.top +
         (long) insets.bottom + (m_rows-1)*getVgap(), Integer.MAX_VALUE);
      return size;
   }

   /**
    * Gets the minimum layout size for the specified target.
    *
    * @param target the target to get the size, may not be <code>null</code>
    *
    * @return the minimum size, never <code>null</code>. If no components are
    * added to the target it gets the minimum possible dimension.
    *
    * @throws AWTError if the target is not the target set with this layout.
    */
   public Dimension minimumLayoutSize(Container target)
   {
      checkContainer(target);

      if(!calculateSizeRequirements(target))
         return new Dimension(Integer.MIN_VALUE, Integer.MIN_VALUE);

      Dimension size = new Dimension(m_totalWidth.minimum, m_totalHeight.minimum);
      Insets insets = target.getInsets();
      size.width = (int) Math.min((long) size.width + (long) insets.left +
         (long) insets.right + (m_cols-1)*getHgap(), Integer.MAX_VALUE);
      size.height = (int) Math.min((long) size.height + (long) insets.top +
         (long) insets.bottom + (m_rows-1)*getVgap(), Integer.MAX_VALUE);
      return size;
   }

   /**
    * Gets the preferred layout size for the specified target.
    *
    * @param target the target to get the size, may not be <code>null</code>
    *
    * @return the preferred size, never <code>null</code>. If no components are
    * added to the target it gets the minimum possible dimension.
    *
    * @throws AWTError if the target is not the target set with this layout.
    */
   public Dimension preferredLayoutSize(Container target)
   {
      checkContainer(target);

      if(!calculateSizeRequirements(target))
         return new Dimension(Integer.MIN_VALUE, Integer.MIN_VALUE);

      Dimension size = new Dimension(m_totalWidth.preferred, m_totalHeight.preferred);
      Insets insets = target.getInsets();
      size.width = (int) Math.min((long) size.width + (long) insets.left +
         (long) insets.right + (m_cols-1)*getHgap(), Integer.MAX_VALUE);
      size.height = (int) Math.min((long) size.height + (long) insets.top +
         (long) insets.bottom + (m_rows-1)*getVgap(), Integer.MAX_VALUE);
      return size;
   }

   /**
    * Gets the x-alignment of the specified target.
    *
    * @param target the target to get the size, may not be <code>null</code>
    *
    * @return 0.5f
    *
    * @throws AWTError if the target is not the target set with this layout.
    */
   public float getLayoutAlignmentX(Container target)
   {
      checkContainer(target);
      return 0.5f;
   }

   /**
    * Gets the y-alignment of the specified target.
    *
    * @param target the target to get the size, may not be <code>null</code>
    *
    * @return 0.5f
    *
    * @throws AWTError if the target is not the target set with this layout.
    */
   public float getLayoutAlignmentY(Container target)
   {
      checkContainer(target);
      return 0.5f;
   }

   /**
    * Invalidates the layout.
    *
    * @param target the target to invalidate, may not be <code>null</code>
    *
    * @throws AWTError if the target is not the target set with this layout.
    */
   public void invalidateLayout(Container target)
   {
      checkContainer(target);
      m_components = null;
      m_compWidths = null;
      m_compHeights = null;
      m_totalWidth = null;
      m_totalHeight = null;
      m_colAlignedSize = null;
      m_rowAlignedSize = null;
      m_rows = 1; //Single row always
      m_cols = 0;
   }

   /**
    * Sets the number of rows in this layout with the specified value and
    * invalidates the layout.
    *
    * @param rows the number of rows
    *
    * @throws IllegalArgumentException if number of columns is <code>zero</code>
    * and supplied <code>rows</code> is <code>zero</code>
    */
   public void setRows(int rows)
   {
      super.setRows(rows);
      invalidateLayout(m_target);
   }

   /**
    * Sets the number of columns in this layout with the specified value and
    * invalidates the layout.
    *
    * @param columns the number of columns
    *
    * @throws IllegalArgumentException if number of rows is <code>zero</code>
    * and supplied <code>cols</code> is <code>zero</code>
    */
   public void setColumns(int cols)
   {
      super.setColumns(cols);
      invalidateLayout(m_target);
   }

   /**
    * Lays out the supplied container using this layout. If the number of rows
    * are zero, it calculates the number of rows based on the components added
    * and the number of columns and visa-versa for number of columns. All
    * components are laid out in each cell according to their preferred size and
    * aligned according to their x and y alignments.
    * <br>
    * For a typical case like 2 rows and 2 columns and specified with right
    * alignment for xalignment of components in the first column, and left
    * alignment for xalignment of components in the second column and center
    * alignment for yalignment of all components then the layout of the
    * container will be like this. When we resize the container the components
    * grow till they reach their maximum size in that direction.
    * <PRE>
    *               hgap
    * _____________________________________________
    * |              |  | _                        |
    * |              |  ||_| checkbox1             |
    * |        Label1|  | _                        |
    * |              |  ||_| checkbox2             |
    * |______________|__|__________________________|
    * |______________|__|__________________________|  vgap
    * |              |  | ______________________   |
    * |     Label1000|  ||_TextField1___________|  |
    * |______________|__|__________________________|
    *
    * </PRE>
    *
    * @param parent the container to be laid out, may not be <code>null</code>
    *
    * @throws AWTError if the parent is not the container set with this layout.
    */
   public void layoutContainer(Container parent)
   {
      checkContainer(parent);

      if(!calculateSizeRequirements(parent))
         return;

      //Get total available width and height from container
      Insets insets = parent.getInsets();
      int w = parent.getWidth() -
         (insets.left + insets.right + (m_cols-1)*getHgap());
      int h = parent.getHeight() -
         (insets.top + insets.bottom + (m_rows-1)*getVgap());

      /* Get X and Y offsets and spans for each cell based on aligned size
       * requirements horizontally and vertically.
       */
      int[] xCellOffsets = new int[m_cols];
      int[] xCellSpans = new int[m_cols];
      int[] yCellOffsets = new int[m_rows];
      int[] yCellSpans = new int[m_rows];

      SizeRequirements.calculateTiledPositions(
         w, m_totalWidth, m_colAlignedSize, xCellOffsets, xCellSpans);
      SizeRequirements.calculateTiledPositions(
         h, m_totalHeight, m_rowAlignedSize, yCellOffsets, yCellSpans);

      //Get X and Y offsets and spans for each component
      int[][] compXOffsets = new int[m_rows][m_cols];
      int[][] compYOffsets = new int[m_rows][m_cols];
      int[][] compXSpans = new int[m_rows][m_cols];
      int[][] compYSpans = new int[m_rows][m_cols];

      /* Uses the xspan of the column as available width to layout the
       * components in cells of that column and calculates the xoffset of the
       * component by adding the cell xoffset to its xoffset with in the cell.
       */
      List colChildren;
      for(int c=0; c<m_cols; c++)
      {
          int alloc_width = xCellSpans[c];
          int[] xOffsets = new int[m_rows];
          int[] xSpans = new int[m_rows];

          colChildren = new ArrayList();
          for(int r=0; r<m_rows; r++)
          {
             if(m_compWidths[r][c] != null)
                colChildren.add(m_compWidths[r][c]);
          }
          SizeRequirements[] children = (SizeRequirements[])
          colChildren.toArray( new SizeRequirements[colChildren.size()] );
          SizeRequirements total =
             SizeRequirements.getAlignedSizeRequirements(children);
          SizeRequirements.calculateAlignedPositions(
             alloc_width, total, children, xOffsets, xSpans);

          for(int r=0; r<m_rows; r++)
          {
             compXOffsets[r][c] = xCellOffsets[c] + xOffsets[r];
             compXSpans[r][c] =  xSpans[r];
          }
      }

      /* Uses the yspan of the row as available height to layout the
       * components in cells of that row and calculates the yoffset of the
       * component by adding the cell yoffset to its yoffset with in the cell.
       */
      List rowChildren;
      for(int r=0; r<m_rows; r++)
      {
         int alloc_height = yCellSpans[r];
         int[] yOffsets = new int[m_cols];
         int[] ySpans = new int[m_cols];

         rowChildren = new ArrayList();
         for(int c=0; c<m_cols; c++)
         {
            if(m_compHeights[r][c] != null)
               rowChildren.add(m_compHeights[r][c]);
         }
         SizeRequirements[] children = (SizeRequirements[])
            rowChildren.toArray( new SizeRequirements[rowChildren.size()] );
         SizeRequirements total =
            SizeRequirements.getAlignedSizeRequirements(children);

         SizeRequirements.calculateAlignedPositions(
            alloc_height, total, children, yOffsets, ySpans);

         for(int c=0; c<m_cols; c++)
         {
            compYOffsets[r][c] = yCellOffsets[r] + yOffsets[c];
            compYSpans[r][c] =  ySpans[c];
         }
      }

      //Sets bounds for each component in the container by including insets
      //values and gaps
      for(int r=0; r<m_rows; r++)
      {
         for(int c=0; c<m_cols; c++)
         {
            Component comp = m_components[r][c];
            int hgap = c*getHgap();
            int vgap = r*getVgap();

            if(comp != null)
            {
               comp.setBounds(
                  (int) Math.min(
                     (long)insets.left + (long)compXOffsets[r][c] + (long)hgap,
                     Integer.MAX_VALUE),
                  (int) Math.min(
                     (long)insets.top + (long)compYOffsets[r][c] + (long)vgap,
                     Integer.MAX_VALUE),
                  compXSpans[r][c], compYSpans[r][c]);
            }
         }
      }
   }

   /**
    * Checks whether the target is the target that is set with this layout.
    *
    * @throws AWTError if the target is not the target that is set with this
    * layout.
    */
   private void checkContainer(Container target)
      throws AWTError
   {
      if(m_target != target)
         throw new AWTError("This layout can not be shared");
   }

   /**
    * Calculates the number of rows and columns to have in this layout and gets
    * aligned size requirements for each column and row. Reserves the space for
    * border insets, horizontal and vertical gaps and uses the remaining width
    * and height to get each column width and row height. Caches the components,
    * aligned size requirements and total width and height.
    *
    * @param the container to be laid out, assumed not to be <code>null</code>
    *
    * @return <code>true</code> if the parent has components, otherwise
    * <code>false</code>.
    */
   private boolean calculateSizeRequirements(Container parent)
   {
      //We already calculated aligned size requirements for each column and row
      //So just return now.
      if(m_components != null)
         return true;

      //No components, nothing to calculate
      int ncomponents = parent.getComponentCount();
      if (ncomponents == 0) {
         return false;
      }

      //If rows are specified calculate columns based on number of components
      //and rows and visa-versa.
      m_rows = getRows();
       m_cols = getColumns();
       if (m_rows > 0) {
         m_cols = (ncomponents + m_rows - 1) / m_rows;
        } else {
         m_rows = (ncomponents + m_cols - 1) / m_cols;
        }

      //Set components in each cell
      m_components = new Component[m_rows][m_cols];
      for(int c=0; c<m_cols; c++)
      {
         for(int r=0; r<m_rows; r++)
         {
            int i = r * m_cols + c;
            if(i < ncomponents)
               m_components[r][c] = parent.getComponent(i);
         }
      }

      //Get width and height requirements for each component
      m_compWidths = new SizeRequirements[m_rows][m_cols];
      m_compHeights = new SizeRequirements[m_rows][m_cols];
      for(int r=0; r<m_rows; r++)
      {
         for(int c=0; c<m_cols; c++)
         {
            Component comp = m_components[r][c];
            if(m_components[r][c] != null)
            {
               Dimension min = comp.getMinimumSize();
               Dimension typ = comp.getPreferredSize();
               Dimension max = comp.getMaximumSize();
               m_compWidths[r][c] = new SizeRequirements(
                  min.width, typ.width, max.width, comp.getAlignmentX());
               m_compHeights[r][c] = new SizeRequirements(
                  min.height, typ.height, max.height, comp.getAlignmentY());
            }
         }
      }

      //Get height requirement for each row for proper alignment of all
      //components in that row.
      List rowHeightChildren;
      m_rowAlignedSize = new SizeRequirements[m_rows];
      for(int r=0; r<m_rows; r++)
      {
         rowHeightChildren = new ArrayList();
         for(int c=0; c<m_cols; c++)
         {
            if(m_compHeights[r][c] != null)
               rowHeightChildren.add(m_compHeights[r][c]);
         }
         m_rowAlignedSize[r] = SizeRequirements.getAlignedSizeRequirements(
            (SizeRequirements[]) rowHeightChildren.toArray(
               new SizeRequirements[rowHeightChildren.size()] ) );
      }

      //Get width requirement for each column for proper alignment of all
      //components in that column.
      m_colAlignedSize = new SizeRequirements[m_cols];
      List colWidthChildren;
      for(int c=0; c<m_cols; c++)
      {
         colWidthChildren = new ArrayList();
         for(int r=0; r<m_rows; r++)
         {
            if(m_compWidths[r][c] != null)
               colWidthChildren.add(m_compWidths[r][c]);
         }
         m_colAlignedSize[c] = SizeRequirements.getAlignedSizeRequirements(
            (SizeRequirements[]) colWidthChildren.toArray(
               new SizeRequirements[colWidthChildren.size()] ) );
      }

      //Gets the total width and height of container if all components are
      //aligned properly in X and Y directions
      m_totalWidth =
         SizeRequirements.getTiledSizeRequirements(m_colAlignedSize);
      m_totalHeight =
         SizeRequirements.getTiledSizeRequirements(m_rowAlignedSize);

      return true;
   }

   /**
    * The components to be laid out in the container. This holds a component
    * for each cell. Gets initialized in <code>
    * calculateSizeRequirements(Container)</code> and set to <code>null</code>
    * when the layout is invalidated.
    */
   private Component[][] m_components = null;

   /**
    * The width requirements for each component. Gets initialized in <code>
    * calculateSizeRequirements(Container)</code> and set to <code>null</code>
    * when the layout is invalidated.
    */
   private SizeRequirements[][] m_compWidths = null;

   /**
    * The height requirements for each component. Gets initialized in <code>
    * calculateSizeRequirements(Container)</code> and set to <code>null</code>
    * when the layout is invalidated.
    */
   private SizeRequirements[][] m_compHeights = null;

   /**
    * The total width requirement of the container if all components are aligned
    * horizontally in each column. Gets initialized in <code>
    * calculateSizeRequirements(Container)</code> and set to <code>null</code>
    * when the layout is invalidated.
    */
   private SizeRequirements m_totalWidth = null;

   /**
    * The total height requirement of the container if all components are
    * aligned vertically in each column. Gets initialized in <code>
    * calculateSizeRequirements(Container)</code> and set to <code>null</code>
    * when the layout is invalidated.
    */
   private SizeRequirements m_totalHeight = null;

   /**
    * The array of aligned size(width) requirements for each column. Gets
    * initialized in <code>calculateSizeRequirements(Container)</code> and set
    * to <code>null</code> when the layout is invalidated.
    */
   private SizeRequirements[] m_colAlignedSize = null;

   /**
    * The array of aligned size(height) requirements for each row. Gets
    * initialized in <code>calculateSizeRequirements(Container)</code> and set
    * to <code>null</code> when the layout is invalidated.
    */
   private SizeRequirements[] m_rowAlignedSize = null;

   /**
    * The number of rows to layout the components. Gets initialized in <code>
    * calculateSizeRequirements(Container)</code> and set to <code>1</code>
    * when the layout is invalidated.
    */
   private int m_rows;

   /**
    * The number of columns to layout the components. Gets initialized in <code>
    * calculateSizeRequirements(Container)</code> and set to <code>0</code>
    * when the layout is invalidated.
    */
   private int m_cols;

   /**
    * The target container that is set with this layout that needs to be laid
    * out, set in the constructor and never modified or <code>null</code> after
    * that.
    */
   private Container m_target;
}
