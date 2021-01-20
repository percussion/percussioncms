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

package com.percussion.sitemanage.task.impl;

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

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
			Iterator<IPSPubItemStatus> itemIter = statusService.getIterableJobStatus().iterator();
			while (itemIter.hasNext()) 
			{
				IPSPubItemStatus item = itemIter.next();
				contentChangeService.deleteChangeEvents(site.getSiteId(),item.getContentId(),PSContentChangeType.PENDING_STAGED);
				log.debug("Cleared the item with id " + item.getContentId() + "from staging incremenatl queue");
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
    protected final Log log = LogFactory.getLog(getClass());	
}
