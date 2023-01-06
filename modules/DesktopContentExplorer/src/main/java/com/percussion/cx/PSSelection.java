/*[ PSSelection.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.cx;

import com.percussion.cx.objectstore.PSNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The class that represents a selection in the UI that represents a part of UI
 * context to execute an action.
 */
public class PSSelection
{
   /**
    * Constructs this object with supplied parameters.
    *
    * @param uiMode the ui mode that represents the component view which is the
    * source of the selection, may not be <code>null</code>
    * @param parent the parent of the supplied node list, may be <code>null</code>
    * @param nodeList the list of <code>PSNode</code> objects that are selected,
    * may not be <code>null</code> or empty.
    */
   public PSSelection(PSUiMode uiMode, PSNode parent, Iterator nodeList)
   {
      if (uiMode == null)
         throw new IllegalArgumentException("uiMode may not be null.");

      if (nodeList == null || !nodeList.hasNext())
         throw new IllegalArgumentException(
            "nodeList may not be null or empty.");

      m_uiMode = uiMode;
      m_parent = parent;
      while(nodeList.hasNext())
      {
         PSNode sel = (PSNode)nodeList.next();

         if(sel == null)
            throw new IllegalArgumentException(
               "Elements in the nodeList may not ne null.");

         m_nodeList.add(sel);
      }
   }

   /**
    * Gets the content of the selection.
    *
    * @return the iterator of <code>PSNode</code> objects, never <code>null</code>
    * or empty.
    */
   public Iterator getNodeList()
   {
      return Collections.unmodifiableCollection(m_nodeList).iterator();
   }

   /**
    * Check if the selection is of multi-select.
    *
    * @return <code>true</code> if the selection has more than one item,
    * <code>false</code> otherwise.
    */
   public boolean isMultiSelect()
   {
      return (m_nodeList.size() > 1);
   }

   /**
    * Check if this selection contains only nodes of type system folder. 
    * Currently defined system folder types are:
    * <ul>
    *    <li>PSNode.TYPE_SYS_FOLDERS</li>
    *    <li>PSNode.TYPE_SYS_SITES</li>
    * </ul>
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isSystemFolderType()
   {
      return isOfType(PSNode.TYPE_SYS_FOLDERS) || 
         isOfType(PSNode.TYPE_SYS_SITES);
   }
   
   /**
    * Check if this selection contains only nodes of type folder. Currently 
    * defined folder types are:
    * <ul>
    *    <li>PSNode.TYPE_FOLDER</li>
    *    <li>PSNode.TYPE_SITE</li>
    *    <li>PSNode.TYPE_SITESUBFOLDER</li>
    * </ul>
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isFolderType()
   {
      return isOfType(PSNode.TYPE_FOLDER) || isOfType(PSNode.TYPE_SITE) ||
         isOfType(PSNode.TYPE_SITESUBFOLDER);
   }

   /**
    * Determine if this selection contains only nodes of any type folder. This 
    * is the ORed result of {@link #isSystemFolderType()} and 
    * {@link #isFolderType()}.
    *
    * @return <code>true</code> if it is of any type folder, <code>false</code>
    *    otherwise.
    */
   public boolean isAnyFolderType()
   {
      return isSystemFolderType() || isFolderType();
   }

   /**
    * Check if the selection is of the specified node type.
    *
    * @param type <code>PSNode</code> type to check for, one of the valid 
    *    <code>PSNode</code> types.
    * @return <code>true</code> if the selection has more than one item,
    *    <code>false</code> otherwise.
    */
   public boolean isOfType(String type)
   {
      if (!PSNode.isTypeValid(type))
         throw new IllegalArgumentException("Illegal node type specified");

      return type.equals(getType());
   }

   /**
    * Get the selection type. Type is assumed to be valid only if every node in
    * the selection is of same type.
    *
    * @return the selction type which will be one of PSNode types, if all nodes
    *    in the selection are of the same node type. Empty string if the 
    *    selection is not of the same type.
    */
   public String getType()
   {
      String type = ((PSNode) m_nodeList.get(0)).getType();
      for (int i=1; i<m_nodeList.size(); i++)
      {
         if (!type.equals(((PSNode) m_nodeList.get(i)).getType()))
            return "";
      }
      
      return type;
   }

   /**
    * Get the selection types which is an array list of union of all node types
    * in the selection.
    *
    * @return the list of node types in the selection. Each selection type in
    * the list is one of the PSNode types. Never <code>null</code> or
    * <code>empty</code>.
    */
   public List getTypes()
   {
      List list = new ArrayList();
      String type = "";
      for(int i=0; i<m_nodeList.size(); i++)
      {
         type = ((PSNode)m_nodeList.get(i)).getType();
         if(!list.contains(type))
            list.add(type);
      }
      return list;
   }

   /**
    * Helper method to test of the selection contains a specified node.
    * @param node PSNode object to test the existence in the selection, must
    * not be <code>null</code>.
    * @return <code>true</code> if selection contains the specified node
    * <code>false</code> otherwise.
    */
   public boolean containsNode(PSNode node)
   {
      if(node==null)
         throw new IllegalArgumentException("node must not be null");
      return m_nodeList.contains(node);
   }

   /**
    * Gets the selection size.
    *
    * @return the size of the list of nodes.
    */
   public int getNodeListSize()
   {
      return m_nodeList.size();
   }

   /**
    * Gets the ui mode of the selection.
    *
    * @return the mode, never <code>null</code>
    */
   public PSUiMode getMode()
   {
      return m_uiMode;
   }

   /**
    * Gets the parent node of the selection.
    *
    * @return the node, may be <code>null</code>
    */
   public PSNode getParent()
   {
      return m_parent;
   }

   /**
    * The UI Mode represented by this selection object, intiailized in the ctor
    * and never <code>null</code> or modified after that.
    */
   private PSUiMode m_uiMode;

   /**
    * The content of the selection, initialized in the ctor, never <code>null
    * </code> or empty or modified after that.
    */
   private List m_nodeList = new ArrayList();

   /**
    * The parent node of the selection object, intiailized in the ctor
    * and may be <code>null</code>. Never modified after that.
    */
   private PSNode m_parent;
}