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
package com.percussion.cms.objectstore;

/**
 * The value of an <code>PSItemField</code> that is treated as a
 * <code>String</code> value.
 */
public class PSTextValue extends PSFieldValue
{
   /**
    * Creates a new instance with the <code>textValue</code> as its value.
    * The text, may be <code>null</code> or empty.
    *
    * @see #setText(String)
    * @param textValue - may be <code>null</code> or empty.
    */
   public PSTextValue(String textValue)
   {
      setText(textValue);
   }

   /**
    * Sets the text this value contains.
    *
    * @param textValue - may be <code>null</code> or empty, if
    * <code>null</code> is supplied empty is stored("").
    */
   public void setText(String textValue)
   {
      if(textValue == null)
         textValue = "";

      m_value = textValue;
   }

   /**
    * In order to implement the <code>IPSFieldValue</code> interface we must
    * have a <code>getValue</code> that returns an <code>Object</code>.  The
    * value stored in this class is a <code>String</code> so instead of
    * calling <code>getValue</code> and having to cast, this convenience method
    * is supplied that returns the <code>String</code>.
    *
    * @return the <code>String</code> never <code>null</code> may be empty.
    */
   public String getValueAsString()
   {
      return m_value;
   }

   /**
    * Clones this objects.  Makes a deep copy.
    *
    * @return deep copy of this object.
    */
   public Object clone()
   {
      PSTextValue copy = null;

      copy = (PSTextValue)super.clone();

      return copy;
   }

   /** @see IPSFieldValue */
   public boolean equals(Object obj)
   {
      if(obj == null || !(getClass().isInstance(obj)))
         return false;

      PSTextValue comp = (PSTextValue) obj;
      if (!compare(m_value, comp.m_value))
         return false;

      return true;
   }

   /** @see IPSFieldValue */
   public int hashCode()
   {
      int hash = 0;

      // super is abtract, don't call
      hash += hashBuilder(m_value);

      return hash;
   }

   /**
    * Implements the interface.  Gets the text this value contains.
    *
    * @return The value as a <code>String</code>, may be empty,
    * never <code>null</code>.
    */
   public Object getValue()
   {
      return m_value;
   }

   /**
    * The value of this class, never <code>null</code>, may be empty.
    */
   private String m_value = "";
}
