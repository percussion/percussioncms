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

package com.percussion.UTComponents;

import javax.swing.*;


/**
 * This class deals with PSAction objects rather than Action objects. PSAction
 * objects have more information about how to display themselves in a menu.
 * <p>
 * If the supplied action is not a PSAction, this object behaves identically 
 * to its base class.
 */
public class PSMenu extends JMenu
{
   // constructors
   /**
    * Default constructor.
    */
   PSMenu()
   {
   }
   
   /**
    * Present to match base class interface.
    * 
    * @param strLabel the label for menu, may be <code>null</code> or empty.
    */
   public PSMenu(String strLabel)
   {
      super(strLabel);
   }
   
   /**
    * Constructs menu with supplied label and mnemonic.
    * 
    * @param strLabel the label of the menu, may be <code>null</code> or empty.
    * @param mnemonic the keyboard character mnemonic 
    */
   public PSMenu(String strLabel, char mnemonic)
   {
      super(strLabel);
      setMnemonic( mnemonic);
   }
   
   /**
    * Similar to add(), but creates a check box menu item rather than a 
    * standard menu item.
    *
    * @returns the newly created menu item
    */
   public JCheckBoxMenuItem addCheckBox(PSAction a)
   {
      JCheckBoxMenuItem newItem = new JCheckBoxMenuItem((String) a.getValue(Action.NAME));
      decorateMenuItem(newItem, a);
      newItem.addActionListener(a);
      add(newItem);
      return(newItem);
   }
  
   public JMenuItem add(Action a)
   {
      JMenuItem newItem = null;
      if (a instanceof PSAction)
         newItem = insert(a, getItemCount());
      else
         newItem = super.add(a);
      return(newItem);
      
   }
   
   /**
    * Inserts the supplied action in this menu. If the supplied action is a 
    * PSAction object, checks if various properties are set. If they are, the
    * menuitem is modified appropriately.
    *
    * @returns the newly created MenuItem
    */
   public JMenuItem insert(Action a, int pos)
   {
      JMenuItem newItem = super.insert(a, pos);
      if (a instanceof PSAction)
      {
         // we have more info to set the menu item
         decorateMenuItem(newItem, (PSAction) a);
      }
      return(newItem);
      
   }

   /**
    * Takes properties out of the action and sets the corresponding property
    * in the menu item.
    *
    * @returns the passed in menu item
    */
   private JMenuItem decorateMenuItem(JMenuItem item, PSAction action)
   {
      char cMnemonic = action.getMnemonic();
      if (0 != cMnemonic)
           item.setMnemonic(cMnemonic);

      KeyStroke ks = action.getAccelerator();
      if (null != ks)
         item.setAccelerator(ks);
      item.setIcon(action.getIcon());
   
      String strTTText = action.getToolTipText();
      if (strTTText.length() > 0)
         item.setToolTipText(strTTText);
      return(item);
      
   }
}
