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
