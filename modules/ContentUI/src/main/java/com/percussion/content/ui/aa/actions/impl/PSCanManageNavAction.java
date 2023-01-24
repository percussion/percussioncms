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

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.fastforward.managednav.IPSManagedNavService;
import com.percussion.fastforward.managednav.PSManagedNavServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;

import java.util.Map;

/**
 * Determine if we can manage navigation via AA. First we see if there is even
 * a nav at all. Then we also check to see if the nav slot is empty.
 * Return <code>true</code> if nav exists and the slot is not empty.
 * @author erikserating
 *
 */
public class PSCanManageNavAction extends PSAAActionBase
{

    /* (non-Javadoc)
     * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
     */
    public PSActionResponse execute(Map<String, Object> params) throws PSAAClientActionException
    {
        boolean result = true;
        PSAAObjectId objectId = getObjectId(params);
        IPSManagedNavService managedNavService =
                PSManagedNavServiceLocator.getContentWebservice();
        String slotId = String.valueOf(managedNavService.getMenuSlotId());
        IPSGuid folderGuid = PSGuidUtils.makeGuid(objectId.getFolderId(), PSTypeEnum.LEGACY_CONTENT);
        IPSGuid navGuid = managedNavService.findNavigationIdFromFolder(folderGuid);
        if(navGuid != null)
        {
            try
            {
                //Check to see if nav slot is empty, if so there is nothing to manage
                PSComponentSummary sum = PSAAObjectId.getItemSummary(navGuid.getUUID());
                String currentuser = getCurrentUser();
                String rev = String.valueOf(sum.getAAViewableRevision(currentuser));
                PSLocator ownerLocator = new PSLocator(navGuid.getUUID(), Integer.parseInt(rev));
                PSRelationshipProcessor relProc = PSRelationshipProcessor.getInstance();
//                PSRelationshipProcessor relProc = new PSRelationshipProcessor(PSRequest
//                           .getContextForRequest());
                PSRelationshipFilter filter = new PSRelationshipFilter();

                filter.setName(PSRelationshipFilter.FILTER_NAME_ACTIVE_ASSEMBLY);
                filter.setOwner(ownerLocator);
                filter.setProperty(IPSHtmlParameters.SYS_SLOTID,slotId);
                PSRelationshipSet dependents = relProc.getRelationships(filter);
                result = !dependents.isEmpty();
            }
            catch (PSCmsException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            result = false;
        }
        return new PSActionResponse(Boolean.toString(result),
                PSActionResponse.RESPONSE_TYPE_PLAIN);
    }

}
