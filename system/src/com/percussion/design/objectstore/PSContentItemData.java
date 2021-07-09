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
package com.percussion.design.objectstore;

import java.util.ArrayList;

import org.w3c.dom.Element;

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
      ArrayList parentComponents) throws PSUnknownNodeTypeException
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
