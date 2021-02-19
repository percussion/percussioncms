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

package com.percussion.rx.publisher.impl;

import static com.percussion.design.objectstore.PSRelationshipConfig.FILTER_TYPE_NONE;
import static com.percussion.design.objectstore.PSRelationshipConfig.TYPE_FOLDER_CONTENT;
import static com.percussion.util.IPSHtmlParameters.SYS_UNPUBLISH_CHANGED_LOCATION;
import static com.percussion.webservices.PSWebserviceUtils.getRelationshipProcessor;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.jexl.PSStringUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSDeliveryType;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.IPSSiteItem;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSStopwatch;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a utility class used to un-publish the items that are selected by a
 * content list and their published location have changed since last published.
 * The following scenarios are not covered by this class:
 * <ul>
 * <li>Item was published with different template.</li>
 * <li>Item was published with the same template, but the template changed from
 * paginated template to non-paginated.</li>
 * </ul>
 * The following scenarios are also not covered by this class, but are covered
 * by edition if its behavior is "Unpublish, then publish":
 * <ul>
 * <li>Items were purged.</li>
 * <li>Items were moved out of the site, but still exist.</li>
 * <li>Items are in archive state.</li>
 * </ul>
 */
public class PSLocationChangeHandler
{
   /**
    * Logger.
    */
   private static final Log ms_log = LogFactory
         .getLog(PSLocationChangeHandler.class);

   /**
    * It maps delivery name to its delivery type, used to cache the delivery
    * type during this process, never <code>null</code>, but may be empty.
    */
   private Map<String, IPSDeliveryType> m_typeMap = new HashMap<>();

   /**
    * Publishing service, never <code>null</code> and can be reset by .
    */
   private IPSPublisherService m_pubService = PSPublisherServiceLocator
         .getPublisherService();

   /**
    * The GUID manager, never <code>null</code>.
    */
   private IPSGuidManager m_guidMgr = PSGuidManagerLocator.getGuidMgr();;

   /**
    * The relationship proxy, never <code>null</code> after the constructor and
    * can be set by {@link #setRelationshipService(IRelationshipService)}.
    */
   private IRelationshipService m_relService = new RelationshipService();

   private IFolderProcessor m_folderProcessor = new FolderProcessor();
   
   /**
    * The publishing job that uses this handler to do un-publishing if there is
    * any.
    */
   private PSPublishingJob m_job;

   /**
    * Construct an instance.
    * 
    * @param job the owner of this handler.
    */
   public PSLocationChangeHandler(PSPublishingJob job)
   {
      m_job = job;
   }

   /**
    * Gets the un-publishing items for the specified (paginated) items.
    * 
    * @param pagedItems the paginated items. All items must be based on one
    *           content item, that is the content IDs are the same. It may not
    *           be <code>null</code>, but may be empty (do nothing in this
    *           case). Assumed the 1st element is the 1st page of the item.
    * 
    * @return the un-publishing items, never <code>null</code>, but may be
    *         empty.
    */

   /**
    * Creates the un-publishing items for a paginated item if the item's
    * location has been changed since last publishing.
    * 
    * @param pagedItems the paginated items. All items must be based on one
    *           content item, that is the content IDs are the same. It may not
    *           be <code>null</code>, but may be empty (do nothing in this
    *           case). Assumed the 1st element is the 1st page of the item.
    * 
    * @return the un-publishing items, never <code>null</code>, but may be
    *         empty.
    */
   public Collection<IPSAssemblyItem> getUnpublishPaginateedItems(
         List<IPSAssemblyItem> pagedItems) throws PSNotFoundException {
      if (pagedItems.isEmpty())
         return Collections.emptyList();

      IPSAssemblyItem item1 = pagedItems.get(0);
      Set<String> paths = getDeliveryPaths(pagedItems, item1.getId().getUUID());

      Collection<IPSSiteItem> siteItems = getRelatedSiteItems(item1);
      Map<Integer, List<IPSSiteItem>> idToSiteItems = mapIdToSiteItems(
            siteItems, null);

      IPSSiteItem theSItem = getLocationChangedSiteItem(item1, idToSiteItems,
            true);
      if (theSItem == null)
      {
         return getUnpublishExtraPages(item1, siteItems, paths);
      }

      List<IPSAssemblyItem> result = new ArrayList<>();
      for (IPSSiteItem sItem : siteItems)
      {
         if (!isSamePaginateSiteItem(theSItem, sItem))
            continue;

         addUnpublishItem(item1, sItem, result);
      }

      return result;
   }

