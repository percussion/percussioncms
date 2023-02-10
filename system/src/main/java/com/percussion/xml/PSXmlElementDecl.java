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

package com.percussion.xml;

import org.apache.xerces.impl.dtd.XMLElementDecl;

/**
 * This class encapsulates a DTD element declaration.
 */
public class PSXmlElementDecl extends XMLElementDecl
{
   /**
    * Default constructor
    */
   public PSXmlElementDecl()
   {
      super();
   }

   /**
    * constructor
    * @param xmlElemDecl the Element Declaration object to encapsulate,
    * may not be <code>null</code>
    * @throw IllegalArgumentException if xmlElemDecl is <code>null</code>
    */
   public PSXmlElementDecl(XMLElementDecl xmlElemDecl)
   {
      super();
      if (xmlElemDecl == null)
         throw new IllegalArgumentException("xmlElemDecl may not be null");
      setValues(xmlElemDecl.name, xmlElemDecl.scope, xmlElemDecl.type,
         xmlElemDecl.contentModelValidator, xmlElemDecl.simpleType);
   }

   /**
    * Returns the name of the encapsulated element
    * @return the name of the encapsulated element, never <code>null</code>
    * or empty
    */
   public String getName()
   {
      return name.rawname;
   }

   /**
    * Returns the type of element
    * @return the type of element, should be one of these values:
    * org.apache.xerces.impl.dtd.XMLElementDecl.TYPE_ANY or
    * org.apache.xerces.impl.dtd.XMLElementDecl.TYPE_EMPTY or
    * org.apache.xerces.impl.dtd.XMLElementDecl.TYPE_MIXED or
    * org.apache.xerces.impl.dtd.XMLElementDecl.TYPE_CHILDREN or
    * org.apache.xerces.impl.dtd.XMLElementDecl.TYPE_SIMPLE
    * @see org.apache.xerces.impl.dtd.XMLElementDecl
    */
   public int getContentType()
   {
      return type;
   }
}
