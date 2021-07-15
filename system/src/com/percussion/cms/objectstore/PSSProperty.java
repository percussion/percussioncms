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

import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import org.w3c.dom.Element;

/**
 * See base class for description. Represents a property of a given
 * {@link com.percussion.cms.objectstore.PSDisplayFormat}
 */
public class PSSProperty extends PSCmsProperty
{        
   /**
    * Required ctor taking a element
    */
   public PSSProperty(Element e)
      throws PSUnknownNodeTypeException
   {
      super(PSSProperty.createKey(new String[] {}), "dummy");
      fromXml(e);
   }
   
   /**
    * Required ctor taking a element
    */
   public PSSProperty()
   {
   }
   
   
   /**
    * convience Ctor that takes in name, value
    * 
    * @param strName of property. Never <code>null</code> or
    *    empty.
    * 
    * @param strValue may be <code>null</code> to specify empty.
    * 
    */
   public PSSProperty(String strName, String strValue)
   {
      super(PSSProperty.createKey(new String[] {}), 
      strName, strValue, null, KEYASSIGN_ALL);       
   }

   // see base class for description
   public static PSKey createKey(String [] values)
   {
      if (values == null || values.length == 0)                         
         return new PSKey(new String [] 
            {
                                           KEY_COL_NAME, 
                                           KEY_COL_VALUE,
                                           KEY_COL_ID
                                        });
      
      return new PSKey(new String [] 
         {               
            KEY_COL_NAME, 
            KEY_COL_VALUE,
            KEY_COL_ID
         }, 
         values, true);      
   }     
   
   //see base class for description
   protected String[] getKeyPartValues(IPSKeyGenerator gen)
   {
      return new String[] {getName(), getValue()};
   }

   /**
    * Because the value is used in key assignment, it cannot be reset.
    *
    * @param value Unused
    *
    * @throws UnsupportedOperationException Always.
    */
   public void setValue(String value)
   {
      throw new UnsupportedOperationException(
         "The value is immutable in this class.");
   }

   // public static define 
   public static final String KEY_COL_ID = "SEARCHID";
   public static final String KEY_COL_NAME = "PROPERTYNAME";
   public static final String KEY_COL_VALUE = "PROPERTYVALUE";
   
   /**
    * The maximum length for a property value.
    */
   public static final int VALUE_LENGTH = 100;
   
   /**
    * The maximum length for a property name.
    */
   public static final int NAME_LENGTH = 50;
   
   /**
    * The maximum length for a property description.
    */
   public static final int DESCRIPTION_LENGTH = 255;
}
