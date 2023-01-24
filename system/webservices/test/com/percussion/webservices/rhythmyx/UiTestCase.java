/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.webservices.rhythmyx;

import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.PSTestUtils;
import com.percussion.webservices.PSUiTestBase;
import com.percussion.webservices.ui.LoadActionsRequest;
import com.percussion.webservices.ui.LoadDisplayFormatsRequest;
import com.percussion.webservices.ui.LoadSearchesRequest;
import com.percussion.webservices.ui.LoadViewsRequest;
import com.percussion.webservices.ui.UiSOAPStub;
import com.percussion.webservices.ui.data.CommunityRef;
import com.percussion.webservices.ui.data.PSAction;
import com.percussion.webservices.ui.data.PSDisplayFormat;
import com.percussion.webservices.ui.data.PSSearchDef;
import com.percussion.webservices.ui.data.PSViewDef;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class UiTestCase extends PSUiTestBase
{
   /**
    * Testing loadActions method.
    * @throws Exception if an error occurs
    */
   @Test
   public void testUiSOAPLoadActions() throws Exception
   {
      UiSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);

      LoadActionsRequest req = new LoadActionsRequest();
      PSAction[] allActions = binding.loadActions(req);
      assertTrue(allActions.length > 0);

      req.setName("Past*");
      PSAction[] actions = binding.loadActions(req);
      assertTrue(actions.length > 0 && allActions.length > actions.length);
      for (PSAction action : actions)
      {
         assertTrue(action.getName().startsWith("Past"));
         assertTrue(action.getUsage().length > 0);
         if (action.getName().equals("Paste"))
            assertTrue(action.getChildren().length > 0);
      }

      req.setName("Unknown");
      PSAction[] unknown = binding.loadActions(req);
      assertTrue(unknown.length == 0);

   }

   /**
    * Testing loadActions method.
    * @throws Exception if an error occurs
    */
   @Test
   public void testUiSOAPLoadDisplayFormats() throws Exception
   {
      UiSOAPStub binding = getBinding(new Integer(600000));
      PSTestUtils.setSessionHeader(binding, m_session);

      LoadDisplayFormatsRequest req = new LoadDisplayFormatsRequest();
      PSDisplayFormat[] all = binding.loadDisplayFormats(req);
      assertTrue(all.length > 0);
      for (PSDisplayFormat df : all)
      {
         assertTrue(df.getColumns().length > 0);
         CommunityRef[] comms = df.getCommunities();
         assertTrue(comms.length > 0);
         for (CommunityRef comm : comms)
         {
            assertTrue(comm.getName().length() > 0);
         }
      }

      req.setName("Related*");
      PSDisplayFormat[] related = binding.loadDisplayFormats(req);
      assertTrue(related.length < all.length);

      req.setName("Unknown");
      PSDisplayFormat[] unknown = binding.loadDisplayFormats(req);
      assertTrue(unknown.length == 0);

   }

   /**
    * Testing both load searches and views, make sure they don't 
    * load the same server object which has the same id.
    * 
    * @throws Exception if an error occurs.
    */
   @Test
   public void testUiSOAPLoadSearchesViews() throws Exception
   {
      UiSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);

      LoadSearchesRequest sreq = new LoadSearchesRequest();
      PSSearchDef[] allSearches = binding.loadSearches(sreq);
      assertTrue(allSearches.length > 0);

      LoadViewsRequest vreq = new LoadViewsRequest();
      PSViewDef[] allViews = binding.loadViews(vreq);
      assertTrue(allViews.length > 0);

      // make sure search objects do not include any view objects
      for (PSSearchDef search : allSearches)
      {
         long id = search.getId();
         for (PSViewDef view : allViews)
            assertTrue(view.getId() != id);
      }

   }

   /**
    * Testing loadSearches method.
    * @throws Exception if an error occurs
    */
   @Test
   public void testUiSOAPLoadSearches() throws Exception
   {
      UiSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);

      LoadSearchesRequest req = new LoadSearchesRequest();
      PSSearchDef[] all = binding.loadSearches(req);
      assertTrue(all.length > 0);
      for (PSSearchDef s : all)
      {
         //assertTrue(df.getColumns().length > 0);
         CommunityRef[] comms = s.getCommunities();
         assertTrue(comms.length > 0);
         for (CommunityRef comm : comms)
         {
            assertTrue(comm.getName().length() > 0);
         }
      }

      req.setName("*search*");
      PSSearchDef[] searches = binding.loadSearches(req);
      assertTrue(searches.length > 0);
      for (PSSearchDef s : searches)
      {
         String nameLower = s.getName().toLowerCase();
         assertTrue(nameLower.indexOf("search") >= 0);
      }
   }

   /**
    * Testing loadSearches method.
    * @throws Exception if an error occurs
    */
   @Test
   public void testUiSOAPLoadViews() throws Exception
   {
      UiSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);

      LoadViewsRequest req = new LoadViewsRequest();
      PSViewDef[] all = binding.loadViews(req);
      assertTrue(all.length > 0);
      for (PSViewDef s : all)
      {
         //assertTrue(df.getColumns().length > 0);
         CommunityRef[] comms = s.getCommunities();
         assertTrue(comms.length > 0);
         for (CommunityRef comm : comms)
         {
            assertTrue(comm.getName().length() > 0);
         }
      }

      req.setName("*view*");
      PSViewDef[] views = binding.loadViews(req);
      assertTrue(views.length > 0);
      for (PSViewDef v : views)
      {
         String nameLower = v.getName().toLowerCase();
         assertTrue(nameLower.indexOf("view") >= 0);
      }
   }
}
