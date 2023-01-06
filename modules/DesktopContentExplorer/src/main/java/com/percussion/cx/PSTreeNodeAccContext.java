/*[ PSTreeNodeAccContext.java ]*************************************************
 *
 * COPYRIGHT (c) 2004 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.cx;

import com.percussion.cx.objectstore.PSNode;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleText;
import javax.swing.text.AttributeSet;
import javax.swing.tree.TreeNode;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Locale;


/**
 * Implementation of {@link AccessibleContext} for a tree node. A context
 * provides information for a screen reader to read a component. 
 */
public class PSTreeNodeAccContext 
   extends AccessibleContext
   implements AccessibleText
{
   /**
    * Initialized in the ctor, never <code>null</code> afterward. This 
    * field holds a reference to the tree being used for the context.
    */
   private PSNavigationTree m_tree = null;
   
   /**
    * The node that this is a context for. Initialized in ctor, never
    * <code>null</code> afterward.
    */
   private PSNavigationTree.PSTreeNode m_node = null;

   /**
    * Ctor
    * 
    * @param node node to create accessiblecontext for, assumed
    * to be not <code>null</code>.
    */
   public PSTreeNodeAccContext(PSNavigationTree tree, PSNavigationTree.PSTreeNode node)
   {
      m_node = node;
      m_tree = tree;
   }
   
   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleContext#getAccessibleRole()
    */
   public AccessibleRole getAccessibleRole()
   {
      return AccessibleRole.LABEL;
   }

   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleContext#getAccessibleStateSet()
    */
   public AccessibleStateSet getAccessibleStateSet()
   {
      AccessibleStateSet set = new AccessibleStateSet();
      set.add(AccessibleState.ACTIVE);
      set.add(AccessibleState.SELECTED);
      set.add(AccessibleState.SHOWING);
      set.add(AccessibleState.VISIBLE);
      return set;
   }

   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleContext#getAccessibleIndexInParent()
    */
   public int getAccessibleIndexInParent()
   {
      // Get parent and look for this node in child list
      TreeNode parent = m_node.getParent();
      if (parent != null)
      {
         return parent.getIndex(m_node);  
      }
      else
      {
         return 0;
      }
   }

   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleContext#getAccessibleChildrenCount()
    */
   public int getAccessibleChildrenCount()
   {
      return m_node.getChildCount();
   }

   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleContext#getAccessibleChild(int)
    */
   public Accessible getAccessibleChild(int arg0)
   {
      return (PSNavigationTree.PSTreeNode) m_node.getChildAt(arg0);
   }

   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleContext#getLocale()
    */
   public Locale getLocale() throws IllegalComponentStateException
   {
      return m_tree.getLocale();
   }
   
   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleContext#getAccessibleDescription()
    */
   public String getAccessibleDescription()
   {
      PSNode data = (PSNode)m_node.getUserObject();
      return data.getName();
   }

   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleContext#getAccessibleName()
    */
   public String getAccessibleName()
   {
      PSNode data = (PSNode)m_node.getUserObject();
      return data.getName();
   }

   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleText#getIndexAtPoint(java.awt.Point)
    */
   public int getIndexAtPoint(Point arg0)
   {
      // XXX Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleText#getCharacterBounds(int)
    */
   public Rectangle getCharacterBounds(int arg0)
   {
      // XXX Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleText#getCharCount()
    */
   public int getCharCount()
   {
      // XXX Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleText#getCaretPosition()
    */
   public int getCaretPosition()
   {
      // XXX Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleText#getAtIndex(int, int)
    */
   public String getAtIndex(int arg0, int arg1)
   {
      // XXX Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleText#getAfterIndex(int, int)
    */
   public String getAfterIndex(int arg0, int arg1)
   {
      // XXX Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleText#getBeforeIndex(int, int)
    */
   public String getBeforeIndex(int arg0, int arg1)
   {
      // XXX Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleText#getCharacterAttribute(int)
    */
   public AttributeSet getCharacterAttribute(int arg0)
   {
      // XXX Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleText#getSelectionStart()
    */
   public int getSelectionStart()
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleText#getSelectionEnd()
    */
   public int getSelectionEnd()
   {
      String text = getAccessibleName();
      return text.length();
   }

   /* (non-Javadoc)
    * @see javax.accessibility.AccessibleText#getSelectedText()
    */
   public String getSelectedText()
   {
      return getAccessibleName();
   }
}