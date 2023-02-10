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
