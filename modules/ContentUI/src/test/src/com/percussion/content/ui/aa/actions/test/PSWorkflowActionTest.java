/******************************************************************************
 *
 * [ PSWorkflowActionTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.test;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.IPSAAClientAction;
import com.percussion.content.ui.aa.actions.PSAAClientActionFactory;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSAdhocTypeEnum;
import com.percussion.services.workflow.data.PSAssignedRole;
import com.percussion.services.workflow.data.PSContentAdhocUser;
import com.percussion.services.workflow.data.PSState;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test workflow actions
 */
@Category(IntegrationTest.class)
public class PSWorkflowActionTest extends PSAAClientActionTestBase
{

   /**
    * Tests both add and remove snippet actions.
    *
    * @throws Exception if any unexpected error occurs.
    */
   public void testWorkflow() throws Exception
   {
      login(USER, "demo");

      // test operations without adhoc users
      performAll(null, null);
      performAll(COMMENT, null);
      
      // test operations with adhoc users if enabled
      if ( adhocEnabled(WORKFLOW_ID, PUB_STATE_ID) &&
           adhocEnabled(WORKFLOW_ID, QE_STATE_ID))
      {
         List<String> users = new ArrayList<String>();
         users.add(USER);
         users.add("admin2");
         
         performAll(null, users);
         performAll(COMMENT, users);
      }
   }

   /**
    * Determines whether the adhoc user is enabled for the specified 
    * workflow and state.
    * 
    * @param workflowId the id of the workflow in question. 
    * @param stateId the id of the state in question.
    * 
    * @return <code>true</code> if the adhoc user is enabled; otherwise
    *    <code>false</code>.
    */
   private boolean adhocEnabled(int workflowId, int stateId)
   {
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid wfId = mgr.makeGuid(workflowId, PSTypeEnum.WORKFLOW);
      IPSGuid stId = mgr.makeGuid(stateId, PSTypeEnum.WORKFLOW_STATE);
      
      IPSWorkflowService service = 
         PSWorkflowServiceLocator.getWorkflowService();
      PSState state = service.loadWorkflowState(stId, wfId);
      List<PSAssignedRole> roles = state.getAssignedRoles();
      for (PSAssignedRole role : roles)
      {
         if (role.getAdhocType() == PSAdhocTypeEnum.ANONYMOUS ||
             role.getAdhocType() == PSAdhocTypeEnum.ENABLED)
         {
            return true;
         }
      }
      return false;
   }
   
   /**
    * Performs all workflow operations with the given comment and adhoc users.
    * 
    * @param comment the comment, may be <code>null</code> or empty.
    * @param adhocUsers a list of add hoc users, may be <code>null</code> or 
    *    empty.
    *    
    * @throws Exception if an error occurs.
    */
   private void performAll(String comment, List<String>adhocUsers)
      throws Exception
   {
      PSComponentSummary summ = PSAAObjectId.getItemSummary(CONTENT_ID);
      if (summ.getContentStateId() == PUB_STATE_ID)
      {
         transitionItem(CONTENT_ID, MOVE_TO_QE, comment, adhocUsers);
         checkinoutItem(CONTENT_ID, comment, PSWorkflowAction.CHECK_OUT);
         checkinoutItem(CONTENT_ID, comment, PSWorkflowAction.CHECK_IN);
         transitionItem(CONTENT_ID, MOVE_TO_PUB, comment, adhocUsers);
      }
      else if (summ.getContentStateId() == QE_STATE_ID)
      {
         transitionItem(CONTENT_ID, MOVE_TO_PUB, comment, adhocUsers);         
         transitionItem(CONTENT_ID, MOVE_TO_QE, comment, adhocUsers);
         checkinoutItem(CONTENT_ID, comment, PSWorkflowAction.CHECK_OUT);
         checkinoutItem(CONTENT_ID, comment, PSWorkflowAction.CHECK_IN);
      }
   }

   /**
    * Performs a transition for the specified content item.
    * 
    * @param contentId the id of the content item.
    * @param trigger the trigger name of the transition, assumed not 
    *    <code>null</code> or empty.
    * @param comment the comment for this transition, may be <code>null</code> 
    *    or empty.
    * @param adhocUsers the adhoc user list, may be <code>null</code> or
    *    empty.
    * 
    * @throws Exception if any error occurs.
    */
   private void transitionItem(int contentId, String trigger, String comment, 
      List<String> adhocUsers) throws Exception
   {
      PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
      IPSAAClientAction action = factory.getAction("Workflow");
      Map<String, Object> params = new HashMap<String, Object>();

      params.put(PSWorkflowAction.OPERATION, PSWorkflowAction.TRANSITION);
      params.put(PSWorkflowAction.CONTENT_ID, String.valueOf(contentId));
      params.put(PSWorkflowAction.TRIGGER_NAME, trigger);
      params.put(PSWorkflowAction.COMMENT, comment);
      params.put(PSWorkflowAction.ADHOC_USERS, getAdHocUsers(adhocUsers));
      
      PSActionResponse aresponse = action.execute(params);
      assertEquals(IPSAAClientAction.SUCCESS, aresponse.getResponseData());
      
      // validate the adhoc users
      if (adhocUsers != null && (!adhocUsers.isEmpty()))
      {
         IPSWorkflowService service = 
            PSWorkflowServiceLocator.getWorkflowService();

         for (String user : adhocUsers)
         {
            List<PSContentAdhocUser> users = service.findAdhocInfoByUser(user);
            if (users.isEmpty())
            {
               fail();
            }
         }
      }
   }
   
   /**
    * Converts a list of string to a string with delimiter of ';'
    * 
    * @param users the to be converted list, it may be <code>null</code> or empty.
    * 
    * @return the converted string, may be <code>null</code> or empty.
    */
   private String getAdHocUsers(List<String> users)
   {
      if (users == null || users.isEmpty())
         return null;
      
      StringBuffer userList = new StringBuffer();
      for (int i=0; i<users.size(); i++)
      {
         if (i > 0)
            userList.append(";");
         userList.append(users.get(i));
      }
      return userList.toString();
   }

   /**
    * Checks in or out the specified item.
    * 
    * @param contentId the id of the item.
    * @param comment the comment for this operation, may be <code>null</code> 
    *    or empty.
    * @param op check in or out operation, assumed not <code>null</code> or 
    *    empty.
    * 
    * @throws Exception if any error accurs.
    */
   private void checkinoutItem(int contentId, String comment, String op) throws Exception
   {
      PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
      IPSAAClientAction action = factory.getAction("Workflow");
      Map<String, Object> params = new HashMap<String, Object>();

      params.put(PSWorkflowAction.OPERATION, op);
      params.put(PSWorkflowAction.CONTENT_ID, String.valueOf(contentId));
      params.put(PSWorkflowAction.COMMENT, comment);
      
      PSActionResponse aresponse = action.execute(params);
      assertEquals(IPSAAClientAction.SUCCESS, aresponse.getResponseData());
   }
   
   /**
    * private constants for the tests
    */
   private static String USER = "admin1";
   private static String COMMENT = "Testing & Comment\n Next line2 \n Next line3";
   private static int WORKFLOW_ID = 5;
   private static int CONTENT_ID = 335;
   private static int PUB_STATE_ID = 5;
   private static int QE_STATE_ID = 6;
   private static String MOVE_TO_QE = "Quick Edit";
   private static String MOVE_TO_PUB = "ReturnToPublic";
   

}
