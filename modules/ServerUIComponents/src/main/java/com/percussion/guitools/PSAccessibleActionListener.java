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
    */
   public void setPrependStr(String str)
   {
      m_prependStr = str;
   }
   private String m_prependStr = "";
   
}
