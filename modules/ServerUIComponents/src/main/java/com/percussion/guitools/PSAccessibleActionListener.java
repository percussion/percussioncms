/**[ PSAccessibleActionListener.java ]********************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 ******************************************************************************/
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
