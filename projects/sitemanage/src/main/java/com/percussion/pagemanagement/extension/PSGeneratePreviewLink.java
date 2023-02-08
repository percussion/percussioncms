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

package com.percussion.pagemanagement.extension;

import com.percussion.extension.IPSUdfProcessor;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Generate "friendly" preview link or URL for a page or asset.
 * 
 * @author yubingchen
 *
 */
public class PSGeneratePreviewLink extends com.percussion.extension.PSSimpleJavaUdfExtension
{

    private static final Logger log = LogManager.getLogger(PSGeneratePreviewLink.class);
    
    /**
     * It returns a "friendly URL", which is the finder path from UI.
     * 
     * See {@link IPSUdfProcessor#processUdf(Object[], IPSRequestContext) processUdf} for detail.
     * 
     * @param params the parameters for this extension, never <code>null</code>. It contains
     * the content ID (1st element) and revision (2nd element) of the page or asset in question.
     * @param request the parameter request, never <code>null</code>
     */
    public Object processUdf(Object[] params, IPSRequestContext request)
    {
        IPSGuid id = getContentId(params);
        IPSContentWs service = PSContentWsLocator.getContentWebservice();
        String[] paths = service.findItemPaths(id);
        if (paths.length == 0)
        {
            String msg1 = "Failed to generate friendly URL for content ID = " + id;
            IPSCmsObjectMgr cmsMgr = PSCmsObjectMgrLocator.getObjectManager();
            if (cmsMgr.findItemEntry(id.getUUID()) != null) {
                log.debug("{} as the item does not exist.", msg1);
            }
            else {
                log.debug("{} as the item is not under a folder.", msg1);
            }
            
            return "";            
        }
        
        return PSPathUtils.getFinderPath(paths[0]);
    }

    /**
     * Gets the content ID from the specified parameters.
     * @param params the parameters, expecting 1st parameter is the content ID and 2nd is the revision.
     * @return the content ID in GUID, never <code>null</code>.
     */
    private IPSGuid getContentId(Object[] params)
    {
        if (params.length < 2) {
            throw new IllegalArgumentException("params must contain 2 parameters.");
        }
        
        int contentId = getIntParameter(params, 0);
        int revision = getIntParameter(params, 1);
        
        return new PSLegacyGuid(contentId, revision);
    }
    
    private int getIntParameter(Object params[], int index)
    {
        Object p = params[index];
        if (!(p instanceof Integer)) {
            throw new IllegalArgumentException("Parameter[" + index + "] is not Integer.");
        }
        
        return ((Integer)p).intValue();
    }
    
}
