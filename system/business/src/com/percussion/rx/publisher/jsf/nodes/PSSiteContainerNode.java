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

import com.percussion.rx.jsf.PSEditableNodeContainer;
import com.percussion.rx.jsf.PSNodeBase;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSCatalogException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This node is the primary container of site items for the design view as well
 * as used in the site list view.
 * 
 * @author dougrand
 * 
 */
public class PSSiteContainerNode extends PSEditableNodeContainer
{
   /**
    * Outcome for the list page.
    */
   public static final String PUB_DESIGN_SITE_VIEWS = "pub-design-site-views";
   
   /**
    * Is this container in the design or runtime trees.
    */
   protected boolean m_design;

   /**
    * The logger for the site container node.
    */
   private static final Log ms_log =
         LogFactory.getLog(PSSiteContainerNode.class);

   /**
    * Ctor.
    * 
    * @param title the title, never <code>null</code> or empty.
    * @param design <code>true</code> for the design tree, <code>false</code>
    * for the runtime tree.
    */
   public PSSiteContainerNode(String title, boolean design) {
      super(title, design ? PUB_DESIGN_SITE_VIEWS : PSRuntimeStatusNode.STATUS_VIEW);
      m_design = design;
   }

   @Override
   public List<? extends PSNodeBase> getChildren()
   {
      // synchronize this operation to prevent loading children more than once
      // from multiple requests/threads. This may happen when browser user
      // quickly clicking the same link more than one. 
      synchronized (this)
      {
         if (m_children != null)
            return getChildrenWithOneSelected();

         try
         {
            List<IPSCatalogSummary> sums = getSiteSummaries();
            Collections.sort(sums, new Comparator<IPSCatalogSummary>() {
               public int compare(IPSCatalogSummary o1, IPSCatalogSummary o2)
               {
                  return o1.getName().compareToIgnoreCase(o2.getName());
               }});
            for (IPSCatalogSummary s : sums)
            {
               try
               {
                  IPSSite site;
                  PSNodeBase snode;
                  if (m_design)
                  {
                     site = getSiteManager().loadSiteModifiable(s.getGUID());
                     snode = new PSSiteNode(site);
                  }
                  else
                  {
                     site = getSiteManager().loadSite(s.getGUID());
                     snode = new PSRuntimeSiteNode(site);
                  }
                     
                  addNode(snode);
               }
               catch (PSNotFoundException e)
               {
                  ms_log.error("Can't load site info: " + s.getName(), e);
               }
            }
         }
         catch (PSCatalogException e)
         {
            ms_log.error("Problem loading children for sites", e);
         }
         return getChildrenWithOneSelected();
      }
   }

   /**
    * Gets all child nodes with one selected node.
    * @return the child nodes, may be <code>null</code> or empty.
    */
   private List<? extends PSNodeBase> getChildrenWithOneSelected()
   {
      List<? extends PSNodeBase> childList = super.getChildren();
      if (childList == null || childList.isEmpty())
         return childList;
      
      boolean isOneSelected = false;
      for (PSNodeBase node : childList)
      {
         if (node.getSelectedRow())
         {
            isOneSelected = true;
            break;
         }
      }
      // if no child node selected, set the 1st one.
      if (!isOneSelected)
         childList.get(0).setSelectedRow(true);
      
      return childList;
   }
   
   /**
    * Provides summaries for all the sites.
    * @return the site summaries. Never <code>null</code>.
    * @throws PSCatalogException if cataloging fails.
    */
   private List<IPSCatalogSummary> getSiteSummaries() throws PSCatalogException
   {
      return getSiteManager().getSummaries(PSTypeEnum.SITE);
   }

   /**
    * A convenience method to access
    * {@link PSSiteManagerLocator#getSiteManager()}.
    * @return the site manager. Never <code>null</code>.
    */
   private IPSSiteManager getSiteManager()
   {
      return PSSiteManagerLocator.getSiteManager();
   }

   @Override
   public boolean isContainerEmpty()
   {
      if (m_children != null)
         return m_children.isEmpty();
      else
         return false;
   }
   
   /**
    * Action to create a new site, and add it to the tree.
    * @return the perform action for the site node, which will navigate to the
    * editor.
    */
   public String createSite()
   {
      IPSSiteManager smgr = getSiteManager();
      IPSSite newsite = smgr.createSite();
      newsite.setName(getUniqueName("Site", false));
      PSSiteNode node = new PSSiteNode(newsite);
      return node.handleNewSite(this, node);
   }

   // see base
   @Override
   protected boolean findObjectByName(String name)
   {
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      try
      {
         smgr.loadSite(name);
      }
      catch (PSNotFoundException e)
      {
         return false;
      }
      
      return true;
   }

   @Override
   public Set<Object> getAllNames()
   {
      final Set<Object> names = new HashSet<Object>();
      
      try
      {
         for (final IPSCatalogSummary summary : getSiteSummaries())
         {
            names.add(summary.getName());
         }
      }
      catch (PSCatalogException e)
      {
         ms_log.error("Problem obtaining site names", e);
      }

      return names;
   }

   @Override
   public String returnToListView()
   {
      return "return-to-sites";
   }
   
   @Override
   public String getHelpTopic()
   {
      return "SiteList";
   }
}
