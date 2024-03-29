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

package com.percussion.services.siterename.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.percussion.delivery.client.IPSDeliveryClient.HttpMethodType;
import com.percussion.delivery.client.IPSDeliveryClient.PSDeliveryActionOptions;
import com.percussion.delivery.client.PSDeliveryClient;
import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.siterename.IPSSiteRenameService;

/**
 * Iterates through all DTS micro services found in PSDeliveryServiceInfo. Calls
 * the "renameSite" method on each service. Should be implemented by each
 * service via the {@link com.percussion.delivery.services.IPSRestService}
 * interface.
 *
 * Note: It should be noted that this class is only triggered AFTER a full publish
 * has first completed after a site rename.
 *
 * @author chriswright
 *
 */
public class PSSiteRenameListener implements IPSSiteRenameService, IPSNotificationListener
{

    /**
     * Logger.
     */
    private static final Logger log = LogManager.getLogger(PSSiteRenameListener.class.getName());

    /**
     * The delivery info service.
     */
    private IPSDeliveryInfoService deliveryInfoService;

    /**
     * The path for the rename site functionality for each micro service in the
     * DTS.
     */
    private static final String ENDPOINT = "updateOldSiteEntries";

    private static final Map<String, String[]> ENDPOINTS;

    static {
        Map<String, String[]> services = new HashMap<>(6);
        services.put("perc-form-processor", new String[]{"form"});
        services.put("feeds", new String[]{"rss"});
        services.put("perc-metadata-services", new String[]{"metadata"});
        services.put("perc-comments-services", new String[]{"comment", "likes"});
        services.put("perc-membership-services", new String[]{"membership"});
        services.put("perc-polls-services", new String[]{"polls"});
        ENDPOINTS = new HashMap<>(services);
    }

    @Autowired
    public PSSiteRenameListener(IPSNotificationService notificationService, IPSDeliveryInfoService deliveryInfoService)
    {
        if (notificationService != null)
        {
            notificationService.addListener(EventType.SITE_RENAMED, this);
        }
        this.deliveryInfoService = deliveryInfoService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyEvent(PSNotificationEvent notification)
    {
        if (!notification.getType().equals(EventType.SITE_RENAMED))
        {
            log.debug("Should not have received notification for event: " + notification);
            return;
        }

        if (!(notification.getTarget() instanceof IPSSite))
        {
            log.debug("Notification target should have been instance of IPSSite: " + notification);
            return;
        }

        IPSSite site = (IPSSite) notification.getTarget();
        deleteOldDTSEntries(site,notification.getServerType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteOldDTSEntries(IPSSite site, String serverType)
    {
        log.info("Received notification event for site: " + site);

        // for each delivery server, get its registered services.
        List<PSDeliveryInfo> infos = deliveryInfoService.findAll();
        for (PSDeliveryInfo deliveryServer : infos) {
            if (deliveryServer.getServerType() != null) {
            if (!deliveryServer.getServerType().equals("license") && (deliveryServer.getServerType().equalsIgnoreCase(serverType))) {
                List<String> services = deliveryServer.getAvailableServices();
                for (String service : services) {
                    log.info("Service name is : " + service + " on server: " + deliveryServer.getServerType());
                    if (!site.getName().equals(site.getPreviousName())) {
                        deleteForService(service, site.getPreviousName(), site.getName(), deliveryServer);
                    }
                }
              }
            }
        }
    }

    /**
     *
     * @param service
     * @param prevSiteName
     * @param server
     */
    private void deleteForService(String service, String prevSiteName, String newSiteName, PSDeliveryInfo server)
    {
        PSDeliveryClient client = new PSDeliveryClient();
        String result = "";
        StringBuilder url = null;
        try
        {
            if (ENDPOINTS.containsKey(service)) {
                for (String serviceUrl : ENDPOINTS.get(service)) {
                    url = new StringBuilder();
                    url.append(service);
                    url.append("/").append(serviceUrl);
                    url.append("/").append(ENDPOINT);
                    url.append("/").append(prevSiteName);
                    url.append("/").append(newSiteName);
                    log.debug("Sending request via url: " + url.toString() + " to server type: " + server.getServerType());
                    result = client.getString(new PSDeliveryActionOptions(server, url.toString(), HttpMethodType.DELETE, true));
                    log.debug("Result is: " + result);
                }
            }
        }
        catch (Exception e)
        {
            log.error("Exception deleting entries for site and url: " + url, e);
        }
    }
}
