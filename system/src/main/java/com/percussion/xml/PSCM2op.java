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

import org.apache.xerces.impl.dtd.models.CMBinOp;
import org.apache.xerces.impl.dtd.models.CMNode;

/**
 * PSCM2op provides content model support for content model nodes that contain
 * the "|", or "," language primitives.
 */
public class PSCM2op extends CMBinOp
{
   /**
    * Constructor
    * @param type the language primitive associated with this content model
    * node. Should be one of these values:
    * org.apache.xerces.impl.dtd.XMLContentSpec.CONTENTSPECNODE_CHOICE or
    * org.apache.xerces.impl.dtd.XMLContentSpec.CONTENTSPECNODE_SEQ
    *
    * @param leftNode the content model node prior to the type separator, or
    * <code>null<code> if left will be set later.
    * @param rightNode the content model node after the type separator, or
    * <code>null<code> if right will be set later.
    */
   public PSCM2op(int type, CMNode leftNode, CMNode rightNode)
   {
      super(type, leftNode, rightNode);
      m_leftChild = leftNode;
      m_rightChild = rightNode;
   }

   /**
    * Returns the language primitive associated with this content model node.
    * @return one of these values:
    * org.apache.xerces.impl.dtd.XMLContentSpec.CONTENTSPECNODE_CHOICE or
    * org.apache.xerces.impl.dtd.XMLContentSpec.CONTENTSPECNODE_SEQ
    */
   public int getType()
   {
      return super.type();
   }

   /**
    * Returns the content model node prior to the separator type.
    * @return the content model node prior to the separator type,
    * or <code>null</code> if no node currently exists.
    */
   public CMNode getLeft()
   {
      return m_leftChild;
   }

   /**
    * Returns the content model node after the separator type.
    * @return the content model node after the separator type,
    * or <code>null</code> if no node currently exists.
    */
   public CMNode getRight()
   {
      return m_rightChild;
   }

   /**
    * These are the references to the two nodes that are on either
    * side of this binary operation.
    */
   protected CMNode m_leftChild = null;
   protected CMNode m_rightChild = null;

}
