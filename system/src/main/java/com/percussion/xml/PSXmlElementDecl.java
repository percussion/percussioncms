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
