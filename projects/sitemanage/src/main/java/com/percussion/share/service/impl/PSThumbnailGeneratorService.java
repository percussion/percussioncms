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
