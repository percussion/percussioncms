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

import org.w3c.dom.Element;

import java.util.ArrayList;

/**
 * The PSXmlField class is used to define a replacement value is a 
 * XML field value.
 *
 * @see        IPSReplacementValue
 *
 * @author       Tas Giakouminakis
 * @version  1.0
 * @since       1.0
 */
public class PSXmlField extends PSNamedReplacementValue
{
   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode the XML element node to construct this object from
    * @param parentDoc the Java object which is the parent of this object
    * @param parentComponents   the parent objects of this object
    *
    * @throws PSUnknownNodeTypeException if the XML element node is not of the
    *   appropriate type
    */
   public PSXmlField(Element sourceNode, IPSDocument parentDoc,
                     ArrayList parentComponents)
         throws PSUnknownNodeTypeException
   {
      super( sourceNode, parentDoc, parentComponents );
   }


   /**
    * Constructs a XML field replacement value.
    *
    * @param name the name of the XML field
    */
   public PSXmlField(String name)
   {
      super( name );
   }


   // see base class for description
   protected int getErrorCode()
   {
      return IPSObjectStoreErrors.XML_FIELD_NAME_EMPTY;
   }


   // see base class for description
   protected String getNodeName()
   {
      return ms_NodeType;
   }


   /**
    * Gets the type of replacement value this object represents.
    * @return {@link #VALUE_TYPE}
    */
   public String getValueType()
   {
      return VALUE_TYPE;
   }


   /**
    * The value type associated with this instances of this class.
    */
   public static final String VALUE_TYPE = "XmlField";

   // package access on this so they may reference each other in fromXml 
   static final String ms_NodeType = "PSXXmlField";
}