   /**
    * Gets the un-publishing items for the specified (paginated) item where it
    * published extra pages in its last publishing (from/to the same folder). <br>
    * Note, this is called if the item is still publishing to the same location,
    * but the published pages may be less.
    * 
    * @param item1 the paginated item, assumed not <code>null</code>.
    * @param siteItems the site items that were published by the specified item,
    *           assumed not <code>null</code>, may be empty.
    * @param paths the published paths, gathered from above site items, never
    *           <code>null</code>, may be empty.
    * 
    * @return the un-publishing items, never <code>null</code>, may be empty.
    */
   private Collection<IPSAssemblyItem> getUnpublishExtraPages(
         IPSAssemblyItem item1, Collection<IPSSiteItem> siteItems,
         Set<String> paths) throws PSNotFoundException {
      List<IPSAssemblyItem> result = new ArrayList<>();
      for (IPSSiteItem sItem : siteItems)
      {
         if (isSamePublishItem(item1, sItem)
               && (!paths.contains(sItem.getLocation())))
         {
            addUnpublishItem(item1, sItem, result);
         }
      }

      return result;
   }

   /**
    * Determines if the work item and the site item have the same folder-id,
    * template-id, delivery-type. Assumed content-id, site-id and
    * delivery-context are the same already.
    * 
    * @param item the work item, assumed not <code>null</code>.
    * @param sItem the site item, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if they are "same"; otherwise return
    *         <code>false</code>.
    */
   private boolean isSamePublishItem(IPSAssemblyItem item, IPSSiteItem sItem)
   {
      return isSameFolder(item, sItem) && isSameTemplate(item, sItem)
            && item.getDeliveryType().equals(sItem.getDeliveryType());
   }

   /**
    * Gets all delivery paths from the specified items. This is also validating
    * all items, make sure their content IDs are equals to the specified one.
    * 
    * @param pagedItems the items, assumed not <code>null</code>, not empty.
    * @param contentId the content ID.
    * 
    * @return the delivery paths, not <code>null</code> or empty.
    */
   private Set<String> getDeliveryPaths(List<IPSAssemblyItem> pagedItems,
         int contentId)
   {
      Set<String> result = new HashSet<>();
      for (IPSAssemblyItem item : pagedItems)
      {
         if (item.getId().getUUID() != contentId)
            throw new IllegalArgumentException(
                  "Content ID must be the same for all paginated items");

         result.add(item.getDeliveryPath());
      }
      return result;
   }

   /**
    * Determines if the specified site items belong to the same paginated item,
    * that means both items have the same delivery-type, template-id, folder-id,
    * content-id, site-id and delivery-context, but page number may be
    * different. <br>
    * Note, assumed the content id, site id and delivery context are the same.
    * 
    * @param item1 the 1st site item in question, assumed not <code>null</code>.
    * @param item2 the 2nd site item in question, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the 2 site items belong to the same paginated
    *         item.
    */
   private boolean isSamePaginateSiteItem(IPSSiteItem item1, IPSSiteItem item2)
   {
      return StringUtils.equals(item1.getDeliveryType(), item2
            .getDeliveryType())
            && isSameTemplateId(item1, item2) && isSameFolderId(item1, item2);
   }

   /**
    * Gets the (previously published) site items for a specified item.
    * 
    * @param item the item in question, assumed not <code>null</code>.
    * 
    * @return the list of previously published site items, not <code>null</code>
    *         , but may be empty.
    */
   private Collection<IPSSiteItem> getRelatedSiteItems(IPSAssemblyItem item)
   {
      int contentId = item.getId().getUUID();
      int deliveryContext = item.getDeliveryContext();
      if (item.getPubServerId() != null)
      {
         IPSGuid pubServerId = new PSGuid(PSTypeEnum.PUBLISHING_SERVER, item.getPubServerId());
         return m_pubService.findServerItemsByIds(pubServerId, deliveryContext, Collections.singleton(contentId));
      }
      else
      {
         IPSGuid siteId = item.getSiteId();
         return m_pubService.findSiteItemsByIds(siteId, deliveryContext, Collections.singleton(contentId));
      }
   }
   
   
   /**
    * Collects the specified item for checking location change if needed.
    * 
    * @param item the item in question, assumed not <code>null</code>.
    */
   public boolean isPosssibleLocationChange(IPSAssemblyItem item)
   {
      if ((!item.isPublish()) || isBlank(item.getDeliveryPath()))
         return false;

      item.setParameterValue(SYS_UNPUBLISH_CHANGED_LOCATION, "true");
      return true;
   }

