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
import com.percussion.services.sitemgr.IPSLocationScheme;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PSContextContainerNode extends PSEditableNodeContainer
{

   /**
    * The node title
    */
   public static String NODE_TITLE = "Contexts";
   
   /**
    * Ctor.
    * 
    * @param title The text to render for the node in the tree.
    */
   public PSContextContainerNode() 
   {
      super(NODE_TITLE, PUB_DESIGN_CONTEXT_VIEW);
   }

   @Override
   synchronized public List<? extends PSNodeBase> getChildren()
   {
      // synchronize this operation to prevent loading children more than once
      // from multiple requests/threads. This may happen when browser user
      // quickly clicking the same link more than once. 
      if (m_children == null)
      {
         IPSSiteManager siteManager = PSSiteManagerLocator.getSiteManager();
         List<IPSPublishingContext> contexts = siteManager.findAllContexts();
         Collections.sort(contexts, new Comparator<IPSPublishingContext>() 
         {
            public int compare(IPSPublishingContext o1, IPSPublishingContext o2)
            {
               return o1.getName().compareToIgnoreCase(o2.getName());
            }
         });
         for (IPSPublishingContext ctx : contexts)
         {
            addNode(new PSContextNode(ctx));
         }
      }
      return super.getChildren();
   }

   /**
    * Action to create a new context, and add it to the tree.
    * 
    * @return the outcome for the node, which will navigate to the editor.
    */
   public String createContext()
   {
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      IPSPublishingContext ctx = smgr.createContext();
      ctx.setName(getUniqueName("Context", false));
      PSContextNode node = 
         new PSContextNode(ctx, new ArrayList<IPSLocationScheme>());
      return node.handleNewContext(this);
   }

   // see base
   @Override
   protected boolean findObjectByName(String name)
   {
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      try
      {
         smgr.loadContext(name);
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
         IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
         List<IPSCatalogSummary> contextSums = smgr
               .getSummaries(PSTypeEnum.CONTEXT);
         for (final IPSCatalogSummary summary : contextSums)
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
      return "return-to-contexts";
   }
    
   @Override
   public String getHelpTopic()
   {
      return "ContextList";
   }

   /**
    * Outcome for the list page.
    */
   public static final String PUB_DESIGN_CONTEXT_VIEW = "pub-design-context-views";

   /**
    * The logger for the site container node.
    */
   private static final Log ms_log =
         LogFactory.getLog(PSContextContainerNode.class);
}
