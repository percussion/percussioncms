/******************************************************************************
 *
 * [ PSJTableWithTooltips.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.guitools;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;


/**
 * A subclass of JTable in which the current cell's value will also be displayed
 * in a tooltip.
 */
public class PSJTableWithTooltips extends JTable
{
   /**
    * @see javax.swing.JTable#JTable(javax.swing.table.TableModel)
    */
   public PSJTableWithTooltips(AbstractTableModel model)
   {
      super(model);
   }
   
   @Override
   public String getToolTipText(MouseEvent e)
   {
      Point p = e.getPoint();
      int rowIndex = rowAtPoint(p);
      int colIndex = columnAtPoint(p);

      return (String) getValueAt(rowIndex, colIndex);
   }  
}
