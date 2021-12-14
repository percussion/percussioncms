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

package com.percussion.rx.publisher.impl;

import com.google.common.collect.Lists;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSDeliveryType;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.IPSSiteItem;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.publisher.data.PSContentList;
import com.percussion.services.publisher.data.PSPubItem;
import com.percussion.services.publisher.data.PSSiteItem;
import com.percussion.services.publisher.impl.PSPublisherService;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSStopwatch;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Testing {@link PSLocationChangeHandler}. Mock services are used for all test
 * cases.
 */
@Category(IntegrationTest.class)
public class PSLocationChangeHandlerTest extends ServletTestCase
{
   static int EI_SITE_ID = 301;
   static int CI_SITE_ID = 303;

   static IPSGuid EI_SITE_GUID = new PSGuid(PSTypeEnum.SITE, EI_SITE_ID);

   static IPSGuid CI_SITE_GUID = new PSGuid(PSTypeEnum.SITE, CI_SITE_ID);

   static IPSContentList TEST_CLIST = getSampleCList();

   static int CONTENT_ID = 300;

   static int FOLDER_ID = 123;

   static int FOLDER_ID_2 = 223;

   static long TEMPLATE_ID = 123L;

   static long TEMPLATE_ID_2 = 124L;

   static String LOCATION_1 = "location_1";

   static String LOCATION_2 = "location_2";

   PSLocationChangeHandler m_handler;

   RelationshipService m_relService;

   PublishService m_pubService;

   FolderProcessor m_folderProcessor;
   
   @Override
   protected void setUp()
   {
      PSPublishingJob job = new PSPublishingJob();
      m_handler = new PSLocationChangeHandler(job);

      m_relService = new RelationshipService();
      m_pubService = new PublishService();
      m_folderProcessor = new FolderProcessor();
      
      m_handler.setRelationshipService(m_relService);
      m_handler.setPublisherService(m_pubService);
      m_handler.setFolderProcessor(m_folderProcessor); 
   }

