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
