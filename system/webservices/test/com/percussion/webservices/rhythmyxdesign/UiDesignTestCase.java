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

package com.percussion.webservices.rhythmyxdesign;

import com.percussion.cms.objectstore.PSMenuContext;
import com.percussion.cms.objectstore.PSMenuMode;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.PSTestUtils;
import com.percussion.webservices.PSUiTestBase;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSErrorsFaultServiceCall;
import com.percussion.webservices.faults.PSErrorsFaultServiceCallError;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.ui.data.ActionType;
import com.percussion.webservices.ui.data.NodeType;
import com.percussion.webservices.ui.data.PSAction;
import com.percussion.webservices.ui.data.PSActionChildrenChildAction;
import com.percussion.webservices.ui.data.PSActionUsageUsed;
import com.percussion.webservices.ui.data.PSActionVisibilitiesContext;
import com.percussion.webservices.ui.data.PSDisplayFormat;
import com.percussion.webservices.ui.data.PSHierarchyNode;
import com.percussion.webservices.ui.data.PSSearchDef;
import com.percussion.webservices.ui.data.PSViewDef;
import com.percussion.webservices.uidesign.CreateActionsRequest;
import com.percussion.webservices.uidesign.CreateActionsRequestType;
import com.percussion.webservices.uidesign.CreateHierarchyNodesRequest;
import com.percussion.webservices.uidesign.CreateHierarchyNodesRequestType;
import com.percussion.webservices.uidesign.CreateSearchesRequest;
import com.percussion.webservices.uidesign.CreateSearchesRequestType;
import com.percussion.webservices.uidesign.DeleteActionsRequest;
import com.percussion.webservices.uidesign.DeleteDisplayFormatsRequest;
import com.percussion.webservices.uidesign.DeleteHierarchyNodesRequest;
import com.percussion.webservices.uidesign.DeleteSearchesRequest;
import com.percussion.webservices.uidesign.DeleteViewsRequest;
import com.percussion.webservices.uidesign.FindActionsRequest;
import com.percussion.webservices.uidesign.FindDisplayFormatsRequest;
import com.percussion.webservices.uidesign.FindHierarchyNodesRequest;
import com.percussion.webservices.uidesign.FindSearchesRequest;
import com.percussion.webservices.uidesign.FindViewsRequest;
import com.percussion.webservices.uidesign.GetChildrenRequest;
import com.percussion.webservices.uidesign.LoadActionsRequest;
import com.percussion.webservices.uidesign.LoadDisplayFormatsRequest;
import com.percussion.webservices.uidesign.LoadHierarchyNodesRequest;
import com.percussion.webservices.uidesign.LoadSearchesRequest;
import com.percussion.webservices.uidesign.LoadViewsRequest;
import com.percussion.webservices.uidesign.MoveChildrenRequest;
import com.percussion.webservices.uidesign.RemoveChildrenRequest;
import com.percussion.webservices.uidesign.SaveActionsRequest;
import com.percussion.webservices.uidesign.SaveDisplayFormatsRequest;
import com.percussion.webservices.uidesign.SaveHierarchyNodesRequest;
import com.percussion.webservices.uidesign.SaveSearchesRequest;
import com.percussion.webservices.uidesign.SaveViewsRequest;
import com.percussion.webservices.uidesign.UiDesignSOAPStub;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.rmi.RemoteException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class UiDesignTestCase extends PSUiTestBase
{
   /**
    * Tests Search related operations
    * 
    * @throws Exception if any error occurs.
    */
   @Test
   public void testSearch() throws Exception
   {
      UiDesignSOAPStub binding = getDesignBinding(new Integer(600000));
      PSTestUtils.setSessionHeader(binding, m_session);

      // Test operation
      try
      {
         // cleanup searches that are left over from previous tests
         // find searches

         PSObjectSummary[] objects = findSearches("benSearch_1", binding);
         if (objects.length > 0)
         {
            PSSearchDef search = loadSearch(objects[0].getId(), binding);
            deleteSearch(search.getId(), binding);
         }

         // create an search
         CreateSearchesRequest req = new CreateSearchesRequest();
         req.setName(new String[] { "benSearch_1" });
         CreateSearchesRequestType[] types = new CreateSearchesRequestType[] { 
            CreateSearchesRequestType.fromString(
               CreateSearchesRequestType._custom) };
         req.setType(types);

         PSSearchDef[] searches = binding.createSearches(req);
         assertTrue(searches.length == 1);
         PSSearchDef search = searches[0];

         // save the search & release lock
         saveSearches(searches[0], binding, true);

         // create with a dup name
         try
         {
            binding.createSearches(req);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            assertTrue(true);
         }
         
         // load the saved search and creat lock
         search = loadSearch(searches[0].getId(), binding);
         assertTrue(search != null);

         // save search and keep the lock
         search.setDescription("Modified Description for " + search.getName());
         saveSearches(search, binding, false);

         // delete the saved search
         deleteSearch(search.getId(), binding);

         // find searches
         objects = findSearches("*search*", binding);
         assertTrue(objects.length > 0);
         updateSearches(objects, binding);
         
         // try to load searches with invalid ids
         try
         {
            long[] invalidIds = new long[objects.length];
            for (int i=0; i<objects.length; i++)
               invalidIds[i] = objects[i].getId();
            invalidIds[objects.length-1] = invalidIds[objects.length-1] + 1000;

            LoadSearchesRequest lreq = new LoadSearchesRequest();
            lreq.setId(invalidIds);
            lreq.setLock(false);
            lreq.setOverrideLock(false);
            binding.loadSearches(lreq);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, objects.length-1, 
               PSSearchDef.class.getName());
         }
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (com.percussion.webservices.faults.PSNotAuthorizedFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e3);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   /**
    * Tests View related operations
    * 
    * @throws Exception if any error occurs.
    */
   @Test
   public void testView() throws Exception
   {
      UiDesignSOAPStub binding = getDesignBinding(new Integer(600000));
      PSTestUtils.setSessionHeader(binding, m_session);

      // Test operation
      try
      {
         // cleanup views that are left over from previous tests
         // find searches

         PSObjectSummary[] objects = findViews("benView_1", binding);
         if (objects.length > 0)
         {
            PSViewDef view = loadView(objects[0].getId(), binding);
            deleteView(view.getId(), binding);
         }

         // create a view

         PSViewDef[] views = binding.createViews(new String[] { "benView_1" });
         assertTrue(views.length == 1);
         PSViewDef view = views[0];

         // save the search & release lock
         saveView(view, binding, true);

         // create with a dup name
         try
         {
            binding.createViews(new String[] { "benView_1" });
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            assertTrue(true);
         }

         // load the saved search and creat lock
         view = loadView(view.getId(), binding);
         assertTrue(view != null);

         // save view and keep the lock
         view.setDescription("Modified Description for " + view.getName());
         saveView(view, binding, false);

         // delete the saved view
         deleteView(view.getId(), binding);

         // find searches
         objects = findViews("*view*", binding);
         assertTrue(objects.length > 0);
         updateViews(objects, binding);

         // try to load searches with invalid ids
         try
         {
            long[] invalidIds = new long[objects.length];
            for (int i=0; i<objects.length; i++)
               invalidIds[i] = objects[i].getId();
            invalidIds[objects.length-1] = invalidIds[objects.length-1] + 1000;

            LoadViewsRequest lreq = new LoadViewsRequest();
            lreq.setId(invalidIds);
            lreq.setLock(false);
            lreq.setOverrideLock(false);
            binding.loadViews(lreq);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, objects.length-1, 
               PSViewDef.class.getName());
         }
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (com.percussion.webservices.faults.PSNotAuthorizedFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e3);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   private PSObjectSummary[] findViews(String name, UiDesignSOAPStub binding)
      throws Exception
   {
      FindViewsRequest freq = new FindViewsRequest();
      freq.setName(name);
      return binding.findViews(freq);
   }

   private PSViewDef loadView(long id, UiDesignSOAPStub binding)
      throws Exception
   {
      LoadViewsRequest lreq = new LoadViewsRequest();
      lreq.setId(new long[] { id });
      lreq.setLock(Boolean.TRUE);
      lreq.setOverrideLock(Boolean.TRUE);
      PSViewDef[] views = binding.loadViews(lreq);
      return views[0];
   }

   private void deleteView(long id, UiDesignSOAPStub binding) throws Exception
   {
      DeleteViewsRequest dreq = new DeleteViewsRequest();
      dreq.setId(new long[] { id });
      dreq.setIgnoreDependencies(Boolean.TRUE);
      binding.deleteViews(dreq);
   }

   private void saveView(PSViewDef view, UiDesignSOAPStub binding,
      boolean releaseLock) throws Exception
   {
      SaveViewsRequest sreq = new SaveViewsRequest();
      sreq.setPSViewDef(new PSViewDef[] { view });
      sreq.setRelease(releaseLock);
      binding.saveViews(sreq);
   }

   private void updateViews(PSObjectSummary[] objects, UiDesignSOAPStub binding)
      throws Exception
   {
      for (PSObjectSummary obj : objects)
      {
         // load and aquire lock
         PSViewDef s = loadView(obj.getId(), binding);

         // modify
         s.setDescription("Modified Description by UiDesignTestCase for "
            + s.getName());

         // save & release lock
         saveView(s, binding, true);
      }
   }

   private void updateSearches(PSObjectSummary[] objects,
      UiDesignSOAPStub binding) throws Exception
   {
      for (PSObjectSummary obj : objects)
      {
         // load and aquire lock
         PSSearchDef s = loadSearch(obj.getId(), binding);

         // modify
         s.setDescription("Modified Description by UiDesignTestCase for "
            + s.getName());

         // save & release lock
         saveSearches(s, binding, true);
      }
   }

   private void saveSearches(PSSearchDef search, UiDesignSOAPStub binding,
      boolean releaseLock) throws Exception
   {
      SaveSearchesRequest sreq = new SaveSearchesRequest();
      sreq.setPSSearchDef(new PSSearchDef[] { search });
      sreq.setRelease(releaseLock);
      binding.saveSearches(sreq);
   }

   /**
    * Tests findAction related operations
    * 
    * @throws Exception if any error occurs.
    */
   @Test
   public void testFindAction() throws Exception
   {
      UiDesignSOAPStub binding = getDesignBinding(new Integer(600000));
      PSTestUtils.setSessionHeader(binding, m_session);

      PSObjectSummary[] allActions = findActionsByType(null, null, null,
         binding);

      ActionType[] types = new ActionType[] { ActionType.cascading,
         ActionType.dynamic, ActionType.item };
      PSObjectSummary[] allActions_2 = findActionsByType(null, null, types,
         binding);

      assertTrue(allActions.length == allActions_2.length);

      PSObjectSummary[] itemActions = findActionsByType(null, null,
         new ActionType[] { ActionType.item }, binding);
      PSObjectSummary[] cascadActions = findActionsByType(null, null,
         new ActionType[] { ActionType.cascading }, binding);
      PSObjectSummary[] dynamicActions = findActionsByType(null, null,
         new ActionType[] { ActionType.dynamic }, binding);

      assertTrue(allActions.length == itemActions.length + cascadActions.length
         + dynamicActions.length);
   }

   /**
    * Tests Action related operations
    * 
    * @throws Exception if any error occurs.
    */
   @Test
   public void testAction() throws Exception
   {
      UiDesignSOAPStub binding = getDesignBinding(new Integer(600000));
      PSTestUtils.setSessionHeader(binding, m_session);

      // Test operation
      try
      {
         // cleanup actions that are left over from previous tests
         // find actions
         PSObjectSummary[] objects = findActions("benAction_*", binding);
         if (objects.length > 0)
         {
            PSAction action = loadAction(objects[0].getId(), binding);
            deleteAction(action.getId(), binding);
         }

         // create an action
         
         // create a MENU_ITEM type action
         CreateActionsRequest req = new CreateActionsRequest();
         req.setName(new String[] {"benAction_item"});
         CreateActionsRequestType[] types = new CreateActionsRequestType[] { 
            CreateActionsRequestType.item };
         req.setType(types);
         PSAction[] actions = binding.createActions(req);
         // validating
         assertTrue(actions.length == 1);
         assertTrue(actions[0].getType() == ActionType.item);
         
         // save and delete to cleanup locks
         saveAction(actions[0], binding, true);

         // create with a dup name
         try
         {
            binding.createActions(req);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            assertTrue(true);
         }
         
         // delete to cleanup locks
         deleteAction(actions[0].getId(), binding);         

         // create a CASCADING type of action
         req.setName(new String[] {"benAction_cascading"});
         types = new CreateActionsRequestType[] { 
            CreateActionsRequestType.cascading };
         req.setType(types);
         actions = binding.createActions(req);
         // validating
         assertTrue(actions.length == 1);
         assertTrue(actions[0].getType() == ActionType.cascading);
         // save and delete to cleanup locks
         saveAction(actions[0], binding, true);
         deleteAction(actions[0].getId(), binding);         

         // create an DYNAMIC type of action
         req.setName(new String[] {"benAction_dynamic"});
         types = new CreateActionsRequestType[] { 
            CreateActionsRequestType.dynamic };
         req.setType(types);
         actions = binding.createActions(req);
         // validating
         assertTrue(actions.length == 1);
         assertTrue(actions[0].getType() == ActionType.dynamic);
         // save and delete to cleanup locks
         saveAction(actions[0], binding, true);
         deleteAction(actions[0].getId(), binding);         
         
         // create an action and save it
         req.setName(new String[] { "benAction_1" });
         types = new CreateActionsRequestType[] { CreateActionsRequestType
            .fromString(CreateActionsRequestType._item) };
         req.setType(types);

         actions = binding.createActions(req);
         assertTrue(actions.length == 1);
         PSAction action = actions[0];

         // save the created (new) action & release the lock
         setChildObject(action);
         saveAction(action, binding, true);

         // load the saved action
         action = loadAction(action.getId(), binding);
         // validate the saved action
         assertTrue(action != null);
         assertTrue(action.getVisibilities().length == 2);
         assertTrue(action.getUsage().length == 2);
         assertTrue(action.getChildren().length == 3);

         // update the action
         action.setDescription("Modified Description for " + action.getName());
         saveAction(action, binding, false); // don't release the lock

         // delete the saved action
         deleteAction(action.getId(), binding);

         // find actions
         objects = findActions("Past*", binding);
         assertTrue(objects.length == 5);
         for (PSObjectSummary obj : objects)
            assertTrue(obj.getName().startsWith("Past"));
         
         // try to load actions with invalid ids
         try
         {
            long[] invalidIds = new long[objects.length];
            for (int i=0; i<objects.length; i++)
               invalidIds[i] = objects[i].getId();
            invalidIds[objects.length-1] = invalidIds[objects.length-1] + 1000;

            LoadActionsRequest lreq = new LoadActionsRequest();
            lreq.setId(invalidIds);
            lreq.setLock(false);
            lreq.setOverrideLock(false);
            binding.loadActions(lreq);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, objects.length-1, 
               PSAction.class.getName());
         }
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (com.percussion.webservices.faults.PSNotAuthorizedFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e3);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   private PSObjectSummary[] findSearches(String name, UiDesignSOAPStub binding)
      throws Exception
   {
      FindSearchesRequest freq = new FindSearchesRequest();
      freq.setName(name);
      return binding.findSearches(freq);
   }

   private PSSearchDef loadSearch(long id, UiDesignSOAPStub binding)
      throws Exception
   {
      LoadSearchesRequest lreq = new LoadSearchesRequest();
      lreq.setId(new long[] { id });
      lreq.setLock(Boolean.TRUE);
      lreq.setOverrideLock(Boolean.TRUE);
      PSSearchDef[] searches = binding.loadSearches(lreq);
      return searches[0];
   }

   private void saveAction(PSAction action, UiDesignSOAPStub binding,
      boolean releaseLock) throws Exception
   {
      SaveActionsRequest sreq = new SaveActionsRequest();
      sreq.setPSAction(new PSAction[] { action });
      sreq.setRelease(releaseLock);
      binding.saveActions(sreq);
   }

   private void deleteSearch(long id, UiDesignSOAPStub binding)
      throws Exception
   {
      DeleteSearchesRequest dreq = new DeleteSearchesRequest();
      dreq.setId(new long[] { id });
      dreq.setIgnoreDependencies(Boolean.TRUE);
      binding.deleteSearches(dreq);
   }

   /**
    * Find actions with supplied name, which may contains wildcard on either
    * end.
    * 
    * @param name the action name.
    * @param binding the object used to send the request, assumed not
    * <code>null</code>.
    * 
    * @return the actions with the supplied name, never <code>null</code>,
    * but may be empty.
    * 
    * @throws Exception if error occurs.
    */
   private PSObjectSummary[] findActions(String name, UiDesignSOAPStub binding)
      throws Exception
   {
      return findActionsByType(name, null, null, binding);
   }

   private PSObjectSummary[] findActionsByType(String name, String label,
      ActionType[] types, UiDesignSOAPStub binding) throws Exception
   {
      FindActionsRequest freq = new FindActionsRequest();
      freq.setName(name);
      freq.setLabel(label);
      freq.setType(types);
      return binding.findActions(freq);
   }

   private void setChildObject(PSAction action)
   {
      PSActionChildrenChildAction[] children = {
         new PSActionChildrenChildAction("Paste_As_Link", getActionId(2)),
         new PSActionChildrenChildAction("Paste_As_New_Copy", getActionId(17)),
         new PSActionChildrenChildAction("Paste_As_Link_To_Slot",
            getActionId(204)) };
      PSActionUsageUsed[] usage = new PSActionUsageUsed[] {
         new PSActionUsageUsed("CXNAV", PSMenuMode.getGuidFromId(3).getValue(),
            "SystemSite", PSMenuContext.getGuidFromId(15).getValue()),
         new PSActionUsageUsed("CXMAIN",
            PSMenuMode.getGuidFromId(4).getValue(), "SystemSite", PSMenuContext
               .getGuidFromId(15).getValue()) };

      PSActionVisibilitiesContext[] visCtx = new PSActionVisibilitiesContext[] {
         new PSActionVisibilitiesContext("name_1", "value_1"),
         new PSActionVisibilitiesContext("name_2", "value_2") };
      // set child objects
      action.setVisibilities(visCtx);
      action.setUsage(usage);
      action.setChildren(children);
   }

   /**
    * Loads an action with the given id.
    * 
    * @param id the id of the action.
    * @param binding used to send request, assumed not <code>null</code>.
    * @return the loaded action.
    * @throws Exception if an error occurs.
    */
   private PSAction loadAction(long id, UiDesignSOAPStub binding)
      throws Exception
   {
      LoadActionsRequest lreq = new LoadActionsRequest();
      lreq.setId(new long[] { id });
      lreq.setLock(Boolean.TRUE);
      lreq.setOverrideLock(Boolean.TRUE);
      PSAction[] actions = binding.loadActions(lreq);
      if (actions.length > 0)
         return actions[0];
      else
         return null;
   }

   private void deleteAction(long id, UiDesignSOAPStub binding)
      throws Exception
   {
      DeleteActionsRequest dreq = new DeleteActionsRequest();
      dreq.setId(new long[] { id });
      dreq.setIgnoreDependencies(Boolean.TRUE);
      binding.deleteActions(dreq);
   }

   /**
    * Gets an action id that contains the action type and the given UUID itself.
    * 
    * @param id the id without id.
    * @return the id with action type.
    */
   private long getActionId(int id)
   {
      return (new PSDesignGuid(new PSGuid(PSTypeEnum.ACTION, id))).getValue();
   }

   /**
    * Tests Action related operations
    * 
    * @throws Exception if any error occurs.
    */
   public void testDisplayFormat() throws Exception
   {
      UiDesignSOAPStub binding = getDesignBinding(new Integer(600000));
      PSTestUtils.setSessionHeader(binding, m_session);

      // Test operation
      try
      {
         // cleanup previously saved displayformats
         PSObjectSummary[] summs = findDisplayFormats("testDspFormat_*",
            binding);
         if (summs.length > 0)
         {
            long[] ids = new long[summs.length];
            for (int i = 0; i < summs.length; i++)
               ids[i] = summs[i].getId();
            loadDisplayFormats(ids, binding);
            deleteDisplayFormats(ids, binding);
         }

         // create a display format
         String[] dfNames = new String[] { 
            "testDspFormat_1", "testDspFormat_2" };
         PSDisplayFormat[] dspFormats = binding.createDisplayFormats(dfNames);
         assertTrue(dspFormats.length == 2);

         for (PSDisplayFormat df : dspFormats)
         {
            assertTrue(df.getColumns().length > 0); // sys_title column
            assertTrue(df.getProperties().length > 0); // sys_community = -1
         }

         long[] ids = new long[] { dspFormats[0].getId(), 
            dspFormats[1].getId() };

         // save the display formats & release lock
         saveDisplayFormats(dspFormats, binding, true);
         
         // create with a dup names
         try
         {
            binding.createDisplayFormats(dfNames);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            assertTrue(true);
         }
         
         // try to load display formats with invalid ids
         try
         {
            long[] invalidIds = new long[ids.length];
            for (int i=0; i<ids.length; i++)
               invalidIds[i] = ids[i];
            invalidIds[ids.length-1] = invalidIds[ids.length-1] + 1000;

            loadDisplayFormats(invalidIds, binding);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, ids.length-1, 
               PSDisplayFormat.class.getName());
         }

         // find the saved display formats
         summs = findDisplayFormats("testDspFormat_*", binding);
         assertTrue(summs.length == 2);

         // load the saved display formats
         dspFormats = loadDisplayFormats(ids, binding);
         assertTrue(dspFormats.length == 2);

         // update the display formats
         dspFormats[0].setDescription("Modified Description for "
            + dspFormats[0].getName());
         dspFormats[1].setDescription("Modified Description for "
            + dspFormats[1].getName());
         saveDisplayFormats(dspFormats, binding, false);

         // delete the saved action
         deleteDisplayFormats(ids, binding);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (com.percussion.webservices.faults.PSNotAuthorizedFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e3);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   private void saveDisplayFormats(PSDisplayFormat[] dspFormats,
      UiDesignSOAPStub binding, boolean releaseLock) throws Exception
   {
      SaveDisplayFormatsRequest sreq = new SaveDisplayFormatsRequest();
      sreq.setPSDisplayFormat(dspFormats);
      sreq.setRelease(releaseLock);
      binding.saveDisplayFormats(sreq);
   }

   private PSObjectSummary[] findDisplayFormats(String name,
      UiDesignSOAPStub binding) throws Exception
   {
      FindDisplayFormatsRequest freq = new FindDisplayFormatsRequest();
      freq.setName(name);
      freq.setLabel(name);

      PSObjectSummary[] summs = binding.findDisplayFormats(freq);
      return summs;
   }

   private PSDisplayFormat[] loadDisplayFormats(long[] ids,
      UiDesignSOAPStub binding) throws Exception
   {
      LoadDisplayFormatsRequest lreq = new LoadDisplayFormatsRequest();
      lreq.setId(ids);
      lreq.setLock(Boolean.TRUE);
      lreq.setOverrideLock(Boolean.TRUE);
      return binding.loadDisplayFormats(lreq);
   }

   private void deleteDisplayFormats(long[] ids, UiDesignSOAPStub binding)
      throws Exception
   {
      DeleteDisplayFormatsRequest dreq = new DeleteDisplayFormatsRequest();
      dreq.setId(ids);
      dreq.setIgnoreDependencies(Boolean.TRUE);
      binding.deleteDisplayFormats(dreq);
   }

   public void testCreateHierarchyNodes() throws Exception
   {
      UiDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         CreateHierarchyNodesRequest request = null;

         // try to get create a node without rhythmyx session
         try
         {
            request = new CreateHierarchyNodesRequest();
            request.setName(new String[] { "chart" });
            request
               .setType(new CreateHierarchyNodesRequestType[] { CreateHierarchyNodesRequestType.folder });
            request.setParentId(new long[] { 0 });
            binding.createHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to create a node with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new CreateHierarchyNodesRequest();
            request.setName(new String[] { "chart" });
            request
               .setType(new CreateHierarchyNodesRequestType[] { CreateHierarchyNodesRequestType.folder });
            request.setParentId(new long[] { 0 });
            binding.createHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to create a node with a null name
         try
         {
            request = new CreateHierarchyNodesRequest();
            request.setName(new String[] { null });
            request
               .setType(new CreateHierarchyNodesRequestType[] { CreateHierarchyNodesRequestType.folder });
            request.setParentId(new long[] { 0 });
            binding.createHierarchyNodes(request);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to create a node with an empty name
         try
         {
            request = new CreateHierarchyNodesRequest();
            request.setName(new String[] { " " });
            request
               .setType(new CreateHierarchyNodesRequestType[] { CreateHierarchyNodesRequestType.folder });
            request.setParentId(new long[] { 0 });
            binding.createHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to create a node with a null type
         try
         {
            request = new CreateHierarchyNodesRequest();
            request.setName(new String[] { "chart" });
            request.setType(new CreateHierarchyNodesRequestType[] { null });
            request.setParentId(new long[] { 0 });
            binding.createHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to create a node with an invalid parent
         try
         {
            request = new CreateHierarchyNodesRequest();
            request.setName(new String[] { "chart" });
            request
               .setType(new CreateHierarchyNodesRequestType[] { CreateHierarchyNodesRequestType.folder });
            request.setParentId(new long[] { new PSGuid(PSTypeEnum.SLOT, 1001)
               .longValue() });
            binding.createHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // create nouns node "chart"
         request = new CreateHierarchyNodesRequest();
         request.setName(new String[] { "chart" });
         request
            .setType(new CreateHierarchyNodesRequestType[] { CreateHierarchyNodesRequestType.folder });
         request.setParentId(new long[] { m_nouns.getId() });
         PSHierarchyNode[] nodes = binding.createHierarchyNodes(request);

         assertTrue(nodes[0] != null);
         SaveHierarchyNodesRequest saveRequest = new SaveHierarchyNodesRequest();
         saveRequest.setPSHierarchyNode(nodes);
         binding.saveHierarchyNodes(saveRequest);

         // try to create a second nouns node "chart"
         try
         {
            request = new CreateHierarchyNodesRequest();
            request.setName(new String[] { "CHart" });
            request
               .setType(new CreateHierarchyNodesRequestType[] { CreateHierarchyNodesRequestType.folder });
            request.setParentId(new long[] { m_nouns.getId() });
            binding.createHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }
      }
      catch (PSInvalidSessionFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
      catch (PSContractViolationFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e);
      }
   }

   public void testFindHierarchyNodes() throws Exception
   {
      UiDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         FindHierarchyNodesRequest request = null;
         PSObjectSummary[] nodes = null;

         // try to find all nodes without rhythmyx session
         try
         {
            request = new FindHierarchyNodesRequest();
            request.setPath(null);
            binding.findHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to find all nodes with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new FindHierarchyNodesRequest();
            request.setPath(null);
            binding.findHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         int count = m_nonTestHierarchyNodesCount + m_testHierarchyNodesCount;

         // find all nodes
         request = new FindHierarchyNodesRequest();
         request.setPath(null);
         nodes = binding.findHierarchyNodes(request);
         assertTrue(nodes != null && nodes.length == count);

         request = new FindHierarchyNodesRequest();
         request.setPath(" ");
         nodes = binding.findHierarchyNodes(request);
         assertTrue(nodes != null && nodes.length == count);

         request = new FindHierarchyNodesRequest();
         request.setPath("*");
         nodes = binding.findHierarchyNodes(request);
         assertTrue(nodes != null && nodes.length == count);

         // try to find a non-existing node
         request = new FindHierarchyNodesRequest();
         request.setPath("somenode");
         nodes = binding.findHierarchyNodes(request);
         assertTrue(nodes != null && nodes.length == 0);

         // find unlocked test nodes
         request = new FindHierarchyNodesRequest();
         request.setPath("Words");
         nodes = binding.findHierarchyNodes(request);
         assertTrue(nodes != null && nodes.length == 1);
         assertTrue(nodes[0].getLocked() == null);

         request = new FindHierarchyNodesRequest();
         request.setPath("WORDS");
         nodes = binding.findHierarchyNodes(request);
         assertTrue(nodes != null && nodes.length == 1);

         request = new FindHierarchyNodesRequest();
         request.setPath("Words/*");
         nodes = binding.findHierarchyNodes(request);
         assertTrue(nodes != null && nodes.length == 6);

         request = new FindHierarchyNodesRequest();
         request.setPath("Words/*");
         request.setType(NodeType.folder);
         nodes = binding.findHierarchyNodes(request);
         assertTrue(nodes != null && nodes.length == 2);

         request = new FindHierarchyNodesRequest();
         request.setPath("Words/*");
         request.setType(NodeType.placeholder);
         nodes = binding.findHierarchyNodes(request);
         assertTrue(nodes != null && nodes.length == 4);

         request = new FindHierarchyNodesRequest();
         request.setPath("Words/Nouns");
         nodes = binding.findHierarchyNodes(request);
         assertTrue(nodes != null && nodes.length == 1);

         request = new FindHierarchyNodesRequest();
         request.setPath("Words/Nouns/*");
         nodes = binding.findHierarchyNodes(request);
         assertTrue(nodes != null && nodes.length == 2);

         request = new FindHierarchyNodesRequest();
         request.setPath("Words/*/b*");
         nodes = binding.findHierarchyNodes(request);
         assertTrue(nodes != null && nodes.length == 2);

         request = new FindHierarchyNodesRequest();
         request.setPath("*ture");
         nodes = binding.findHierarchyNodes(request);
         assertTrue(nodes != null && nodes.length == 0);

         // lock test nodes
         long[] lockIds = getHierachyNodeIds();
         lockHierarchyNodes(lockIds, session);

         // find locked test nodes
         request = new FindHierarchyNodesRequest();
         request.setPath("Words");
         nodes = binding.findHierarchyNodes(request);
         assertTrue(nodes != null && nodes.length == 1);
         assertTrue(nodes[0].getLocked() != null);

         // release locked objects
         PSTestUtils.releaseLocks(session, lockIds);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   public void testLoadHierarchyNodes() throws Exception
   {
      UiDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         long[] ids = getHierachyNodeIds();

         LoadHierarchyNodesRequest request = null;
         PSHierarchyNode[] nodes = null;

         // try to load nodes without rhythmyx session
         try
         {
            request = new LoadHierarchyNodesRequest();
            request.setId(ids);
            binding.loadHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load nodes with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new LoadHierarchyNodesRequest();
            request.setId(ids);
            binding.loadHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to load nodes with null ids
         try
         {
            request = new LoadHierarchyNodesRequest();
            request.setId(null);
            binding.loadHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load nodes with empty ids
         try
         {
            request = new LoadHierarchyNodesRequest();
            request.setId(new long[0]);
            binding.loadHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to load slots with invalid ids
         try
         {
            long[] invalidIds = new long[ids.length];
            for (int i=0; i<ids.length; i++)
               invalidIds[i] = ids[i];
            invalidIds[2] = invalidIds[2] + 1000;

            request = new LoadHierarchyNodesRequest();
            request.setId(invalidIds);
            binding.loadHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, 2, PSHierarchyNode.class.getName());
         }

         // load nodes read-only
         request = new LoadHierarchyNodesRequest();
         request.setId(ids);
         nodes = binding.loadHierarchyNodes(request);
         assertTrue(nodes != null && nodes.length == 7);

         // load nodes read-writable
         request = new LoadHierarchyNodesRequest();
         request.setId(ids);
         request.setLock(true);
         nodes = binding.loadHierarchyNodes(request);
         assertTrue(nodes != null && nodes.length == 7);

         // load locked nodes read-writable with locking session
         request = new LoadHierarchyNodesRequest();
         request.setId(ids);
         request.setLock(true);
         nodes = binding.loadHierarchyNodes(request);
         assertTrue(nodes != null && nodes.length == 7);

         // try to load locked nodes read-writable with new session
         String session2 = PSTestUtils.login("admin2", "demo");
         PSTestUtils.setSessionHeader(binding, session2);
         try
         {
            request = new LoadHierarchyNodesRequest();
            request.setId(ids);
            request.setLock(true);
            binding.loadHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorResultsFault e)
         {
            verifyErrorResultsFault(e, -1, PSHierarchyNode.class.getName());
         }

         // release locked objects
         PSTestUtils.releaseLocks(session, ids);
      }
      catch (com.percussion.webservices.faults.PSErrorResultsFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorResultsFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e2);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (com.percussion.webservices.faults.PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   public void testSaveHierarchyNodes() throws Exception
   {
      UiDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         PSHierarchyNode[] nodes = getHierarchyNodes();

         SaveHierarchyNodesRequest request = null;

         // try to save nodes without rhythmyx session
         try
         {
            request = new SaveHierarchyNodesRequest();
            request.setPSHierarchyNode(nodes);
            binding.saveHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to save nodes with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new SaveHierarchyNodesRequest();
            request.setPSHierarchyNode(nodes);
            binding.saveHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to save nodes with null nodes
         try
         {
            request = new SaveHierarchyNodesRequest();
            request.setPSHierarchyNode(null);
            binding.saveHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to save nodes with empty nodes
         try
         {
            request = new SaveHierarchyNodesRequest();
            request.setPSHierarchyNode(new PSHierarchyNode[0]);
            binding.saveHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to save nodes in read-only mode
         try
         {
            request = new SaveHierarchyNodesRequest();
            request.setPSHierarchyNode(nodes);
            binding.saveHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorsFault e)
         {
            // expected exception
            PSErrorsFaultServiceCall[] calls = e.getServiceCall();
            for (int i = 0; i < calls.length; i++)
            {
               PSErrorsFaultServiceCall call = calls[i];

               assertTrue(call.getSuccess() == null);
               assertTrue(call.getError() != null);
            }
         }

         // lock nodes
         long[] lockIds = getHierachyNodeIds();
         lockHierarchyNodes(lockIds, session);

         // save locked nodes, do not release
         request = new SaveHierarchyNodesRequest();
         request.setPSHierarchyNode(nodes);
         request.setRelease(false);
         binding.saveHierarchyNodes(request);

         // save locked nodes and release
         request = new SaveHierarchyNodesRequest();
         request.setPSHierarchyNode(nodes);
         request.setRelease(true);
         binding.saveHierarchyNodes(request);

         // try to save nodes in read-only mode
         try
         {
            request = new SaveHierarchyNodesRequest();
            request.setPSHierarchyNode(nodes);
            binding.saveHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorsFault e)
         {
            // expected exception
            PSErrorsFaultServiceCall[] calls = e.getServiceCall();
            for (int i = 0; i < calls.length; i++)
            {
               PSErrorsFaultServiceCall call = calls[i];

               assertTrue(call.getSuccess() == null);
               assertTrue(call.getError() != null);
            }
         }
      }
      catch (com.percussion.webservices.faults.PSErrorResultsFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorResultsFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e2);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (com.percussion.webservices.faults.PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   public void testDeleteHierarchyNodes() throws Exception
   {
      UiDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         long[] ids = getHierachyNodeIds();

         DeleteHierarchyNodesRequest request = null;

         // try to delete nodes without rhythmyx session
         try
         {
            request = new DeleteHierarchyNodesRequest();
            request.setId(ids);
            binding.deleteHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to delete nodes with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new DeleteHierarchyNodesRequest();
            request.setId(ids);
            binding.deleteHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to delete nodes with null ids
         try
         {
            request = new DeleteHierarchyNodesRequest();
            request.setId(null);
            binding.deleteHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to delete nodes with empty ids
         try
         {
            request = new DeleteHierarchyNodesRequest();
            request.setId(new long[0]);
            binding.deleteHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // lock objects for admin2
         String session2 = PSTestUtils.login("admin2", "demo");
         lockHierarchyNodes(ids, session2);

         // try to delete objects locked by somebody else
         try
         {
            request = new DeleteHierarchyNodesRequest();
            request.setId(ids);
            binding.deleteHierarchyNodes(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSErrorsFault e)
         {
            // expected exception
            PSErrorsFaultServiceCall[] calls = e.getServiceCall();
            assertTrue(calls != null && calls.length == 7);
            for (PSErrorsFaultServiceCall call : calls)
            {
               PSErrorsFaultServiceCallError error = call.getError();
               assertTrue(error != null);
            }
         }

         // release locked objects
         PSTestUtils.releaseLocks(session2, ids);

         // delete locked nodes
         request = new DeleteHierarchyNodesRequest();
         request.setId(ids);
         binding.deleteHierarchyNodes(request);
         m_words = null;
         m_verbs = null;
         m_do = null;
         m_be = null;
         m_nouns = null;
         m_book = null;
         m_picture = null;

         // delete non-existing nodes
         request = new DeleteHierarchyNodesRequest();
         request.setId(ids);
         binding.deleteHierarchyNodes(request);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSErrorsFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ErrorsFault Exception caught: " + e2);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e3)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e3);
      }
      catch (com.percussion.webservices.faults.PSNotAuthorizedFault e4)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e4);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   public void testGetChildren() throws Exception
   {
      UiDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         GetChildrenRequest request = null;

         // try to get child nodes without rhythmyx session
         try
         {
            request = new GetChildrenRequest();
            request.setId(m_words.getId());
            binding.getChildren(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to get child nodes with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new GetChildrenRequest();
            request.setId(m_words.getId());
            binding.getChildren(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to get child nodes with parent id = 0
         request = new GetChildrenRequest();
         long[] children = binding.getChildren(request);
         assertTrue(children.length > 0);

         // get child nodes of non existing parent
         request = new GetChildrenRequest();
         request.setId(m_words.getId() + 100);
         long[] childIds = binding.getChildren(request);
         assertTrue(childIds.length == 0);

         // get all root nodes
         request = new GetChildrenRequest();
         request.setId(0);
         childIds = binding.getChildren(request);
         assertTrue(childIds.length > 0);
         boolean exists = false;
         for (long childId : childIds)
         {
            if (childId == m_words.getId())
            {
               exists = true;
               break;
            }
         }
         assertTrue(exists);

         // get "Words" child nodes
         request = new GetChildrenRequest();
         request.setId(m_words.getId());
         childIds = binding.getChildren(request);
         assertTrue(childIds.length == 2);
         assertTrue(childIds[0] == m_nouns.getId());
         assertTrue(childIds[1] == m_verbs.getId());

         // get "Verbs" child nodes
         request = new GetChildrenRequest();
         request.setId(m_verbs.getId());
         childIds = binding.getChildren(request);
         assertTrue(childIds.length == 2);
         assertTrue(childIds[0] == m_be.getId());
         assertTrue(childIds[1] == m_do.getId());

         // get "be" child nodes
         request = new GetChildrenRequest();
         request.setId(m_be.getId());
         childIds = binding.getChildren(request);
         assertTrue(childIds.length == 0);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   public void testMoveChildren() throws Exception
   {
      UiDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         MoveChildrenRequest request = null;

         // try to move child nodes without rhythmyx session
         try
         {
            request = new MoveChildrenRequest();
            request.setSourceId(m_verbs.getId());
            request.setTargetId(m_nouns.getId());
            long[] ids = new long[1];
            ids[0] = m_be.getId();
            request.setId(ids);
            binding.moveChildren(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to move child nodes with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new MoveChildrenRequest();
            request.setSourceId(m_verbs.getId());
            request.setTargetId(m_nouns.getId());
            long[] ids = new long[1];
            ids[0] = m_be.getId();
            request.setId(ids);
            binding.moveChildren(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to move child nodes with null source
         try
         {
            request = new MoveChildrenRequest();
            request.setTargetId(m_nouns.getId());
            long[] ids = new long[1];
            ids[0] = m_be.getId();
            request.setId(ids);
            binding.moveChildren(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to move child nodes with null target
         try
         {
            request = new MoveChildrenRequest();
            request.setSourceId(m_verbs.getId());
            long[] ids = new long[1];
            ids[0] = m_be.getId();
            request.setId(ids);
            binding.moveChildren(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to move child nodes with null ids
         try
         {
            request = new MoveChildrenRequest();
            request.setSourceId(m_verbs.getId());
            request.setTargetId(m_nouns.getId());
            binding.moveChildren(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to move child nodes with empty ids
         try
         {
            request = new MoveChildrenRequest();
            request.setSourceId(m_verbs.getId());
            request.setTargetId(m_nouns.getId());
            long[] ids = new long[0];
            request.setId(ids);
            binding.moveChildren(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // move child node "be" from "verbs" to "nouns"
         request = new MoveChildrenRequest();
         request.setSourceId(m_verbs.getId());
         request.setTargetId(m_nouns.getId());
         long[] ids = new long[1];
         ids[0] = m_be.getId();
         request.setId(ids);
         binding.moveChildren(request);
         // make sure the right children were moved
         GetChildrenRequest getChildrenRequest = new GetChildrenRequest();
         getChildrenRequest.setId(m_verbs.getId());
         long[] childIds = binding.getChildren(getChildrenRequest);
         assertTrue(childIds.length == 1);
         getChildrenRequest.setId(m_nouns.getId());
         childIds = binding.getChildren(getChildrenRequest);
         assertTrue(childIds.length == 3);

         // move child nodes "be" and "picture" from "nouns" to "verbs"
         request = new MoveChildrenRequest();
         request.setSourceId(m_nouns.getId());
         request.setTargetId(m_verbs.getId());
         ids = new long[2];
         ids[0] = m_be.getId();
         ids[1] = m_picture.getId();
         request.setId(ids);
         binding.moveChildren(request);
         // make sure the right children were moved
         getChildrenRequest = new GetChildrenRequest();
         getChildrenRequest.setId(m_verbs.getId());
         childIds = binding.getChildren(getChildrenRequest);
         assertTrue(childIds.length == 3);
         getChildrenRequest.setId(m_nouns.getId());
         childIds = binding.getChildren(getChildrenRequest);
         assertTrue(childIds.length == 1);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   public void testRemoveChildren() throws Exception
   {
      UiDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         RemoveChildrenRequest request = null;

         // try to remove child nodes without rhythmyx session
         try
         {
            request = new RemoveChildrenRequest();
            request.setParentId(m_verbs.getId());
            long[] ids = new long[1];
            ids[0] = m_be.getId();
            request.setId(ids);
            binding.removeChildren(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to remove child nodes with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new RemoveChildrenRequest();
            request.setParentId(m_verbs.getId());
            long[] ids = new long[1];
            ids[0] = m_be.getId();
            request.setId(ids);
            binding.removeChildren(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to remove child nodes with null parent
         try
         {
            request = new RemoveChildrenRequest();
            long[] ids = new long[1];
            ids[0] = m_be.getId();
            request.setId(ids);
            binding.removeChildren(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to remove child nodes with null ids
         try
         {
            request = new RemoveChildrenRequest();
            request.setParentId(m_verbs.getId());
            binding.removeChildren(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to remove child nodes with empty ids
         try
         {
            request = new RemoveChildrenRequest();
            request.setParentId(m_verbs.getId());
            long[] ids = new long[0];
            request.setId(ids);
            binding.removeChildren(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // remove child node "be" from "verbs"
         request = new RemoveChildrenRequest();
         request.setParentId(m_verbs.getId());
         long[] ids = new long[1];
         ids[0] = m_be.getId();
         request.setId(ids);
         binding.removeChildren(request);
         // make sure the right children were removed
         GetChildrenRequest getChildrenRequest = new GetChildrenRequest();
         getChildrenRequest.setId(m_verbs.getId());
         long[] childIds = binding.getChildren(getChildrenRequest);
         assertTrue(childIds.length == 1);

         // remove child nodes "book" and "picture" from "nouns"
         request = new RemoveChildrenRequest();
         request.setParentId(m_nouns.getId());
         ids = new long[2];
         ids[0] = m_book.getId();
         ids[1] = m_picture.getId();
         request.setId(ids);
         binding.removeChildren(request);
         // make sure the right children were removed
         getChildrenRequest = new GetChildrenRequest();
         getChildrenRequest.setId(m_nouns.getId());
         childIds = binding.getChildren(getChildrenRequest);
         assertTrue(childIds.length == 0);

         // remove child nodes "nouns" and "verbs" from "words"
         request = new RemoveChildrenRequest();
         request.setParentId(m_words.getId());
         ids = new long[2];
         ids[0] = m_nouns.getId();
         ids[1] = m_verbs.getId();
         request.setId(ids);
         binding.removeChildren(request);
         // make sure the right children were removed
         getChildrenRequest = new GetChildrenRequest();
         getChildrenRequest.setId(m_words.getId());
         childIds = binding.getChildren(getChildrenRequest);
         assertTrue(childIds.length == 0);
         // make sure the children were removed recursiv
         FindHierarchyNodesRequest findRequest = new FindHierarchyNodesRequest();
         findRequest.setPath("*");
         PSObjectSummary[] summaries = binding.findHierarchyNodes(findRequest);
         assertTrue(summaries.length > 0);
         boolean exists = false;
         for (PSObjectSummary summary : summaries)
         {
            if (summary.getId() == m_words.getId())
            {
               exists = true;
               break;
            }
         }
         assertTrue(exists);
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   public void testPathsToIds() throws Exception
   {
      UiDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         String[] request = null;

         // try to convert paths to ids without rhythmyx session
         try
         {
            request = new String[1];
            request[0] = "/Words/Verbs/be";
            binding.pathsToIds(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to paths to ids with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new String[1];
            request[0] = "/Words/Verbs/be";
            binding.pathsToIds(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to convert paths to ids with null paths
         try
         {
            binding.pathsToIds(null);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to convert paths to ids with empty paths
         try
         {
            binding.pathsToIds(new String[0]);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // convert paths to ids
         request = new String[3];
         request[0] = "/Words/Nouns/book";
         request[1] = "/Words/Verbs";
         request[2] = "/Words";
         long[][] idsArray = binding.pathsToIds(request);
         assertTrue(idsArray.length == 3);
         assertTrue(idsArray[0].length == 3);
         assertTrue(idsArray[0][0] == m_words.getId());
         assertTrue(idsArray[0][1] == m_nouns.getId());
         assertTrue(idsArray[0][2] == m_book.getId());
         assertTrue(idsArray[1].length == 2);
         assertTrue(idsArray[1][0] == m_words.getId());
         assertTrue(idsArray[1][1] == m_verbs.getId());
         assertTrue(idsArray[2].length == 1);
         assertTrue(idsArray[1][0] == m_words.getId());

         // convert paths to ids
         request = new String[3];
         request[0] = "Words/Nouns/book";
         request[1] = "/Words/Verbs/";
         request[2] = "Words/";
         idsArray = binding.pathsToIds(request);
         assertTrue(idsArray.length == 3);
         assertTrue(idsArray[0].length == 3);
         assertTrue(idsArray[0][0] == m_words.getId());
         assertTrue(idsArray[0][1] == m_nouns.getId());
         assertTrue(idsArray[0][2] == m_book.getId());
         assertTrue(idsArray[1].length == 2);
         assertTrue(idsArray[1][0] == m_words.getId());
         assertTrue(idsArray[1][1] == m_verbs.getId());
         assertTrue(idsArray[2].length == 1);
         assertTrue(idsArray[1][0] == m_words.getId());

         // try to convert paths to ids an invalid path
         try
         {
            request = new String[3];
            request[0] = "/Words/Nouns/book";
            request[1] = "/Words/foo";
            request[2] = "/Words";
            idsArray = binding.pathsToIds(request);
         }
         catch (RemoteException e)
         {
            // expected exception
            assertTrue(true);
         }
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
   }

   public void testIdsToPaths() throws Exception
   {
      UiDesignSOAPStub binding = getDesignBinding(null);

      // Test operation
      try
      {
         String session = m_session;

         long[] request = null;

         // try to convert ids to paths without rhythmyx session
         try
         {
            request = new long[1];
            request[0] = m_words.getId();
            binding.idsToPaths(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to ids to paths with an invalid rhythmyx session
         PSTestUtils.setSessionHeader(binding, "foobar");
         try
         {
            request = new long[1];
            request[0] = m_words.getId();
            binding.idsToPaths(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected exception
            assertTrue(true);
         }

         PSTestUtils.setSessionHeader(binding, session);

         // try to convert ids to paths with null ids
         try
         {
            binding.idsToPaths(null);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to convert ids to path with empty ids
         try
         {
            request = new long[0];
            binding.idsToPaths(request);
            assertFalse("Should have thrown exception", true);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // convert ids to path
         request = new long[3];
         request[0] = m_words.getId();
         request[1] = m_verbs.getId();
         request[2] = m_book.getId();
         String[] paths = binding.idsToPaths(request);
         assertTrue(paths.length == 3);
         assertTrue(paths[0].equals("/Words"));
         assertTrue(paths[1].equals("/Words/Verbs"));
         assertTrue(paths[2].equals("/Words/Nouns/book"));

         // try to convert ids to path an invalid id
         try
         {
            request = new long[3];
            request[0] = m_words.getId();
            request[1] = m_verbs.getId() + 100;
            request[2] = m_book.getId();
            binding.idsToPaths(request);
         }
         catch (PSErrorResultsFault e)
         {
            // expected exception
            assertTrue(true);
         }
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new junit.framework.AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (com.percussion.webservices.faults.PSContractViolationFault e2)
      {
         throw new junit.framework.AssertionFailedError(
            "ContractViolationFault Exception caught: " + e2);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError(
            "NotAuthorizedFault Exception caught: " + e);
      }
   }
}
