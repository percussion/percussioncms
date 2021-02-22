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
package com.percussion.deploy.server.dependencies;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.data.PSTableChangeEvent;
import com.percussion.deploy.client.IPSDeployConstants;
import com.percussion.deploy.error.IPSDeploymentErrors;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deploy.objectstore.PSApplicationIDTypes;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDependencyData;
import com.percussion.deploy.objectstore.PSDependencyFile;
import com.percussion.deploy.objectstore.PSDeployComponentUtils;
import com.percussion.deploy.objectstore.PSIdMap;
import com.percussion.deploy.objectstore.PSIdMapping;
import com.percussion.deploy.objectstore.PSTransactionSummary;
import com.percussion.deploy.server.IPSIdTypeHandler;
import com.percussion.deploy.server.PSAppTransformer;
import com.percussion.deploy.server.PSArchiveHandler;
import com.percussion.deploy.server.PSDbmsHelper;
import com.percussion.deploy.server.PSDependencyDef;
import com.percussion.deploy.server.PSDependencyMap;
import com.percussion.deploy.server.PSImportCtx;
import com.percussion.deploy.server.PSItemData;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.search.PSSearchIndexEventQueue;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.cache.PSItemSummaryCache;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcSelectFilter;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableSchema;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to handle packaging and deploying a content definition.
 */
