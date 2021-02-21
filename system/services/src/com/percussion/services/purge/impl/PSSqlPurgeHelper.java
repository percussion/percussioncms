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
package com.percussion.services.purge.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSRelationshipChangeEvent;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSObjectPermissions;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSFolderSecurityManager;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.cms.objectstore.server.PSRelationshipDbProcessor;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.PSTableChangeEvent;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSException;
import com.percussion.fastforward.managednav.IPSManagedNavService;
import com.percussion.fastforward.managednav.PSManagedNavServiceLocator;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.server.cache.IPSCacheHandler;
import com.percussion.server.cache.PSAssemblerCacheHandler;
import com.percussion.server.cache.PSCacheManager;
import com.percussion.server.cache.PSFolderRelationshipCache;
import com.percussion.server.cache.PSItemSummaryCache;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.PSNotificationServiceLocator;
import com.percussion.services.purge.IPSSqlPurgeHelper;
import com.percussion.services.purge.data.RevisionData;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.PSRelationshipServiceLocator;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.request.PSRequestInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.percussion.cms.objectstore.PSFolder.FOLDER_CONTENT_TYPE_ID;

/**
 * This class uses direct SQL calls to purge items and old item revisions. This
 * will bypass any security and will remove all references to items
 */
@Transactional
public class PSSqlPurgeHelper implements IPSSqlPurgeHelper
{
   /**
    * Injected hibernate session factory
    */
   private SessionFactory sessionFactory;

   /**
    * This is a fast Database method for purging items and folders from the
    * system If folders are selected subfolders will be recursively purged as
    * well as content unless the items are also linked to other folders not
    * being purged. No effects will be run on this action to prevent any side
    * effects, all relationships to and from the items will be removed.
    * 
    * @param item
    * @return number of items purged
    * @throws PSException
    */
   public int purge(PSLocator item) throws PSException, PSValidationException {
      return purgeAll(Collections.singleton(item));
   }

   /**
    * This is a fast Database method for purging Navon and Navtree items from a
    * folder and subfolders. This will prevent inconsistent navigation making
    * sure navon items are not left without a parent.
    * 
    * @param item
    * @return number of items purged
    * @throws PSException
    */
   public int purgeNavigation(PSLocator item) throws PSException, PSValidationException {
      List<Integer> typeFilter = getNavContentTypeIds();
      return purgeAll(null, Collections.singleton(item), typeFilter);
   }

   /**
    * Gets the navigation content type IDs.
    * 
    * @return the content type IDs, never <code>null</code>.
    */
   private List<Integer> getNavContentTypeIds()
   {
      IPSManagedNavService navService = PSManagedNavServiceLocator.getContentWebservice();
      long navonTypeId = navService.getNavonContentTypeId();
      long navTreeTypeId = navService.getNavtreeContentTypeId();

      List<Integer> typeFilter = new ArrayList<>();
      typeFilter.add((int) navonTypeId);
      typeFilter.add((int) navTreeTypeId);
      return typeFilter;
   }

   /**
    * This is a fast Database method for purging Navon and Navtree and folder
    * items from a folder and subfolders. This is a replacement for the older
    * remove from folder on a folder functionality. This mechanism is not
    * normally recommended as it will orphan all the other content making it
    * only available from search.
    * 
    * @param items
    * @return number of items purged
    * @throws PSException
    */
   public int purgeNavigationAndFolders(List<PSLocator> items) throws PSException, PSValidationException {
      List<Integer> typeFilter = getNavContentTypeIds();
      typeFilter.add(FOLDER_CONTENT_TYPE_ID);
      return purgeAll(null, items, typeFilter);
   }

   /**
    * This is a fast Database method for purging items and folders from the
    * system If folders are selected subfolders will be recursively purged as
    * well as content unless the items are also linked to other folders not
    * being purged. No effects will be run on this action to prevent any side
    * effects, all relationships to and from the items will be removed.
    * 
    * @param items
    * @return number of items purged
    * @throws PSException
    */
   public int purgeAll(PSLocator parent, Collection<PSLocator> items) throws PSException, PSValidationException {
      return purgeAll(parent, items, null);
   }

   /**
    * This is a fast Database method for purging items and folders from the
    * system If folders are selected subfolders will be recursively purged as
    * well as content unless the items are also linked to other folders not
    * being purged. No effects will be run on this action to prevent any side
    * effects, all relationships to and from the items will be removed.
    * 
    * @param items
    * @return number of items purged
    * @throws PSException
    */
   public int purgeAll(Collection<PSLocator> items) throws PSException, PSValidationException {
      return purgeAll(null, items, null);
   }

