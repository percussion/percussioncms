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
package com.percussion.services.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.security.PSSecurityProviderPool;
import com.percussion.security.PSThreadRequestUtils;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.security.data.PSCommunityRoleAssociation;
import com.percussion.services.system.PSAssignmentTypeHelper.PSBackendRoleInfo;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSAdhocTypeEnum;
import com.percussion.services.workflow.data.PSAssignedRole;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.services.workflow.data.PSContentAdhocUser;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.services.workflow.data.PSWorkflowRole;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.timing.PSStopwatchStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.framework.JUnit4TestAdapter;

import org.apache.commons.collections.CollectionUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test the adhoc user test
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSAssignmentTypeHelperTest
{
   /**
    * State ID for the community roles test state
    */
   public static final int COMMTEST_STATE_ID = 1004;

   /**
    * State ID for the public test state
    */
   public static final int PUBLIC_STATE_ID = 1003;

   /**
    * State ID for the review test state
    */
   public static final int REVIEW_STATE_ID = 1002;

   /**
    * State ID for the draft test state
    */
   public static final int DRAFT_STATE_ID = 1001;

   /**
    * Test workflow id
    */
   public static final int TEST_WF_ID = 1001;

   /**
    * Guid Manager
    */
   private static IPSGuidManager ms_gmgr = PSGuidManagerLocator.getGuidMgr();

   /**
    * Fictitious user to assign adhoc info for
    */
   public static final String ADHOC_USER_NORMAL = "buddy";

   /**
    * Fictitious user to assign anon adhoc info for
    */
   public static final String ADHOC_USER_ANON = "buddy2";

   /**
    * Content ids involved in adhoc testing
    */
   private static int ADHOC_CIDS[] = new int[6];
   
   /**
    * Content ids involved in community testing
    */
   public static int COMM_CIDS[] = new int[4];
   
   /**
    * Community ids of the items whose IDs are stored in {@link #COMM_CIDS}.
    */
   public static int COMM_IDS[] = new int[4];
   
   /**
    * CID for most tests
    */
   private static int TEST_CID = 0;

   /**
    * Lower bound, component summaries with ids equal or greater than this are
    * assumed to be dummies that should be removed
    */
   private static final int LOWER_CSUM_ID = 100000000;

   /**
    * Use to generate new ids
    */
   private static int ms_current_csum_id = LOWER_CSUM_ID;

   /**
    * Fictictious roles for faked up workflow
    */
   public static enum TestRole {
      /**
       * Someone who can read content and is an assignee for approving, but
       * can't read public
       */
      QA(101),
      /**
       * Someone who can modify content and approve it, but can't read draft
       */
      EDITOR(102),
      /**
       * Someone who is allowed to act on draft items, and read anything else
       */
      AUTHOR(103),
      /**
       * Someone who is allowed to do anything, anytime
       */
      ADMIN(104),
      
      /**
       * Someone from the EI community
       */
      EI_MEMBERS(105),
      
      /**
       * Someone from the CI community
       */
      CI_MEMBERS(106),
      
      /**
       * Someone from the EI admin community
       */
      EI_ADMIN_MEMBERS(107),
      
      /**
       * Someone from the CI admin community
       */
      CI_ADMIN_MEMBERS(108);      

      /**
       * The roleid
       */
      private int mi_roleid;

      /**
       * Internal ctor
       * 
       * @param roleid
       */
      TestRole(int roleid) {
         mi_roleid = roleid;
      }

      /**
       * Get the roleid
       * 
       * @return the roleid
       */
      public int getRoleId()
      {
         return mi_roleid;
      }

   }

   /**
    * Role ids involved (length must match ADHOC_CIDS)
    */
   private static final int ADHOC_ROLE[] =
   {TestRole.QA.getRoleId(), TestRole.EDITOR.getRoleId(),
         TestRole.AUTHOR.getRoleId(), TestRole.QA.getRoleId(),
         TestRole.EDITOR.getRoleId(), TestRole.AUTHOR.getRoleId()};
   
   /**
    * Create a dummy component summary
    * 
    * @return the new component summary
    */
   public static PSComponentSummary createNewComponentSummary()
   {
      PSComponentSummary sum = new PSComponentSummary();
      sum.setContentId(ms_current_csum_id++);
      sum.setCommunityId(1001);
      sum.setContentCreatedDate(new Date());
      sum.setName("cid" + sum.getContentId());
      sum.setWorkflowAppId(TEST_WF_ID);
      sum.setContentStateId(1);
      return sum;
   }

   /**
    * Setup additional information needed to test adhoc user assignment
    * 
    * @throws PSORMException
    */
   @BeforeClass
   public static void setupInfo() throws PSORMException
   {
      IPSWorkflowService service = PSWorkflowServiceLocator
            .getWorkflowService();
      List<PSComponentSummary> sumsToSave = new ArrayList<PSComponentSummary>();

      teardownInfo(); // Just in case

      PSThreadRequestUtils.initServerThreadRequest();
      
      PSServerConfiguration config = PSServer.getServerConfiguration();
      // this loads security provider instances
      com.percussion.security.PSSecurityProviderPool.init(config);
      
      PSComponentSummary test = createNewComponentSummary();
      TEST_CID = test.getContentId();
      sumsToSave.add(test);

      for (int i = 0; i < ADHOC_CIDS.length; i++)
      {
         PSComponentSummary sum = createNewComponentSummary();
         int cid = sum.getContentId();
         ADHOC_CIDS[i] = cid;
         sumsToSave.add(sum);
         int type = i < 3
               ? PSAdhocTypeEnum.ANONYMOUS.getValue()
               : PSAdhocTypeEnum.ENABLED.getValue();
         String name = i < 3 ? ADHOC_USER_ANON : ADHOC_USER_NORMAL;
         int role = ADHOC_ROLE[i];
         PSContentAdhocUser adhoc = new PSContentAdhocUser(cid, role, name,
               type);
         service.saveContentAdhocUser(adhoc);
      }
      
      for (int i = 0, commId = 1001; i < COMM_CIDS.length; i++, commId++)
      {
         PSComponentSummary sum = createNewComponentSummary();
         int cid = sum.getContentId();
         COMM_CIDS[i] = cid;
         COMM_IDS[i] = commId;
         sum.setCommunityId(commId);
         sumsToSave.add(sum);
         
         // EI_Admin is adhoc normal
         if (commId == 1001)
         {
            PSContentAdhocUser adhoc = new PSContentAdhocUser(cid, 
               TestRole.EI_ADMIN_MEMBERS.getRoleId(), ADHOC_USER_ANON,
               PSAdhocTypeEnum.ANONYMOUS.getValue());
            service.saveContentAdhocUser(adhoc);
         }
         // EI is adhoc anonymous
         else if (commId == 1002)
         {
            PSContentAdhocUser adhoc = new PSContentAdhocUser(cid, 
               TestRole.EI_MEMBERS.getRoleId(), ADHOC_USER_NORMAL,
               PSAdhocTypeEnum.ENABLED.getValue());
            service.saveContentAdhocUser(adhoc);
         }
      }

      PSWorkflow testwf = new PSWorkflow();
      testwf.setName("TestWorkflow");
      testwf.setGUID(ms_gmgr.makeGuid(TEST_WF_ID, PSTypeEnum.WORKFLOW));
      testwf.setAdministratorRole(TestRole.ADMIN.name());
      testwf.addRole(makeWorkflowRole(TestRole.ADMIN, testwf));
      testwf.addRole(makeWorkflowRole(TestRole.QA, testwf));
      testwf.addRole(makeWorkflowRole(TestRole.EDITOR, testwf));
      testwf.addRole(makeWorkflowRole(TestRole.AUTHOR, testwf));
      testwf.addRole(makeWorkflowRole(TestRole.EI_MEMBERS, testwf));
      testwf.addRole(makeWorkflowRole(TestRole.CI_MEMBERS, testwf));
      testwf.addRole(makeWorkflowRole(TestRole.EI_ADMIN_MEMBERS, testwf));
      testwf.addRole(makeWorkflowRole(TestRole.CI_ADMIN_MEMBERS, testwf));      
      PSState testDraftState = new PSState();
      testwf.addState(testDraftState);
      testDraftState.setGUID(ms_gmgr.makeGuid(DRAFT_STATE_ID, 
         PSTypeEnum.WORKFLOW_STATE));
      testDraftState.setName("Draft");
      testDraftState.setWorkflowId(testwf.getGUID().longValue());
      testDraftState.addAssignedRole(makeRole(TestRole.ADMIN, testDraftState,
            testwf, PSAssignmentTypeEnum.ADMIN, PSAdhocTypeEnum.DISABLED));
      testDraftState.addAssignedRole(makeRole(TestRole.QA,
            testDraftState, testwf, PSAssignmentTypeEnum.READER,
            PSAdhocTypeEnum.ENABLED));
      testDraftState.addAssignedRole(makeRole(TestRole.EDITOR, testDraftState,
            testwf, PSAssignmentTypeEnum.NONE, PSAdhocTypeEnum.DISABLED));
      testDraftState.addAssignedRole(makeRole(TestRole.AUTHOR, testDraftState,
            testwf, PSAssignmentTypeEnum.ASSIGNEE, PSAdhocTypeEnum.ANONYMOUS));

      PSState testReviewState = new PSState();
      testwf.addState(testReviewState);
      testReviewState
            .setGUID(ms_gmgr.makeGuid(REVIEW_STATE_ID, 
               PSTypeEnum.WORKFLOW_STATE));
      testReviewState.setName("Review");
      testReviewState.setWorkflowId(testwf.getGUID().longValue());
      testReviewState.addAssignedRole(makeRole(TestRole.ADMIN, testReviewState,
            testwf, PSAssignmentTypeEnum.ADMIN, PSAdhocTypeEnum.DISABLED));
      testReviewState.addAssignedRole(makeRole(TestRole.AUTHOR,
            testReviewState, testwf, PSAssignmentTypeEnum.READER,
            PSAdhocTypeEnum.ANONYMOUS));
      testReviewState.addAssignedRole(makeRole(TestRole.QA,
            testReviewState, testwf, PSAssignmentTypeEnum.ASSIGNEE,
            PSAdhocTypeEnum.ENABLED));
      testReviewState.addAssignedRole(makeRole(TestRole.EDITOR,
            testReviewState, testwf, PSAssignmentTypeEnum.READER,
            PSAdhocTypeEnum.ENABLED));

      PSState testPublicState = new PSState();
      testwf.addState(testPublicState);
      testPublicState.setGUID(ms_gmgr.makeGuid(PUBLIC_STATE_ID, 
               PSTypeEnum.WORKFLOW_STATE));
      testPublicState.setName("Public");
      testPublicState.setWorkflowId(testwf.getGUID().longValue());
      testPublicState.addAssignedRole(makeRole(TestRole.ADMIN, testPublicState,
            testwf, PSAssignmentTypeEnum.ADMIN, PSAdhocTypeEnum.DISABLED));
      testPublicState.addAssignedRole(makeRole(TestRole.AUTHOR,
            testPublicState, testwf, PSAssignmentTypeEnum.READER,
            PSAdhocTypeEnum.ENABLED));
      testPublicState.addAssignedRole(makeRole(TestRole.EDITOR,
            testPublicState, testwf, PSAssignmentTypeEnum.READER,
            PSAdhocTypeEnum.ANONYMOUS));
      
      // state with community roles
      PSState testCommState = new PSState();
      testwf.addState(testCommState);
      testCommState.setGUID(ms_gmgr.makeGuid(COMMTEST_STATE_ID, 
         PSTypeEnum.WORKFLOW_STATE));
      testCommState.setName("CommTest");
      testCommState.setWorkflowId(testwf.getGUID().longValue());
      PSAssignedRole role;
      role = makeRole(TestRole.EDITOR, testCommState,
         testwf, PSAssignmentTypeEnum.ASSIGNEE, PSAdhocTypeEnum.DISABLED);
      role.setDoNotify(true);
      testCommState.addAssignedRole(role);
      testCommState.addAssignedRole(makeRole(TestRole.AUTHOR, testCommState,
         testwf, PSAssignmentTypeEnum.READER, PSAdhocTypeEnum.DISABLED));  
      role = makeRole(TestRole.QA,
         testCommState, testwf, PSAssignmentTypeEnum.ASSIGNEE,
         PSAdhocTypeEnum.ENABLED);
      role.setDoNotify(true);
      testCommState.addAssignedRole(role);  
      
      role = makeRole(TestRole.EI_MEMBERS,
         testCommState, testwf, PSAssignmentTypeEnum.ASSIGNEE,
         PSAdhocTypeEnum.ENABLED);
      role.setDoNotify(true);
      testCommState.addAssignedRole(role);
 
      role = makeRole(TestRole.EI_ADMIN_MEMBERS, 
         testCommState, testwf, PSAssignmentTypeEnum.ASSIGNEE, 
         PSAdhocTypeEnum.ANONYMOUS);
      role.setDoNotify(true);
      testCommState.addAssignedRole(role);      
      testCommState.addAssignedRole(makeRole(TestRole.CI_MEMBERS, testCommState,
            testwf, PSAssignmentTypeEnum.ASSIGNEE, PSAdhocTypeEnum.DISABLED));
      testCommState.addAssignedRole(makeRole(TestRole.CI_ADMIN_MEMBERS, 
         testCommState, testwf, PSAssignmentTypeEnum.READER, 
         PSAdhocTypeEnum.DISABLED));      

      service.saveWorkflow(testwf);
      
      // Create dummy content items
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      cms.saveComponentSummaries(sumsToSave);

      // Enable filtering workflow roles by community roles because
      // this feature is off by the OOB server
      PSAssignmentTypeHelper.setFilterAssignedRolesByCommnity(true);
   }

   /**
    * Make a workflow role
    * 
    * @param role
    * @param wf
    * @return a new workflow role, never <code>null</code>
    */
   private static PSWorkflowRole makeWorkflowRole(TestRole role, PSWorkflow wf)
   {
      PSWorkflowRole wfrole = new PSWorkflowRole();
      wfrole.setName(role.name());
      wfrole.setGUID(ms_gmgr.makeGuid(role.getRoleId(),
            PSTypeEnum.WORKFLOW_ROLE));
      wfrole.setWorkflowId(wf.getGUID().longValue());
      return wfrole;
   }

   /**
    * Make an assigned role
    * 
    * @param trole
    * @param state
    * @param wf
    * @param assignmentType
    * @param adhocType
    * @return a new assigned role, never <code>null</code>
    */
   private static PSAssignedRole makeRole(TestRole trole, PSState state,
         PSWorkflow wf, PSAssignmentTypeEnum assignmentType,
         PSAdhocTypeEnum adhocType)
   {
      PSAssignedRole role = new PSAssignedRole();
      role.setStateId(state.getGUID().longValue());
      role.setWorkflowId(wf.getGUID().longValue());
      role.setGUID(ms_gmgr
            .makeGuid(trole.getRoleId(), PSTypeEnum.WORKFLOW_ROLE));
      role.setAssignmentType(assignmentType);
      if (adhocType != null)
      {
         role.setAdhocType(adhocType);
      }
      
      role.setDoNotify(false);
      
      return role;
   }

   /**
    * Teardown additional information needed to test adhoc user assignment
    * 
    * @throws PSORMException
    */
   @AfterClass
   public static void teardownInfo() throws PSORMException
   {
      IPSWorkflowService service = PSWorkflowServiceLocator
            .getWorkflowService();
      List<PSContentAdhocUser> adhocs = service
            .findAdhocInfoByUser(ADHOC_USER_ANON);
      for (PSContentAdhocUser ah : adhocs)
      {
         service.deleteContentAdhocUser(ah);
      }
      adhocs = service.findAdhocInfoByUser(ADHOC_USER_NORMAL);
      for (PSContentAdhocUser ah : adhocs)
      {
         service.deleteContentAdhocUser(ah);
      }

      IPSGuid wf = ms_gmgr.makeGuid(TEST_WF_ID, PSTypeEnum.WORKFLOW);
      if (wf != null)
      {
         try
         {
            service.deleteWorkflow(wf);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      // Cleanup test component summaries
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      List<PSComponentSummary> sumsToDelete = new ArrayList<PSComponentSummary>();
      for (int i = LOWER_CSUM_ID; i < LOWER_CSUM_ID + 100; i++)
      {
         PSComponentSummary sum = cms.loadComponentSummary(i);
         if (sum != null)
         {
            sumsToDelete.add(sum);
         }
      }
      if (sumsToDelete.size() > 0)
      {
         cms.deleteComponentSummaries(sumsToDelete);
      }
      
      // Disabled filtering workflow roles by community roles because
      // this feature is off by the OOB server      
      PSAssignmentTypeHelper.setFilterAssignedRolesByCommnity(false);
      
      if(PSRequestInfo.isInited())
         PSRequestInfo.resetRequestInfo();
   }

   /**
    * Test that the supplied values resolve to the specified assignment type.
    * This method modifies the component summary of the supplied content id to 
    * specify the supplied workflow and state id.
    * 
    * @param val The assignment type value to match, assumed not
    * <code>null</code>.
    * @param contentid The contentid of the item to update and test.
    * @param workflowid The workflow id to use.
    * @param stateid The state id to use.
    * @param user The name of the user, assumed not <code>null</code> or empty.
    * @param roles A list of the user's roles, assumed not <code>null</code>.
    * @param communityId The user's current community id.
    * 
    * @throws PSORMException if there are errors updating the component summary.
    * @throws PSSystemException If there are errors determining the assignment
    * type.
    */
   private void doTest(PSAssignmentTypeEnum val, int contentid, int workflowid,
         int stateid, String user, List<String> roles, int communityId)
         throws PSORMException, PSSystemException
   {
      updateComponentSummary(contentid, workflowid, stateid);

      IPSSystemService sservice = PSSystemServiceLocator.getSystemService();
      List<PSAssignmentTypeEnum> results = sservice.getContentAssignmentTypes(
            toGuidList(Collections.singletonList(contentid)), user,
            roles, communityId);
      assertEquals(val, results.get(0));
   }

   /**
    * Update the component summary of the supplied content id to the specified
    * wf and state.
    * 
    * @param contentid The content id to use.
    * @param workflowid The workflow id to set.
    * @param stateid The state id to set.
    * 
    * @throws PSORMException if the update fails. 
    */
   public static void updateComponentSummary(int contentid, int workflowid,
      int stateid) throws PSORMException
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      List<PSComponentSummary> sumsToSave = new ArrayList<PSComponentSummary>();
      PSComponentSummary sum = cms.loadComponentSummary(contentid);
      sum.setContentStateId(stateid);
      sum.setWorkflowAppId(workflowid);
      sumsToSave.add(sum);
      cms.saveComponentSummaries(sumsToSave);
   }

   /**
    * Convert a list of ids to appropriate guids
    * @param ids the ids
    * @return the same list as guids
    */
   private List<IPSGuid> toGuidList(List<Integer> ids)
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      if (ids.size() == 1)
      {
         int contentid = ids.get(0);
         PSComponentSummary sum = cms.loadComponentSummary(contentid);
         return Collections.singletonList(
               ms_gmgr.makeGuid(sum.getCurrentLocator())
               );
      }
      else
      {
         List<IPSGuid> rval = new ArrayList<IPSGuid>();
         for(int id : ids)
         {
            PSComponentSummary sum = cms.loadComponentSummary(id);
            rval.add(ms_gmgr.makeGuid(sum.getCurrentLocator()));
         }
         return rval;
      }
      
   }

   /**
    * Test a number of cases that are easy to figure out from a set of user
    * roles using the simple workflow as the example.
    * 
    * @throws PSSystemException
    * @throws PSORMException
    */
   @Test
   public void testStraightAssignmentType() throws PSSystemException,
         PSORMException
   {
      List<String> roles = new ArrayList<String>();
      roles.add("Author");
      roles.add("Editor");

      doTest(PSAssignmentTypeEnum.ASSIGNEE, TEST_CID, 4, 1, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.READER, TEST_CID, 4, 2, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.READER, TEST_CID, 4, 3, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.READER, TEST_CID, 4, 4, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);

      roles.clear();
      roles.add("QA");
      doTest(PSAssignmentTypeEnum.NONE, TEST_CID, 4, 1, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.READER, TEST_CID, 4, 2, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.ASSIGNEE, TEST_CID, 4, 3, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.ASSIGNEE, TEST_CID, 4, 4, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);

      roles.clear();
      roles.add("Admin");
      doTest(PSAssignmentTypeEnum.ADMIN, TEST_CID, 4, 1, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.ADMIN, TEST_CID, 4, 2, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.ADMIN, TEST_CID, 4, 3, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.ADMIN, TEST_CID, 4, 4, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);
      
      // In the wrong community the access devolves to reader
      doTest(PSAssignmentTypeEnum.READER, TEST_CID, 4, 1, ADHOC_USER_NORMAL,
            roles, COMM_IDS[1]);
      doTest(PSAssignmentTypeEnum.READER, TEST_CID, 4, 2, ADHOC_USER_NORMAL,
            roles, COMM_IDS[1]);
      doTest(PSAssignmentTypeEnum.READER, TEST_CID, 4, 3, ADHOC_USER_NORMAL,
            roles, COMM_IDS[1]);
      doTest(PSAssignmentTypeEnum.READER, TEST_CID, 4, 4, ADHOC_USER_NORMAL,
            roles, COMM_IDS[1]);      

      roles.clear();
      roles.add("Editor");
      doTest(PSAssignmentTypeEnum.READER, TEST_CID, 4, 1, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.READER, TEST_CID, 4, 2, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.READER, TEST_CID, 4, 3, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.READER, TEST_CID, 4, 4, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);

      roles.clear();
      roles.add("Author");

      doTest(PSAssignmentTypeEnum.ASSIGNEE, TEST_CID, 4, 1, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.NONE, TEST_CID, 4, 2, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.READER, TEST_CID, 4, 3, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.READER, TEST_CID, 4, 4, ADHOC_USER_NORMAL,
            roles, COMM_IDS[0]);

      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      List<Integer> cids = new ArrayList<Integer>();
      List<PSComponentSummary> sums = cms.findComponentSummariesByType(311);
      for (PSComponentSummary sum : sums)
      {
         cids.add(sum.getContentId());
      }
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.finish();
      sws.start("test");
      IPSSystemService sservice = PSSystemServiceLocator.getSystemService();
      sservice.getContentAssignmentTypes(toGuidList(cids), ADHOC_USER_NORMAL, 
            roles, COMM_IDS[0]);
      sws.stop();
      System.out.println("For " + cids.size() + " items, Stats: "
            + sws.getStats());
      sws.finish();
   }

   /**
    * Perform a series of tests and check that the specified adhoc assignment
    * types do or don't apply as appropriate
    * 
    * @throws PSSystemException
    * @throws PSORMException
    */
   @Test
   public void testAdhocAssignmentTypes() throws PSSystemException,
         PSORMException
   {
      List<String> roles = new ArrayList<String>();
      roles.add(TestRole.EDITOR.name());
      roles.add(TestRole.QA.name());
      // Draft state
      doTest(PSAssignmentTypeEnum.NONE, ADHOC_CIDS[0], TEST_WF_ID,
         DRAFT_STATE_ID, ADHOC_USER_ANON, roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.NONE, ADHOC_CIDS[1], TEST_WF_ID,
         DRAFT_STATE_ID, ADHOC_USER_ANON, roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.ASSIGNEE, ADHOC_CIDS[2], TEST_WF_ID,
         DRAFT_STATE_ID, ADHOC_USER_ANON, roles, COMM_IDS[0]);

      doTest(PSAssignmentTypeEnum.READER, ADHOC_CIDS[3], TEST_WF_ID,
         DRAFT_STATE_ID, ADHOC_USER_NORMAL, roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.NONE, ADHOC_CIDS[4], TEST_WF_ID,
         DRAFT_STATE_ID, ADHOC_USER_NORMAL, roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.NONE, ADHOC_CIDS[5], TEST_WF_ID,
         DRAFT_STATE_ID, ADHOC_USER_NORMAL, roles, COMM_IDS[0]);

      // Review state
      doTest(PSAssignmentTypeEnum.NONE, ADHOC_CIDS[0], TEST_WF_ID,
         REVIEW_STATE_ID, ADHOC_USER_ANON, roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.NONE, ADHOC_CIDS[1], TEST_WF_ID,
         REVIEW_STATE_ID, ADHOC_USER_ANON, roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.READER, ADHOC_CIDS[2], TEST_WF_ID,
         REVIEW_STATE_ID, ADHOC_USER_ANON, roles, COMM_IDS[0]);

      doTest(PSAssignmentTypeEnum.ASSIGNEE, ADHOC_CIDS[3], TEST_WF_ID,
         REVIEW_STATE_ID, ADHOC_USER_NORMAL, roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.READER, ADHOC_CIDS[4], TEST_WF_ID,
         REVIEW_STATE_ID, ADHOC_USER_NORMAL, roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.NONE, ADHOC_CIDS[5], TEST_WF_ID,
         REVIEW_STATE_ID, ADHOC_USER_NORMAL, roles, COMM_IDS[0]);

      // Public state
      doTest(PSAssignmentTypeEnum.NONE, ADHOC_CIDS[0], TEST_WF_ID,
         PUBLIC_STATE_ID, ADHOC_USER_ANON, roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.READER, ADHOC_CIDS[1], TEST_WF_ID,
         PUBLIC_STATE_ID, ADHOC_USER_ANON, roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.NONE, ADHOC_CIDS[2], TEST_WF_ID,
         PUBLIC_STATE_ID, ADHOC_USER_ANON, roles, COMM_IDS[0]);

      doTest(PSAssignmentTypeEnum.NONE, ADHOC_CIDS[3], TEST_WF_ID,
         PUBLIC_STATE_ID, ADHOC_USER_NORMAL, roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.NONE, ADHOC_CIDS[4], TEST_WF_ID,
         PUBLIC_STATE_ID, ADHOC_USER_NORMAL, roles, COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.NONE, ADHOC_CIDS[5], TEST_WF_ID,
         PUBLIC_STATE_ID, ADHOC_USER_NORMAL, roles, COMM_IDS[0]);
      
      // test w/no adhoc assignment, should behave as normal role
      doTest(PSAssignmentTypeEnum.ASSIGNEE, TEST_CID, TEST_WF_ID,
         REVIEW_STATE_ID, ADHOC_USER_NORMAL, Collections
            .singletonList(TestRole.QA.name()), COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.ADMIN, TEST_CID, TEST_WF_ID, PUBLIC_STATE_ID,
         ADHOC_USER_NORMAL, Collections.singletonList(TestRole.ADMIN.name()),
         COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.READER, TEST_CID, TEST_WF_ID,
         PUBLIC_STATE_ID, ADHOC_USER_NORMAL, Collections
            .singletonList(TestRole.AUTHOR.name()), COMM_IDS[0]);
      doTest(PSAssignmentTypeEnum.READER, TEST_CID, TEST_WF_ID,
         PUBLIC_STATE_ID, ADHOC_USER_ANON, Collections
            .singletonList(TestRole.EDITOR.name()), COMM_IDS[0]);
   }
   
   /**
    * Perform a series of tests and check that the community specific roles
    * do or don't apply as appropriate
    * 
    * @throws Exception If the test fails
    */
   @Test
   public void testCommunityAssignmentTypes() throws Exception
   {
      List<String> roles = new ArrayList<String>();
      roles.add(TestRole.EDITOR.name());
      roles.add(TestRole.QA.name());

      doTest(PSAssignmentTypeEnum.ASSIGNEE, ADHOC_CIDS[0], TEST_WF_ID,
         COMMTEST_STATE_ID, ADHOC_USER_ANON, roles, 1001);
      
      // Item in EI_Admin, User in EI_Admin role, logged into that community
      roles.clear();
      roles.add(TestRole.EI_ADMIN_MEMBERS.name());
      doTest(PSAssignmentTypeEnum.ASSIGNEE, COMM_CIDS[0], TEST_WF_ID,
         COMMTEST_STATE_ID, ADHOC_USER_ANON, roles, COMM_IDS[0]);

      // Item in EI_Admin, User in EI_Admin role/community, not assigned adhoc
      doTest(PSAssignmentTypeEnum.NONE, COMM_CIDS[0], TEST_WF_ID,
         COMMTEST_STATE_ID, ADHOC_USER_NORMAL, roles, COMM_IDS[0]);

      // Item in EI_Admin, User in EI_Admin role, not logged into that community
      roles.add(TestRole.EI_MEMBERS.name());
      doTest(PSAssignmentTypeEnum.READER, COMM_CIDS[0], TEST_WF_ID,
         COMMTEST_STATE_ID, ADHOC_USER_ANON, roles, COMM_IDS[1]);

      // Item in EI_Admin, User not in that community
      roles.clear();
      roles.add(TestRole.EI_MEMBERS.name());
      doTest(PSAssignmentTypeEnum.NONE, COMM_CIDS[0], TEST_WF_ID,
         COMMTEST_STATE_ID, ADHOC_USER_NORMAL, roles, COMM_IDS[1]);

      // Item in EI, user in EI role, logged into EI community
      doTest(PSAssignmentTypeEnum.ASSIGNEE, COMM_CIDS[1], TEST_WF_ID,
         COMMTEST_STATE_ID, ADHOC_USER_NORMAL, roles, COMM_IDS[1]);

      // Item in EI, User in that role/comm, not assigned adhoc
      doTest(PSAssignmentTypeEnum.NONE, COMM_CIDS[1], TEST_WF_ID,
         COMMTEST_STATE_ID, ADHOC_USER_ANON, roles, COMM_IDS[1]);

      // Item in EI, user in EI role, not logged into EI community
      roles.add(TestRole.CI_MEMBERS.name());
      doTest(PSAssignmentTypeEnum.READER, COMM_CIDS[1], TEST_WF_ID,
         COMMTEST_STATE_ID, ADHOC_USER_NORMAL, roles, COMM_IDS[2]);

      // Item in EI, User not in that community
      roles.clear();
      roles.add(TestRole.CI_MEMBERS.name());
      doTest(PSAssignmentTypeEnum.NONE, COMM_CIDS[1], TEST_WF_ID,
         COMMTEST_STATE_ID, ADHOC_USER_NORMAL, roles, COMM_IDS[2]);

      // Item in CI, user in that role/comm
      doTest(PSAssignmentTypeEnum.ASSIGNEE, COMM_CIDS[2], TEST_WF_ID,
         COMMTEST_STATE_ID, ADHOC_USER_ANON, roles, COMM_IDS[2]);

      // Item in CI, user in that role, not logged into that community
      roles.add(TestRole.CI_ADMIN_MEMBERS.name());
      doTest(PSAssignmentTypeEnum.READER, COMM_CIDS[2], TEST_WF_ID,
         COMMTEST_STATE_ID, ADHOC_USER_ANON, roles, COMM_IDS[3]);

      // Item in CI, user not in that role/comm
      roles.clear();
      roles.add(TestRole.CI_ADMIN_MEMBERS.name());
      doTest(PSAssignmentTypeEnum.NONE, COMM_CIDS[2], TEST_WF_ID,
         COMMTEST_STATE_ID, ADHOC_USER_ANON, roles, COMM_IDS[3]);

      // Item in CI_Admin, user in that role, not logged into that community
      roles.add(TestRole.EI_MEMBERS.name());
      doTest(PSAssignmentTypeEnum.READER, COMM_CIDS[3], TEST_WF_ID,
         COMMTEST_STATE_ID, ADHOC_USER_ANON, roles, COMM_IDS[1]);

      // item in CI_Admin, user not in that role/comm
      roles.clear();
      roles.add(TestRole.EI_MEMBERS.name());
      doTest(PSAssignmentTypeEnum.NONE, COMM_CIDS[3], TEST_WF_ID,
         COMMTEST_STATE_ID, ADHOC_USER_ANON, roles, COMM_IDS[1]);
   }
   
   /**
    * Tests the various filter methods
    * 
    * @throws Exception if there are any errors.
    */
   @Test
   public void testFilterRoles() throws Exception
   {
      List<TestRole> stateRoles = new ArrayList<TestRole>();
      List <TestRole> filteredStateRoles = new ArrayList<TestRole>();
      List<TestRole> commonFilteredStateRoles = new ArrayList<TestRole>();
      
      // test item in draft state w/ no comm roles
      filteredStateRoles.add(TestRole.EDITOR);
      filteredStateRoles.add(TestRole.QA);
      filteredStateRoles.add(TestRole.ADMIN);
      filteredStateRoles.add(TestRole.AUTHOR);
      stateRoles.addAll(filteredStateRoles);
      
      doFilterTest(stateRoles, filteredStateRoles, ADHOC_CIDS[0], 1001);
      
      // test item in comm state
      filteredStateRoles.clear();
      stateRoles.clear();
      stateRoles.add(TestRole.EDITOR);
      stateRoles.add(TestRole.AUTHOR);
      stateRoles.add(TestRole.QA);
      stateRoles.add(TestRole.EI_MEMBERS);
      stateRoles.add(TestRole.EI_ADMIN_MEMBERS);
      stateRoles.add(TestRole.CI_MEMBERS);
      stateRoles.add(TestRole.CI_ADMIN_MEMBERS);
      
      commonFilteredStateRoles.add(TestRole.EDITOR);
      commonFilteredStateRoles.add(TestRole.AUTHOR);
      commonFilteredStateRoles.add(TestRole.QA);
      
      filteredStateRoles.addAll(commonFilteredStateRoles);
      filteredStateRoles.add(TestRole.EI_ADMIN_MEMBERS);
      doFilterTest(stateRoles, filteredStateRoles, COMM_CIDS[0], COMM_IDS[0]);
      
      filteredStateRoles.clear();
      filteredStateRoles.addAll(commonFilteredStateRoles);
      filteredStateRoles.add(TestRole.EI_MEMBERS);
      doFilterTest(stateRoles, filteredStateRoles, COMM_CIDS[1], COMM_IDS[1]);
      
      filteredStateRoles.clear();
      filteredStateRoles.addAll(commonFilteredStateRoles);
      filteredStateRoles.add(TestRole.CI_MEMBERS);
      doFilterTest(stateRoles, filteredStateRoles, COMM_CIDS[2], COMM_IDS[2]);
      
      filteredStateRoles.clear();
      filteredStateRoles.addAll(commonFilteredStateRoles);
      filteredStateRoles.add(TestRole.CI_ADMIN_MEMBERS);
      doFilterTest(stateRoles, filteredStateRoles, COMM_CIDS[3], COMM_IDS[3]);
   }
   
   /**
    * Tests the {@link PSBackendRoleInfo} inner class of the 
    * {@link PSAssignmentTypeHelper} class.
    * 
    * @throws Exception If the test fails.
    */
   @Test
   public void testBackEndRoleInfo() throws Exception
   {
      PSBackendRoleInfo roleInfo = new PSBackendRoleInfo();
      List<IPSGuid> commIds = new ArrayList<IPSGuid>();
      List<String> roleNames = new ArrayList<String>();
      
      // test empty
      doRoleInfoTest(roleInfo, roleNames, commIds);
      
      roleNames.add("Admin");
      roleNames.add("Editor");
      doRoleInfoTest(roleInfo, roleNames, commIds);
      
      // ensure cache is ok
      doRoleInfoTest(roleInfo, roleNames, commIds);
      
      // now add and test mix of cached and new
      roleNames.add(1, "Author");
      doRoleInfoTest(roleInfo, roleNames, commIds);
      
      // do the same for community roles
      roleNames.add(1, "EI_Admin_Members");
      commIds.add(ms_gmgr.makeGuid(1001, PSTypeEnum.COMMUNITY_DEF));
      roleNames.add("EI_Members");
      commIds.add(ms_gmgr.makeGuid(1002, PSTypeEnum.COMMUNITY_DEF));
      doRoleInfoTest(roleInfo, roleNames, commIds);
      
      roleNames.add("CI_Members");
      commIds.add(ms_gmgr.makeGuid(1003, PSTypeEnum.COMMUNITY_DEF));
      doRoleInfoTest(roleInfo, roleNames, commIds);
      // ensure cache is ok
      doRoleInfoTest(roleInfo, roleNames, commIds);
      
      // now test with everything coming from the cache
      roleNames.clear();
      roleNames.add("EI_Admin_Members");
      roleNames.add("EI_Members");
      roleNames.add("CI_Members");
      doRoleInfoTest(roleInfo, roleNames, commIds);
   }

   /**
    * Test that the supplied {@link PSBackendRoleInfo} instance can translate
    * the supplied <code>roleNames</code> to role ids and back as well as load
    * community relationships.
    * 
    * @param roleInfo The role info class to use
    * @param roleNames The list of names to check, assumed not 
    * <code>null</code>, may be empty.
    * @param commIds The list of communities that the roles should be associated
    * with, assumed not <code>null</code>, may be empty.
    */
   private void doRoleInfoTest(PSBackendRoleInfo roleInfo,
      List<String> roleNames, List<IPSGuid> commIds)
   {
      Set<Long> roleIds = roleInfo.getBackendRoleIds(roleNames);
      assertEquals(roleNames.size(), roleIds.size());
      assertTrue(CollectionUtils.isEqualCollection(roleNames, 
         roleInfo.getBackendRoleNames(roleIds)));
      List<IPSGuid> roleGuids = new ArrayList<IPSGuid>();
      for (Long roleId : roleIds)
      {
         roleGuids.add(ms_gmgr.makeGuid(roleId, PSTypeEnum.ROLE));
      }
      
      if (!roleGuids.isEmpty())
      {
         List<IPSGuid> assocCommIds = new ArrayList<IPSGuid>();
         for (PSCommunityRoleAssociation cra : 
            roleInfo.getCommunityRoleAssociations(roleGuids))
         {
            assocCommIds.add(ms_gmgr.makeGuid(cra.getCommunityId(), 
               PSTypeEnum.COMMUNITY_DEF));
         }
         assertTrue(CollectionUtils.isEqualCollection(commIds, assocCommIds));
      }
   }

   /**
    * Test various ways of community filtering of state roles
    * 
    * @param stateRoles List of state role names to filter, assumed not
    * <code>null</code>.  The list is not modified by this method.
    * @param filteredStateRoles Expected result of filtering role name list,
    * assumed not <code>null</code>. 
    * @param contentId The content ID to use.
    * @param commId The community ID of the item specified by the supplied
    * content ID.
    * 
    * @throws Exception If there are any errors or failures.
    */
   private void doFilterTest(List<TestRole> stateRoles, 
         List<TestRole> filteredStateRoles,
         int contentId, int commId) throws Exception
   {
      List<Integer> stateRoleIds = new ArrayList<Integer>();
      List<String> stateRoleNames = new ArrayList<String>();
      List<Integer> filteredStateRoleIds = new ArrayList<Integer>();
      List<String> filteredStateRoleNames = new ArrayList<String>();
      
      for (TestRole role : stateRoles)
      {
         stateRoleIds.add(role.getRoleId());
         stateRoleNames.add(role.name());
      }
      
      for (TestRole role : filteredStateRoles)
      {
         filteredStateRoleIds.add(role.getRoleId());
         filteredStateRoleNames.add(role.name());
      }
      
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid contentGuid = mgr.makeGuid(new PSLocator(contentId));
      updateComponentSummary(contentId, TEST_WF_ID, COMMTEST_STATE_ID);
      List<Integer> testRoleIds = new ArrayList<Integer>(stateRoleIds);
      List<String> testRoles = new ArrayList<String>(stateRoleNames);
      
      PSAssignmentTypeHelper.filterAssignedRolesByCommunity(contentId, 
         testRoleIds);
      assertTrue(CollectionUtils.isEqualCollection(filteredStateRoleIds, 
         testRoleIds));
      
      PSAssignmentTypeHelper.filterAssignedRolesByCommunity(contentGuid, 
         testRoles);
      assertTrue(CollectionUtils.isEqualCollection(filteredStateRoleNames, 
         testRoles));
      
      testRoles = new ArrayList<String>(stateRoleNames);
      PSAssignmentTypeHelper.filterAssignedRolesByCommunity(commId, TEST_WF_ID, 
         testRoles);
      assertTrue(CollectionUtils.isEqualCollection(filteredStateRoleNames, 
         testRoles));
   }

}
