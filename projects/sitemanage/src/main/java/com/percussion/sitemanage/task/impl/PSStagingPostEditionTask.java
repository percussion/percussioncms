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

package com.percussion.sitemanage.task.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.pubserver.data.PSPublishServerInfo;
import com.percussion.rx.publisher.IPSEditionTask;
import com.percussion.rx.publisher.IPSEditionTaskStatusCallback;
import com.percussion.services.contentchange.IPSContentChangeService;
import com.percussion.services.contentchange.data.PSContentChangeType;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Date;
import java.util.Map;

/**
 * This is a post edition task assumed to be get added to all staging editions to perform certain tasks
 * after the staging publishing happens.
 * The initial task is clearing of incremental publishing queue.
 * 
 */
public class PSStagingPostEditionTask implements IPSEditionTask {

	@Override
	public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException 
	{
		PSSpringWebApplicationContextUtils.injectDependencies(this);
	}

	@Override
	public void perform(IPSEdition edition, IPSSite site, Date startTime, Date endTime, long jobId, 
			long duration, boolean success, Map<String, String> params, IPSEditionTaskStatusCallback statusService) 
					throws Exception 
	{
		log.info("Started staging post edition task...");
		PSPublishServerInfo info = pubServerService.getPubServer(site.getSiteId().toString(), Long.toString(edition.getPubServerId().longValue()));
		debugData(edition, site, startTime, jobId);
		//Checke whether the server is staging
		if (PSPubServer.STAGING.equalsIgnoreCase(info.getServerType())) 
		{
			log.info("Clearing published items from staging incremenatl queue");
			//get all the items and delete the incremental events
			for (IPSPubItemStatus item : statusService.getIterableJobStatus()) {
				contentChangeService.deleteChangeEvents(site.getSiteId(), item.getContentId(), PSContentChangeType.PENDING_STAGED);
				log.debug("Cleared the item with id {} from staging incremenatl queue",
						item.getContentId());
			}
		}
		else
		{
			log.info("Server type is not staging, skipping the post edition tasks.");
		}
		log.info("Finished staging post edition task...");
	}

	/**
	 * Helper function that logs debug information.
	 * @param edition assumed not <code>null</code>
	 * @param site assumed not <code>null</code>
	 * @param startTime assumed not <code>null</code>
	 * @param jobId assumed not <code>null</code>
	 */
	private void debugData(IPSEdition edition, IPSSite site, Date startTime, long jobId) {
		log.debug("Started staging post edition task data: ");
		log.debug("edition id - " + edition.getGUID().toString());
		log.debug("site id - " + site.getGUID().toString());
		log.debug("job id - " + Long.toString(jobId));
		log.debug("Start time - " + startTime.toString());
	}

	@Override
	public TaskType getType() 
	{
		return TaskType.POSTEDITION;
	}

	private IPSContentChangeService contentChangeService;
	private IPSPubServerService pubServerService;

	public IPSContentChangeService getContentChangeService() 
	{
		return contentChangeService;
	}

	public void setContentChangeService(IPSContentChangeService contentChangeService) 
	{
		this.contentChangeService = contentChangeService;
	}

	public IPSPubServerService getPubServerService() 
	{
		return pubServerService;
	}

	public void setPubServerService(IPSPubServerService pubServerService) 
	{
		this.pubServerService = pubServerService;
	}
    protected final Logger log = LogManager.getLogger(IPSConstants.PUBLISHING_LOG);
}
