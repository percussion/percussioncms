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

package com.percussion.sitemanage.service.impl;

import static com.percussion.itemmanagement.service.impl.PSAbstractWorkflowExtension.WorkflowItem.passedStartPublishDate;

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
