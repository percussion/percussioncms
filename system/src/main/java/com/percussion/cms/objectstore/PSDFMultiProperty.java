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
public class PSDFMultiProperty extends PSMultiValuedProperty
{          
   /**
    * Required ctor to be contained within a {@link
    * com.percussion.cms.objectstore.PSDbComponentCollection}
    */
   public PSDFMultiProperty(Element src)
      throws PSUnknownNodeTypeException
   {      
      super(src);      
   }
   
   /**
    * Convienve ctor to specify property name.
    */
   public PSDFMultiProperty(String strName)      
   {      
      super(PSDFProperty.class, strName);      
   }

   /**
    * no args constructor
    */
   public PSDFMultiProperty()      
   {      
      super();      
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

      PSDFProperty prop = new PSDFProperty(name, value);
      return prop;
   }

   // public defines
   public static final String KEY_COL = "propertyId";
   public static final String KEY_COL_NAME = "propertyName";
   public static final String KEY_COL_VAL = "propertyValue";
   public static final String XML_NODE_NAME = "PSXDFMultiProperty";
}
