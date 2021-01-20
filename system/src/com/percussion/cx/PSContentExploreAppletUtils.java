/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.cx;

import com.percussion.i18n.ui.PSI18NTranslationKeyValues;

public class PSContentExploreAppletUtils {

	/**
	    * Convenience method that calls
	    * {@link PSContentExploreAppletUtils#getResourceMnemonic(String, String, char)
	    * getResourceMnemonic(resClass.getName(), label, mnemonic)}
	    */
	   public static char getResourceMnemonic(Class resClass, String label, char mnemonic)
	   {
	      if (resClass == null)
	         throw new IllegalArgumentException("resClass may not be null.");
	
	      if (label == null || label.trim().length() == 0)
	         throw new IllegalArgumentException("label may not be null or empty");
	
	      return PSContentExploreAppletUtils.getResourceMnemonic(resClass.getName(), label, mnemonic);
	   }

	/**
	    * This method retrieves a resource that must contain a single character to
	    * use as the mnemonic for a given label, button or menu item.
	    * 
	    * @param category the category of the resource, usually the classname, must
	    *           never be <code>null</code> or empty
	    * @param label the label of the specific item, must never be
	    *           <code>null</code> or empty
	    * @param mnemonic the value to return if no mnemonic is found
	    * @return The mnemonic or the mnemonic parameter if no mnemonic is found
	    */
	   public static char getResourceMnemonic(String category, String label, char mnemonic)
	   {
	      if (category == null || category.trim().length() == 0)
	         throw new IllegalArgumentException("category may not be null or empty");
	
	      if (label == null || label.trim().length() == 0)
	         throw new IllegalArgumentException("label may not be null or empty");
	
	      // Make sure we strip @ from the label
	      if (label.startsWith("@") && label.length() > 1)
	         label = label.substring(1);
	
	      String string = category + "@" + label;
	
	      int m = PSI18NTranslationKeyValues.getInstance().getMnemonic(string);
	
	      if (m != 0)
	      {
	         return (char) m;
	      }
	      else
	      {
	         return mnemonic;
	      }
	   }

	/**
	    * Lookup the associated tooltip text for the given class and label
	    * 
	    * @param clazz the class, must never be <code>null</code>
	    * @param label the label, must never be <code>null</code> or empty
	    * @return the tooltip or <code>null</code> if undefined
	    */
	   public static String getResourceTooltip(Class clazz, String label)
	   {
	      if (clazz == null)
	         throw new IllegalArgumentException("clazz may not be null.");
	
	      if (label == null || label.trim().length() == 0)
	         throw new IllegalArgumentException("label may not be null or empty");
	
	      // Make sure we strip @ from the label
	      if (label.startsWith("@") && label.length() > 1)
	         label = label.substring(1);
	
	      String key = clazz.getName() + "@" + label;
	
	      return PSI18NTranslationKeyValues.getInstance().getTooltip(key);
	   }

}
