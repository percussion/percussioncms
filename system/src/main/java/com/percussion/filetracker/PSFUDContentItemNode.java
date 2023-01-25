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

package com.percussion.filetracker;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 * This class wraps the content item node in the application and extends
 * the abstract class PSFUDAbstractNode. A Content Item node can have two
 * states, viz. normal remote absent.
 *
 * @see IPSFUDNode for node states.
 *
 */
public class PSFUDContentItemNode extends PSFUDAbstractNode
{

   /**
    * Constructor takes the parent node and current element that is encapsulated
    * by this class. Validation of these parameters is handled by the base class.
    *
    * @param parent node as IPSFUDNode
    *
    * @param content item element in the XML document as DOM Element
    *
    * @throws PSFUDNullElementException that is thrown by the base class
    *
    */
   public PSFUDContentItemNode(IPSFUDNode parent, Element elem)
      throws PSFUDNullElementException
   {
      super(parent, elem);
   }

   /**
    * return the children as an array of PSFUDFileNode objects.
    *
    * @return  child nodes as an array Objects, can be <code>null</code> though
    *          a rare possibility.
    *
    */
   public Object[] getChildren()
   {
      if (m_Children != null || null == m_Element)
      {
         return m_Children;
      }

      NodeList nl = m_Element.getElementsByTagName(PSFUDApplication.ELEM_FILE);
      if(null != nl)
      {
         ArrayList list = new ArrayList();
         Element elem = null;
         int index = 0;
         String temp = null;
         for(int i = 0; i < nl.getLength(); i++)
         {
            try
            {
               elem = (Element)nl.item(i);
               temp = elem.getAttribute(PSFUDApplication.ATTRIB_NAME);
               // empty file name is invalid, skip it
               if(null == temp || temp.trim().length() < 1)
                  continue;
               list.add(new PSFUDFileNode(this, elem));
            }
            catch(PSFUDNullElementException e)
            {
               //never happens
            }
         }
         m_Children = list.toArray();
         list=null;
      }
      return m_Children;
   }

   /**
    * Returns the the string to be used to display this leaf in the tree table.
    *
    * @return  string representation of the node for displaying in the
    *          application, never <code>null</code> or empty.
    *
    */
   public String toString()
   {
      String value = m_Element.getAttribute(
                     PSFUDApplication.ATTRIB_TITLE).trim() + " (ID:" +
                     m_Element.getAttribute(
                     PSFUDApplication.ATTRIB_CONTENTID).trim() + ")";
      return value;
   }
}



