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
package com.percussion.webservices.content;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.design.objectstore.PSView;
import com.percussion.design.objectstore.PSViewSet;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.data.PSContentTypeWorkflow;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.services.content.data.PSItemStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.collections.IteratorUtils;
import org.junit.experimental.categories.Category;

/**
 * This class is used for testing the methods that are not exposed through web
 * services.
 * 
 * @author bjoginipally
 * 
 */
@Category(IntegrationTest.class)
public class PSContentDesignWsTest extends ServletTestCase
{
   /**
    * Test the save associated workflows method, this covers the load also.
    * @throws Exception if fails
    */
   public void testLoadAndSaveAssociatedWorkflows() throws Exception
   {
      login("admin1", "demo");
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid ctguid = gmgr.makeGuid("311", PSTypeEnum.NODEDEF);
      IPSContentDesignWs cd = PSContentWsLocator.getContentDesignWebservice();
      List<PSContentTypeWorkflow> ctwfs = cd.loadAssociatedWorkflows(ctguid,
            true, true);
      assertEquals(ctwfs.size(), 2);
      List<IPSGuid> wfguids = new ArrayList<IPSGuid>();
      for (PSContentTypeWorkflow ctwf : ctwfs)
      {
         wfguids.add(ctwf.getWorkflowId());
      }
      cd.saveAssociatedWorkflows(ctguid, Collections.singletonList(wfguids.get(0)), true);
      List<PSContentTypeWorkflow> ctwfsmod = cd.loadAssociatedWorkflows(ctguid,
            true, false);
      assertEquals(ctwfsmod.size(), 1);
      cd.saveAssociatedWorkflows(ctguid, wfguids, true);
      ctwfsmod = cd.loadAssociatedWorkflows(ctguid,
            false, false);
      assertEquals(ctwfsmod.size(), 2);
   }
   
   @SuppressWarnings("unchecked")
   public void testGetItemEditUrl() throws Exception
   {
      login("admin1", "demo");
      String CONTENT_TYPE_NAME = "rffGeneric";
      IPSNodeDefinition node = loadNode(CONTENT_TYPE_NAME);
      
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
      // Testing a view with 2 hidden fields
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/

      // get URL for creating new generic item
      String viewName = IPSConstants.SYS_HIDDEN_FIELDS_VIEW_NAME + "description,filename";
      createView(viewName, CONTENT_TYPE_NAME);
      
      // make sure the new view is added
      List<String> fields = getViewFields(viewName, CONTENT_TYPE_NAME);
      assertTrue(fields.contains("sys_title"));
      assertTrue(!fields.contains("description"));
      assertTrue(!fields.contains("filename"));
      
      // get URL for editing an generic item id = 335
      PSLegacyGuid id = new PSLegacyGuid(335, -1);
      IPSContentDesignWs cd = PSContentWsLocator.getContentDesignWebservice();
      IPSContentWs cw = PSContentWsLocator.getContentWebservice();
      PSItemStatus status = cw.prepareForEdit(id); 
      String url = cd.getItemEditUrl(id, CONTENT_TYPE_NAME, viewName);
      cw.releaseFromEdit(status, false);
      
      assertTrue(url != null);
      assertTrue(url.indexOf("sys_contentid") > 0);
      
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      // Testing a view without any hidden fields
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      viewName = IPSConstants.SYS_HIDDEN_FIELDS_VIEW_NAME;
      url = cd.getItemEditUrl(null, CONTENT_TYPE_NAME, viewName);
      fields = getViewFields(viewName, CONTENT_TYPE_NAME);
      
      List<String> allFields = getViewFields(IPSConstants.SYS_ALL_VIEW_NAME,
            CONTENT_TYPE_NAME);
      assertTrue(fields.size() == allFields.size());
   }
   
   private IPSNodeDefinition loadNode(String ContentTypeName)
   {
      List<IPSNodeDefinition> nodes = PSContentTypeHelper
            .loadNodeDefs(ContentTypeName);
      assertTrue(ContentTypeName + " Content Type must exist", nodes != null
            && (!nodes.isEmpty()));
      return nodes.get(0);
   }
   
   /**
    * Creates a view if not exists for the given view name and content type.
    * 
    * @param viewName the view name, assumed not blank.
    * @param ctName the content type name, assumed not blank.
    */
   private void createView(String viewName, String ctName)
   {
      IPSContentDesignWs cd = PSContentWsLocator.getContentDesignWebservice();
      String url = cd.getItemEditUrl(null, ctName, viewName);
      assertTrue(url != null);
      assertTrue(url.indexOf("sys_contentid") == -1);      
   }

   /**
    * Gets a list of field names for the specified view name and content type.
    * 
    * @param viewName the name of the view in question, assumed not blank.
    * @param ctName the name of the content type of the view.
    * 
    * @return the list of field names, never <code>null</code>.
    */
   private List<String> getViewFields(String viewName, String ctName)
   {
      IPSNodeDefinition node = loadNode(ctName);
      PSContentEditor ctEditor = PSItemDefManager.getInstance()
      .getContentEditorDef(node.getGUID().longValue());
      PSViewSet vset = ctEditor.getViewSet();
      PSView view = vset.getView(viewName);
      return IteratorUtils.toList(view.getFields());      
   }
   
   /**
    * Login using the supplied credentials
    * 
    * @param uid The user id, assumed not <code>null</code> or empty.
    * @param pwd The password, assumed not <code>null</code> or empty.
    * 
    * @return The session id, never <code>null</code> or empty.
    * 
    * @throws Exception if the login fails.
    */
   private String login(String uid, String pwd) throws Exception
   {
      // hack to get by re-logging in to same session see PSSecurityfilter)
      session.setAttribute("RX_LOGIN_ATTEMPTS", null);
      PSSecurityFilter.authenticate(request, response, uid, pwd);
      String sessionId = (String) session.getAttribute(
         IPSHtmlParameters.SYS_SESSIONID);
      return sessionId;
   }

}