   /**
    * Creates un-publishing items from previously collected publishing items.
    * 
    * 
    * @param siteId the ID of the site, not <code>null</code>.
    * @param deliveryContext the delivery context used for the publishing.
    * @param contentlist the processing content list, not <code>null</code>.
    * @param unpublishKeys the key of the un-published site items that were
    *           processed at the beginning of the current job.
    * @param publishItems the items to be published that are used to calculate items 
    *  to be unpublished.
    * 
    * @return a list of un-publishing work items, never <code>null</code>, but
    *         may be empty.
    */
   public Collection<IPSAssemblyItem> getUnpublishingItems(IPSGuid siteId,
         int deliveryContext, IPSContentList contentlist,
         Set<PSSiteItemKey> unpublishKeys, Collection<IPSAssemblyItem> publishItems) throws PSNotFoundException {
      notNull(siteId);


      return getUnpublishingItemsSiteOrServer(siteId,
            deliveryContext, contentlist,
            unpublishKeys, publishItems, true);
   }

   /**
    * Creates un-publishing items from previously collected publishing items.
    * 
    * 
    * @param serverId the ID of the server, not <code>null</code>.
    * @param deliveryContext the delivery context used for the publishing.
    * @param contentlist the processing content list, not <code>null</code>.
    * @param unpublishKeys the key of the un-published site items that were
    *           processed at the beginning of the current job.
    * @param publishItems the items to be published that are used to calculate items 
    *  to be unpublished.
    * 
    * @return a list of un-publishing work items, never <code>null</code>, but
    *         may be empty.
    */
   public Collection<IPSAssemblyItem> getUnpublishingItemsByServer(IPSGuid serverId,
         int deliveryContext, IPSContentList contentlist,
         Set<PSSiteItemKey> unpublishKeys, Collection<IPSAssemblyItem> publishItems) throws PSNotFoundException {
      notNull(serverId);

      return getUnpublishingItemsSiteOrServer(serverId,
            deliveryContext, contentlist,
            unpublishKeys, publishItems, false);
   }

   /**
    * Creates un-publishing items from previously collected publishing items.
    * 
    * 
    * @param isSite the ID of the site, not <code>null</code>.
    * @param deliveryContext the delivery context used for the publishing.
    * @param contentlist the processing content list, not <code>null</code>.
    * @param unpublishKeys the key of the un-published site items that were
    *           processed at the beginning of the current job.
    * @param publishItems the items to be published that are used to calculate items 
    *  to be unpublished.
    * 
    * @return a list of un-publishing work items, never <code>null</code>, but
    *         may be empty.
    */
   public Collection<IPSAssemblyItem> getUnpublishingItemsSiteOrServer(IPSGuid objectId,
         int deliveryContext, IPSContentList contentlist,
         Set<PSSiteItemKey> unpublishKeys, Collection<IPSAssemblyItem> publishItems, boolean isSite) throws PSNotFoundException {
      notNull(objectId);

      if (publishItems.isEmpty())
         return publishItems;

      PSStopwatch swTemp = new PSStopwatch();
      swTemp.start();

      // processing the work items in chunks as a way to reduces memory usage
      int processedSize = 1000;

      List<IPSAssemblyItem> result = new ArrayList<>();
      List<IPSAssemblyItem> subResult = new ArrayList<>();
      for (IPSAssemblyItem item : publishItems)
      {
         if ( ! isPosssibleLocationChange(item) )
            continue;

         subResult.add(item);
         if (subResult.size() < processedSize)
            continue;

         result.addAll(createUnpublishItems(subResult, objectId, deliveryContext,
               unpublishKeys, false, isSite));
         subResult.clear();
      }

      if (!subResult.isEmpty())
      {
         result.addAll(createUnpublishItems(subResult, objectId, deliveryContext,
               unpublishKeys, false, isSite));
      }

      swTemp.stop();
      ms_log.debug("Process content list '" + contentlist.getName() + "' ("
            + contentlist.getGUID().getUUID() + ") detected " + result.size()
            + " location changes - unpublish " + swTemp.toString());

      return result;
   }

