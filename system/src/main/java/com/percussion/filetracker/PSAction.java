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

package com.percussion.filetracker;

import javax.swing.*;

/**
 * This class adds storage for the mnemonic letter and accelerator keys used 
 * with menu items. When a menu item is created using add(action), the mnemonic
 * and accel key can be taken from this object and added to the newly created
 * menu item.
 * <p>
 * The class is abstract because it does not define actionPerformed().
 */
abstract class PSAction extends AbstractAction
{
   // constructors
   /**
    * @param strMenuText text displayed with the item, if null or empty, no
    * text is displayed
    * @param cMnemonic the character that is set to access menu items by using
    * ALT key combos, if 0, it is ignored
    * @param AccelKey keystroke describing the accelerator key for this action,
    * ignored if null
    */
   public PSAction(String strMenuText, char cMnemonic, KeyStroke AccelKey)
   {
      this(strMenuText, cMnemonic, AccelKey, (ImageIcon)null);
   }


   /**
    * @param strMenuText text displayed with the item, if null or empty, no
    * text is displayed
    * @param cMnemonic the character that is set to access menu items by using
    * ALT key combos, if 0, it is ignored
    * @param Icon the image for the UI, ignored if null
    */
   public PSAction(String strMenuText, char cMnemonic, Icon Img)
   {
      this(strMenuText, cMnemonic, (KeyStroke)null, Img);
   }


   /**
    * @param strMenuText text displayed with the item, if null or empty, no
    * text is displayed
    * @param cMnemonic the character that is set to access menu items by using
    * ALT key combos, if 0, it is ignored
    * @param AccelKey keystroke describing the accelerator key for this action,
    * ignored if null
    * @param Icon the image for the UI, ignored if null
    */
   public PSAction(String strMenuText, char cMnemonic, KeyStroke AccelKey,
      Icon Img)
   {
      super(null == strMenuText ? "" : strMenuText, 
         null == Img ? new ImageIcon() : Img);

      if (0 != cMnemonic)
         putValue(MNEMONIC_KEY, new Character(cMnemonic));
      if (null != AccelKey)
         putValue(ACCEL_KEY, AccelKey);
   }

   // attributes
   /**
    * This is the internal name of the action. If present, the name of the 
    * component will be set to this value.
    *
    * @param strName the internal name of the action. If empty or null, the 
    * internal name is cleared.
    */
   public void setInternalName(String strName)
   {
      if (0 == strName.trim().length())
         strName = null;   

      putValue(INTERNAL_NAME_KEY, strName);
   }

   /**
    * Returns the internal name assigned to this action. If there is no internal
    * name, null is returned.
    */
   public String getInternalName()
   {
      try
      {
         String strText = (String)getValue(INTERNAL_NAME_KEY);
         return(strText);
      } 
      catch (ClassCastException e)
      {
         return(null);
      }
   }

   /**
    * Returns <code>true</code> if this action has a successfully loaded icon 
    * image, otherwise, <code>false</code> is returned.
    */
   public boolean hasIcon()
   {
      Icon img = (Icon) getValue(SMALL_ICON);
      return (img.getIconWidth() > 0 && img.getIconHeight() > 0);
   }

   /**
    * Returns the previously set tool tip text. If no tip has been set, the
    * empty string is returned.
    */
   public String getToolTipText()
   {
      try
      {
         String strText = (String)getValue(SHORT_DESCRIPTION);
         return(null == strText ? "" : strText);
      } 
      catch (ClassCastException e)
      {
         return("");
      }
   }

   /**
    * Sets the tooltip for this action item. If strToolTip is null, the tip
    * is cleared.
    */
   public void setToolTipText(String strToolTip)
   {
      if (null == strToolTip)
         strToolTip = "";
      putValue(SHORT_DESCRIPTION, strToolTip);
   }

   /**
    * Returns the image icon for this action or null if there is not a loaded
    * image.
    */
   public Icon getIcon()
   {
      return(hasIcon() ? (Icon) getValue(SMALL_ICON) : null);
   }

   /**
    * Sets the the icon to the provided icon.
    */
   public void setIcon(Icon icon)
   {
      putValue(SMALL_ICON, icon);
   }

  /**
    * @returns the mnemonic letter associated with this action item. If it has
    * not been set, 0 is returned.
    */
   public char getMnemonic()
   {
      try
      {
         Character Mnemonic = (Character)getValue(MNEMONIC_KEY);
         return(null == Mnemonic ? 0 : Mnemonic.charValue());
      }
      catch (ClassCastException e)
      {
         return(0);
      }
   }

  /**
    * Sets the new mnemonic.
    */
   public void setMnemonic(Character mnemonic)
   {
    putValue(MNEMONIC_KEY, mnemonic);
   }

   /**
    * @returns the accelerator key associated with this action item. If no
    * key has been set, null is returned.
    */
   public KeyStroke getAccelerator()
   {
      return((KeyStroke) getValue(ACCEL_KEY));
   }

   /**
    * Sets the new accelerator key.
    */
   public void setAccelerator(KeyStroke key)
   {
      putValue(ACCEL_KEY, key);
   }

   // private constants
   final static String MNEMONIC_KEY = "mn";
   final static String ACCEL_KEY = "ak";
   final static String TOOLTIP_KEY = "tt";
   final static String INTERNAL_NAME_KEY = "iname";

   // private storage
}   
