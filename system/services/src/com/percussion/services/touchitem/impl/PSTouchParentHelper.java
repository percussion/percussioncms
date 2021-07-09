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
package com.percussion.services.touchitem.impl;

import static java.util.Arrays.asList;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.error.PSException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.data.IPSIdentifiableItem;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is touch parent code that used to be
 * in {@link PSTouchParentFolderEffect}.
 * <p>
 * It will only touch the dependents who are in public or
 * quick-edit state. It "touches" the last modified date for the dependents 
 * and their Active Assembly relationship parents, so that they will be picked 
 * up by the next incremental publishing. For folder dependents, it touches
 * all item descendants of the folders, but not the folder themselves.
 * 
 * @see IPSCmsObjectMgr#filterItemsByPublishableFlag(java.util.List, java.util.List)
 * @see IPSCmsObjectMgr#touchItems(Collection) 
 */
public final class PSTouchParentHelper
{  
   /**
    * See Class Doc: {@link PSTouchParentHelper}.
    * 
    * @param request not null.
    * @param rel not null.
    * @throws Exception On any error.
    */
   protected int touchItemAndParents(IPSRequestContext request,
         PSRelationship rel) throws Exception
   {
      Integer dependentId = new Integer(rel.getDependent().getId());
      return touchItemAndParents(request, dependentId);
   }
   
   /**
    * See Class Doc: {@link PSTouchParentHelper}.
    * @param request not null.
    * @param contentId Legacy content id of the item, not null.
    * @throws Exception on any error.
    */
   private int touchItemAndParents(IPSRequestContext request,
         Integer contentId) throws Exception
   {
      @SuppressWarnings("unchecked")
      Set<Integer> processedIds = (Set<Integer>) request.getPrivateObject(PROCESSED_DEPENDENTS);
      if (processedIds == null)
      {
         processedIds = new HashSet<>();
         request.setPrivateObject(PROCESSED_DEPENDENTS, processedIds);
      }
      AtomicInteger counter = new AtomicInteger(0);
      touchItemAndParents(contentId, request, processedIds, counter);
      return counter.get();
   }
   
   /**
    * Touches the supplied item or folder.
    *
    * @param contentId the id of an item or folder.
    * @param request the request object, assumed not <code>null</code>.
    * @param processedIds the processed item IDs<code>null</code>.
    *
    * @throws PSException if an error occurs.
    */
   private void touchItemAndParents(Integer contentId,
         IPSRequestContext request, Set<Integer> processedIds, AtomicInteger counter) throws Exception
   {
      if (processedIds == null) {
          throw new IllegalArgumentException("processedIds");
      }
      if (processedIds.contains(contentId))
         return;
      if (isFolderDependent(contentId, request))
      {
         touchAAParentForFolderChildren(contentId, request, processedIds, counter);
      }
      else
      {
         touchAAParentForItems(asList(contentId), request, processedIds, counter);
      }

      // register the to be processed item
      processedIds.add(contentId);

   }

   /**
    * Touches all child items and their AA parents for the supplied folder id.
    *
    * @param contentId the folder id.
    * @param request the request object, assumed not <code>null</code>.
    *
    * @throws PSException if an error occurs.
    */
   private void touchAAParentForFolderChildren(Integer contentId,
         IPSRequestContext request, Set<Integer> processedIds, AtomicInteger total) throws PSException
   {
      PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();
      PSLocator locator = new PSLocator(contentId.intValue(), 1);
      PSKey[] locators = processor.getDescendentsLocators(
            PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE,
            PSRelationshipConfig.TYPE_FOLDER_CONTENT, locator);

      int counter = 0;
      Set<Integer> ids = new HashSet<>();
      Integer id;
      // process a group (1000 items) at a time
      for (int i = 0; i < locators.length; i++)
      {
         id = new Integer(locators[i].getPartAsInt("CONTENTID"));
         if (processedIds.contains(id))
            continue;

         ids.add(id);

         if (counter >= 999)
         {
            touchAAParentForItems(ids, request, processedIds, total);
            counter = 0;
            ids.clear();
         }
         else
         {
            counter++;
         }
      }
      if (!ids.isEmpty())
         touchAAParentForItems(ids, request, processedIds, total);
   }

   /**
    * Just like {@link #touchAAParentForItem(Integer, IPSRequestContext)},
    * except this is touching a list of items.
    *
    * @param ids a list of content ids (as <code>Integer</code> objects),
    *    assumed not <code>null</code>, but may be empty.
    * @param req the request object, assumed not <code>null</code>.
    *
    * @throws PSException if an error occurs.
    */
   private void touchAAParentForItems(Collection<Integer> ids, IPSRequestContext req, 
         Set<Integer> processedIds, AtomicInteger counter)
         throws PSException
   {

      if (ms_logger.isDebugEnabled()) {
         ms_logger.debug("Touching items and their parents: " + ids + " processed: " + processedIds);
      }
      List<ItemId> items = new ArrayList<>();
      for (Integer id : ids) {
         if (processedIds.contains(id)) continue;
         items.add(new ItemId(id));
      }
      try
      {
         items = PSCmsObjectMgrLocator.getObjectManager().filterItemsByPublishableFlag(items, asList("y","i"));
      }
      catch (PSORMException e)
      {
         throw new RuntimeException(e);
      }
      ids = new ArrayList<>();
      for (ItemId i : items) {
         ids.add(i.getContentId());
      }
      if (ms_logger.isDebugEnabled()) {
         ms_logger.debug("After filtering: " + ids);
      }
      Collection<Integer> touchIds = PSPublisherServiceLocator
         .getPublisherService()
         .touchActiveAssemblyParents(ids);
      counter.addAndGet(touchIds.size());
      counter.addAndGet(ids.size());
      if (ms_logger.isDebugEnabled()) {
         ms_logger.debug("Touched: " + touchIds);
      }
      processedIds.addAll(ids);
   }
   
   /**
    * Wrapper to hold an item id for 
    * id workflow status filtering.
    * @author adamgent
    *
    */
   private static class ItemId implements IPSIdentifiableItem {

      private IPSGuid itemId;
      private Integer contentId;
      private ItemId(Integer id) {
         contentId = id;
         itemId = new PSLegacyGuid(id, -1);
      }
      
      public IPSGuid getItemId()
      {
         return itemId;
      }
      
      public Integer getContentId() {
         return contentId;
      }
      
   }

   /**
    * Determine whether the item of the supplied content id is a folder or an
    * item.
    * 
    * @param contentId
    *           the content id of a folder or item, assumed not <code>null</code>.
    * @param request
    *           the request context, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the supplied content id is a folder id.
    * 
    * @throws PSCmsException
    *            if an error occurs.
    */
   private boolean isFolderDependent(Integer contentId,
         IPSRequestContext request)
         throws PSCmsException
   {
      PSServerFolderProcessor p = PSServerFolderProcessor.getInstance();
      return p.isItemFolder(new PSLocator(contentId));
   }

   /**
    * The singleton instance logger used by this class.
    */
   private static final  Logger ms_logger = LogManager
         .getLogger(PSTouchParentHelper.class);


   /**
    * The name of the private object saved in the current request context.
    * The private object is a collection (<code>Set</code>) of the dependent 
    ids
    * (as <code>Integer</code> objects). It may be <code>null</code> if the
    * private object has not been set.
    */
   private final static String PROCESSED_DEPENDENTS = "TouchParentFolderEffect_ProcessedDepedents";
}

