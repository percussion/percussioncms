/******************************************************************************
 *
 * [ PSAddRemoveSnippetActionTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions.test;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.IPSAAClientAction;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSAAClientActionFactory;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test the add and remove snippet actions
 */
@Category(IntegrationTest.class)
public class PSAddRemoveSnippetActionTest extends PSAAClientActionTestBase
{

   /**
    * Tests both add and remove snippet actions.
    * 
    * @throws Exception if any unexpected error occurs.
    */
   public void testAddRemove() throws Exception
   {
      login("admin1", "demo");
      
      // positive test the actions

      // make sure the item is in check out state
      int CONTENT_ID = 335;
      int SLOT_ID = 518;
      List<PSItemStatus> status = prepareItem(CONTENT_ID);
      
      List<PSAaRelationship> rels = getRelationships(CONTENT_ID, SLOT_ID);
      assertTrue(rels.size() > 0);
      
      PSAaRelationship rel = rels.get(rels.size()-1);
      removeRelationship(rel);
      
      List<PSAaRelationship> rels2 = getRelationships(CONTENT_ID, SLOT_ID);
      assertTrue(rels2.size() == (rels.size() -1));
      
      addRelationship(rel);
      rels2 = getRelationships(CONTENT_ID, SLOT_ID);
      assertTrue(rels2.size() == rels.size());
      
      releaseItem(status);
      
      // negative test the actions
      removeRelationshipWithError(rel);
      addRelationshipWithError(rel);
   }
   
   /**
    * Make sure the given item is checked out.
    * 
    * @param contentId the item in question.
    * 
    * @return the status used for check in later, never <code>null</code>.
    * 
    * @throws Exception if any error occurs.
    */
   private List<PSItemStatus> prepareItem(int contentId) throws Exception
   {
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid id = mgr.makeGuid(contentId, PSTypeEnum.LEGACY_CONTENT);
      IPSContentWs cservice = PSContentWsLocator.getContentWebservice();
      List<PSItemStatus> status = cservice.prepareForEdit(
         Collections.singletonList(id));
      
      return status;
   }

   /**
    * Reverse the prepare action.
    * 
    * @param status the to be reversed state.
    * 
    * @throws Exception if any error occurs.
    */
   private void releaseItem(List<PSItemStatus> status) throws Exception
   {
      IPSContentWs cservice = PSContentWsLocator.getContentWebservice();
      cservice.releaseFromEdit(status, false);
   }
   
   /**
    * Tests remove snippet action.
    * 
    * @param rel the to be removed snippet (or relationship), assumed not 
    *    <code>null</code>.
    *    
    * @throws Exception if any error occurs.
    */
   private void removeRelationship(PSAaRelationship rel) throws Exception
   {
      PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
      IPSAAClientAction action = factory.getAction("RemoveSnippet");
      Map<String, Object> params = new HashMap<String, Object>();
      params.put(PSRemoveSnippetAction.RELATIONSHIP_IDS, 
         String.valueOf(rel.getId()));
      
      PSActionResponse aresponse = null;
      aresponse = action.execute(params);
      assertEquals(IPSAAClientAction.SUCCESS, aresponse.getResponseData());
   }

   /**
    * Negative test remove snippet action.
    */
   private void removeRelationshipWithError(PSAaRelationship rel)
   {
      PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
      IPSAAClientAction action = factory.getAction("RemoveSnippet");
      Map<String, Object> params = new HashMap<String, Object>();
      params.put(PSRemoveSnippetAction.RELATIONSHIP_IDS, 
            String.valueOf(rel.getId()));
      executeWithError(action, params);
   }

   /**
    * Execute the action with exception
    * 
    * @param action the action in question.
    * @param params the incomplete parameter.
    */
   private void executeWithError(IPSAAClientAction action, 
      Map<String, Object> params)
   {
      try
      {
         action.execute(params);
         fail();
      }
      catch (PSAAClientActionException e)
      {
      }
   }