   /**
    * Testing all no un-publish scenarios for non-paginated item
    * 
    * @throws Exception if an error occurs.
    */
   public void testNoUnpublish_NonPaginate() throws Exception
   {
      // [1]      
      // no related site item, nothing to unpublish
      IPSAssemblyItem item = createItem(LOCATION_1, TEMPLATE_ID);

      Collection<IPSAssemblyItem> items = m_handler.getUnpublishingItems(
            EI_SITE_GUID, 1, TEST_CLIST, null, Lists.newArrayList(item));
      assertTrue(items.isEmpty());

      // [2]
      // same location/template/deliveryType/folder-id, nothing to unpublish
      IPSSiteItem sItem = createSiteItem(LOCATION_1, TEMPLATE_ID);
      m_pubService.addSiteItem(sItem);

      items = m_handler.getUnpublishingItems(EI_SITE_GUID, 1, TEST_CLIST, null, Lists.newArrayList(item));
      assertTrue(items.isEmpty());

      // [3]
      // different delivery-type, same location/template/folder-id, nothing to
      // unpublish
      m_pubService.clearSiteItems();
      sItem = createSiteItem(CONTENT_ID, FOLDER_ID, LOCATION_1, TEMPLATE_ID,
            "ftp");
      m_pubService.addSiteItem(sItem);

      items = m_handler.getUnpublishingItems(EI_SITE_GUID, 1, TEST_CLIST, null, Lists.newArrayList(item));
      assertTrue(items.isEmpty());

      // [4]
      // different template/location, same folder-id, nothing to unpublish
      m_pubService.clearSiteItems();
      sItem = createSiteItem(CONTENT_ID, FOLDER_ID, LOCATION_2, TEMPLATE_ID_2,
            "filesystem");
      m_pubService.addSiteItem(sItem);

      items = m_handler.getUnpublishingItems(EI_SITE_GUID, 1, TEST_CLIST, null, Lists.newArrayList(item));
      assertTrue(items.size() == 0);

      // [5]
      // different folder-id/location, same template, nothing to unpublish
      m_pubService.clearSiteItems();
      sItem = createSiteItem(CONTENT_ID, FOLDER_ID_2, LOCATION_2, TEMPLATE_ID,
            "filesystem");
      m_pubService.addSiteItem(sItem);

      items = m_handler.getUnpublishingItems(EI_SITE_GUID, 1, TEST_CLIST, null, Lists.newArrayList(item));
      assertTrue(items.size() == 0);

      // [6]
      // different location, same folder-id/template, but the item already
      // handled by edition un-publishing behavior, nothing to unpublish
      sItem = createSiteItem(LOCATION_2, TEMPLATE_ID);
      m_pubService.clearSiteItems();
      m_pubService.addSiteItem(sItem);

      PSSiteItemKey key = new PSSiteItemKey(sItem);
      Set<PSSiteItemKey> unpubs = new HashSet<PSSiteItemKey>();
      unpubs.add(key);
      items = m_handler.getUnpublishingItems(EI_SITE_GUID, 1, TEST_CLIST,
            unpubs, Lists.newArrayList(item));
      assertTrue(items.size() == 0);

      // [7]
      // 1 item, but moved from more than one folder since last publishing
      // un-publishing is un-determined, nothing to unpublish

      m_pubService.clearSiteItems();
      sItem = createSiteItem(CONTENT_ID, FOLDER_ID_2, LOCATION_2, TEMPLATE_ID,
            "filesystem");
      m_pubService.addSiteItem(sItem);
      sItem = createSiteItem(CONTENT_ID, FOLDER_ID_2 + 2, LOCATION_2 + "hello",
            TEMPLATE_ID, "filesystem");
      m_pubService.addSiteItem(sItem);
      setParentIds(FOLDER_ID);

      items = m_handler.getUnpublishingItems(EI_SITE_GUID, 1, TEST_CLIST, null, Lists.newArrayList(item));
      assertTrue(items.size() == 0);

      // [8]
      // 2 same items, but moved from more than one folder since last publishing
      // un-publishing is un-determined, nothing to unpublish
      List<IPSAssemblyItem> pubItems = Lists.newArrayList(item);
      item = createItem(CONTENT_ID, FOLDER_ID_2, LOCATION_2, TEMPLATE_ID,
            "filesystem");
      pubItems.add(item);

      m_pubService.clearSiteItems();
      sItem = createSiteItem(CONTENT_ID, FOLDER_ID + 1, LOCATION_1 + "a",
            TEMPLATE_ID, "filesystem");
      m_pubService.addSiteItem(sItem);
      sItem = createSiteItem(CONTENT_ID, FOLDER_ID_2 + 1, LOCATION_2 + "a",
            TEMPLATE_ID, "filesystem");
      m_pubService.addSiteItem(sItem);
      setParentIds(FOLDER_ID, FOLDER_ID_2);

      items = m_handler.getUnpublishingItems(EI_SITE_GUID, 1, TEST_CLIST, null, pubItems);
      assertTrue(items.size() == 0);

   }

   private void setParentIds(Integer... folderIds)
   {
      List<Integer> parentIds = new ArrayList<Integer>();
      m_folderProcessor.clear();
      
      for (Integer id : folderIds )
      {
         parentIds.add(id);
         addLocatorPath(EI_SITE_ID, id);
      }

      m_relService.setParents(parentIds);
   }

