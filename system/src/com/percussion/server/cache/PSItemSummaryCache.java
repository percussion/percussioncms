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
package com.percussion.server.cache;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSFolderAcl;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSFolderSecurityManager;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.data.IPSTableChangeListener;
import com.percussion.data.PSTableChangeEvent;
import com.percussion.data.PSUpdateHandler;
import com.percussion.design.objectstore.*;
import com.percussion.error.PSException;
import com.percussion.server.*;
import com.percussion.services.legacy.IPSCmsObjectMgrInternal;
import com.percussion.services.legacy.IPSItemEntry;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.legacy.data.PSItemEntry;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.util.PSCollection;
import com.percussion.util.PSStopwatch;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.percussion.util.PSDataTypeConverter.parseStringToDate;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.notNull;

/**
 * This class is used to cache all items and folders. However, it only
 * caches the skeleton, name and Acl (for folder), of the items at the
 * beginning.  The complete folder objects will be lazily loaded.
 */
public class PSItemSummaryCache implements IPSTableChangeListener
{

   
   public enum REVISION_LABELS {
      TIP, CURRENT, PUBLIC
   }

   private Map<Integer,PSItemEntry> m_checkedOutItems;

   private Map<Integer, PSItemEntry> m_publicItems;

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
   static PSItemSummaryCache createInstance()
         throws PSCacheException
   {
      if (ms_instance != null)
         throw new IllegalStateException(
               "PSItemSummaryCache has already been created");

      ms_instance = new PSItemSummaryCache();

      return ms_instance;
   }

   /**
    * Private constructor. Must use {@link #createInstance()} to create an
    * instance.
    */
   private PSItemSummaryCache()
   {
   }

   /**
    * Returns the singleton instance of this class. This singleton instance
    * must be instantiated by call {@link #createInstance()}
    *
    * @return the singleton instance of this class, may be <code>null</code>
    *    if it has not been created or initialized.
    */
   public static PSItemSummaryCache getInstance()
   {
      return ms_instance;
      }

