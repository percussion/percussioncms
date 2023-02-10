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
package com.percussion.sitemanage.service;

import java.util.List;


import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.sitemanage.data.PSSitePublishItem;
import com.percussion.sitemanage.data.PSSitePublishJob;
import com.percussion.sitemanage.data.PSSitePublishLogDetailsRequest;
import com.percussion.sitemanage.data.PSSitePublishLogRequest;
import com.percussion.sitemanage.data.PSSitePublishPurgeRequest;
import com.percussion.utils.guid.IPSGuid;

/**
 * @author DavidBenua
 *
 */
public interface IPSSitePublishStatusService {
	
	/**
	 * Gets the list of currently active jobs.  
	 * @return the jobs. Never <code>null</code> but may be <code>empty</code>. 
	 * @throws PSDataServiceException
	 */
	List<PSSitePublishJob> getCurrentJobs() throws PSDataServiceException;

	/**
     * Gets the list of currently active jobs by site.
     * @param siteId the site id.
     * @return the jobs. Never <code>null</code> but may be <code>empty</code>. 
     * @throws PSDataServiceException
     */
    List<PSSitePublishJob> getCurrentJobsBySite(String siteId) throws PSDataServiceException;
	
	/**
	 * Gets the completed jobs in recent history. 
	 * @param request the job request.
	 * @return the job status for each completed job. Never <code>null</code> but may 
	 * be <code>empty</code>. 
	 * @throws PSDataServiceException
	 */
	List<PSSitePublishJob> getLogs(PSSitePublishLogRequest request) throws PSDataServiceException; 
	
	/**
	 * Purges a list of log entries
	 * @param purgeReq contains the list of job ids to purge. 
	 * @throws PSDataServiceException
	 */
	void purgeLog(PSSitePublishPurgeRequest purgeReq) throws PSDataServiceException;

	/**
	 * Gets the log details for a job 
	 * @param request never <code>null</code>.
	 * @return the list of item status records. Never <code>null</code> 
	 * but may be <code>empty</code>
	 * @throws PSDataServiceException
	 */
	List<PSSitePublishItem> getJobDetails(PSSitePublishLogDetailsRequest request) throws PSDataServiceException;
	
	/**
	 * Checks whether the site has ever been published.
	 * @param siteGuid must not be <code>null</code>
	 * @param serverGuid must not be <code>null</code>
	 * @return true if the site has been previously published, this is based on existance of pub logs.
	 * @throws PSDataServiceException
	 */
	boolean isSitePublished(IPSGuid siteGuid) throws PSDataServiceException;
	
}
