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
package com.percussion.cx;



import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.cx.error.PSContentExplorerException;

/**
 * @todo is there a base Iface for other objects in the cx?
 */
public interface IPSClientObjects
{

   /**
    * This method is called to populate an object from its XML representation.
    * <p>
    * @param sourceNode   the XML element node from which to populate.  Must not
    * be <code>null</code>.
    * @throws PSContentExplorerException if the XML element node does not
    * represent a type supported by this class.
    * @throws com.percussion.error.PSContentExplorerException 
    */
   public void fromXml(Element sourceNode) throws  PSContentExplorerException;

   /**
    * This method is called to create an XML element node with the
    * appropriate format for this object. An element node may contain a
    * hierarchical structure, including child objects. The element node can
    * also be a child of another element node.
    *
    * @param doc - the document from which the element node will be created.
    * Must not be <code>null</code>.
    * @return - the newly created XML element node.  Never <code>null</code>.
    */
   public Element toXml(Document doc);

}