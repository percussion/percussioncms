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
package com.percussion.controls.contenteditor.checkboxtree;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * The root node of the tree. This node has no checkbox, only a label. It 
 * provides all functionality to load the entire tree from an xml document.
 */
public class PSCheckboxTreeRootNode extends DefaultMutableTreeNode implements
   MutableTreeNode
{
   /**
    * Load the xml document and populate the tree. The expected xml format is:
    * &lt;!ELEMENT Tree (Node*)&gt;
    * &lt;!ATTLIST Tree
    *    label CDATA #REQUIRED
    * &gt;
    * &lt;!ELEMENT Node (Node*)&gt;
    * &lt;!ATTLIST Node
    *    id CDATA #REQUIRED
    *    label CDATA #REQUIRED
    *    selectable (yes | no) "no"
    * &gt;
    * 
    * @param doc the xml document which contains the tree information, 
    *    not <code>null</code>.
    */
   public void loadDocument(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc cannot be null");
      
      Element rootNode = doc.getDocumentElement();
      m_label = rootNode.getAttribute("label");
      if (m_label == null)
         throw new IllegalArgumentException(
            "invalid xml document, missing required root label");
      
      loadElement(this, rootNode);
   }

   /**
    * Load each element in the tree. Call recursively for each node in the xml
    * file.
    * 
    * @param parentNode the tree node to attach this node to, assumed not 
    *    <code>null</code>.
    * @param el the xml element that describes the current node, assumed not 
    *    <code>null</code>.
    */
   private void loadElement(MutableTreeNode parentNode, Element el)
   {
      NodeList childList = el.getChildNodes();

      int len = (childList != null) ? childList.getLength() : 0;
      int j = 0;
      for (int i=0; i<len; i++)
      {
         Node childNode = childList.item(i);
         if (childNode.getNodeType() == Node.ELEMENT_NODE)
         {
            Element childElement = (Element) childNode;
            String id = childElement.getAttribute("id");
            if (id == null || id.trim().length() == 0)
               throw new IllegalArgumentException(
                  "invalid xml document, missing required node id");
            String label = childElement.getAttribute("label");
            if (label == null || label.trim().length() == 0)
               throw new IllegalArgumentException(
                  "invalid xml document, missing required node label");
            if (childElement.getTagName().equalsIgnoreCase("node"))
            {
               PSCheckboxTreeNode node = new PSCheckboxTreeNode(id, label);
               String selectableAttr = childElement.getAttribute("selectable");
               if (selectableAttr != null && 
                  selectableAttr.equalsIgnoreCase("yes"))
               {
                  node.setSelectable(true);
               }

               parentNode.insert(node, j++);
               loadElement(node, childElement);
            }
         }
      }
   }

   /**
    * Print the label. Used as a label when this node is used in a tree.
    * @return the lable text, never <code>null</code>, may be empty.
    */
   @Override
   public String toString()
   {
      return m_label;
   }

   /**
    * The label for the tree that appears next to the root icon, never
    * <code>null</code>, may be empty.
    */
   private String m_label = "";

   /**
    * Generated serial version id.
    */
   private static final long serialVersionUID = -8009350818045684763L;
}
