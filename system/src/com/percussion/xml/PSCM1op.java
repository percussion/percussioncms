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

import org.apache.xerces.impl.dtd.models.CMNode;
import org.apache.xerces.impl.dtd.models.CMUniOp;

/**
 * PSCM1op provides content model support for content model nodes that contain
 * the "*", "?", and "+" language primitives.
 */
public class PSCM1op extends CMUniOp
{
   /**
    * Constructor
    * @param type the language primitive associated with this content model
    * node. Should be one of these values:
    * org.apache.xerces.impl.dtd.XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE or
    * org.apache.xerces.impl.dtd.XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE or
    * org.apache.xerces.impl.dtd.XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE
    *
    * @param childNode The content model node to associate with type, or
    * <code>null</code> if node will be set later.
    */
   public PSCM1op(int type, CMNode childNode)
   {
      super(type, childNode);
      m_child = childNode;
   }

   /**
    * Returns the language primitive associated with this content model node.
    * @return one of these values
    * org.apache.xerces.impl.dtd.XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE or
    * org.apache.xerces.impl.dtd.XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE or
    * org.apache.xerces.impl.dtd.XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE
    */
   public int getType()
   {
      return super.type();
   }

   /**
    * Returns the content model node associated with type.
    * @return the content model node associated with type, or <code>null</code>
    * if no node currently exists.
    */
   public CMNode getNode()
   {
      return m_child;
   }

   /**
    * This is the reference to the one child that we have for this
    * unary operation. May be <code>null</code>. Never modified once it is set.
    */
    protected CMNode m_child = null;

}
