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
package com.percussion.design.objectstore;

import org.w3c.dom.Element;

import java.util.List;

/**
 * The replacement values used for content item data information.
 */
public class PSContentItemData extends PSNamedReplacementValue
{
   /**
    * Constructs a new content item data from its XML representation.
    * 
    * @param source the XML element node to construct this object from, not
    *    <code>null</code>.
    * @param parent the Java object which is the parent of this object, may be
    *    <code>null</code>.
    * @param parentComponents the parent objects of this object, may be 
    *    <code>null</code> or empty.
    * @throws IllegalArgumentException if source is <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML representation is not
    *    in the expected format.
    */
   public PSContentItemData(Element source, IPSDocument parent, 
      List parentComponents) throws PSUnknownNodeTypeException
   {
      super(source, parent, parentComponents);
   }

   /**
    * Constructs a new content item data replacement value for the supplied
    * field name. 
    * 
    * @param fieldName, not <code>null</code>.
    * 
    * @throws IllegalArgumentException if the supplied field name is not 
    * supported.
    */
   public PSContentItemData(String fieldName)
   {
      super(fieldName);
   }

   /**
    * Gets the type of replacement value this object represents.
    * 
    * @return {@link #VALUE_TYPE}
    */
   public String getValueType()
   {
      return VALUE_TYPE;
   }

   /**
    * Gets the text which can be displayed to represent this value.
    * 
    * @return "PSXContentItemData/" + <code>getName()</code>
    */
   public String getValueDisplayText()
   {
      return XML_NODE_NAME + "/" + getName();
   }

   // see base class for description
   public String getNodeName()
   {
      return XML_NODE_NAME;
   }

   // see base class for description
   public int getErrorCode()
   {
      return IPSObjectStoreErrors.RELATIONSHIP_PROPERTY_NAME_EMPTY;
   }

   /**
    * The value type associated with instances of this class.
    */
   public static final String VALUE_TYPE = "ContentItemData";
   
   /**
    * The node name used in XML representations.
    */
   public static final String XML_NODE_NAME = "PSXContentItemData";
}
