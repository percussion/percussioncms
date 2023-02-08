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
package com.percussion.integration;

/**
 * This class holds the information used to create the return HTML data from
 * a <code>PSSearch</code> object. It is just a way of indicating what label
 * a user would want to show as the column header for the specified field,
 * @see PSSearch for more information.
 */

public class PSColumn
{
   /**
    * Construct a new column object.
    * 
    * @param label the label to describe the column data, must not be <code>
    *    null</code>, may be empty
    * @param fieldName the Rhythmyx field name to be used to associate the 
    *    column name with, must not be <code>null</code>, or empty
    */
   public PSColumn(String label, String fieldName)
   {
      if (label == null)
         throw new IllegalArgumentException("label may not be null");
      if (fieldName == null || fieldName.trim().length() == 0)
         throw new IllegalArgumentException("fieldName may not be null or empty");
      m_label = label;
      m_fieldName = fieldName;
   }
   
   /**
    * Returns the label of this column, used for the table header.
    * 
    * @return the label, will not be <code>null</code> or empty
    */
   public String getLabel()
   {
      return m_label;
   }
   
   /**
    * Returns the Rhythmyx field name of this column, used for identifying the
    * field within the <code>PSSearch</code> result.
    * 
    * @return the field name, will not be <code>null</code> or empty
    */
   public String getFieldName()
   {
      return m_fieldName;
   }
 
   /**
    * Storage for the column label, set in the ctor, never <code>null</code>, 
    * empty, or modified after that.
    */  
   private String m_label = null;

   /**
    * Storage for the internal Rhythmyx field name, set in the ctor, never 
    * <code>null</code>, empty, or modified after that.
    */  
   private String m_fieldName = null;
}
