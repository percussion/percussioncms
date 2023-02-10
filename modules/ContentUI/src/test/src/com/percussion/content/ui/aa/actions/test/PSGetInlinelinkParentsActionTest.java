/******************************************************************************
 *
 * [ PSGetInlinelinkParentsActionTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.test;

import com.percussion.content.ui.aa.actions.IPSAAClientAction;
import com.percussion.content.ui.aa.actions.PSAAClientActionFactory;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Map;

/**
 * This is used for testing GetInlinelinkParentsAction
 */
@Category(IntegrationTest.class)
public class PSGetInlinelinkParentsActionTest extends PSAAClientActionTestBase
{
   /**
    * Tests the GetInlinelinkParents action
    * 
    * @throws Exception if any error occurs.
    */
   public void testGetInlinelinkParents() throws Exception
   {
      login("admin1", "demo");
      
      // positive test the actions

      // make sure the item is in check out state
      int DEPENDENT_ID = 460;
      String MGR_IDS = "[339,344,690,692]";
      
      PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
      IPSAAClientAction action = factory.getAction("GetInlinelinkParents");
      Map<String, Object> params = new HashMap<String, Object>();
      
      params.put(PSGetInlinelinkParentsAction.DEPENDENT_ID, String.valueOf(DEPENDENT_ID));
      params.put(PSGetInlinelinkParentsAction.MANAGED_IDS, String.valueOf(MGR_IDS));
         
      PSActionResponse resp = action.execute(params);
      TestCase.assertTrue(resp.getResponseData().length() > 0);
      JSONArray jarray = new JSONArray(resp.getResponseData());
      TestCase.assertTrue(jarray.length() == 2);
      
      // negative test - the above parent/owner ids do not exist in the 
      // managed ids, MGR_IDS_2
      String MGR_IDS_2 = "[200,300]";
      params.put(PSGetInlinelinkParentsAction.DEPENDENT_ID, String.valueOf(DEPENDENT_ID));
      params.put(PSGetInlinelinkParentsAction.MANAGED_IDS, String.valueOf(MGR_IDS_2));
      resp = action.execute(params);
      TestCase.assertTrue(resp.getResponseData().length() > 0);
      jarray = new JSONArray(resp.getResponseData());
      TestCase.assertTrue(jarray.length() == 0);

      // negative test - there is no parent/owner ids for the specified dependent
      params.put(PSGetInlinelinkParentsAction.DEPENDENT_ID, String.valueOf(1));
      params.put(PSGetInlinelinkParentsAction.MANAGED_IDS, String.valueOf(MGR_IDS));
      resp = action.execute(params);
      TestCase.assertTrue(resp.getResponseData().length() > 0);
      jarray = new JSONArray(resp.getResponseData());
      TestCase.assertTrue(jarray.length() == 0);
   }
}
