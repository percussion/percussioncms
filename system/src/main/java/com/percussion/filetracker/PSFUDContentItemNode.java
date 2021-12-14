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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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



