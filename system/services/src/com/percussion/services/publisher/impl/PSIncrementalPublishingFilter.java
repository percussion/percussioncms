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
package com.percussion.services.publisher.impl;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSSlotContentFinder;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.IPSPublisherServiceErrors;
import com.percussion.services.publisher.IPSSiteItem;
import com.percussion.services.publisher.PSPublisherException;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.timing.PSTimer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Filter a content list for incremental publishing
 * 
 * @author dougrand
 */
public class PSIncrementalPublishingFilter
{
   /**
    * Log
    */
   private static final Logger ms_log = LogManager.getLogger(PSIncrementalPublishingFilter.class);

   /**
    * Each site item needs to be recorded under a key that allows us to lookup
    * the corresponding site item for each candidate item. We don't need the 
    * context since the site items are already selected by context. Location is 
    * used instead of location hash as it is more accurate.
    */
   static class ItemKey
   {
      /**
       * The item's content id
       */
      long mi_contentid;
      
      /**
       * The template used
       */
      long mi_templateid;
      
      /**
       * The published location
       */
      String mi_location;
      
      /**
       * Ctor
       * @param contentid the content id of the published item
       * @param templateid the template used to publish the item
       * @param location the location the item was published to
       */
      public ItemKey(long contentid, long templateid, String location)
      {
         mi_contentid = contentid;
         mi_templateid = templateid;
         mi_location = location;
      }

      /**
       * Create a key from a site item. 
       * @param item the site item, assumed never <code>null</code>.
       */
      public ItemKey(IPSSiteItem item) {
         this(item.getContentId(), item.getTemplateId(), item.getLocation());
      }

      /**
       * @return the contentid
       */
      public long getContentid()
      {
         return mi_contentid;
      }

      /**
       * @return the location
       */
      public String getLocation()
      {
         return mi_location;
      }

      /**
       * @return the templateid
       */
      public long getTemplateid()
      {
         return mi_templateid;
      }

      /* (non-Javadoc)
       * @see java.lang.Object#equals(java.lang.Object)
       */
      @Override
      public boolean equals(Object obj)
      {
         return EqualsBuilder.reflectionEquals(this, obj);
      }

      /* (non-Javadoc)
       * @see java.lang.Object#hashCode()
       */
      @Override
      public int hashCode()
      {
         int lh = mi_location != null ? mi_location.hashCode() : 0;
         return (int) (mi_contentid + mi_templateid) + lh;
      }
   }
   
   /**
    * Filter the content list items using information stored in the site items
    * table as well as touching appropriate parent items for templates that use
    * auto slots.
    * 
    * @param items content list to publish, never <code>null</code>. This
    * list will be modified by the filtering process.
    * @param params the parameters from the content list code, never
    * <code>null</code>. The parameters <code>sys_siteid</code> and
    * <code>sys_context</code> are required. Note that the siteid may be
    * either a simple number such as <code>301</code> or in the future it may
    * be a fully qualified GUID.
    * @param movedIds the IDs of the items that have been moved to/from
    * different folder since last publishing run.
    * @throws PSPublisherException if required parameters are missing.
    * @see PSTouchParentItemsHandler for a full explanation of what parent items
    * are touched via AA relationships
    */
   public void filter(List<IPSFilterItem> items,
      Map<String, String> params, Collection<Integer> movedIds) 
      throws PSPublisherException
   {
      notNull(items, "clist");
      notNull(params, "params");

      ms_log.debug("Found " + items.size() + " items (selected by generator) that need to be filtered.");
      ms_log.debug("Found " + movedIds.size() + " moved items");

      String siteidstr = params.get(IPSHtmlParameters.SYS_SITEID);
      if (StringUtils.isBlank(siteidstr))
      {
         throw new PSPublisherException(IPSPublisherServiceErrors.SITE_MISSING);
      }
      String contextstr = params.get(IPSHtmlParameters.SYS_CONTEXT);
      if (StringUtils.isBlank(contextstr))
      {
         throw new PSPublisherException(
               IPSPublisherServiceErrors.CONTEXT_MISSING);
      }
      int context = Integer.parseInt(contextstr);

      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
      IPSPublisherService pub = PSPublisherServiceLocator.getPublisherService();
      try
      {
         IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
         IPSSite site = smgr.loadUnmodifiableSite(gmgr.makeGuid(siteidstr,
               PSTypeEnum.SITE));
         
         
         PSTimer timerTotal = new PSTimer(ms_log);
         PSTimer timer = new PSTimer(ms_log);
         
         Set<IPSGuid> computedCTSet = findComputedContentTypes(asm, cmgr, site);
         timer.logElapsed("Found " + computedCTSet.size() + " computed content types");

         // This will get the associated content items and the AA parents
         timer = new PSTimer(ms_log);
         Collection<Integer> alwaysPublishIds = pub
               .getContentTypeItems(computedCTSet);
         timer.logElapsed("Found " + alwaysPublishIds.size() + " computed item & its parent IDs");

         timer = new PSTimer(ms_log);
         Collection<Integer> ids = getContentIds(items, movedIds, alwaysPublishIds);
         ids = pub.findItemsSinceLastPublish(site.getGUID(), context, ids);
         timer.logElapsed("Found " + ids.size() + " modified or fail published items since last publish");
         
         filterByIds(items, ids, movedIds, alwaysPublishIds);

         timerTotal.logElapsed("Found " + items.size() + " items that need to be published");
      }
      catch (PSNotFoundException e)
      {
         throw new PSPublisherException(IPSPublisherServiceErrors.SITE_LOAD, e,
               siteidstr);
      }
      catch (RepositoryException e)
      {
         throw new PSPublisherException(IPSPublisherServiceErrors.UNEXPECTED,
               e, e.getLocalizedMessage());
      }
   }