   /**
    * Creates un-publishing work items from the specified publishing items. The
    * un-publishing items are created when determines the publishing locations
    * have been changed since the last publishing.
    * 
    * @param items the publishing items in question, assumed not
    *           <code>null</code>.
    * @param isSite the ID of the site, not <code>null</code>.
    * @param deliveryContext the delivery context used for the publishing.
    * @param unpublishKeys the key of the un-published site items that were
    *           processed at the beginning of the current job.
    * @param isPaginated <code>true</code> if check for paginated item.
    * 
    * @return the created un-publishing work items, never <code>null</code>, may
    *         be empty.
    */
   private List<IPSAssemblyItem> createUnpublishItems(
         List<IPSAssemblyItem> items, IPSGuid objectId, int deliveryContext,
         Set<PSSiteItemKey> unpublishKeys, boolean isPaginated, boolean isSite) throws PSNotFoundException {
      Collection<Integer> ids = getIdsFromItems(items);
      Collection<IPSSiteItem> siteItems = isSite?m_pubService.findSiteItemsByIds(
            objectId, deliveryContext, ids):
               m_pubService.findServerItemsByIds(objectId, deliveryContext, ids);
      Map<Integer, List<IPSSiteItem>> idToItems = mapIdToSiteItems(siteItems,
            unpublishKeys);

      return createUnpublishItems(items, idToItems, isPaginated);
   }

   /**
    * Creates un-publishing work items from the specified items and related site
    * items.
    * 
    * @param items the work item, assumed not <code>null</code>.
    * @param idToSiteItems a map that maps the content ID to a list of site
    *           items. Assumed not <code>null</code>.
    * @param isPaginated <code>true</code> if check for paginated item.
    * 
    * @return the created un-publishing work item, never <code>null</code>, may
    *         be empty.
    * 
    * @see #addUnpublishItem(IPSAssemblyItem, IPSSiteItem, List)
    */
   private List<IPSAssemblyItem> createUnpublishItems(
         Collection<IPSAssemblyItem> items,
         Map<Integer, List<IPSSiteItem>> idToSiteItems, boolean isPaginated) throws PSNotFoundException {
      List<IPSAssemblyItem> result = new ArrayList<>();

      for (IPSAssemblyItem item : items)
      {
         IPSSiteItem sItem = getLocationChangedSiteItem(item, idToSiteItems,
               isPaginated);
         if (sItem != null)
         {
            addUnpublishItem(item, sItem, result);
         }
      }

      return result;
   }

   /**
    * Gets a site item which is relate to the specified work item, but its
    * published location is different. Assumed site and delivery context are the
    * same for both the work item and the site items. The returned site item
    * matches the following criteria:
    * <ul>
    * <li>The location is different with the work item</li>
    * <li>The template ID is the same with the work item</li>
    * <li>The page number is the same with the work item</li>
    * <li>The delivery type is the same with the work item</li>
    * <li>The folder ID is same with the work item <b>OR</b></li>
    * <li>The work item is under one folder and there is only one site item with
    * the same content ID <b>OR</b></li>
    * <li>The work item and the site item (with the same content ID) are under
    * the same set of folders, except one folder.</li>
    * </ul>
    * 
    * @param item the work item, assumed not <code>null</code>.
    * @param idToSiteItems a map that maps the content ID to a list of site
    *           items. Assumed not <code>null</code>.
    * @param isPaginated <code>true</code> if check for paginated item.
    * 
    * @return the site item described above. It may be <code>null</code> if
    *         cannot find such site item.
    */
   private IPSSiteItem getLocationChangedSiteItem(IPSAssemblyItem item,
         Map<Integer, List<IPSSiteItem>> idToSiteItems, boolean isPaginated)
   {
      List<IPSSiteItem> sItems = idToSiteItems.get(item.getId().getUUID());
      if (sItems == null)
         return null;

      for (IPSSiteItem sItem : sItems)
      {
         if (!StringUtils.equals(item.getDeliveryType(), sItem
               .getDeliveryType()))
            continue;

         if (item.getDeliveryPath().equals(sItem.getLocation()))
            return null;

         if (!isSameTemplate(item, sItem))
            continue;

         // When we are checking in content list item has no page id
         // If site item has page id unpublishing will be handled in page expander
         // in delivery so we skip here (site item will not match 0)  
         // If we are in page expander (isPagenated=true) we will skip
         // if the current item page does not match the site item page.
         if (!isSamePage(item, sItem))
            continue;

         if (isSameFolder(item, sItem) || isSameButMovedFolder(item, sItem))
            return sItem;
      }

      return getPublishedAndMovedItem(item, sItems, isPaginated);
   }

