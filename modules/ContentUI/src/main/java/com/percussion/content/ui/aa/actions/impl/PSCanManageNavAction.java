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
