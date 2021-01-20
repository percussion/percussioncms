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


import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDbmsHelper;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.security.PSSecurityToken;
import com.percussion.tablefactory.PSJdbcFilterContainer;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcSelectFilter;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.util.PSIteratorUtils;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class to handle discovery of state dependencies.  
 * The <code>PSWorkflowDefDependencyHandler</code> class handles the packaging
 * and installation of a state.
 */
public class PSStateDefDependencyHandler extends PSDataObjectDependencyHandler
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
   public PSStateDefDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   // see base class
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (! dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      Set childDeps = new HashSet();
         
      // get transitions as LOCAL child dependencies
      Iterator childIDs = getChildIdsForStateDep(dep);
      while (childIDs.hasNext())
      {
         String childId = (String)childIDs.next();
         PSDependencyHandler handler = getDependencyHandler(
            PSTransitionDefDependencyHandler.DEPENDENCY_TYPE);
         PSDependency childDep = handler.getDependency(tok, childId, 
            PSWorkflowDefDependencyHandler.DEPENDENCY_TYPE, 
            dep.getParentId());
         if (childDep != null)
         {
            childDep.setDependencyType(PSDependency.TYPE_LOCAL);
            childDeps.add(childDep);
         }
      }
      
      return childDeps.iterator();
    }

    /**
     * Get a list of ids of child transition dependencies for a given state 
     * deployable object.
     *
     * @param dep The state deployable object, assume not <code>null</code>.
     *
     * @return An iterator over zero or more ids of child dependencies as
     * <code>String</code>. It will never be <code>null</code>, but may be
     * empty.
     *
     * @throws PSDeployException if any error occurs.
     */
    private Iterator getChildIdsForStateDep(PSDependency dep)
      throws PSDeployException
    {
      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
      String[] columns = {TRANSITION_ID};
      PSJdbcSelectFilter fltTransFromStateId;
      PSJdbcSelectFilter fltWorkflowId;

      String stateId = dep.getDependencyId();
      String workflowId = dep.getParentId();

      // create filters:
      // WHERE (WORKFLOW_ID = workflowId) AND
      //       (TRANSITION_FROM_STATEID = stateId)
      //
      // Only check the TRANSITION_FROM_STATEID, don't need to check 
      // TRANSITION_TO_STATEID, the transition only needs to be a child of 
      // the source state
      
      fltTransFromStateId = new PSJdbcSelectFilter(TRANSITION_FROM_STATEID,
         PSJdbcSelectFilter.EQUALS, stateId, Types.INTEGER);
      fltWorkflowId = new PSJdbcSelectFilter(WORKFLOW_ID,
         PSJdbcSelectFilter.EQUALS, workflowId, Types.INTEGER);

      PSJdbcFilterContainer fltWhere = new PSJdbcFilterContainer();
      fltWhere.doAND(fltTransFromStateId);
      fltWhere.doAND(fltWorkflowId);

      PSJdbcTableData data = dbmsHelper.catalogTableData(
         TRANSITIONS_TABLE, columns, fltWhere);

      // use "Set" to make sure it is a distinct list
      Set ids = new HashSet();

      if (data != null && data.getRows().hasNext())
      {
         Iterator rows = data.getRows();
         String id;
         PSJdbcRowData row;
         while (rows.hasNext())
         {
            row = (PSJdbcRowData)rows.next();
            
            // this is a nullable column
            id = getColumnValueNullable(STATES_TABLE, TRANSITION_ID, row);
            if (id != null && id.trim().length() != 0)
               ids.add(id);
         }
      }
      
      return ids.iterator();
    }

   // see base class
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      List deps = new ArrayList();
      Iterator ids = getChildPairIdsFromTable(STATES_TABLE, STATE_ID, 
         WORKFLOW_ID, null);
      while (ids.hasNext())
      {
         String id = (String)ids.next();
         PSPairDependencyId pairId = new PSPairDependencyId(id);
         PSDependency dep = getDependency(tok, pairId.getChildId(), 
            PSWorkflowDefDependencyHandler.DEPENDENCY_TYPE, 
            pairId.getParentId());
         if (dep != null)
            deps.add(dep);
      }
      
      return deps.iterator();
   }
   
   // see base class
   public Iterator getDependencies(PSSecurityToken tok, String parentType, 
      String parentId) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (parentType == null || parentType.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentType may not be null or empty");
            
      if (parentId == null || parentId.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentId may not be null or empty");

      List deps = new ArrayList();
      Iterator ids = getChildPairIdsFromTable(STATES_TABLE, STATE_ID, 
         WORKFLOW_ID, parentId);
      while (ids.hasNext())
      {
         String id = (String)ids.next();
         PSPairDependencyId pairId = new PSPairDependencyId(id);
         PSDependency dep = getDependency(tok, pairId.getChildId(), 
            PSWorkflowDefDependencyHandler.DEPENDENCY_TYPE, 
            pairId.getParentId());
         if (dep != null)
            deps.add(dep);
      }
      
      return deps.iterator();
      
   }
   
   // see base class
   public PSDependency getDependency(PSSecurityToken tok, String id, 
      String parentType, String parentId)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
      
      if (parentType == null || parentType.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentType may not be null or empty");
            
      if (parentId == null || parentId.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentId may not be null or empty");

      if (!parentType.equals(PSWorkflowDefDependencyHandler.DEPENDENCY_TYPE))
         throw new IllegalArgumentException("parentType wrong type");
            
      PSDependency stateDep = null;
      
      // create the filter from the id of the state dependency
      PSJdbcSelectFilter fltStateId = new PSJdbcSelectFilter(STATE_ID,
         PSJdbcSelectFilter.EQUALS, id, Types.INTEGER);
      PSJdbcSelectFilter fltWorklowId = new PSJdbcSelectFilter(WORKFLOW_ID,
         PSJdbcSelectFilter.EQUALS, parentId, 
         Types.INTEGER);

      PSJdbcFilterContainer fltFinal = new PSJdbcFilterContainer();
      fltFinal.doAND(fltStateId);
      fltFinal.doAND(fltWorklowId);
      
      // get the result set from the database
      String[] columns = {STATE_NAME};  
      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance(); 
      
      PSJdbcTableData data = dbmsHelper.catalogTableData(
         STATES_TABLE, columns, fltFinal);

      // should only get back one, take the first if found
      if (data != null && data.getRows().hasNext())
      {
         Iterator rows = data.getRows();
         PSJdbcRowData row = (PSJdbcRowData) rows.next();
         String stateName = dbmsHelper.getColumnString(STATES_TABLE, STATE_NAME,
            row);
         stateDep = createDependency(m_def, id, stateName);
         stateDep.setParent(parentId, parentType);
      }

      return stateDep;
   }
    

   // see base class
   public boolean doesDependencyExist(PSSecurityToken tok, String id, 
      String parentId) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
      
      if (parentId == null || parentId.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentId may not be null or empty");
      
      return getDependency(tok, id, 
         PSWorkflowDefDependencyHandler.DEPENDENCY_TYPE, parentId) != null;
   }

   
   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>TransitionDef</li>
    * </ol>
    *
    * @return An iterator over one or more types as <code>String</code>
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
   public String getParentType()
   {
      return PSWorkflowDefDependencyHandler.DEPENDENCY_TYPE;
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

      reserveNewId(dep, idMap, STATES_TABLE, DEPENDENCY_TYPE);
   }

   /**
    * Override the method from super class, but this is to get the next id 
    * specifically for <code>STATE_ID</code> in <code>STATES_TABLE</code>.
    */
   protected String getNextId(String table, PSDependency dep, 
      String tgtParentId) throws PSDeployException
   {
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      if (tgtParentId == null || tgtParentId.trim().length() == 0)
         throw new IllegalArgumentException(
            "tgtParentId may not be null or empty");
      
      String newId = null;
      
      // Check to see if there are already any states in this workflow.  If not,
      // reuse the source state ids so intial state will keep its id
      Iterator ids = getChildPairIdsFromTable(STATES_TABLE, STATE_ID, 
         WORKFLOW_ID, tgtParentId);
      if (!ids.hasNext())
         newId = dep.getDependencyId();
      else
      {
         // already have states, so get the next available id
         int id = PSDbmsHelper.getInstance().getNextIdInMemory(STATES_TABLE, 
            STATE_ID, WORKFLOW_ID, tgtParentId);
            
         newId = String.valueOf(id);
      }
      
      return newId;
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

      // nothing to deploy, assume it has been handled for workflow handler
      return PSIteratorUtils.emptyIterator();
   }

   // see base class
   public void installDependencyFiles(PSSecurityToken tok,
      PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx)
         throws PSDeployException
   {
      // nothing to install, assume it has been handled for workflow handler
   }
   
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "StateDef";

   // private table and column names
   private static final String STATES_TABLE = "STATES";
   private static final String STATE_ID = "STATEID";
   private static final String STATE_NAME = "STATENAME";

   private static final String TRANSITIONS_TABLE = "TRANSITIONS";
   private static final String TRANSITION_FROM_STATEID =
      "TRANSITIONFROMSTATEID";
   private static final String TRANSITION_ID = "TRANSITIONID";
   private static final String WORKFLOW_ID = "WORKFLOWAPPID";

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List ms_childTypes = new ArrayList();

   static
   {
      ms_childTypes.add(PSTransitionDefDependencyHandler.DEPENDENCY_TYPE);
   }

}
