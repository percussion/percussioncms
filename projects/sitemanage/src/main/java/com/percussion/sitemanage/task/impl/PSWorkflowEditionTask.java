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

import com.percussion.itemmanagement.service.impl.PSAbstractWorkflowExtension;
import com.percussion.rx.publisher.IPSEditionTask;
import com.percussion.rx.publisher.IPSEditionTaskStatusCallback;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSSiteItem.Operation;
import com.percussion.services.publisher.IPSSiteItem.Status;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.guid.IPSGuid;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * This post edition task workflows the item after publish.
 * It only workflows the item if  the item is currently in the state specified
 * by the parameter {@value #STATE_PARAMETER} and not checked out by
 * any user.
 * 
 * <p>
 * The trigger used to workflow the item is specified by the parameter
 * {@value #TRIGGER_PARAMETER}.
 * <p>
 * 
 * @author adamgent
 *
 */
public class PSWorkflowEditionTask extends PSAbstractWorkflowExtension implements IPSEditionTask
{
    
    @Override
    public TaskType getType()
    {
        return TaskType.POSTEDITION;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Workflows all the items in the job if they are valid.
     */
    @Override
    public void perform(
            @SuppressWarnings("unused") IPSEdition edition, 
            IPSSite site, 
            @SuppressWarnings("unused") Date start, 
            @SuppressWarnings("unused") Date end, 
            long jobId, 
            @SuppressWarnings("unused") long duration, 
            @SuppressWarnings("unused") boolean success,
            Map<String, String> params, 
            IPSEditionTaskStatusCallback statusService) throws Exception
    {    
        log.debug("Started workflowing items for jobId: " + jobId);   

        setSecurity(); 
        List<IPSPubItemStatus> items = statusService.getJobStatus();
        
        WorkflowItemWorker worker = getWorker(params);
        
        boolean isDefaultPubServer = false;
        IPSGuid pubServerId = edition.getPubServerId();
        PSPubServer pubServer = getPubServerService().getDefaultPubServer(site.getGUID());
        if (pubServerId != null && pubServer != null && pubServerId.equals(pubServer.getGUID()))
            isDefaultPubServer = true;
        
        Set<Integer> pubIds = new HashSet<>();
        Set<Integer> unpubIds = new HashSet<>();
        for(IPSPubItemStatus item : items) {
        	try
        	{
        		boolean skipped = worker.processItem(item, site, isDefaultPubServer);
        		int contentId = item.getContentId();
        		if (item.getStatus() == Status.SUCCESS)
        		{
        			if (!skipped)
        			{
        				if (item.getOperation() == Operation.PUBLISH)
        				{
        					pubIds.add(contentId);
        				}
        				else
        				{
        					unpubIds.add(contentId);
        				}
        			}
        		}
        	}
        	catch(Exception e)
        	{
        		log.error("Error workflowing this content: " + item.getContentId(), e);
        	}
        }
        log.debug("Finished workflowing items for jobId: " + jobId);
        
        if (!pubIds.isEmpty())
        {
            log.debug("Started updating post date for items for jobId: " + jobId);
            getCmsObjectManager().setPostDate(pubIds);
            log.debug("Finished updating post date for items for jobId: " + jobId);

            log.debug("Started clearing start date for items for jobId: " + jobId);
            getCmsObjectManager().clearStartDate(pubIds);
            log.debug("Finished clearing start date for items for jobId: " + jobId);
        }

        if (!unpubIds.isEmpty())
        {
            log.debug("Started clearing expiry date for items for jobId: " + jobId);
            getCmsObjectManager().clearExpiryDate(unpubIds);
            log.debug("Finished clearing expiry date for items for jobId: " + jobId);
        }
    }  
    
}

