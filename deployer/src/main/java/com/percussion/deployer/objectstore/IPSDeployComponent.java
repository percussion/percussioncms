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

package com.percussion.deployer.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The PSDeployComponent interface defines required methods for all component
 * level object store objects.
 */
public interface IPSDeployComponent extends Cloneable
{
   /**
    * This method is called to create an XML element node with the
    * appropriate format for the given object. An element node may contain a
    * hierarchical structure, including child objects. The element node can
    * also be a child of another element node.
    * 
    * @param doc The document to use to create the element, may not be 
    * <code>null</code>.
    * 
    * @return the newly created XML element node, never <code>null</code>
    * 
    * @throws IllegalArgumentException if <code>doc</code> is <code>null</code>.
    */
   public Element toXml(Document doc);
   
   /**
    * This method is called to populate an object from its XML representation.
    * An element node may contain a hierarchical structure, including child 
    * objects. The element node can also be a child of another element node.
    * 
    * @param sourceNode the XML element node to populate from not 
    * <code>null</code>.
    * 
    * @throws PSUnknownNodeTypeException if the XML element node does not 
    * represent a type supported by the class.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException;
   
   /**
    * Creates a new instance of this object, performing a shallow copy of all 
    * members.
    * 
    * @param obj The object from which to copy values.
    * 
    * @throws IllegalArgumentException if the supplied object is 
    * <code>null</code> or of the wrong type.
    */
   public void copyFrom(IPSDeployComponent obj);
   
   /**
    * Returns a hash code value for the object. See 
    * {@link java.lang.Object#hashCode() Object.hashCode()} for more info.
    */
   public int hashCode();
   
   /**
    * Determines if this object is equal to another.  See 
    * {@link java.lang.Object#equals(Object) Object.equals()} for more info.
    */
   public boolean equals(Object obj);

}
