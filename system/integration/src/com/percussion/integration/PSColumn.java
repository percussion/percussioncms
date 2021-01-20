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