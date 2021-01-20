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
package com.percussion.extension;

import org.w3c.dom.Element;

/**
 * The IPSExtensionDefFactory interface defines methods for serializing and
 * deserializing whole extension defs. Each class which implements
 * IPSExtensionDef should have a corresponding class which implements
 * IPSExtensionDefFactory.
 */
public interface IPSExtensionDefFactory
{
   /**
    * Convenience method that calls {@link #toXml(Element, IPSExtensionDef, 
    * boolean) toXml(root, def, false)}.
    */
   public Element toXml(Element root, IPSExtensionDef def);

   /**
    * Serializes the content of the given extension def into a newly created
    * element under the given root element.
    *
    * @param root The element under which the extension def element will
    *    be created. Must not be <CODE>null</CODE>.
    * @param def The extension definition to be serialized. Must not be
    *    <CODE>null</CODE>.
    * @param excludeMethods <code>true</code> to exclude the extension methods
    *    from serialization, <code>false</code> to include.
    * @return The newly created element under <CODE>root</CODE>. Never
    *    <CODE>null</CODE>.
    */
   public Element toXml(Element root, IPSExtensionDef def, 
      boolean excludeMethods);

   /**
    * Creates a new extension def instance of the proper derived type from
    * the given XML element, which is analogous to the serialized content
    * created from <CODE>toXml</CODE>.
    *
    * @param defElement The top-level extension def element. Must not be
    *    <CODE>null</CODE>.
    * @return A new IPSExtensionDef instance of the proper derived type,
    *    representing the serialized content. Never <CODE>null</CODE>.
    * @throws PSExtensionException If the element content is missing or
    *    corrupted.
    */
   public IPSExtensionDef fromXml(Element defElement)
      throws PSExtensionException;
}
