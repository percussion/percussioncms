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

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class wraps the Application node in the Tree Table model and extends
 * the abstract class PSFUDAbstractNode. This is the root node of the model.
 * An application node can have two states, viz. normal remote absent.
 *
 * @see IPSFUDNode for node states.
 *
 */
public class PSFUDAppNode extends PSFUDAbstractNode
{
   /**
    * Constructor. Loads the snapshot document on construction.
    */
   public PSFUDAppNode(PSFUDApplication app) throws PSFUDNullElementException
   {
      super(null, app.getElement());
      m_Application = app;
   }

   /**
    * Returns the the string to be used to display this node in the first
    * colummn of the JTreeTable.
    *
    * @return string representation of the node. This gets displayed in the
    *         tree table.
    *
    */
   public String toString()
   {
      String value = MainFrame.getConfig().getServerAlias();
      if(m_Application.isOffline())
         value += " (offline)";

      return value.trim();
   }

   /**
    * Loads the children, caching the results. The application node shall
    * obtain only category nodes as its children.
    *
    * @return array of child nodes as an array Objects, can be empty
    *         <code>null</code> it does have category child nodes.
    *
    */
   public Object[] getChildren()
   {
      if (m_Children != null || null == m_Element)
      {
         return m_Children;
      }

      NodeList nl = m_Element.getElementsByTagName(
         PSFUDApplication.ELEM_CATEGORY);
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
               temp = elem.getAttribute(PSFUDApplication.ATTRIB_CATEGORYID);
               // empty categoryid is invalid, skip it
               if(null == temp || temp.trim().length() < 1)
                  continue;
               list.add(new PSFUDContentTypeNode(this, (Element)nl.item(i)));
            }
            catch(PSFUDNullElementException e)
            {
               //never happens
            }
         }
         m_Children = list.toArray();
         list = null;
      }
      return m_Children;
   }

   /**
    * Reference to the PSFUDApplication object. This is used to access required
    * methods, if any. Can never be <code>null</code>.
    *
    */
   private PSFUDApplication m_Application = null;
}



