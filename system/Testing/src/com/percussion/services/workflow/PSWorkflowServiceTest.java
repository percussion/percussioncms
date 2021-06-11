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
package com.percussion.services.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.workflow.data.PSAdhocTypeEnum;
import com.percussion.services.workflow.data.PSAgingTransition;
import com.percussion.services.workflow.data.PSAssignedRole;
import com.percussion.services.workflow.data.PSContentAdhocUser;
import com.percussion.services.workflow.data.PSContentApproval;
import com.percussion.services.workflow.data.PSContentWorkflowState;
import com.percussion.services.workflow.data.PSNotification;
import com.percussion.services.workflow.data.PSNotificationDef;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSTransitionRole;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.services.workflow.data.PSWorkflowRole;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.timing.PSStopwatch;
import com.percussion.workflow.PSWorkflowAppsContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.JUnit4TestAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit test for the system services to test loading workflows. This has been
 * made a separate test since it can be executed without a running server.
 */
@Category(IntegrationTest.class)
public class PSWorkflowServiceTest
{

   private static final Logger log = LogManager.getLogger(PSWorkflowServiceTest.class);

   /**
    * Fictitious user for testing
    */
   private static final String TEST_USER = "buddy";

   /**
    * Content ids involved
    */
   private static final int ADHOC_CIDS[] =
   {1000, 1001, 1002, 1003, 1004, 1005};

   /**
    * Role ids involved (length must match ADHOC_CIDS)
    */
   private static final int ADHOC_ROLE[] =
   {1, 2, 3, 4, 5, 6};

   /**
    * Adhoc types involved (length must match ADHOC_CIDS)
    */
   private static final PSAdhocTypeEnum ADHOC_TYPES[] =
   {PSAdhocTypeEnum.ANONYMOUS, PSAdhocTypeEnum.ENABLED,
         PSAdhocTypeEnum.DISABLED, PSAdhocTypeEnum.ENABLED,
         PSAdhocTypeEnum.ANONYMOUS, PSAdhocTypeEnum.DISABLED};

   IPSWorkflowService m_service;
   IPSGuidManager m_gmgr;
   IPSGuid m_wf4Id;
   PSWorkflow m_wf4;
   PSState m_wf4State0;
   
   @Before
   public void setUp()
   {
      m_service = PSWorkflowServiceLocator.getWorkflowService();
      m_gmgr = PSGuidManagerLocator.getGuidMgr();
      
      m_wf4Id = m_gmgr.makeGuid(4, PSTypeEnum.WORKFLOW);
      m_wf4 = m_service.loadWorkflow(m_wf4Id);
      List<PSState> states = m_wf4.getStates();
      m_wf4State0 = states.get(0);
   }

   @Test
   public void testAddAndRemoveRole()
   {
      int wfRoleSize = m_wf4.getRoles().size();
      List<Integer> roleSizes = getStateRoleSizes(m_wf4.getStates());
      
      String roleName = "role_name_" + System.currentTimeMillis();
      
      // add role
      m_service.addWorkflowRole(m_wf4Id, roleName);
      
      // validates the added role
      PSWorkflow wf = m_service.loadWorkflow(m_wf4Id);
      assertTrue(wfRoleSize == wf.getRoles().size() - 1);
      validateRoleSizes(wfRoleSize, roleSizes, -1);
      
      // remove role
      m_service.removeWorkflowRole(m_wf4Id, roleName);
      
      // validate remove role
      wf = m_service.loadWorkflow(m_wf4Id);
      assertTrue(wfRoleSize == wf.getRoles().size());
      
      // validate removed role
      validateRoleSizes(wfRoleSize, roleSizes, 0);
      
     // add a role to ALL workflows
      roleName = "role_name_" + System.currentTimeMillis();
      m_service.addWorkflowRole(null, roleName);
      wf = m_service.loadWorkflow(m_wf4Id);
      assertTrue(wfRoleSize == wf.getRoles().size() - 1);
      validateRoleSizes(wfRoleSize, roleSizes, -1);
      
      // remove a role from ALL workflows
      m_service.removeWorkflowRole(null, roleName);
      wf = m_service.loadWorkflow(m_wf4Id);
      assertTrue(wfRoleSize == wf.getRoles().size());
      validateRoleSizes(wfRoleSize, roleSizes, 0);
   }

