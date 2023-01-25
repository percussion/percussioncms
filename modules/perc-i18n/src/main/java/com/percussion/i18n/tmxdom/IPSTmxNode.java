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

import org.w3c.dom.Element;

/**
* This is the interface every node of the TMX Document must implement. Defines
* common methods to all TMX nodes. Also defines the Node type constants and Node
* name constants.
*/
public interface IPSTmxNode
{
   /**
    * Merge the supplied TMX node with this applying the merge rules for this
    * particular type of node. Merge rules are specified in the interface
    * {@link IPSTmxMergeConfig}.
    * @param node must not be <code>null</code>
    * @throws PSTmxDomException
    */
   void merge(IPSTmxNode node)
      throws PSTmxDomException;

   /**
    * String representation of the TMX node which is the same as the string
    * representation of the underlying XML DOM node.
    * @return XML string, never <code>null</code>
    * @throws PSTmxDomException if string conversion fails for some reason.
    */
   String toString()
      throws PSTmxDomException;

   /**
    * Method to return the TMX Document interface object this node is
    * associated.
    * @return TMX Document interface object, never <code>null</code>.
    * @see IPSTmxDocument
    */
   IPSTmxDocument getTMXDocument();

   /**
    * Method to get the immediate paraent TMX node of this node.
    * @return parent TMX node, never <code>null</code> except when the node is
    * {@link IPSTmxDocument}.
    */
   IPSTmxNode getParent();

   /**
    * Access method for the XML DOM element this TMX wraps. Never modify the DOM
    * element directly which can leave the  TMX node in a vague state. Use only
    * TMX nodes to make any changes.
    * @return DOM element, never <code>null</code>.
    */
   Element getDOMElement();

   /**
    * Node type value for TMX document
    */
   static final int TMXDOCUMENT = 0;

   /**
    * Node type value for TMX root.
    */
   static final int TMXROOT = 1;

   /**
    * Node type value for TMX header.
    */
   static final int TMXHEADER = 2;

   /**
    * Node type value for TMX body
    */
   static final int TMXBODY = 3;

   /**
    * Node type value for TMX note
    */
   static final int TMXNOTE = 4;

   /**
    * Node type value for TMX property.
    */
   static final int TMXPROPERTY = 5;

   /**
    * Node type value for TMX translation unit.
    */
   static final int TMXTRANSLATIONUNIT = 6;

   /**
    * Node type value for TMX translation unit variant.
    */
   static final int TMXTRANSLATIONUNITVARIANT = 7;

   /**
    * Node type value for TMX Segment
    */
   static final int TMXSEGMENT = 8;

   /**
    * Array of string representing the element names for each node type. These
    * correspond to the node type values above.
    * @see IPSTmxDtdConstants
    */
   static final String[] NODENAMEMAP =
      {
         "", //TMX Document has no node name
         IPSTmxDtdConstants.ELEM_TMX_ROOT,
         IPSTmxDtdConstants.ELEM_HEADER,
         IPSTmxDtdConstants.ELEM_BODY,
         IPSTmxDtdConstants.ELEM_NOTE,
         IPSTmxDtdConstants.ELEM_PROP,
         IPSTmxDtdConstants.ELEM_TU,
         IPSTmxDtdConstants.ELEM_TUV,
         IPSTmxDtdConstants.ELEM_SEG
      };
   }
