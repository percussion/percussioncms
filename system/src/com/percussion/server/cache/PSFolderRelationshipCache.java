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
package com.percussion.server.cache;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSRelationshipChangeEvent;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.data.IPSBackEndErrors;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.design.objectstore.PSRelationshipPropertyData;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSException;
import com.percussion.extension.services.PSDatabasePool;
import com.percussion.server.IPSServerErrors;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSItemEntry;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.PSNotificationServiceLocator;
import com.percussion.services.relationship.data.PSRelationshipData;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSSqlHelper;
import com.percussion.util.PSStopwatch;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * This class is used to cache all folder relationships. However, it only
 * caches the skeleton of the folder relationship for less memory consumption.
 */
public class PSFolderRelationshipCache  implements IPSNotificationListener
{
   
   public void notifyEvent(PSNotificationEvent notify)
   {
      if (!EventType.RELATIONSHIP_CHANGED.equals(notify.getType()))
         return;

      PSRelationshipChangeEvent event = (PSRelationshipChangeEvent) notify
            .getTarget();
      
      boolean isDelete = event.getAction() == PSRelationshipChangeEvent.ACTION_REMOVE;
      
      PSRelationshipSet rset = event.getRelationships();

      for (PSRelationship psRelationship : (Iterable<PSRelationship>) rset) {
         if (isDelete) {
            log.debug("deleting relationship from relationship cache: {} ", psRelationship.toString());
            delete(psRelationship);
         } else {
            log.debug("updating relationship from relationship cache: {} ", psRelationship.toString());
            update(psRelationship);
         }

      }
      
   }
   
   
   /**
    * Creates and obtains the single instance of this class.  This method
    * should only be called once (this should be done by the server when
    * initializing).
    *
    * @return The instance of this class.
    *
    * @throws IllegalStateException if {@link #createInstance()} has already
    *    been called.
    * @throws PSCacheException if other error occurs.
    */
   public static PSFolderRelationshipCache createInstance()
         throws PSCacheException
   {
      if (ms_instance != null)
         throw new IllegalStateException(
               "PSFolderRelationshipCache has already been created");

      ms_instance = new PSFolderRelationshipCache();
      ms_itemcache = PSItemSummaryCache.getInstance();
      
      IPSNotificationService srv = PSNotificationServiceLocator
            .getNotificationService();
      srv.addListener(EventType.RELATIONSHIP_CHANGED, ms_instance);
      
      return ms_instance;
   }

   private static void createRelationshipConfig() {
      PSRelationshipConfigSet configurationSet = PSRelationshipCommandHandler.getConfigurationSet();
      ms_relationshipConfigsByName = new HashMap<>();
      ms_relationshipConfigsById = new HashMap<>();
      for (PSRelationshipConfig config : configurationSet.getConfigList()) {
         ms_relationshipConfigsByName.put(config.getName(), config);
         ms_relationshipConfigsById.put(config.getId(), config);
      }
   }

   /**
    * Get the item cache instance. Must call {@link #createInstance()} first.
    * 
    * @return the item cache, never <code>null</code>.
    */
   public PSItemSummaryCache getItemCache()
   {
      if (ms_itemcache == null)
         throw new IllegalStateException("ms_itemCache must not be null.");
      
      return ms_itemcache;
   }
   
   /**
    * Returns the singleton instance of this class. This singleton instance
    * must be instantiated by call {@link #createInstance()}
    *
    * @return the singleton instance of this class, may be <code>null</code>
    *    if it has not been initialized (or started).
    */
   public static PSFolderRelationshipCache getInstance()
   {
      if (ms_instance == null)
         return null;
      
      ms_instance.m_rwlock.readLock().lock();
      try
      {
         return ms_instance.m_isStarted ? ms_instance : null;
      }
      finally
      {
         ms_instance.m_rwlock.readLock().unlock();
      }
   }

   /**
    * Private constructor. Must use {@link #createInstance()} to create an
    * instance.
    */
   private PSFolderRelationshipCache()
   {
   }

   /**
    * Reinitialize the caching operation according to the supplied flag.
    *
    * @param isEnabled
    *           <code>true</code> if re-initializing the caching; otherwise
    *           stop the caching operation.
    *
    * @throws PSCacheException
    *            if an error occurs.
    */
   void reinitialize(boolean isEnabled) throws PSCacheException
   {
      m_rwlock.writeLock().lock();
      try
      {
         ms_itemcache.reinitialize(isEnabled);

         if (isEnabled)
         {
            stop();
            start();
         }
         else
         {
            stop();
         }
      }
      finally
      {
         m_rwlock.writeLock().unlock();
      }
   }

   /**
    * Stop the caching operation. Release all cached data.
    * <p>
    * Note: Assume the caller is locked by {@see m_rwlock.writeLock.lock()}.
    */
   private void stop()
   {
      if (! m_isStarted)
         return; // already stopped

      m_isStarted = false;
      m_relationshipMap = new HashMap<>();
      m_aARelationshipMap = new HashMap<>();
      m_graph = new PSRelationshipGraph();
   }

