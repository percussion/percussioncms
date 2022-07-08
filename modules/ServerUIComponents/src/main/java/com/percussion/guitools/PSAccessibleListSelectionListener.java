/**[ PSAccessibleListSelectionListener.java ]********************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 ******************************************************************************/
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
    * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
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
