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

package com.percussion.services.touchitem.impl;

import static com.percussion.cms.objectstore.PSCmsObject.TYPE_FOLDER;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.fastforward.managednav.IPSManagedNavService;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.content.data.PSItemSummary.ObjectTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.services.touchitem.IPSTouchItemService;
import com.percussion.services.touchitem.PSTouchItemConfiguration;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.system.IPSSystemWs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * See {@link IPSTouchItemService}.
 * 
 * @author adamgent
 * @see IPSTouchItemService
 */
public class PSTouchItemService
      implements
         IPSTouchItemService
{
   /**
    * The logger for this class.
    */
   private static Logger ms_logger = Logger.getLogger(PSTouchItemService.class);
   private IPSContentWs contentWs;
   private IPSGuidManager guidMgr;
   private IPSCmsObjectMgr cmsMgr;
   private IPSManagedNavService navService;
   private PSTouchItemConfiguration configuration = new PSTouchItemConfiguration();
   private PSTouchParentHelper touchParentHelper;
   private IPSSystemWs systemWs;
      
   /**
    * Autowired by spring.
    * @param contentWs not null.
    * @param guidMgr not null.
    * @param cmsMgr not null.
    * @param navService not null.
    */
   public PSTouchItemService(IPSContentWs contentWs, IPSGuidManager guidMgr, 
         IPSCmsObjectMgr cmsMgr, IPSManagedNavService navService,
         IPSSystemWs systemWs)
   {
      this.contentWs = contentWs;
      this.guidMgr = guidMgr;
      this.cmsMgr = cmsMgr;
      this.navService = navService;
      this.touchParentHelper = new PSTouchParentHelper();
      this.systemWs = systemWs;
   }
   
   /**
    * {@inheritDoc}
    */
   public int touchItems(IPSRequestContext request, PSRelationship rel)
   {
      try
      {
         if (requireTouchFolderItems(rel))
            touchParentHelper.touchItemAndParents(request, rel);
      }
      catch (Exception e)
      {
         ms_logger.error("Error for legacy touch items", e);
         throw new RuntimeException(e);
      }
      
      if (!getConfiguration().isTouchItemEnabled())
         return 0;
      
      if (!requireExtendedTouchItems(rel))
         return 0;
      
      Collection<Integer> touchedItems = new HashSet<Integer>();
      
      PSLocator depLoc = rel.getDependent();
      if (rel.getDependentObjectType() == TYPE_FOLDER)
      {
         return touchItems(depLoc, touchedItems, 0);
      }
      else
      {
         IPSGuid id = guidMgr.makeGuid(depLoc);
         Impact imp = getImpact(id);
         if (imp.isImpactItem)
         {
            return touchItems(id, imp.levelTargetTypesMap, touchedItems,
                  false);
         }
      }
      
      return 0;
   }

   /**
    * {@inheritDoc}
    */   
   public int touchItems(IPSGuid id)
   {
      if (!getConfiguration().isTouchItemEnabled())
         return 0;

      Impact impact = getImpact(id);
      if (!impact.hasImpact())
         return 0;
      
      int count = 0;
      if (impact.isNavItem && getConfiguration().isTouchDescendantNavItems())
      {
         if (getConfiguration().isTouchLandingPages())
         {
            count += touchDescendantLandingPage(id);
         }
         else
         {
            count += touchDescendantNavon(id);
         }
      }
      
      if (impact.isImpactItem)
      {
         Collection<Integer> touchedItems = new HashSet<Integer>();
         touchedItems.add(((PSLegacyGuid) id).getContentId());
         
         count += touchItems(id, impact.levelTargetTypesMap, touchedItems,
               false);
      }
      
      ms_logger.debug("For item (" + id.toString()
            + "), the total number of touched items is " + count);

      return count;
   }
   /**
    * {@inheritDoc}  
    */
   public void updateSiteItems(IPSRequestContext requestContext, PSRelationship rel)
   {
      PSRequest request = getRequest(requestContext);
      PSLocator defId = rel.getDependent();

      IPSSiteManager msg = PSSiteManagerLocator.getSiteManager();
      IPSPublisherService pubSrv = PSPublisherServiceLocator.getPublisherService();
      PSServerFolderProcessor folderProcessor = PSServerFolderProcessor.getInstance();
      try
      {
         Collection<PSLocator> ids = folderProcessor.getDescendantFoldersWithItems(defId);
         List<IPSSite> sites = msg.getItemSites(new PSLegacyGuid(defId));

         List<Integer> folderIds = new ArrayList<Integer>();
         for (PSLocator loc : ids)
            folderIds.add(loc.getId());
         for (IPSSite site : sites)
         {
            pubSrv.markFolderIdsForMovedFolders(site.getGUID(), folderIds);
         }
      }
      catch(Exception e)
      {
         // do nothing if there is an error.
         if (ms_logger.isDebugEnabled())
            ms_logger.error("Caught error while updating site items", e);
      }
   }
   
   /**
    * Will touch the subtree of nodes at the given id (all
    * navon nodes under id will be touched).
    * @param id not null.
    * @return number of nodes touched.
    */
   private int touchDescendantNavon(IPSGuid id)
   {
      return touchDescendantNavItems(id, false);
   }
   
   /**
    * Will touch the subtree of nodes at the given id (all
    * landing page nodes under id will be touched).
    * @param id not null.
    * @return number of nodes touched.
    */
   private int touchDescendantLandingPage(IPSGuid id)
   {
      return touchDescendantNavItems(id, true);
   }
   
   /**
    * Will touch the subtree of nodes at the given id (all
    * nodes under id will be touched).
    * @param id not null.
    * @param touchLandingPages <code>true</code> to only touch
    * landing pages, <code>false</code> to only touch navons.
    * @return number of nodes touched.
    */
   private int touchDescendantNavItems(IPSGuid id,
         boolean touchLandingPages)
   {
      List<IPSGuid> nodeIds = navService.findDescendantNavonIds(id);
      List<Integer> ids = new ArrayList<Integer>();
      
      for (IPSGuid nodeId : nodeIds)
      {
         IPSGuid itemId;
         if (touchLandingPages)
         {
            itemId = navService.getLandingPageFromNavnode(nodeId);
            if (itemId == null)
            {
               ms_logger.warn("For navon item (" + nodeId.toString()
                     + "), landing page was not found");
               
               continue;
            }
         }
         else
         {
            itemId = nodeId;
         }
         
         int cid = ((PSLegacyGuid) itemId).getContentId();
         ids.add(new Integer(cid));
      }
      cmsMgr.touchItems(ids);
      
      ms_logger.debug("For navon item (" + id.toString()
            + "), the number of touched descendants is " + ids.size());

      return ids.size();
   }
   
   /**
    * Touches all items which are direct parents of the specified items via
    * an AA relationship.
    * 
    * @param ids of the items.
    * @param touchedItems id's of items which have already been
    * touched.
    * 
    * @return number of new items which have been touched.
    */
   private int touchAAParentItems(Collection<IPSGuid> ids,
         Collection<Integer> touchedItems)
   {
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setName(PSRelationshipFilter.FILTER_NAME_ACTIVE_ASSEMBLY);
      
      List<IPSGuid> ownerIds = new ArrayList<IPSGuid>();
      for (IPSGuid id : ids)
      {
         try
         {
            ownerIds.addAll(systemWs.findOwners(id, filter));
         }
         catch (PSErrorException e)
         {
            ms_logger.error("Failed to find AA parent items for item: " +
                  id.toString(), e);
         }
      }
      
      Set<Integer> idSet = new HashSet<Integer>();
      
      if (!ownerIds.isEmpty())
      {
         List<Integer> contentIds = guidMgr.extractContentIds(ownerIds);

         // remove duplicates
         idSet.addAll(contentIds);
         idSet.removeAll(touchedItems);

         cmsMgr.touchItems(idSet);
         touchedItems.addAll(idSet);
      }

      return idSet.size();
   }
   
   /**
    * Touches all items which are of the specified type and located
    * at the specified level relative to the location of the specified
    * item.
    *  
    * @param id id for an item.
    * @param level relative location at or above the current item's
    * location.
    * @param targetTypes items of one of the these content types will
    * be touched.
    * @param touchedItems id's of items which have already been
    * touched.
    * @return number of items touched.
    */
   private int touchItems(IPSGuid id, int level,
         Set<String> targetTypes, Collection<Integer> touchedItems)
   {
      boolean touchAAParents = getConfiguration().shouldTouchAAParents(
            getContentTypeId(id), level, targetTypes);
    
      int absLevel = Math.abs(level) + 1;
      
      try
      {
         int count = 0;
         String[] paths = contentWs.findFolderPaths(id);
         for (String path : paths)
         {
            List<IPSGuid> ids = contentWs.findPathIds(path);
            if (ids.size() >= absLevel)
            {
               PSLocator parentId = 
                  ((PSLegacyGuid)ids.get(ids.size() - absLevel)).getLocator();   
               count += touchFolderChildren(parentId, targetTypes, touchedItems,
                     touchAAParents);               
            }
         }

         return count;
      }
      catch (PSErrorException e)
      {
         ms_logger.error("Failed to touch items for item: " + id.toString(), e);
         return 0;
      }
      
   }
   
   /**
    * For the specified item, touches all items identified by the
    * specified level-target type map.
    *  
    * @param id id for an item.
    * @param levelTargets map of relative folder locations to target content
    * types which should be touched.
    * @param touchedItems id's of items which have already been
    * touched.
    * @param skipSiblings <code>true</code> to avoid touching items
    * which are siblings of the specified item, <code>false</code> to
    * touch these items.
    * @return number of items touched.
    */
   private int touchItems(IPSGuid id, Map<Integer, Set<String>> levelTargets,
         Collection<Integer> touchedItems, boolean skipSiblings)
   {
      int count = 0;
      
      for (Integer level : levelTargets.keySet())
      {
         if (level > 0)
         {
            ms_logger.warn("Unsupported touch item configuration level: " +
                  level);
            continue;
         }
         
         if (skipSiblings && level == 0)
         {
            continue;
         }
         
         Set<String> targetTypes = levelTargets.get(level);
         count += touchItems(id, level, targetTypes, touchedItems);
      }
            
      return count;
   }
   
   /**
    * A Union like container that represents what is
    * impacted (item type) by the change.
    * @author adamgent
    */
   private static class Impact {
      /**
       * true if navigation items
       */
      public boolean isNavItem = false;
      /**
       * True if the item is associated with
       * a navon through the landing page aa relationship.
       */
      public boolean isLandingPage = false;
      /**
       * True if the item is
       * PSItemCon
       */
      public boolean isImpactItem = false;
      public Map<Integer, Set<String>> levelTargetTypesMap =
         new HashMap<Integer, Set<String>>();
      public boolean hasImpact() {
         return (isNavItem || isLandingPage || isImpactItem);
      }
   }
   /**
    * Determines what needs to be touched.
    * @param id not null.
    * @return not null.
    */
   private Impact getImpact(IPSGuid id)
   {
      Impact im = new Impact();
      long contentTypeId = getContentTypeId(id);
      im.isNavItem = isNavigationType(contentTypeId);
      im.isLandingPage = navService.isLandingPage(id);
      im.levelTargetTypesMap = 
         getConfiguration().getLevelTargetTypes(contentTypeId);
      im.isImpactItem = !im.levelTargetTypesMap.isEmpty();
      return im;
   }
   
   /**
    * Determines if the given content type is a navigation type
    * (navon, or navtree).
    * @param typeId content type id.
    * @return <code>true</code> if it is a navigation type.
    */
   private boolean isNavigationType(long typeId)
   {
      long navonId = navService.getNavonContentTypeId();
      long navtreeId = navService.getNavtreeContentTypeId();
          
      return (typeId == navonId || typeId == navtreeId);
   }
   
   /**
    * Touches given folder children so long as they're
    * of a type in the types parameter.
    * @param folderId the locator of the folder.
    * @param types not null, maybe empty.
    * @param touchedItems content id's of items which have already been
    * touched.
    * @param touchAAParents <code>true</code> to touch all AA parents
    * of the touched child items, <code>false</code> otherwise.
    * @return number of items touched.
    */
   private int touchFolderChildren(PSLocator folderId, Set<String> types,
         Collection<Integer> touchedItems, boolean touchAAParents)
   {
      if (types.isEmpty()) return 0;
      
      int count = 0;
      
      Map<Integer, IPSGuid> childMap = findFolderChildrenByTypes(folderId,
            types); 
      if (!childMap.isEmpty())
      {
         Set<Integer> contentIds = childMap.keySet();
         contentIds.removeAll(touchedItems);
         cmsMgr.touchItems(contentIds);
         touchedItems.addAll(contentIds);
         
         count += contentIds.size();
         
         if (touchAAParents)
         {
            Set<IPSGuid> guids = new HashSet<IPSGuid>();
            for (Integer contentId : contentIds)
            {
               guids.add(childMap.get(contentId));
            }

            if (!guids.isEmpty())
            {
               count += touchAAParentItems(guids, touchedItems);                  
            }
         }
      }
      
      ms_logger.debug("For folder (" + folderId.getId()
            + "), the number of touched items is " + count);
      
      return count;
   }
   
   /**
    * Determines if the relationship needs to be processed
    * for at least the legacy touch parent for the specified
    * folder relationship.
    * @param rel maybe null.
    * @return <code>false</code> if rel is <code>null</code> 
    * or is not a folder relationship.
    */
   private boolean requireTouchFolderItems(PSRelationship rel)
   {
      if (rel == null)
         return false;
       
      if (!rel.getConfig().getName().equals(
            PSRelationshipConfig.TYPE_FOLDER_CONTENT))
      {
         return false;
      }

      return true;

   }
   
   /**
    * Similar to {@link #requireTouchFolderItems(PSRelationship)}
    * except this method determines if the extended
    * touching based on content type is needed
    * (in other words more than just touch parent.)
    * @param rel not null.
    * @return <code>true</code> if extended processing is needed.
    */
   private boolean requireExtendedTouchItems(PSRelationship rel) {
      if (! requireTouchFolderItems(rel) ) return false;
      if (rel.getDependentObjectType() == TYPE_FOLDER)
      {
         return true;
      }
      long contentTypeId = getContentTypeId(guidMgr.makeGuid(
            rel.getDependent()));
      return isImpactType(contentTypeId);
   }

   /**
    * Finds folder children that are the given content types.
    * @param parent not null.
    * @param typeNames not null, maybe empty.
    * @return map of content id to guid, not null maybe empty.
    */
   private Map<Integer, IPSGuid> findFolderChildrenByTypes(PSLocator parent,
         Collection<String> typeNames)
   {
      Map<Integer, IPSGuid> childMap = new HashMap<Integer, IPSGuid>();
      
      for (PSItemSummary item : findFolderChildren(parent))
      {
         String typeName = item.getContentTypeName();
         if (typeNames.contains(typeName))
         {
            IPSGuid guid = item.getGUID();
            childMap.put(((PSLegacyGuid) guid).getContentId(), guid);
         }
      }
      
      return childMap;
   }
   
   /**
    * Finds children of a folder.
    * @param parent not null
    * @return not null maybe empty.
    */
   private List<PSItemSummary> findFolderChildren(PSLocator parent)
   {
      try
      {
         IPSGuid id = guidMgr.makeGuid(parent);
         return contentWs.findFolderChildren(id, false);
      }
      catch (PSErrorException e)
      {
         ms_logger.error("Failed to find folder children", e);
         throw new RuntimeException(e);
      }
   }

   /**
    * Determines if the type needs to be touched based on
    * the configuration {@link PSTouchItemConfiguration}.
    * @param typeId content type id.
    * @return <code>true</code> if the item needs touching.
    */
   private boolean isImpactType(long typeId)
   {
      return !getConfiguration().getLevelTargetTypes(
            typeId).isEmpty();
   }
   
   /** 
    * Convenience method to determine the content type
    * of the specified item.
    * 
    * @param id of the item.
    * 
    * @return content type id of the specified item.
    */
   private long getContentTypeId(IPSGuid id)
   {
      PSLocator loc = guidMgr.makeLocator(id);
      
      try
      {
         Object[] item = PSServerFolderProcessor.getItem(loc);
         
         return (Long) item[3];
      }
      catch (PSCmsException e)
      {
         ms_logger.error("Failed to get content type id for item: " +
               id.toString(), e);
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Gets the request from a request context.
    * @param reqCtx not null.
    * @return not null.
    * @throws IllegalArgumentException if reqCtx is null or 
    * cannot be casted.
    */
   private PSRequest getRequest(IPSRequestContext reqCtx)
   {
      if (!(reqCtx instanceof PSRequestContext))
         throw new IllegalArgumentException("request context must be an instance of PSRequestContext.");
      
      return ((PSRequestContext)reqCtx).getRequest();
   }

   /**
    * Performs touch items processing on all descendants of
    * the specified folder.
    * 
    * @param parent folder.
    * @param touchedItems collection of id's of items which
    * have already been touched.
    * @param folderLevel the level of the current folder relative
    * to the parent (0 - parent, 1 - child, 2 - grandchild).
    * 
    * @return number of new items which have been touched.
    */
   private int touchItems(PSLocator parent, Collection<Integer> touchedItems,
         int folderLevel)
   {
      if (folderLevel >= Math.abs(configuration.getMinimumLevel()))
      {
         return 0;
      }
      
      int currentLevel = folderLevel + 1;
      int count = 0;
      
      List<PSItemSummary> children = findFolderChildren(parent);
      for (PSItemSummary child : children)
      {
         IPSGuid guid = child.getGUID();
         if (child.getObjectType() == ObjectTypeEnum.FOLDER)
         {
            count += touchItems(guidMgr.makeLocator(
                  guid), touchedItems, currentLevel);            
         }
         else
         {
            Impact imp = getImpact(guid);
            if (imp.isImpactItem)
            {
               count += touchItems(guid, imp.levelTargetTypesMap, touchedItems,
                     true);
            }
         }
      }
      
      return count;
   }
   
   /**
    * {@inheritDoc}
    */
   public PSTouchItemConfiguration getConfiguration()
   {
      return configuration;
   }

   /**
    * {@inheritDoc}
    */
   public void setConfiguration(PSTouchItemConfiguration configuration)
   {
      this.configuration = configuration;
   }
   
}