   /**
    * Testing all un-publish scenarios for non-paginated item
    * 
    * @throws Exception if an error occurs.
    */
   public void testUnpublish_NonPaginate() throws Exception
   {
      // [1]
      // different location, same everything else, un-publish 1
      
      IPSAssemblyItem item = createItem(LOCATION_1, TEMPLATE_ID);
      List<IPSAssemblyItem> pubItems = Lists.newArrayList(item);

      m_pubService.clearSiteItems();
      IPSSiteItem sItem = createSiteItem(LOCATION_2, TEMPLATE_ID);
      m_pubService.addSiteItem(sItem);

      Collection<IPSAssemblyItem> items = m_handler.getUnpublishingItems(
            EI_SITE_GUID, 1, TEST_CLIST, null, pubItems);
      assertTrue(items.size() == 1);

      // [2]
      // different location & folder-id, same template-id & delivery-type,
      // un-publish
      m_pubService.clearSiteItems();
      sItem = createSiteItem(CONTENT_ID, FOLDER_ID_2, LOCATION_2, TEMPLATE_ID,
            "filesystem");
      m_pubService.addSiteItem(sItem);
      setParentIds(FOLDER_ID);

      items = m_handler.getUnpublishingItems(EI_SITE_GUID, 1, TEST_CLIST, null, pubItems);
      assertTrue(items.size() == 1);

      // [3]
      // same as [2], but the folder-id of the site item is negative of current 
      // folder-id. this happens after the folder has been moved.
      // un-publish
      m_pubService.clearSiteItems();
      sItem = createSiteItem(CONTENT_ID, FOLDER_ID * -1, LOCATION_2, TEMPLATE_ID,
            "filesystem");
      m_pubService.addSiteItem(sItem);
      setParentIds(FOLDER_ID);

      items = m_handler.getUnpublishingItems(EI_SITE_GUID, 1, TEST_CLIST, null, pubItems);
      assertTrue(items.size() == 1);
      
      // [4]
      // 1 same location / folder-id, template-id & delivery-type
      // 1 different location & folder-id, same template-id & delivery-type,
      // unpublish
      m_pubService.clearSiteItems();
      sItem = createSiteItem(LOCATION_1, TEMPLATE_ID);
      m_pubService.addSiteItem(sItem);
      sItem = createSiteItem(CONTENT_ID, FOLDER_ID_2 + 1, LOCATION_2 + "a", TEMPLATE_ID, "filesystem");
      m_pubService.addSiteItem(sItem);

      item = createItem(CONTENT_ID, FOLDER_ID_2, LOCATION_2, TEMPLATE_ID, "filesystem");
      pubItems.add(item);

      setParentIds(FOLDER_ID, FOLDER_ID_2); 

      items = m_handler.getUnpublishingItems(EI_SITE_GUID, 1, TEST_CLIST, null, pubItems);
      assertTrue(items.size() == 1);
      
      // [5]
      // same as above [4], but same item is also under different (CI) site
      m_relService.addParent(FOLDER_ID_2 + 1);
      addLocatorPath(CI_SITE_ID, FOLDER_ID_2 + 1);

      items = m_handler.getUnpublishingItems(EI_SITE_GUID, 1, TEST_CLIST, null, pubItems);
      assertTrue(items.size() == 1);
   }

   private void addLocatorPath(int siteId, int folderId)
   {
      List<PSLocator> path = new ArrayList<PSLocator>();
      path.add(new PSLocator(siteId, 1));
      
      PSLocator folderLoc = new PSLocator(folderId, 1);
      path.add(folderLoc);

      m_folderProcessor.addPath(path, folderLoc);
   }
   
