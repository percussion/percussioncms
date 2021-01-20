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

package com.percussion.filetracker;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * This is an abstract class implementing some methods in the interface
 * IPSFUDNode that are common for the derived classes . Methods getChildren()
 * and toString() must me overridden by the derived classes.
 */
public abstract class PSFUDAbstractNode
   implements IPSFUDNode
{
   /**
    * Constructor.
    *
    * @param parent node as IPSFUDNode, can be <code>null</code>
    *
    * @param content item element in the XML document as DOM Element, cannot be
    * <code>null</code>
    *
    * @throws PSFUDNullElementException if element is <code>null</code>.
    *
    */
   public PSFUDAbstractNode(IPSFUDNode parent, Element elem)
      throws PSFUDNullElementException
   {
      if(null == elem)
      {
         throw new PSFUDNullElementException(MainFrame.getRes().
                                    getString("errorNullElement"));
      }
      m_Parent = parent;
      m_Element = elem;
      
      m_ElementStatus = PSFUDDocMerger.getChildElement(m_Element,
                                                IPSFUDNode.ELEM_STATUS);
      if(null == m_ElementStatus)
         m_ElementStatus = PSFUDDocMerger.createChildElement(m_Element,
                                                IPSFUDNode.ELEM_STATUS);
   }

   /**
    * Returns the element this class encapsulates
    *
    * @return encapsulated DOM Element never <code>null</code>.
    *
    */
   public Element getElement()
   {
      return m_Element;
   }

   /**
    * returns status code
    *
    * @status code as int
    *
    * @see IPSFUDNode for status code values
    *
    */
   public int getStatusCode()
   {
      if(null != m_Parent && !m_Parent.isRemoteExists())
         return STATUS_CODE_ABSENT;

      if(null  == m_ElementStatus)
         return 0;

      String tmp = m_ElementStatus.getAttribute(ATTRIB_CODE);
      try
      {
         return Integer.parseInt(tmp);
      }
      catch(Exception e)
      {
         return 0;
      }
   }

   /**
    * returns status text, potentially used as tool tip text.
    *
    * @return status text as String may be empty but nevel <code>null</code>.
    *
    */
   public String getStatusText()
   {
      if(null  == m_ElementStatus)
         return "";

      Node node = m_ElementStatus.getFirstChild();
      if(null == node || Node.TEXT_NODE != node.getNodeType())
         return "";

      return ((Text)node).getData();
   }

   /**
    * returns true if remote file exists
    *
    *  @return true if remote file exists else false
    */
   public boolean isRemoteExists()
   {
      if(STATUS_CODE_ABSENT != getStatusCode())
         return true;

      return false;
   }

   /**
    * Parent node of this node. Shall not be <code>null</code> except for the
    * root node.
    */
   protected IPSFUDNode m_Parent = null;

   /**
    * Node element that is encapsulated by this node , nevel be
    * <code>null</code> after the this class is instantiated.
    */
   protected Element m_Element = null;

   /**
    * Reference to status element that is child of current element.
    * Stored since this required very frequently hence avoiding walking through
    * evrytime. Never be <code>null</code> after the this class is instantiated.
    */
   protected Element m_ElementStatus = null;

   /**
    * Array of child nodes for this class object is used for caching to improve
    * performance. This object is obtained only the derived class objects. Null
    * for leaf nodes. 
    */
   protected Object[] m_Children = null;
}