   /**
    * Filter the specified content list items. It retains the following items:
    * <ul>
    *    <li>Items have been modified since last publishing and 
    *    unsuccessful published</li>
    *    <li>Moved items, where they have been moved into different folder 
    *    since last publishing.</li>
    *    <li>Items with computed content type or "auto-indexed" items</li>
    * </ul> 
    * 
    * @param items the to be filtered content list items, assumed not 
    * <code>null</code>, but may be empty.
    * @param ids the IDs of the items that have modified since last publish
    * and items that failed to publish in last publishing run. Assumed not
    * <code>null</code>, but may be empty.
    * @param movedIds the IDs of items that have been moved since last
    * publish run.
    * @param alwaysPublishIds the IDs of items with computed content type,
    * assumed not <code>null</code>, but may be empty.
    */
   private void filterByIds(List<IPSFilterItem> items,
         Collection<Integer> ids, Collection<Integer> movedIds,
         Collection<Integer> alwaysPublishIds)
   {
      ids.addAll(movedIds);
      ids.addAll(alwaysPublishIds);
      
      for (Iterator<IPSFilterItem> iter = items.iterator(); iter.hasNext();) 
      {
         IPSFilterItem item = iter.next();
         int id = ((PSLegacyGuid)item.getItemId()).getContentId();
         if (!ids.contains(id))
            iter.remove();
       }
   }
   
   /**
    * Gets the content IDs from the specified content list items, excludes 
    * the specified "moved IDs" and "always published IDs".
    * 
    * @param items the specified content list items, assumed not 
    * <code>null</code>, may be empty.
    * @param movedIds the list of "moved IDs", where the related items
    * have been moved since last publish run. Assumed not 
    * <code>null</code>, may be empty. 
    * @param alwaysPublishIds the IDs of the items with "computed"
    * content type, assumed not <code>null</code>, but may be empty.
    * 
    * @return the content IDs described above, not <code>null</code>,
    * but may be empty.
    */
   private Collection<Integer> getContentIds(List<IPSFilterItem> items,
         Collection<Integer> movedIds, Collection<Integer> alwaysPublishIds)
   {
      List<Integer> contentIds = new ArrayList<>();
      for (IPSFilterItem item : items)
      {
         int id = ((PSLegacyGuid)item.getItemId()).getContentId();
         contentIds.add(id);
      }
      contentIds.removeAll(movedIds);
      contentIds.removeAll(alwaysPublishIds);
      
      return contentIds;
   }
   
   /**
    * Starting with the templates that are associated with the site, find
    * content types that reference computed slots.
    * 
    * @param asm assembly service
    * @param cmgr content manager
    * @param site the site
    * @return a set of content type guids
    * @throws RepositoryException
    */
   private Set<IPSGuid> findComputedContentTypes(IPSAssemblyService asm,
         IPSContentMgr cmgr, IPSSite site) throws RepositoryException
   {
      Set<IPSGuid> computedCTSet = new HashSet<>();
      String finder = null;

      for (IPSAssemblyTemplate t : site.getAssociatedTemplates())
      {
         for (IPSTemplateSlot slot : t.getSlots())
         {
            try
            {
               finder = slot.getFinderName();
               if (!StringUtils.isBlank(finder))
               {
                  IPSSlotContentFinder scf = asm.loadFinder(finder);
                  if (scf.getType().isMustIncrementallyPublish())
                  {
                     // Add the content types associated with the template to
                     // the pass list
                     for (IPSNodeDefinition def : cmgr
                           .findNodeDefinitionsByTemplate(t.getGUID()))
                     {
                        computedCTSet.add(def.getGUID());
                     
                        if (ms_log.isDebugEnabled())
                        {
                           ms_log.debug("Auto Slot: " + slot.getName()
                                 + ", Template: " + t.getName()
                                 + ", ContentType: " + def.getName() + "("
                                 + def.getGUID().getUUID() + ")");
                        }
                     }
                  }
               }
            }
            catch (PSAssemblyException e)
            {
               ms_log
                     .warn("Problem while loading finder information for finder: "
                           + finder);
            }
         }
      }

      return computedCTSet;
   }
}
