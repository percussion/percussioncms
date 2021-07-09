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
package com.percussion.content;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

/**
 * Adds some convenience methods to the DOM node interface.
 */
public abstract class HTMLNode implements Node
{
   public HTMLNode()
   {
      m_value = new StringBuffer();
      m_children = new HTMLNodeList();
   }

   ///////////////////////// our stuff
   public void setParentNode(HTMLNode parent)
   {
      m_parent = parent;
   }

   public int getNumChildNodes()
   {
      return m_children.size();
   }

   public void setPreviousSibling(HTMLNode prevSibling)
   {
      m_prevSib = prevSibling;
   }

   public void setNextSibling(HTMLNode nextSibling)
   {
      m_nextSib = nextSibling;
   }

   public void setOwnerDocument(Document doc)
   {
      m_ownerDoc = doc;
   }

   public HTMLElement getFirstElementChild()
   {
      Node n = null;
      for (n = getFirstChild(); n != null; n = n.getNextSibling() )
      {
         if (n.getNodeType() == ELEMENT_NODE)
            break;
      }

      return (HTMLElement)n;
   }

   ////////////////////////// DOM stuff
   public Document getOwnerDocument()
   {
      return m_ownerDoc;
   }

   public String getNodeName()
   {
      return m_name;
   }

   public String getNodeValue()
      throws DOMException
   {
      return m_value.toString();
   }

   public void setNodeValue(String nodeValue)
      throws DOMException
   {
      m_value.setLength(0);
      m_value.append(nodeValue);
   }

   // should be implemented for subclasses
   public abstract short getNodeType();

   public Node getParentNode()
   {
      return m_parent;
   }

   public NodeList getChildNodes()
   {
      return m_children;
   }

   public Node getFirstChild()
   {
      if (m_children.size() == 0)
         return null;
      return (HTMLNode)m_children.get(0);
   }

   public Node getLastChild()
   {
      final int len = m_children.size();
      if (len == 0)
         return null;
      return (HTMLNode)m_children.get(len-1);
   }

   public Node getPreviousSibling()
   {
      return m_prevSib;
   }

   public Node getNextSibling()
   {
      return m_nextSib;
   }

   // this will be overridden by HTMLElement
   public NamedNodeMap getAttributes()
   {
      return null;
   }

   public Node insertBefore(Node newChild, Node refChild)
      throws DOMException
   {
      if (newChild == null)
         throw new HTMLException((short)0, "Cannot insert null child under " + this);

      // System.out.println("Adding " + newChild + " to " + this
      //    + " before " + refChild);

      // stitch together the new child's previous sibling with
      // the new child's next sibling
      HTMLNode nChild = (HTMLNode)newChild;
      HTMLNode rChild = (HTMLNode)refChild;

      HTMLNode parentNode = (HTMLNode)nChild.getParentNode();
      HTMLNode prevSib = (HTMLNode)nChild.getPreviousSibling();
      HTMLNode nextSib = (HTMLNode)nChild.getNextSibling();

      if (prevSib != null)
         prevSib.setNextSibling(nextSib);

      if (nextSib != null)
         nextSib.setPreviousSibling(prevSib);

      if (parentNode != null)
      {
         parentNode.removeChild(nChild);
      }

      nChild.setParentNode(this);
      nChild.setPreviousSibling(null);
      nChild.setNextSibling(rChild);
      if (rChild != null)
      {
         HTMLNode rPrev = (HTMLNode)rChild.getPreviousSibling();
         if (rPrev != null)
         {
            rPrev.setNextSibling(nChild);
         }
         nChild.setPreviousSibling(rPrev);
      }
      else
      {
         // it's going to get added to the end of the list
         HTMLNode last = (HTMLNode)getLastChild();
         if (last != null)
         {
            last.setNextSibling(nChild);
         }
         nChild.setPreviousSibling(last);
      }

      int idx = m_children.size();
      if (refChild != null)
      {
         idx = m_children.indexOf(refChild);
      }

      if (idx < 0)
         throw new HTMLException(DOMException.NOT_FOUND_ERR, "refChild not found");

      m_children.add(idx, nChild);

      // System.out.println("Newly added node " + nChild + ", parent=" + this
      //    + ", prevsib=" + nChild.getPreviousSibling() + ", nextsib="
      //    + nChild.getNextSibling());

      return nChild;
   }

