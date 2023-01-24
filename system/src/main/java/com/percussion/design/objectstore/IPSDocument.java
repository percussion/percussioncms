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

import org.w3c.dom.Document;


/**
 * The IPSDocument interface defines required methods for all document level
 * object store objects. There are currently two document level object store
 * objects, the server and the application. All other object store objects
 * are components, and as such are derived from IPSComponent.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSDocument {
   /**
    * This method is called to create an XML document with the appropriate
    * format for the given object.
    *
    * @return    the newly created XML document
    */
   public abstract Document toXml();

   /**
    * This method is called to populate an object from an XML
    * document.
    *
    * @exception PSUnknownDocTypeException   if the XML document does not
    *                                        represent a type supported
    *                                        by the class.
    */
   public abstract void fromXml(Document sourceDoc)
      throws PSUnknownDocTypeException, PSUnknownNodeTypeException;
}
