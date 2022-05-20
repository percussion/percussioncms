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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class implements the method to set appropriate accessible name upon 
 * action event. This is mainly required since Swing components like JComboBox 
 * does not do right thing when selection is changed.
 *
 */
public class PSAccessibleActionListener implements ActionListener
{
   
   /**
    * Default Constructor to work in the current usage
    *
    */public PSAccessibleActionListener()
   {
   }
   
   /**
    * Accessible listener will prepend any string stored for echoing..
    * @param prependString
    */
   public PSAccessibleActionListener(String prependString)
   {
      m_prependStr = prependString;
   }
   /** Override to set the accessible name on selection change
    * @see ActionListener#actionPerformed(java.awt.event.
    * ActionEvent)
    */
   public void actionPerformed(ActionEvent e)
   {
      Object obj = e.getSource();
      if(obj instanceof JComboBox)
      {
         JComboBox cb = (JComboBox) obj;
         int index = cb.getSelectedIndex();
         if(index!=-1)
         {
            int count = cb.getItemCount();
            cb.getAccessibleContext().setAccessibleName(
                  m_prependStr + " " + cb.getSelectedItem().toString() + " " + index + " of " + count);
         }
      }
   }
   
   /**
    * @return Returns the prepend label for Accessibility.
    */
   public String getPrependStr()
   {
      return m_prependStr;
   }
   /**
    * @param prepepend label for Accessibility to set.
    */
   public void setPrependStr(String str)
   {
      m_prependStr = str;
   }
   private String m_prependStr = "";
   
}
