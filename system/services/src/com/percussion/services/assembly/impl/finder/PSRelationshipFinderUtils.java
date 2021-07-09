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
package com.percussion.services.assembly.impl.finder;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSRelationshipChangeEvent;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.impl.finder.PSContentFinderBase.ContentItem;
import com.percussion.services.assembly.impl.finder.PSContentFinderBase.ContentItemOrder;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.services.memory.PSCacheAccessLocator;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.PSNotificationServiceLocator;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.percussion.services.assembly.impl.finder.PSContentFinderBase.ORDER_BY;
import static com.percussion.services.memory.IPSCacheAccess.CONTENT_FINDER_RELS;
import static org.apache.commons.lang.Validate.notNull;

/**
 * This class provides various helper methods for relationship content finders.
 * <p> 
 * Note, The revision that is included in the dependent ID 
 * {@link IPSFilterItem#getItemId()} is directly from the relationship which 
 * may be <code>-1</code>. It is up to the caller (or filter) to re-adjust the
 * reversion.
 *
 * @author YuBingChen
 */
public abstract class PSRelationshipFinderUtils<T extends Object> extends PSContentFinderUtils
      implements
         IPSNotificationListener
{
   
   private ConcurrentMap<IPSGuid, IPSGuid> locks = new ConcurrentHashMap<>();
   
   /**
    * Initialize the finder, set up notification for evicting cached
    * relationships.
    */
   public void init()
   {
      IPSNotificationService srv = PSNotificationServiceLocator
            .getNotificationService();
      srv.addListener(EventType.RELATIONSHIP_CHANGED, this);
   }

   /**
    * Evict cache in "slot" region where the owner is the key of the 
    * owner of the modified relationships.
    */
   @SuppressWarnings("unchecked")
   public void notifyEvent(PSNotificationEvent notify)
   {
      if (!EventType.RELATIONSHIP_CHANGED.equals(notify.getType()))
         return;
            
      PSRelationshipChangeEvent event = (PSRelationshipChangeEvent) notify
            .getTarget();
      PSRelationshipSet rset = event.getRelationships();
      
      Iterator it = rset.iterator();
      // de-dup owner IDs
      Set<PSLegacyGuid> ownerIds = new HashSet<>();
      while (it.hasNext())
      {
         PSRelationship rel = (PSRelationship)it.next();
         PSLegacyGuid id = new PSLegacyGuid(rel.getOwner());
         ownerIds.add(id);
      }
      IPSCacheAccess cache = PSCacheAccessLocator.getCacheAccess();
      if (ownerIds.isEmpty())
      {
         cache.clearRelationships();
      }
      else
      {
         // evict distinct owner IDs
         for (PSLegacyGuid id : ownerIds)
            cache.evict(id, CONTENT_FINDER_RELS);
      }
   }
      
   /**
    * Gets get the related items for the specified "owner" item, where the
    * relationship type is active assembly and the "sys_slotid" property (of the
    * relationship) equals specified value.
    * 
    * @param sourceItem the source assembly item,never <code>null</code>. for
    *            the default method, it is the owner of the related (returned)
    *            items.
    * @param slotId the ID of the container. It is the value of the "sys_slotid"
    *            property of the active assembly relationships.
    * @param params the parameters passed to this finder, it may be <code>null</code> or empty if there is no parameters.
    * 
    * @return a set of related items. It can never be <code>null</code>,
    *         but may be empty. The revision in {@link IPSFilterItem#getItemId()} 
    *         is directly from the relationship which may be <code>-1</code>. 
    *         It is up to the caller (or filter) to re-adjust the version.
    */   
   private Set<ContentItem> getContentItemsInner(IPSAssemblyItem sourceItem, T slot, Map<String, Object> params)
   {
      notNull(sourceItem, "sourcItem may not be null.");

      List<PSRelationship> rels = getRelationships(sourceItem.getId());
      Set<ContentItem> rval = new HashSet<>();
      
      // Now get the relevant relationships for the particular slot. These
      // will have properties that match the slot id for the given template
      // slot we've been passed. The variant id's are used to lookup the
      // template if the selectors don't include an override (template)
      for (PSRelationship rel : rels)
      {
         if (isTargetRelationship(rel, slot, params))
         {
            ContentItem item = createContentItem(sourceItem, rel);
            rval.add(item);
         }
      }
      
      return rval;
   }

   /**
    * Determines if the specified relationship matches the given slot.
    * @param rel the relationship in question, not <code>null</code>.
    * @param slotId the ID of the slot.
    * @return <code>true</code> if the relationship matches the given slot.
    */
   abstract protected boolean isTargetRelationship(PSRelationship rel, T slot, Map<String, Object> params);
   
   /**
    * Determines if the specified relationship matches the given slot.
    * @param rel the relationship in question, not <code>null</code>.
    * @param slotId the ID of the slot.
    * @return <code>true</code> if the relationship matches the given slot.
    */
   protected boolean matchesSlotId(PSRelationship rel, Long slotId)
   {
      if (!rel.getConfig().isActiveAssemblyRelationship())
         return false;
      
      String slotid = rel.getProperty("sys_slotid");
      return (!StringUtils.isBlank(slotid) && Long.parseLong(slotid) == slotId.longValue());
   }

   /**
    * Creates an content item from the given source and relationship.
    * @param sourceItem the source item, assumed not <code>null</code>.
    * @param rel the relationship, assumed not <code>null</code>.
    * @return the created content item, not <code>null</code>.
    */
   private ContentItem createContentItem(IPSAssemblyItem sourceItem,
         PSRelationship rel)
   {
      PSLocator dep = rel.getDependent();
      int contentid = dep.getId();
      int revision = dep.getRevision();
      // revision = -1 is possible here, but we are relying on the caller 
      // to re-adjust the revision according its needs, for example
      // it may be re-adjusted in sys_previewFilter (PSPreviewFilter)
      // and sys_publicFilter (PSPublicFilter)
      IPSGuid relguid = new PSLegacyGuid(contentid, revision);
      String sort = rel.getProperty(PSRelationshipConfig.PDU_SORTRANK);
      String varid = rel.getProperty(PSRelationshipConfig.PDU_VARIANTID);
      // use the source template if there is no template specified
      // in the AA relationship
      IPSGuid template = StringUtils.isBlank(varid) ? sourceItem
            .getTemplate().getGUID() : new PSGuid(PSTypeEnum.TEMPLATE,
            varid);
      ContentItem item = new ContentItem(relguid, template, Integer.parseInt(sort));
      item.setRelationshipId(rel.getGuid());
      String siteid = rel.getProperty(PSRelationshipConfig.PDU_SITEID);
      if (!StringUtils.isBlank(siteid))
      {
         item.setSiteId(new PSGuid(PSTypeEnum.SITE, siteid));
      }
      String folderid = rel
            .getProperty(PSRelationshipConfig.PDU_FOLDERID);
      if (!StringUtils.isBlank(folderid))
      {
         item.setFolderId(new PSLegacyGuid(Integer.parseInt(folderid),
               0));
      }
      item.setWidgetName(rel.getProperty(PSRelationshipConfig.PDU_WIDGET_NAME));
      item.setOwnerId(new PSLegacyGuid(rel.getOwner()));
      
      return item;
   }

   /**
    * Gets the related items for the specified "owner" item, where the
    * relationship type is active assembly and the "sys_slotid" property (of the
    * relationship) equals specified value.
    * 
    * @param sourceItem the source assembly item,never <code>null</code>. for
    * the default method, it is the owner of the related (returned) items.
    * @param slotId the ID of the container. It is the value of the "sys_slotid"
    * property of the active assembly relationships.
    * @param params the parameters passed to the finder. There is only one
    * optional parameter, {@link PSContentFinderBase#ORDER_BY}. The returned
    * items will be re-ordered according to the specified parameter; otherwise
    * the returned items are ordered by {@link PSContentFinderBase.ContentItem}.
    * 
    * @return a set of related items, never <code>null</code>, but may be empty.
    * The items can be re-ordered with {@link ContentItemOrder}.
    * <p> 
    * Note, The revision in {@link IPSFilterItem#getItemId()} is directly from 
    * the relationship which may be <code>-1</code>. It is up to the caller 
    * (or filter) to re-adjust the version.
    */   
   public Set<ContentItem> getContentItems(IPSAssemblyItem sourceItem,
         T slot, Map<String, Object> params)
   {
      Set<ContentItem> items = getContentItemsInner(sourceItem, slot, params);

      String orderby = getValue(params, ORDER_BY, null);
      if (StringUtils.isNotBlank(orderby))
      {
         String locale = getLocale(sourceItem, params);
         items = reorderItems(items, orderby, locale);
      }

      return items;
   }
   
   /**
    * Find the active assembly relationships for the related content finder.
    * 
    * @param sourceItem the assembly item, which contains current context,
    * never <code>null</code>.
    * 
    * @return a set of matching relationships, never <code>null</code>
    */
   @SuppressWarnings("unchecked")
   protected List<PSRelationship> getRelationships(IPSGuid id)
   {
      notNull(id, "id may not be null.");

     // ArrayList<PSRelationship> relationships = (ArrayList<PSRelationship>) getCacheAccess().get(id,
      //      CONTENT_FINDER_RELS);
      ArrayList<PSRelationship> relationships = null;
      if (relationships == null)

      {

            List<PSRelationship> rels = queryRelationships(null, id);
            if (rels instanceof ArrayList)
            {
               relationships = (ArrayList<PSRelationship>) rels;
            }
            else
            {
               relationships = new ArrayList<>(rels);
            }

      }

      return relationships;
   }

   /**
    * Gets the cache handler.
    * 
    * @return the cache handler, never <code>null</code>.
    */
   private synchronized IPSCacheAccess getCacheAccess()
   {
      if (m_cache == null)
         m_cache = PSCacheAccessLocator.getCacheAccess();
      
      return m_cache;
   }

   /**
    * Retrieves the active assembly relationships from the repository, where 
    * the owner is the given item. This is called by 
    * {@link #getRelationships(IPSAssemblyItem)}.
    * 
    * @param sourceItem the assembly item, which contains current context,
    * assumed not <code>null</code>.
    * @param ownerId the owner of the returned relationship, assumed not
    * <code>null</code>.
    * 
    * @return the related relationships, never <code>null</code>, may be
    * empty.
    */
   protected List<PSRelationship> queryRelationships(
         IPSAssemblyItem sourceItem, IPSGuid ownerId)
   {
      notNull(ownerId, "ownderId may not be null.");

      try
      {
         PSRelationshipFilter filter = new PSRelationshipFilter();
         PSLegacyGuid guid = (PSLegacyGuid) ownerId;
         PSLocator owner = new PSLocator(guid.getContentId(), guid
               .getRevision());
         filter.setOwner(owner);
         filter.limitToOwnerRevision(true);
         filter.setCategory(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
         PSRelationshipProcessor proc = PSRelationshipProcessor.getInstance();
         return proc.getRelationshipList(filter);
      }
      catch (PSCmsException e)
      {
         ms_log.error("problem getting relationships for owner id="
               + ownerId.toString(), e);
         return Collections.emptyList();
      }
   }
   
   /**
    * Get id based object to only lock when trying to add the same id to the cache
    * @param id
    * @return the GUID id
    */
   private IPSGuid getCacheSyncObject(final IPSGuid id) {
      return locks.computeIfAbsent(id, k -> id);
   }
   
   /** Remove the id from the cache sync.
    * @param id the GUID id
    */
   private void clearCacheSyncObject(final IPSGuid id) {
      locks.remove(id);
   }

   /**
    * Logger
    */
   private static final Logger ms_log = LogManager.getLogger(PSRelationshipFinderUtils.class);
 
   /**
    * Cache handler
    */
   private IPSCacheAccess m_cache = null;

   
   
}
