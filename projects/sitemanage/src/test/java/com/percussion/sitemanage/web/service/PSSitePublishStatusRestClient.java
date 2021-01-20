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
package com.percussion.sitemanage.web.service;

import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.test.PSObjectRestClient;
import com.percussion.sitemanage.data.PSSitePublishItem;
import com.percussion.sitemanage.data.PSSitePublishJob;
import com.percussion.sitemanage.data.PSSitePublishLogDetailsRequest;
import com.percussion.sitemanage.data.PSSitePublishLogRequest;
import com.percussion.sitemanage.data.PSSitePublishPurgeRequest;
import com.percussion.sitemanage.service.IPSSitePublishStatusService;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;

public class PSSitePublishStatusRestClient extends PSObjectRestClient implements IPSSitePublishStatusService
{
    private String path = "/Rhythmyx/services/sitemanage/pubstatus";
    
    public List<PSSitePublishJob> getCurrentJobs() throws PSDataServiceException
    {
        return getObjectsFromPath(concatPath(path,"current"), PSSitePublishJob.class);
    }
    
    public List<PSSitePublishJob> getCurrentJobsBySite(String siteId) throws PSDataServiceException
    {
        return getObjectsFromPath(concatPath(path,"current",siteId), PSSitePublishJob.class);
    }

    public List<PSSitePublishItem> getJobDetails(PSSitePublishLogDetailsRequest request) throws PSDataServiceException
    {
        String response = postObjectToPath(concatPath(path,"details"), request);
        return objectsFromResponseBody(response, PSSitePublishItem.class);
    }

    public List<PSSitePublishJob> getLogs(PSSitePublishLogRequest request) throws PSDataServiceException
    {
        String response = postObjectToPath(concatPath(path,"logs"), request);
        return objectsFromResponseBody(response, PSSitePublishJob.class);
    }

    public void purgeLog(PSSitePublishPurgeRequest purgeReq) throws PSDataServiceException
    {
        postObjectToPath(concatPath(path,"purge"), purgeReq);
    }

    @Override
    public boolean isSitePublished(IPSGuid siteId) throws PSDataServiceException
    {
        return true;
    }
}

