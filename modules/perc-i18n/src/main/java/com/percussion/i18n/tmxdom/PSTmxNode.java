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
package com.percussion.i18n.tmxdom;

import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Element;

/**
 * This abstract class provides default implementations for some generic methods
 * in the implementing interface.
 */
public abstract class PSTmxNode
   implements IPSTmxNode
{
   /*
    * Default implementation of the method defined in the interface.
    */
   public void merge(IPSTmxNode node)
      throws PSTmxDomException
   {
      throw new PSTmxDomException("mergeNotImplemented", "");
   }

   /*
    * Default implementation of the method defined in the interface.
    */
   public String toString()
      throws PSTmxDomException
   {
      return PSXmlDocumentBuilder.toString(m_DOMElement);
   }

   /*
    * Default implementation of the method defined in the interface.
    */
   public IPSTmxDocument getTMXDocument(){
      return m_PSTmxDocument;
   }

   /*
    * Default implementation of the method defined in the interface.
    */
   public IPSTmxNode getParent(){
      return m_Parent;
   }

   /*
    * Default implementation of the method defined in the interface.
    */
   public Element getDOMElement(){
      return m_DOMElement;
   }

   /**
    * The TMX document this node associated with. Every node must be associated
    * with a TMX document. Never <code>null</code> after the implementing class
    * object is  constructed.
    */
   protected IPSTmxDocument m_PSTmxDocument = null;

   /**
    * The XML DOM element this TMX node is associated with. Every TMX node wraps
    * the DOM counterpart. Never <code>null</code> after the implementing class
    * object is  constructed.
    */
   protected Element m_DOMElement = null;

   /**
    * The parent TMX node of this node. Not <code>null</code> after the
    * implementing class object is  constructed except if the node is the root
    * one.
    */
   protected IPSTmxNode m_Parent = null;
}
