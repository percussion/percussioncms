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
package com.percussion.workflow.service.impl;

import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.maintenance.service.IPSMaintenanceManager;
import com.percussion.maintenance.service.IPSMaintenanceProcess;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowException;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.services.workflow.data.PSContentAdhocUser;
import com.percussion.services.workflow.data.PSContentApproval;
import com.percussion.services.workflow.data.PSContentWorkflowState;
import com.percussion.services.workflow.data.PSNotification;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.guid.IPSGuid;
import org.hibernate.SessionFactory;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author JaySeletz
 *
 */
public class PSWorkflowCacheBuilderTest
{

    @Test
    public void testBuildWorkflowCache()
    {
        MockMaintMgr maintMgr = new MockMaintMgr();
        MockWorkflowService wfsvc = new MockWorkflowService();
        PSWorkflowCacheBuilder cacheBuilder = new PSWorkflowCacheBuilder(wfsvc, maintMgr);
        
        cacheBuilder.buildWorkflowCache();
        
        int tries = 0;
        while (!wfsvc.didLoadWorkflows)
        {
            tries++;
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                fail("Threadus Interruptus");
            }
            
            if (tries > 1000)
            {
                fail("Did not build workflow cache in alloted time");
            }
        }
        
        assertTrue("Maint proc not started", maintMgr.didStartWork);
        assertTrue("Maint proc not stopped", maintMgr.didStopWork);
    }
    
    private class MockMaintMgr implements IPSMaintenanceManager
    {

        boolean didStartWork = false;
        String procId = null;
        boolean didStopWork = false;
        boolean hasFailures = false;
        
        @Override
        public void startingWork(IPSMaintenanceProcess process)
        {
            procId = process.getProcessId();
            didStartWork = true;
        }

        @Override
        public boolean isWorkInProgress()
        {
            return didStartWork && !didStopWork;
        }

        @Override
        public void workCompleted(IPSMaintenanceProcess process)
        {
            if (process.getProcessId().equals(procId))
                didStopWork = true;
        }

        @Override
        public boolean hasFailures()
        {
            return hasFailures;
        }

        @Override
        public void workFailed(IPSMaintenanceProcess process)
        {
            hasFailures = true;
        }

        @Override
        public boolean clearFailures()
        {
            boolean hadFailures = hasFailures;
            hasFailures = false;
            return hadFailures;
        }        
    }
    
    private class MockWorkflowService implements IPSWorkflowService
    {
        private boolean didLoadWorkflows = false;
        
        
        @Override
        public List<PSWorkflow> findWorkflowsByName(String name)
        {
            didLoadWorkflows = true;
            return null;
        }


        /**
         * Gets the underlying session factory
         *
         * @return
         */
        @Override
        public SessionFactory getSessionFactory() {
            return null;
        }

        /**
         * Sets the session factory. Handled via spring auto wiring.
         *
         * @param sessionFactory
         */
        @Override
        public void setSessionFactory(SessionFactory sessionFactory) {

        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#findWorkflowSummariesByName(java.lang.String)
         */
        @Override
        public List<PSObjectSummary> findWorkflowSummariesByName(String name)
        {
            // Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#loadWorkflow(com.percussion.utils.guid.IPSGuid)
         */
        @Override
        public PSWorkflow loadWorkflow(IPSGuid id)
        {
            // Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#loadWorkflow(com.percussion.utils.guid.IPSGuid)
         */
        @Override
        public PSWorkflow loadWorkflowDb(IPSGuid id)
        {
            // Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#saveWorkflow(com.percussion.services.workflow.data.PSWorkflow)
         */
        @Override
        public void saveWorkflow(PSWorkflow workflow)
        {
            // Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#deleteWorkflow(com.percussion.utils.guid.IPSGuid)
         */
        @Override
        public void deleteWorkflow(IPSGuid wfid) throws Exception
        {
            // Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#loadWorkflowState(com.percussion.utils.guid.IPSGuid, com.percussion.utils.guid.IPSGuid)
         */
        @Override
        public PSState loadWorkflowState(IPSGuid stateId, IPSGuid workflowId)
        {
            // Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#loadWorkflowStateByName(java.lang.String, com.percussion.utils.guid.IPSGuid)
         */
        @Override
        public PSState loadWorkflowStateByName(String stateName, IPSGuid workflowId)
        {
            // Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#createState(com.percussion.utils.guid.IPSGuid)
         */
        @Override
        public PSState createState(IPSGuid workflowId)
        {
            // Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#createTransition(com.percussion.utils.guid.IPSGuid, com.percussion.utils.guid.IPSGuid)
         */
        @Override
        public PSTransition createTransition(IPSGuid wfId, IPSGuid stateId)
        {
            // Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#createNotification(com.percussion.utils.guid.IPSGuid, com.percussion.utils.guid.IPSGuid)
         */
        @Override
        public PSNotification createNotification(IPSGuid wfId, IPSGuid transitionId)
        {
            // Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#isPublic(com.percussion.utils.guid.IPSGuid, com.percussion.utils.guid.IPSGuid)
         */
        @Override
        public boolean isPublic(IPSGuid stateid, IPSGuid workflowId) throws PSWorkflowException
        {
            // Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#findAdhocInfoByUser(java.lang.String)
         */
        @Override
        public List<PSContentAdhocUser> findAdhocInfoByUser(String username)
        {
            // Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#findAdhocInfoByItem(com.percussion.utils.guid.IPSGuid)
         */
        @Override
        public List<PSContentAdhocUser> findAdhocInfoByItem(IPSGuid contentId)
        {
            // Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#saveContentAdhocUser(com.percussion.services.workflow.data.PSContentAdhocUser)
         */
        @Override
        public void saveContentAdhocUser(PSContentAdhocUser adhoc)
        {
            // Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#deleteContentAdhocUser(com.percussion.services.workflow.data.PSContentAdhocUser)
         */
        @Override
        public void deleteContentAdhocUser(PSContentAdhocUser adhoc)
        {
            // Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#getWorkflowStateForContent(java.util.List)
         */
        @Override
        public List<PSContentWorkflowState> getWorkflowStateForContent(List<IPSGuid> contentids)
        {
            // Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#findApprovalsByUser(java.lang.String)
         */
        @Override
        public List<PSContentApproval> findApprovalsByUser(String username)
        {
            // Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#findApprovalsByItem(com.percussion.utils.guid.IPSGuid)
         */
        @Override
        public List<PSContentApproval> findApprovalsByItem(IPSGuid contentid)
        {
            // Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#saveContentApproval(com.percussion.services.workflow.data.PSContentApproval)
         */
        @Override
        public void saveContentApproval(PSContentApproval approval)
        {
            // Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#deleteContentApprovals(com.percussion.utils.guid.IPSGuid)
         */
        @Override
        public void deleteContentApprovals(IPSGuid contentid)
        {
            // Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#getAllWorkflowActions(java.util.List, java.util.List, java.lang.String, java.util.List, java.lang.String)
         */
        @SuppressWarnings("unused")
        @Override
        public List<PSMenuAction> getAllWorkflowActions(List<IPSGuid> contentids,
                List<PSAssignmentTypeEnum> assignmentTypes, String userName, List<String> userRoles, String locale)
                throws PSWorkflowException
        {
            // Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#updateWorkflowVersion(com.percussion.utils.guid.IPSGuid)
         */
        @Override
        public void updateWorkflowVersion(IPSGuid id)
        {
            // Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#addWorkflowRole(com.percussion.utils.guid.IPSGuid, java.lang.String)
         */
        @Override
        public void addWorkflowRole(IPSGuid wfId, String roleName)
        {
            // Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#addRoleToWorkflow(com.percussion.utils.guid.IPSGuid, java.lang.String, com.percussion.services.workflow.data.PSWorkflow)
         */
        @Override
        public void addRoleToWorkflow(IPSGuid id, String roleName, PSWorkflow wf)
        {
            // Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#removeWorkflowRole(com.percussion.utils.guid.IPSGuid, java.lang.String)
         */
        @Override
        public boolean removeWorkflowRole(IPSGuid wfId, String roleName)
        {
            // Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#getDefaultWorkflow()
         */
        @Override
        public PSWorkflow getDefaultWorkflow()
        {
            // Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#getDefaultWorkflowName()
         */
        @Override
        public String getDefaultWorkflowName()
        {
            // Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see com.percussion.services.workflow.IPSWorkflowService#getDefaultWorkflowId()
         */
        @Override
        public IPSGuid getDefaultWorkflowId()
        {
            // Auto-generated method stub
            return null;
        }

        @Override
        public void copyWorkflowToRole(String fromRole, String toRole)
        {
            // Auto-generated method stub
        }
        
    }
}
