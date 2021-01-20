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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.sitemanage.service.impl;

import com.percussion.services.contentchange.IPSContentChangeService;
import com.percussion.services.contentchange.PSContentChangeServiceLocator;
import com.percussion.services.contentchange.data.PSContentChangeType;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.IPSItemFilterRule;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.util.IPSHtmlParameters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * This is a staging filter rule that extends public asset item filter rule. This rule while getting the
 * workflow items passes true for staging.
 */
public class PSStagingItemFilterRule extends PSPublicAssetItemFilterRule implements IPSItemFilterRule
{
    
    @Override
    public List<IPSFilterItem> filter(List<IPSFilterItem> items, Map<String, String> params)
    {
        boolean isPublish = !"unpublish".equals(params.get(IPSHtmlParameters.SYS_PUBLISH));
        PSPubServer pubServer = findPubServer(params.get(IPSHtmlParameters.SYS_EDITIONID));
        String ignoreAssets = pubServer==null?"false":pubServer.getProperty(PUBLISH_IGNORE_UNMODIFIED_ASSETS_PROPERTY).getValue();
        boolean ignoreUnModAssets = StringUtils.equals(ignoreAssets, "true");
        Long serverId = pubServer==null?null:pubServer.getServerId();
        WorkflowItemWorker worker = getWorker(params);
        List<IPSFilterItem> rvalue = new ArrayList<IPSFilterItem>();
        IPSContentChangeService contentChangeService = PSContentChangeServiceLocator.getContentChangeService();
        List<Integer> changedIds;
        Set<Integer> changedIdsSet = null;
        if (ignoreUnModAssets) {
            if (contentChangeService != null) {
                changedIds = contentChangeService.getChangedContent(pubServer.getSiteId(), PSContentChangeType.PENDING_STAGED);
                changedIdsSet = new HashSet<>(changedIds);
            }
        }
        for(IPSFilterItem item : items) {
            WorkflowItem wfItem = worker.getWorkflowItem(item.getItemId(), true);
            IPSFilterItem r = process(worker, item, wfItem, isPublish, ignoreUnModAssets, serverId, changedIdsSet);
            if (r != null)
                rvalue.add(r);
        }
        return rvalue;
    }
}

