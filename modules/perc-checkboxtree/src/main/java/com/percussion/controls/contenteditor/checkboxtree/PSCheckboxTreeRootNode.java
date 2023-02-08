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
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
