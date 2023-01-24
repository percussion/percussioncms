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

