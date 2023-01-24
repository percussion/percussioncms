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
