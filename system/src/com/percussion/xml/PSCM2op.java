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