   /**
    * Tests add snippet action.
    * 
    * @param rel it contains the properties of the new snippet, assumed not 
    *    <code>null</code>.
    *    
    * @throws Exception if any error occurs.
    */
   private void addRelationship(PSAaRelationship rel) throws Exception
   {
      PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
      IPSAAClientAction action = factory.getAction("AddSnippet");
      Map<String, Object> params = new HashMap<String, Object>();
      
      params.put(PSAddSnippetAction.OWNER_ID, 
         String.valueOf(rel.getOwner().getId()));
      params.put(PSAddSnippetAction.DEPENDENT_ID, 
               String.valueOf(rel.getDependent().getId()));
      params.put(PSAddSnippetAction.SLOT_ID, 
               String.valueOf(rel.getSlotId().longValue()));
      params.put(PSAddSnippetAction.TEMPLATE_ID, 
               String.valueOf(rel.getTemplateId().longValue()));
      if (rel.getFolderId() != -1)
      {
         IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
         IPSGuid fid = mgr.makeGuid(rel.getFolderId(), PSTypeEnum.LEGACY_CONTENT);
         IPSContentWs cws = PSContentWsLocator.getContentWebservice();
         String[] res = cws.findFolderPaths(fid);
         params.put(PSAddSnippetAction.FOLDER_PATH, res[0]);
      }
      if (rel.getSiteId() != null)
      {
         IPSSiteManager mgr = PSSiteManagerLocator.getSiteManager();
         IPSSite site = mgr.loadSite(rel.getSiteId());
         params.put(PSAddSnippetAction.SITE_NAME, site.getName());
      }
         
      PSActionResponse aresponse = null;
      aresponse = action.execute(params);
      assertTrue(aresponse.getResponseData().length() > 0);
   }

   /**
    * Negative tests the add snippet action.
    * 
    * @param rel it contains the properties of the new snippet, assumed not 
    *    <code>null</code>.
    *    
    * @throws Exception if any unexpected error occurs.
    */
   private void addRelationshipWithError(PSAaRelationship rel) throws Exception
   {
      PSAAClientActionFactory factory = PSAAClientActionFactory.getInstance();
      IPSAAClientAction action = factory.getAction("AddSnippet");
      Map<String, Object> allParams = new HashMap<String, Object>();
      
      allParams.put(PSAddSnippetAction.OWNER_ID, 
         String.valueOf(rel.getOwner().getId()));
      allParams.put(PSAddSnippetAction.DEPENDENT_ID, 
               String.valueOf(rel.getDependent().getId()));
      allParams.put(PSAddSnippetAction.SLOT_ID, 
               String.valueOf(rel.getSlotId().longValue()));
      allParams.put(PSAddSnippetAction.TEMPLATE_ID, 
               String.valueOf(rel.getTemplateId().longValue()));

      // missing OWNER_ID
      Map<String, Object> params = new HashMap<String,Object>(allParams);
      params.remove(PSAddSnippetAction.OWNER_ID);

      executeWithError(action, params);

      // missing DEPENDENT_ID
      params = new HashMap<String,Object>(allParams);
      params.remove(PSAddSnippetAction.DEPENDENT_ID);

      executeWithError(action, params);

      // missing SLOT_ID
      params = new HashMap<String,Object>(allParams);
      params.remove(PSAddSnippetAction.SLOT_ID);

      executeWithError(action, params);

      // missing TEMPLATE_ID
      params = new HashMap<String,Object>(allParams);
      params.remove(PSAddSnippetAction.TEMPLATE_ID);

      executeWithError(action, params);
   }


   /**
    * Gets a list of relationships with the specified parameters.
    * 
    * @param contentId the owner id of the relationship.
    * @param slotId the slot id of the relationship property.
    * 
    * @return the requested relationships, never <code>null</code>.
    * 
    * @throws Exception if any error occurs.
    */
   private List<PSAaRelationship> getRelationships(int contentId, int slotId)
      throws Exception
   {
      PSComponentSummary summ = PSAAObjectId.getItemSummary(contentId);
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(summ.getHeadLocator());
      filter.setProperty(PSRelationshipConfig.PDU_SLOTID, String.valueOf(slotId));
      filter.setCategory(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
      
      IPSContentWs cservice = PSContentWsLocator.getContentWebservice();
      List<PSAaRelationship> rels = cservice.loadContentRelations(filter, false);
      
      return rels;
   }
   
}
