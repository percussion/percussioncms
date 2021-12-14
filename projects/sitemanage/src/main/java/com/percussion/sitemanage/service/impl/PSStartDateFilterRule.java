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

package com.percussion.sitemanage.service.impl;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.IPSItemFilterRule;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.percussion.itemmanagement.service.impl.PSAbstractWorkflowExtension.WorkflowItem.passedStartPublishDate;


/**
 * 
 * This item filter is a publishnow item filter. It is used to get items only
 * if the start date of the item is not null and before the current date or equal to the current date.
 * Returns list of items which match the filtering criteria.
 */
public class PSStartDateFilterRule implements IPSItemFilterRule
{

    @Override
    public List<IPSFilterItem> filter(List<IPSFilterItem> items, Map<String, String> params) throws PSFilterException
    {
        List<IPSFilterItem> allowedStartDateItems = new ArrayList<>();
        IPSCmsObjectMgr cmMgr = PSCmsObjectMgrLocator.getObjectManager();;
        for(IPSFilterItem item : items)
        {
            PSComponentSummary summary = cmMgr.loadComponentSummary(item.getItemId().getUUID());            
            if(isPublishable(summary))     
            {
                allowedStartDateItems.add(item);
            }
        }
        return allowedStartDateItems;
    }

    @Override
    public int getPriority()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void init(IPSExtensionDef arg0, File arg1) throws PSExtensionException
    {
        // TODO Auto-generated method stub

    }
    
    private boolean isPublishable(PSComponentSummary summary)
    {
        if (passedStartPublishDate(summary))
            return true;

        // if the item is scheduled to publish in the future, but has already
        // published,
        // then we should publish the "last published revision"
        return summary.getPublicRevision() != -1;
    }
}
