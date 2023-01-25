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
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.cas.PSModifyRelatedContent;
import com.percussion.cms.PSCmsException;
import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.error.PSException;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

import java.util.Collection;
import java.util.Map;

/**
 * An action that will move a slot item to another slot at the specified index
 * using the specified template.
 * <p>
 * Expects the following parameters:
 * </p>
 * <table border="1" cellspacing="0" cellpadding="5">
 * <thead>  
 * <th>Name</th><th>Allowed Values</th><th>Details</th> 
 * </thead>
 * <tbody>
 * <tr>
 * <td>objectId</td><td>The object id string</td><td>Required</td>
 * </tr>
 * <tr>
 * <td>newslotid</td><td>Slot id of the target slot</td><td>Required</td>
 * </tr>
 * <tr>
 * <td>newtemplate</td><td>Template to use</td><td>Required</td>
 * </tr>
 * <tr>
 * <td>index</td><td>The desired position to move to</td>
 * <td>Optional, if not set then will be put in the last position</td>
 * </tr>
 * </tbody>
 * </table>
 */
public class PSMoveToSlotAction extends PSAAActionBase
{

   /* (non-Javadoc)
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      PSAAObjectId objectId = getObjectId(params);      
      
      int nslot = getValidatedInt(params, "newslotid", true);
      int ntemp = getValidatedInt(params, "newtemplate", false);
      int idx = getValidatedInt(params, "index", false);
            
      IPSRequestContext request = getRequestContext();
      try
      {
         //Validate if target slot can accept the template
         validateTemplate(objectId, nslot, ntemp);
         PSModifyRelatedContent.moveToSlot(
                  Integer.parseInt(objectId.getRelationshipId()), nslot,
                  idx, ntemp, request);
      }      
      catch (PSException e)
      {
         if(e.getLocalizedMessage().equals(
            PSModifyRelatedContent.ERROR_MSG_NO_VARIANT_FOUND))
            throw new PSAAClientActionException("needs_template_id");
         throw new PSAAClientActionException(e);
      }      
      return new PSActionResponse(SUCCESS, PSActionResponse.RESPONSE_TYPE_PLAIN);
   }
   
   /**
    * Helper method to validate that the target slot can except the 
    * template specified or found in the relationship for the
    * object being moved.
    * @param objectId assumed not <code>null</code>.
    * @param slotid 
    * @param templateid
    * @throws PSAAClientActionException
    */
   private void validateTemplate(PSAAObjectId objectId, int slotid, int templateid)
           throws PSAAClientActionException, PSNotFoundException {
      PSAAObjectId targetId = (PSAAObjectId)objectId.clone();
      IPSRequestContext request = getRequestContext();
      try
      {
         if(templateid == -1)
         {
            PSRelationship rel = PSModifyRelatedContent.getRelationship(
               Integer.parseInt(targetId.getRelationshipId()), request);
            String variantid = rel.getProperty(IPSHtmlParameters.SYS_VARIANTID);
            if(StringUtils.isBlank(variantid))
               return; // no variant id this will be caught in the execute method
            templateid = Integer.parseInt(variantid);
         }
         targetId.modifyParam(IPSHtmlParameters.SYS_SLOTID, String.valueOf(slotid));
         Collection<IPSAssemblyTemplate> templates = 
            PSGetItemTemplatesForSlotAction.getAssociatedTemplates(targetId);
         boolean isValidTemplate = false;
         for(IPSAssemblyTemplate temp : templates)
         {
            if(temp.getGUID().getUUID() == templateid)
            {
               isValidTemplate = true;
               break;
            }
         }
         if(!isValidTemplate)
         {
            throw new PSAAClientActionException("needs_template_id");
         }
      }
      catch (JSONException e)
      {
         throw new PSAAClientActionException(e);
      }
      catch (PSAssemblyException e)
      {
         throw new PSAAClientActionException(e);
      }      
      catch (PSCmsException e)
      {
         throw new PSAAClientActionException(e);
      }
      
   }

}