   /**
    * This is a fast Database method for purging items and folders from the
    * system If folders are selected subfolders will be recursively purged as
    * well as content unless the items are also linked to other folders not
    * being purged. No effects will be run on this action to prevent any side
    * effects, all relationships to and from the items will be removed. If
    * typeFilter is not null, only the specified content type ids will be
    * removed.
    * 
    * @param parent - the parent folder if available. items will not be purged
    *           from this folder if they are linked elsewhere.
    * @param items
    * @param typeFilter
    * @return number of items purged
    * @throws PSException
    */
   public int purgeAll(PSLocator parent, Collection<PSLocator> items, List<Integer> typeFilter) throws PSException, PSValidationException {
      
      Session session = sessionFactory.getCurrentSession();
      
      int count = 0;
      Map<Integer, List<String>> contentTypeTableMap = new HashMap<>();
      Set<Integer> ids = new HashSet<>();
      for (PSLocator item : items)
      {
         ids.add(item.getId());
      }
      
      Map<Integer, Set<Integer>> contentTypeMap = processItems(parent, ids, typeFilter);

      // Get the type tables first. if there are any errors we will throw
      // exception before any relationships
      // processed.
      List<String> qualifiedTables = new ArrayList<>();

      for (int typeId : contentTypeMap.keySet())
      {
         PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();

         PSItemDefinition itemDef = itemDefMgr.getItemDef(typeId, -1);
         List<PSBackEndTable> tables = itemDef.getTypeTables();
         List<String> tableKeys = new ArrayList<>();
         for (PSBackEndTable table : tables)
         {
            tableKeys.add(table.getTable());
         }
         tableKeys.addAll(CONTENTTABLES);
         contentTypeTableMap.put(typeId, tableKeys);
      }

      PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);

      for (int typeId : contentTypeMap.keySet())
      {
         Set<Integer> typeIds = contentTypeMap.get(typeId);

         PSItemDefManager mgr = PSItemDefManager.getInstance();
         String path = mgr.getTypeEditorUrl(typeId);
         PSInternalRequest iReq = PSServer.getInternalRequest(path, req, null, true);
         // Need the ContentEditorHandler to be able to notify of item change
         // This is embedded deeply in this code.
         PSContentEditorHandler ceh;
         if (iReq != null)
         {
            IPSInternalRequestHandler rh = iReq.getInternalRequestHandler();
            if (rh != null && rh instanceof PSContentEditorHandler)
            {
               ceh = (PSContentEditorHandler) rh;

               List<String> tables = contentTypeTableMap.get(typeId);

               Set<Integer> batch = new HashSet<>();
               for (int id : contentTypeMap.get(typeId))
               {
                  batch.add(id);
                  if (batch.size() == 1000)
                  {
                     deleteBatch(tables, batch, ceh);

                     batch.clear();
                     
                  }
               }
               deleteBatch(tables, batch, ceh);
            }
            else
            {

            }

            count += typeIds.size();

         }

      }

