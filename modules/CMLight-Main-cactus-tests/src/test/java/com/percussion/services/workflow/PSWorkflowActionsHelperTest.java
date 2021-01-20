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

import com.percussion.cms.objectstore.PSDateValue;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSRequest;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * Test case for the {@link PSWorkflowActionsHelper} class, requires FF and
 * sample content be installed. Does not test adhoc actions or multi-approval
 * transition as none are configured by default and there is no programatic
 * manipulation possible.
 */
@Category(IntegrationTest.class)
public class PSWorkflowActionsHelperTest extends ServletTestCase
{
   /**
    * Test using the helper to get the appropriate actions.
    * 
    * @throws Exception if the test fails.
    */
   public void testActions() throws Exception
   {
      // login as author1 and create an item
      IPSSecurityWs secWs = PSSecurityWsLocator.getSecurityWebservice();
      secWs.login(request, response, "author1", "demo", null, 
         "Enterprise_Investments", null);  
      
      PSServerItem item1 = createItem("1");
      int contentid1 = item1.getContentId();

      
      
      // check no actions on a public item
      List<IPSGuid> contentIds = new ArrayList<IPSGuid>();
      List<PSAssignmentTypeEnum> assnTypes = new ArrayList<PSAssignmentTypeEnum>();
      List<String> roleList = new ArrayList<String>();

      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
      contentIds.add(guidMgr.makeGuid(new PSLocator(contentid1, 1)));
      
      // test no actions as reader
      assnTypes.add(PSAssignmentTypeEnum.READER);
      roleList.add("Editor");
      PSWorkflowActionsHelper helper = new PSWorkflowActionsHelper(contentIds, 
         assnTypes, "editor1", roleList, "en-us");
      assertTrue(helper.getAllWorkflowActions().isEmpty());

      // test no actions as none
      assnTypes.clear();
      roleList.clear();
      assnTypes.add(PSAssignmentTypeEnum.NONE);
      roleList.add("QA");
      helper = new PSWorkflowActionsHelper(contentIds, 
         assnTypes, "qa1", roleList, "en-us");
      assertTrue(helper.getAllWorkflowActions().isEmpty());
      
      // test as author, actions should be checkin, approve, direct to public
      assnTypes.clear();
      roleList.clear();
      assnTypes.add(PSAssignmentTypeEnum.ASSIGNEE);
      roleList.add("Author");
      helper = new PSWorkflowActionsHelper(contentIds, assnTypes, "author1", 
         roleList, "en-us");
      List<PSMenuAction> actions = helper.getCIAOActions();
      assertEquals(actions.size(), 1);
      assertTrue(actions.get(0).getName().equals(
         PSMenuAction.CHECKIN_ACTION_NAME));
      actions = helper.getTranstionActions();
      assertEquals(actions.size(), 2);
      Set<String> expectedActions = new HashSet<String>();
      expectedActions.add("DirecttoPublic");
      expectedActions.add("Approve");
      checkActions(expectedActions, actions);
      
      // test as editor, since item is checked out by author, actions should
      // be empty
      assnTypes.clear();
      roleList.clear();
      assnTypes.add(PSAssignmentTypeEnum.ASSIGNEE);
      roleList.add("Editor");
      helper = new PSWorkflowActionsHelper(contentIds, assnTypes, "editor1", 
         roleList, "en-us");
      assertTrue(helper.getAllWorkflowActions().isEmpty());
                  
      // ensure admin has force-checkin, approve, d2p
      assnTypes.clear();
      roleList.clear();
      assnTypes.add(PSAssignmentTypeEnum.ADMIN);
      roleList.add("Admin");
      helper = new PSWorkflowActionsHelper(contentIds, assnTypes, "admin1", 
         roleList, "en-us");
      actions = helper.getCIAOActions();
      assertEquals(actions.size(), 1);
      assertTrue(actions.get(0).getName().equals(
         PSMenuAction.FORCE_CHECKIN_ACTION_NAME));
      actions = helper.getTranstionActions();
      assertEquals(actions.size(), 2);
      expectedActions = new HashSet<String>();
      expectedActions.add("DirecttoPublic");
      expectedActions.add("Approve");
      checkActions(expectedActions, actions);
      
      // checkin, actions should be checkout, approve, d2p
      IPSContentWs contentWs = PSContentWsLocator.getContentWebservice();
      contentWs.checkinItems(contentIds, "");
      assnTypes.clear();
      roleList.clear();
      assnTypes.add(PSAssignmentTypeEnum.ASSIGNEE);
      roleList.add("Author");
      helper = new PSWorkflowActionsHelper(contentIds, assnTypes, "author1", 
         roleList, "en-us");      
      actions = helper.getCIAOActions();
      assertEquals(actions.size(), 1);
      assertTrue(actions.get(0).getName().equals(
         PSMenuAction.CHECKOUT_ACTION_NAME));
      actions = helper.getTranstionActions();
      assertEquals(actions.size(), 2);
      expectedActions = new HashSet<String>();
      expectedActions.add("DirecttoPublic");
      expectedActions.add("Approve");
      checkActions(expectedActions, actions);
      
      // check as reader, still no actions
      assnTypes.clear();
      roleList.clear();      
      assnTypes.add(PSAssignmentTypeEnum.READER);
      roleList.add("Editor");
      helper = new PSWorkflowActionsHelper(contentIds, 
         assnTypes, "editor1", roleList, "en-us");
      assertTrue(helper.getAllWorkflowActions().isEmpty());
      
      // check intersection of checkout actions on multiple items
      PSServerItem item2 = createItem("2");
      int contentid2 = item2.getContentId();
      contentIds.add(guidMgr.makeGuid(new PSLocator(contentid2, 1)));

      // test two with one in common
      assnTypes.clear();
      roleList.clear();
      assnTypes.add(PSAssignmentTypeEnum.ASSIGNEE);
      assnTypes.add(PSAssignmentTypeEnum.ASSIGNEE);
      roleList.add("Author");
      helper = new PSWorkflowActionsHelper(contentIds, assnTypes, "author1", 
         roleList, "en-us");      
      actions = helper.getCIAOActions();
      assertEquals(actions.size(), 0);
      actions = helper.getTranstionActions();
      assertEquals(actions.size(), 2);
      expectedActions = new HashSet<String>();
      expectedActions.add("DirecttoPublic");
      expectedActions.add("Approve");
      checkActions(expectedActions, actions);      
      
      // test two that are the same
      contentWs.checkinItems(contentIds, "");
      helper = new PSWorkflowActionsHelper(contentIds, assnTypes, "author1", 
         roleList, "en-us");      
      actions = helper.getCIAOActions();
      assertEquals(actions.size(), 1);
      assertTrue(actions.get(0).getName().equals(
         PSMenuAction.CHECKOUT_ACTION_NAME));
      actions = helper.getTranstionActions();
      assertEquals(actions.size(), 2);
      expectedActions = new HashSet<String>();
      expectedActions.add("DirecttoPublic");
      expectedActions.add("Approve");
      checkActions(expectedActions, actions);
      
      // test two that are different
      IPSSystemWs systemWs = PSSystemWsLocator.getSystemWebservice();
      systemWs.transitionItems(Collections.singletonList(contentIds.get(1)), 
         "DirecttoPublic");
      helper = new PSWorkflowActionsHelper(contentIds, assnTypes, "author1", 
         roleList, "en-us");
      assertTrue(helper.getAllWorkflowActions().isEmpty());
      
      // check intersection of transition actions on multiple items
      
      
   }

