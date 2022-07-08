/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This action is used to add a snippet to a slot.
 * <p>
 * Expects the following parameters:
 * </p>
 * <table border="1" cellspacing="0" cellpadding="5">
 * <thead>  
 * <th>Name</th><th>Allowed Values</th><th>Details</th> 
 * </thead>
 * <tbody>
 * <tr>
 * <td>{@link #OWNER_ID}</td><td>The owner id</td><td>Required</td>
 * </tr>
 * <tr>
 * <td>{@link #DEPENDENT_ID}</td><td>The dependent id</td><td>Required</td>
 * </tr>
 * <tr>
 * <td>{@link #SLOT_ID}</td><td>The slot id</td><td>Required</td>
 * </tr>
 * <tr>
 * <td>{@link #TEMPLATE_ID}</td><td>The template id</td><td>Required</td>
 * </tr>
 * <tr>
 * <td>{@link #FOLDER_ID}</td><td>The folder id</td><td>Optional</td>
 * </tr>
 * <tr>
 * <td>{@link #SITE_ID}</td><td>The site id</td><td>Optional</td>
 * </tr>
 * </tbody>
 * </table>
 */
public class PSAddSnippetAction extends PSAAActionBase
{

   /* (non-Javadoc)
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      int ownerId = getValidatedInt(params, OWNER_ID, true);
      int dependentId = getValidatedInt(params, DEPENDENT_ID, true);
      int slotId = getValidatedInt(params, SLOT_ID, true);
      int templateId = getValidatedInt(params, TEMPLATE_ID, true);
      Object folderPath = getParameter(params, FOLDER_PATH);
      Object siteName = getParameter(params, SITE_NAME);
      
      Map<String, IPSGuid> siteFolder;
      try
      {
         siteFolder = PSActionUtil.resolveSiteFolders(siteName, folderPath);
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }
     
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid ownerGuid = getItemGuid(ownerId);
      IPSGuid dependentGuid = getItemGuid(dependentId);
      IPSGuid slotGuid = mgr.makeGuid(slotId, PSTypeEnum.SLOT);
      IPSGuid templateGuid = mgr.makeGuid(templateId, PSTypeEnum.TEMPLATE);
      
      IPSGuid folderGuid = siteFolder.get(IPSHtmlParameters.SYS_FOLDERID);
      IPSGuid siteGuid = siteFolder.get(IPSHtmlParameters.SYS_SITEID);
      
      IPSContentWs cservice = PSContentWsLocator.getContentWebservice();
      List<PSAaRelationship> rel = null;
      try
      {
         rel = cservice.addContentRelations(ownerGuid, 
            Collections.singletonList(dependentGuid), folderGuid, siteGuid, 
            slotGuid, templateGuid, -1);
      }
      catch (PSErrorException e)
      {
         throw createException(e);
      }
      
      String rid = String.valueOf(rel.get(0).getId());
      return new PSActionResponse(rid, PSActionResponse.RESPONSE_TYPE_PLAIN);
   }

   
   /**
    * Parameter names of this action.
    */
   public static String OWNER_ID = "ownerId";
   public static String DEPENDENT_ID = "dependentId";
   public static String SLOT_ID = "slotId";
   public static String TEMPLATE_ID = "templateId";
   public static String FOLDER_PATH = "folderPath";
   public static String SITE_NAME = "siteName";
}