public class PSContentDefDependencyHandler
      extends
         PSDataObjectDependencyHandler implements IPSIdTypeHandler
{

   /**
    * Construct a dependency handler.
    *
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be of the type supported by this class.  See
    * {@link #getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSContentDefDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   // see base class
   @SuppressWarnings("unchecked")
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (! dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      String csId = dep.getDependencyId();
      Set<PSDependency> childDeps = new HashSet<PSDependency>();
      
      // get all content type children for this content def
      List<PSDependency> contentTypeDeps = 
            getChildDepsFromParentID(CONTENT_TABLE,
         CONTENTTYPE_ID, CONTENT_ID, csId,
         PSCEDependencyHandler.DEPENDENCY_TYPE, tok);
      childDeps.addAll(contentTypeDeps);
         
      // get all workflow (Element) children for this content def
      Set wfDepIds = new HashSet();
      wfDepIds.addAll(PSDeployComponentUtils.cloneList(getChildIdsFromTable(
         CONTENT_TABLE, WORKFLOW_ID, CONTENT_ID, csId)));
      wfDepIds.addAll(PSDeployComponentUtils.cloneList(getChildIdsFromTable(
         CONTENT_APPROVALS_TABLE, WORKFLOW_ID, CONTENT_ID, csId)));
      wfDepIds.addAll(PSDeployComponentUtils.cloneList(getChildIdsFromTable(
         CONTENT_STATUS_HISTORY_TABLE, WORKFLOW_ID, CONTENT_ID, csId)));
      childDeps.addAll(getDepsFromIds(wfDepIds.iterator(), 
         PSWorkflowDependencyHandler.DEPENDENCY_TYPE, tok));
      
      // get role deps from the adhoc users and content approvals tables
      Set roleDepIds = new HashSet();
      roleDepIds.addAll(PSDeployComponentUtils.cloneList(getChildIdsFromTable(
         CONTENT_ADHOC_USERS_TABLE, ROLE_ID_COL, CONTENT_ID, csId)));
      roleDepIds.addAll(PSDeployComponentUtils.cloneList(getChildIdsFromTable(
         CONTENT_APPROVALS_TABLE, ROLE_ID_COL, CONTENT_ID, csId)));
      childDeps.addAll(getDepsFromIds(roleDepIds.iterator(), 
         PSRoleDefDependencyHandler.DEPENDENCY_TYPE, tok));      

      // walk all relationship configs, and for each non-folder relationship, 
      // see if there are any child relationships of that type
      Iterator configs = PSRelationshipCommandHandler.getRelationshipConfigs();
      while (configs.hasNext())
      {
         PSRelationshipConfig config = (PSRelationshipConfig)configs.next();
         if (!PSRelationshipConfig.CATEGORY_FOLDER.equals(
            config.getCategory()))
         {
            PSDependencyHandler crHandler = getDependencyHandler(
               PSContentRelationDependencyHandler.DEPENDENCY_TYPE);
            PSDependency crDep = crHandler.getDependency(tok, 
               PSPairDependencyId.getPairDependencyId(dep.getDependencyId(), 
                  config.getName()));
            if (crDep != null)
            {
               String isLocal = config.getSystemProperty(
                  PSRelationshipConfig.RS_ISLOCALDEPENDENCY);
               if (PSRelationshipConfig.PROPERTY_TRUE.equals(isLocal))
               {
                  crDep.setDependencyType(PSDependency.TYPE_LOCAL);
               }
               
               childDeps.add(crDep);
            }
         }
      }
         
      // add acls
      addAclDependency(tok, PSTypeEnum.LEGACY_CONTENT, dep, childDeps);
      
      // get all Community (Element) children for this content def
      List cmDeps = getChildDepsFromParentID(CONTENT_TABLE,
         COMMUNITY_ID, CONTENT_ID, csId,
         PSCommunityDependencyHandler.DEPENDENCY_TYPE, tok);
      childDeps.addAll(cmDeps);
      
      // add all id type dependencies
      childDeps.addAll(PSIdTypeDependencyHandler.getIdTypeDependencies(tok, 
         dep, this));
      
      return childDeps.iterator();
    }

   // see base class
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      return getDependencies(tok, CONTENT_TABLE, CONTENT_ID, CONTENT_NAME, 
         OBJECT_TYPE_FILTER);
   }

   // see base class
   public PSDependency getDependency(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      return getDependency(tok, id, CONTENT_TABLE, CONTENT_ID, CONTENT_NAME, 
         OBJECT_TYPE_FILTER);
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>ContentEditor</li>
    * <li>Community</li>
    * <li>ContentRelation</li>
    * <li>Schema</li>
    * <li>Workflow</li>
    * </ol>
    *
    * @return An iterator over zero or more types as <code>String</code>
    * objects, never <code>null</code>, does not contain <code>null</code> or
    * empty entries.
    */
   public Iterator getChildTypes()
   {
      return ms_childTypes.iterator();
   }

   // see base class
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }

   // see base class
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      List<PSDependencyFile> files = new ArrayList<PSDependencyFile>();
      
      // get the first dep data for the content object from system tables
      files.add(getDepFileFromDepData(getDepDataFromTable(dep, CONTENT_TABLE,
         CONTENT_ID, true)));

      PSDependencyData adhocUsersData = getDepDataFromTable(dep, 
         CONTENT_ADHOC_USERS_TABLE, CONTENT_ID, false);
      if (adhocUsersData != null)
         files.add(getDepFileFromDepData(adhocUsersData));
         
      PSDependencyData approvalsData = getDepDataFromTable(dep, 
         CONTENT_APPROVALS_TABLE, CONTENT_ID, false);
      if (approvalsData != null)
         files.add(getDepFileFromDepData(approvalsData));

      PSDependencyData historyData = getDepDataFromTable(dep, 
         CONTENT_STATUS_HISTORY_TABLE, CONTENT_ID, false);
      if (historyData != null)
         files.add(getDepFileFromDepData(historyData));
         
      // get the table schema and data for all item tables, use internal user to 
      // bypass community filtering
      PSRequest adminReq = PSRequest.getContextForRequest();
      Iterator tableData = getItemTableData(adminReq.getSecurityToken(), dep);
      while (tableData.hasNext())
      {
         PSDependencyData data = (PSDependencyData) tableData.next();
         files.add(getDepFileFromDepData(data));
      }

      return files.iterator();
   }

   // see base class
   public void installDependencyFiles(PSSecurityToken tok,
      PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
      
      // get the new id
      PSIdMapping idMapping = getIdMapping(ctx, dep);
      String newContentId = idMapping == null ? dep.getDependencyId() : 
         idMapping.getTargetId();
         
      // retrieve the data from the archive
      PSDependencyData contentDepData = null;

      // get the content data first
      Iterator files = getDependecyDataFiles(archive, dep);
      PSDependencyFile file = (PSDependencyFile)files.next();
      contentDepData = getDepDataFromFile(archive, file);

      // prepare a map to collect the cached data for updating the item summary
      // cache if it is on.
      Map<String, String> cacheData = null;
      if (PSItemSummaryCache.getInstance() != null)
      {
         cacheData = new HashMap<String, String>();
         for (int i=0; i<CACHED_COLS.length; i++)
            cacheData.put(CACHED_COLS[i], null);
      }
            
      // install the retrieved content data
      PSJdbcTableData newData;      
      newData = transferIdsInContentDepData(contentDepData.getData(), dep, 
         newContentId, ctx, PSJdbcRowData.ACTION_REPLACE, cacheData);
      installDepDataForDepDef(contentDepData.getSchema(), newData, dep, ctx);

      // get the content type
      int ctypeId = -1;
      String strContentTypeId = "";
      // should have a single child content type dependency
      Iterator children = dep.getDependencies(
         PSCEDependencyHandler.DEPENDENCY_TYPE);      
      if (children.hasNext())
      {
         PSDependency child = (PSDependency) children.next();
         try
         {
            strContentTypeId = child.getDependencyId();
            ctypeId = Integer.parseInt(strContentTypeId);            
         }
         catch (NumberFormatException e)
         {
            // fall thru
         }
      }
      
      if (ctypeId == -1)
      {
         // this would likely be a bug, but could be badly messed up source data
         Object[] args = new Object[] {"Any", 
            PSCEDependencyHandler.DEPENDENCY_TYPE, dep.getDependencyId(), 
            dep.getObjectType()};
         throw new PSDeployException(IPSDeploymentErrors.CHILD_DEP_NOT_FOUND, 
            args);
      }
      
      // tranform content type id if required
      if (ctx.getCurrentIdMap() != null)
         ctypeId = getNewIdInt(ctx, strContentTypeId,
            PSCEDependencyHandler.DEPENDENCY_TYPE);
      
      PSItemDefinition itemDef;
      try
      {
         // use internal server user to bypass community filtering
         itemDef = PSItemDefManager.getInstance().getItemDef(ctypeId, 
            PSRequest.getContextForRequest().getSecurityToken());
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }

      // install the table schema and data for all item tables
      while (files.hasNext())
      {
         file = (PSDependencyFile)files.next();
         PSDependencyData itemDepData = getDepDataFromFile(archive, file);
         PSJdbcTableSchema schema = itemDepData.getSchema();
         String table = schema.getName();
         
         // delete current data in this table for this content id
         PSJdbcTableSchema curSchema = dbmsHelper.catalogTable(table, false);
         if (curSchema != null)
            deleteDepIdFromTable(dep, ctx, table, CONTENT_ID, curSchema);
         
         // tranlate ids         
         if (dbmsHelper.getDependencyType(table) == PSDependency.TYPE_SYSTEM)
         {
            newData = transferIdsInContentDepData(itemDepData.getData(), dep, 
               newContentId, ctx, PSJdbcRowData.ACTION_INSERT, cacheData);
         }
         else
         {
            newData = transferIdsInItemDepData(itemDepData.getData(), 
               newContentId);
            if (ctx.getCurrentIdMap() != null)
            {
               PSItemData itemData = new PSItemData(itemDef, newData); 
               transformIds(itemData, ctx.getIdTypes(), ctx.getCurrentIdMap());
               newData = itemData.getTgtTableData();
            }
         }

         // Bump NEXTNUMBER as necessary for sys id's being imported
         reserveSysIds(newData);
         
         // insert the data
         schema.setAllowSchemaChanges(false); // don't want to change the table!
         PSDbmsHelper.getInstance().processTable(schema, newData);
         addTransactionLogEntry(dep, ctx, table, PSTransactionSummary.TYPE_DATA, 
            PSTransactionSummary.ACTION_CREATED);
      }
      
      // updates the item summary cache if it is on
      if (cacheData != null)
         updateItemCache(newContentId, cacheData);
      
      if (idMapping != null)
         idMapping.setIsNewObject(false);
      
      // if fts enabled, need to reindex the item
      if (PSServer.getServerConfiguration().isSearchEngineAvailable())
         PSSearchIndexEventQueue.getInstance().indexItem(new PSLocator(
            newContentId));
   }

   /**
    * Updates the item summary cache from the supplied data.
    * 
    * @param newContentId
    *           The targeted content id, assume not <code>null</code>.
    * @param cacheData
    *           It contains the cached data of the deployed item. The map key is
    *           the column name of the {@link #CONTENT_TABLE} as a
    *           <code>String</code> object. The map value is the value of the
    *           column as a <code>String</code> object. Assume the map keys
    *           are not <code>null</code>.
    * 
    * @throws PSDeployException
    *            if one of the values is <code>null</code> or empty.
    */
   private void updateItemCache(String newContentId, Map cacheData)
         throws PSDeployException
   {
      // validate the cache data
      for (int i=0; i<CACHED_COLS.length; i++)
      {
         String value = (String) cacheData.get(CACHED_COLS[i]);
         if (value == null || value.trim().length() == 0)
         {
            String[] args = new String[] { newContentId, CACHED_COLS[i],
                  CONTENT_TABLE };
            throw new PSDeployException(
                  IPSDeploymentErrors.MISSING_REQUIRED_CACHE_DATA, args);
         }
      }
      
      PSItemSummaryCache itemCache = PSItemSummaryCache.getInstance();
      PSTableChangeEvent deleteEvent = new PSTableChangeEvent(CONTENT_TABLE, PSTableChangeEvent.ACTION_DELETE, cacheData);
      itemCache.tableChanged(deleteEvent);      
      PSTableChangeEvent insertEvent = new PSTableChangeEvent(CONTENT_TABLE, PSTableChangeEvent.ACTION_INSERT, cacheData);
      itemCache.tableChanged(insertEvent);
   }
   
   // see base class
   public void reserveNewId(PSDependency dep, PSIdMap idMap)
      throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      reserveNewId(dep, idMap, NEXTNUMBER_ID, DEPENDENCY_TYPE);
   }

   //see IPSIdTypeHandler interface
   public PSApplicationIDTypes getIdTypes(PSSecurityToken tok, PSDependency dep) 
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");
         
      PSApplicationIDTypes idTypes = new PSApplicationIDTypes(dep);

      // Get item def, use internal user to bypass community filtering
      PSItemDefinition itemDef;
      PSRequest adminReq = PSRequest.getContextForRequest();
      PSSecurityToken adminTok = adminReq.getSecurityToken();
      try
      {
         itemDef = PSItemDefManager.getInstance().getItemDef(
            new PSLocator(dep.getDependencyId()), adminTok);
      }
      catch (PSInvalidContentTypeException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      
      // walk fields and check to ID type support, recurse into children
      List mappings = new ArrayList();
      Iterator tableData = getItemTableData(adminTok, dep);
      while (tableData.hasNext())
      {
         PSDependencyData depData = (PSDependencyData) tableData.next();
         PSItemData itemData = new PSItemData(itemDef, depData.getData());
         PSAppTransformer.checkItemData(mappings, itemData, null);         
      }
      
      idTypes.addMappings(dep.getDisplayName(),
         IPSDeployConstants.ID_TYPE_ELEMENT_ITEM_DATA,
            mappings.iterator());

      
      return idTypes;
   }
   
   /**
    * See {@link IPSIdTypeHandler#transformIds(Object, PSApplicationIDTypes, 
    * PSIdMap)} for details.  <code>object</code> supplied must be an instanceof
    * {@link PSItemData}.
    */
   public void transformIds(Object object, PSApplicationIDTypes idTypes, 
      PSIdMap idMap) throws PSDeployException
   {
      if (object == null)
         throw new IllegalArgumentException("object may not be null");

      if (idTypes == null)
         throw new IllegalArgumentException("idTypes may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      if (!(object instanceof PSItemData))
      {
         throw new IllegalArgumentException("invalid object type");
      }
      
      PSItemData itemData = (PSItemData) object;
      
      // walk id types and perform any transforms
       Iterator resources = idTypes.getResourceList(false);
       while (resources.hasNext())
       {
          String resource = (String)resources.next();
          Iterator elements = idTypes.getElementList(resource, false);
          while (elements.hasNext())
          {
             String element = (String)elements.next();
             Iterator mappings = idTypes.getIdTypeMappings(
                   resource, element, false);
             while (mappings.hasNext())
             {

                PSApplicationIDTypeMapping mapping =
                   (PSApplicationIDTypeMapping)mappings.next();

                if (mapping.getType().equals(
                   PSApplicationIDTypeMapping.TYPE_NONE))
                {
                   continue;
                }

                if (element.equals(
                   IPSDeployConstants.ID_TYPE_ELEMENT_ITEM_DATA))
                {
                   // xform 
                   PSAppTransformer.transformItemData(itemData, mapping, idMap);
                }
             }
          }
       }      
   }
   
   /**
    * Get table data for all item tables (non-system tables).
    * 
    * @param tok The security token to use, assumed not <code>null</code>.
    * @param dep The dependency for which the table data is retrieved, assumed 
    * not <code>null</code>.
    * 
    * @return An iterator over zero or more {@link PSDependencyData} objects,
    * never <code>null</code>, might be empty if there are no content rows for
    * the item represented by this dependency.
    * 
    * @throws PSDeployException If there are any error retrieving the data.
    */
   private Iterator getItemTableData(PSSecurityToken tok, PSDependency dep) 
      throws PSDeployException
   {
      // get the table schema and data for all item tables, do as internal user
      // since we're not filtering by community at all
      try
      {
         List<PSDependencyData> tableData = new ArrayList<PSDependencyData>();

         String csId = dep.getDependencyId();
         PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();
         PSItemDefinition itemDef = itemDefMgr.getItemDef(new PSLocator(
            Integer.parseInt(csId)), tok);
         Iterator tables = getAppCETables(itemDef.getContentEditor());
         while (tables.hasNext())
         {
            String table = (String)tables.next();
            PSDependencyData itemData = getDepDataFromTable(dep, table, 
               IPSConstants.ITEM_PKEY_CONTENTID, false);
            if (itemData != null)
               tableData.add(itemData);
         }
         
         return tableData.iterator();
      }
      catch (PSInvalidContentTypeException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
   }
   
   /**
    * Using the idMap in <code>ctx</code> to transfer the ids from the source
    * to target for a given table data and location scheme object.
    *
    * @param data The to be transfered table data, assumed not <code>null</code>
    * @param ctDep The content item dependency, assumed not <code>null</code>.
    * @param newContentId The new content id to use, assumed not 
    * <code>null</code> or empty.
    * @param ctx The import context to aid in the installation, assumed not
    * <code>null</code>.
    * @param action One of the <code>PSJdbcRowData.ACTION_xxx</code> values.
    * @param cacheData this is used to collect the cached data of the item.
    * The map key is the column name of the {@link #CONTENT_TABLE} table as 
    * <code>String</code> object. The map value is the value of the column as
    * <code>String</code> object. It may be <code>null</code> if not to collect
    * the data.
    *
    * @return The transfered table data, it will never to <code>null</code>.
    *
    * @throws PSDeployException if any error occurs.
    */
   private PSJdbcTableData transferIdsInContentDepData(
      PSJdbcTableData data, PSDependency ctDep, String newContentId, 
         PSImportCtx ctx, int action, Map<String, String> cacheData)
      throws PSDeployException
   {
      boolean collectCacheData = false;
      if (cacheData != null) 
         collectCacheData = CONTENT_TABLE.equalsIgnoreCase(data.getName());
      
      // get the source row
      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
      List<PSJdbcRowData> tgtRowList = new ArrayList<PSJdbcRowData>();
      Iterator rows = data.getRows();
      if (rows.hasNext())
      {
         while (rows.hasNext())
         {
            PSJdbcRowData srcRow = (PSJdbcRowData)rows.next();
         
            // walk the columns and build a new row, xform the ids as we go
            // xform the ids for CONTENT_ID, CONTENT_STATE_ID, CONTENTYPE_ID, 
            // COMMUNITY_ID and WORKFLOW_ID
            String wfId = null; // used to optimize state and transition id xform 
            List<PSJdbcColumnData> tgtColList = new ArrayList<PSJdbcColumnData>();
            Iterator srcCols = srcRow.getColumns();
            while (srcCols.hasNext())
            {
               PSJdbcColumnData col = (PSJdbcColumnData)srcCols.next();
               // the column name must be upper case for collecting cache data 
               String colName = col.getName().toUpperCase();
               if (colName.equalsIgnoreCase(CONTENT_ID))
               {
                  col.setValue(newContentId);
               }
               else if (colName.equalsIgnoreCase(CONTENTTYPE_ID))
               {
                  mapChildIdForColumnNullable(col, ctDep, 
                     PSCEDependencyHandler.DEPENDENCY_TYPE, ctx);
               }
               else if (colName.equalsIgnoreCase(COMMUNITY_ID))
               {
                  mapChildIdForColumnNullable(col, ctDep, 
                     PSCommunityDependencyHandler.DEPENDENCY_TYPE, ctx);
               }
               else if (colName.equalsIgnoreCase(WORKFLOW_ID))
               {
                  wfId = col.getValue();
                  PSIdMapping mapping = getIdMapping(ctx, wfId, 
                     PSWorkflowDependencyHandler.DEPENDENCY_TYPE);               
                  if (mapping != null)
                     col.setValue(mapping.getTargetId());
               }
               else if (colName.equalsIgnoreCase(CONTENT_STATE_ID) || 
                  colName.equalsIgnoreCase(STATE_ID_COL))
               {
                  mapStateIdColumn(col, srcRow, ctx, wfId);
               }
               else if (colName.equalsIgnoreCase(TRANSITION_ID_COL))
               {
                  mapTransIdColumn(col, srcRow, ctx, wfId);
               }
               else if (colName.equalsIgnoreCase(CONTENT_STATUS_HISTORY_ID_COL))
               {
                  col.setValue(String.valueOf(dbmsHelper.getNextId(
                     CONTENT_STATUS_HISTORY_TABLE)));
               }
               tgtColList.add(col);
               
               // collect cached data if needed
               if (collectCacheData && cacheData.containsKey(colName))
                  cacheData.put(colName, col.getValue());
            }

            PSJdbcRowData tgtRow = new PSJdbcRowData(tgtColList.iterator(),
               action);
            tgtRowList.add(tgtRow);
         }
      }
      else // not expecting no rows
      {
         throw new PSDeployException(IPSDeploymentErrors.NO_ROWS_TO_PROCESS);
      }

      PSJdbcTableData newData = new PSJdbcTableData(CONTENT_TABLE,
         tgtRowList.iterator());

      return newData;
   }
   
   /**
    * Using the idMap in <code>ctx</code> to transfer the ids from the source
    * to target for a given table data and location scheme object.
    *
    * @param data The to be transfered table data, assumed not <code>null</code>
    * @param newContentId The new content id to use, assumed not 
    * <code>null</code> or empty.
    *
    * @return The transfered table data, it will never to <code>null</code>.
    *
    * @throws PSDeployException if any error occurs.
    */
   private PSJdbcTableData transferIdsInItemDepData(PSJdbcTableData data, 
      String newContentId) throws PSDeployException
   {
      // get the source row
      List<PSJdbcRowData> tgtRowList = new ArrayList<PSJdbcRowData>();
      Iterator rows = data.getRows();
      
      
      if (!rows.hasNext())
      {
         throw new PSDeployException(IPSDeploymentErrors.NO_ROWS_TO_PROCESS);
      }
      
      while (rows.hasNext())
      {
         PSJdbcRowData srcRow = (PSJdbcRowData)rows.next();
         
         // walk the columns and build a new row, xform the ids as we go,
         // xform the ids for CONTENT_ID
         List<PSJdbcColumnData> tgtColList = new ArrayList<PSJdbcColumnData>();
         Iterator srcCols = srcRow.getColumns();
         while (srcCols.hasNext())
         {
            PSJdbcColumnData col = (PSJdbcColumnData)srcCols.next();
            String colName = col.getName();
            if (colName.equalsIgnoreCase(IPSConstants.ITEM_PKEY_CONTENTID))
            {
               col.setValue(newContentId);
            }

            tgtColList.add(col);
         }

         PSJdbcRowData tgtRow = new PSJdbcRowData(tgtColList.iterator(),
            PSJdbcRowData.ACTION_INSERT);
         tgtRowList.add(tgtRow);
      }

      PSJdbcTableData newData = new PSJdbcTableData(data.getName(),
         tgtRowList.iterator());

      return newData;
   }

   /**
    * Ensures that the current NEXTNUMBER value will not conflict with any 
    * of the sysids to be installed.  Reserves new ids as necessary to increment
    * the NEXTNUMBER table value.
    * 
    * @param data The data to be installed, assumed not <code>null</code>,
    * may or may not be a child table.
    * 
    * @throws PSDeployException if there are any errors reserving new ids.
    */
   private void reserveSysIds(PSJdbcTableData data) throws PSDeployException 
   {
      // walk the data and get the highest sysid in use
      int maxId = -1;
      boolean hasSysId = true;
      Iterator rows = data.getRows();
      while (rows.hasNext() && hasSysId)
      {
         PSJdbcRowData row = (PSJdbcRowData)rows.next();
         
         // walk the columns and look for sysid column
         boolean foundSysId = false;
         Iterator cols = row.getColumns();         
         while (cols.hasNext() && !foundSysId)
         {
            PSJdbcColumnData col = (PSJdbcColumnData)cols.next();
            String colName = col.getName();
            if (colName.equalsIgnoreCase(IPSConstants.CHILD_ITEM_PKEY))
            {
               // found it, now remember the max value
               foundSysId = true;
               try
               {
                  int val = Integer.parseInt(col.getValue());
                  if (val > maxId)
                     maxId = val;
               }
               catch (NumberFormatException e)
               {
                  // not a real sysid, unlikely, not our problem here
               }
            }
         }
         
         // if no sysid col, then no point in going on
         hasSysId = foundSysId;
      }  
      
      // now ensure NEXTNUMBER is correct
      if (maxId > -1)
      {
         PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
         // we may waste an ID here, but it's the easiest and safest way to 
         // check
         int nextId = dbmsHelper.getNextId(data.getName());
         
         // determine if we need to "reserve" ids for what we are installing
         int diff = maxId - nextId;
         if (diff > 0)
            PSDbmsHelper.getNextIdBlock(data.getName(), diff);            
      }
   }   
   
   /**
    * Map or transfer a state id for a given column.
    * 
    * @param col The column object, assume not <code>null</code>
    * @param srcRow The row object contains above column, assume not 
    * <code>null</code>
    * @param ctx The import context to aid in the installation, may not be
    * <code>null</code>.
    * @param wfId The parent workflow id, may be <code>null</code> or empty if 
    * not known.
    * 
    * @throws PSDeployException if any error occurs.
    */
   private void mapStateIdColumn(PSJdbcColumnData col, PSJdbcRowData srcRow, 
      PSImportCtx ctx, String wfId) throws PSDeployException
   {               
      String stId = col.getValue();
      if (stId != null && stId.trim().length() != 0)
      {
         // get the workflow id if not supplied
         if (wfId == null || wfId.trim().length() == 0)
         {
            Iterator srcCols = srcRow.getColumns();
            while (srcCols.hasNext())
            {
               PSJdbcColumnData srcCol = (PSJdbcColumnData)srcCols.next();
               String colName = srcCol.getName();
               if (colName.equalsIgnoreCase(WORKFLOW_ID))
               {
                  wfId = srcCol.getValue();
                  break;
               }
            }
         }
         
         if (wfId != null && wfId.trim().length() > 0)
         {
            PSIdMapping stateMapping = getIdMapping(ctx, stId, 
               PSStateDefDependencyHandler.DEPENDENCY_TYPE, wfId, 
               PSWorkflowDefDependencyHandler.DEPENDENCY_TYPE);
            if ( stateMapping != null )
            {
               col.setValue(stateMapping.getTargetId());
            }            
         }
      }
   }

   /**
    * Map or transfer a transition id for a given column.
    * 
    * @param col The column object, assume not <code>null</code>
    * @param srcRow The row object contains above column, assume not 
    * <code>null</code>
    * @param ctx The import context to aid in the installation, may not be
    * <code>null</code>.
    * @param wfId The parent workflow id, may be <code>null</code> or empty if 
    * not known.
    * 
    * @throws PSDeployException if any error occurs.
    */
   private void mapTransIdColumn(PSJdbcColumnData col, PSJdbcRowData srcRow, 
      PSImportCtx ctx, String wfId) throws PSDeployException
   {               
      String transId = col.getValue();
      // ignore invalid transition ids - 0 is used for entry into the workflow
      if (transId != null && transId.trim().length() != 0 && 
         !transId.equals("0"))
      {
         // get the workflow id if not supplied
         if (wfId == null || wfId.trim().length() == 0)
         {
            Iterator srcCols = srcRow.getColumns();
            while (srcCols.hasNext())
            {
               PSJdbcColumnData srcCol = (PSJdbcColumnData)srcCols.next();
               String colName = srcCol.getName();
               if (colName.equalsIgnoreCase(WORKFLOW_ID))
               {
                  wfId = srcCol.getValue();
                  break;
               }
            }
         }
         
         if (wfId != null && wfId.trim().length() > 0)
         {
            PSIdMapping transMapping = getIdMapping(ctx, transId, 
               PSTransitionDefDependencyHandler.DEPENDENCY_TYPE, wfId, 
               PSWorkflowDefDependencyHandler.DEPENDENCY_TYPE);
            if ( transMapping != null )
            {
               col.setValue(transMapping.getTargetId());
            }            
         }
      }
   }

   /**
    * Gets the list of content editor tables used by the supplied content editor.
    * 
    * @param ce The content editor defintion, assumed not <code>null</code>.
    * 
    * @return A list of table names as <code>String</code> objects, never
    * <code>null</code>, may be empty.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private Iterator getAppCETables(PSContentEditor ce) throws PSDeployException
   {
      Set<String> names = new HashSet<String>();
      PSContentEditorPipe cePipe = 
         (PSContentEditorPipe)ce.getPipe();
                  
      // check for tables 
      Iterator tables = 
         PSContentEditorObjectDependencyHandler.getLocatorTables(
            cePipe.getLocator());
      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
      while (tables.hasNext())
      {
         String tableName = (String)tables.next();
         if (dbmsHelper.getDependencyType(tableName) != 
            PSDependency.TYPE_SYSTEM)
         {
            names.add(tableName);
         }
      }  
      return names.iterator();
   }
   

   
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = 
      IPSDeployConstants.DEP_OBJECT_TYPE_CONTENT_DEF;

   // ID for the next number at CONTENT_ID column
   private final static String NEXTNUMBER_ID = "CONTENT";
      
   // Constants for table and column names

   private final static String CONTENT_ID = "CONTENTID";
   private final static String CONTENTTYPE_ID = "CONTENTTYPEID";
   private final static String COMMUNITY_ID = "COMMUNITYID";
   private final static String WORKFLOW_ID = "WORKFLOWAPPID";

   private final static String CONTENT_TABLE = "CONTENTSTATUS";
   private final static String CONTENT_NAME = "TITLE";   
   private final static String CONTENT_STATE_ID = "CONTENTSTATEID";
   private final static String CONTENT_OBJECT_TYPE_COL = "OBJECTTYPE";
   
   private static final String CONTENT_STATUS_HISTORY_TABLE = 
      "CONTENTSTATUSHISTORY";
   private static final String CONTENT_STATUS_HISTORY_ID_COL = 
      "CONTENTSTATUSHISTORYID";
   private static final String CONTENT_ADHOC_USERS_TABLE = "CONTENTADHOCUSERS";
   private static final String ROLE_ID_COL = "ROLEID";
   private static final String STATE_ID_COL = "STATEID";
   private static final String TRANSITION_ID_COL = "TRANSITIONID"; 
   private static final String CONTENT_APPROVALS_TABLE = "CONTENTAPPROVALS";
   
   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<String>();

   /**
    * This is a list of cached column names in the CONTENTSTATUS table. 
    */
   private final static String[] CACHED_COLS = new String[] { CONTENT_ID,
         CONTENT_NAME, CONTENT_OBJECT_TYPE_COL, COMMUNITY_ID, CONTENTTYPE_ID };

   /**
    * Filter used to retrieve content status entries of type item only.  Never
    * <code>null</code>.
    */
   static PSJdbcSelectFilter OBJECT_TYPE_FILTER = new PSJdbcSelectFilter(
      CONTENT_OBJECT_TYPE_COL, PSJdbcSelectFilter.EQUALS, String.valueOf(
         PSComponentSummary.TYPE_ITEM), Types.INTEGER);
         
   static
   {
      ms_childTypes.add(PSCEDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSCommunityDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSContentRelationDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSSchemaDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSWorkflowDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSRoleDefDependencyHandler.DEPENDENCY_TYPE);
   }

}
