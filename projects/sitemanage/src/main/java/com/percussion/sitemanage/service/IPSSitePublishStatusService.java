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