   /**
    * It populates all folder relationships in memory. Do nothing if already
    * started.
    * <p>
    * Note: Assume the caller is locked by {@see #m_rwlock.writeLock.lock()}.
    *
    * @throws PSCacheException if an error occurs.
    */
   private void start() throws PSCacheException
   {
      if (m_isStarted)
         return; // do nothing

      PSStopwatch watch = new PSStopwatch();
      watch.start();

      m_relationshipMap = new HashMap<>();
      m_aARelationshipMap = new HashMap<>();
      m_graph = new PSRelationshipGraph();
      m_aa_graph = new PSRelationshipGraph();

      PreparedStatement stmt = null;
      ResultSet rs = null;
      Connection conn = null;
      Integer key;
      log.debug("Pre loading folder relationships for folder cache");
      try
      {
         StringBuffer buf = new StringBuffer();

         buf.append("SELECT r.RID, r.OWNER_ID, r.DEPENDENT_ID, r.CONFIG_ID FROM ");
         buf.append(PSSqlHelper.qualifyTableName(IPSConstants.PSX_RELATIONSHIPS));
         buf.append(" r ");
         buf.append("WHERE r.CONFIG_ID = ");
         buf.append(String.valueOf(PSRelationshipConfig.ID_FOLDER_CONTENT));
         buf.append(" OR ");
         buf.append("r.CONFIG_ID = ");
         buf.append(String.valueOf(PSRelationshipConfig.ID_RECYCLED_CONTENT));
         
         String query = buf.toString();
         conn = PSDatabasePool.getDatabasePool().getConnection();
         if (conn == null)
            throw new PSException(IPSBackEndErrors.EXEC_DATA_NO_CONNECTIONS);
         stmt = PSPreparedStatement.getPreparedStatement(conn, query);
         rs = stmt.executeQuery();

         // store the list of relationships
         Integer parent, child, configId;
         while (rs.next())
         {
            key = Integer.valueOf(rs.getInt(1));
            parent = Integer.valueOf(rs.getInt(2));
            child = Integer.valueOf(rs.getInt(3));
            configId = Integer.valueOf(rs.getInt(4));
            addRelationship(key, new PSLocator(parent), new PSLocator(child), configId);
         }
         cleanupFolders();
         m_graph.trimSize();
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         throw new PSCacheException(
            IPSServerErrors.CACHE_UNEXPECTED_EXCEPTION, e.toString());
      }
      finally
      {
         if (null != rs)
            try
            {
               rs.close();
            }
            catch (Exception e)
            {
            };
         if (null != stmt)
            try
            {
               stmt.close();
            }
            catch (Exception e)
            {
            };
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
               // ignore, should not happen here.
               log.error(e.getMessage());
               log.debug(e.getMessage(), e);
            }
         }
      }

      int folderRelationshipCount = m_relationshipMap.size();
      log.info("Loaded {} folder relationships into cache", folderRelationshipCount);
      
      try
      {
         StringBuffer buf = new StringBuffer();

         List<PSRelationshipConfig> aAConfigs = PSRelationshipCommandHandler.getConfigurationSet().getConfigListByCategory(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
         Map<Integer,PSRelationshipConfig> configIdMap = new HashMap<>();
         buf.append("SELECT r.RID, r.CONFIG_ID, r.OWNER_ID, r.OWNER_REVISION, r.DEPENDENT_ID, r.DEPENDENT_REVISION, r.SLOT_ID, r.SORT_RANK, r.VARIANT_ID,r.FOLDER_ID,r.SITE_ID, r.INLINE_RELATIONSHIP, WIDGET_NAME FROM ");
         buf.append(PSSqlHelper.qualifyTableName(IPSConstants.PSX_RELATIONSHIPS));
         buf.append(" r INNER JOIN ");
         buf.append(PSSqlHelper.qualifyTableName(IPSConstants.CONTENT_STATUS_TABLE));
         buf.append(" c ON c.CONTENTID=r.OWNER_ID ");
         buf.append("WHERE r.CONFIG_ID in (");
         // get all AA rels by config
         boolean first=true;
         for (PSRelationshipConfig config: aAConfigs)
         {
            configIdMap.put(config.getId(), config);
            if (!first)
            {
               buf.append(",");
            }
            first=false;
            buf.append(config.getId());
         }
         buf.append(")");
         buf.append(" and  r.OWNER_REVISION != -1 and r.OWNER_REVISION in (c.CURRENTREVISION,c.TIPREVISION,c.PUBLIC_REVISION)");

         
         String query = buf.toString();
        
         conn = PSDatabasePool.getDatabasePool().getConnection();
         if (conn == null)
            throw new PSException(IPSBackEndErrors.EXEC_DATA_NO_CONNECTIONS);
         stmt = PSPreparedStatement.getPreparedStatement(conn, query);
         rs = stmt.executeQuery();

         // store the list of relationships

         PSRelationshipData rdata = null;
         while (rs.next())
         {
           
            rdata = new PSRelationshipData();
            
            rdata.setId(rs.getInt(1));
            int configId = rs.getInt(2);
            rdata.setConfigId(configId);
            rdata.setConfig(configIdMap.get(configId));
            rdata.setOwnerId(rs.getInt(3));
            rdata.setOwnerRevision(rs.getInt(4));
            rdata.setDependentId(rs.getInt(5));
            rdata.setDependentRevision(rs.getInt(6));
            if (rs.getObject(7) != null)
               rdata.setSlotId(rs.getLong(7));
            if (rs.getObject(8) != null)
               rdata.setSortRank(rs.getInt(8));
            if (rs.getObject(9) != null)
               rdata.setVariantId(rs.getLong(9));
            if (rs.getObject(10) != null)
               rdata.setFolderId(rs.getInt(10));
            if (rs.getObject(11) != null)
               rdata.setSiteId(rs.getLong(11));
            rdata.setInlineRelationship(rs.getString(12));
            rdata.setWidgetName(rs.getString(13));
            addAARelationship(rdata);
         }
         cleanupFolders();
         m_aa_graph.trimSize();
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         throw new PSCacheException(
            IPSServerErrors.CACHE_UNEXPECTED_EXCEPTION, e.toString());
      }
      finally
      {
         if (null != rs)
            try
            {
               rs.close();
            }
            catch (Exception e)
            {
            };
         if (null != stmt)
            try
            {
               stmt.close();
            }
            catch (Exception e)
            {
            };
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
               // ignore, should not happen here.
               log.error(e.getMessage());
               log.debug(e.getMessage(), e);
            }
         }
      }
      int activeAssemblyRelationshipCount = m_aARelationshipMap.size();
      log.info("Loaded {} ActiveAssembly relationships into cache", +activeAssemblyRelationshipCount);
      
      m_isStarted = true;

      watch.stop();
      if (log.isDebugEnabled())
         log.debug("start elapse time: {}", watch.toString());
   }

   /**
    * Returns the paths to the root for the supplied item.
    * Note: Assume the caller is locked by {@see #m_rwlock.readLock.lock()}.
    * 
    * @param locator
    *           the locator of the item, never <code>null</code>. The
    *           revision of the locator is ignored.
    *
    * @return a 2 dimension array. The 1st dimension is a list of paths; the
    *         2nd dimension contains the actual paths. Within each path, the 1st
    *         element is the root, followed by its immediate child, ...etc.
    *         Never <code>null</code>, but may be empty if the supplied
    *         object is the root, which does not have a parent.
    */
   private List<List<PSLocator>> getPathsToRoot(PSLocator locator, String relationshipTypeName)
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      List<List<PSLocator>> paths = new ArrayList<>();
      for (List<PSGraphEntry> pathsList : m_graph.getPathsToRoot(locator, relationshipTypeName))
      {
         paths.add(convertToLocator(pathsList));
      }

      return paths;
   }

   private PSRelationshipConfig getRelationshipConfigForName(String relationshipTypeName) {
      if (ms_relationshipConfigsByName == null || ms_relationshipConfigsByName.size() == 0) {
         createRelationshipConfig();
      }
      return ms_relationshipConfigsByName.get(relationshipTypeName);
   }

   private PSRelationshipConfig getRelationshipConfigForId(Integer relationshipTypeId) {
      if (ms_relationshipConfigsById == null || ms_relationshipConfigsById.size() == 0) {
         createRelationshipConfig();
      }
      return ms_relationshipConfigsById.get(relationshipTypeId);
   }

   /**
    * Gets all locators of the ancesters for the supplied locator. This
    * is a convenience method, it calls {@link #getParentLocators(PSLocator)}
    * and then all all paths into a list, the 1st path, followed by the 2nd path
    * and so forth.
    *
    * @param itemLocator the locator of an item or folder, never
    *    <code>null</code>.
    *
    * @return a list over zero or more {@link PSLocator} objects. Never
    *    <code>null</code>, may be empty if there is no parent for the supplied
    *    locator.
    */
   public List<PSLocator> getOwnerLocators(PSLocator itemLocator, String relationshipTypeName)
   {
      List<List<PSLocator>> locators;
      m_rwlock.readLock().lock();
      try
      {
         locators = getPathsToRoot(itemLocator, relationshipTypeName);
      }
      finally
      {
         m_rwlock.readLock().unlock();
      }

      List<PSLocator> owners = new ArrayList<>();
      for (List<PSLocator> locatorList : locators)
         owners.addAll(locatorList);

      return owners;
   }

  /**
   * Get parent path built for the specified item's parents. If the item happens
   * to have multiple immediate parents, the method returns all paths. The path
   * is build as described below:
   * <p>
   * "/sys_title_root/.../sys_title_second/sys_title_first"
   * <p>
   * If the item has no parents it returns an empty array NOT its title alone.
   * <p>
   * @param locator
   *           the locator of the item, never <code>null</code>.
   * @param relationshipTypeName the type of the relationship, never <code>null</code>.
   *
   * @return the parent paths, never <code>null</code>, may be empty.
   */
   public String[] getParentPaths(PSLocator locator, String relationshipTypeName)
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      List<List<PSLocator>> locators; 
      m_rwlock.readLock().lock();
      try
      {
         locators = getPathsToRoot(locator, relationshipTypeName);
      }
      finally
      {
         m_rwlock.readLock().unlock();
      }
      List<String> result = new ArrayList<>();

      IPSItemEntry item;
      for (List<PSLocator> locs : locators)
      {
         StringBuffer buffer = new StringBuffer();
         for (PSLocator loc : locs)
         {
            item = getItem(Integer.valueOf(loc.getId()));
            if (item == null)
            {
               log.info("Cannot find item with content id, {} , but it exist in folder relationship.", loc.getId());
               buffer = new StringBuffer(); // a broken path, reset the buffer
               break;
            }
            else
            {
               buffer.append('/');
               buffer.append(item.getName());
            }
         }
         result.add(new String(buffer));
      }
      return (String[]) result.toArray(new String[result.size()]);
   }

   /**
    * Returns the immediate children locators for the supplied folder locator.
    *
    * @param locator
    *           the folder locator, never <code>null</code>.
    *
    * @return the children locators, never <code>null</code>, but may be
    *         empty.
    */
   public List<PSLocator> getChildLocators(PSLocator locator)
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      List<PSGraphEntry> childObjs;
      m_rwlock.readLock().lock();
      try
      {
         childObjs = m_graph.getChildrenList(locator);
      }
      finally
      {
         m_rwlock.readLock().unlock();
      }
      return convertToLocator(childObjs);
   }

   /**
    * Returns the immediate children IDs for the supplied parent folder.
    *
    * @param parentID
    *           the folder ID, never <code>null</code>.
    *
    * @return the children IDs, never <code>null</code>, but may be empty.
    */
   public List<Integer> getChildIDs(Integer parentID)
   {
      if (parentID == null)
         throw new IllegalArgumentException("locator may not be null");

   
      m_rwlock.readLock().lock();
      List<PSGraphEntry> childObjs;
      try
      {
         childObjs = m_graph.getChildrenList(new PSLocator(parentID));
      }
      finally
      {
         m_rwlock.readLock().unlock();
      }
      List<Integer> ids = new ArrayList<>();
      for (PSGraphEntry c : childObjs)
         ids.add(c.getValue().getId());
      
      return ids;
   }

   /**
    * Returns the immediate parent locators for the supplied item.
    *
    * @param locator
    *           the item, never <code>null</code>.
    *
    * @return the parent locators, never <code>null</code>, but may be empty.
    */
   public List<PSLocator> getParentLocators(PSLocator locator)
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");
      // For folder need to check revisionless
      PSLocator nonRevLocator = locator.getRevision()==-1 ? locator : new PSLocator(locator.getId());

      m_rwlock.readLock().lock();
   
      List<PSGraphEntry> childObjs;
      try
      {
         childObjs = m_graph.getParents(nonRevLocator);
      }
      finally
      {
         m_rwlock.readLock().unlock();
      }
      return convertToLocator(childObjs);
   }

   /**
    * Converts the supplied array of {@link PSGraphEntry}to array of locators.
    *
    * @param entries
    *           the array of {@link PSGraphEntry}objects, assume it is array of
    *           <code>Integer</code> objects and it not <code>null</code>.
    *
    * @return the converted locators, never <code>null</code>.
    */
   private List<PSLocator> convertToLocator(List<PSGraphEntry> entries)
   {
      List<PSLocator> locators = new ArrayList<>(entries.size());

      for (PSGraphEntry entry : entries)
      {
         locators.add(entry.getValue());
      }

      return locators;
   }

   /**
    * Updates the supplied relationships to the cache. The relationships will
    * be added into the cache if not exist. The relationships will be ignored
    * for none folder relationship type.
    *
    * @param relationships the to be updated relationship, never
    *    <code>null</code>.
    */
   public void update(PSRelationshipSet relationships)
   {
      if (relationships == null)
         throw new IllegalArgumentException("relationships may not be null");

      m_rwlock.writeLock().lock();
      try
      {
         Iterator<PSRelationship> it = relationships.iterator();
         while (it.hasNext())
         {
            update((PSRelationship) it.next());
         }
      }
      finally
      {
         m_rwlock.writeLock().unlock();
      }
   }

   /**
    * Updates the supplied relationship to the cache. The relationship will
    * be added into the cache if not exist. Do nothing if it is not a folder
    * relationship.
    *
    * <p>Note: Assume the caller is locked by {@see #m_rwlock.writeLock().lock()}.
    *
    * @param relationship the to be updated relationship, assume never
    *    <code>null</code>.
    */
   private void update(PSRelationship relationship)
   {
      boolean isFolder = isFolderRelationship(relationship);
      boolean isAa = isAARelationship(relationship);
      if (!isFolder && !isAa)
         return;

      Integer rid = Integer.valueOf(relationship.getId());
      PSLocator parent = relationship.getOwner();
      PSLocator child = relationship.getDependent();
      int configId = relationship.getConfig().getId();

      if (isFolder)
      {
         FolderRelationship cache = m_relationshipMap.get(rid);
         boolean insertToCache = false;
         if (cache == null)
         {
            insertToCache = true;
         }
         else if (cache.m_parentId != parent.getId()
               || cache.m_childId != child.getId()
               || cache.m_configId != configId)  //include the relationship config id in case it has been changed.
         {
            delete(relationship);
            insertToCache = true;
         }
   
         if (insertToCache)
         {
            addRelationship(rid, parent, child, relationship.getConfig().getId());
   
            log.debug("Inserted {} with relationship rid: {}", relationship.getConfig().getName() , rid);
         }
      } else {
         PSRelationshipData cache = m_aARelationshipMap.get(rid);
         int sort = NumberUtils.toInt(relationship.getProperty(PSRelationshipConfig.PDU_SORTRANK));
         Integer folderId = relationship.getLegacyFolderId();
         if (folderId==null) folderId = Integer.valueOf(-1);
         int variantId = NumberUtils.toInt(relationship.getProperty(PSRelationshipConfig.PDU_VARIANTID));
         int slotId = NumberUtils.toInt(relationship.getProperty(PSRelationshipConfig.PDU_SLOTID));
         configId = relationship.getConfig().getId();
         boolean insertToCache = false;
         if (cache == null)
         {
            insertToCache = true;
         }
         else if ((variantId != cache.getVariantId() || (cache.getFolderId() != folderId)
               || cache.getSortRank() != sort || cache.getOwnerId() != parent.getId() || cache.getOwnerRevision() != parent.getRevision()
               || cache.getDependentId() != child.getId() || cache.getDependentRevision() != relationship.getDependent().getRevision()
               || cache.getSlotId() != slotId)
               || cache.getConfigId() != configId) {
            delete(relationship);
            insertToCache = true;
         }
   
         if (insertToCache)
         {
            PSRelationshipData rel = new PSRelationshipData();
            rel.setId(relationship.getId());
            rel.setConfig(relationship.getConfig());
            rel.setConfigId(relationship.getConfig().getId());
            rel.setDependentId(relationship.getDependent().getId());
            rel.setDependentRevision(relationship.getDependent().getRevision());
            rel.setOwnerId(relationship.getOwner().getId());
            rel.setOwnerRevision(relationship.getOwner().getRevision());
            rel.setPersisted(relationship.isPersisted());
            Integer siteId = relationship.getLegacySiteId();
            rel.setSiteId(siteId != null ? (long)siteId : -1);
            rel.setSortRank(NumberUtils.toInt(relationship.getProperty(PSRelationshipConfig.PDU_SORTRANK)));
            rel.setInlineRelationship(relationship.getProperty(PSRelationshipConfig.PDU_INLINERELATIONSHIP));
            rel.setVariantId(variantId);
            rel.setFolderId(folderId !=null ? folderId : -1);
            rel.setSlotId(NumberUtils.toLong(relationship.getProperty(PSRelationshipConfig.PDU_SLOTID)));
            rel.setWidgetName(relationship.getProperty(PSRelationshipConfig.PDU_WIDGET_NAME));
            addAARelationship(rel);
   
            log.debug("insert aa relationship rid: {} ", rid);
         }
      }
   }

   /**
    * Log error message for the supplied parameters. This is used when fail to
    * add a folder relationship.
    *
    * @param rid the relationship id, assume not <code>null</code>.
    * @param parent the parent folder id, assume not <code>null</code>.
    * @param child the child id, assume not <code>null</code>.
    * @param msg the error message, assume not <code>null</code>.
    */
   private void logError(Integer rid, int parent, int child, String msg)
   {
      log.error("A validation error occurred while modifying the folder "
         + "tree cache. The offending node is being skipped, but an "
         + "administrator should clean up the database manually if the error "
         + "was a circular dependency."
         + "\r\nThe problem was: {} "
         + "\r\nThe parent content id = {} "
         + "\r\nThe child content id = {} "
         + "\r\nThe relationship id = {} ", msg, parent, child, rid);
   }

   /**
    * Adds a new folder relationship from the supplied parameters. Log error
    * and do nothing if fail to validate the parameters.
    *
    * @param rid the relationship id, assume not <code>null</code>.
    * @param parent the parent folder id, assume not <code>null</code>.
    * @param child the child id, assume not <code>null</code>.
    * @param configId the id of the relationship type, assume not <code>null</code>.
    *
    * <p>Note: Assume the caller is synchronized by {@see #m_cacheMonitor}.
    */
   private void addRelationship(Integer rid, PSLocator parent, PSLocator child, int configId)
   {
      // Validating the relationship during the folder cache starting process.
      // But not to validate it after it is started.
      // This is because the validation will fail on move operation where
      // the relationship processor adds the new folder relationship first,
      // then deletes the old once.
      
      if (! m_isStarted)
      {
         if (! validateNewRelationship(rid, parent, child, configId))
            return; // do nothing for invalid folder relationship
      }

      // now it is safe to add the relationship
      FolderRelationship cache = new FolderRelationship(parent.getId(),
            child.getId(), configId);
      m_relationshipMap.put(rid, cache);
      m_graph.addRelationship(rid, parent, child);
   }

   /**
    * Adds a new Aa relationship from the supplied parameters. Log error
    * and do nothing if fail to validate the parameters.
    *
    * @param rdata The relationship data
    *
    * <p>Note: Assume the caller is synchronized by {@see #m_cacheMonitor}.
    */
   private void addAARelationship(PSRelationshipData rdata)
   {
      PSLocator ownerLocator = new PSLocator(rdata.getOwnerId(),rdata.getOwnerRevision());
      PSLocator depLocator = new PSLocator(rdata.getDependentId(),rdata.getDependentRevision());

      // Validating the relationship during the folder cache starting process.
      // But not to validate it after it is started.
      // This is because the validation will fail on move operation where
      // the relationship processor adds the new folder relationship first,
      // then deletes the old once.
      if (! m_isStarted)
      {
         if (! validateNewAaRelationship(rdata.getId(),ownerLocator, rdata.getDependentId()))
            return; // do nothing for invalid folder relationship
      }
      m_aARelationshipMap.put(rdata.getId(), rdata);
      m_aa_graph.addRelationship(rdata.getId(),ownerLocator, depLocator,rdata.getSortRank());
   }

   
   /**
    * Validates the supplied parameters for a new folder relationship.
    *
    * @param rid the relationship id, assume not <code>null</code>.
    * @param parent the parent folder id, assume not <code>null</code>.
    * @param child the child id, assume not <code>null</code>.
    * @param configId the relationship config Id, assume not <code>null</code>.
    *
    * @return <code>true</code> if the parameters are valid for creating
    *    a new folder relationship; <code>false</code> otherwise.
    */
   private boolean validateNewRelationship(Integer rid, PSLocator parent,
         PSLocator child, int configId)
   {
      boolean isValid = true;

      // validate parent and child
      IPSItemEntry parentItem = getItem(parent.getId());
      IPSItemEntry childItem = getItem(child.getId());

      // both parent and child must be exist in the item summary cache
      if (parentItem == null)
      {
         logError(rid, parent.getId(), child.getId(),
               "the owner does not exist in the Item Summary cache");
         isValid = false;
      }
      else if (childItem == null)
      {
         logError(rid, parent.getId(), child.getId(),
               "the dependent not exists in Item Summary cache.");
         isValid = false;
      }
      // the parent must be a folder
      else if (! parentItem.isFolder())
      {
         logError(rid, parent.getId(), child.getId(), "the owner is not a folder.");
         isValid = false;
      }
      // validate a child folder if needed
      else if (childItem.isFolder())
      {
         // the child must not be the root folder
         if (child.getId() == PSFolder.ROOT_ID)
         {
            logError(rid,parent.getId(), child.getId(), "the dependent is the ROOT folder.");
            isValid = false;
         }
         else
         {
            // the child must not have a parent already. this is to prevent
            // circular folder relationship
            List<PSGraphEntry> ps = m_graph.getParents(child);
            FolderRelationship folderRel = m_relationshipMap.get(rid);
            if (!ps.isEmpty() && folderRel != null && folderRel.m_configId == configId)
            {
               logError(rid, parent.getId(),  child.getId(),
                     "the dependent is a folder and it already has an owner "
                           + "(ownerid=" + ps.get(0).getValue() + ").");
               isValid = false;
            }
         }
      }

      return isValid;
   }
   
   /**
    * Validates the supplied parameters for a new folder relationship.
    *
    * @param rid the relationship id, assume not <code>null</code>.
    * @param parentLoc the parent folder id, assume not <code>null</code>.
    * @param child the child id, assume not <code>null</code>.
    *
    * @return <code>true</code> if the parameters are valid for creating
    *    a new folder relationship; <code>false</code> otherwise.
    */
   private boolean validateNewAaRelationship(Integer rid, PSLocator parentLoc,
         Integer child)
   {
      boolean isValid = true;

      // validate parent and child
      int parent = parentLoc.getId();
      IPSItemEntry parentItem = getItem(parent);
      IPSItemEntry childItem = getItem(child);

      // both parent and child must be exist in the item summary cache
      if (parentItem == null)
      {
         logError(rid, parent, child,
               "the owner does not exist in the Item Summary cache");
         isValid = false;
      }
      else if (childItem == null)
      {
         logError(rid, parent, child,
               "the dependent not exists in Item Summary cache.");
         isValid = false;
      }
      // the parent must be a folder
      else if (parentItem.isFolder())
      {
         logError(rid, parent, child, "the owner of an Active Assembly Relationship should not be a folder.");
         isValid = false;
      }
      // TODO validate Navigation Items
     

      return isValid;
   }
   
   public PSLocator findChildOfType(PSLocator current,List<Long> types)
   {
      if (types==null || types.isEmpty())
         throw new IllegalArgumentException("Must pass a list of type ids");
      List<PSLocator> childLocators = getChildLocators(current);
      for (PSLocator loc : childLocators)
      {
         IPSItemEntry item = getItem(Integer.valueOf(loc.getId()));
         Long itemTypeId = new Long(item.getContentTypeId());
         if (types.contains(itemTypeId))
         {
            return new PSLocator(loc.getId());
         }
      }
      return null;
   }
   /**
    * Deletes the supplied relationships from the cache. Do nothing is the
    * relationship does not exist in the cache or it is not a folder
    * relationship.
    *
    * @param relationships the to be deleted relationships, never
    *    <code>null</code>.
    */
   public void delete(PSRelationshipSet relationships)
   {
      if (relationships == null)
         throw new IllegalArgumentException("relationships may not be null");

      m_rwlock.writeLock().lock();
      try
      {
         Iterator<PSRelationship> it = relationships.iterator();
         while (it.hasNext())
         {
            delete((PSRelationship) it.next());
         }
      }
      finally
      {
         m_rwlock.writeLock().unlock();
      }
      
   }

   /**
    * Gets the (folder) relationship from the supplied relationship id.
    *
    * @param rid the relationship id.
    *
    * @return the (created) relationship, may be <code>null</code> if the
    *    relationship id does not exist.
    *
    * @throws IllegalStateException if the dependent of the relationship does
    *    not exist in item cache.
    */
   public PSRelationship getRelationship(int rid)
   {
      m_rwlock.readLock().lock();
      try
      {
         return getRelationshipNoLock(rid);
      }
      finally
      {
         m_rwlock.readLock().unlock();
      }
   }
   
   /**
    * The same as {@link #getRelationship(int)}, except this method assumed
    * the caller own the read lock {@see m_rwlock.readLock().lock()}.
    */
   private PSRelationship getRelationshipNoLock(int rid)
   {
      FolderRelationship entry;
      entry = m_relationshipMap.get(Integer.valueOf(rid));

      if (entry == null)
         return null;

      PSRelationshipConfig config;
      config = getRelationshipConfigForId(entry.m_configId);

      if (config == null) {
         config = PSRelationshipCommandHandler.getRelationshipConfig(PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      }

      PSLocator parent = new PSLocator(entry.m_parentId);
      PSLocator child = new PSLocator(entry.m_childId);

      PSRelationship rel = new PSRelationship(rid, parent, child, config);
      IPSItemEntry item = getItem(Integer.valueOf(entry.m_childId));
      if (item == null)
         throw new IllegalStateException(
               "child id, "
                     + entry.m_childId
                     + ", does not exist in item cache, "
                     + "but it is a dependent of relationship id, "
                     + rid);
      rel.setDependentCommunityId(item.getCommunityId());
      rel.setDependentObjectType(item.getObjectType());
      rel.setPersisted(true);

      return rel;
   }

   /**
    * Gets the relationships between the supplied parent and its child items or
    * folders.
    *
    * @param parent
    *           the locator of the parent. Never <code>null</code>.
    *
    * @return the relationships, never <code>null</code>, but may be empty.
    */
   public List<PSRelationship> getChildren(PSLocator parent, PSRelationshipFilter filter)
   {
      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");

      List<PSRelationship> rels = new ArrayList<>();
      m_rwlock.readLock().lock();
      try
      {
         List<PSGraphEntry> childObjs = m_graph.getChildrenList(parent);

         PSRelationship rel;
         Integer rid;
         for (PSGraphEntry child : childObjs)
         {
            rid = (Integer) child.getrelationshipId();
            rel = getRelationshipNoLock(rid.intValue());
            if (!rel.getConfig().getName().equals(filter.getName())) {
               continue;
            }
            if (rel == null)
               throw new IllegalStateException("Cannot find rid=" + rid
                     + ", parentId=" + parent.getId() + ", childId="
                     + child.getValue());

            rels.add(rel);
         }
      }
      finally
      {
         m_rwlock.readLock().unlock();
      }

      return rels;
   }

   /**
    * Gets the relationships between the supplied child and its parent folders.
    *
    * @param child
    *           the locator of the child. Never <code>null</code>.
    *
    * @return the relationships, never <code>null</code>, but may be empty.
    */
   public List<PSRelationship> getParents(PSLocator child)
   {
      if (child == null)
         throw new IllegalArgumentException("child may not be null");

      List<PSRelationship> rels = new ArrayList<>();

      m_rwlock.readLock().lock();
      try
      {
         // For folder need to check revisionless
         PSLocator childLoc = child.getRevision()==-1 ? child : new PSLocator(child.getId());
         List<PSGraphEntry> parentObjs = m_graph.getParents(childLoc);

         PSRelationship rel;
         Integer rid;
         for (PSGraphEntry childEntry : parentObjs)
         {
            rid = childEntry.getrelationshipId();
            rel = getRelationshipNoLock(rid.intValue());
            rels.add(rel);
         }
      }
      finally
      {
         m_rwlock.readLock().unlock();
      }

      return rels;
   }

   /**
    * Gets the locator from the supplied path.
    *
    * @param paths
    *           a list of one or more <code>String</code> objects, which is
    *           the sys_title field value of the items. It must not be not be
    *           <code>null</code> or empty. The first element is the immediate
    *           child of the root folder. The second element is the child of the
    *           first element, so forth. The list of names will be used in case
    *           insensitive manner.
    *
    * @return contentid of the specified path, <code>-1</code> if no such path
    *         exist in the system.
    */
   public int getIdByPath(List<String> paths, String relationshipTypeName)
   {
      if (paths == null || paths.isEmpty())
         throw new IllegalArgumentException("The paths may not be null or empty");

      PSLocator parentId = new PSLocator(Integer.valueOf(PSFolder.ROOT_ID));
      PSLocator childId = parentId;
      String childPath;
      Iterator<String> itPaths = paths.iterator();
      List<PSGraphEntry> children;
      FolderRelationship pathRel;
      PSRelationshipConfig config;
      IPSItemEntry item;
      m_rwlock.readLock().lock();
      try
      {
         while (itPaths.hasNext())
         {
            children = m_graph.getChildrenList(parentId);
            childPath = itPaths.next();
            boolean foundCurPath = false;
            if (children!=null)
            {
               for (PSGraphEntry child : children)
               {
                  childId = child.getValue();
                  item = getItem(childId.getId());
                  pathRel = m_relationshipMap.get(child.getrelationshipId());
                  config = getRelationshipConfigForId(pathRel.m_configId);
                  if (item == null)
                  {
                     log.info("Cannot find content id, {} , in item cache, but it is in folder relationship with rid = {}", child.toString(), child.getrelationshipId().toString());
                  }
                  else if (item.getName().equalsIgnoreCase(childPath) && config != null
                                    && config.getName().equalsIgnoreCase(relationshipTypeName))
                  {
                     foundCurPath = true;
                     break;
                  }
               }
            }
            if (! foundCurPath)
               return -1;
            else
               parentId = childId;
         }
      }
      finally
      {
         m_rwlock.readLock().unlock();
      }

      return childId.getId();
   }

   /**
    * Gets all folder descendants (child, grand child, ...etc) for the 
    * specified parent folder.
    * 
    * @param parentGuid the folder id, never <code>null</code>.
    * 
    * @return a list of folder child ids, which does not include
    *    any item (or none folder) child ids. Never <code>null</code> or empty.
    */
   public List<IPSGuid> getFolderDescendants(IPSGuid parentGuid)
   {
      if (parentGuid == null)
         throw new IllegalArgumentException("parentGuid must not be null.");
      
      Integer parentID = parentGuid.getUUID();
      List<IPSGuid> children = new ArrayList<>();
      
      m_rwlock.readLock().lock();
      try
      {
         getFolderDescendants(parentID, children, false, PSItemSummaryCache
               .getInstance());
      }
      finally
      {
         m_rwlock.readLock().unlock();
      }
      
      return children;
   }

   /**
    * It recursively collects all the folder children of the specified parent
    * folder id.
    * <p>
    * Note, assume the readlock is activated already.
    * 
    * @param parentID the parent folder id, assumed not <code>null</code>.
    * @param children the list, used to collect folder child ids, assume not
    * <code>null</code>.
    * @param isAddFolderID Determines if collecting the specified the given
    * folder id to the child list. The initial call should be <code>false</code>
    * (assume it is the parent folder), it should be <code>true</code> for any
    * subsequent recursive calls. 
    * @param itemCache the item cache object, assumed not <code>null</code>.
    */
   private void getFolderDescendants(Integer parentID, List<IPSGuid> children,
         boolean isAddFolderID, PSItemSummaryCache itemCache)
   {
      List<PSGraphEntry> childObjs = m_graph.getChildrenList(new PSLocator(parentID));

      if (isAddFolderID)
         children.add(new PSLegacyGuid(parentID.intValue(), -1));

      if (childObjs != null)
      {
         Integer cID;
         IPSItemEntry childItem;
         for (PSGraphEntry c : childObjs)
         {
            cID = (Integer) c.getValue().getId();
            childItem = getItem(cID);
            if (childItem.isFolder())
            {
               getFolderDescendants(cID, children, true, itemCache);
            }
         }
      }
   }

   /**
    * Get a snapshot of the current statistics of the folder cache relationship.
    * The structure of the returned element is following:
    * <PRE><CODE>
    *    &lt;--
    *       The cache statistics element with each attribute referring to
    *       each kind of statistics.
    *    --&gt;
    *    &lt;ELEMENT FolderRelationshipCacheStatistics (totalRelationships,
    *       totalParents, totalChildren)
    *     &gt;
    *    &lt;--
    *       totalRelationships - Total number of cached relationships.
    *    --&gt;
    *    &lt;ELEMENT totalRelationships (#PCDATA)&gt;
    *    &lt;--
    *       totalParents - Total number of cached parents.
    *    --&gt;
    *    &lt;ELEMENT totalParents (#PCDATA)&gt;
    *    &lt;--
    *       totalChildren - Total number of cached children.
    *    --&gt;
    *    &lt;ELEMENT totalChildren (#PCDATA)&gt;
    * </CODE></PRE>
    *
    * @param doc the docment used to generate the XML, never <code>null</code>.
    *
    * @return the generated statistics in XML, never <code>null</code>
    */
   public Element getCacheStatistics(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      
      m_rwlock.readLock().lock();
      Element el = doc.createElement("RelationshipCacheStatistics");
      try
      {
         int totalRelationship = m_relationshipMap.size();
         int totalAaRelationship = m_aARelationshipMap.size();
      
      PSXmlDocumentBuilder.addElement( doc, el, "totalFolderRelationships",
         String.valueOf(totalRelationship) );
      PSXmlDocumentBuilder.addElement( doc, el, "totalFolderParents",
            String.valueOf(m_graph.getTotalParent()) );
      PSXmlDocumentBuilder.addElement( doc, el, "totalFolderChildren",
            String.valueOf(m_graph.getTotalChildren()) );
      PSXmlDocumentBuilder.addElement( doc, el, "totalAaRelationships",
            String.valueOf(totalAaRelationship) );
         PSXmlDocumentBuilder.addElement( doc, el, "totalAaParents",
               String.valueOf(m_aa_graph.getTotalParent()) );
         PSXmlDocumentBuilder.addElement( doc, el, "totalAaChildren",
               String.valueOf(m_aa_graph.getTotalChildren()) );

      }
      finally
      {
         m_rwlock.readLock().unlock();
      }
      
      return el;
   }

   public void deleteOwnerRevisions(int ownerid, Collection<Integer> revisions)
   {
   
      for (int rev : revisions)
      {
         PSLocator locator = new PSLocator(ownerid,rev);
         List<PSRelationship> rels = getAaChildren(locator,null);
         if (rels!=null)
         {
            for (PSRelationship rel :rels)
               delete(rel);
         }
         
      }
   }
   
   /**
    * Deletes the supplied relationship from the cache. Do nothing if the
    * relationship does not exist in the cache or it is not a folder
    * relationship.
    *
    * <p>Note: Assume the caller is locked by {@see #m_rwlock.writeLock().lock()}.
    *
    * @param relationship the to be deleted relationship, assume never
    *    <code>null</code>.
    */
   private void delete(PSRelationship relationship)
   {
      if (isFolderRelationship(relationship) )
      {
         Integer rid = Integer.valueOf(relationship.getId());
         deleteFolderRel(rid, true);
      }
      else if (isAARelationship(relationship) )
      {
         Integer rid = Integer.valueOf(relationship.getId());
         deleteAaRel(rid,true);
      }
   } 
  
  /*
   * A convenience method, just like {@link #delete(PSRelationship)}, except
   * it accept a different parameters.
   *
   * @param rid the to be deleted relationship id, assume not <code>null</code>.
   * @param isValid <code>true</code> if delete a valid relationship;
   *    otherwise, log a warning message for deleting an invalid relationship.
   */
  private void deleteFolderRel(Integer rid, boolean isValid)
  {
     FolderRelationship cache = m_relationshipMap.get(rid);
     if (cache != null)
     {
        m_relationshipMap.remove(rid);

        m_graph.removeRelationship(rid, new PSLocator(cache.m_parentId),
              new PSLocator(cache.m_childId));

        if (isValid)
           log.debug("delete relationship rid: {} ", rid);
        else
           log.warn("delete invalid folder relationship (rid,ownerid,dependentid) : ({}, {}, {})", rid, cache.m_parentId, cache.m_childId);
     }
     else
     {
        log.debug("delete non-existing relationship rid: {} ", rid);
     }
  }
  
  private void deleteAaRel(Integer rid, boolean isValid)
  {
     PSRelationshipData cache =m_aARelationshipMap.get(rid);
     if (cache != null)
     {
        m_aARelationshipMap.remove(rid);

        m_aa_graph.removeRelationship(rid, new PSLocator(cache.getOwnerId(),cache.getOwnerRevision()),
              new PSLocator(cache.getDependentId(),cache.getDependentRevision()));

        if (isValid)
           log.debug("delete relationship rid: {}", rid);
        else
           log.warn("delete invalid Active Assembly relationship (rid,ownerid,dependentid) : ( {}, {}, revision {}, {})", rid, cache.getOwnerId(), cache.getOwnerRevision(), cache.getDependentId());
     }
     else
     {
        log.debug("delete non-existing relationship rid: {} ", rid);
     }
  }

   /**
    * Cleanup the folder structure, to make sure there is only one ROOT folder,
    * which id is <code>PSFolder.ROOT_ID</code>; remove other folders which are
    * not the descendents of the known ROOT folder.
    */
   private void cleanupFolders()
   {
      // get valid folders
      Set<PSLocator> validFolders = new HashSet<>();
      walkSubFolders(new PSLocator(PSFolder.ROOT_ID), validFolders);

      // get invalid folders
      Set<PSLocator> invalidFolders = new HashSet<>(m_graph.getAllParents());
      invalidFolders.removeAll(validFolders);

      // remove all relationships which relate to the invalid folders
      Iterator<PSLocator> folders = invalidFolders.iterator();
      List<PSGraphEntry> children = new ArrayList<>();
      
      while (folders.hasNext()) {
         for (PSGraphEntry child : m_graph.getChildrenList(folders.next())) {
            children.add(child);
         }
      }
      
      for (PSGraphEntry child : children) {
         deleteFolderRel(child.getrelationshipId(), false);
      }
   }

   /**
    * Walks the sub folders for the supplied parent id and collects all visited
    * folder ids.
    *
    * @param psLocator the parent id, assume not <code>null</code>.
    * @param visitedFolders a set over zero or more <code>Integer</code>
    *    objects, used to collect all visited folder ids. Assume not
    *    <code>null</code>. The <code>parentId</code> and its sub-folders will
    *    be added into this set.
    */
   private void walkSubFolders(PSLocator psLocator, Set<PSLocator> visitedFolders)
   {
      visitedFolders.add(psLocator);

      // visit immediate sub folders
      List<PSGraphEntry> children = m_graph.getChildrenList(psLocator);
      PSLocator child;
      IPSItemEntry childItem;
      if (children == null)
         return;
      
      for (PSGraphEntry childEntry : children)
      {
         child = childEntry.getValue();
         childItem = getItem(child.getId());
         if (childItem != null && childItem.isFolder())
            walkSubFolders(child, visitedFolders);  // recursive to sub folders
      }
   }
   /**
    * Gets the item from the item cache for the given Content ID.
    * 
    * @param id the content id, assumed not <code>null</code>.
    * 
    * @return the item, may be <code>null</code> if not found.
    */
   private IPSItemEntry getItem(Integer id)
   {
      if (ms_itemcache == null)
         throw new IllegalStateException("ms_intemcache must not be null.");
      
      return ms_itemcache.getItem(id);
   }

   /**
    * Determines whether the supplied relationship is a folder type
    * relationship.
    *
    * @param relationship the relationship object, assume not <code>null</code>.
    *
    * @return <code>true</code> if it is a folder type relationship; otherwise
    *    return <code>false</code>.
    */
   private boolean isFolderRelationship(PSRelationship relationship)
   {
      PSRelationshipConfig config = relationship.getConfig();
      return config.getName().equalsIgnoreCase(
            PSRelationshipConfig.TYPE_FOLDER_CONTENT) ||
              config.getName().equalsIgnoreCase(PSRelationshipConfig.TYPE_RECYCLED_CONTENT);
   }

   /**
    * Determines whether the supplied relationship is a Aa type
    * relationship.
    *
    * @param relationship the relationship object, assume not <code>null</code>.
    *
    * @return <code>true</code> if it is a folder type relationship; otherwise
    *    return <code>false</code>.
    */
   private boolean isAARelationship(PSRelationship relationship)
   {
      PSRelationshipConfig config = relationship.getConfig();
      return config.getCategory().equals(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
   }
   
   
   public List<PSRelationship> getAaChildren(PSLocator parent, String slot)
   {
      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");

      List<PSRelationship> rels = new ArrayList<>();

      m_rwlock.readLock().lock();
      try
      {

         PSRelationship rel;
         Integer rid;
         for (PSGraphEntry child : m_aa_graph.getChildrenList(parent))
         {
            rid = (Integer) child.getrelationshipId();
            rel = getAaRelationshipNoLock(rid.intValue());
            if (rel == null)
            {
               
               log.debug("Cannot find rid= {} , parentId= {} , rev= {} , childId= {}", rid, parent.getId(), parent.getRevision(), child.getValue());
               continue;
            }
            String relSlot = rel.getProperty("sys_slotid");
            if (slot==null || StringUtils.equals(relSlot, slot))
               rels.add(rel);
         }
      }
      finally
      {
         m_rwlock.readLock().unlock();
      }

      return rels;
   }

   public Collection<PSRelationship> getAAParents(boolean publicRev, boolean tip, boolean current, String slot, PSLocator child)
   {
      if (child == null)
         throw new IllegalArgumentException("child may not be null");

      List<PSRelationship> rels = new ArrayList<>();

      m_rwlock.readLock().lock();
      try
      {
         List<PSGraphEntry> parentObjs = m_aa_graph.getParents(child);

         PSRelationship rel;
         Integer rid;
         for (PSGraphEntry parent : parentObjs)
         {
            rid = (Integer) parent.getrelationshipId();
            rel = getAaRelationshipNoLock(rid.intValue());
            if (rel==null)
            {
               
               log.debug("Cannot find rel= {} for child {} ", rid, child.getId());
            }else {
               PSLocator owner = rel.getOwner();
               int revision = owner.getRevision();
               boolean slotMatch = (slot == null || StringUtils.equals(rel.getProperty("sys_slotid"), slot));
               IPSItemEntry item = getItemCache().getItem(rel.getOwner().getId());
               if (item == null) {
                  log.warn("Owner {} of relationship {} cannot be found : skipping ", rel.getOwner().getId(), rid);
                  continue;
               }
               if (slotMatch && ((publicRev && item.getPublicRevision() == revision)
                       || (tip && item.getTipRevision() == revision)
                       || (current && item.getCurrentRevision() == revision)
               )) {
                  rels.add(rel);
               }
            }
            
         }
      }
      finally
      {
         m_rwlock.readLock().unlock();
      }

      return rels;
   }

   private PSRelationship getAaRelationshipNoLock(int rid)
   {
      PSRelationshipData entry = m_aARelationshipMap.get(Integer.valueOf(rid));

      if (entry == null)
      {
         if (log.isDebugEnabled())
            log.debug("Aa Relationshp cache miss for rid= {} ", rid);
         return null;
      }

   
      IPSItemEntry item = getItem(Integer.valueOf(entry.getOwnerId()));
      if (item == null)
      {
         log.warn(
               "owner id, {} , does not exist in item cache, but it is an owner of a relationship id, {} , which has dependent id: {} ", entry.getOwnerId(), rid, entry.getDependentId());
         return null;
      }
      IPSItemEntry dependent = getItem(Integer.valueOf(entry.getDependentId()));
      if (dependent == null)
      {
         log.warn(
               "child id, {} , does not exist in item cache, but it is a dependent of relationship id, {} ", entry.getDependentId(), rid);
         return null;
      }
      
      PSRelationship rel = new PSRelationship(entry.getId(), 
            new PSLocator(entry.getOwnerId(), entry.getOwnerRevision()),
            new PSLocator(entry.getDependentId(), entry.getDependentRevision()),
            entry.getConfig());
      rel.setPersisted(true);
      
      // set user properties
      Set<String> pnames = entry.getConfig().getUserProperties().keySet();
 
      for (String pname : pnames)
      {
         PSRelationshipPropertyData propdata = getUserProperty(entry,pname);
         if (propdata!=null)
            rel.setUserProperty(propdata);
      }
     
      rel.setDependentCommunityId(dependent.getCommunityId());
      rel.setDependentObjectType(dependent.getObjectType());
      rel.setPersisted(true);
      
      return rel;
   }

   private PSRelationshipPropertyData getUserProperty(PSRelationshipData r,
         String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name may not be null.");
      
      PSRelationshipPropertyData retProp = r.getProperty(name);
      if (retProp != null)
         return retProp;

      // try the pre-defined user properties
      else if (name.equalsIgnoreCase(PSRelationshipConfig.PDU_FOLDERID))
         retProp = getIntProp(PSRelationshipConfig.PDU_FOLDERID, r
               .getFolderId());

      else if (name.equalsIgnoreCase(PSRelationshipConfig.PDU_INLINERELATIONSHIP))
         retProp = new PSRelationshipPropertyData(
               PSRelationshipConfig.PDU_INLINERELATIONSHIP, r
                     .getInlineRelationship());

      else if (name.equalsIgnoreCase(PSRelationshipConfig.PDU_SITEID))
         retProp = getLongProp(PSRelationshipConfig.PDU_SITEID, r.getSiteId());

      else if (name.equalsIgnoreCase(PSRelationshipConfig.PDU_SLOTID))
         retProp = getLongProp(PSRelationshipConfig.PDU_SLOTID, r.getSlotId());

      else if (name.equalsIgnoreCase(PSRelationshipConfig.PDU_SORTRANK))
         retProp = getIntProp(PSRelationshipConfig.PDU_SORTRANK, r
               .getSortRank());

      else if (name.equalsIgnoreCase(PSRelationshipConfig.PDU_VARIANTID))
         retProp = getLongProp(PSRelationshipConfig.PDU_VARIANTID, r
               .getVariantId());
      
      else if (name.equalsIgnoreCase(PSRelationshipConfig.PDU_WIDGET_NAME))
         retProp = new PSRelationshipPropertyData(PSRelationshipConfig.PDU_WIDGET_NAME, r
               .getWidgetName());

      if (retProp != null)
         retProp.setPersisted(r.isPersisted());
      
      return retProp;
   }
   

   /**
    * Creates a relationship property from a name and an integer value.
    * 
    * @param name the name of the property, assumed not <code>null</code> or
    *    empty.
    * @param value the value of the property. It may be <code>-1</code> if 
    *    the value of this property is unknown (or <code>null</code> in the
    *    repository).
    * 
    * @return the created relationship property, never <code>null</code>.
    */
   private PSRelationshipPropertyData getIntProp(String name, int value)
   {
      PSRelationshipPropertyData prop;
      
      if (value == -1)
         prop = new PSRelationshipPropertyData(name, null);
      else
         prop = new PSRelationshipPropertyData(name, String.valueOf(value));
      
      return prop;
   }
   
   /**
    * Creates a relationship property from a name and a long value.
    * 
    * @param name the name of the property, assumed not <code>null</code> or
    *           empty.
    * @param value the value of the property. It may be <code>-1</code> if the
    *           value of this property is unknown (or <code>null</code> in the
    *           repository).
    * 
    * @return the created relationship property, never <code>null</code>.
    */
   private PSRelationshipPropertyData getLongProp(String name, long value)
   {
      PSRelationshipPropertyData prop;

      if (value == -1)
         prop = new PSRelationshipPropertyData(name, null);
      else
         prop = new PSRelationshipPropertyData(name, String.valueOf(value));

      return prop;
   }   

   
   
   /**
    * The singleton instance of the {@link PSFolderRelationshipCache} class.
    * Initialized by {@link #createInstance()}, never <code>null</code> after
    * that.
    */
   private static PSFolderRelationshipCache ms_instance = null;

   /**
    * The item cache. It is initialized by {@link #createInstance()}, never
    * <code>null</code> after that.
    */
   private static PSItemSummaryCache ms_itemcache = null;

   /**
    * A relationship between relationship / config_id and respective {@link PSRelationshipConfig}
    * by id.
    */
   private static Map<Integer, PSRelationshipConfig> ms_relationshipConfigsById = null;

   /**
    * A relationship between relationship / config_id and respective {@link PSRelationshipConfig}
    * by name.
    */
   private static Map<String, PSRelationshipConfig> ms_relationshipConfigsByName = null;
   
   /**
    * It contains all folder relationships. It maps the relationship id to its
    * corresponding {@link FolderRelationship} object. The map keys are
    * <code>Integer</code> objects; the map values are
    * <code>FolderRelationship</code> objects. It is initialized by ctor, never
    * <code>null</code> after that, but may be empty.
    */
   private Map<Integer,FolderRelationship> m_relationshipMap;
   private Map<Integer,PSRelationshipData> m_aARelationshipMap;

   /**
    * The graph representation of the relationships in
    * {@link #m_relationshipMap}. It is initialized by ctor, never
    * <code>null</code> after that.
    */
   private PSRelationshipGraph m_graph;
   private PSRelationshipGraph m_aa_graph;

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static final Logger log = LogManager.getLogger("FolderCache");

   /**
    * Indicates whether it is initialized and in caching mode. <code>true</code>
    * if it is in caching mode; otherwise it is not in caching mode. Default to
    * be <code>false</code>
    */
   private boolean m_isStarted = false;

   /**
    * Internal class, used to represent a folder relationship in the cache.
    */
   private class FolderRelationship
   {
      /**
       * Constructs a folder relationship object from a parent and child id.
       *
       * @param parentId the parent id of the folder relationship.
       * @param childId the child id of the folder relationship.
       */
      FolderRelationship(int parentId, int childId, int configId)
      {
         m_parentId = parentId;
         m_childId = childId;
         m_configId = configId;
      }

      private int m_parentId;
      private int m_childId;
      private int m_configId;
   }

   /**
    * This reader/writer lock allows safe update of the folder relationship while
    * allowing general access for readers. The read lock is taken for normal
    * operations. The write lock is taken when the item def manager updates the
    * content repository information.
    */
   private ReentrantReadWriteLock m_rwlock = new ReentrantReadWriteLock(true);

   //Default number of levels / recursion
   public static final int DEFAULT_MAX_RECURSION=20;

   //Property name for the maximum number of levels in the cache tree
   private static final String MAX_RECURSION_PROP = "maxFolderCacheLevels";
}