   /**
    * It populates the cache with name and acl for all folders. Do nothing if
    * it is already started.
    * <p>
    * Note: Assume the caller is sychronized by {@link #m_cacheMonitor}.
    *
    * @throws PSCacheException if an error occurs.
    */
   private void start() throws PSCacheException
   {
      if (m_isStarted)
         return; // already started

      PSStopwatch watch = new PSStopwatch();
      watch.start();

      m_items.clear();
      
      // load all items
      LoadItems();
      
      // load all folder ACL's
      loadFolderAcls();
      
      // load the cached properties value for all folders
      LoadFolderProperties();
      
      m_isStarted = true;

      watch.stop();
      log.debug("start elapse time: {} ", watch.toString());
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
      synchronized (m_cacheMonitor)
      {
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
   }

   /**
    * Stops the caching operation and release all cached data.
    * <p>
    * Note: Assume the caller is sychronized by {@link #m_cacheMonitor}.
    */
   private void stop()
   {
      if (! m_isStarted)
         return; // already stopped

      m_isStarted = false;
      m_items.clear();

   }
   
   /**
    * Convenience method, 
    * call {@link #getCachedItem(Integer) getCacheItem(Integer.valueof(id))} 
    */
   private PSItemEntry getCachedItem(int id)
   {
      return getCachedItem(Integer.valueOf(id));
   }

   /**
    * Returns a item from the cache with the supplied id. 
    * 
    * @param id the content id, assume not <code>null</code>.
    * 
    * @return the internal cached item. It may be <code>null</code> if the item does not exist in cache.
    */
   private PSItemEntry getCachedItem(Integer id)
   {
         return (PSItemEntry) m_items.get(id);
   }
   
   /**
    * This class is used to determine if a set of columns (of the CONTENTSTATUS table)
    * contains required (updated) columns for insert, update or delete operation upon receiving 
    * a table change event notification.
    * 
    * @author YuBingChen
    *
    */
   private class FoundColumns
   {
      Map<String, Boolean> mi_foundColumns = new HashMap<>();
      
      /**
       * Constructs the object for a list of updated columns.
       * @param updateColumns the list of updated columns in question, assumed not <code>null</code>.
       */
      FoundColumns(Iterator<PSUpdateColumn> updateColumns)
      {
         for (String col : ms_columns)
            mi_foundColumns.put(col, Boolean.FALSE);     
         
         lookupUpdatedColumns(updateColumns);
      }
      
      /**
       * Look up (or processing) the given updated columns before determine if the columns
       * contain required columns for insert, update or delete operations.
       * 
       * @param updateColumns the columns in question, not <code>null</code>.
       */
      void lookupUpdatedColumns(Iterator<PSUpdateColumn> updateColumns)
      {
         PSBackEndColumn column;
         PSUpdateColumn updateColumn;
         PSBackEndTable sysTable;
         while (updateColumns.hasNext())
         {
            updateColumn = updateColumns.next();
            column = updateColumn.getColumn();
            sysTable = column.getTable();
            if (sysTable.getTable().equalsIgnoreCase(
                  IPSConstants.CONTENT_STATUS_TABLE))
            {
               String colName = column.getColumn().toUpperCase();
               Boolean value = mi_foundColumns.get(colName);
               if (value != null)
                  mi_foundColumns.put(colName, Boolean.TRUE);
            }
         }
      }
      
      /**
       * Determines if the object contains required columns for update operation.
       * @return <code>true</code> if it contains required columns for update.
       */
      boolean hasUpdateColumns()
      {
         return mi_foundColumns.get(CONTENTID_COLUMN).booleanValue()
               && (mi_foundColumns.get(TITLE_COLUMN).booleanValue()
                     || mi_foundColumns.get(CONTENTLASTMODIFIEDDATE_COLUMN).booleanValue()
                     || mi_foundColumns.get(CONTENTPOSTDATE_COLUMN).booleanValue()
                     || mi_foundColumns.get(CONTENTSTATEID_COLUMN).booleanValue() 
                     || mi_foundColumns.get(WORKFLOWAPPID_COLUMN).booleanValue()
                     || mi_foundColumns.get(CONTENTPUBLISHDATE_COLUMN).booleanValue());
      }
      
      /**
       * Determines if the object contains required columns for insert operation.
       * @return <code>true</code> if it contains required columns for insert.
       */
      boolean hasInsertColumns()
      {
         return mi_foundColumns.get(CONTENTID_COLUMN).booleanValue()
               && mi_foundColumns.get(TITLE_COLUMN).booleanValue()
               && mi_foundColumns.get(OBJECTTYPE_COLUMN).booleanValue()
               && mi_foundColumns.get(CONTENTTYPEID_COLUMN).booleanValue();
      }
      
      /**
       * Determines if the object contains required columns for delete operation.
       * @return <code>true</code> if it contains required columns for delete.
       */
      boolean hasDeleteColumns()
      {
         return mi_foundColumns.get(CONTENTID_COLUMN).booleanValue();
      }
      
   }
   
   /**
    * Setup the table change listener. Let the handler notify us if the
    * supplied request handler is an update handler, which updates the 
    * <code>CONTENTSTATUS</code> table for the following columns:
    * <p>
    *    <code>CONTENTID</code>
    *    <code>TITLE</code>
    *    <code>COMMUNITYID</code>
    *    <code>CONTENTTYPEID</code>
    *    <code>OBJECTTYPE</code>
    * <p>
    *
    * @param requestHandler the request handler. It will be ignored if it is
    *    not an instance of <code>PSUpdateHandler</code>.
    */
   public void initNotifyListener(IPSRequestHandler requestHandler)
   {
      if (requestHandler instanceof PSUpdateHandler)
      {
         // see if the update handler contains a table we care about.
         PSUpdateHandler uh = (PSUpdateHandler) requestHandler;
         PSDataSet ds = uh.getDataSet();
         PSPipe pipe = ds.getPipe();
         if (!(pipe instanceof PSUpdatePipe))
            return;
   
         PSUpdatePipe upPipe = (PSUpdatePipe) pipe;
         PSDataSynchronizer sync = upPipe.getDataSynchronizer();
   
         PSCollection tableCol = upPipe.getBackEndDataTank().getTables();
   
         boolean added = false;
         Iterator tables = tableCol.iterator();
         while (tables.hasNext() && !added)
         {
            PSBackEndTable table = (PSBackEndTable) tables.next();
   
            // interested in CONTENTSTATUS table changes only
            if (!table.getTable().equalsIgnoreCase(IPSConstants.CONTENT_STATUS_TABLE))
               continue;

            FoundColumns foundColumns = new FoundColumns(sync.getUpdateColumns().iterator());

            // register the notification if needed
            if (sync.isUpdatingAllowed() && foundColumns.hasUpdateColumns())
            {
               // to be notified after update
               uh.addTableChangeListener(this);
               return;
            }
            else if (sync.isInsertingAllowed() && foundColumns.hasInsertColumns())
            {
               // to be notified after insert
               uh.addTableChangeListener(this);
               return;
            }
            else if (sync.isDeletingAllowed() && foundColumns.hasDeleteColumns())
            {
               // to be notified after delete
               uh.addTableChangeListener(this);
               return;
            }
         }
      }
   }

   // see IPSTableChangeListener interface
   public Iterator getColumns(String tableName, int actionType)
   {
      if (tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName may not be null or empty");

      return Arrays.asList(ms_columns).iterator();
   }

   // see IPSTableChangeListener interface
   public void tableChanged(PSTableChangeEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event may not be null");

      if (!m_isStarted) // not in caching mode.
         return;

      if (event.getTableName().equalsIgnoreCase(
         IPSConstants.CONTENT_STATUS_TABLE))
         contentStatusChanged(event);
   }
   
   /**
    * Container class used to process the table change event.
    * 
    * @author yubingchen
    */
   private class NotifiedItemInfo
   {
      private String mi_contentIdS;
      private String mi_contentTypeIdS;
      private String mi_title;
      private String mi_communityIdS;
      private String mi_objectTypeS;
      private String mi_workflowIdS;
      private String mi_stateIdS;
      private String mi_createByUserName;
      private String mi_lastModifiedDateS;
      private String mi_createdDateS;
      private String mi_postDateS;
      private String mi_tipRevision;
      private String mi_currentRevision;
      private String mi_publicRevision;
      private String mi_lastModifierS;
      private String ms_lastCheckedOutUsername;
      /**
       * Creates the object from the table columns.
       * 
       * @param columns the table columns, assumed not <code>null</code>.
       */
      private NotifiedItemInfo(Map columns)
      {
         mi_contentIdS = (String) columns.get(CONTENTID_COLUMN);
         mi_contentTypeIdS = (String) columns.get(CONTENTTYPEID_COLUMN);
         mi_title = (String) columns.get(TITLE_COLUMN);
         mi_communityIdS = (String) columns.get(COMMUNITYID_COLUMN);
         mi_objectTypeS = (String) columns.get(OBJECTTYPE_COLUMN);
         mi_workflowIdS = (String) columns.get(WORKFLOWAPPID_COLUMN);
         mi_stateIdS = (String) columns.get(CONTENTSTATEID_COLUMN);
         mi_createByUserName = (String) columns.get(CONTENTCREATEDBY_COLUMN);         
         mi_lastModifiedDateS = (String) columns.get(CONTENTLASTMODIFIEDDATE_COLUMN);
         mi_createdDateS = (String) columns.get(CONTENTCREATEDDATE_COLUMN);

         mi_postDateS = (String) columns.get(CONTENTPOSTDATE_COLUMN);
         mi_tipRevision = (String) columns.get(TIPREVISION_COLUMN);
         mi_currentRevision = (String) columns.get(CURRENTREVISION_COLUMN);
         mi_publicRevision = (String) columns.get(PUBLIC_REVISION_COLUMN);
         mi_lastModifierS = (String) columns.get(CONTENTLASTMODIFIER_COLUMN);
         ms_lastCheckedOutUsername = (String) columns.get(CONTENTCHECKOUTUSERNAME_COLUMN);
      }
      
      /**
       * Determines if this object contains all required data for insertion.
       * @return <code>true</code> if it does.
       */
      private boolean hasInsertData()
      {
         return isNotBlank(mi_contentIdS) &&
            isNotBlank(mi_contentTypeIdS) &&
            isNotBlank(mi_title) &&
            isNotBlank(mi_objectTypeS);
      }
      
      /**
       * Determines if this object contains all required data for updates.
       * @return <code>true</code> if it does.
       */
      private boolean hasUpdateData()
      {
         return isNotBlank(mi_contentIdS) && 
         isNotBlank(mi_contentTypeIdS) ||
         isNotBlank(mi_title) ||
         isNotBlank(mi_communityIdS) ||
         isNotBlank(mi_objectTypeS) ||
         isNotBlank(mi_workflowIdS) ||
         isNotBlank(mi_stateIdS) ||
         isNotBlank(mi_createByUserName) ||
         isNotBlank(mi_lastModifiedDateS) ||
         isNotBlank(mi_lastModifierS) ||
         isNotBlank(mi_createdDateS) ||
         isNotBlank(mi_postDateS);  
      }
      
      /**
       * Determines if this object contains all required data for deletion.
       * @return <code>true</code> if it does.
       */
      private boolean hasDeleteData()
      {
         return isNotBlank(mi_contentIdS);
      }
   }
   
   /**
    * Handles table change events for the <code>CONTENTSTATUS</code> table.
    * 
    * @param event the table change event, assumed not <code>null</code> and
    *    for the <code>CONTENTSTATUS</code> table.
    */
   private void contentStatusChanged(PSTableChangeEvent event)
   {
      int action = event.getActionType();
      NotifiedItemInfo itemInfo = new NotifiedItemInfo(event.getColumns());

      if ((action == PSTableChangeEvent.ACTION_INSERT) && (!itemInfo.hasInsertData()))
      {
         // skip if not all data exists for insert
         return;
      }
      else if ((action == PSTableChangeEvent.ACTION_UPDATE) && (!itemInfo.hasUpdateData()))
      {
         // skip if not all data exist for update
         return;
      } 
      else if (! itemInfo.hasDeleteData())
      {
         // skip if not all data exists for delete
         return;
      }

      switch (action)
      {
         case PSTableChangeEvent.ACTION_DELETE:
            deleteItem(itemInfo);
            break;
            
         case PSTableChangeEvent.ACTION_INSERT:
            insertItem(itemInfo);
            break;
            
         case PSTableChangeEvent.ACTION_UPDATE:
            updateItem(itemInfo);
            break;
      }
   }
   
   /**
    * Gets the specified items.
    * @param itemIds the IDs of the items.
    * @return the items, never <code>null</code>, but may be empty.
    */
   public List<IPSItemEntry> getItems(List<Integer> itemIds)
   {
      notNull(itemIds);      
      List<IPSItemEntry> itemList = new ArrayList<>();
      
      for (Integer itemId : itemIds)
      {
         IPSItemEntry item = getItem(itemId);
         if (item != null)
            itemList.add(item);
      }
      
      return itemList;
   }

   /**
    * Returns the item entry from the supplied item id.
    *
    * @param itemId the content id of the item, never <code>null</code>.
    *
    * @return the item entry object, may be <code>null</code> if cannot find
    *    the item the cached.
    */
   public IPSItemEntry getItem(Integer itemId)
   {
      if (itemId == null)
         throw new IllegalArgumentException("itemId must not be null");
      
      return getCachedItem(itemId);
   }
   
   /**
    * Converts the given string to integer.
    * 
    * @param s the number string, which may be blank.
    * 
    * @return the converted integer. It may be <code>-1</code> if the string is blank or failed to convert to number.
    */
   private int getInteger(String s)
   {
      if (isBlank(s))
         return -1;
      
      try
      {
         return Integer.parseInt(s);
      }
      catch (NumberFormatException e)
      {
         log.error("Failed to convert String to Integer for \"{}\", default to -1, Error : {}",s, e.getMessage());
         log.debug(e.getMessage(),e);
         return -1;
      }
   }
   
   /**
    * Inserts an item from the supplied parameters.
    * 
    * @param itemInfo the inserted item info, assumed not <code>null</code>.
    */
   private void insertItem(NotifiedItemInfo itemInfo)
   {

         int contentId = getInteger(itemInfo.mi_contentIdS);
         Integer id = Integer.valueOf(contentId);
         PSItemEntry item = (PSItemEntry) m_items.get(id);
         if (item == null)
         {
            synchronized (getCacheSyncObject(contentId)) {
               int objectType = getInteger(itemInfo.mi_objectTypeS);
               int communityId = getInteger(itemInfo.mi_communityIdS);
               int contentTypeId = getInteger(itemInfo.mi_contentTypeIdS);
               int workflowId = getInteger(itemInfo.mi_workflowIdS);
               int stateId = getInteger(itemInfo.mi_stateIdS);
               
               Date lastModifiedDate = isBlank(itemInfo.mi_lastModifiedDateS) ? null : parseStringToDate(itemInfo.mi_lastModifiedDateS);
               Date createDate = isBlank(itemInfo.mi_createdDateS) ? null :parseStringToDate(itemInfo.mi_createdDateS);
               Date postDate = isBlank(itemInfo.mi_postDateS) ? null : parseStringToDate(itemInfo.mi_postDateS);
               
               int tipRevision = getInteger(itemInfo.mi_tipRevision);
               int currentRevision = getInteger(itemInfo.mi_currentRevision);
               int publicRevision = getInteger(itemInfo.mi_publicRevision);
               String checkedOutUserName = itemInfo.ms_lastCheckedOutUsername;
               if (objectType == PSCmsObject.TYPE_FOLDER)
               {
                  item = new PSFolderEntry(contentId, itemInfo.mi_title, communityId,
                        contentTypeId, objectType);
               }
               else
               {
                  
                  item =
                     new PSItemEntry(contentId,
                           itemInfo.mi_title,
                           communityId,
                           contentTypeId,
                           objectType,
                           itemInfo.mi_createByUserName,
                           lastModifiedDate,
                           itemInfo.mi_lastModifierS,
                           postDate,
                           createDate,
                           workflowId,
                           stateId,
                           tipRevision,
                             currentRevision,
                             publicRevision,
                             checkedOutUserName);
                  
                  setWorkflowStateName(item);
                  setContentTypeLabel(item);
                  
                  m_items.put(id, item);
                  if (tipRevision!=currentRevision)
                  {
                     m_checkedOutItems.put(id,item);
                  }

                  if (publicRevision > 0 && publicRevision!=currentRevision)
                  {
                     m_publicItems.put(id,item);
                  }
                  
               }
               
               m_items.put(id, item);
               log.debug("insert item id: {} ", contentId);
            }
         }
  
   }

   /**
    * Set the workflow state name for the specified item.
    * @param item the item in question, never <code>null</code>.
    */
   private void setWorkflowStateName(PSItemEntry item)
   {
      if (item.getWorkflowAppId() < 0 || item.getContentStateId() < 0)
         return;
      
      PSWorkflow wf = PSWebserviceUtils.getWorkflow(item.getWorkflowAppId());
      String stateName = PSWebserviceUtils.getStateById(wf, item.getContentStateId()).getName();
      item.setStateName(stateName);
   }
   
   /**
    * Set the content type label for the specified item.
    * @param item the item in question, never <code>null</code>.
    */
   private void setContentTypeLabel(PSItemEntry item)
   {
      try
      {
         String label = PSItemDefManager.getInstance().contentTypeIdToLabel(item.getContentTypeId());
         item.setContentTypeLabel(label);
      }
      catch (PSInvalidContentTypeException e)
      {
         log.error("Invalid content type id ( {} ) for contentId = {} , Error {} ", item.getContentTypeId(), item.getContentId(), e.getMessage());
         log.debug(e.getMessage(),e);
      }
      
   }
   
   /**
    * Updates an item from the supplied parameters.
    * 
    * @param itemInfo the updated item info, assumed not <code>null</code>.
    */
   private void updateItem(NotifiedItemInfo itemInfo)
   {
      Integer contentId = getInteger(itemInfo.mi_contentIdS);
      synchronized (getCacheSyncObject(contentId))
      {

         PSItemEntry item = getCachedItem(contentId);
         if (item != null)
         {
            if (isNotBlank(itemInfo.mi_title))
               item.setName(itemInfo.mi_title);

            if (isNotBlank(itemInfo.mi_lastModifiedDateS))
            {
               Date lastModifiedDate = parseStringToDate(itemInfo.mi_lastModifiedDateS);
               item.setLastModifiedDate(lastModifiedDate);
            }

            item.setCheckedOutUsername(itemInfo.ms_lastCheckedOutUsername);

            if (isNotBlank(itemInfo.mi_lastModifierS))
               item.setLastModifier(itemInfo.mi_lastModifierS);

            if (isNotBlank(itemInfo.mi_postDateS))
            {
               Date postedDate = parseStringToDate(itemInfo.mi_postDateS);
               item.setPostDate(postedDate);
            }

            // update workflow related properties if needed
            int workflowId = getInteger(itemInfo.mi_workflowIdS);
            if (workflowId > 0)
               item.setWorkflowAppId(workflowId);
            int stateId = getInteger(itemInfo.mi_stateIdS);
            if (stateId > 0)
               item.setContentStateId(stateId);
            if (item.getWorkflowAppId() > 0 && item.getContentStateId() > 0)
               setWorkflowStateName(item);

            int origTip = item.getTipRevision();
            int origCur = item.getCurrentRevision();
            int origPub = item.getPublicRevision();
            
            Set<Integer> origRevs = new HashSet<>();
            if (origTip>0)
               origRevs.add(origTip);
            if (origCur>0)
               origRevs.add(origCur);
            if (origPub>0)
               origRevs.add(origPub);
            
            
            if (itemInfo.mi_currentRevision!=null)
            {
               
              
               int tipRevision = getInteger(itemInfo.mi_tipRevision);
               int currentRevision = getInteger(itemInfo.mi_currentRevision);
               int publicRevision = getInteger(itemInfo.mi_publicRevision);
               
               
               item.setTipRevision(tipRevision);
               item.setCurrentRevision(currentRevision);
               item.setPublicRevision(publicRevision);
   
               origRevs.remove(tipRevision);
               origRevs.remove(currentRevision);
               origRevs.remove(publicRevision);
               
               if (origRevs.size()>0)
               {
                  log.debug("Content status revision change, clean up AA revisions {} for id {} name {} ", origRevs, contentId, itemInfo.mi_title);
                  PSFolderRelationshipCache relCache = PSFolderRelationshipCache.getInstance();
                  relCache.deleteOwnerRevisions(contentId, origRevs);
               }
            
            }

            log.debug("update item id {} with name {} ", contentId, itemInfo.mi_title);
         }

      }

   }

   /**
    * Returns the Acl of the supplied content id.
    *
    * @param contentId the content id.
    * @return the folder Acl object, may be <code>null</code> if the folder
    *    for the supplied id is not cached.
    */
   public PSFolderAcl getFolderAcl(int contentId)
   {
      PSItemEntry item = getCachedItem(contentId);
      if (item == null || !item.isFolder())
      {
         return null;
      }
      else
      {
         PSFolderEntry folder = (PSFolderEntry) item;
         return folder.getFolderAcl();
      }
   }

   /**
    * Returns the {@link PSFolder} object of the supplied folder id.
    *
    * @param folderId the content id of the folder.
    *
    * @return the {@link PSFolder} object. It may be <code>null</code> if it
    *    has not been lazily loaded or the id is not a cached folder id.
    */
   public PSFolder getFolder(int folderId)
   {
      PSItemEntry item = getCachedItem(folderId);
      if (item == null || (!item.isFolder()))
      {
         return null;
      }
      else
      {
         PSFolderEntry folder = (PSFolderEntry) item;
         return folder.getFolder();
      }
   }

   /**
    * Determines if the supplied content id exists in the cache.
    *
    * @param id the content id.
    *
    * @return <code>true</code> if the item exists in the cache;
    *    otherwise return <code>false</code>.
    */
   public boolean isItemExist(int id)
   {
      PSItemEntry item = getCachedItem(id);
      return item != null;
   }

   /**
    * Determines if the supplied content id exist in the cache and it is a
    * folder entry.
    *
    * @param id the content id.
    *
    * @return <code>true</code> if there is a folder entry in the cache;
    *    otherwise return <code>false</code>.
    */
   public boolean isFolderExist(int id)
   {
      PSItemEntry folder = getCachedItem(id);

      return (folder != null) ? folder.isFolder() : false;
   }

   /**
    * Update the supplied folder object into the cache. The folder will be
    * added into the cache if it does not exist in the cache.
    *
    * @param folder the to be updated folder object.
    */
   public void updateFolder(PSFolder folder)
   {
    
      PSLocator locator = (PSLocator) folder.getLocator();
      Integer id = (locator.getId());
      synchronized (getCacheSyncObject(id))
      {
         PSItemEntry item = (PSItemEntry) m_items.get(id);
         PSFolderEntry folderEntry;
         if (item == null)
         {
            folderEntry = new PSFolderEntry(folder);
            m_items.put(id, folderEntry);
            log.debug("insert PSFolder for id: {} ", id);
         }
         else
         {
            folderEntry = (PSFolderEntry) item;
            folderEntry.updateFolder(folder);
            log.debug("update PSFolder for id: {} ", id);
         }
      }
   }

   /**
    * Updates the supplied item into the cache. The item will be added into the
    * cache if it not exist; otherwise the same item will be updated.
    * 
    * @param item the to be cached item, never <code>null</code>.
    */
   public void updateItem(PSItemEntry item)
   {
      if (item == null)
         throw new IllegalArgumentException("item may not be null");
      
      Integer key = Integer.valueOf(item.getContentId());
      
      synchronized (getCacheSyncObject(key)) {
     
         if (item.isCheckedOut())
               m_checkedOutItems.put(key, item);      
         else
               m_checkedOutItems.remove(key);
        
         if (item.hasOlderPublicRevision())
            m_publicItems.put(key, item);
         else
            m_publicItems.remove(key);
         
         m_items.put(key, item);
         
      }
      
   }
   
   /**
    * Updates the last modified date for the specified items.
    * @param contentIds the IDs of the items in question. Note <code>null</code>.
    * @param date the new last modified date of the items, not <code>null</code>.
    */
   public void updateLastModifiedDate(Collection<Integer> contentIds, Date date)
   {
      notNull(contentIds);
      notNull(date);
      
      for (Integer id : contentIds)
      {
         synchronized (getCacheSyncObject(id)) {
            PSItemEntry item = m_items.get(id);
            if (item != null)
               item.setLastModifiedDate(date);
         }
       }
    
   }

   /**
    * Updates the post date for the specified items.
    * @param contentIds the IDs of the items in question. Note <code>null</code>.
    * @param date the new post date of the items, not <code>null</code>.
    */
   public void updatePostDate(Collection<Integer> contentIds, Date date)
   {
      notNull(contentIds);
      notNull(date);

      for (Integer id : contentIds)
      {
         synchronized (getCacheSyncObject(id)) {
            PSItemEntry item = m_items.get(id);
            if (item != null && item.getPostDate() == null)
               item.setPostDate(date);
         }
      }
     
   }
   
   /**
    * Updates the workflow and state ids for the specified items.
    * @param contentIds the IDs of the items in question. Note <code>null</code>.
    * @param workflowId
    * @param stateId
    */
   public void updateWorkflowAndState(Collection<Integer> contentIds, int workflowId, int stateId)
   {
      notNull(contentIds);
      
      for (Integer id : contentIds)
      {
         synchronized (getCacheSyncObject(id)) {
            PSItemEntry item = m_items.get(id);
            if (item != null)
            {
               item.setWorkflowAppId(workflowId);
               item.setContentStateId(stateId);
            }
         }
      }    
   }

   /**
    * Deletes the supplied item from the cache. Do nothing if the supplied
    * item id does not exist in the cache.
    *
    * @param itemInfo the deleted item info, assumed not <code>null</code>.
    */
   private void deleteItem(NotifiedItemInfo itemInfo)
   {
      int contentId = getInteger(itemInfo.mi_contentIdS);
      synchronized (getCacheSyncObject(contentId)) {
         m_items.remove(Integer.valueOf(contentId));
      }

      log.debug("delete item id: {} ", contentId);
   }
   
   /**
    * Loads all folder ACL's for the cached folders from the repository. 
    * <p>
    * Note: Assume the caller is sychronized by {@link #m_cacheMonitor}.
    * 
    * @throws IllegalStateException if {@link #m_items} has not been initialized
    * @throws PSCacheException if an error occurs.
    */
   private void loadFolderAcls() throws PSCacheException
   {
      if (m_items == null)
         throw new IllegalStateException(
            "m_items must be initialized before this is called");
      
      // gather all folder ids
      Iterator items = m_items.values().iterator();
      List idList = new ArrayList();
      while (items.hasNext())
      {
         PSItemEntry item = (PSItemEntry) items.next();
         if (item.isFolder())
            idList.add(Integer.valueOf(item.getContentId()));
      }
      
      loadFolderAcls(idList);
   }

   /**
    * Loads all folder ACL's from the repository and set EMPTY folder acl object 
    * for the folders whoes folder acl are not defined.
    * <p>
    * Note: Assume the caller is sychronized by {@link #m_cacheMonitor}.
    *
    * @param idList a list of folder id's as <code>Integer</code>. This is used
    *    to set EMPTY acls, assumed not <code>null</code>, may be empty.
    * 
    * @throws PSCacheException if an error occurs.
    */
   private void loadFolderAcls(List idList) throws PSCacheException
   {
      int[] ids = new int[idList.size()];
      for (int i=0; i<idList.size(); i++)
      {
         Integer id = (Integer) idList.get(i);
         ids[i++] = id.intValue();
      }

      PSFolderAcl[] acls = null;
      try
      {
         acls = PSFolderSecurityManager.loadFolderAcls(null);
      }
      catch (PSException e)
      {
         throw new PSCacheException(
            IPSServerErrors.CACHE_UNEXPECTED_EXCEPTION, e.toString());
      }

      // populate the folder acls
      PSFolderEntry folder;
      PSItemEntry item;
      List emptyAcls = new ArrayList(idList);
      for (int i=0; i<acls.length; i++)
      {
         Integer id = Integer.valueOf(acls[i].getContentId());
         item = (PSItemEntry) m_items.get(id);
         if (item != null && (item instanceof PSFolderEntry))
         {
            folder = (PSFolderEntry) item;
            folder.setFolderAcl(acls[i]);
            emptyAcls.remove(id);
         }
      }
      
      // set an EMPTY acl object for all folders who have no acl was found
      // this is used for getFolderAcl(int), so that the cached folders
      // always have a folder acl object.
      for (int i=0; i<emptyAcls.size(); i++)
      {
         Integer id = (Integer) emptyAcls.get(i);
         item = (PSItemEntry) m_items.get(id);
         if (item.isFolder())
         {
            folder = (PSFolderEntry) m_items.get(id);
            PSFolderAcl acl = new PSFolderAcl(folder.getContentId(), 
               folder.getCommunityId());
            folder.setFolderAcl(acl);
         }
      }

      log.debug("loaded {} Folder Acls", acls.length);
   }

   /**
    * Load the sys_pubFileName property value for all folders from the backend
    * repository.
    * <p>
    * Note: Assume the caller is sychronized by {@link #m_cacheMonitor}.
    * 
    * @throws PSCacheException if an error occurs.
    */
   private void LoadItems() throws PSCacheException
   {
      try
      {
         Collection<IPSItemEntry> allItemEntries = m_cmsObjectMgr.loadAllItemEntries();
         PSItemEntry value = null;
         
         for (IPSItemEntry itemEntry : allItemEntries)
         {
            if (itemEntry.getObjectType() == PSCmsObject.TYPE_FOLDER)
            {
               value = new PSFolderEntry(itemEntry.getContentId(), itemEntry.getName(), itemEntry.getCommunityId(),
                     itemEntry.getContentTypeId(), itemEntry.getObjectType());
            }
            else
            {
               value = (PSItemEntry) itemEntry;
            }
            
            m_items.put(itemEntry.getContentId(), value);
         }
         
         log.debug("loaded {} items", m_items.size());
      }
      catch (Exception e)
      {
         throw new PSCacheException(
               IPSServerErrors.CACHE_UNEXPECTED_EXCEPTION, e.toString());
      }
   }

   /**
    * Load the cached property values for all folders from the backend
    * repository.
    * <p>
    * Note: Assume the caller is sychronized by {@link #m_cacheMonitor}.
    * 
    * @throws PSCacheException if an error occurs.
    */
   private void LoadFolderProperties() throws PSCacheException
   {
      PSInternalRequest ir = PSServer.getInternalRequest(GET_CACHED_FOLDER_PROPS_RSC,
            new PSRequestContext(PSRequest.getContextForRequest()), null,
            false, null);
      if (ir == null)
         throw new PSCacheException(IPSCmsErrors.REQUIRED_RESOURCE_MISSING,
               GET_CACHED_FOLDER_PROPS_RSC);
      ResultSet rs = null;
      try
      {
         rs = ir.getResultSet();
         int contentId;
         String propName;
         String propValue;
         while (rs.next())
         {
            contentId = rs.getInt(1);
            propName = rs.getString(2);
            propValue = rs.getString(3);
            
            Object item = m_items.get(Integer.valueOf(contentId));
            if (item != null && (item instanceof PSFolderEntry))
            {
               PSFolderEntry folder = (PSFolderEntry) item;
               if (PSFolder.PROPERTY_GLOBALTEMPLATE.equalsIgnoreCase(propName))
               {
                  folder.setGlobalTemplateProperty(propValue);
               }
               else if (PSFolder.PROPERTY_PUB_FILE_NAME
                     .equalsIgnoreCase(propName))
               {
                  folder.setPubFileNameProperty(propValue);
               }
            }
         }
      }
      catch (Exception e)
      {
         throw new PSCacheException(
               IPSServerErrors.CACHE_UNEXPECTED_EXCEPTION, e.toString());
      }
      finally
      {
         if (rs != null)
         {
            try
            {
               rs.close();
            }
            catch (SQLException e)
            {
               // close quietly
            }
         }
         
         ir.cleanUp();
      }
   }
   
   /**
    * Get a snapshot of the current statistics of the item cache.
    * The structure of the returned element is the following:
    * <PRE><CODE>
    *    &lt;--
    *       The cache statistics element with each attribute referring to
    *       each kind of statistics.
    *    --&gt;
    *    &lt;ELEMENT ItemSummaryCacheStatistics (totalItemsAndFolders,
    *       totalItems, totalFolders)
    *     &gt;
    *    &lt;--
    *       totalItemsAndFolders - Total number of cached items and folders.
    *    --&gt;
    *    &lt;ELEMENT totalItemsAndFolders (#PCDATA)&gt;
    *    &lt;--
    *       totalItems - Total number of cached items.
    *    --&gt;
    *    &lt;ELEMENT totalItems (#PCDATA)&gt;
    *    &lt;--
    *       totalFolders - Total number of cached folders.
    *    --&gt;
    *    &lt;ELEMENT totalFolders (#PCDATA)&gt;
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

      int totalItems;
      int totalFolders = 0;
      synchronized (m_cacheMonitor)
      {
         totalItems = m_items.size();
         Iterator it = m_items.values().iterator();
         PSItemEntry item;
         while (it.hasNext())
         {
            item = (PSItemEntry) it.next();
            if (item.isFolder())
               totalFolders++;
         }
      }

      Element el = doc.createElement("ItemSummaryCacheStatistics");
      PSXmlDocumentBuilder.addElement( doc, el, "totalItemsAndFolders",
         String.valueOf(totalItems) );
      PSXmlDocumentBuilder.addElement( doc, el, "totalItems",
            String.valueOf(totalItems - totalFolders) );
      PSXmlDocumentBuilder.addElement( doc, el, "totalFolders",
            String.valueOf(totalFolders) );

      return el;
   }
   
   private IPSCmsObjectMgrInternal m_cmsObjectMgr = (IPSCmsObjectMgrInternal)PSCmsObjectMgrLocator.getObjectManager();;
   
   /**
    * The singleton instance of the {@link PSItemSummaryCache} class.
    * Initialized by {@link #createInstance()}, never <code>null</code> after
    * that.
    */
   private static PSItemSummaryCache ms_instance = new PSItemSummaryCache();

   /**
    * It contains all item objects. It maps the item id to its
    * corresponding {@link PSItemEntry} object. The map keys are
    * <code>Integer</code> objects; the map values are
    * <code>PSItemEntry</code> objects. It is initialized by ctor, never
    * <code>null</code> after that, but may be empty.
    */
   private ConcurrentHashMap<Integer,PSItemEntry> m_items = new ConcurrentHashMap<>();


   /**
    * Indicates whether the {@link #start()} has been called. <code>true</code>
    * if the cache has been initialized; otherwise <code>false</code>.
    */
   private boolean m_isStarted = false;

   /**
    * The logger object, never <code>null</code>.
    */
   private static final Logger log = LogManager.getLogger("PSItemSummaryCache");

   /**
    * The column names in {@link #ms_columns}
    */
   private final static String CONTENTID_COLUMN = "CONTENTID";
   private final static String CONTENTTYPEID_COLUMN = "CONTENTTYPEID";
   private final static String TITLE_COLUMN = "TITLE";
   private final static String COMMUNITYID_COLUMN = "COMMUNITYID";
   private final static String OBJECTTYPE_COLUMN = "OBJECTTYPE";
   
   private final static String CONTENTCREATEDBY_COLUMN = "CONTENTCREATEDBY";
   private final static String CONTENTPOSTDATE_COLUMN = "CONTENTPOSTDATE";
   private final static String CONTENTSTATEID_COLUMN = "CONTENTSTATEID";
   private final static String WORKFLOWAPPID_COLUMN = "WORKFLOWAPPID";
   private final static String CONTENTCREATEDDATE_COLUMN = "CONTENTCREATEDDATE";
   private final static String CONTENTLASTMODIFIEDDATE_COLUMN = "CONTENTLASTMODIFIEDDATE";
   private final static String TIPREVISION_COLUMN = "TIPREVISION";
   private final static String CURRENTREVISION_COLUMN = "CURRENTREVISION";
   private final static String PUBLIC_REVISION_COLUMN = "PUBLIC_REVISION";
   private final static String CONTENTLASTMODIFIER_COLUMN = "CONTENTLASTMODIFIER";
   private final static String CONTENTCHECKOUTUSERNAME_COLUMN = "CONTENTCHECKOUTUSERNAME";
   private final static String CONTENTPUBLISHDATE_COLUMN = "CONTENTPUBLISHDATE";
   

   /**
    * The updated column names for the update handlers, used for setup
    * the table changed notification.
    */
   private final static String[] ms_columns = new String[]
   {
      CONTENTID_COLUMN,
      CONTENTTYPEID_COLUMN,
      TITLE_COLUMN,
      COMMUNITYID_COLUMN,
      OBJECTTYPE_COLUMN,
      CONTENTCREATEDBY_COLUMN,
      CONTENTPOSTDATE_COLUMN,
      CONTENTSTATEID_COLUMN,
      WORKFLOWAPPID_COLUMN,
      CONTENTCREATEDDATE_COLUMN,
      CONTENTLASTMODIFIEDDATE_COLUMN,
      TIPREVISION_COLUMN,
      CURRENTREVISION_COLUMN,
      PUBLIC_REVISION_COLUMN,
      CONTENTLASTMODIFIER_COLUMN,
      CONTENTCHECKOUTUSERNAME_COLUMN,
      CONTENTPUBLISHDATE_COLUMN
   };

   /**
    * Resource used to query the cached properties for all folders.
    */
   private static final String GET_CACHED_FOLDER_PROPS_RSC =
      "sys_psxInternalResources/getCachedFolderProperties";
   
   /**
    * Object used to synchronize access to the handler's cache.  Never 
    * <code>null</code>, immutable.
    */
   private Object m_cacheMonitor = new Object();  
   
   private ConcurrentMap<Integer, Integer> locks = new ConcurrentHashMap<>();
   
   private Object getCacheSyncObject(final Integer id) {
      locks.putIfAbsent(id, id);
      return locks.get(id);
    }
}
