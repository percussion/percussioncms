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
package com.percussion.services.catalog;

import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.PSInvalidXmlException;

import java.io.IOException;

import org.xml.sax.SAXException;

/**
 * A single item that can be stored into or retrieved from a service. An item is
 * capable of serializing and restoring itself into an XML document. The type is
 * responsible for storing enough information to restore itself, the actual
 * information about the type of the serialized document must be stored by the
 * requestor.
 * <P>
 * Example: If MSM wishes to store an assembler, it can request a list of
 * assembler ids from the assembly system. It can then store a specific
 * assembler, but it must store with it the appropriate type id. On restoration,
 * the type id is used to determine what service will restore the data.
 * 
 * @author dougrand
 */
public interface IPSCatalogItem extends IPSCatalogIdentifier
{
   /**
    * Serialize the information in the item into XML format. The implementation
    * of this is up to the class in question. It is acceptable to use the
    * JavaBean APIs to accomplish this.
    * 
    * @return a string containing the XML that represents the item. This
    *         information must contain the items guid, but is not required to
    *         hold the type information.
    * @throws IOException if there is a problem serializing the object
    * @throws SAXException if there is an issue converting the object to XML
    */
   String toXML() throws IOException, SAXException;

   /**
    * Restore the given item from an XML description. The implementation of this
    * is up to the class in question. It is acceptable to use the JavaBean APIs
    * to accomplish this.
    * 
    * @param xmlsource the string that represents this item, never
    *           <code>null</code> or empty
    * @throws SAXException if there is a problem parsing the xml source
    * @throws IOException if there is a problem reading the xml source
    * @throws PSInvalidXmlException if the name of the supplied element
    *    is incorrect or any of its attributes or children are required but
    *    missing or incorrect
    */
   void fromXML(String xmlsource) throws IOException, SAXException,
      PSInvalidXmlException;


   /**
    * Set a global identifier. Useful for setting the initial identifier for
    * objects being newly created.
    * 
    * @param newguid the globally unique id, never <code>null</code>. See
    *           {@link IPSGuid} for more information.
    * @throws IllegalStateException if the object already has an identifier
    *            assigned
    */
   void setGUID(IPSGuid newguid) throws IllegalStateException;

}
