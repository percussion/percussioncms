/******************************************************************************
 *
 * [ PSAssemblyActionsTest.java ]
 * 
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.content.ui.aa.actions.test;

import com.percussion.content.ui.aa.actions.IPSAAClientAction;
import com.percussion.content.ui.aa.actions.PSAAClientActionFactory;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import org.json.JSONObject;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests for various actions that call the assembly service
 * to assemble content.
 */
@Category(IntegrationTest.class)
public class PSAssemblyActionsTest
         extends
            PSAAClientActionTestBase
{

   public void testGetAllowedSnippetTemplates() throws Exception
   {
      login("admin1", "demo");
      PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
      IPSAAClientAction action = factory.getAction("GetAllowedSnippetTemplates");
      
      Map<String, Object> params = new HashMap<String, Object>();
      params.put(IPSAAClientAction.OBJECT_ID_PARAM, 
               ms_testJsonArrays.get("Snippet"));
      PSActionResponse response = action.execute(params);
      JSONObject obj = new JSONObject(response.getResponseData());
      String content = obj.getString("templateHtml");
      TestCase.assertTrue(content.indexOf("aaSnippet") != -1);
   }
   
   public void testGetSnippetContent() throws Exception
   {
      login("admin1", "demo");
      PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
      IPSAAClientAction action = factory.getAction("GetSnippetContent");
      
      Map<String, Object> params = new HashMap<String, Object>();
      params.put(IPSAAClientAction.OBJECT_ID_PARAM, 
               ms_testJsonArrays.get("Snippet"));
      PSActionResponse response = action.execute(params);
      String content = response.getResponseData();
      TestCase.assertTrue(content.indexOf(
         "10 Mistakes You Can\'t Afford") != -1);
   }
   
   public void testGetSlotContent() throws Exception
   {
      login("admin1", "demo");
      PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
      IPSAAClientAction action = factory.getAction("GetSlotContent");
      
      Map<String, Object> params = new HashMap<String, Object>();
      params.put(IPSAAClientAction.OBJECT_ID_PARAM, 
               ms_testJsonArrays.get("Slot"));
      params.put("isaamode", "true");
      PSActionResponse response = action.execute(params);
      String content = response.getResponseData();
      TestCase.assertTrue(content.indexOf("<div class=\"RelatedContent\">") != -1);
   }
   
   public void testGetFieldContent() throws Exception
   {
      login("admin1", "demo");
      PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
      IPSAAClientAction action = factory.getAction("GetFieldContent");
      
      Map<String, Object> params = new HashMap<String, Object>();
      params.put(IPSAAClientAction.OBJECT_ID_PARAM, 
               ms_testJsonArrays.get("Field"));
      PSActionResponse response = action.execute(params);
      String content = response.getResponseData();
      TestCase.assertTrue(content.indexOf(
         "10 Mistakes You Can\'t Afford") != -1);
   }
}
