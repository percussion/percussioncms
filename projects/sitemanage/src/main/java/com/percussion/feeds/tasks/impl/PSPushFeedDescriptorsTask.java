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
package com.percussion.feeds.tasks.impl;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.feeds.service.IPSFeedsInfoService;
import com.percussion.rx.publisher.IPSEditionTask;
import com.percussion.rx.publisher.IPSEditionTaskStatusCallback;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.pubserver.PSPubServerDaoLocator;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;

import java.io.File;
import java.util.Date;
import java.util.Map;

/**
 * A post edition task that calls the CMS Feed info service to have it push feed
 * descriptors for the publishing site to the feeds service on the delivery tier.
 * @author erikserating
 *
 */
public class PSPushFeedDescriptorsTask implements IPSEditionTask
{

    private IPSFeedsInfoService infoService;
    
    /* (non-Javadoc)
     * @see com.percussion.rx.publisher.IPSEditionTask#getType()
     */
    public TaskType getType()
    {
        return TaskType.POSTEDITION;
    }

    /* (non-Javadoc)
     * @see com.percussion.rx.publisher.IPSEditionTask#perform(com.percussion.services.publisher.IPSEdition, com.percussion.services.sitemgr.IPSSite, java.util.Date, java.util.Date, long, long, boolean, java.util.Map, com.percussion.rx.publisher.IPSEditionTaskStatusCallback)
     */
    @SuppressWarnings("unused")
    public void perform(IPSEdition edition, IPSSite site, Date startTime, Date endTime, long jobid, long duration,
                    boolean success, Map<String, String> params, IPSEditionTaskStatusCallback status) throws Exception
    {
    	PSPubServer server = PSPubServerDaoLocator.getPubServerManager().loadPubServer(edition.getPubServerId());
        infoService.pushFeeds(site, server);
    }

    /* (non-Javadoc)
     * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
     */
    public void init(@SuppressWarnings("unused") IPSExtensionDef def, @SuppressWarnings("unused") File file)
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);

    }

    /**
     * @return the infoService
     */
    public IPSFeedsInfoService getInfoService()
    {
        return infoService;
    }

    /**
     * @param infoService the infoService to set
     */
    public void setInfoService(IPSFeedsInfoService infoService)
    {
        this.infoService = infoService;
    }
    
    

}
