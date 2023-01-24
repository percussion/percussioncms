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
package com.percussion.cx.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The IPSComponent interface defines required methods for all component
 * level object store objects. 
 */
public interface IPSComponent 
{
   /**
    * This method is called to create an XML element node with the
    * appropriate format for the given object. An element node may contain a
    * hierarchical structure, including child objects. The element node can
    * also be a child of another element node.
    *
    * @param doc the document to use to create the element, may not be <code>
    * null</code>
    * 
    * @return the newly created XML element node, may be <code>null</code> if
    * the component does not have any data to represent.
    * 
    * @throws IllegalArgumentException if the doc is <code>null</code>.
    */
   public Element toXml(Document doc);

   /**
    * This method is called to populate an object from its XML representation.
    * An element node may contain a hierarchical structure, including child 
    * objects. The element node can also be a child of another element node.
    * <p>
    * Each component should add itself to <code>parentComponents</code> before
    * constructing its child components, and should restore the original
    * <code>parentComponents</code> before returning.
    *
    * @param sourceNode   the XML element node to populate from, not <code>null
    * </code>.
    * 
    * @throws IllegalArgumentException if the sourceNode is <code>null</code>
    * @throws PSUnknownNodeTypeException if the XML element node does not 
    * represent a type supported by the class.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException;
      
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