   /**
    * Testing all no un-publish scenarios for paginated item
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testNoUnpublish_Paginate() throws Exception
   {
      // no site item found, no un-publishing
      IPSSiteItem sItem = createSiteItem(LOCATION_1, TEMPLATE_ID, 1);
      m_pubService.addSiteItem(sItem);
      IPSAssemblyItem item = createItem(LOCATION_1, TEMPLATE_ID, 1);
      List<IPSAssemblyItem> items = Collections.singletonList(item);

      Collection<IPSAssemblyItem> unpubItems = m_handler
            .getUnpublishPaginateedItems(items);
      assertTrue(unpubItems.size() == 0);

      // same everything, no un-publishing
      m_pubService.clearSiteItems();
      sItem = createSiteItem(LOCATION_1, TEMPLATE_ID, 1);
      m_pubService.addSiteItem(sItem);
      item = createItem(LOCATION_1, TEMPLATE_ID, 1);

      unpubItems = m_handler.getUnpublishPaginateedItems(items);
      assertTrue(unpubItems.size() == 0);

      // different location & template, same everything else, no un-publishing
      m_pubService.clearSiteItems();
      sItem = createSiteItem(LOCATION_2, TEMPLATE_ID - 2, 1);
      m_pubService.addSiteItem(sItem);
      item = createItem(LOCATION_1, TEMPLATE_ID, 1);

      unpubItems = m_handler.getUnpublishPaginateedItems(items);
      assertTrue(unpubItems.size() == 0);

      // different location & delivery-type, same everything else, no
      // un-publishing
      m_pubService.clearSiteItems();
      sItem = createSiteItem(CONTENT_ID, FOLDER_ID, LOCATION_2, TEMPLATE_ID,
            "ftp", 1);
      m_pubService.addSiteItem(sItem);
      item = createItem(LOCATION_1, TEMPLATE_ID, 1);

      unpubItems = m_handler.getUnpublishPaginateedItems(items);
      assertTrue(unpubItems.size() == 0);
   }

   /**
    * Testing all un-publish scenarios for paginated item
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testUnpublish_Paginate() throws Exception
   {
      // different location, same everything else, un-publishing
      IPSSiteItem sItem = createSiteItem(LOCATION_2, TEMPLATE_ID, 1);
      m_pubService.addSiteItem(sItem);
      sItem = createSiteItem(LOCATION_2 + "_2", TEMPLATE_ID, 2);
      m_pubService.addSiteItem(sItem);
      IPSAssemblyItem item = createItem(LOCATION_1, TEMPLATE_ID, 1);
      List<IPSAssemblyItem> items = Collections.singletonList(item);
      setParentIds(FOLDER_ID);

      Collection<IPSAssemblyItem> unpubItems = m_handler
            .getUnpublishPaginateedItems(items);
      assertTrue(unpubItems.size() == 2);

      // different location and folder-id, same everything else, un-publishing
      m_pubService.clearSiteItems();
      sItem = createSiteItem(CONTENT_ID, FOLDER_ID_2, LOCATION_2, TEMPLATE_ID,
            "filesystem", 1);
      m_pubService.addSiteItem(sItem);
      sItem = createSiteItem(CONTENT_ID, FOLDER_ID_2, LOCATION_2 + "_2",
            TEMPLATE_ID, "filesystem", 2);
      m_pubService.addSiteItem(sItem);
      sItem = createSiteItem(CONTENT_ID, FOLDER_ID_2, LOCATION_2 + "_3",
            TEMPLATE_ID, "filesystem", 3);
      m_pubService.addSiteItem(sItem);
      item = createItem(LOCATION_1, TEMPLATE_ID, 1);

      setParentIds(FOLDER_ID);

      unpubItems = m_handler.getUnpublishPaginateedItems(items);
      assertTrue(unpubItems.size() == 3);

      // same everything, but published more pages, un-publishing the extra
      // pages
      m_pubService.clearSiteItems();
      sItem = createSiteItem(CONTENT_ID, FOLDER_ID, LOCATION_1, TEMPLATE_ID,
            "filesystem", 1);
      m_pubService.addSiteItem(sItem);
      sItem = createSiteItem(CONTENT_ID, FOLDER_ID, LOCATION_1 + "_2",
            TEMPLATE_ID, "filesystem", 2);
      m_pubService.addSiteItem(sItem);
      sItem = createSiteItem(CONTENT_ID, FOLDER_ID, LOCATION_1 + "_3",
            TEMPLATE_ID, "filesystem", 3);
      m_pubService.addSiteItem(sItem);
      item = createItem(LOCATION_1, TEMPLATE_ID, 1);

      setParentIds(FOLDER_ID);

      unpubItems = m_handler.getUnpublishPaginateedItems(items);
      assertTrue("should be 2 extra pages to unpublish", unpubItems.size() == 2);
   }

   /**
    * Test to process large amount of items, make sure it can process 1000 at a
    * time still works for items > 1000
    * 
    * @throws Exception if an error occurs.
    */
   public void testLargeUnpublish() throws Exception
   {
      List<IPSAssemblyItem> pubItems = createItems(2500);
      createSiteItems(2500);
      setParentIds(FOLDER_ID);

      // it takes 1 milli-second per un-published item
      PSStopwatch w = new PSStopwatch();
      w.start();
      Collection<IPSAssemblyItem> items = m_handler.getUnpublishingItems(
            EI_SITE_GUID, 1, TEST_CLIST, null, pubItems);
      w.stop();
      System.out.println("Processed " + items.size() + " un-published item. " + w.toString());
      assertTrue(items.size() == 2500);
   }

