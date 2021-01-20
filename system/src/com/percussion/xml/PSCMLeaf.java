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

package com.percussion.xml;

import org.apache.xerces.impl.dtd.models.CMLeaf;
import org.apache.xerces.xni.QName;

/**
 * PSCMLeaf provides content model support for content model nodes that are
 * leaf node; in other words, nodes which do not have associated language
 * primitives and are not model groups. For example, Leaf nodes that represent
 * parsed character (#pcdata).
 */
public class PSCMLeaf extends CMLeaf
{
   /**
    * Constructs a content model leaf.
    * @param element contains the name of the leaf content model node
    * (such as #PCDATA or elementX). May not be <code>null</code>.
    * @param position if its -1, that means its an epsilon node. Zero and
    * greater are non-epsilon positions.
    * For details, please see the Xerces javadoc for
    * org.apache.xerces.framework.XMLContentSpecNode
    */
   public PSCMLeaf(QName element, int position)
   {
      super(element, position);
      m_element.setValues(element);
      m_position = position;
   }

   /**
    * Constructs a content model leaf.
    * @param element contains the name of the leaf content model node
    * (such as #PCDATA or elementX).  May not be <code>null</code>.
    */
   public PSCMLeaf(QName element)
   {
      super(element);
      m_element.setValues(element);
   }

   /**
    * Returns the name of the leaf content model node
    * (such as #PCDATA or elementX)
    * @return the name of the left content model node, never <code>null</code>
    * or empty
    */
   public String getName()
   {
      return m_element.rawname;
   }

   /**
    * This is the element that this leaf represents.
    */
   protected QName m_element = new QName();

   /**
    * If its -1, that means its an
    * epsilon node. Zero and greater are non-epsilon positions.
    */
   protected int m_position = -1;
}
