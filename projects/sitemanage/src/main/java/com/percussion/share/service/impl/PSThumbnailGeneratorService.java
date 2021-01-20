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

package com.percussion.share.service.impl;

import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.queue.IPSPageImportQueue;
import com.percussion.server.PSRequest;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.share.service.IPSSystemProperties;
import com.percussion.share.service.impl.PSThumbnailRunner.Function;
import com.percussion.sitemanage.service.IPSSiteTemplateService;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.request.PSRequestInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@PSSiteManageBean("thumbnailGeneratorService")
public class PSThumbnailGeneratorService
{

    private IPSSiteTemplateService siteTemplateService;

    private IPSTemplateService templateService;

    private IPSPageService pageService;

    private IPSSystemProperties systemProps;

    private boolean m_isServerStarted = false;

    @Autowired
    public PSThumbnailGeneratorService(IPSSiteTemplateService siteTemplateService, IPSTemplateService templateService,
            IPSPageService pageService, IPSNotificationService notificationService)
    {
        this.siteTemplateService = siteTemplateService;
        this.templateService = templateService;
        this.pageService = pageService;

        notificationService.addListener(EventType.TEMPLATE_SAVED, new IPSNotificationListener()
        {
            @Override
            public void notifyEvent(PSNotificationEvent event)
            {
                generateTemplateThumbnail((String) event.getTarget(), true);
            }
        });

        notificationService.addListener(EventType.TEMPLATE_LOAD, new IPSNotificationListener()
        {
            @Override
            public void notifyEvent(PSNotificationEvent event)
            {
                generateTemplateThumbnail((String) event.getTarget(), true);
            }
        });

        notificationService.addListener(EventType.PAGE_SAVED, new IPSNotificationListener()
        {
            @Override
            public void notifyEvent(PSNotificationEvent event)
            {
                generatePageThumbnail((String) event.getTarget(), true);
            }
        });

        notificationService.addListener(EventType.PAGE_LOAD, new IPSNotificationListener()
        {
            @Override
            public void notifyEvent(PSNotificationEvent event)
            {
                checkPageThumbnail((String) event.getTarget(), true);
            }
        });

        notificationService.addListener(EventType.PAGE_DELETE, new IPSNotificationListener()
        {
            @Override
            public void notifyEvent(PSNotificationEvent event)
            {
                // delete((String) event.getTarget(),
                // PSThumbnailRunner.Function.DELETE_PAGE_THUMBNAIL);
            }
        });

        notificationService.addListener(EventType.CORE_SERVER_INITIALIZED, new IPSNotificationListener()
        {
            @Override
            public void notifyEvent(PSNotificationEvent event)
            {
                m_isServerStarted = true;
            }
        });

        notificationService.addListener(EventType.TEMPLATE_DELETE, new IPSNotificationListener()
        {
            @Override
            public void notifyEvent(PSNotificationEvent event)
            {
                delete((String) event.getTarget(), PSThumbnailRunner.Function.DELETE_TEMPLATE_THUMBNAIL);
            }
        });
        
        notificationService.addListener(EventType.CORE_SERVER_SHUTDOWN, new IPSNotificationListener()
        {
            @Override
            public void notifyEvent(PSNotificationEvent event)
            {
                PSThumbnailRunner.shutdown();
            }
        });
    }

    /**
     * Set the system properties on this service. This service will always use
     * the the values provided by the most recently set instance of the
     * properties.
     * 
     * @param systemProps the system properties
     */
    public void setSystemProps(IPSSystemProperties systemProps)
    {
        this.systemProps = systemProps;

        Integer i = null;
        if (m_isServerStarted)
            try
            {
                i = Integer.parseInt(systemProps.getProperty("thumbnailWorkerLimit"));
            }
            catch (Exception e)
            {
                i = -1;
            }
        PSThumbnailRunner.setActiveWorkerLimit(i);
    }

    /**
     * Gets the system properties used by this service.
     * 
     * @return The properties
     */
    public IPSSystemProperties getSystemProps()
    {
        return systemProps;
    }

    private void delete(String id, Function function)
    {
        doRun(id, function, null, null);

    }

    private void generateTemplateThumbnail(String templateId, boolean waitForCompletion)
    {
        doRun(templateId, PSThumbnailRunner.Function.GENERATE_TEMPLATE_THUMBNAIL, null, null);
    }

    private void generatePageThumbnail(String pageId, boolean waitForCompletion)
    {
        doRun(pageId, PSThumbnailRunner.Function.GENERATE_PAGE_THUMBNAIL, null, null);
    }

    private void checkPageThumbnail(String pageId, boolean waitForCompletion)
    {
        doRun(pageId, PSThumbnailRunner.Function.CHECK_FOR_PAGE_THUMBNAIL, null, null);
    }

    private void doRun(String id, PSThumbnailRunner.Function function, PSPage page, PSTemplate template)
    {
        final Map<String, Object> requestInfoMap = PSRequestInfo.copyRequestInfoMap();
        PSRequest request = (PSRequest) requestInfoMap.get(PSRequestInfo.KEY_PSREQUEST);
        requestInfoMap.put(PSRequestInfo.KEY_PSREQUEST, request.cloneRequest());
        PSThumbnailRunner runner = new PSThumbnailRunner(siteTemplateService, templateService, pageService,
                true, requestInfoMap);
        runner.scheduleThumbnailJob(id, function);
        Thread thumbnailRunner = new Thread(runner);
        thumbnailRunner.setDaemon(true);
        thumbnailRunner.start();
    }

}