   /**
    * Gets the site item (or published log entry) of the specified work item
    * that has been moved since last publishing. The site item is determined by
    * the following criteria:
    * <ul>
    * <li>The item is under the same number of folders as it was published to
    * the current site</li>. If the item is also under different site folder(s),
    * those folder(s) will not be considered in this calculation.
    * <li>The item are still under the same folders as it was published, except
    * one folder</li>
    * <li>The template ID of the site item is the same as the work item</li>
    * </ul>
    * Note, this is called by
    * {@link #getLocationChangedSiteItem(IPSAssemblyItem, Map, boolean)} in
    * which it cannot find the publishing path of the item matches any of the
    * previously published site items. An item may publish more than ones into
    * the same folder with the following scenarios:
    * <ul>
    * <li>A paginated item publishes more than one page into the same folder</li>
    * <li>A non-paginated item publishes more one page into the same folder with
    * different templates, one page per template.</li>
    * </ul>
    * Un-publishing a site item is undetermined when an item was published into
    * more than one folder, but it is moved or removed from those folders,
    * because we don't keep track the original folders. Fortunately, the edition
    * level un-publishing behavior is able to handle this scenario.
    * 
    * @param item the publishing item, assumed not <code>null</code>.
    * @param sItems the publish log entries of the item that were last published
    *           to the current site, assumed not <code>null</code>, but may be
    *           empty.
    * @param isPaginated <code>true</code> if check for paginated item.
    * 
    * @return the last published site item that the item has been moved from. It
    *         may be <code>null</code> if cannot find one that meets the
    *         criteria described above.
    */
   private IPSSiteItem getPublishedAndMovedItem(IPSAssemblyItem item,
         List<IPSSiteItem> sItems, boolean isPaginated)
   {
      Set<Integer> curParentIds = getParentFolderIds(item);

      Map<Integer, List<IPSSiteItem>> pubId2Item = groupItemsByFolderIds(sItems);
      Set<Integer> putlishedFolderIds = pubId2Item.keySet();

      if (curParentIds.size() != putlishedFolderIds.size())
      {
         curParentIds = limitToSite(curParentIds, item.getSiteId().getUUID());
         if (curParentIds.size() != putlishedFolderIds.size())
            return null;
      }

      int count = 0;
      IPSSiteItem movedItem = null;
      for (Integer pubId : putlishedFolderIds)
      {
         if (curParentIds.contains(pubId))
         {
            count++;
         }
         else
         {
            for (IPSSiteItem sItem : pubId2Item.get(pubId))
            {
               if (isSameTemplate(item, sItem))
               {
                  if (isPaginated
                        || ((!isPaginated) && isSamePage(item, sItem)))
                  {
                     movedItem = sItem;
                     break;
                  }
               }
            }
         }
      }
      return (count + 1) == putlishedFolderIds.size() ? movedItem : null;
   }

   /**
    * Filter the specified folder IDs, get the folders under the specified site.
    * 
    * @param parentIds the folder IDs in question, assumed not <code>null</code>.
    * @param siteId the site ID in UUID format, assumed not <code>null</code>.
    * 
    * @return the folder IDs under the site, never <code>null</code>, may be empty.
    */
   private Set<Integer> limitToSite(Set<Integer> parentIds, Integer siteId)
   {
      Set<Integer> result = new HashSet<>();
      for (Integer id : parentIds)
      {
         if (isUnderSite(siteId, id))
            result.add(id);
      }
      return result;
   }

   /**
    * Determines if a specified folder is under the specified site.
    * 
    * @param siteId the site ID in UUID format, assumed not <code>null</code>.
    * @param folderId the folder ID in question, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the folder is or under the site.
    */
   private boolean isUnderSite(Integer siteId, Integer folderId)
   {
      if (siteId.intValue() == folderId.intValue())
         return true;
      
      List<List<PSLocator>> paths = m_folderProcessor.getFolderLocatorPaths(new PSLocator(folderId, 1));
      if (paths.isEmpty())
         return false;
         
      List<PSLocator> locators = paths.get(0);
      for (PSLocator loc : locators)
      {
         if (loc.getId() == siteId)
            return true;
      }
      return false;
   }
   
   /**
    * Group the specified site items by folder IDs. Assumed the content IDs,
    * site and delivery context of the site items are the same. This is used to
    * process a paginated item, which may publish to the same folder more than
    * ones can be the following scenarios:
    * <ul>
    * <li>Different template</li>
    * <li>Same template, but different page number for paginated item</li>
    * </ul>
    * 
    * @param siteItems the site items, assumed not <code>null</code>, may be
    *           empty.
    * 
    * @return a map that maps the folder ID to site items published to the same
    *         folder, never <code>null</code>, but may be empty.
    */
   private Map<Integer, List<IPSSiteItem>> groupItemsByFolderIds(
         List<IPSSiteItem> siteItems)
   {
      Map<Integer, List<IPSSiteItem>> result = new HashMap<>();
      for (IPSSiteItem item : siteItems)
      {
         Integer id = item.getFolderId() == null ? 0 : item.getFolderId();
         List<IPSSiteItem> items = result.get(id);
         if (items == null)
         {
            items = new ArrayList<>();
            result.put(id, items);
         }
         items.add(item);
      }
      return result;
   }

