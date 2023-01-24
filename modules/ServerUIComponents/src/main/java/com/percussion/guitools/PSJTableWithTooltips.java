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
