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