   /**
    * Gets the parent folder IDs that the specified item is under.
    * 
    * @param item the item in question, assumed not <code>null</code>.
    * 
    * @return a collection of folder IDs, never <code>null</code>, but may be
    *         empty.
    */
   private Set<Integer> getParentFolderIds(IPSAssemblyItem item)
   {
      PSLocator iloc = ((PSLegacyGuid) item.getId()).getLocator();
      Set<Integer> ids = new HashSet<>();
      List<PSLocator> locs = m_relService.getParents(TYPE_FOLDER_CONTENT, iloc,
            FILTER_TYPE_NONE);
      for (PSLocator loc : locs)
      {
         ids.add(loc.getId());
      }
      return ids;
   }

   /**
    * Determines if the specified item and the site item have the same folder
    * ID.
    * 
    * @param item the item, assumed not <code>null</code>.
    * @param sItem the (published) site item, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the item is still under the same folder as it
    *         was published to; otherwise return <code>false</code>.
    */
   private boolean isSameFolder(IPSAssemblyItem item, IPSSiteItem sItem)
   {
      if (sItem.getFolderId() == null)
         return false;

      return item.getFolderId() == sItem.getFolderId().intValue();
   }

   /**
    * Determines if the folder of specified item has been moved since its
    * last publishing (recorded by the specified site item).
    * After moving a folder, the folder ID itself and its descendants
    * of the related site items are set to negative of its original value. 
    * 
    * @param item the item, assumed not <code>null</code>.
    * @param sItem the (last published) site item, assumed not 
    * <code>null</code>.
    * 
    * @return <code>true</code> if the folder ID of the site item equals to the 
    * negative value of the folder ID of the item; otherwise return 
    * <code>false</code>.
    */
   private boolean isSameButMovedFolder(IPSAssemblyItem item, IPSSiteItem sItem)
   {
      if (sItem.getFolderId() == null)
         return false;

      if (sItem.getFolderId().intValue() < 0)
         return item.getFolderId() == sItem.getFolderId().intValue() * -1;
      else
         return false;
   }

   /**
    * Determines if the page number of the specified work item is the same as
    * the specified site item. <br>
    * Note, for non-paginated items, the page number is always <code>0</code>,
    * but for paginated items, the page number starts from <code>1</code>, so
    * the page number is greater than <code>0</code> for paginated site items.
    * However, during executing content list, the page number is always
    * <code>0</code> for both paginated or non-paginated items because the
    * paginated items can only be recognized during assembly item.
    * 
    * 
    * @param item the work item, assumed not <code>null</code>.
    * @param sItem the site item, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the page numbers are the same.
    */
   private boolean isSamePage(IPSAssemblyItem item, IPSSiteItem sItem)
   {
      int page = item.getPage() == null ? 0 : item.getPage().intValue();
      return page == sItem.getPage();
   }

   /**
    * Determines if the template IDs are the same for the given site items.
    * 
    * @param item1 the 1st site item, assumed not <code>null</code>.
    * @param item2 the 2nd site item, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the template IDs are the same.
    */
   private boolean isSameTemplateId(IPSSiteItem item1, IPSSiteItem item2)
   {
      long id1 = item1.getTemplateId() == null ? 0 : item1.getTemplateId()
            .longValue();
      long id2 = item2.getTemplateId() == null ? 0 : item2.getTemplateId()
            .longValue();
      return id1 == id2;
   }

   /**
    * Determines if the folder IDs are the same for the given site items.
    * 
    * @param item1 the 1st site item, assumed not <code>null</code>.
    * @param item2 the 2nd site item, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the folder IDs are the same.
    */
   private boolean isSameFolderId(IPSSiteItem item1, IPSSiteItem item2)
   {
      int id1 = item1.getFolderId() == null ? 0 : item1.getFolderId()
            .intValue();
      int id2 = item2.getFolderId() == null ? 0 : item2.getFolderId()
            .intValue();
      return id1 == id2;
   }

