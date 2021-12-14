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