   private void validateRoleSizes(int wfRoleSize, List<Integer> roleSizes, int offSet)
   {
      PSWorkflow wf = m_service.loadWorkflow(m_wf4Id);
      assertTrue(wfRoleSize == wf.getRoles().size() + offSet);
      List<PSState> states2 = wf.getStates();
      for (int i=0; i < states2.size(); i++)
      {
         PSState s2 = states2.get(i);
         assertTrue(roleSizes.get(i) == s2.getAssignedRoles().size() + offSet);
      }
   }

   private List<Integer> getStateRoleSizes(List<PSState> states)
   {
      List<Integer> result = new ArrayList<Integer>();
      for (PSState s : states)
      {
         result.add(s.getAssignedRoles().size());
      }
      return result;
   }
   
   /**
    * Test create state and transitions
    */
   @Test
   public void testCreation()
   {
      // validates the created IDs are unique
      PSState state1 = createState(m_wf4Id);
      PSState state2 = createState(m_wf4Id);
      
      assertFalse(state1.getGUID().equals(state2.getGUID()));
      
      PSTransition trans1 = m_service.createTransition(m_wf4Id, state1.getGUID());
      PSTransition trans2 = m_service.createTransition(m_wf4Id, state2.getGUID());
      
      assertFalse(trans1.getGUID().equals(trans2.getGUID()));
      
      
      // add state to the workflow
      List<PSState> states = m_wf4.getStates();
      int initSize = states.size();
      states.add(state1);
      states.add(state2);
      
      m_service.saveWorkflow(m_wf4);
      
      PSWorkflow wf2 = m_service.loadWorkflow(m_wf4Id);
      List<PSState> states2 = wf2.getStates();
      assertTrue(states2.size() == initSize + 2);
      
      // remove states from the workflow
      removeState(state1.getGUID(), states2);
      removeState(state2.getGUID(), states2);
      
      m_service.saveWorkflow(wf2);
      
      PSWorkflow wf3 = m_service.loadWorkflow(m_wf4Id);
      List<PSState> states3 = wf3.getStates();
      assertTrue(states3.size() == initSize);
   }
   
   private void removeState(IPSGuid stateId, List<PSState> states)
   {
      Iterator<PSState> it = states.iterator();
      while (it.hasNext())
      {
         PSState s = it.next();
         if (s.getGUID().equals(stateId))
         {
            it.remove();
            return;
         }
      }
   }
   
   private PSState createState(IPSGuid wfId)
   {
      PSState state = m_service.createState(wfId);
      state.setName("State_" + state.getGUID().getUUID());
      state.setDescription(state.getName());
      
      return state;
   }
   
   /**
    * Test load workflow state and transitions
    */
   @Ignore
   public void testLoadWorkflowState()
   {
      IPSGuid state4Id = m_gmgr.makeGuid(4, PSTypeEnum.WORKFLOW_STATE);
      IPSGuid wf6Id = m_gmgr.makeGuid(6, PSTypeEnum.WORKFLOW);
      PSState state46 = m_service.loadWorkflowState(state4Id, wf6Id);

      List<PSTransition> trans = state46.getTransitions();
      List<PSAgingTransition> agingTrans = state46.getAgingTransitions();
      
      assertTrue(trans.size() == 2);
      assertTrue(agingTrans.size() == 2);
      
      for (PSTransition t : trans)
      {
         assertFalse(t.isAllowAllRoles());
      }
   }
   
