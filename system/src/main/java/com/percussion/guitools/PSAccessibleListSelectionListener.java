/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
package com.percussion.guitools;

import com.percussion.cms.objectstore.PSObjectAclEntry;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * This class implements the method to set appropriate accessible name upon 
 * action event. This is mainly required since Swing components like JComboBox 
 * does not do right thing when selection is changed.
 *
 */
public class PSAccessibleListSelectionListener
   implements ListSelectionListener
{
   /** Override to set the accessible name on selection change
    * @see ListSelectionListener#valueChanged(ListSelectionEvent)
    */
   public void valueChanged(ListSelectionEvent e)
   {
      if(e.getValueIsAdjusting())
         return;
      Object obj = e.getSource();
      JList list = (JList)obj;
      int selIndex = list.getSelectedIndex();
      int selCount = list.getSelectedIndices().length;
      int size = list.getModel().getSize();
      if(size>0 && selIndex!=-1 && selCount>0)
      {
         Object obj2 = list.getSelectedValue();
         String str = "";
         if ( obj2 instanceof PSObjectAclEntry )
            str = ((PSObjectAclEntry)obj2).getName();
         else
            str = obj2.toString();
         list.getAccessibleContext().setAccessibleName(
                 str
               + " "
               + selCount
               + " selected out of"
               + size
               + " total");
      }
   }

}