   /**
    * Determines if the specified work item is using the same template as the
    * published site item.
    * 
    * @param item the work item, assumed not <code>null</code>.
    * @param sItem the site item, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the template IDs are the same in both the
    *         work item and published site item.
    */
   private boolean isSameTemplate(IPSAssemblyItem item, IPSSiteItem sItem)
   {
      if (sItem.getTemplateId() == null)
         return false;

      return sItem.getTemplateId().intValue() == getTemplateIdFromItem(item);
   }

   /**
    * Gets the template ID from the specified assembly item.
    * 
    * @param item the assembly item in question, assumed not <code>null</code>.
    * 
    * @return the template ID, it may be <code>-1</code> if template is unknown.
    */
   private int getTemplateIdFromItem(IPSAssemblyItem item)
   {
      if (item.getTemplate() != null)
      {
         return item.getTemplate().getGUID().getUUID();
      }
      String sTemplateId = item.getParameterValue(
            IPSHtmlParameters.SYS_TEMPLATE, null);
      int templateId = convertToNumber(sTemplateId);
      if (templateId != -1)
         return templateId;

      String url = item.getAssemblyUrl();
      // Get the query portion of the url
      int q = url != null ? url.indexOf('?') : -1;
      if (q <= 0)
         return -1;

      String query = url.substring(q + 1);
      try
      {
         PSStringUtils sutils = new PSStringUtils();
         Map<String, String> params = sutils.stringToMap(query);
         templateId = convertToNumber(params
               .get(IPSHtmlParameters.SYS_TEMPLATE));
         if (templateId != -1)
            return templateId;

         templateId = convertToNumber(IPSHtmlParameters.SYS_VARIANTID);
         if (templateId != -1)
            return templateId;

         templateId = convertToNumber(IPSHtmlParameters.SYS_VARIANT);
         if (templateId != -1)
            return templateId;

         ms_log.error("Failed to get template ID from assembly URL: " + url);
         return -1;
      }
      catch (UnsupportedEncodingException e)
      {
         ms_log.error("Failed to get template ID from assembly URL: " + url, e);
         return -1;
      }
   }

   /**
    * Quietly convert a string to integer.
    * 
    * @param number the string representation of an integer.
    * 
    * @return the converted integer, it may be <code>-1</code> if failed to
    *         convert the number or the number is <code>null</code>.
    */
   private int convertToNumber(String number)
   {
      if (isBlank(number))
         return -1;

      try
      {
         return Integer.parseInt(number);
      }
      catch (NumberFormatException e)
      {
         return -1;
      }
   }

   /**
    * Creates an un-publishing item from the specified publishing item and its
    * site item.
    * 
    * @param item the publishing item, assumed not <code>null</code>.
    * @param sItem the site item, assumed not <code>null</code>.
    * @param queuedItems a list of work items, used to collect the created
    *           un-publishing items for the caller, assumed not
    *           <code>null</code>.
    * 
    * @return the un-publishing item, never <code>null</code>.
    */
   private IPSAssemblyItem addUnpublishItem(IPSAssemblyItem item,
         IPSSiteItem sItem, List<IPSAssemblyItem> queuedItems) throws PSNotFoundException {
      IPSDeliveryType dtype = getDeliveryType(sItem.getDeliveryType());
      IPSAssemblyItem clone = (IPSAssemblyItem) item.pageClone(); // item.clone();

      clone.setPublish(false);
      clone.setUnpublishRefId(sItem.getSiteItem().referenceId);
      clone.setDeliveryPath(sItem.getLocation());
      clone.setAssemblyUrl(sItem.getContentUrl());
      clone.setPage(sItem.getPage());
      if (sItem.getFolderId() != null && sItem.getFolderId() != 0)
         clone.setFolderId(sItem.getFolderId());

      clone.setId(m_guidMgr.makeGuid(new PSLocator(sItem.getContentId(), sItem
            .getRevisionId())));

      m_job.processAndQueueUnpublishItem(clone.getDeliveryContext(), sItem
            .getSiteItem().unpublishInfo, clone, dtype, queuedItems);

      return clone;
   }

   /**
    * Gets the delivery type from delivery type name.
    * 
    * @param deliveryType the delivery type name, assumed not <code>null</code>.
    * 
    * @return the delivery type, never <code>null</code>.
    */
   private IPSDeliveryType getDeliveryType(String deliveryType) throws PSNotFoundException {
      IPSDeliveryType dtype = m_typeMap.get(deliveryType);
      if (dtype != null)
         return dtype;

      dtype = m_pubService.loadDeliveryType(deliveryType);
      m_typeMap.put(deliveryType, dtype);
      return dtype;
   }

