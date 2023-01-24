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
package com.percussion.cx;


import com.percussion.cx.error.PSContentExplorerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