   /**
    * Tests the CRUD operation for the transition notifications
    */
   @Test
   public void testTtransitionNotificationCRUD()
   {
      List<PSTransition> trans = m_wf4State0.getTransitions();
      PSTransition tran = trans.get(0);

      List<PSNotification> notifs = tran.getNotifications();
      int notifsSize = notifs.size();
      
      PSNotification notif = new PSNotification();
      notif.setTransitionId(tran.getGUID().getUUID());
      notif.setNotificationId(1);
      notif.setWorkflowId(4);
      notif.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_NOTIFICATION, 100));
      
      // add a notification
      notifs.add(notif);
      m_wf4State0.setTransitions(trans);
      m_service.saveWorkflow(m_wf4);
      verifyTransitionNotifsSize(m_wf4Id, notifsSize + 1);
      
      // remove the notification
      PSWorkflow wf = m_service.loadWorkflow(m_wf4Id);
      List<PSState> states = wf.getStates();
      PSState state = states.get(0);
      trans = state.getTransitions();
      tran = trans.get(0);
      
      tran.getNotifications().remove(notif);
      state.setTransitions(trans);
      m_service.saveWorkflow(wf);
      
      verifyTransitionNotifsSize(m_wf4Id, notifsSize);
   }

   private void verifyTransitionNotifsSize(IPSGuid wfId, int size)
   {
      PSWorkflow wf = m_service.loadWorkflow(wfId);
      List<PSState> states = wf.getStates();
      PSState state = states.get(0);
      List<PSTransition> trans = state.getTransitions();
      PSTransition tran = trans.get(0);
      int currSize = tran.getNotifications().size();
      
      assertTrue(currSize == size);
   }

   /**
    * Test the CRUD operation for transition roles
    */
   @Test
   public void testTransitionRoleCRUD()
   {
      // add on existing roles
      PSWorkflow wf = m_service.loadWorkflow(m_wf4Id);
      IPSGuid stateId = new PSGuid(PSTypeEnum.WORKFLOW_STATE, 5);
      PSState state = m_service.loadWorkflowState(stateId, m_wf4Id);
      verifyCachedState(wf, state, stateId);
      
      List<PSTransition> trans = state.getTransitions();
      PSTransition tran = trans.get(0);
      List<PSTransitionRole> roles = tran.getTransitionRoles();
      int initRoleSize = roles.size();
      
      PSTransitionRole role = new PSTransitionRole();
      role.setRoleId(3);
      role.setTransitionId(tran.getGUID().getUUID());
      role.setWorkflowId(4);
      
      // add one role
      roles.add(role);
      state.setTransitions(trans);
      m_service.saveWorkflow(wf);
      
      verifyTransitionRoleSize(m_wf4Id, initRoleSize +1);
      
      // remove the role
      wf = m_service.loadWorkflow(m_wf4Id);
      state = m_service.loadWorkflowState(stateId, m_wf4Id);
      verifyCachedState(wf, state, stateId);
      trans = state.getTransitions();
      tran = trans.get(0);
      roles = tran.getTransitionRoles();
      roles.remove(role);
      
      state.setTransitions(trans);
      m_service.saveWorkflow(wf);
      verifyTransitionRoleSize(m_wf4Id, initRoleSize);
   }

   private void verifyCachedState(PSWorkflow wf, PSState state, IPSGuid stateId)
   {
      for (PSState s : wf.getStates())
      {
         if (s.getGUID().equals(stateId))
         {
            assertTrue(s == state);
            return;
         }
      }
   }
   
   private void verifyTransitionRoleSize(IPSGuid wfId, int size)
   {
      IPSGuid stateId = new PSGuid(PSTypeEnum.WORKFLOW_STATE, 5);
      PSState state = m_service.loadWorkflowState(stateId, m_wf4Id);
      List<PSTransition> trans = state.getTransitions();
      PSTransition tran = trans.get(0);
      List<PSTransitionRole> roles = tran.getTransitionRoles();
      
      assertTrue(roles.size() == size);
   }
   
   /**
    * Tests the cached (non-persisted) transitions.
    */
   @Test
   public void testStateTransitionCache()
   {
      PSState state = m_wf4State0;
      
      // test transition list
      List<PSTransition> trans = state.getTransitions();
      List<PSTransition> trans_2 = state.getTransitions();
      
      assertTrue(trans == trans_2);

      state.setTransitions(trans);
      trans_2 = state.getTransitions();
      
      assertTrue(trans != trans_2);
      assertEquals(trans, trans_2);
      
      // test aging transition list
      
      List<PSAgingTransition> agingTrans = state.getAgingTransitions();
      List<PSAgingTransition> agingTrans_2 = state.getAgingTransitions();
      
      assertTrue(agingTrans == agingTrans_2);
      
      state.setAgingTransitions(agingTrans);
      agingTrans_2 = state.getAgingTransitions();
      
      assertTrue(agingTrans != agingTrans_2);
      assertEquals(agingTrans, agingTrans_2);
   }
   
   /**
    * Tests the CRUD operation for the state transitions.
    */
   @Test
   public void testStateTransitionCRUC()
   {
      List<PSTransition> trans = m_wf4State0.getTransitions();
      
      int initTransSize = trans.size();
      PSTransition tran = trans.get(0);
      
      // remove 1st transition
      trans.remove(tran);
      m_wf4State0.setTransitions(trans);
      m_service.saveWorkflow(m_wf4);
      verifyStateTransitionSize(m_wf4Id, initTransSize - 1);
      
      // add 1st transition back
      PSWorkflow wf2 = m_service.loadWorkflow(m_wf4Id);
      List<PSState> states = wf2.getStates();
      PSState state = states.get(0);
      trans = state.getTransitions();
      trans.add(tran);
      state.setTransitions(trans);
      m_service.saveWorkflow(wf2);
      
      verifyStateTransitionSize(m_wf4Id, initTransSize);
   }
   
   private void verifyStateTransitionSize(IPSGuid wfId, int size)
   {
      PSWorkflow wf = m_service.loadWorkflow(wfId);
      List<PSState> states = wf.getStates();
      PSState state = states.get(0);
      List<PSTransition> trans = state.getTransitions();
      assertTrue(trans.size() == size);
   }
   
   /**
    * Test the CRUD operation for the state roles
    */
   @Test
   public void testStateRoleCRUD()
   {
      IPSGuid id1 = m_gmgr.makeGuid(4, PSTypeEnum.WORKFLOW);
      PSWorkflow wf = m_service.loadWorkflow(id1);
    
      List<PSState> states = wf.getStates();
      PSState state = states.get(0);      
      List<PSAssignedRole> roles = state.getAssignedRoles();
      int initRoleSize = roles.size();
      PSAssignedRole role = roles.get(0);
      
      // remove 1st role
      roles.remove(role);
      m_service.saveWorkflow(wf);
      
      // verify the remove
      verifyStateRoleSize(id1, initRoleSize -1);
      
      // add the role
      roles.add(role);
      m_service.saveWorkflow(wf);
      
      // verify add role
      verifyStateRoleSize(id1, initRoleSize);
   }

   private void verifyStateRoleSize(IPSGuid wfId, int roleSize)
   {
      PSWorkflow wf = m_service.loadWorkflow(wfId);
      List<PSState> states = wf.getStates();
      PSState state = states.get(0);
      List<PSAssignedRole> roles = state.getAssignedRoles();
      assertTrue(roles.size() == roleSize);
   }
   
   /**
    * Test the CRUD operation for workflow roles
    */
   @Test
   public void testWorkflowRoleCRUD()
   {
      IPSGuid id1 = m_gmgr.makeGuid(4, PSTypeEnum.WORKFLOW);
      PSWorkflow wf = m_service.loadWorkflow(id1);
      
      List<PSWorkflowRole> roles = wf.getRoles();
      int initRoleSize = roles.size();
      
      PSWorkflowRole role = new PSWorkflowRole();
      role.setWorkflowId(4);
      role.setName("Default");
      role.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_ROLE, 7));
      
      roles.add(role);
      
      m_service.saveWorkflow(wf);
      wf = m_service.loadWorkflow(id1);
      roles = wf.getRoles();
      assertTrue(roles.size() == initRoleSize + 1);
      
      roles.remove(role);
      m_service.saveWorkflow(wf);
      wf = m_service.loadWorkflow(id1);
      assertTrue(roles.size() == initRoleSize);
   }

   /**
    * Test the CRUD operation for workflow notification definition
    */
   @Test
   public void testWorkflowNotificationCRUD()
   {
      IPSGuid id1 = m_gmgr.makeGuid(4, PSTypeEnum.WORKFLOW);
      PSWorkflow wf = m_service.loadWorkflow(id1);
    
      List<PSNotificationDef> notifs = wf.getNotificationDefs();
      int initRoleSize = notifs.size();
      
      PSNotificationDef notif = new PSNotificationDef();
      notif.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_NOTIFICATION, 301));
      notif.setWorkflowId(4);
      notif.setSubject("Content into Review");
      notif.setBody("A content item has transitioned into the review state.");
      notif.setDescription("Notification for transitions into review state");
      
      // Add one notification
      notifs.add(notif);
      m_service.saveWorkflow(wf);

      // verify the added notification
      PSWorkflow wf2 = m_service.loadWorkflow(id1);
      assertTrue(wf != wf2);
      notifs = wf2.getNotificationDefs();
      assertTrue(notifs.size() == initRoleSize + 1);      

      // Remove the added notification
      notifs.remove(notif);
      m_service.saveWorkflow(wf2);
      
      // verify the removed notification
      PSWorkflow wf3 = m_service.loadWorkflow(id1);
      assertTrue(wf2 != wf3);
      notifs = wf3.getNotificationDefs();
      assertTrue(notifs.size() == initRoleSize);             
   }
   

   /**
    * Setup additional information needed to test adhoc user assignment
    */
   @BeforeClass
   public static void setupAdhocInfo()
   {
      IPSWorkflowService service = PSWorkflowServiceLocator.getWorkflowService();

      teardownAdhocInfo(); // Just in case

      for (int i = 0; i < ADHOC_CIDS.length; i++)
      {
         int cid = ADHOC_CIDS[i];
         int type = ADHOC_TYPES[i].getValue();
         int role = ADHOC_ROLE[i];
         PSContentAdhocUser adhoc = new PSContentAdhocUser(cid, role,
               TEST_USER, type);
         service.saveContentAdhocUser(adhoc);
      }
   }

   /**
    * Teardown additional information needed to test adhoc user assignment
    */
   @AfterClass
   public static void teardownAdhocInfo()
   {
      IPSWorkflowService service = PSWorkflowServiceLocator
            .getWorkflowService();
      List<PSContentAdhocUser> adhocs = service.findAdhocInfoByUser(TEST_USER);
      for (PSContentAdhocUser ah : adhocs)
      {
         service.deleteContentAdhocUser(ah);
      }
   }

   /**
    * Test that the adhoc data is present
    */
   @Test
   public void testAdhocData()
   {
      IPSWorkflowService service = PSWorkflowServiceLocator
            .getWorkflowService();
      List<PSContentAdhocUser> adhocs = service.findAdhocInfoByUser(TEST_USER);
      assertNotNull(adhocs);
      assertTrue(adhocs.size() > 0);

      PSContentAdhocUser adhoc = null;
      for (PSContentAdhocUser a : adhocs)
      {
         assertEquals(TEST_USER, a.getUser());
         if (a.getContentId() == 1001)
         {
            adhoc = a;
            break;
         }
      }
      assertNotNull(adhoc);
      assertEquals(2, adhoc.getRoleId());
      assertEquals(PSAdhocTypeEnum.ENABLED.getValue(), adhoc.getAdhocType());
      assertEquals(2, adhoc.getRoleId());
      
      adhocs = service.findAdhocInfoByItem(new PSLegacyGuid(
         new PSLocator(ADHOC_CIDS[0])));
      
      assertTrue(adhocs.size() == 1);
      PSContentAdhocUser a = adhocs.get(0);
      assertEquals(ADHOC_CIDS[0], a.getContentId());
   }

   /**
    * Test loading workflow objects.
    * 
    * @throws Exception if the test fails.
    */
   @Test
   public void testWorkflowObjects() throws Exception
   {
      IPSWorkflowService service = PSWorkflowServiceLocator
            .getWorkflowService();

      List<PSWorkflow> wfs;
      String suffix = "Workflow";
      wfs = service.findWorkflowsByName("%" + suffix);
      assertTrue(wfs.size() >= 2);
      for (PSWorkflow test : wfs)
      {
         assertTrue(test.getName().endsWith(suffix));
      }

      wfs = service.findWorkflowsByName(null);
      assertTrue(wfs.size() >= 2);
      wfs = service.findWorkflowsByName("");
      assertTrue(wfs.size() >= 2);
      wfs = service.findWorkflowsByName("%");
      assertTrue(wfs.size() >= 2);

      int transcount = 0;
      int agingcount = 0;
      int transNotifCount = 0;

      String[] names =
      {"Simple Workflow", "Standard Workflow"};
      for (String name : names)
      {
         wfs = service.findWorkflowsByName(name);
         assertEquals(wfs.size(), 1);
         PSWorkflow wf = wfs.get(0);
         assertEquals(wf.getName(), name);
         assertTrue(!wf.getStates().isEmpty());
         assertNotNull(wf.getInitialState());
         assertNotNull(wf.getAdministratorRole());

         for (PSState state : wf.getStates())
         {
            assertEquals(state.getWorkflowId(), wf.getGUID().longValue());

            assertTrue(state.getTransitions().size() > 0);
            transcount += state.getTransitions().size();
            for (PSTransition transition : state.getTransitions())
            {
               assertTrue(transition.getToState() > 0);
               assertNotNull(transition.getRequiresComment());
               List<PSNotification> notifs = transition.getNotifications();
               transNotifCount += notifs.size();
               for (PSNotification notif : notifs)
               {
                  assertNotNull(notif.getGUID());
                  assertNotNull(notif.getStateRoleRecipientType());
                  assertEquals(notif.getWorkflowId(), wf.getGUID().longValue());
                  assertEquals(notif.getTransitionId(), transition.getGUID()
                        .longValue());
               }
               if (!transition.isAllowAllRoles())
               {
                  assertNotNull(transition.getTransitionRoles());
                  assertTrue(transition.getTransitionRoles().size() > 0);
               }
            }

            agingcount += state.getAgingTransitions().size();
            for (PSAgingTransition transition : state.getAgingTransitions())
            {
               assertTrue(transition.getToState() > 0);
               assertNotNull(transition.getToState());
               assertNotNull(transition.getType());
               List<PSNotification> notifs = transition.getNotifications();
               transNotifCount += notifs.size();
               for (PSNotification notif : notifs)
               {
                  assertNotNull(notif.getGUID());
                  assertNotNull(notif.getStateRoleRecipientType());
                  assertEquals(notif.getWorkflowId(), wf.getGUID().longValue());
                  assertEquals(notif.getTransitionId(), transition.getGUID()
                        .longValue());
               }
            }

            assertTrue(state.getAssignedRoles().size() > 0);
            for (PSAssignedRole role : state.getAssignedRoles())
            {
               assertEquals(role.getStateId(), state.getStateId());
               assertEquals(role.getWorkflowId(), state.getWorkflowId());
               assertNotNull(role.getAdhocType());
               assertNotNull(role.getAssignmentType());
            }
         }
         assertTrue(!wf.getRoles().isEmpty());
         assertTrue(!wf.getNotificationDefs().isEmpty());
      }

      System.out.println("TRANSITION COUNT: " + transcount);
      System.out.println("AGING COUNT: " + agingcount);
      assertTrue(agingcount > 0);
      assertTrue(transNotifCount > 0);

      PSWorkflow wf = service.loadWorkflow(wfs.get(0).getGUID());
      assertNotNull(wf);
      assertEquals(wf.getName(), wf.getName());

      List<PSObjectSummary> sums;
      sums = service.findWorkflowSummariesByName(null);
      assertTrue(sums.size() >= 2);
      sums = service.findWorkflowSummariesByName("");
      assertTrue(sums.size() >= 2);
      sums = service.findWorkflowSummariesByName("%");
      assertTrue(sums.size() >= 2);

      for (String name : names)
      {
         sums = service.findWorkflowSummariesByName(name);
         assertEquals(sums.size(), 1);
         assertEquals(sums.get(0).getName(), name);
      }

      // loading workflow state
      List<PSState> states = wf.getStates();
      for (PSState state : states)
      {
         PSState s = service.loadWorkflowState(state.getGUID(), wf.getGUID());
         assertTrue(s != null);
      }

      // loading workflow without force to lazy load
      wf = service.loadWorkflow(wf.getGUID());
      PSWorkflowAppsContext wfCtx = new PSWorkflowAppsContext(wf);
      assertTrue(wfCtx != null);
   }

   /**
    * Test the community - workflow relationships for the standard fast forward
    * setup.
    * 
    * @throws Exception for any error.
    */
   @Test
   public void testWorkflowCommunity() throws Exception
   {
      IPSAclService service = PSAclServiceLocator.getAclService();
      IPSBackEndRoleMgr mgr = PSRoleMgrLocator.getBackEndRoleManager();

      List<PSCommunity> communities = mgr.findCommunitiesByName("%");
      List<String> cnames = new ArrayList<String>();
      for (PSCommunity community : communities)
      {
         String name = community.getName();
         cnames.add(name);
      }

      PSStopwatch sw = new PSStopwatch();
      sw.start();
      Collection<IPSGuid> communityWorkflows = service
            .findObjectsVisibleToCommunities(cnames, PSTypeEnum.WORKFLOW);
      sw.stop();
      System.out.println("Check permissions: " + sw);
      assertTrue(communityWorkflows.size() > 0);

      cnames.clear();
      cnames.add("Default");
      sw.start();
      communityWorkflows = service.findObjectsVisibleToCommunities(cnames,
            PSTypeEnum.WORKFLOW);
      sw.stop();
      System.out.println("Check permissions: " + sw);
      assertTrue(communityWorkflows.size() >= 2);
   }

   /**
    * This test checks that the cache causes identical workflow and state
    * objects to be loaded, and that the find methods (which don't use the
    * cache) should return equivalent but not identical objects.
    */
   @Test
   public void testWorkflowAndStateCache()
   {
      IPSWorkflowService service = PSWorkflowServiceLocator
            .getWorkflowService();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid id1 = gmgr.makeGuid(4, PSTypeEnum.WORKFLOW);
      IPSGuid id2 = gmgr.makeGuid(5, PSTypeEnum.WORKFLOW);
      PSWorkflow wf1 = service.loadWorkflow(id1);
      PSWorkflow wf2 = service.loadWorkflow(id2);
      PSStopwatch sw = new PSStopwatch();
      sw.start();
      PSWorkflow wf3 = service.loadWorkflow(id1);
      PSWorkflow wf4 = service.loadWorkflow(id2);
      sw.stop();
      System.out.println("Second load for 2 workflows took " + sw);
      assertTrue(wf1 == wf3);
      assertTrue(wf2 == wf4);

      // Check for equal and the same on find
      List<PSWorkflow> wfs = service.findWorkflowsByName("Simple Workflow");
      assertNotNull(wfs);
      assertEquals(1, wfs.size());
      PSWorkflow wf5 = wfs.get(0);
      assertTrue(wf3 == wf5);
      assertEquals(wf3.getName(), wf5.getName());
      assertEquals(wf3.getInitialStateId(), wf5.getInitialStateId());
      
      // Check for equal but not the same after clear the EHCache
      IPSCmsObjectMgr cmsMgr = PSCmsObjectMgrLocator.getObjectManager();
      cmsMgr.flushSecondLevelCache();
      
      wf3 = service.loadWorkflow(id1);
      wf4 = service.loadWorkflow(id2);
      assertTrue(wf1 != wf3);
      assertTrue(wf2 != wf4);
      assertEquals(wf1.getName(), wf3.getName());
      assertEquals(wf1.getInitialStateId(), wf3.getInitialStateId());

      // load from the EHCache 
      PSWorkflow wf6 = service.loadWorkflow(id1);
      PSWorkflow wf7 = service.loadWorkflow(id2);
      assertTrue(wf6 == wf3);
      assertTrue(wf7 == wf4);

   }

   /**
    * Test the workflow state extraction
    * 
    * @throws PSORMException
    */
   @Test
   public void testGetWorkflowState() throws PSORMException
   {
      IPSWorkflowService service = PSWorkflowServiceLocator
            .getWorkflowService();
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      List<PSComponentSummary> sums = cms.findComponentSummariesByType(311);
      Map<IPSGuid, PSComponentSummary> smap = new HashMap<IPSGuid, PSComponentSummary>();
      List<IPSGuid> guids = new ArrayList<IPSGuid>();
      for (PSComponentSummary s : sums)
      {
         IPSGuid guid = gmgr.makeGuid(s.getCurrentLocator());
         guids.add(guid);
         smap.put(guid, s);
      }
      List<PSContentWorkflowState> states = service
            .getWorkflowStateForContent(guids);
      // Check the state values
      for (PSContentWorkflowState state : states)
      {
         int wfid = (int) state.getWorkflowAppId().longValue();
         int stateid = (int) state.getStateId().longValue();
         PSComponentSummary sum = smap.get(state.getContentId());
         assertEquals(sum.getWorkflowAppId(), wfid);
         assertEquals(sum.getContentStateId(), stateid);
      }

      // Reload and time
      PSStopwatch sw = new PSStopwatch();
      sw.start();
      states = service.getWorkflowStateForContent(guids);
      sw.stop();
      System.out.println("(WorkflowState) Took " + sw + " to load "
            + states.size());
   }
   
   /**
    * Test creating, finding, and deleting content approvals
    * 
    * @throws Exception if there are any errors
    */
   @Test
   public void testContentApprovals() throws Exception
   {
      IPSWorkflowService service = 
         PSWorkflowServiceLocator.getWorkflowService();
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
      
      Set<IPSGuid> contentIds1 = new HashSet<IPSGuid>();
      Set<IPSGuid> contentIds2 = new HashSet<IPSGuid>();
      List<PSContentApproval> approvals = null;
      final String ADMIN1 = "admin1";
      try
      {
         // delete existing
         deleteApprovals(TEST_USER, null);
         deleteApprovals(ADMIN1, null);
         
         // create new
         int contentid1 = 1001;
         int contentid2 = 1002;
         IPSGuid guid1 = guidMgr.makeGuid(new PSLocator(contentid1));
         IPSGuid guid2 = guidMgr.makeGuid(new PSLocator(contentid2));
         
         PSContentApproval approval1 = new PSContentApproval(contentid1, 1003, 
            TEST_USER, 4, 2, 2);
         service.saveContentApproval(approval1);
         contentIds1.add(guid1);
               
         
         PSContentApproval approval2 = new PSContentApproval(contentid2, 1004, 
            TEST_USER, 5, 3, 1);
         service.saveContentApproval(approval2);
         contentIds1.add(guid2);
         
         approvals = service.findApprovalsByUser(TEST_USER);
         assertEquals(approvals.size(), 2);
         assertTrue(approvals.contains(approval1));
         assertTrue(approvals.contains(approval2));
         
         PSContentApproval approval3 = new PSContentApproval(contentid1, 1001, 
            ADMIN1, 4, 2, 2);
         service.saveContentApproval(approval3);
         contentIds2.add(guid1);
         
         PSContentApproval approval4 = new PSContentApproval(contentid2, 1001, 
            ADMIN1, 4, 2, 2);
         service.saveContentApproval(approval4);
         contentIds2.add(guid2);
         
         approvals = service.findApprovalsByUser(ADMIN1);
         assertEquals(approvals.size(), 2);
         assertTrue(approvals.contains(approval3));
         assertTrue(approvals.contains(approval4));
         
         approvals = service.findApprovalsByItem(guid1);
         assertEquals(approvals.size(), 2);
         assertTrue(approvals.contains(approval1));
         assertTrue(approvals.contains(approval3));
         
         approvals = service.findApprovalsByItem(guid2);
         assertEquals(approvals.size(), 2);
         assertTrue(approvals.contains(approval2));
         assertTrue(approvals.contains(approval4));
         
         service.deleteContentApprovals(guid1);
         approvals = service.findApprovalsByItem(guid1);
         assertTrue(approvals.isEmpty());
         approvals = service.findApprovalsByItem(guid2);
         assertTrue(!approvals.isEmpty());
      }
      finally
      {
         deleteApprovals(TEST_USER, contentIds1);
         deleteApprovals(ADMIN1, contentIds2);
      }
   }

   /**
    * Test updating the workflow version
    */
   @Test
   public void testUpdateWorkflowVersion()
   {
      IPSWorkflowService service = 
         PSWorkflowServiceLocator.getWorkflowService();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
            
      // create test workflow
      PSWorkflow workflow = new PSWorkflow();
      workflow.setName("TestUpdateWorkflowVersion");
      workflow.setGUID(gmgr.makeGuid(9999, PSTypeEnum.WORKFLOW));
      service.saveWorkflow(workflow);
      IPSGuid wfGuid = workflow.getGUID();
      
      // update NULL version
      service.updateWorkflowVersion(wfGuid);
      PSWorkflow wf2 = service.loadWorkflow(wfGuid);
      Integer wf2Ver = wf2.getVersion();
      assertEquals(wf2Ver.intValue(), 0);
      
      // update non-NULL version
      service.updateWorkflowVersion(wfGuid);
      PSWorkflow wf3 = service.loadWorkflow(wfGuid);
      assertEquals(wf3.getVersion().intValue(), wf2Ver + 1);
      
      // clean-up
      try
      {
         service.deleteWorkflow(wfGuid);
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }
   
   /**
    * Delete approvals for the supplied user and ensure they are not found
    * 
    * @param userName The username to use, assumed not <code>null</code> or
    * empty.
    * @param contentIds The set of content ids to delete,
    * may be <code>null</code> to delete any approvals the user has.
    */
   private void deleteApprovals(String userName, Set<IPSGuid> contentIds)
   {
      IPSWorkflowService service = 
         PSWorkflowServiceLocator.getWorkflowService();
      
      List<PSContentApproval> approvals;
      approvals = service.findApprovalsByUser(userName);
      for (PSContentApproval approval : approvals)
      {
         IPSGuid guid = new PSLegacyGuid(approval.getContentId(), 0);
         if (contentIds == null || !contentIds.contains(guid))
         {
            service.deleteContentApprovals(guid);
            assertTrue(service.findApprovalsByItem(guid).isEmpty());
         }
      }
   }

}
