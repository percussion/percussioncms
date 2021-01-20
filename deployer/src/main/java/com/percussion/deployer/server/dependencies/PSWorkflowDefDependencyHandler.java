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
package com.percussion.deployer.server.dependencies;


import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyData;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSDeployComponentUtils;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDbmsHelper;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.data.PSBackEndRole;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.services.workflow.data.PSWorkflowRole;
import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to handle packaging and deploying a workflow definition.
 */
public class PSWorkflowDefDependencyHandler
   extends PSDataObjectDependencyHandler
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
    * @throws PSDeployException if any error occurs.
    */
   public PSWorkflowDefDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap) throws PSDeployException
   {
      super(def, dependencyMap);

      // cache the schema for all tables

      PSJdbcTableSchema schema;
      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();

      for (int i=0; i < TABLE_ENUM.length; i++)
      {
         schema = dbmsHelper.catalogTable(TABLE_ENUM[i], false);
         schema.setAllowSchemaChanges(false);

         m_schemaMap.put(TABLE_ENUM[i], schema);
      }
   }

   // see base class
   @Override
   public Iterator<PSDependency> getChildDependencies(PSSecurityToken tok,
         PSDependency dep) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      String workflowId = dep.getDependencyId();
      Set<PSDependency> childDeps = new HashSet<PSDependency>();

      // get LOCAL state child dependencies
      Iterator childIDs = getChildPairIdsFromTable(STATES_TABLE, STATE_ID,
         WORKFLOW_ID, workflowId);
      while (childIDs.hasNext())
      {
         String childId = (String)childIDs.next();
         PSPairDependencyId pairId = new PSPairDependencyId(childId);
         PSDependencyHandler handler = 
               getDependencyHandler(PSStateDefDependencyHandler.DEPENDENCY_TYPE);
         PSDependency childDep = handler.getDependency(tok, pairId.getChildId(),
            PSWorkflowDefDependencyHandler.DEPENDENCY_TYPE,
            pairId.getParentId());
         if (childDep != null)
         {
            childDep.setDependencyType(PSDependency.TYPE_LOCAL);
            childDeps.add(childDep);
         }
      }

      // Acl deps
      addAclDependency(tok, PSTypeEnum.WORKFLOW, dep, childDeps);

      return childDeps.iterator();
   }

   // see base class
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      return getDependencies(tok, WORKFLOW_TABLE, WORKFLOW_ID, WORKFLOW_NAME);
   }


   // see base class
   public PSDependency getDependency(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      return getDependency(tok, id, WORKFLOW_TABLE, WORKFLOW_ID,
         WORKFLOW_NAME);
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>StateDef</li>
    * <li>RoleDef</li>
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
   public void reserveNewId(PSDependency dep, PSIdMap idMap)
      throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      reserveNewId(dep, idMap, WORKFLOW_TABLE, DEPENDENCY_TYPE);
   }

   /**
    * Override the method from supper class, but this is to get the next id
    * specifically for <code>WORKFLOW_ID</code> in <code>WORKFLOW_TABLE</code>.
    */
   protected String getNextId(String table, PSDependency dep)
      throws PSDeployException
   {
      int id = PSDbmsHelper.getInstance().getNextIdInMemory(WORKFLOW_TABLE,
         WORKFLOW_ID, null, null);

      return String.valueOf(id);
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

      // retrieve data from all tables, add to the list if not empty
      for (int i=0; i < TABLE_ENUM.length; i++)
      {
         // the data in workflow table must not be empty
         boolean isRequireData = TABLE_ENUM[i].equals(WORKFLOW_TABLE);

         PSDependencyData depData = getDepDataFromTable(dep, TABLE_ENUM[i],
            WORKFLOW_ID, isRequireData);

         if (depData != null)
            files.add(getDepFileFromDepData(depData));
      }

      return files.iterator();
   }

   // see base class
   @SuppressWarnings("unchecked")
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

      Map dataMap = getImportDataFromArchive(archive, dep);

      // get the target workflow id
      PSIdMapping wfMapping = getIdMapping(ctx, dep);
      String workflowId = (wfMapping == null) ? dep.getDependencyId() :
         wfMapping.getTargetId();

      // reserve ids for various tables, since they are managed by this
      // handler only, not managed by the frame work.
      Map rolesIdMap = reserveIdsForTable(ROLE_ID, workflowId,
         (PSDependencyData)dataMap.get(ROLES_TABLE));
      Map notifIdMap = reserveIdsForTable(NOTIFICATION_ID, workflowId,
         (PSDependencyData)dataMap.get(NOTIFICATIONS_TABLE));

      // delete the rows which reference to the workflow dependency from
      // all database tables.
      // NOTE: the sequence in TABLE_ENUM needs to be re-arranged when
      // there are foreign keys involved and modified.
      for (int i=0; i < TABLE_ENUM.length; i++)
      {
         if (! TABLE_ENUM[i].equals(WORKFLOW_TABLE))
         {
            deleteDepIdFromTable(dep, ctx, TABLE_ENUM[i],
               WORKFLOW_ID, m_schemaMap.get(TABLE_ENUM[i]));
         }
      }

      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      // installing imported data to the database
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\

      // get the current version
      Integer version = null;
      IPSGuid guid = PSGuidUtils.makeGuid(workflowId, PSTypeEnum.WORKFLOW);
      PSWorkflow workflow = ms_wfService.loadWorkflow(guid);
      if (workflow != null)
      {
         version = workflow.getVersion();
      }
      else
      {
         // workflow doesn't exist
         version = new Integer(0);
      }
      
      // update the version in dependency table data
      PSDependencyData data = (PSDependencyData)dataMap.get(WORKFLOW_TABLE);
      PSJdbcTableData tableData = data.getData();
      tableData.updateColumn("VERSION", String.valueOf(version));

      // install workflow data
      installDataToWorkflowTable(data, dep, ctx);

      // update(increment) the workflow version
      ms_wfService.updateWorkflowVersion(guid);
      
      data = (PSDependencyData)dataMap.get(STATES_TABLE);
      if (data != null)
         installDataToStatesTable(data, dep, ctx);

      data = (PSDependencyData)dataMap.get(TRANSITIONS_TABLE);
      if (data != null)
         installDataToTnxTable(data, dep, ctx);

      data = (PSDependencyData)dataMap.get(NOTIFICATIONS_TABLE);
      if (data != null)
         installDataToNotifTable(data, dep, ctx, notifIdMap);

      data = (PSDependencyData)dataMap.get(ROLES_TABLE);
      if (data != null)
         installDataToRolesTable(data, dep, ctx, rolesIdMap);

      data = (PSDependencyData)dataMap.get(STATEROLES_TABLE);
      if (data != null)
         installDataToStateRolesTable(data, dep, ctx, rolesIdMap);

      data = (PSDependencyData)dataMap.get(TRANSITIONNOTIFICATIONS_TABLE);
      if (data != null)
         installDataToTnxNotifTable(data, dep, ctx, notifIdMap);

      data = (PSDependencyData)dataMap.get(TRANSITIONROLES_TABLE);
      if (data != null)
         installDataToTnxRolesTable(data, dep, ctx, rolesIdMap);

      // Ensure the Workflow Roles being installed are included in the 
      // the target system's "Back End Roles" (security roles). 
      // (Reload the workflow to get the latest data).
      IPSBackEndRoleMgr beRoleMgr = PSRoleMgrLocator.getBackEndRoleManager();
      workflow = ms_wfService.loadWorkflow(guid);
      List<PSWorkflowRole> wfRoles = workflow.getRoles();    
      for (PSWorkflowRole wfRole : wfRoles)
      {
         String wfRoleName = wfRole.getName();        
         PSBackEndRole ber = beRoleMgr.createRole(wfRoleName);
      }
   }

   /**
    * Install a dependency data to the <code>WORKFLOW_TABLE</code> for a given
    * workflow dependency object.
    *
    * @param depData The dependency data, assume not <code>null</code>.
    * @param wfDep The workflow dependency object, assume not <code>null</code>
    * @param ctx The import context to aid in the installation, assume not
    * <code>null</code>.
    *
    * @throws PSDeployException if any error occurs.
    */
   private void installDataToWorkflowTable(PSDependencyData depData,
      PSDependency wfDep, PSImportCtx ctx)
      throws PSDeployException
   {
      PSJdbcTableSchema schema = depData.getSchema();
      schema.setAllowSchemaChanges(false); // don't want to change the table!

      Iterator rows = depData.getData().getRows();

      if ( ! rows.hasNext() ) // no rows
         throw new PSDeployException(IPSDeploymentErrors.NO_ROWS_TO_PROCESS);

      PSIdMapping wfMapping = getIdMapping(ctx, wfDep);

      // transfer ids in all rows
      List<PSJdbcRowData> tgtRowList = new ArrayList<PSJdbcRowData>();

      while (rows.hasNext())
      {
         PSJdbcRowData srcRow = (PSJdbcRowData)rows.next();

         // walk the columns and build a new row, xform the ids as we go
         // xform the ids for STATE_ID, WORKFLOW_ID

         List<PSJdbcColumnData> tgtColList = new ArrayList<PSJdbcColumnData>();
         Iterator srcCols = srcRow.getColumns();
         while (srcCols.hasNext())
         {
            PSJdbcColumnData col = (PSJdbcColumnData)srcCols.next();
            String colName = col.getName();
            if (colName.equalsIgnoreCase(WORKFLOW_ID))
            {
               if (wfMapping != null)
                  col.setValue(wfMapping.getTargetId());
            }
            else if (colName.equalsIgnoreCase(WORKFLOW_INITIALSTATEID))
            {
               setStateIdCol(col, wfDep, ctx);
            }
            tgtColList.add(col);
         }

         PSJdbcRowData tgtRow = new PSJdbcRowData(tgtColList.iterator(),
            PSJdbcRowData.ACTION_REPLACE);
         tgtRowList.add(tgtRow);
      }

      PSJdbcTableData newData = new PSJdbcTableData(WORKFLOW_TABLE,
         tgtRowList.iterator());

      // now, installing the transfered data
      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
      dbmsHelper.processTable(schema, newData);

      // make sure the mappings are reset after update
      if (wfMapping != null)
         wfMapping.setIsNewObject(false);

      // update the log transaction
      int transAction = getRowAction(ctx, wfDep);
      addTransactionLogEntry(wfDep, ctx, WORKFLOW_TABLE,
         PSTransactionSummary.TYPE_DATA, transAction);
   }

   /**
    * Install a dependency data to the <code>STATES_TABLE</code> for a given
    * workflow dependency object.
    *
    * @param depData The dependency data, assume not <code>null</code>.
    * @param wfDep The workflow dependency object, assume not <code>null</code>
    * @param ctx The import context to aid in the installation, assume not
    * <code>null</code>.
    *
    * @throws PSDeployException if any error occurs.
    */
   private void installDataToStatesTable(PSDependencyData depData,
      PSDependency wfDep, PSImportCtx ctx)
      throws PSDeployException
   {
      PSJdbcTableSchema schema = depData.getSchema();
      schema.setAllowSchemaChanges(false); // don't want to change the table!

      Iterator rows = depData.getData().getRows();

      if ( ! rows.hasNext() ) // no rows
         throw new PSDeployException(IPSDeploymentErrors.NO_ROWS_TO_PROCESS);

      PSIdMapping wfMapping = getIdMapping(ctx, wfDep);

      // transfer ids in all rows
      List<PSJdbcRowData> tgtRowList = new ArrayList<PSJdbcRowData>();

      // save state idmappings to flip isNew setting only after update
      List<PSIdMapping> stateMappings = new ArrayList<PSIdMapping>();

      while (rows.hasNext())
      {
         PSJdbcRowData srcRow = (PSJdbcRowData)rows.next();

         // walk the columns and build a new row, xform the ids as we go
         // xform the ids for STATE_ID, WORKFLOW_ID

         List<PSJdbcColumnData> tgtColList = new ArrayList<PSJdbcColumnData>();
         Iterator srcCols = srcRow.getColumns();
         while (srcCols.hasNext())
         {
            PSJdbcColumnData col = (PSJdbcColumnData)srcCols.next();
            String colName = col.getName();
            if (colName.equalsIgnoreCase(WORKFLOW_ID))
            {
               if (wfMapping != null)
                  col.setValue(wfMapping.getTargetId());
            }
            else if (colName.equalsIgnoreCase(STATE_ID))
            {
               PSIdMapping stateMapping = setStateIdCol(col, wfDep, ctx);
               if (stateMapping != null)
                  stateMappings.add(stateMapping);
            }

            tgtColList.add(col);
         }

         PSJdbcRowData tgtRow = new PSJdbcRowData(tgtColList.iterator(),
            PSJdbcRowData.ACTION_INSERT);
         tgtRowList.add(tgtRow);
      }

      PSJdbcTableData newData = new PSJdbcTableData(STATES_TABLE,
         tgtRowList.iterator());

      // now, installing the transfered data
      PSDbmsHelper.getInstance().processTable(schema, newData);

      // make sure state mappings are reset after update
      Iterator mappings = stateMappings.iterator();
      while (mappings.hasNext())
      {
         PSIdMapping mapping = (PSIdMapping)mappings.next();
         mapping.setIsNewObject(false);
      }

      // update the log transaction
      addTransactionLogEntry(wfDep, ctx, STATES_TABLE,
         PSTransactionSummary.TYPE_DATA, PSTransactionSummary.ACTION_CREATED);
   }

   /**
    * Set a state id column from source id to the mapping id if needed
    *
    * @param col The column object, assume not <code>null</code>.
    * @param wfDep The workflow dependency object, assume not <code>null</code>
    * @param ctx The import context to aid in the installation, assume not
    * <code>null</code>.
    *
    * @return The mapping used, or <code>null</code> if no tranformation
    * required.
    *
    * @throws PSDeployException if any error occurs.
    */
   private PSIdMapping setStateIdCol(PSJdbcColumnData col, PSDependency wfDep,
      PSImportCtx ctx) throws PSDeployException
   {
      String stateDepId = col.getValue();
      PSDependency stateDep = getChildDependency(wfDep, stateDepId,
         PSStateDefDependencyHandler.DEPENDENCY_TYPE);

      PSIdMapping stateMapping = getIdMapping(ctx, stateDep);
      if ( stateMapping != null )
         col.setValue(stateMapping.getTargetId());

      return stateMapping;
   }

   /**
    * Set a transition id column from source id to the mapping id if needed
    *
    * @param col The column object, assume not <code>null</code>.
    * @param wfDep The workflow dependency object, assume not <code>null</code>
    * @param ctx The import context to aid in the installation, assume not
    * <code>null</code>.
    *
    * @return The mapping used, or <code>null</code> if no tranformation
    * required.
    *
    * @throws PSDeployException if any error occurs.
    */
   private PSIdMapping setTransIdCol(PSJdbcColumnData col, PSDependency wfDep,
      PSImportCtx ctx) throws PSDeployException
   {
      String transDepId = col.getValue();
      PSDependency transDep = getChildDependency(wfDep, transDepId,
         PSTransitionDefDependencyHandler.DEPENDENCY_TYPE);

      PSIdMapping transMapping = getIdMapping(ctx, transDep);
      if ( transMapping != null )
         col.setValue(transMapping.getTargetId());

      return transMapping;
   }

   /**
    * Install a dependency data to the <code>TRANSITIONS_TABLE</code> for
    * a given workflow dependency object.
    *
    * @param depData The dependency data, assume not <code>null</code>.
    * @param wfDep The workflow dependency object, assume not <code>null</code>
    * @param ctx The import context to aid in the installation, assume not
    * <code>null</code>.
    *
    * @throws PSDeployException if any error occurs.
    */
   private void installDataToTnxTable(PSDependencyData depData,
      PSDependency wfDep, PSImportCtx ctx)
      throws PSDeployException
   {
      PSJdbcTableSchema schema = depData.getSchema();
      schema.setAllowSchemaChanges(false); // don't want to change the table!

      Iterator rows = depData.getData().getRows();

      if ( ! rows.hasNext() ) // no rows
         throw new PSDeployException(IPSDeploymentErrors.NO_ROWS_TO_PROCESS);

      PSIdMapping wfMapping = getIdMapping(ctx, wfDep);

      // transfer ids in all rows
      List<PSJdbcRowData> tgtRowList = new ArrayList<PSJdbcRowData>();

      // save transition idmappings to flip isNew setting only after update
      List<PSIdMapping> transMappings = new ArrayList<PSIdMapping>();

      while (rows.hasNext())
      {
         PSJdbcRowData srcRow = (PSJdbcRowData)rows.next();

         // walk the columns and build a new row, xform the ids as we go
         // xform the ids for TRANSITION_ID, WORKFLOW_ID,
         // TRANSITIONFROMSTATE_ID, TRANSITIONTOSTATE_ID

         List<PSJdbcColumnData> tgtColList = new ArrayList<PSJdbcColumnData>();
         Iterator srcCols = srcRow.getColumns();
         while (srcCols.hasNext())
         {
            PSJdbcColumnData col = (PSJdbcColumnData)srcCols.next();
            String colName = col.getName();
            if (colName.equalsIgnoreCase(WORKFLOW_ID))
            {
               if (wfMapping != null)
                  col.setValue(wfMapping.getTargetId());
            }
            else if (colName.equalsIgnoreCase(TRANSITION_ID))
            {
               PSIdMapping transMapping = setTransIdCol(col, wfDep, ctx);
               if (transMapping != null)
                  transMappings.add(transMapping);
            }
            else if ( colName.equalsIgnoreCase(TRANSITIONTOSTATE_ID) ||
                      colName.equalsIgnoreCase(TRANSITIONFROMSTATE_ID) )
            {
               setStateIdCol(col, wfDep, ctx);
            }

            tgtColList.add(col);
         }

         PSJdbcRowData tgtRow = new PSJdbcRowData(tgtColList.iterator(),
            PSJdbcRowData.ACTION_INSERT);
         tgtRowList.add(tgtRow);
      }

      PSJdbcTableData newData = new PSJdbcTableData(TRANSITIONS_TABLE,
         tgtRowList.iterator());

      // now, installing the transfered data
      PSDbmsHelper.getInstance().processTable(schema, newData);

      // make sure transition mappings are reset after update
      Iterator mappings = transMappings.iterator();
      while (mappings.hasNext())
      {
         PSIdMapping mapping = (PSIdMapping)mappings.next();
         mapping.setIsNewObject(false);
      }

      // update the log transaction
      addTransactionLogEntry(wfDep, ctx, TRANSITIONS_TABLE,
         PSTransactionSummary.TYPE_DATA, PSTransactionSummary.ACTION_CREATED);
   }

   /**
    * Install a dependency data to the <code>NOTIFICATIONS_TABLE</code> for
    * a given workflow dependency object.
    *
    * @param depData The dependency data, assume not <code>null</code>.
    * @param wfDep The workflow dependency object, assume not <code>null</code>
    * @param ctx The import context to aid in the installation, assume not
    * <code>null</code>.
    * @param notifDdMap It maps old id (as the key in <code>String</code>) to
    * new id (as the value in <code>String</code>) for the
    * <code>NOTIFICATION_ID</code> column (in <code>depData</code>), assume
    * not <code>null</code>
     *
    * @throws PSDeployException if any error occurs.
    */
   private void installDataToNotifTable(PSDependencyData depData,
      PSDependency wfDep, PSImportCtx ctx, Map notifDdMap)
      throws PSDeployException
   {
      PSJdbcTableSchema schema = depData.getSchema();
      schema.setAllowSchemaChanges(false); // don't want to change the table!

      Iterator rows = depData.getData().getRows();

      if ( ! rows.hasNext() ) // no rows
         throw new PSDeployException(IPSDeploymentErrors.NO_ROWS_TO_PROCESS);

      PSIdMapping wfMapping = getIdMapping(ctx, wfDep);

      // transfer ids in all rows

      List<PSJdbcRowData> tgtRowList = new ArrayList<PSJdbcRowData>();

      while (rows.hasNext())
      {
         PSJdbcRowData srcRow = (PSJdbcRowData)rows.next();

         // walk the columns and build a new row, xform the ids as we go
         // xform the ids for NOTIFICATION_ID and WORKFLOW_ID

         List<PSJdbcColumnData> tgtColList = new ArrayList<PSJdbcColumnData>();
         Iterator srcCols = srcRow.getColumns();
         while (srcCols.hasNext())
         {
            PSJdbcColumnData col = (PSJdbcColumnData)srcCols.next();
            String colName = col.getName();
            if (colName.equalsIgnoreCase(WORKFLOW_ID))
            {
               if (wfMapping != null)
                  col.setValue(wfMapping.getTargetId());
            }
            else if (colName.equalsIgnoreCase(NOTIFICATION_ID))
            {
               String newId = (String) notifDdMap.get(col.getValue());
               col.setValue(newId);
            }

            tgtColList.add(col);
         }

         PSJdbcRowData tgtRow = new PSJdbcRowData(tgtColList.iterator(),
            PSJdbcRowData.ACTION_INSERT);
         tgtRowList.add(tgtRow);
      }

      PSJdbcTableData newData = new PSJdbcTableData(NOTIFICATIONS_TABLE,
         tgtRowList.iterator());

      // now, installing the transfered data
      PSDbmsHelper.getInstance().processTable(schema, newData);

      // update the log transaction
      addTransactionLogEntry(wfDep, ctx,
         NOTIFICATIONS_TABLE, PSTransactionSummary.TYPE_DATA,
         PSTransactionSummary.ACTION_CREATED);
   }

   /**
    * Install a dependency data to the <code>ROLES_TABLE</code> for
    * a given workflow dependency object.
    *
    * @param depData The dependency data, assume not <code>null</code>.
    * @param wfDep The workflow dependency object, assume not <code>null</code>
    * @param ctx The import context to aid in the installation, assume not
    * <code>null</code>.
    * @param rolesIdMap It maps old id (as the key in <code>String</code>) to
    * new id (as the value in <code>String</code>) for the <code>ROLE_ID</code>
    * column (in <code>depData</code>), assume not <code>null</code>
     *
    * @throws PSDeployException if any error occurs.
    */
   private void installDataToRolesTable(PSDependencyData depData,
      PSDependency wfDep, PSImportCtx ctx, Map rolesIdMap)
      throws PSDeployException
   {
      PSJdbcTableSchema schema = depData.getSchema();
      schema.setAllowSchemaChanges(false); // don't want to change the table!

      Iterator rows = depData.getData().getRows();

      if ( ! rows.hasNext() ) // no rows
         throw new PSDeployException(IPSDeploymentErrors.NO_ROWS_TO_PROCESS);

      PSIdMapping wfMapping = getIdMapping(ctx, wfDep);

      // transfer ids in all rows

      List<PSJdbcRowData> tgtRowList = new ArrayList<PSJdbcRowData>();

      while (rows.hasNext())
      {
         PSJdbcRowData srcRow = (PSJdbcRowData)rows.next();

         // walk the columns and build a new row, xform the ids as we go
         // xform the ids for ROLE_ID and WORKFLOW_ID

         List<PSJdbcColumnData> tgtColList = new ArrayList<PSJdbcColumnData>();
         Iterator srcCols = srcRow.getColumns();
         while (srcCols.hasNext())
         {
            PSJdbcColumnData col = (PSJdbcColumnData)srcCols.next();
            String colName = col.getName();
            if (colName.equalsIgnoreCase(WORKFLOW_ID))
            {
               if (wfMapping != null)
                  col.setValue(wfMapping.getTargetId());
            }
            else if (colName.equalsIgnoreCase(ROLE_ID))
            {
               String newId = (String) rolesIdMap.get(col.getValue());
               col.setValue(newId);
            }

            tgtColList.add(col);
         }

         PSJdbcRowData tgtRow = new PSJdbcRowData(tgtColList.iterator(),
            PSJdbcRowData.ACTION_INSERT);
         tgtRowList.add(tgtRow);
      }

      PSJdbcTableData newData = new PSJdbcTableData(ROLES_TABLE,
         tgtRowList.iterator());

      // now, installing the transfered data
      PSDbmsHelper.getInstance().processTable(schema, newData);

      // update the log transaction
      addTransactionLogEntry(wfDep, ctx,
         ROLES_TABLE, PSTransactionSummary.TYPE_DATA,
         PSTransactionSummary.ACTION_CREATED);
   }

   /**
    * Install a dependency data to the <code>STATEROLES_TABLE</code> for
    * a given workflow dependency object.
    *
    * @param depData The dependency data, assume not <code>null</code>.
    * @param wfDep The workflow dependency object, assume not <code>null</code>
    * @param ctx The import context to aid in the installation, assume not
    * <code>null</code>.
    * @param rolesIdMap It maps old id (as the key in <code>String</code>) to
    * new id (as the value in <code>String</code>) for the <code>ROLE_ID</code>
    * column (in <code>depData</code>), assume not <code>null</code>
     *
    * @throws PSDeployException if any error occurs.
    */
   private void installDataToStateRolesTable(PSDependencyData depData,
      PSDependency wfDep, PSImportCtx ctx, Map rolesIdMap)
      throws PSDeployException
   {
      PSJdbcTableSchema schema = depData.getSchema();
      schema.setAllowSchemaChanges(false); // don't want to change the table!

      Iterator rows = depData.getData().getRows();

      if ( ! rows.hasNext() ) // no rows
         throw new PSDeployException(IPSDeploymentErrors.NO_ROWS_TO_PROCESS);

      PSIdMapping wfMapping = getIdMapping(ctx, wfDep);

      // transfer ids in all rows

      List<PSJdbcRowData> tgtRowList = new ArrayList<PSJdbcRowData>();

      while (rows.hasNext())
      {
         PSJdbcRowData srcRow = (PSJdbcRowData)rows.next();

         // walk the columns and build a new row, xform the ids as we go
         // xform the ids for WORKFLOW_ID, STATE_ID and ROLE_ID

         List<PSJdbcColumnData> tgtColList = new ArrayList<PSJdbcColumnData>();
         Iterator srcCols = srcRow.getColumns();
         while (srcCols.hasNext())
         {
            PSJdbcColumnData col = (PSJdbcColumnData)srcCols.next();
            String colName = col.getName();
            if (colName.equalsIgnoreCase(WORKFLOW_ID))
            {
               if (wfMapping != null)
                  col.setValue(wfMapping.getTargetId());
            }
            else if (colName.equalsIgnoreCase(ROLE_ID))
            {
               String newId = (String) rolesIdMap.get(col.getValue());
               col.setValue(newId);
            }
            else if (colName.equalsIgnoreCase(STATE_ID))
            {
               setStateIdCol(col, wfDep, ctx);
            }

            tgtColList.add(col);
         }

         PSJdbcRowData tgtRow = new PSJdbcRowData(tgtColList.iterator(),
            PSJdbcRowData.ACTION_INSERT);
         tgtRowList.add(tgtRow);
      }

      PSJdbcTableData newData = new PSJdbcTableData(STATEROLES_TABLE,
         tgtRowList.iterator());

      // now, installing the transfered data
      PSDbmsHelper.getInstance().processTable(schema, newData);

      // update the log transaction
      addTransactionLogEntry(wfDep, ctx,
         STATEROLES_TABLE, PSTransactionSummary.TYPE_DATA,
         PSTransactionSummary.ACTION_CREATED);
   }

   /**
    * Install a dependency data to the
    * <code>TRANSITIONNOTIFICATIONS_TABLE</code> for a given workflow
    * dependency object.
    *
    * @param depData The dependency data, assume not <code>null</code>.
    * @param wfDep The workflow dependency object, assume not <code>null</code>
    * @param ctx The import context to aid in the installation, assume not
    * <code>null</code>.
    * @param notifIdMap It maps old id (as the key in <code>String</code>) to
    * new id (as the value in <code>String</code>) for the
    * <code>NOTIFICATION_ID</code> column (in <code>depData</code>), assume
    * not <code>null</code>
     *
    * @throws PSDeployException if any error occurs.
    */
   private void installDataToTnxNotifTable(PSDependencyData depData,
      PSDependency wfDep, PSImportCtx ctx, Map notifIdMap)
      throws PSDeployException
   {
      PSJdbcTableSchema schema = depData.getSchema();
      schema.setAllowSchemaChanges(false); // don't want to change the table!

      PSIdMapping wfMapping = getIdMapping(ctx, wfDep);
      String workflowId = (wfMapping == null) ? wfDep.getDependencyId() :
         wfMapping.getTargetId();

      Map tnxNotifIdMap = reserveIdsForTable(TRANSITIONNOTIFICATION_ID,
         workflowId, depData);

      Iterator rows = depData.getData().getRows();

      if ( ! rows.hasNext() ) // no rows
         throw new PSDeployException(IPSDeploymentErrors.NO_ROWS_TO_PROCESS);

      // transfer ids in all rows

      List<PSJdbcRowData> tgtRowList = new ArrayList<PSJdbcRowData>();

      while (rows.hasNext())
      {
         PSJdbcRowData srcRow = (PSJdbcRowData)rows.next();

         // walk the columns and build a new row, xform the ids as we go
         // xform the ids for WORKFLOW_ID, TRANSITIONNOTIFICATION_ID,
         // TRANSITION_ID and NOTIFICATION_ID

         List<PSJdbcColumnData> tgtColList = new ArrayList<PSJdbcColumnData>();
         Iterator srcCols = srcRow.getColumns();
         while (srcCols.hasNext())
         {
            PSJdbcColumnData col = (PSJdbcColumnData)srcCols.next();
            String colName = col.getName();
            if (colName.equalsIgnoreCase(WORKFLOW_ID))
            {
               col.setValue(workflowId);
            }
            else if (colName.equalsIgnoreCase(TRANSITIONNOTIFICATION_ID))
            {
               String newId = (String) tnxNotifIdMap.get(col.getValue());
               col.setValue(newId);
            }
            else if (colName.equalsIgnoreCase(TRANSITION_ID))
            {
               setTransIdCol(col, wfDep, ctx);
            }
            else if (colName.equalsIgnoreCase(NOTIFICATION_ID))
            {
               String newId = (String) notifIdMap.get(col.getValue());
               col.setValue(newId);
            }

            tgtColList.add(col);
         }

         PSJdbcRowData tgtRow = new PSJdbcRowData(tgtColList.iterator(),
            PSJdbcRowData.ACTION_INSERT);
         tgtRowList.add(tgtRow);
      }

      PSJdbcTableData newData = new PSJdbcTableData(
         TRANSITIONNOTIFICATIONS_TABLE, tgtRowList.iterator());

      // now, installing the transfered data
      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
      dbmsHelper.processTable(schema, newData);

      // update the log transaction
      addTransactionLogEntry(wfDep, ctx,
         TRANSITIONNOTIFICATIONS_TABLE, PSTransactionSummary.TYPE_DATA,
         PSTransactionSummary.ACTION_CREATED);
   }

   /**
    * Install the supplied  dependency data to the
    * <code>TRANSITIONROLES_TABLE</code> for a given workflow dependency object.
    *
    * @param depData The dependency data, assumed not <code>null</code>.
    * @param wfDep The workflow dependency object, assume not <code>null</code>
    * @param ctx The import context to aid in the installation, assumed not
    * <code>null</code>.
    * @param rolesIdMap It maps old id (as the key in <code>String</code>) to
    * new id (as the value in <code>String</code>) for the <code>ROLE_ID</code>
    * column (in <code>depData</code>), assumed not <code>null</code>
     *
    * @throws PSDeployException if any error occurs.
    */
   private void installDataToTnxRolesTable(PSDependencyData depData,
      PSDependency wfDep, PSImportCtx ctx, Map rolesIdMap)
      throws PSDeployException
   {
      PSJdbcTableSchema schema = depData.getSchema();
      schema.setAllowSchemaChanges(false); // don't want to change the table!

      PSIdMapping wfMapping = getIdMapping(ctx, wfDep);
      String workflowId = (wfMapping == null) ? wfDep.getDependencyId() :
         wfMapping.getTargetId();

      Iterator rows = depData.getData().getRows();

      if ( ! rows.hasNext() ) // no rows
         throw new PSDeployException(IPSDeploymentErrors.NO_ROWS_TO_PROCESS);

      // transfer ids in all rows

      List<PSJdbcRowData> tgtRowList = new ArrayList<PSJdbcRowData>();

      while (rows.hasNext())
      {
         PSJdbcRowData srcRow = (PSJdbcRowData)rows.next();

         // walk the columns and build a new row, xform the ids as we go
         // xform the ids for WORKFLOW_ID, TRANSITIONROLE_ID,
         // TRANSITION_ID

         List<PSJdbcColumnData> tgtColList = new ArrayList<PSJdbcColumnData>();
         Iterator srcCols = srcRow.getColumns();
         while (srcCols.hasNext())
         {
            PSJdbcColumnData col = (PSJdbcColumnData)srcCols.next();
            String colName = col.getName();
            if (colName.equalsIgnoreCase(WORKFLOW_ID))
            {
               col.setValue(workflowId);
            }
            else if (colName.equalsIgnoreCase(TRANSITION_ID))
            {
               setTransIdCol(col, wfDep, ctx);
            }
            else if (colName.equalsIgnoreCase(TRANSITIONROLE_ID))
            {
               String newId = (String) rolesIdMap.get(col.getValue());
               col.setValue(newId);
            }

            tgtColList.add(col);
         }

         PSJdbcRowData tgtRow = new PSJdbcRowData(tgtColList.iterator(),
            PSJdbcRowData.ACTION_INSERT);
         tgtRowList.add(tgtRow);
      }

      PSJdbcTableData newData = new PSJdbcTableData(
         TRANSITIONROLES_TABLE, tgtRowList.iterator());

      // now, installing the transfered data
      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
      dbmsHelper.processTable(schema, newData);

      // update the log transaction
      addTransactionLogEntry(wfDep, ctx,
         TRANSITIONROLES_TABLE, PSTransactionSummary.TYPE_DATA,
         PSTransactionSummary.ACTION_CREATED);
   }

   /**
    * Reserves ids for a database table.
    *
    * @param data A dependency data object, it contains the database table name
    * that need to reserve ids for. It may be <code>null</code>.
    * @param idCol The column name of the reserved id.
    * @param workflowId 
    * @param depData 
    *
    * @return A list of pairs, old and new ids, in a <code>Map</code>. The old
    * id is the map's key (in <code>String</code>), the new id is the map's
    * value (in <code>String</code>). It may be <code>null</code> if
    * <code>data</code> is <code>null</code>.
    *
    * @throws PSDeployException if any error occurs.
    */
   private Map reserveIdsForTable(String idCol, String workflowId,
      PSDependencyData depData) throws PSDeployException
   {
      Map<String, String> idmap = null;

      if ( depData != null )
      {
         PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();

         PSJdbcTableData tblData = depData.getData();
         List rows = PSDeployComponentUtils.cloneList(tblData.getRows());

         int idBlockSize = rows.size();
         int[] ids = dbmsHelper.getNextIdBlockInMemory(tblData.getName(),
            idCol, WORKFLOW_ID, workflowId, idBlockSize);

         String table = tblData.getName();
         idmap = new HashMap<String, String>(idBlockSize);
         for (int i=0; i < idBlockSize; i++)
         {
            PSJdbcRowData row = (PSJdbcRowData)rows.get(i);
            String oldId = dbmsHelper.getColumnString(table, idCol, row);
            idmap.put(oldId, Integer.toString(ids[i]));
         }
      }

      return idmap;
   }

   /**
    * Retrieve the imported workflow data from the archive for the given
    * dependency.
    *
    * @param archive The archive to be retrieved from, assume not
    * <code>null</code>
    * @param dep The dependency object, assume not <code>null</code>.
    *
    * @return A list of retrieved <code>PSDependencyData</code> in a
    * <code>Map</code>, it will never be <code>null</code> or empty. The map's
    * value is <code>PSDependencyData</code>, the map's key is the table name
    * of the <code>PSDependencyData</code> object.
    *
    * @throws PSDeployException if any error occurs.
    */
   private Map<String, PSDependencyData> getImportDataFromArchive(PSArchiveHandler archive,
      PSDependency dep) throws PSDeployException
   {
      Map<String, PSDependencyData> dataMap = new HashMap<String, PSDependencyData>();
      Iterator files = getDependecyDataFiles(archive, dep);
      while (files.hasNext())
      {
         PSDependencyFile file = (PSDependencyFile)files.next();
         PSDependencyData depData =  getDepDataFromFile(archive, file);
         dataMap.put(depData.getData().getName(), depData);
      }
      return dataMap;
   }
   
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "WorkflowDef";

   // Constants for table containing workflow
   private static final String WORKFLOW_TABLE = "WORKFLOWAPPS";
   private static final String WORKFLOW_ID = "WORKFLOWAPPID";
   private static final String WORKFLOW_NAME = "WORKFLOWAPPNAME";
   private static final String WORKFLOW_INITIALSTATEID = "INITIALSTATEID";

   private static final String ROLES_TABLE = "ROLES";
   private static final String ROLE_ID = "ROLEID";
   private static final String ROLE_NAME = "ROLENAME";
   private static final String STATES_TABLE = "STATES";
   private static final String STATE_ID = "STATEID";

   private static final String STATEROLES_TABLE = "STATEROLES";
   private static final String TRANSITIONS_TABLE = "TRANSITIONS";
   private static final String TRANSITION_ID = "TRANSITIONID";
   private static final String TRANSITIONFROMSTATE_ID = "TRANSITIONFROMSTATEID";
   private static final String TRANSITIONTOSTATE_ID = "TRANSITIONTOSTATEID";

   private static final String TRANSITIONNOTIFICATIONS_TABLE =
      "TRANSITIONNOTIFICATIONS";
   private static final String TRANSITIONNOTIFICATION_ID =
      "TRANSITIONNOTIFICATIONID";
   private static final String NOTIFICATIONS_TABLE = "NOTIFICATIONS";
   private static final String NOTIFICATION_ID = "NOTIFICATIONID";

   private static final String TRANSITIONROLES_TABLE =
      "TRANSITIONROLES";
   private static final String TRANSITIONROLE_ID =
      "TRANSITIONROLEID";


   private static final String[] TABLE_ENUM = { TRANSITIONROLES_TABLE,
      TRANSITIONNOTIFICATIONS_TABLE, STATEROLES_TABLE, TRANSITIONS_TABLE,
      ROLES_TABLE, NOTIFICATIONS_TABLE, STATES_TABLE, WORKFLOW_TABLE };

   /**
    * List of child types supported by this handler, never <code>null</code> or
    * empty.
    */
   private static List<String> ms_childTypes = new ArrayList<String>();

   static
   {
      ms_childTypes.add(PSStateDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSAclDefDependencyHandler.DEPENDENCY_TYPE);
   }

   /**
    * List of table schemas for all tables with table name as the map key (in
    * <code>String</code> and the schema object as the map value (in
    * <code>PSJdbcTableSchema</code>, initialized by constructor, will
    * never be <code>null</code> or empty after that.
    */
   private Map<String, PSJdbcTableSchema> m_schemaMap = new HashMap<String, PSJdbcTableSchema>();
   
   /**
    * Get the workflow service.
    */
   private static IPSWorkflowService ms_wfService = 
      PSWorkflowServiceLocator.getWorkflowService();

}
