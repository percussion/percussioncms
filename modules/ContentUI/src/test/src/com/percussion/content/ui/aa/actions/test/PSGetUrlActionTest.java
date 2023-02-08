/******************************************************************************
 *
 * [ PSGetUrlActionTest.java ]
 * 
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.content.ui.aa.actions.test;

import com.percussion.content.ui.aa.actions.IPSAAClientAction;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSAAClientActionFactory;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.utils.testing.IntegrationTest;
import org.json.JSONObject;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Map;

/**
 * @author erikserating
 *
 */
@Category(IntegrationTest.class)
public class PSGetUrlActionTest extends PSAAClientActionTestBase
{
   public void testGetUrl() throws Exception
   {
      login("admin1", "demo");
      PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
      IPSAAClientAction action = factory.getAction("GetUrl");
      doTestErrors(action);
      doTestUrlTypes(action);
      
   }
   
   private void doTestErrors(IPSAAClientAction action) throws Exception
   {
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
      
      // Test actionname not supplied
      try
      {
         params.clear();         
         params.put(IPSAAClientAction.OBJECT_ID_PARAM, 
            ms_testJsonArrays.get("Page"));
         action.execute(params);
         fail();
      }
      catch(PSAAClientActionException e)
      {
         String msg = e.getLocalizedMessage();
         assertEquals(msg, "Missing required actionname parameter.");
      }
   }
   
   public void doTestUrlTypes(IPSAAClientAction action) throws Exception
   {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put(IPSAAClientAction.OBJECT_ID_PARAM, 
               ms_testJsonArrays.get("Page"));
      
      // Test TYPE_CE_EDIT
      params.put("actionname", PSGetUrlAction.TYPE_CE_EDIT);
      PSActionResponse aresponse = action.execute(params);
      JSONObject obj = new JSONObject(aresponse.getResponseData());
      String url = obj.getString("url");
      System.out.println(url);
      assertTrue(url.indexOf("/Rhythmyx/psx_cerffGeneric/rffGeneric.html?sys_command=edit&sys_view=sys_All&refreshHint=Selected&sys_contentid=335&sys_revision=")>=0);      
      
       // Test TYPE_CE_VIEW_AUDIT_TRAIL
      params.put("actionname", PSGetUrlAction.TYPE_CE_VIEW_AUDIT_TRAIL);
      aresponse = action.execute(params);
      obj = new JSONObject(aresponse.getResponseData());
      url = obj.getString("url");
      System.out.println(url);
      assertTrue(url.indexOf("/Rhythmyx/psx_cerffGeneric/rffGeneric.html?sys_command=preview&sys_userview=sys_audittrail&sys_contentid=335&sys_revision=")>=0);
      
      // Test TYPE_CE_VIEW_CONTENT
      params.put("actionname", PSGetUrlAction.TYPE_CE_VIEW_CONTENT);
      aresponse = action.execute(params);
      obj = new JSONObject(aresponse.getResponseData());
      url = obj.getString("url");
      System.out.println(url);
      assertTrue(url.indexOf("/Rhythmyx/psx_cerffGeneric/rffGeneric.html?sys_command=preview&sys_view=sys_All&sys_contentid=335&sys_revision=")>=0);
      
      // Test TYPE_CE_VIEW_PROPERTIES
      params.put("actionname", PSGetUrlAction.TYPE_CE_VIEW_PROPERTIES);
      aresponse = action.execute(params);
      obj = new JSONObject(aresponse.getResponseData());
      url = obj.getString("url");
      System.out.println(url);
      assertTrue(url.indexOf("/Rhythmyx/psx_cerffGeneric/rffGeneric.html?sys_command=preview&sys_view=sys_ItemMeta&sys_contentid=335&sys_revision=")>=0);
      
      // Test TYPE_CE_VIEW_REVISIONS
      params.put("actionname", PSGetUrlAction.TYPE_CE_VIEW_REVISIONS);
      aresponse = action.execute(params);
      obj = new JSONObject(aresponse.getResponseData());
      url = obj.getString("url");
      System.out.println(url);
      assertTrue(url.indexOf("/Rhythmyx/psx_cerffGeneric/rffGeneric.html?sys_command=preview&sys_userview=sys_Revisions&sys_contentid=335&sys_revision=")>=0);
      
      // Test TYPE_PREVIEW_MYPAGE
      params.put("actionname", PSGetUrlAction.TYPE_PREVIEW_MYPAGE);
      aresponse = action.execute(params);
      obj = new JSONObject(aresponse.getResponseData());
      url = obj.getString("url");
      System.out.println(url);
      assertTrue(url.indexOf("/Rhythmyx/assembler/render?sys_contentid=335&sys_revision=-1&sys_variantid=500&sys_authtype=0&sys_context=0&sys_folderid=306&sys_siteid=301")>=0);
      
      // Test TYPE_PREVIEW_PAGE
      params.put("actionname", PSGetUrlAction.TYPE_PREVIEW_PAGE);
      aresponse = action.execute(params);
      
      // Test TYPE_CE_FIELDEDIT
      params.put(IPSAAClientAction.OBJECT_ID_PARAM, 
               ms_testJsonArrays.get("Field"));
      params.put("actionname", PSGetUrlAction.TYPE_CE_FIELDEDIT);
      aresponse = action.execute(params);
      obj = new JSONObject(aresponse.getResponseData());
      url = obj.getString("url");
      System.out.println(url);
      assertTrue(url.indexOf("/Rhythmyx/psx_cerffGeneric/rffGeneric.html?sys_command=edit&sys_view=sys_SingleField%3Adisplaytitle&refreshHint=Selected&sys_contentid=372&sys_revision=")>=0);
      
   }
   
   
   
   
   
   
   
   
   
   

}
