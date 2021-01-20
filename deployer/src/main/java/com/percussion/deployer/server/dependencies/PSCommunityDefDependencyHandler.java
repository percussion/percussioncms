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
import com.percussion.services.security.PSSecurityException;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcSelectFilter;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class to handle packaging and deploying a community definition.
 */
public class PSCommunityDefDependencyHandler
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
    * @throws PSDeployException 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSCommunityDefDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap) throws PSDeployException
   {
      super(def, dependencyMap);

      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();

      // initialize schemas for child relationship tables
      m_commCPSchema = dbmsHelper.catalogTable(COMM_CP_TABLE, false);
      m_commRLSchema = dbmsHelper.catalogTable(COMM_RL_TABLE, false);
      // initialize m_childTypes
      m_childTypes = new ArrayList<String>();
   }

   // see base class
   @Override
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (! dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      // No child dependencies, just create the List for the return value.
      List<PSDependency> childDeps = new ArrayList<PSDependency>();

      return childDeps.iterator();
   }


   // see base class
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      return getDependencies(tok, COMMUNITY_TABLE, COMMUNITY_ID,
         COMMUNITY_NAME);
   }

   // see base class
   public PSDependency getDependency(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      return getDependency(tok, id, COMMUNITY_TABLE, COMMUNITY_ID,
         COMMUNITY_NAME);
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>Component</li>
    * <li>ContentEditor</li>
    * <li>RoleDef</li>
    * <li>Site (v4.5 and above)</li>
    * <li>VariantDef</li>
    * <li>Workflow</li>
    * </ol>
    *
    * @return An iterator over zero or more types as <code>String</code>
    * objects, never <code>null</code>, does not contain <code>null</code> or
    * empty entries.
    */
   public Iterator getChildTypes()
   {
      return m_childTypes.iterator();
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

      reserveNewId(dep, idMap, NEXTNUMBER_ID, DEPENDENCY_TYPE);
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

      PSDependencyData depData;
      List<PSDependencyFile> files = new ArrayList<PSDependencyFile>();

      // get the first dep data for the content object of itself
      depData = getDepDataFromTable(dep, COMMUNITY_TABLE, COMMUNITY_ID, true);
      files.add(getDepFileFromDepData(depData));

      // get the Role relationship data
      depData = getDepDataFromTable(dep, COMM_RL_TABLE, COMMUNITY_ID, false);
      if (depData != null)
      {
         depData = convertRoleIdToRoleName(depData);
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
      // get the community data first
      Iterator files = getDependecyDataFiles(archive, dep);
      PSDependencyFile file = (PSDependencyFile)files.next();
      PSDependencyData depData = getDepDataFromFile(archive, file);

      Integer version = null;
      try
      {
         // get the current version and update
         PSCommunity community = ms_beRoleMgr.loadCommunity(
               PSGuidUtils.makeGuid(dep.getDependencyId(),
                     PSTypeEnum.COMMUNITY_DEF));
         version = community.getVersion() + 1;
      }
      catch (PSSecurityException e)
      {
         // community doesn't exist
         version = new Integer(0);
      }
      
      // update the version in dependency table data
      PSJdbcTableData tableData = depData.getData();
      tableData.updateColumn("VERSION", String.valueOf(version));
           
      // install the community data
      installDepDataForDepDef(depData, dep, ctx, COMMUNITY_TABLE,
         COMMUNITY_ID, COMMUNITY_NAME, DEPENDENCY_TYPE);

      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      // delete the parent from the child relationship tables
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      deleteDepIdFromTable(dep, ctx, COMM_CP_TABLE, COMMUNITY_ID,
         m_commCPSchema);
      deleteDepIdFromTable(dep, ctx, COMM_RL_TABLE, COMMUNITY_ID,
         m_commRLSchema);

      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
      // install the child relationship data
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/

      // prepare a map between a child table and its id
      Map<String, String> columnMap = new HashMap<String, String>();
      columnMap.put(COMM_CP_TABLE, COMM_CP_ID);

      // retrieve the files and install them
      while (files.hasNext())
      {
         file = (PSDependencyFile)files.next();
         depData = getDepDataFromFile(archive, file);
         PSJdbcTableData data = depData.getData();
         String table = data.getName();
         if (table.equalsIgnoreCase(COMM_RL_TABLE))
            data = transferIdsForRLData(data, dep, ctx, tok);
         else
            data = transferIdsForChildData(data, (String)columnMap.get(table),
               dep, ctx, tok);

         installDependencyData(depData.getSchema(), data, dep, ctx,
            PSTransactionSummary.ACTION_CREATED, null);
      }
      // The components have been removed as dependents of communities and this is
      // a similar hack that the server does while saving the communities, any
      // save from work bench will add all the components from component table
      // to component community table.This code has been duplicated from
      // <code>PSBackEndRoleMgr#saveCommunity</code> method and needs to be
      // updated both places at a time.
      try
      {
         PSIdMapping commMapping = getIdMapping(ctx, dep);
         IPSGuid communityGuid = PSGuidUtils.makeGuid(commMapping.getTargetId(),
               PSTypeEnum.COMMUNITY_DEF);
         addComponentCommunityAssociations(communityGuid);
      }
      catch (Exception e)
      {
         Object[] args = { dep.getDisplayName() };
         throw new PSDeployException(
               IPSDeploymentErrors.FAILED_TO_CREATE_COMPONENT_COMMUNITY_ASSNS,
               e, args);
      }
   }

   /**
    * Adds all the components from components table to component communities
    * table for the supplied community id. This code has been duplicated from
    * <code>PSBackEndRoleMgr#saveCommunity</code> method and needs to be
    * updated both places at a time.
    * 
    * @param communityGuid assumed not <code>null</code>.
    * @throws PSDeployException
    * @throws SQLException
    * @throws NamingException
    */
   private void addComponentCommunityAssociations(IPSGuid communityGuid)
      throws PSDeployException, NamingException, SQLException
   {
      Connection conn = null;
      PreparedStatement st = null;
      try
      {
         conn = PSConnectionHelper.getDbConnection();
         StringBuilder b = new StringBuilder();
         b.append("INSERT INTO ");
         b.append(qualify("RXCOMPONENTCOMMUNITY"));
         b.append(" SELECT COMPONENTID, ");
         b.append(communityGuid.getUUID());
         b.append(" FROM ");
         b.append(qualify("RXSYSCOMPONENT"));
         st = conn.prepareStatement(b.toString());
         st.execute();
      }
      finally
      {
         if(st!=null)
         {
            st.close();
         }
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
               //ignore
            }
         }
      }
   }
   
   /**
    * Qualify a tablename for a native SQL statement
    * 
    * @param tablename the tablename, assumed not <code>null</code>
    * @return the qualified name
    * @throws NamingException
    * @throws SQLException
    */
   private Object qualify(String tablename) throws NamingException,
         SQLException
   {
      PSConnectionDetail detail = PSConnectionHelper.getConnectionDetail();
      return PSSqlHelper.qualifyTableName(tablename, detail.getDatabase(),
            detail.getOrigin(), detail.getDriver());
   }

   // see base class
   public boolean shouldDeferInstallation()
   {
      // need to defer community def installation until after child role have
      // been installed, otherwise we can't transform the role id.
      return true;
   }
   
   /**
    * Convert role id (at column <code>COMM_RL_ID</code> in
    * <code>COMM_RL_TABLE</code>) to its corresponding role name (at column
    * <code>ROLE_NAME</code> in <code>ROLE_TABLE</code>) for a dependency data.
    *
    * @param depData The to be converted dependency data, assume not
    * <code>null</code>.
    *
    * @return The converted dependency data, will never be <code>null</code> or
    * empty.
    *
    * @throws PSDeployException if any error occurs.
    */
   PSDependencyData convertRoleIdToRoleName(PSDependencyData depData)
      throws PSDeployException
   {
      PSJdbcTableData data = depData.getData();

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

         // walk the columns and build a new row, xform the COMM_RL_ID to its
         // corresponding ROLE_NAME in ROLE_TABLE
         List<PSJdbcColumnData> tgtColList = new ArrayList<PSJdbcColumnData>();
         Iterator srcCols = srcRow.getColumns();
         while (srcCols.hasNext())
         {
            PSJdbcColumnData col = (PSJdbcColumnData)srcCols.next();
            String colName = col.getName();
            if (colName.equalsIgnoreCase(COMM_RL_ID))
            {
               // query the database based on the role id, colume value
               PSJdbcSelectFilter filter = new PSJdbcSelectFilter(ROLE_ID,
                  PSJdbcSelectFilter.EQUALS, col.getValue(), Types.INTEGER);
               PSDependencyData pdata;
               pdata = getDepDataFromTable(ROLE_TABLE, filter, true);

               // get the role name from the result set
               PSJdbcRowData row;
               row = (PSJdbcRowData) pdata.getData().getRows().next();
               String roleName = PSDbmsHelper.getInstance().getColumnString(
                  ROLE_TABLE, ROLE_NAME, row);

               col.setValue(roleName);
            }

            tgtColList.add(col);
         }

         PSJdbcRowData tgtRow = new PSJdbcRowData(tgtColList.iterator(),
            PSJdbcRowData.ACTION_INSERT);
         tgtRowList.add(tgtRow);
      }

      PSJdbcTableData newData = new PSJdbcTableData(COMM_RL_TABLE,
         tgtRowList.iterator());

      PSDependencyData newDepData = new PSDependencyData(depData.getSchema(),
         newData);

      return newDepData;
   }

   /**
    * Transfer community id from the source data to target id, and convert
    * role-name (at column <code>COMM_RL_ID</code>) to its corresponding role
    * name (at column <code>ROLE_NAME</code> in <code>ROLE_TABLE</code>). This
    * is a reverse operation as the <code>convertRoleIdToRoleName()</code> did
    * to the <code>COMM_RL_ID</code> column.
    *
    * @param data The source data, assume not <code>null</code>.
    * @param dep The community dependency object, assume not <code>null</code>
    * @param ctx The import context to aid in the installation, assume not
    * <code>null</code>.
    * @param tok The security token, assume not <code>null</code>.
    *
    * @return The transfered or converted table data, will never be
    * <code>null</code> or empty.
    *
    * @throws PSDeployException if any error occurs.
    */
   private PSJdbcTableData transferIdsForRLData(PSJdbcTableData data,
      PSDependency dep, PSImportCtx ctx, PSSecurityToken tok)
      throws PSDeployException
   {
      // get the id map for community
      PSIdMapping commMapping = getIdMapping(ctx, dep);

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

         // walk the columns and build a new row, xform the id for
         // COMMUNITY_ID, convert role-name to role-id for COMM_RL_ID
         List<PSJdbcColumnData> tgtColList = new ArrayList<PSJdbcColumnData>();
         Iterator srcCols = srcRow.getColumns();
         while (srcCols.hasNext())
         {
            PSJdbcColumnData col = (PSJdbcColumnData)srcCols.next();
            String colName = col.getName();
            if (colName.equalsIgnoreCase(COMMUNITY_ID))
            {
               if (commMapping != null)
                  col.setValue(commMapping.getTargetId());
            }
            else if (colName.equalsIgnoreCase(COMM_RL_ID))
            {
               // Make sure Community's role is a Back End Role is on target.
               // NOTE: This must be done before call to "getDepDataFromTable()".
               ms_beRoleMgr.createRole(col.getValue());
               
               // query the database based on the role id, colume value
               PSJdbcSelectFilter filter = new PSJdbcSelectFilter(ROLE_NAME,
                  PSJdbcSelectFilter.EQUALS, col.getValue(), Types.VARCHAR);
               PSDependencyData pdata;
               pdata = getDepDataFromTable(ROLE_TABLE, filter, true);

               // get the role id from the result set
               PSJdbcRowData row;
               row = (PSJdbcRowData) pdata.getData().getRows().next();
               String roleId = PSDbmsHelper.getInstance().getColumnString(
                  ROLE_TABLE, ROLE_ID, row);

               col.setValue(roleId);
            }

            tgtColList.add(col);
         }

         PSJdbcRowData tgtRow = new PSJdbcRowData(tgtColList.iterator(),
            PSJdbcRowData.ACTION_INSERT);
         tgtRowList.add(tgtRow);
      }

      PSJdbcTableData newData = new PSJdbcTableData(COMM_RL_TABLE,
         tgtRowList.iterator());

      return newData;
   }

   /**
    * Using idMap in <code>ctx</code> to transfer ids from source to target for
    * a given table data.
    *
    * @param data The source table data, assume not <code>null</code>.
    * @param childIdCol 
    * @param dep The community dependency object, assume not <code>null</code>.
    * @param ctx The import context to aid in the installation, assume not
    * <code>null</code>.
    * @param tok The security token, assume not <code>null</code>.
    *
    * @return The transfered or converted table data, will never be
    * <code>null</code> or empty.
    *
    * @throws PSDeployException if any error occurs.
    */
   private PSJdbcTableData transferIdsForChildData(PSJdbcTableData data,
      String childIdCol, PSDependency dep, PSImportCtx ctx, PSSecurityToken tok)
      throws PSDeployException
   {
      // get the id map for community
      PSIdMapping commMapping = getIdMapping(ctx, dep);

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

         // walk the columns and build a new row, xform the ids as we go
         // xform the ids for COMMUNITY_ID, COMM_CP_ID, COMM_CT_ID,
         // COMM_ST_ID, COMM_VR_ID and COMM_WF_ID
         List<PSJdbcColumnData> tgtColList = new ArrayList<PSJdbcColumnData>();
         Iterator srcCols = srcRow.getColumns();
         while (srcCols.hasNext())
         {
            PSJdbcColumnData col = (PSJdbcColumnData)srcCols.next();
            String colName = col.getName();
            if (colName.equalsIgnoreCase(COMMUNITY_ID))
            {
               if (commMapping != null)
                  col.setValue(commMapping.getTargetId());
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
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "CommunityDef";


   // Constants for table containing community
   private static final String COMMUNITY_TABLE = "RXCOMMUNITY";
   private static final String COMMUNITY_ID = "COMMUNITYID";
   private static final String COMMUNITY_NAME = "NAME";

   // ID for the next number at COMMUNITY_ID column
   private final static String NEXTNUMBER_ID = "communityid";

   private static final String COMM_RL_TABLE = "RXCOMMUNITYROLE";
   private static final String COMM_RL_ID = "ROLEID";
   private static final String COMM_CP_TABLE = "RXCOMPONENTCOMMUNITY";
   private static final String COMM_CP_ID = "COMPONENTID";
   private static final String ROLE_TABLE = "PSX_ROLES";
   private static final String ROLE_ID = "ID";
   private static final String ROLE_NAME = "NAME";

   /**
    * Schema for <code>COMM_CP_TABLE</code> table, the relationship to
    * component, initialized by constructor, will never be <code>null</code>
    * or modified after that.
    */
   private PSJdbcTableSchema m_commCPSchema;

   /**
    * Schema for <code>COMM_RL_TABLE</code> table, the relationship to roles,
    * initialized by constructor, will never be <code>null</code> or modified
    * after that.
    */
   private PSJdbcTableSchema m_commRLSchema;

   /**
    * List of child types supported by this handler, initialized by constructor,
    * it will never be <code>null</code> or empty after that.
    */
   private static List<String> m_childTypes;
   
   /**
    * Get the back-end role manager.
    */
   private static IPSBackEndRoleMgr ms_beRoleMgr = 
      PSRoleMgrLocator.getBackEndRoleManager();
}