   /**
    * Test that the actions and expected actions match
    * 
    * @param expectedActions A set of the expected action trigger names, assumed
    * not <code>null</code> or empty
    * @param actions The list of actions to check, assumed not 
    * <code>null</code>.
    * 
    * @throws Exception if the check fails
    */
   private void checkActions(Set<String> expectedActions, 
      List<PSMenuAction> actions) throws Exception
   {
      Set<String> testActions = new HashSet<String>();
      for (PSMenuAction action : actions)
      {
         testActions.add(action.getParameters().getParameter(
            PSWorkflowActionsHelper.getTriggerParamName()));
      }
      
      assertEquals(testActions, expectedActions);
   }

   /**
    * Create a brief item
    * 
    * @param identifier Appended onto the system title of the item name for
    * debugging purposes, assumed not <code>null</code> or empty. 
    * 
    * @return the item, never <code>null</code>.
    * 
    * @throws Exception if there are any errors.
    */
   private PSServerItem createItem(String identifier) throws Exception
   {
      PSRequest psReq = (PSRequest) PSRequestInfo.getRequestInfo(
         PSRequestInfo.KEY_PSREQUEST);
      PSSecurityToken tok = psReq.getSecurityToken();
      
      // create the item
      PSItemDefinition itemDef = PSItemDefManager.getInstance().getItemDef(
         "rffBrief", tok);
      PSServerItem item = new PSServerItem(itemDef, null, tok);
      Iterator<PSItemField> fields = item.getAllFields();
      while (fields.hasNext())
      {
         PSItemField field = fields.next();
         String name = field.getName();
         if (name.equals("sys_title") || name.equals("displaytitle") || 
            name.equals("callout"))
         {
            field.addValue(new PSTextValue("testBrief" + identifier));
         }
         else if (name.equals("sys_contentstartdate"))
            field.addValue(new PSDateValue(new Date()));
      }
      
      item.save(tok);
      return item;
   }
}

