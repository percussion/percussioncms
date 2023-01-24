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
