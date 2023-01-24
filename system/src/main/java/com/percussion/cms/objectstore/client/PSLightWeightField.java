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
package com.percussion.cms.objectstore.client;

import com.percussion.cms.PSDisplayChoices;
import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.design.objectstore.PSField;

/**
 * Feather weight object encpasulating a system, shared and local field.
 * Encapsulates field's display name, intername and data type.
 */
public class PSLightWeightField implements Comparable<PSLightWeightField>
{
   /**
    * Constructs the object.
    *
    * @param internalName internal name of the field, never <code>null</code>
    * or empty.
    *
    * @param dataType data type of the field, never <code>null</code>
    * or empty.
    *
    * @param displayName display name of the field, never <code>null</code>,
    * may be empty.
    * 
    * @param mnemonic The mnemonic of the field, may be <code>null</code> or 
    * empty, its lenght must be <= 1. 
    *
    * @throws IllegalArgumentException if the arguments are invalid.
    */
   public PSLightWeightField(String internalName, String dataType,
      String displayName, String mnemonic)
   {
      if (internalName == null || internalName.trim().length() == 0)
         throw new IllegalArgumentException(
         "Internal name cannot be null or empty");
      m_internalName = internalName;
      if (dataType == null || dataType.trim().length() == 0)
         throw new IllegalArgumentException(
         "Data type cannot be null or empty");
      m_dataType = normalizeDataType(dataType);
      if (displayName == null)
         throw new IllegalArgumentException(
         "Display name cannot be null");
      m_displayName = displayName;
      
      if (mnemonic == null)
         mnemonic = "";
      mnemonic = mnemonic.trim();
      if (mnemonic.length() > 1)
         throw new IllegalArgumentException("mnemonic must be <= 1");
      m_mnemonic = mnemonic;
   }

   /**
    * Normalizes the datatype to one of the <code>PSSearchField.TYPE_XXX</code>
    * types supported by this class.
    *
    * @param type The type to normalize, assumed not <code>null</code> or empty.
    *
    * @return The normalized type if it is one of the recognized
    * <code>PSField</code> types, otherwise the supplied <code>type</code>
    * is returned.  Never <code>null</code> or empty.
    */
   private String normalizeDataType(String type)
   {
      if (type.equalsIgnoreCase(PSField.DT_INTEGER))
         return PSSearchField.TYPE_NUMBER;
      else if (type.equalsIgnoreCase(PSField.DT_DATETIME))
         return PSSearchField.TYPE_DATE;
      else if (type.equalsIgnoreCase(PSField.DT_TEXT))
         return PSSearchField.TYPE_TEXT;
      else
         return type;
   }

   // see interface for description
   public int compareTo(PSLightWeightField o)
   {
      return m_displayName.compareTo(o.m_displayName);
   }

   /**
    * Gets the display name.
    *
    * @return The name, never <code>null</code> may be empty.
    */
   public String getDisplayName()
   {
      return m_displayName;
   }

   /**
    * Gets the mnemonic for the display name.
    *
    * @return the mnemonic character, never <code>null</code> may be empty,
    *    it's size is always <= 1.
    */
   public String getMnemonic()
   {
      return m_mnemonic;
   }

   /**
    * Gets the internal name.
    *
    * @return The name, never <code>null</code> or empty.
    */
   public String getInternalName()
   {
      return m_internalName;
   }

   /** Gets the data type.
    *
    * @return The type, never <code>null</code> or empty.
    */
   public String getDataType()
   {
      return m_dataType;
   }

   /**
    * Sets the choices to use for keyword entries for this field.
    *
    * @param choices The display choices to use for keyword support, may be
    * <code>null</code> to clear the choices.
    */
   public void setDisplayChoices(PSDisplayChoices choices)
   {
      m_choices = choices;
   }

   /**
    * Get the keyword choices for this field.
    *
    * @return The display choices object, may be <code>null</code> if
    * this field does not support keywords.
    */
   public PSDisplayChoices getDisplayChoices()
   {
      return m_choices;
   }

   /**
    * Returns internal field name.
    * @return internalName, never <code>null</code> or empty.
    */
   public String toString()
   {
      return m_internalName;
   }

   /**
    * Internal name of the field, initialized in the ctor never <code>null
    * </code> or empty.
    */
   private String m_internalName;

   /**
    * Display name of the field, initialized in the ctor never <code>null
    * </code> may be empty.
    */
   private String m_displayName;
   
   /**
    * The menmonic character to used for the display label. Initialized in
    * constructor, never <code>null</code> after that, may be empty.
    */
   private String m_mnemonic = null;

   /**
    * Data type of the field, initialized in the ctor never <code>null
    * </code> or empty.
    */
   private String m_dataType;

   /**
    * The display choices object representing keyword choices for this field.
    * Modified by calls to <code>setDisplayChoices()</code>, may be
    * <code>null</code>.
    */
   private PSDisplayChoices m_choices = null;
}
