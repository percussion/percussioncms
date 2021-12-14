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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.i18n;


/**
 * A container class for an unit in the TMX resource bundle
 */
public class PSTmxUnit
{
   
   /**
    * Constructs the object from supplied parameters.
    * 
    * @param value The value of a TMX unit, it may be <code>null</code>
    *    or empty.
    * @param mnemonic The mnemonic string of the TMX entry. It may be
    *    <code>null</code> or empty.
    * @param tooltip The tooltip string of the TMX entry. It may be 
    *    <code>null</code> or empty
    */
   public PSTmxUnit(String value, String mnemonic, String tooltip)
   {   
      int m;
      
      if (mnemonic == null || mnemonic.trim().length() == 0)
         m = 0;
      else if (mnemonic.length() > 1)
         throw new IllegalArgumentException("Mnemonic string may only have " +
               "one character");
      else
         m = mnemonic.charAt(0);
      
      init(value, m, tooltip);
   }

   /**
    * Ctor
    * @param value The value of a TMX unit, it may be <code>null</code>
    *    or empty.
    * @param mnemonic The mnemonic value of the TMX unit
    * @param tooltip The tooltip string of the TMX unit. It may be 
    *    <code>null</code> or empty
    */
   public PSTmxUnit(String value, int mnemonic, String tooltip) {
      init(value, mnemonic, tooltip);
   }

   /**
    * Initialize object
    * @param value The value of a TMX unit, it may be <code>null</code>
    *    or empty.
    * @param mnemonic The mnemonic value of the TMX unit
    * @param tooltip The tooltip string of the TMX unit. It may be 
    *    <code>null</code> or empty
    */
   private void init(String value, int mnemonic, String tooltip)
   {
      m_value = (value == null) ? "" : value;
      m_mnemonic = mnemonic;
      
      if (tooltip == null || tooltip.trim().length() == 0)
      {
         m_tooltip = null;
      }
      else
      {
         m_tooltip = tooltip;
      }
   }
   
   
   /**
    * Returns if this unit has a valid value
    * @return the value is <code>true</code> if the value is non-empty and
    * not <code>null</code>
    */
   public boolean isValid()
   {
      return m_value != null && m_value.trim().length() > 0;
   }

   /**
    * Overwrite {@link Object#toString()}
    */
   public String toString()
   {
      return m_value;
   }
   
   /**
    * Get the mnemonic
    * @return the mnemonic, <code>0</code> if there is no mnemonic
    */
   public int getMnemonic()
   {
      return m_mnemonic;
   }
   /**
    * Get the tooltip
    * @return the tooltip, <code>null</code> if no tooltip is defined
    */
   public String getTooltip()
   {
      return m_tooltip;
   }
   /**
    * Get the value
    * @return the value, never <code>null</code> but may be empty if
    * there is no value.
    */
   public String getValue()
   {
      return m_value;
   }
   
   /**
    * The value of the string. Initialized by ctor, never 
    * <code>null</code>, but may be empty.
    */
   private String m_value;
   
   /**
    * The mnemonic character. Initialized by ctor, may be 
    * <code>0</code> if not exist. 
    */
   private int m_mnemonic;
   
   /**
    * The tooltip, intialized in the ctor, may be <code>null</code> if
    * not specified in the tuv entry.
    */
   private String m_tooltip;
}