      return count;
   }

   /**
    * To be called only for non Oracle databases to prevnt locking.
    * READ_UNCOMMITTED is not supported and not required in the same way on
    * oracle.
    * 
    * @param data
    * @return number of items purged
    * @throws Exception
    */
   @Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
   int purgeRevisions_readUncommitted(RevisionData data) throws Exception
   {
      return purgeRevisions_Main(data);
   }

   /**
    * To be called only for Oracle databases to prevnt locking. READ_UNCOMMITTED
    * is not supported and not required in the same way on oracle.
    * 
    * @param data
    * @return number of items purged
    * @throws Exception
    */
   @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
   public int purgeRevisions_readCommitted(RevisionData data) throws Exception
   {
      return purgeRevisions_Main(data);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.purge.IPSSqlPurgeHelper#purgeRevisions(com.percussion
    * .services.purge.data.RevisionData)
    */
   public int purgeRevisions(RevisionData data) throws Exception
   {
      if (PSSqlHelper.isOracle())
      {
         return purgeRevisions_readCommitted(data);
      }

      return purgeRevisions_readUncommitted(data);
   }

   /**
    * Uses direct database access to purge revision items matching provided
    * critera
    * 
    * min cond. # 1) ALWAYS keep minimum number X of revisions to keep [number]
    * min cond. date 2) ALWAYS keep all revisions D younger than number of days
    * [number]
    * 
    * if after that anything is left, than:
    * 
    * max cond # 3) delete all revisions above Y number (count) (delete
    * [Y+1,....max]) max cond date 4) delete all revisions above E days old
    * (delete [E+1,....max])
    * 
    * Y must be greater than X (if set); error if not. E must be greater than D
    * (if set); error if not.
    * 
    * @param data
    * @return number of revisions purged
    * @throws Exception
    */
   @Transactional
   public int purgeRevisions_Main(RevisionData data) throws Exception
   {

      String prop = PSServer.getServerProps().getProperty("allowPurgeRevisionsScheduledTask", "false");
      int revisionsRemoved = 0;

      if (!Boolean.parseBoolean(prop))
      {
         throw new Exception("Purge Revision code is disabled: allowPurgeRevisionsScheduledTask proprty is not set.");
      }

      if (data == null)
      {
         throw new Exception("Data argument cannot be null");
      }

      if (data.getKeepMinNumberOfRevs() < 0)
      {
         throw new Exception("Inconsistent data input: keepMinNumberOfRevs " + "is required");
      }

      if (data.getKeepRevsYoungerThanDays() < 0)
      {
         throw new Exception("Inconsistent data input: keepRevsYoungerThanDays " + "is required");
      }

      if (data.getDeleteRevsAboveCount() < 0)
      {
         throw new Exception("Inconsistent data input: deleteRevsAboveCount " + "is required");
      }
      if (data.getDeleteRevsOlderThanDays() < 0)
      {
         throw new Exception("Inconsistent data input: deleteRevsOlderThanDays " + "is required");
      }

      if (data.getKeepMinNumberOfRevs() > data.getDeleteRevsAboveCount())
      {
         throw new Exception("Inconsistent data input: deleteRevsAboveCount can't be "
               + "less than keepMinNumberOfRevs");
      }

      if (data.getKeepRevsYoungerThanDays() > data.getDeleteRevsOlderThanDays())
      {
         throw new Exception("Inconsistent data input:  deleteRevsOlderThanDays can't be "
               + "less than keepRevsYoungerThanDays");
      }

      Map<Integer, List<Table>> contentTypeMap = new HashMap<>();
      PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();
      Session session = null;
      PreparedStatement pst = null;
      PreparedStatement pstDelCSHistory = null;
      PreparedStatement pstDelObjRelations = null;
      Connection conn = null;

      session = sessionFactory.getCurrentSession();

      try
      {

         SQLQuery delCsHistoryQuery = session.createSQLQuery("DELETE from "
                 + qualify("CONTENTSTATUSHISTORY") + " where CONTENTID = ? and REVISIONID <= ?");

         SQLQuery delelObjRelations = session.createSQLQuery("DELETE from "
                 + qualify("PSX_OBJECTRELATIONSHIP")
                 + " where OWNER_ID = ? and OWNER_REVISION > 0 and OWNER_REVISION <= ? or "
                 + " DEPENDENT_ID = ? and DEPENDENT_REVISION > 0 and DEPENDENT_REVISION <= ?");

         SQLQuery selTypes =  session.createSQLQuery("select CONTENTTYPEID from " + qualify("CONTENTTYPES"));



         Iterator<?> rs = selTypes.list().iterator();
         while (rs.hasNext())
         {
            int contentTypeId = (Integer)rs.next();
            PSItemDefinition itemDef = itemDefMgr.getItemDef(contentTypeId, -1);
            List<PSBackEndTable> tables = itemDef.getTypeTables();
            List<Table> tableList = new ArrayList<>();
            for (PSBackEndTable table : tables)
            {
               String qName = qualify(table.getTable());
               tableList.add(new Table(qName, conn));
            }
            contentTypeMap.put(contentTypeId, tableList);
         }

         selTypes = session.createSQLQuery(
                     "select csh.CONTENTID, cs.CONTENTTYPEID, csh.REVISIONID, max(csh.LASTMODIFIEDDATE) as LASTMODIFIEDDATE  "
                           + "from "
                           + qualify("CONTENTSTATUSHISTORY")
                           + " csh, "
                           + qualify("CONTENTSTATUS")
                           + " cs where csh.CONTENTID = cs.CONTENTID and "
                           + "csh.REVISIONID < cs.PUBLIC_REVISION "
                           + "group by  cs.CONTENTTYPEID,csh.REVISIONID,csh.CONTENTID "
                           + "union all select csh.CONTENTID, cs.CONTENTTYPEID, csh.REVISIONID, max(csh.LASTMODIFIEDDATE) as LASTMODIFIEDDATE "
                           + "from " + qualify("CONTENTSTATUSHISTORY") + " csh, " + qualify("CONTENTSTATUS")
                           + " cs where csh.CONTENTID = cs.CONTENTID and "
                           + "PUBLIC_REVISION is null and csh.REVISIONID < cs.CURRENTREVISION "
                           + "group by  cs.CONTENTTYPEID,csh.REVISIONID,csh.CONTENTID "
                           + "order by CONTENTID, REVISIONID desc, LASTMODIFIEDDATE ");

         rs = selTypes.list().iterator();

         revisionsRemoved = deleteItemRevisions(data, contentTypeMap, delCsHistoryQuery, delelObjRelations, rs, conn);
      }
      catch (Exception x)
      {
         ms_logger.error(x.getMessage(), x);
         throw new PSException(x.getMessage(),x);
      }
      finally
      {
         if (pst != null)
            try
            {
               pst.close();
            }
            catch (SQLException x)
            {
            }
         if (pstDelCSHistory != null)
            try
            {
               pstDelCSHistory.close();
            }
            catch (SQLException x)
            {
            }
         if (pstDelObjRelations != null)
            try
            {
               pstDelObjRelations.close();
            }
            catch (SQLException x)
            {
            }

         Set<Integer> keys = contentTypeMap.keySet();
         for (Iterator<Integer> it = keys.iterator(); it.hasNext();)
         {
            Integer conId = it.next();
            List<Table> tables = contentTypeMap.get(conId);
            if (tables != null)
            {
               for (Iterator<Table> it0 = tables.iterator(); it0.hasNext();)
               {
                  Table t = it0.next();
                  PreparedStatement ps = t.getPreparedStatement();
                  try
                  {
                     ps.close();
                  }
                  catch (SQLException x)
                  {
                  }
               }
            }
         }

      }
      return revisionsRemoved;
   }

   /**
    * @param data
    * @param contentTypeMap
    * @param pstDelCSHistory
    * @param pstDelObjRelations
    * @param rs
    * @param conn
    * @throws SQLException
    */
   @Transactional
   public int deleteItemRevisions(RevisionData data, Map<Integer, List<Table>> contentTypeMap,
                                  SQLQuery pstDelCSHistory, SQLQuery pstDelObjRelations, Iterator<?> rs, Connection conn)
         throws SQLException
   {
      long now = System.currentTimeMillis();
      long minDays = now - (1000L * 60 * 60 * 24) * data.getKeepRevsYoungerThanDays();
      long maxDays = now - (1000L * 60 * 60 * 24) * data.getDeleteRevsOlderThanDays();
      int revisionsRemoved = 0;
      int prevContentId = 0;
      int prevRevisionId = 0;
      int revisionCount = 0;
      int keepMinNumberOfRevs = data.getKeepMinNumberOfRevs();
      int deleteRevsAboveCount = data.getDeleteRevsAboveCount();
      boolean rolledBackId = false;
      boolean completedContentId = false;

      // Algorithm description
      //
      // 1) when minimum of unique REVISIONs is reached use minDays and keep
      // walking unique
      // REVISION numbers until minDays is reached.
      // 2) keep walking unique REVISION numbers until either maxDays or max is
      // reached
      //
      // 3) remember the current unique REVISION number.
      //
      // 4) delete all records with REVISION number greater than 4) from
      // CONTENTSTATUSHISTORY
      // and other tables for given content type

      while (rs.hasNext())
      {
         Object[] tuple= (Object[]) rs.next();
         // condition #1 minimum number of revisions
         int currContentId = (Integer) tuple[0];
         int currContentTypeId =(Integer) tuple[1];
         int currRevisionId = (Integer) tuple[2];
         if (currContentId != prevContentId)
         { // new contentId
            revisionCount = 1;
            prevContentId = currContentId;
            prevRevisionId = currRevisionId;
            completedContentId = false;
         }
         else if (completedContentId)
         {
            if (!rolledBackId)
            {
               purge_logger.debug("Removed Content Id:" + currContentId + ", Revision Id:" + currRevisionId);
               revisionsRemoved++;
            }
            continue; // the processing for this contentId is completed skip all
                      // other records with this contentId, avoid multiple
                      // deletes
         }
         else if (currRevisionId != prevRevisionId)
         {
            rolledBackId = false;
            revisionCount++;
            prevRevisionId = currRevisionId;
         }
         if (revisionCount <= keepMinNumberOfRevs)
         {
            continue;
         }

         // condition #2 minimum number of days
         Date date = (Date)tuple[3];
         if (date == null)
            continue;

         long revDate = date.getTime();
         if (revDate > minDays)
         {
            continue;
         }

         // delete conditions - least restrictive
         if (revDate >= maxDays && revisionCount <= deleteRevsAboveCount)
         {
            // condition #3 maximum number of days and
            // condition #4 maximum revision count
            continue;
         }

         // if either condition #3 or condition #4 is true - proceed to delete

         // Reaching this point indicates that all revisions below the current
         // revision for this contentId must be deleted from all tables
         boolean txCompleted = false;

         try
         {
            conn.setAutoCommit(false);
            pstDelCSHistory.setParameter(0, currContentId);
            pstDelCSHistory.setParameter(1, currRevisionId);
            pstDelCSHistory.executeUpdate();

            pstDelObjRelations.setParameter(0, currContentId);
            pstDelObjRelations.setParameter(1, currRevisionId);
            pstDelObjRelations.setParameter(2, currContentId);
            pstDelObjRelations.setParameter(3, currRevisionId);
            pstDelObjRelations.executeUpdate();

            List<Table> tables = contentTypeMap.get(currContentTypeId);
            if (tables != null)
            {
               for (Iterator<Table> it = tables.iterator(); it.hasNext();)
               {
                  Table t = it.next();
                  PreparedStatement ps = t.getPreparedStatement();
                  ps.setInt(0, currContentId);
                  ps.setInt(1, currRevisionId);
                  ps.executeUpdate();
               }
            }
            txCompleted = true;
            completedContentId = true;

         }
         catch (Exception ex)
         {
            purge_logger.error("Rolled back purge revisions for:" + currContentId + ", Revision Id:" + currRevisionId);
            throw new RuntimeException(ex);
         }
         finally
         {
            try
            {
               if (txCompleted)
               {
                  conn.commit();
                  revisionsRemoved++;
                  purge_logger.debug("Removed Content Id:" + currContentId + ", Revision Id:" + currRevisionId);
               }
               else
               {
                  conn.rollback();
               }
               conn.setAutoCommit(true);
            }
            catch (SQLException x)
            {
            }
         }

      }
      return revisionsRemoved;
   }

   /**
    * short hand to PSSqlHelper.qualifyTableName
    */
   private String qualify(String table) throws SQLException
   {
      return PSSqlHelper.qualifyTableName(table);
   }

   /**
    * Creates a map of content items in the selected ids, folders and subfolders
    * with the content type id as the key.
    * 
    * @param items
    * @param typeFilter
    * @return
    * @throws PSCmsException
    */
   private Map<Integer, Set<Integer>> processItems(PSLocator parent, Set<Integer> items, List<Integer> typeFilter)
         throws PSCmsException
   {

      Map<Integer, Set<Integer>> contentTypeMap = createContentTypeMap(items, typeFilter);

      // process any folder items
      if (contentTypeMap.get(FOLDER_CONTENT_TYPE_ID).size() > 0)
      {
         Map<Integer, Set<Integer>> folderContentTypeMap = new HashMap<>();

         createContentTypeFolderMap(getDependents(contentTypeMap.get(FOLDER_CONTENT_TYPE_ID)), folderContentTypeMap,
               typeFilter);

         // Merge sets of folders
         Set<Integer> folders = contentTypeMap.get(FOLDER_CONTENT_TYPE_ID);

         folders.addAll(folderContentTypeMap.get(FOLDER_CONTENT_TYPE_ID));

         // All info gathered, check permissions if required before purging.
         checkFolderPermissions(folders);

         // filter folder items if they are linked to other folders. Add in
         // parent folder, but remove it before the final purge list.
         if (parent != null)
            folders.add(parent.getId());
         filterLinkedItems(folders, folderContentTypeMap);
         if (parent != null)
            folders.remove(parent.getId());
         mergeContentTypeMap(contentTypeMap, folderContentTypeMap);
      }
      // If we are not actually removing folders then remove them from the map
      // for purging.
      if (typeFilter != null && !typeFilter.contains(FOLDER_CONTENT_TYPE_ID))
      {
         contentTypeMap.remove(FOLDER_CONTENT_TYPE_ID);
      }
      return contentTypeMap;
   }

   /**
    * Check if the user has admin access to the specified folders.
    * 
    * @param folders
    * @throws PSCmsException
    */
   private void checkFolderPermissions(Set<Integer> folders) throws PSCmsException
   {
      // check the user's permission on the specified folder
      PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      IPSRequestContext ctx = new PSRequestContext(req);
      PSServerFolderProcessor folderproc = PSServerFolderProcessor.getInstance();
      for (int folder : folders)
      {
         folderproc.checkHasFolderPermission(new PSLocator(folder, -1), PSObjectPermissions.ACCESS_ADMIN, false,
               true);
      }
   }

   /**
    * Merge two content type -> id maps.
    * 
    * @param contentTypeMap
    * @param folderContentTypeMap
    */
   private void mergeContentTypeMap(Map<Integer, Set<Integer>> contentTypeMap,
         Map<Integer, Set<Integer>> folderContentTypeMap)
   {
      for (Entry<Integer, Set<Integer>> entry : folderContentTypeMap.entrySet())
      {
         if (!contentTypeMap.containsKey(entry.getKey()))
         {
            contentTypeMap.put(entry.getKey(), new HashSet<>());
         }
         contentTypeMap.get(entry.getKey()).addAll(entry.getValue());
      }

   }

   /**
    * Removes from the content type map any items that are in folders other than
    * those specified in foldersToCheck.
    * 
    * @param foldersToCheck
    * @param folderContentTypeMap
    * @throws PSCmsException
    */
   private void filterLinkedItems(Set<Integer> foldersToCheck, Map<Integer, Set<Integer>> folderContentTypeMap)
         throws PSCmsException
   {
      PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      PSFolderSecurityManager.setCheckFolderPermissions(false);
      PSRelationshipDbProcessor relProcessor = new PSRelationshipDbProcessor(req);

      try
      {
         for (Entry<Integer, Set<Integer>> entry : folderContentTypeMap.entrySet())
         {
            Set<Integer> typeItems = entry.getValue();
            // Folders should never be in more than one folder
            if (entry.getKey() != FOLDER_CONTENT_TYPE_ID)
            {
               Set<Integer> removeItems = new HashSet<>();

               for (int id : entry.getValue())
               {

                  PSRelationshipConfig config = PSRelationshipCommandHandler.getRelationshipConfig(FOLDER_RELATE_TYPE);
                  PSRelationshipSet relationshipSet = relProcessor.queryRelationships(config, new PSLocator(id, -1),
                          true, PSRelationshipConfig.FILTER_TYPE_ITEM_COMMUNITY
                                  | PSRelationshipConfig.FILTER_TYPE_FOLDER_PERMISSIONS);
                  Iterator<PSRelationship> relIt = relationshipSet.iterator();
                  boolean filter = false;
                  while (relIt.hasNext())
                  {
                     PSRelationship rel = relIt.next();
                     int folderId = rel.getOwner().getId();
                     if (!foldersToCheck.contains(folderId))
                     {
                        filter = true;
                        break;
                     }
                  }
                  if (filter)
                     removeItems.add(id);

               }
               typeItems.removeAll(removeItems);
            }
         }
      }
      finally
      {
         // enable permission checking
         PSFolderSecurityManager.setCheckFolderPermissions(true);
      }
   }

   /**
    * From the set of ids and the optional type filter calculates the content
    * type id and builds the map of content items against the type id.
    * 
    * @param ids
    * @param typeFilter
    * @return
    */
   private Map<Integer, Set<Integer>> createContentTypeMap(Set<Integer> ids, List<Integer> typeFilter)
   {
      Map<Integer, Set<Integer>> contentTypeMap = new HashMap<>();
      contentTypeMap.put(FOLDER_CONTENT_TYPE_ID, new HashSet<>());
      IPSCmsContentSummaries summ = PSCmsContentSummariesLocator.getObjectManager();

      List<PSComponentSummary> itemSum = summ.loadComponentSummaries(ids);

      for (PSComponentSummary item : itemSum)
      {
         Integer typeId = (int) item.getContentTypeId();
         // Do not filter folders at this point we will not actually purge
         // them if they are not in the typeFilter later.
         if (typeId == FOLDER_CONTENT_TYPE_ID || typeFilter == null || typeFilter.contains(typeId))
         {
            Set<Integer> typeList = contentTypeMap.get(typeId);
            if (typeList == null)
            {
               typeList = new HashSet<>();
               contentTypeMap.put(typeId, typeList);
            }
            typeList.add(item.getContentId());
         }
      }

      return contentTypeMap;
   }

   /**
    * Given a set of folder ids, will create a content type map for all the
    * subfolder content.
    * 
    * @param folderIds
    * @param contentTypeMap
    * @throws PSCmsException
    */
   private void createContentTypeFolderMap(Set<Integer> folderIds, Map<Integer, Set<Integer>> contentTypeMap,
         List<Integer> typeFilter) throws PSCmsException
   {

      IPSCmsContentSummaries summ = PSCmsContentSummariesLocator.getObjectManager();
      List<PSComponentSummary> itemSum = summ.loadComponentSummaries(folderIds);

      for (PSComponentSummary item : itemSum)
      {
         Integer typeId = (int) item.getContentTypeId();
         // Do not filter folders at this point we will not actually purge
         // them if they are not in the typeFilter later.
         if (typeId == FOLDER_CONTENT_TYPE_ID || typeFilter == null || typeFilter.contains(typeId))
         {
            Set<Integer> typeList = contentTypeMap.get(typeId);
            if (typeList == null)
            {
               typeList = new HashSet<>();
               contentTypeMap.put(typeId, typeList);
            }
            typeList.add(item.getContentId());
            if (typeId == FOLDER_CONTENT_TYPE_ID)
            {

               createContentTypeFolderMap(getDependent(item.getContentId()), contentTypeMap, typeFilter);

            }
         }

      }
      if (contentTypeMap.get(FOLDER_CONTENT_TYPE_ID) == null)
      {
         contentTypeMap.put(FOLDER_CONTENT_TYPE_ID, new HashSet<>());
      }
   }

   /**
    * gets a set of ids within the folder, ignores all permissions.
    * 
    * @param id
    * @return set of content ids
    * @throws PSCmsException
    */
   private Set<Integer> getDependent(int id) throws PSCmsException
   {
      return getDependents(Collections.singleton(id));
   }

   /**
    * gets a set of ids within the folders, ignores all permissions.
    * 
    * @param ids
    * @return set of content ids
    * @throws PSCmsException
    */
   private Set<Integer> getDependents(Set<Integer> ids) throws PSCmsException
   {
      Set<Integer> dependents = new HashSet<>();
      for (int id : ids)
      {
         PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
         PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();
         PSRelationshipSet relationships = processor.getDependents(RECYCLE_RELATE_TYPE, new PSLocator(id, -1),
               PSRelationshipConfig.FILTER_TYPE_COMMUNITY | PSRelationshipConfig.FILTER_TYPE_FOLDER_PERMISSIONS);

         Iterator<PSRelationship> itt = relationships.iterator();

         while (itt.hasNext())
            dependents.add(itt.next().getDependent().getId());
      }
      return dependents;
   }

   /**
    * Purge content through sql for the set of ids by deleting from the
    * specified table names.
    * 
    * @param tables
    * @param ids
    * @param ceh
    * @throws PSException
    */
   
   public Set<Integer> deleteBatch(List<String> tables, Set<Integer> ids, PSContentEditorHandler ceh) throws PSException, PSValidationException {
      if (ids == null || ids.isEmpty())
         return Collections.emptySet();

      
      List<Integer> localContent = new ArrayList<>();
      try
      {
         //  If local content has more than one owner we leave it.
         String localContentsql = "SELECT r.DEPENDENT_ID FROM "+ qualify("PSX_OBJECTRELATIONSHIP") + " r where r.CONFIG_ID=(select config_id from "+ qualify("PSX_RELATIONSHIPCONFIGNAME") + " where config_name like 'LocalContent')  group by r.DEPENDENT_ID having count(distinct(r.OWNER_ID))=1 and MAX(r.OWNER_ID) in (:ids)";

         localContent = sessionFactory.getCurrentSession()
               .createSQLQuery(localContentsql)
               .setParameterList("ids", ids).list();
      }
      catch (SQLException e)
      {
        ms_logger.error("Error getting local content query for delete");
      }
     
      // Clean up managed links for item
      sessionFactory.getCurrentSession()
            .createQuery("DELETE FROM PSManagedLink ml WHERE ml.childId IN (:ids) or ml.parentId in (:ids)")
            .setParameterList("ids", ids)
            .executeUpdate();

      IPSRelationshipService svc = PSRelationshipServiceLocator.getRelationshipService();

      // get all owner relationships for the current content id and delete
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setCommunityFiltering(false); // do not filter by community
      filter.setDependentIds(ids); // disregard dependent revision
      List<PSRelationship> rels = svc.findByFilter(filter);

      for (int id : ids)
      {
         filter = new PSRelationshipFilter();
         filter.setCommunityFiltering(false); // do not filter by community
         filter.setOwnerId(id); // disregard owner rev
         filter.setCommunityFiltering(false); // do not filter by community
         rels.addAll(svc.findByFilter(filter));
      }

      Set<Integer> filteredIds = filterIdsWithFolderRels(ids);

      List<PSRelationship> filteredRels = filterRelsById(rels, filteredIds);

      // get all dependent relationships for the current content id and delete
      PSRelationshipSet relset = new PSRelationshipSet();
      if (filteredRels != null && !filteredRels.isEmpty())
      {
         svc.deleteRelationship(filteredRels);
         relset.addAll(filteredRels);
      }

      updateCaches(relset);

      purge_logger.debug("Deleting from tables " + tables + " for id list" + filteredIds);

      if (filteredIds.size() > 0) {
         for (String table : tables) {
            String sqlString = "DELETE FROM " + table + " where CONTENTID IN (:ids)";

            Query query = sessionFactory.getCurrentSession()
                    .createSQLQuery(sqlString)
                    .addSynchronizedQuerySpace(table)
                    .setParameterList("ids", filteredIds);

            query.executeUpdate();
         }
      }
      
      
      PSItemSummaryCache itemCache = PSItemSummaryCache.getInstance();
      Map<String, String> cacheData = new HashMap<>();
      // Notify purge event.
      for (int id : filteredIds)
      {
         // Update item summary cache
         if (itemCache != null)
         {
            cacheData.clear();
            cacheData.put("CONTENTID", String.valueOf(id));
            PSTableChangeEvent deleteEvent = new PSTableChangeEvent("CONTENTSTATUS", PSTableChangeEvent.ACTION_DELETE,
                  cacheData);
            itemCache.tableChanged(deleteEvent);
         }
         // notify other handlers 

      }
      if (!localContent.isEmpty())
      {
         deleteBatch(tables, new HashSet<>(localContent), ceh);
         filteredIds.addAll(localContent);
      }
      for (Integer filteredId : filteredIds) {
         ceh.notifyPurge(filteredId);
      }
      return filteredIds;
   }

   private List<PSRelationship> filterRelsById(List<PSRelationship> rels, Set<Integer> filteredIds) {
      List<PSRelationship> filteredRels = new ArrayList<>();
      for (PSRelationship rel : rels) {
         if ((filteredIds.contains(rel.getDependent().getId()) ||
                 filteredIds.contains(rel.getOwner().getId())) &&
                 !rel.getConfig().equals(FOLDER_RELATE_TYPE)) {
            filteredRels.add(rel);
         } else if (rel.getConfig().getName().equals(RECYCLE_RELATE_TYPE)) {
            filteredRels.add(rel);
         }
      }
      if (ms_logger.isDebugEnabled()) {
         for (PSRelationship rel: filteredRels) {
            ms_logger.debug("Adding rel: " + rel.getId() + " to list of deletes. Has " +
                    " owner of: " + rel.getOwner() + " and dependent: " + rel.getDependent());
         }
      }
      return filteredRels;
   }

   private Set<Integer> filterIdsWithFolderRels(Set<Integer> ids) {
      IPSCmsContentSummaries summaries = PSCmsContentSummariesLocator.getObjectManager();
      IPSRelationshipService svc = PSRelationshipServiceLocator.getRelationshipService();

      List<PSComponentSummary> itemSums = summaries.loadComponentSummaries(ids);
      Set<Integer> checkedIds = new HashSet<>();
      for (PSComponentSummary summ : itemSums) {
         if (summ.getContentTypeId() == FOLDER_CONTENT_TYPE_ID) {
            PSRelationshipFilter filter = new PSRelationshipFilter();
            filter.setCommunityFiltering(false); // do not filter by community
            filter.setDependentId(summ.getContentId()); // disregard dependent revision
            List<PSRelationship> rels = new ArrayList<>();
            try {
               rels = svc.findByFilter(filter);
            } catch (PSException e) {
               ms_logger.error("Error retrieving relationships for id: " + summ.getContentId()
                       + ". Marking this id so it will not be deleted.", e);
               continue;
            }
            boolean foundFolderRel = false;
            for (PSRelationship rel : rels) {
               if (rel.getConfig().getName().equals(FOLDER_RELATE_TYPE)) {
                  foundFolderRel = true;
               }
            }
            if (!foundFolderRel) {
               checkedIds.add(summ.getContentId());
            }
         } else {
            checkedIds.add(summ.getContentId());
         }
      }
      return checkedIds;
   }

   /**
    * Updates both folder and assembly caches for the modified relationships.
    * 
    * @param relationships the modified relationships, assumed not
    *           <code>null</code>.
    */
   private void updateCaches(PSRelationshipSet relationships)
   {
      
      // notify relationship change listeners.
      PSRelationshipChangeEvent event = new PSRelationshipChangeEvent(
            PSRelationshipChangeEvent.ACTION_REMOVE, relationships);
      PSNotificationEvent notifyEvent = new PSNotificationEvent(
            EventType.RELATIONSHIP_CHANGED, event);
      IPSNotificationService srv = PSNotificationServiceLocator
         .getNotificationService();
      srv.notifyEvent(notifyEvent);

      // update the folder cache if needed
      PSFolderRelationshipCache cache = PSFolderRelationshipCache.getInstance();
      if (cache != null)
      {
         cache.delete(relationships);
      }

      // update the assembly cache
      PSCacheManager mgr = PSCacheManager.getInstance();
      IPSCacheHandler handler = mgr.getCacheHandler(PSAssemblerCacheHandler.HANDLER_TYPE);

      // if caching not enabled, handler will be null
      if (!(handler instanceof PSAssemblerCacheHandler))
         return;
      
      ((PSAssemblerCacheHandler) handler).relationshipChanged(event);
 
   }

   /**
    * Commons logger
    */
   static Log ms_logger = LogFactory.getLog("RevisionPurge");

   /**
    * Revision purge logger
    */
   static Log purge_logger = LogFactory.getLog("RevisionPurge");

   /**
    * The non type specific content tables that need cleaning up with a purge.
    */
   private static final List<String> CONTENTTABLES = Collections.unmodifiableList(Arrays.asList("CONTENTSTATUS",
         "CONTENTSTATUSHISTORY", "CONTENTAPPROVALS", "CONTENTADHOCUSERS","PSX_CONTENTCHANGEEVENT"));

   /**
    * The relationship type for folder object
    */
   public static final String FOLDER_RELATE_TYPE = PSRelationshipConfig.TYPE_FOLDER_CONTENT;

    /**
     * The relationship type for the recycled content.
     */
   private static final String RECYCLE_RELATE_TYPE = PSRelationshipConfig.TYPE_RECYCLED_CONTENT;

   /**
    * Private data holder class
    */
   private static class Table
   {
      private String name;

      private PreparedStatement pst;

      Table(String name, Connection conn) throws SQLException
      {
         this.name = name;
         this.pst = PSPreparedStatement.getPreparedStatement(conn, "DELETE FROM " + name
               + " WHERE CONTENTID = ? AND REVISIONID <= ?");
      }

      String getName()
      {
         return name;
      }

      PreparedStatement getPreparedStatement()
      {
         return pst;
      }

      void close() throws SQLException
      {
         if (pst != null)
         {
            pst.close();
         }
      }

   }
   
   /**
    * Get the hibernate session factory
    * @return SessionFactory
    */
   public SessionFactory getSessionFactory()
   {
      return sessionFactory;
   }

   /**
    * Set the hibernate session factory
    * @param sessionFactory
    */
   public void setSessionFactory(SessionFactory sessionFactory)
   {
      this.sessionFactory = sessionFactory;
   }

}
