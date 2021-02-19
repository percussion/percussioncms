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
package com.percussion.services.workflow.impl;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.data.PSTableChangeEvent;
import com.percussion.data.utils.PSTableUpdateHandlerBase;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.services.security.impl.PSAclService;
import com.percussion.services.workflow.*;
import com.percussion.services.workflow.data.*;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.workflow.PSWorkFlowUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Table;
import java.util.*;
import java.util.stream.Stream;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Implementation of the workflow service
 * 
 * @author dougrand
 */
@PSBaseBean("sys_workflowService")
@Transactional(noRollbackFor = Exception.class)
public class PSWorkflowService
      implements
         IPSWorkflowService
{



   private SessionFactory sessionFactory;

   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }
    @Autowired
   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   /**
    * Commons logger
    */
   static Log ms_log = LogFactory.getLog(PSWorkflowService.class);

   /**
    * This listener responds to table change notices by removing the cached
    * workflows. This almost never happens in production, and the minor time
    * loss in development is unimportant.
    */
   @SuppressWarnings("unchecked")
   public static class EvictionListener extends PSTableUpdateHandlerBase
   {
      /**
       * These are the classes associated with the cached data. We skim these
       * objects, getting the associated table data from the hibernate/ejb3
       * configuration annotations.
       */
      static final Class msi_involvedObjects[] = new Class[]
      {PSWorkflow.class, PSAssignedRole.class, PSTransitionRole.class,
            PSTransitionBase.class, PSNotification.class,
            PSWorkflowRole.class, PSNotificationDef.class, PSState.class};

      /**
       * These tables, initialized from the class data above, are used to advise
       * the listener framework when we care about events.
       */
      static String msi_tables[] = null;

      static
      {
         List<String> tables = new ArrayList<>();
         for (Class c : msi_involvedObjects)
         {
            Table t = (Table) c.getAnnotation(Table.class);
            if (t != null)
            {
               tables.add(t.name());
            }
         }
         msi_tables = new String[tables.size()];
         tables.toArray(msi_tables);
      }

      /**
       * Access to the cache service, wired when the service is wired.
       */
      IPSCacheAccess mi_cache = null;

      /**
       * Ctor
       * 
       * @param cache the cache accessor, never <code>null</code>
       */
      public EvictionListener(IPSCacheAccess cache) {
         super(msi_tables);
         if (cache == null)
         {
            throw new IllegalArgumentException("cache may not be null");
         }
         mi_cache = cache;
      }

      /**
       * This listener cares about the workflow id column only.
       */
      public Iterator getColumns(String tableName, 
         int actionType)
      {
         if (StringUtils.isBlank(tableName))
         {
            throw new IllegalArgumentException("tableName may not be null or "
                  + "empty");
         }
         
         List<String> columns = new ArrayList<>();
         columns.add(WORKFLOW_ID_COLUMN);
         
         return columns.iterator();
      }

      /**
       * Update the version of the workflow which has been modified, then
       * destroy everything in the workflow section of the cache. This is such a
       * rare event that being more fine-grained isn't worthwhile.
       */
      @Transactional
      public void tableChanged(PSTableChangeEvent e)
      {
         if (e == null)
            throw new IllegalArgumentException("event may not be null");
         
         // get the workflow id
         String strWfId = (String) e.getColumns().get(WORKFLOW_ID_COLUMN);
         if (strWfId != null)
         {
            // update the workflow version
            IPSWorkflowService service = 
               PSWorkflowServiceLocator.getWorkflowService();
            service.updateWorkflowVersion(PSGuidUtils.makeGuid(strWfId,
                  PSTypeEnum.WORKFLOW));
         }
         
         mi_cache.clear(CACHE_REGION);
         ms_log.debug("Clearing cache region: " + CACHE_REGION);
      }

   }

   /**
    * Cache region identifier, look at ehcache.xml for more information
    */
   private static final String CACHE_REGION = "workflow";

   /**
    * Workflow id column name.
    */
   private static final String WORKFLOW_ID_COLUMN = "WORKFLOWAPPID";
   
   /**
    * Cache service, used to invalidate site information
    */
   IPSCacheAccess m_cache;
   
   /**
    * GUID manager, initialized by the constructor.
    */
   IPSGuidManager m_guidMgr;
   

   /**
    * Creates the workflow service.
    * @param cache the cache service, not <code>null</code>.
    * @param guidMgr the GUID manager, not <code>null</code>.
    */
   @Autowired
   public PSWorkflowService(IPSCacheAccess cache, IPSGuidManager guidMgr)
   {
      notNull(cache);
      notNull(guidMgr);
      
      setCache(cache);
      m_guidMgr = guidMgr;
   }
   
   //see interface
   @SuppressWarnings("unchecked")
   @Transactional
   public List<PSObjectSummary> findWorkflowSummariesByName(String name)
   {
      String query;
      if (StringUtils.isBlank(name))
         query = "%";
      else
         query = name;
      Session session = sessionFactory.getCurrentSession();
      Criteria c = session.createCriteria(PSWorkflow.class);
      c.add(Restrictions.ilike("name", query));
      /* use a projection to avoid loading all the states, if label gets added
       * to PSWorkflow, then this will need to be modified`
       */
      c.setProjection(Projections.projectionList()
         .add(Projections.property("name"))
         .add(Projections.property("description"))
         .add(Projections.property("id")));

      List<Object> queryResults = c.list();
      List<PSObjectSummary> sums = new ArrayList<>(queryResults.size());
      for (Object o : queryResults)
      {
         Object[] oa = (Object[]) o;
         //name and id should never be null
         sums.add(new PSObjectSummary(new PSGuid(PSTypeEnum.WORKFLOW,
               ((Long) oa[2]).longValue()), oa[0].toString(),
               oa[0].toString(), oa[1] == null ? "" : oa[1].toString()));
      }

      return sums;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.system.IPSSystemService#loadWorkflow(com.percussion.utils.guid.IPSGuid)
    */
   public PSWorkflow loadWorkflow(IPSGuid id)
   {
      notNull(id);
      //  We split out the actual request to the DB.  If this method
      // is transactional a request is made to db to start transaction even
      // if item is returned from cache.
      PSWorkflow rval = (PSWorkflow) m_cache.get(id, CACHE_REGION);
      if (rval == null)
      { 
         
         rval = PSWorkflowServiceLocator.getWorkflowService().loadWorkflowDb(id);
      }
      return rval;
   }

   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.system.IPSSystemService#loadWorkflow(com.percussion.utils.guid.IPSGuid)
    */
   @Transactional
   public PSWorkflow loadWorkflowDb(IPSGuid id)
   {
      
      PSWorkflow rval = (PSWorkflow) sessionFactory.getCurrentSession().get(PSWorkflow.class,
            id.longValue());

      // Always force lazy load for these objects since they
      // are cached in memory
      if (rval != null)
      {
         forceLazyLoad(rval);
         m_cache.save(id, rval, CACHE_REGION);
      }
      return rval;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.IPSWorkflowService#addWorkflowRole(com.percussion.utils.guid.IPSGuid, java.lang.String)
    */
   @Transactional
   public void addWorkflowRole(IPSGuid wfId, String roleName)
   {
      notEmpty(roleName);

      IPSGuid id = m_guidMgr.createGuid(PSTypeEnum.WORKFLOW_ROLE);
      if (wfId == null)
      {
         addRoleToAllWorkflows(id, roleName);
         return;
      }
      
      PSWorkflow wf = loadWorkflow(wfId);
      addRoleToWorkflow(id, roleName, wf);
      saveWorkflow(wf);
   }
   
   @Transactional
   public void addRoleToWorkflow(IPSGuid id, String roleName, PSWorkflow wf)
   {
      
      Validate.notNull(wf);
      Validate.notEmpty(roleName);
      
      if (id == null)
         id = m_guidMgr.createGuid(PSTypeEnum.WORKFLOW_ROLE);
      
      PSWorkflowRole wfRole = createWorkflowRole(id, roleName, wf);
      wf.addRole(wfRole);
      
      for (PSState state : wf.getStates())
      {
         PSAssignmentTypeEnum assignmentType = wf.getName().equalsIgnoreCase(
               "LocalContent")
               ? PSAssignmentTypeEnum.ASSIGNEE
               : PSAssignmentTypeEnum.READER;
         PSAssignedRole role = createPermissionStateRole(state, wfRole.getGUID(), assignmentType);
         state.addAssignedRole(role);
      }
      
      
   }

   /**
    * Adds a role to all workflows. It does the same as 
    * {@link #addWorkflowRole(IPSGuid, String)}, except this will add the role
    * to all workflows.
    * 
    * @param roleName the name of the role, assumed not empty.
    */
   private void addRoleToAllWorkflows(IPSGuid id, String roleName)
   {
      List<PSWorkflow> wfs = loadWorkflows(null);
      for (PSWorkflow wf : wfs)
      {
         addRoleToWorkflow(id, roleName, wf);
         saveWorkflow(wf);
      }      
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.IPSWorkflowService#removeWorkflowRole(com.percussion.utils.guid.IPSGuid, java.lang.String)
    */
   @Transactional
   public boolean removeWorkflowRole(IPSGuid wfId, String roleName)
   {
      notEmpty(roleName);
      
      if (wfId == null)
         return removeRoleFromAllWorkflows(roleName);
      
      PSWorkflow wf = loadWorkflow(wfId);
      return removeRoleFromWorkflow(roleName, wf);
   }

   public void copyWorkflowToRole(String fromRole, String toRole) {
      List<PSWorkflow> wfs = loadWorkflows(null);
      for (PSWorkflow wf : wfs) {
         boolean update = false;
         PSWorkflowRole wfRole = findWorkflowRole(wf, fromRole);
         PSWorkflowRole toWfRole = findWorkflowRole(wf, toRole);
         if (wfRole != null) {
            IPSGuid id;
            if (toWfRole == null) {
               update = true;
               id = m_guidMgr.createGuid(PSTypeEnum.WORKFLOW_ROLE);
               toWfRole = createWorkflowRole(id, toRole, wf);
               wf.addRole(wfRole);
            }
            final IPSGuid toWfRoleGuid = toWfRole.getGUID();


            for (PSState state : wf.getStates()) {
               List<PSAssignedRole> origAssignedRoles = state.getAssignedRoles();

               PSAssignedRole toAssignedRole = null;
               PSAssignedRole fromAssignedRole = null;

               for (PSAssignedRole assignedRole : origAssignedRoles) {
                  if (assignedRole.getGUID().equals(toWfRole.getGUID()))
                     toAssignedRole = assignedRole;
                  else if (assignedRole.getGUID().equals(wfRole.getGUID()))
                     fromAssignedRole = assignedRole;
               }

               if (fromAssignedRole != null) {
                  update = true;
                  if (toAssignedRole == null) {
                     toAssignedRole = createPermissionStateRole(state, toWfRoleGuid, fromAssignedRole.getAssignmentType());
                     state.addAssignedRole(toAssignedRole);
                  } else {
                     toAssignedRole.setAssignmentType(fromAssignedRole.getAssignmentType());
                  }

                  toAssignedRole.setDoNotify(fromAssignedRole.isDoNotify());
                  toAssignedRole.setAdhocType(fromAssignedRole.getAdhocType());
                  toAssignedRole.setShowInInbox(fromAssignedRole.isShowInInbox());
               }

               List<PSTransition> transitions = state.getTransitions();
               for (PSTransition transition : transitions) {
                  List<PSTransitionRole> trs = transition.getTransitionRoles();
                  PSTransitionRole toTr = null;
                  PSTransitionRole fromTr = null;

                  for (PSTransitionRole tr : trs)
                  {
                     if (tr.getGUID().equals(toWfRoleGuid))
                        toTr = tr;
                     if (tr.getGUID().equals(wfRole.getGUID()))
                        fromTr = tr;
                  }

                  if (fromTr != null && toTr == null)
                  {
                     update = true;
                     PSTransitionRole transRole = new PSTransitionRole();
                     transRole.setRoleId(toWfRoleGuid.getUUID());
                     transRole.setTransitionId(transition.getGUID().getUUID());
                     transRole.setWorkflowId(wf.getGUID().getUUID());
                     trs.add(transRole);
                  }

               }
               if (update)
                  state.setTransitions(transitions);

            }

         }
         if (update)
            saveWorkflow(wf);
      }


   }



   /**
    * Removes a specified role from a specified workflow. This does the same
    * as {@link #removeWorkflowRole(IPSGuid, String)}, except this can only
    * remove a role from one workflow.
    *
    * @param roleName the name of the role in question, assumed not empty.
    * @param wf the workflow, assumed not <code>null</code>.
    *
    * @return <code>true</code> if the role has been removed from the workflows;
    * otherwise the role does not exist in the workflow and no role is removed.
    */
   private boolean removeRoleFromWorkflow(String roleName, PSWorkflow wf)
   {
      PSWorkflowRole wfRole = findWorkflowRole(wf, roleName);
      if (wfRole == null)
         return false;

      wf.getRoles().remove(wfRole);

      for (PSState state : wf.getStates())
      {
         removeStateRole(state, wfRole.getGUID());
      }

      saveWorkflow(wf);

      return true;
   }

   /**
    * Removes the specified role from all workflows.
    * 
    * @param roleName the name of the role in question, assumed not empty.
    * 
    * @return <code>true</code> if the role has been removed from any of the workflows;
    * otherwise the role does not exist in any of the workflow and no role is removed.
    */
   private boolean removeRoleFromAllWorkflows(String roleName)
   {
      boolean isRemoved = false;
      List<PSWorkflow> wfs = loadWorkflows(null);
      for (PSWorkflow wf : wfs)
      {
         if (removeRoleFromWorkflow(roleName, wf))
            isRemoved = true;
      }
      
      return isRemoved;
   }
   
   /**
    * Find the specified workflow role from the given workflow.
    * 
    * @param wf the workflow, assumed not <code>null</code>.
    * @param roleName the name of the role in question, assumed not empty.
    * 
    * @return the workflow role with the specified name. It may be <code>null</code> if cannot find one.
    */
   private PSWorkflowRole findWorkflowRole(PSWorkflow wf, String roleName)
   {
      for (PSWorkflowRole r : wf.getRoles())
      {
         if (r.getName().equalsIgnoreCase(roleName))
         {
            return r;
         }
      }
      
      return null;
   }
   
   /**
    * Removes the specified role from the specified state.
    * 
    * @param state the state, assumed not <code>null</code>.
    * @param roleId the ID of the role in question, assumed not <code>null</code>.
    */
   private void removeStateRole(PSState state, IPSGuid roleId)
   {
      Iterator<PSAssignedRole> it = state.getAssignedRoles().iterator();
      while (it.hasNext())
      {
         PSAssignedRole r = it.next();
         if (r.getGUID().equals(roleId))
         {
            it.remove();
            return;
         }
      }
   }
   
   /**
    * Creates a read-only role in the specified workflow state.
    * 
    * @param state the workflow state, assumed not <code>null</code>.
    * @param roleId the ID of the role, assumed not <code>null</code>.
    * @param assignmentType the <code>PSAssignmentTypeEnum</code>
    * to set, assumed not <code>null</code>.
    * 
    * @return the created role, not <code>null</code>.
    */
   private PSAssignedRole createPermissionStateRole(PSState state, 
         IPSGuid roleId, PSAssignmentTypeEnum assignmentType)
   {
      PSAssignedRole role = new PSAssignedRole();
      role.setGUID(roleId);
      role.setWorkflowId(state.getWorkflowId());
      role.setStateId(state.getStateId());
      role.setAdhocType(PSAdhocTypeEnum.DISABLED);
      role.setAssignmentType(assignmentType);
      
      return role;
   }
   
   /**
    * Loads workflows objects using the supplied name filter.
    * 
    * @param name The name, may be <code>null</code> or empty to find all.
    * 
    * @return The list of workflows, never <code>null</code>, may be empty.
    *         The lazily loaded members of the returned objects have not yet
    *         been loaded - call {@link #forceLazyLoad(PSWorkflow)} to load
    *         them.
    */
   @SuppressWarnings(value = {"unchecked"})
   private List<PSWorkflow> loadWorkflows(String name)
   {
      if (StringUtils.isBlank(name))
         name = "%";
      List<PSWorkflow> wfs = sessionFactory.getCurrentSession().createQuery(
            "from PSWorkflow where name like :name").setParameter("name", name).list();
      return wfs;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSSystemService#findWorkflowsByName(String)
    */
   @Transactional
   public List<PSWorkflow> findWorkflowsByName(String name)
   {
      List<PSWorkflow> workflows = new ArrayList<>();
      List<PSObjectSummary> sums = findWorkflowSummariesByName(name);
      for (PSObjectSummary sum : sums)
      {
         PSWorkflow workflow = loadWorkflow(sum.getGUID());
         if (workflow != null)
            workflows.add(workflow);
      }

      return workflows;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.system.IPSSystemService#loadWorkflowState(com.percussion.utils.guid.IPSGuid,
    *      com.percussion.utils.guid.IPSGuid)
    */
   public PSState loadWorkflowState(IPSGuid stateId, IPSGuid workflowId)
   {
      if (workflowId == null
            || workflowId.getType() != PSTypeEnum.WORKFLOW.getOrdinal())
      {
         throw new IllegalArgumentException(
               "workflowId may not be null and must be of type WORKFLOW");
      }
      if (stateId == null
            || stateId.getType() != PSTypeEnum.WORKFLOW_STATE.getOrdinal())
      {
         throw new IllegalArgumentException(
               "stateId may not be null and must be of type WORKFLOW_STATE");
      }

      PSWorkflow wf = loadWorkflow(workflowId);
      for (PSState state : wf.getStates())
      {
         if (stateId.equals(state.getGUID()))
            return state;
      }
      
      return null;      
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.IPSWorkflowService#loadWorkflowStateByName(String, com.percussion.utils.guid.IPSGuid)
    */
   public PSState loadWorkflowStateByName(String stateName, IPSGuid workflowId)
   {
      if (workflowId == null
            || workflowId.getType() != PSTypeEnum.WORKFLOW.getOrdinal())
      {
         throw new IllegalArgumentException(
               "workflowId may not be null and must be of type WORKFLOW");
      }
      if (StringUtils.isBlank(stateName))
      {
         throw new IllegalArgumentException(
               "stateName may not be null");
      }

      PSWorkflow wf = loadWorkflow(workflowId);
      for (PSState state : wf.getStates())
      {
         if (stateName.equals(state.getName()))
            return state;
      }
      
      return null;      
   }

   /**
    * Forces lazy load of members.
    * 
    * @param workflow the workflow to load members, assumed not
    *           <code>null</code>.
    */
   private void forceLazyLoad(PSWorkflow workflow)
   {
      for (PSState state : workflow.getStates())
      {
         forceLazyLoad(state);
      }

      for (PSWorkflowRole role : workflow.getRoles())
      {
         role.getGUID();
      }

      for (PSNotificationDef notif : workflow.getNotificationDefs())
      {
         notif.getGUID();
      }
   }

   /**
    * Make sure the entire state object is in memory
    * 
    * @param state the state, assumed never <code>null</code>
    */
   private void forceLazyLoad(PSState state)
   {
      state.getStateId();

      for (PSTransition t : state.getTransitions())
      {
         t.getGUID();

         for (PSTransitionRole r : t.getTransitionRoles())
         {
            r.getGUID();
         }
         for (PSNotification notif : t.getNotifications())
         {
            notif.getTransitionId();
         }
      }
      for (PSAgingTransition t : state.getAgingTransitions())
      {
         t.getGUID();
      }

      for (PSAssignedRole r : state.getAssignedRoles())
      {
         r.getGUID();
      }
   }

   public boolean isPublic(IPSGuid stateId, IPSGuid workflowId)
         throws PSWorkflowException
   {
      if (workflowId == null)
         throw new IllegalArgumentException("workflowId may not be null.");
      if (stateId == null)
         throw new IllegalArgumentException("stateId may not be null.");

      PSState state = loadWorkflowState(stateId, workflowId);

      if (state == null)
      {
         Object[] args = new Object[]
         {stateId, workflowId};
         throw new PSWorkflowException(
               IPSWorkflowErrors.ERROR_LOADING_WORKFLOW_STATE, args);
      }
      return state.isPublishable();
   }

   /**
    * Spring property accessor
    * 
    * @return get the cache service
    */
   public IPSCacheAccess getCache()
   {
      return m_cache;
   }

   /**
    * Set the cache service
    * 
    * @param cache the service, never <code>null</code>
    */
   public void setCache(IPSCacheAccess cache)
   {
      if (cache == null)
      {
         throw new IllegalArgumentException("cache may not be null");
      }
      m_cache = cache;

      PSServer.addInitListener(new EvictionListener(m_cache));
   }

   public void deleteContentAdhocUser(PSContentAdhocUser adhoc)
   {
      if (adhoc == null)
      {
         throw new IllegalArgumentException("adhoc may not be null");
      }
      sessionFactory.getCurrentSession().delete(adhoc);
   }

   @SuppressWarnings("unchecked")
   public List<PSContentAdhocUser> findAdhocInfoByUser(String username)
   {
      if (username == null || StringUtils.isBlank(username))
      {
         throw new IllegalArgumentException("username may not be null or empty");
      }
      Session s = sessionFactory.getCurrentSession();

         Criteria c = s.createCriteria(PSContentAdhocUser.class);
         c.add(Restrictions.eq("user", username));
         return c.list();

   }
   
   @SuppressWarnings("unchecked")
   @Transactional
   public List<PSContentAdhocUser> findAdhocInfoByItem(IPSGuid contentId)
   {
      if (contentId == null)
         throw new IllegalArgumentException("contentId may not be null");
      
      Session s = sessionFactory.getCurrentSession();

         Criteria c = s.createCriteria(PSContentAdhocUser.class);
         c.add(Restrictions.eq("contentId", contentId.getUUID()));
         
         return c.list();

   }
   
   @Transactional
   public void saveContentAdhocUser(PSContentAdhocUser adhoc)
   {
      if (adhoc == null)
      {
         throw new IllegalArgumentException("adhoc may not be null");
      }
      sessionFactory.getCurrentSession().saveOrUpdate(adhoc);
   }

   @Transactional
   public void deleteWorkflow(IPSGuid wfid) throws Exception
   {
      if (wfid == null)
      {
         throw new IllegalArgumentException("wfid may not be null");
      }
      PSWorkflow workflow = loadWorkflow(wfid);
      if (workflow != null)
      {
         // Check if it is the default workflow         
         if (workflow.getName().equalsIgnoreCase(getDefaultWorkflowName()))
         {
            throw new Exception("The workflow '"+ workflow.getName() + "' cannot be deleted because is the default workflow.");
         }
         sessionFactory.getCurrentSession().delete(workflow);
      }
      m_cache.evict(wfid, CACHE_REGION);
   }

   @Transactional
   public void saveWorkflow(PSWorkflow workflow)
   {
      if (workflow == null)
      {
         throw new IllegalArgumentException("workflow may not be null");
      }
      sessionFactory.getCurrentSession().saveOrUpdate(workflow);
      m_cache.evict(workflow.getGUID(), CACHE_REGION);
   }

   @SuppressWarnings("unchecked")
   @Transactional
   public List<PSContentWorkflowState> getWorkflowStateForContent(
         List<IPSGuid> contentids)
   {
      Session s = sessionFactory.getCurrentSession();

         List<PSContentWorkflowState> rval = new ArrayList<>();
         // Extract content ids
         Map<Integer, IPSGuid> cidToGuid = new HashMap<>();
         for (IPSGuid contentid : contentids)
         {
            PSLegacyGuid lg = (PSLegacyGuid) contentid;
            cidToGuid.put(lg.getContentId(), lg);
         }
         Criteria c = s.createCriteria(PSComponentSummary.class);
         c.add(Restrictions.in("m_contentId", cidToGuid.keySet()));
         ProjectionList list = Projections.projectionList();
         list.add(Projections.property("m_contentId"));
         list.add(Projections.property("m_workflowAppId"));
         list.add(Projections.property("m_contentStateId"));
         c.setProjection(list);
         List<Object[]> results = c.list();
         for (Object[] row : results)
         {
            int cid = (Integer) row[0];
            int wid = (Integer) row[1];
            int sid = (Integer) row[2];
            IPSGuid workflowId = m_guidMgr.makeGuid(wid, PSTypeEnum.WORKFLOW);
            IPSGuid stateId = m_guidMgr.makeGuid(sid, PSTypeEnum.WORKFLOW_STATE);
            IPSGuid contentId = cidToGuid.get(cid);
            rval.add(new PSContentWorkflowState(contentId, workflowId,
                        stateId));
         }
         return rval;

   }

   @Transactional
   public void deleteContentApprovals(IPSGuid contentid)
   {
      if (contentid == null)
         throw new IllegalArgumentException("contentid may not be null");
      List<PSContentApproval> approvals = findApprovalsByItem(contentid);
      if (!approvals.isEmpty())
      {
         Session session = sessionFactory.getCurrentSession();
         approvals.forEach(app -> session.delete(app));
      }

   }

   @SuppressWarnings("unchecked")
   @Transactional
   public List<PSContentApproval> findApprovalsByUser(String username)
   {
      if (username == null || StringUtils.isBlank(username))
      {
         throw new IllegalArgumentException("username may not be null or empty");
      }
      Session s = sessionFactory.getCurrentSession();

         Criteria c = s.createCriteria(PSContentApproval.class);
         c.add(Restrictions.eq("user", username));
         return c.list();

   }
   
   @SuppressWarnings("unchecked")
   @Transactional
   public List<PSContentApproval> findApprovalsByItem(IPSGuid contentid)
   {
      if (contentid == null)
         throw new IllegalArgumentException("contentid may not be null");
      
      Session s = sessionFactory.getCurrentSession();

         Criteria c = s.createCriteria(PSContentApproval.class);
         c.add(Restrictions.eq("contentId", contentid.getUUID()));
         
         return c.list();

   }
   
   @Transactional
   public void saveContentApproval(PSContentApproval approval)
   {
      if (approval == null)
      {
         throw new IllegalArgumentException("adhoc may not be null");
      }
      sessionFactory.getCurrentSession().saveOrUpdate(approval);
   }

   @Transactional
   public List<PSMenuAction> getAllWorkflowActions(List<IPSGuid> contentids,
      List<PSAssignmentTypeEnum> assignmentTypes, String userName,
      List<String> userRoles, String locale) throws PSWorkflowException
   {
      PSWorkflowActionsHelper helper = new PSWorkflowActionsHelper(contentids,
         assignmentTypes, userName, userRoles, locale);
      return helper.getAllWorkflowActions();
   }
   
   @Transactional
   public void updateWorkflowVersion(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      long uuid = id.getUUID();
      List<Integer> verList = getWorkflowVersionForId(uuid);
      if (verList.isEmpty())
         return;
      
      Integer version = verList.get(0);
      if (version != null)
      {
         version++;
      }
      else
      {
         version = new Integer(0);
      }
      
      Session s = sessionFactory.getCurrentSession();

         Query q = s.createQuery(
               "update PSWorkflow w set w.version = :version "
               + "where w.id = :id");
         q.setInteger("version", version);
         q.setLong("id", uuid);
         q.executeUpdate();
         
         m_cache.evict(id, CACHE_REGION);

   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.IPSWorkflowService#createState(com.percussion.utils.guid.IPSGuid)
    */
   @Transactional
   public PSState createState(IPSGuid workflowId)
   {
      notNull(workflowId);
      
      PSState state = new PSState();
      IPSGuid stateId = m_guidMgr.createGuid(PSTypeEnum.WORKFLOW_STATE);
      state.setGUID(stateId);
      state.setWorkflowId(workflowId.longValue());
      
      return state; 
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.IPSWorkflowService#createTransition(com.percussion.utils.guid.IPSGuid, com.percussion.utils.guid.IPSGuid)
    */
   @Transactional
   public PSTransition createTransition(IPSGuid wfId, IPSGuid stateId)
   {
      notNull(wfId);
      notNull(stateId);
      
      PSTransition trans = new PSTransition();
      IPSGuid id = m_guidMgr.createGuid(PSTypeEnum.WORKFLOW_TRANSITION);
      trans.setGUID(id);
      trans.setWorkflowId(wfId.longValue());
      trans.setStateId(stateId.longValue());
      
      return trans;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.workflow.IPSWorkflowService#createNotification(com.percussion.utils.guid.IPSGuid, com.percussion.utils.guid.IPSGuid)
    */
   @Transactional
   public PSNotification createNotification(IPSGuid wfId, IPSGuid transitionId)
   {
      notNull(wfId);
      notNull(transitionId);
      
      PSNotification notif = new PSNotification();
      IPSGuid id = m_guidMgr.createGuid(PSTypeEnum.WORKFLOW_NOTIFICATION);
      notif.setGUID(id);
      notif.setTransitionId(transitionId.longValue());
      notif.setWorkflowId(wfId.longValue());
            
      return notif;
   }
   
   /**
    * Creates a workflow role for the specified workflow and role name.
    *
    * @param id the ID of the created role, assumed not <code>null</code>.
    * @param roleName the name of the role, assumed not blank.
    * @param wf the workflow, assumed not <code>null</code>.
    * 
    * @return the created workflow role with created ID and the specified name, 
    * not <code>null</code>.
    */
   private PSWorkflowRole createWorkflowRole(IPSGuid id, String roleName, PSWorkflow wf)
   {
      PSWorkflowRole role = new PSWorkflowRole();
      role.setGUID(id);
      role.setWorkflowId(wf.getGUID().longValue());
      role.setName(roleName);
      role.setDescription(roleName);
      
      return role;
   }
   
   /**
    * Get the version of a specified workflow.
    * 
    * @param id the workflow id, assumed not <code>null</code>.
    * 
    * @return list with one element, which is the version of the
    * workflow.  May be empty if a version could not be found for the specified
    * workflow.  
    */
   @SuppressWarnings("unchecked")
   private List<Integer> getWorkflowVersionForId(long id)
   {
      return sessionFactory.getCurrentSession().createQuery(
            "select w.version from PSWorkflow w " +
            "where w.id = :id").setParameter("id", id).list();
   }
   
   /**
    * Gets the default workflow object.
    * 
    * @return the a <code>PSWorkflow</code> object. Never empty or <code>null</code>.
    * @throws RuntimeException if the workflow name in the property files is empty, 
    * don't exist in the file or not exist in CMS.
    */
   @Transactional
   public PSWorkflow getDefaultWorkflow()
   {
      String defaultWorkflowName = PSWorkFlowUtils.getDefaultWorkflowProperty();
      
      List<PSWorkflow> defaultWorkflows = findWorkflowsByName(defaultWorkflowName);
      
      if (StringUtils.isBlank(defaultWorkflowName) || defaultWorkflows.isEmpty())
      {
         throw new RuntimeException("The workflow in the " + PSWorkFlowUtils.FILE_PROPERTIES + 
               " workflow property file is empty or not exist.");
      }
      
      return defaultWorkflows.get(0);
   }
   
   /**
    * Gets the name of the default workflow.
    * 
    * @return the name of the default workflow. Never empty or <code>null</code>.
    * @throws RuntimeException if the workflow name in the property files is empty, 
    * don't exist in the file or not exist in CMS.
    */
   @Transactional
   public String getDefaultWorkflowName()
   {
      String defaultWorkflowName = PSWorkFlowUtils.getDefaultWorkflowProperty();
      
      if (StringUtils.isBlank(defaultWorkflowName) || 
            findWorkflowsByName(defaultWorkflowName).isEmpty())
      {
         throw new RuntimeException("The workflow in the " + PSWorkFlowUtils.FILE_PROPERTIES + 
               " workflow property file is empty or not exist.");
      }
      
      return defaultWorkflowName;
   }
   
   /**
    * Gets the default workflow ID.
    * 
    * @return the workflow ID, never <code>null</code>.
    */
   @Transactional
   public IPSGuid getDefaultWorkflowId()
   {
      PSWorkflow defaultWorkflow = getDefaultWorkflow();

      return defaultWorkflow.getGUID();
   }

}