   private List<IPSAssemblyItem> createItems(int totalItems)
   {
      List<IPSAssemblyItem> pubItems = new ArrayList<IPSAssemblyItem>();
      for (int i = 0; i < totalItems; i++)
      {
         IPSAssemblyItem item = createItem(CONTENT_ID + i, FOLDER_ID,
               LOCATION_1 + "_" + i, TEMPLATE_ID, "filesystem");
         pubItems.add(item);
      }
      return pubItems;
   }

   private void createSiteItems(int totalItems)
   {
      m_pubService.clearSiteItems();
      for (int i = 0; i < totalItems; i++)
      {
         IPSSiteItem sItem = createSiteItem(CONTENT_ID + i, FOLDER_ID_2,
               LOCATION_2 + "_" + i, TEMPLATE_ID, "filesystem");
         m_pubService.addSiteItem(sItem);
      }
   }

   static private IPSContentList getSampleCList()
   {
      PSContentList clist = new PSContentList();
      clist.setName("sample-content-list");
      clist.setGUID(new PSGuid(PSTypeEnum.CONTENT_LIST, 123));

      return clist;
   }

   private IPSAssemblyItem createItem(String location, Long templateId, int page)
   {
      IPSAssemblyItem item = createItem(location, templateId);
      item.setPage(page);
      return item;
   }

   private IPSAssemblyItem createItem(String location, Long templateId)
   {
      return createItem(CONTENT_ID, FOLDER_ID, location, templateId,
            "filesystem");
   }

   private IPSAssemblyItem createItem(int contentId, int folderId,
         String location, Long templateId, String deliveryType)
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      IPSAssemblyItem item = asm.createAssemblyItem();
      item.setJobId(1);
      item.setId(new PSLegacyGuid(contentId, 1));
      item.setFolderId(folderId);

      item.setDeliveryPath(location);
      item.setParameterValue(IPSHtmlParameters.SYS_TEMPLATE, Long
            .toString(templateId));

      item.setPublish(true);
      item.setDeliveryType(deliveryType);
      item.setDeliveryContext(1);
      item.setSiteId(EI_SITE_GUID);
      item.setAssemblyUrl("");

