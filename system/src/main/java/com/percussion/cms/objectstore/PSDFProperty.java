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

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Element;

/**
 * See base class for description. Represents a property of a given
 * {@link com.percussion.cms.objectstore.PSDisplayFormat}
 */
public class PSDFProperty extends PSCmsProperty
{         
	/**
	 * Empty constructor
	 */
	public PSDFProperty() {
	}
	   
   /**
    * Required ctor taking a element
    */
   public PSDFProperty(Element e)
      throws PSUnknownNodeTypeException
   {
      super(PSDFProperty.createKey(new String[] {}), "dummy");
      fromXml(e);
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
   public PSDFProperty(String strName, String strValue)
   {
      super(PSDFProperty.createKey(new String[] {}), 
         strName, strValue, null, KEYASSIGN_ALL);       
   }

   // see base class for description
   public static PSKey createKey(String [] values)
   {
      if (values == null || values.length == 0)                         
         return new PSKey(new String [] {
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
   public static final String KEY_COL_ID = "DISPLAYID";
   public static final String KEY_COL_NAME = "PROPERTYNAME";
   public static final String KEY_COL_VALUE = "PROPERTYVALUE";
   public static final int VALUE_LENGTH = 255;
   public static final int NAME_LENGTH = 128;
   public static final int DESCRIPTION_LENGTH = 255;
}
