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



