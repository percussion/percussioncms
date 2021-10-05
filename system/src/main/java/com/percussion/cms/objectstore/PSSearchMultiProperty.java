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
 * {@link com.percussion.cms.objectstore.PSSearch}
 */
public class PSSearchMultiProperty extends PSMultiValuedProperty
{          
   /**
    * Required ctor to be contained within a {@link
    * com.percussion.cms.objectstore.PSDbComponentCollection}
    */
   public PSSearchMultiProperty(Element src)
      throws PSUnknownNodeTypeException
   {      
      super(src);      
   }
   
   /**
    * no args constructor for xstream
    */
	public PSSearchMultiProperty() {
	}
   
   /**
    * Convienve ctor to specify property name.
    */
   public PSSearchMultiProperty(String strName)      
   {      
      super(PSSProperty.class, strName);      
   }

   // see base class for description
   public String getNodeName()
   {
      return XML_NODE_NAME;
   }
   
   // see base class for description
   protected PSCmsProperty createProperty(String name, String value)
   {
      if (name == null)
         throw new IllegalArgumentException(
            "name must not be null");

      PSSProperty prop = new PSSProperty(name, value);
      return prop;
   }

   // public defines
   public static final String KEY_COL = "propertyId";
   public static final String KEY_COL_NAME = "propertyName";
   public static final String KEY_COL_VAL = "propertyValue";
   public static final String XML_NODE_NAME = "PSXSearchMultiProperty";
}
