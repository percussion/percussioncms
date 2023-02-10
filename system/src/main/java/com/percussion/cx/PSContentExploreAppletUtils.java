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