   /**
    * Gets the content IDs from the specified items.
    * 
    * @param items the items, not <code>null</code>, but may be empty.
    * 
    * @return the content IDs, never <code>null</code>, but may be empty.
    */
   private Collection<Integer> getIdsFromItems(Collection<IPSAssemblyItem> items)
   {
      Collection<Integer> result = new HashSet<>();
      for (IPSAssemblyItem item : items)
      {
         result.add(item.getId().getUUID());
      }

      return result;
   }

   /**
    * Map content IDs to the related site items.
    * 
    * @param items the site items, assumed not <code>null</code>.
    * @param unpublishKeys the keys of the unpublished items, which were
    *           processed at the beginning of the job. It may be
    *           <code>null</code> or empty.
    * 
    * @return the ID / item map, never <code>null</code>.
    */
   private Map<Integer, List<IPSSiteItem>> mapIdToSiteItems(
         Collection<IPSSiteItem> items, Set<PSSiteItemKey> unpublishKeys)
   {
      Map<Integer, List<IPSSiteItem>> result = new HashMap<>();
      for (IPSSiteItem item : items)
      {
         if (unpublishKeys != null && (!unpublishKeys.isEmpty()))
         {
            PSSiteItemKey k = new PSSiteItemKey(item);
            if (unpublishKeys.contains(k))
               continue;
         }

         Integer contentId = item.getContentId();
         List<IPSSiteItem> pubItems = result.get(contentId);
         if (pubItems == null)
         {
            pubItems = new ArrayList<>();
            result.put(contentId, pubItems);
         }
         pubItems.add(item);
      }

      return result;
   }

   /**
    * The relationship service proxy, as a way for unit test to create its own
    * mock service.
    */
   interface IRelationshipService
   {
      /**
       * The same as
       * {@link PSRelationshipProcessor#getParents(String, PSKey, int)}.
       */
      List<PSLocator> getParents(String type, PSLocator object,
            int doNotApplyFilters);
   }

   /**
    * Implements {@link IRelationshipService}
    */
   class RelationshipService implements IRelationshipService
   {
      private PSRelationshipProcessor m_processor = getRelationshipProcessor();

      /*
       * (non-Javadoc)
       * 
       * @see IRelationshipService#getParents(String, PSLocator, int)
       */
      @SuppressWarnings("unchecked")
      public List<PSLocator> getParents(String type, PSLocator object,
            int doNotApplyFilters)
      {
         try
         {
            return m_processor.getParents(type, object, doNotApplyFilters);
         }
         catch (PSCmsException e)
         {
            throw new RuntimeException("Failed to retrieve folders for item ",
                  e);
         }
      }
   }

   /**
    * Set the relationship service proxy. This is only used by unit test to
    * provide its own mock service.
    * 
    * @param relService the new relationship service, not <code>null</code>.
    */
   void setRelationshipService(IRelationshipService relService)
   {
      notNull(relService);

      m_relService = relService;
   }

   /**
    * Set the publisher service, used by unit test so that it can provide 
    * its own mock service (through this interface).
    * 
    * @param pubService the new publisher service, not <code>null</code>.
    */
   void setPublisherService(IPSPublisherService pubService)
   {
      notNull(pubService);

      m_pubService = pubService;
   }

   void setFolderProcessor(IFolderProcessor processor)
   {
      notNull(processor);
      
      m_folderProcessor = processor;
   }
   
   /**
    * This is a proxy of the folder processor, used by unit test so that it 
    * can provide its own mock service (through this interface).
    */
   interface IFolderProcessor
   {
      /**
       * This is the same as {@link PSServerFolderProcessor#getFolderLocatorPaths(PSLocator)}.
       */
      List<List<PSLocator>> getFolderLocatorPaths(PSLocator loc);
   }
   
   /**
    * Implements {@link IFolderProcessor}
    */
   class FolderProcessor implements IFolderProcessor
   {
      private PSServerFolderProcessor m_processor;
      
      FolderProcessor()
      {
         m_processor = getFolderProcessor();
      }
      
      public List<List<PSLocator>> getFolderLocatorPaths(PSLocator loc)
      {
         try
         {
            return m_processor.getFolderLocatorPaths(loc);
         }
         catch (PSCmsException e)
         {
            throw new RuntimeException("Failed to get folder locator paths", e);
         }
      }
   }

   /**
    * Creates a folder processor instance from the current request, which
    * is hidden in the current thread.
    * 
    * @return the created folder processor, never <code>null</code>.
    */
   private PSServerFolderProcessor getFolderProcessor()
   {
      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      return PSServerFolderProcessor.getInstance();
   }


}
