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
