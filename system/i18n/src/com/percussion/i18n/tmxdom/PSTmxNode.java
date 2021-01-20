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
