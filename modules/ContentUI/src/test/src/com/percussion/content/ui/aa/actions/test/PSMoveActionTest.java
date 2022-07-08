/******************************************************************************
 *
 * [ PSMoveActionTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.test;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.content.ui.aa.actions.IPSAAClientAction;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSAAClientActionFactory;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.services.assembly.impl.ModifyRelatedContentUtils;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Map;

/**
 * Test the move action methods
 */
@Category(IntegrationTest.class)
public class PSMoveActionTest extends PSAAClientActionTestBase
{
   
   
   public void testMove() throws Exception
   {
      doCheckErrors();
      login("admin1", "demo");
      doReorder();
      doUp();
      doDown();
      
   }
   
   protected void doCheckErrors()
   {
      PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
      IPSAAClientAction action = factory.getAction("Move");
      Map<String, Object> params = new HashMap<String, Object>();      
      
      // Test objectid not supplied
      try
      {
         action.execute(params);
         fail();
      }
      catch(PSAAClientActionException e)
      {
         String msg = e.getLocalizedMessage();
         assertEquals(msg, "Required objectid does not exist.");
      }     
      
      
      // Test mode not supplied
      try
      {
         params.clear();         
         params.put(IPSAAClientAction.OBJECT_ID_PARAM, TEST_OBJECTID);
         action.execute(params);
         fail();
      }
      catch(PSAAClientActionException e)
      {
         String msg = e.getLocalizedMessage();
         assertEquals(msg, "Missing required mode parameter.");
      }
      
      // Test invalid mode
      try
      {
         params.clear();
         params.put(IPSAAClientAction.OBJECT_ID_PARAM, TEST_OBJECTID);
         params.put(PARAM_MODE, "bogusMode");
         action.execute(params);
         fail();
      }
      catch(PSAAClientActionException e)
      {
         String msg = e.getLocalizedMessage();
         assertEquals(msg, "Invalid mode! Must be 'up', 'down' or 'reorder'.");
      }
      
      // Test missing index param
      try
      {
         params.clear();
         params.put(IPSAAClientAction.OBJECT_ID_PARAM, TEST_OBJECTID);
         params.put(PARAM_MODE, "reorder");         
         action.execute(params);
         fail();
      }
      catch(PSAAClientActionException e)
      {
         String msg = e.getLocalizedMessage();
         assertEquals(msg, "index parameter required when using reorder mode.");
      }
      
      // Test invalid index param
      try
      {
         params.clear();
         params.put(IPSAAClientAction.OBJECT_ID_PARAM, TEST_OBJECTID);
         params.put(PARAM_MODE, "reorder");
         params.put(PARAM_INDEX, "NONINTEGER");
         action.execute(params);
         fail();
      }
      catch(PSAAClientActionException e)
      {
         String msg = e.getLocalizedMessage();
         assertEquals(msg, "Invalid format! index must be an integer.");
      }      
   }
   
   
   
   protected void doUp() throws Exception
   {
      PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
      IPSAAClientAction action = factory.getAction("Move");
      Map<String, Object> params = new HashMap<String, Object>();
      params.put(PARAM_MODE, "up");
      params.put(IPSAAClientAction.OBJECT_ID_PARAM, TEST_OBJECTID);      
      PSActionResponse aresponse = null;
      aresponse = action.execute(params);
      assertEquals(IPSAAClientAction.SUCCESS, aresponse.getResponseData());
      PSAaRelationship r = 
         new PSAaRelationship(
                 ModifyRelatedContentUtils.getRelationship(REL_ID, getRequestContext()));
      assertEquals(r.getSortRank(), 1);       
      
   }
   
   protected void doDown() throws Exception
   {
      PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
      IPSAAClientAction action = factory.getAction("Move");
      Map<String, Object> params = new HashMap<String, Object>();
      params.put(PARAM_MODE, "down");
      params.put(IPSAAClientAction.OBJECT_ID_PARAM, TEST_OBJECTID);
      PSActionResponse aresponse = null;
      aresponse = action.execute(params);
      assertEquals(IPSAAClientAction.SUCCESS, aresponse.getResponseData());
      PSAaRelationship r = 
         new PSAaRelationship(
                 ModifyRelatedContentUtils.getRelationship(REL_ID, getRequestContext()));
      assertEquals(r.getSortRank(), 2);        
        
   }
   
   protected void doReorder() throws Exception
   {
      PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
      IPSAAClientAction action = factory.getAction("Move");
      Map<String, Object> params = new HashMap<String, Object>();
      params.put(PARAM_MODE, "reorder");
      params.put(PARAM_INDEX, "2");
      params.put(IPSAAClientAction.OBJECT_ID_PARAM, TEST_OBJECTID);
      PSActionResponse aresponse = null;
      aresponse = action.execute(params);
      assertEquals(IPSAAClientAction.SUCCESS, aresponse.getResponseData());
      PSAaRelationship r = 
         new PSAaRelationship(
                 ModifyRelatedContentUtils.getRelationship(REL_ID, getRequestContext()));
      assertEquals(r.getSortRank(), 2);    
   }
   
   private static final int REL_ID = 1732;
   private static final String TEST_OBJECTID = 
      makeJSONArrayString("1","1","1","1","1","1","1","1","1","1","1",String.valueOf(REL_ID),"1");
   private static final String PARAM_MODE = "mode";
   private static final String PARAM_INDEX = "index";
}
