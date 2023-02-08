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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PSContextContainerNode extends PSEditableNodeContainer
{

   /**
    * The node title
    */
   public static String NODE_TITLE = "Contexts";
   
   /**
    * Ctor.
    *
    */
   public PSContextContainerNode() 
   {
      super(NODE_TITLE, PUB_DESIGN_CONTEXT_VIEW);
   }

   @Override
   synchronized public List<? extends PSNodeBase> getChildren() throws PSNotFoundException {
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
   public String createContext() throws PSNotFoundException {
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      IPSPublishingContext ctx = smgr.createContext();
      ctx.setName(getUniqueName("Context", false));
      PSContextNode node = 
         new PSContextNode(ctx, new ArrayList<>());
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
      final Set<Object> names = new HashSet<>();
      
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
      catch (PSCatalogException | PSNotFoundException e)
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
   private static final Logger ms_log =
         LogManager.getLogger(PSContextContainerNode.class);
}
