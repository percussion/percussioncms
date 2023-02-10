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


