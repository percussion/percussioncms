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