      return item;
   }

   private IPSSiteItem createSiteItem(String location, Long templateId, int page)
   {
      return createSiteItem(CONTENT_ID, FOLDER_ID, location, templateId,
            "filesystem", page);
   }

   private IPSSiteItem createSiteItem(String location, Long templateId)
   {
      return createSiteItem(CONTENT_ID, FOLDER_ID, location, templateId,
            "filesystem");
   }

   private IPSSiteItem createSiteItem(int contentId, int folderId,
         String location, Long templateId, String deliveryType)
   {
      return createSiteItem(contentId, folderId, location, templateId,
            deliveryType, null);
   }

   private IPSSiteItem createSiteItem(int contentId, int folderId,
         String location, Long templateId, String deliveryType, Integer page)
   {
      PSPubItem doc = new PSPubItem();
      doc.deliveryType = deliveryType;
      if (page != null)
         doc.page = page;
      PSSiteItem siteItem = new PSSiteItem();
      siteItem.status = (short) doc.getStatus().ordinal();
      siteItem.unpublishInfo = new byte[]
      {};
      
      siteItem.setLocation(location);
      siteItem.setTemplateId(templateId);
      siteItem.setContext(1);
      siteItem.setSiteId(EI_SITE_ID);
      siteItem.setContentId(contentId);
      siteItem.setFolderId(folderId);

      return siteItem;
   }

   /**
    * The mock implementation of the relationship service
    */
   class RelationshipService
         implements
            PSLocationChangeHandler.IRelationshipService
   {
      /**
       * See {@link #getParents(String, PSLocator, int)}
       */
      private List<Integer> m_parentIds = new ArrayList<Integer>();

      /*
       * (non-Javadoc)
       * 
       * @seecom.percussion.rx.publisher.impl.PSLocationChangeHandler.
       * IRelationshipService#getParents(java.lang.String,
       * com.percussion.design.objectstore.PSLocator, int)
       */
      public List<PSLocator> getParents(String type, PSLocator object,
            int doNotApplyFilters)
      {
         List<PSLocator> results = new ArrayList<PSLocator>();
         for (Integer id : m_parentIds)
            results.add(new PSLocator(id, 1));

         return results;
      }

      /**
       * Sets the parent IDs.
       * 
       * @param parentIds the parent IDs in UUID format, assumed not
       *           <code>null</code> or empty.
       */
      public void setParents(List<Integer> parentIds)
      {
         m_parentIds = parentIds;
      }
      
      public void addParent(Integer id)
      {
         m_parentIds.add(id);
      }
   }

   /**
    * The mock implementation of the publisher service.
    */
   class PublishService extends PSPublisherService
   {
      private IPSPublisherService m_pubService = PSPublisherServiceLocator
            .getPublisherService();

      private List<IPSSiteItem> m_siteItems = new ArrayList<IPSSiteItem>();

      /*
       * (non-Javadoc)
       * 
       * @seecom.percussion.services.publisher.impl.PSPublisherService#
       * findSiteItemsByIds(com.percussion.utils.guid.IPSGuid, int,
       * java.util.Collection)
       */
      public Collection<IPSSiteItem> findSiteItemsByIds(IPSGuid siteid,
            int deliveryContext, Collection<Integer> contentIds)
      {
         List<IPSSiteItem> result = new ArrayList<IPSSiteItem>();
         for (IPSSiteItem item : m_siteItems)
         {
            if (contentIds.contains(item.getContentId()))
               result.add(item);
         }
         return result;
      }

      public void addSiteItem(IPSSiteItem sItem)
      {
         m_siteItems.add(sItem);
      }

      public void clearSiteItems()
      {
         m_siteItems.clear();
      }

      public IPSDeliveryType loadDeliveryType(String dtypeName)
            throws PSNotFoundException
      {
         return m_pubService.loadDeliveryType(dtypeName);
      }
   }
   
   class FolderProcessor implements PSLocationChangeHandler.IFolderProcessor
   {
      //private List<List<PSLocator>> m_paths = new ArrayList<List<PSLocator>>();
      private Map<PSLocator, List<List<PSLocator>>> pathMap = new HashMap<PSLocator, List<List<PSLocator>>>();
      
      public List<List<PSLocator>> getFolderLocatorPaths(PSLocator loc)
      {
         return pathMap.get(loc);
      }
      
      public void addPath(List<PSLocator> path, PSLocator loc)
      {
         List<List<PSLocator>> value = new ArrayList<List<PSLocator>>();
         value.add(path);
         pathMap.put(loc, value);
      }
      
      public void clear()
      {
         pathMap.clear();
      }
   }
}