   public Node replaceChild(Node newChild, Node oldChild)
      throws DOMException
   {
      insertBefore(newChild, oldChild);
      return removeChild(oldChild);
   }

   public Node removeChild(Node oldChild)
      throws DOMException
   {
      // stitch
      HTMLNode oChild = (HTMLNode)oldChild;
      HTMLNode oPrev =  (HTMLNode)oChild.getPreviousSibling();
      HTMLNode oNext =  (HTMLNode)oChild.getNextSibling();

      if (oPrev != null)
         oPrev.setNextSibling(oNext);

      if (oNext != null)
         oNext.setPreviousSibling(oPrev);

      int idx = m_children.indexOf(oChild);
      if (idx < 0)
         throw new HTMLException(DOMException.NOT_FOUND_ERR, "oldChild not found");

      // orphaning the node is not technically necessary, but let's
      // be completely anal so we can help the garbage collector
      oChild.setParentNode(null);
      oChild.setPreviousSibling(null);
      oChild.setNextSibling(null);
      oChild.setOwnerDocument(null);

      return oChild;
   }

   public Node appendChild(Node newChild)
      throws DOMException
   {
      return insertBefore(newChild, null);
   }

   public boolean hasChildNodes()
   {
      return (0 != m_children.size());
   }

   public Node cloneNode(boolean deep)
   {
      // TODO: implement
      if (true)
         throw new RuntimeException("cloneNode not supported");
      return null;
   }

   public String toString()
   {
      if (m_name != null)
         return m_name;
      else return getClass().getName();
   }

   /**
    * This should be overridden by the nodes(like elements) which have to test
    * for attributes and return appropriate value. The default is to return
    * <code>false</code>.
    *
    * @return Always <code>false</code>.
    *
    * @see  org.w3c.dom.Node#hasAttributes()  hasAttributes
    **/
   public boolean hasAttributes()
   {
      return false;
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    *
    */
   public boolean isSupported(String feature, String Version)
   {
      // TODO: implement
      throw new UnsupportedOperationException(
         "Method 'isSupported' not supported");
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    */
   public String getLocalName()
   {
      // TODO: implement
      throw new RuntimeException("Method getLocalName not supported");
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    */
   public String getNamespaceURI()
   {
      // TODO: implement
      throw new RuntimeException("Method getNamespaceURI not supported");
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    */
   public String getPrefix()
   {
      // TODO: implement
      throw new RuntimeException("Method getPrefix not supported");
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    */
   public void normalize()
   {
      // TODO: implement
      throw new RuntimeException("Method normalize not supported");
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    */
   public void setPrefix(String p0) throws DOMException
   {
      // TODO: implement
      throw new RuntimeException("Method setPrefix not supported");
   }

   /**
    * Method introduced later in DOM level 2. Not implemented.
    */
   public boolean supports(String p0, String p1)
   {
      // TODO: implement
      throw new RuntimeException("Method supports not supported");
   }
   
   public short compareDocumentPosition(Node other) throws DOMException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public String getBaseURI()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Object getFeature(String feature, String version)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getTextContent() throws DOMException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Object getUserData(String key)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public boolean isDefaultNamespace(String namespaceURI)
   {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isEqualNode(Node arg)
   {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isSameNode(Node other)
   {
      // TODO Auto-generated method stub
      return false;
   }

   public String lookupNamespaceURI(String prefix)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String lookupPrefix(String namespaceURI)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setTextContent(String textContent) throws DOMException
   {
      // TODO Auto-generated method stub
      
   }

   public Object setUserData(String key, Object data, UserDataHandler handler)
   {
      // TODO Auto-generated method stub
      return null;
   }  

   protected String m_name;
   protected StringBuffer m_value;
   protected HTMLNode m_parent;
   protected HTMLNodeList m_children;
   protected HTMLNode m_prevSib;
   protected HTMLNode m_nextSib;
   protected Document m_ownerDoc; // TODO: class HTMLDocument   
}
