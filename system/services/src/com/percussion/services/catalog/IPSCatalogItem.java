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
package com.percussion.services.catalog;

import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.PSInvalidXmlException;
import org.xml.sax.SAXException;

import java.io.IOException;

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
