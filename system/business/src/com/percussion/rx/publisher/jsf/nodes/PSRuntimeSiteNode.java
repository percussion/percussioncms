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
package com.percussion.rx.publisher.jsf.nodes;

import com.percussion.rx.jsf.PSNodeBase;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.List;

/**
 * The runtime site node. This shows the site's edition as children and allows
 * the user to pick an edition to run. The logs child shows the publishing 
 * logs for the entire site. The user can drill into the various logs.
 * 
 * @author dougrand
 *
 */
public class PSRuntimeSiteNode extends PSNodeBase
{
   /**
    * The site, loaded when this node is edited, cleared on cancel or save.
    */
   IPSSite m_site = null;

   /**
    * Sites have child nodes.
    */
   protected List<PSNodeBase> m_children = new ArrayList<>();

   /**
    * The current index into the collection. <code>-1</code> indicates that no
    * element is currently selected.
    */
   protected int m_index = -1;
   
   /**
    * Ctor.
    * @param site
    */
   public PSRuntimeSiteNode(IPSSite site) {
      super(site.getName(), PSRuntimeStatusNode.STATUS_VIEW);
      m_site = site;
   }

   /**
    * Facade method on the site object. Facade methods translate from internal
    * to external representations, and perform any needed server site validation
    * that cannot be handled by JSF.
    * 
    * @return the site's name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_site.getName();
   }

   /**
    * Gets the ID of the site that associated with this node.
    * @return the site ID, never <code>null</code>.
    */
   public IPSGuid getSiteID()
   {
      return m_site.getGUID();
   }
   
   /**
    * Facade method on the site object. Facade methods translate from internal
    * to external representations, and perform any needed server site validation
    * that cannot be handled by JSF.
    * 
    * @return the site's description, may be <code>null</code> or empty.
    */
   public String getDescription()
   {
      return m_site.getDescription();
   }

   @Override
   public boolean isContainer()
   {
      return true;
   }

   @Override
   public boolean isContainerEmpty()
   {
      return false;
   }

   /**
    * Add a node to this site.
    * 
    * @param node the node to add, never <code>null</code>.
    */
   public void addNode(PSNodeBase node)
   {
      if (node == null)
      {
         throw new IllegalArgumentException("node may not be null");
      }
      m_children.add(node);
      node.setParent(this);
      getModel().addNode(node);
   }

   @Override
   public List<? extends PSNodeBase> getChildren()
   {
      if (m_children.size() == 0)
      {
         PSRuntimeEditionListNode editions = new PSRuntimeEditionListNode(m_site);
         addNode(editions);
         IPSPublisherService psvc = PSPublisherServiceLocator
            .getPublisherService();
         List<IPSEdition> elist = psvc.findAllEditionsBySite(m_site.getGUID());
         for(IPSEdition e : elist)
         {
            PSRuntimeEditionNode enode = new PSRuntimeEditionNode(e);
            editions.addNode(enode);
         }
         addNode(new PSRuntimeSiteLogNode(m_site));
      }
      return m_children;
   }
   
   @Override
   public String toString(int indendation)
   {
      StringBuilder b = new StringBuilder();
      for (int i = 0; i < indendation; i++)
      {
         b.append(' ');
      }
      b.append(super.toString());
      b.append('\n');
      if (m_children != null)
      {
         for (PSNodeBase node : m_children)
         {
            b.append(node.toString(indendation + 2));
            b.append('\n');
         }
      }
      return b.toString();
   }

   public int getRowCount()
   {
      if (m_children == null)
         return -1;
      else
         return m_children.size();
   }

   public Object getRowData()
   {
      return getRowData(m_index);
   }

   public Object getRowData(int i)
   {
      if (isRowAvailable(i))
         return m_children.get(i);
      else
         return null;
   }

   public int getRowIndex()
   {
      return m_index;
   }

   public Object getRowKey()
   {
      PSNodeBase node = (PSNodeBase) getRowData();
      if (node != null)
         return node.getKey();
      else
         return null;
   }

   public boolean isRowAvailable()
   {
      return isRowAvailable(m_index);
   }

   public boolean isRowAvailable(int index)
   {
      if (m_children != null)
         return index >= 0 && index < m_children.size()
               && m_children.get(index) != null;
      else
         return false;
   }

   public void setRowIndex(int i)
   {
      m_index = i;
   }

   public void setRowKey(Object key)
   {
      if (key == null)
      {
         throw new IllegalArgumentException("key may not be null");
      }

      if (m_children == null)
         return;
      String comparekey = key.toString();
      int current = m_index;
      for (m_index = 0; m_index < m_children.size(); m_index++)
      {
         if (isRowAvailable())
         {
            if (comparekey.equals(getRowKey()))
            {
               return;
            }
         }
      }

      // Revert if not found
      m_index = current;
   }
   
   /**
    * @return the name of the css class to use when rendering this node's
    * link in the navigation tree.
    */
   public String getNavLinkClass()
   {
      return "treenode";
   }
}
