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
package com.percussion.services.pubserver;

import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSPublisherJobStatus.State;
import com.percussion.rx.publisher.IPSPublishingJobStatusCallback;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationServiceLocator;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.guid.IPSGuid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author JaySeletz
 * 
 */
public class PSPubServicePubJobStatusHandler implements IPSPublishingJobStatusCallback
{

   private IPSPublisherService m_pubSvc;
   private IPSPubServerDao m_pubServerDao;
   private IPSSiteManager m_siteManager;
   private static final Logger m_log = LogManager.getLogger(PSPubServicePubJobStatusHandler.class);

   public PSPubServicePubJobStatusHandler()
   {
      m_pubSvc = PSPublisherServiceLocator.getPublisherService();
      m_pubServerDao = PSPubServerDaoLocator.getPubServerManager();
      m_siteManager = PSSiteManagerLocator.getSiteManager();
   }

   public void notifyStatus(IPSPublisherJobStatus status)
   {
      if (status.getState().equals(State.COMPLETED) || status.getState().equals(State.COMPLETED_W_FAILURE))
      {

         try
         {
             IPSGuid editionId = status.getEditionId();
             IPSEdition edition = m_pubSvc.loadEdition(editionId);
             IPSGuid pubServerId = edition.getPubServerId();
             IPSSite site = m_siteManager.loadSiteModifiable(edition.getSiteId());
             PSPubServer pubServer = m_pubServerDao.loadPubServerModifiable(pubServerId);
             boolean hasServerChanged = false;

             if (!pubServer.hasFullPublished())
             {
                 pubServer.setHasFullPublisehd(true);
                 hasServerChanged = true;
             }

             if (pubServer.getSiteRenamed()) {
                 pubServer.setSiteRenamed(false);
                 hasServerChanged = true;
                 notifySiteRename(site, pubServer.getServerType());
             }

             if (hasServerChanged) {
                 m_pubServerDao.savePubServer(pubServer);
             }
         }
         catch (Exception e)
         {
            m_log.error("Failed to update pubserver status for completed edition: " + e.getLocalizedMessage());
         }
         
         
      }
   }

    private void notifySiteRename(IPSSite site, String serverType)
    {
        PSNotificationEvent notifyEvent = new PSNotificationEvent(PSNotificationEvent.EventType.SITE_RENAMED, site);
        notifyEvent.setServerType(serverType);
        IPSNotificationService srv = PSNotificationServiceLocator.getNotificationService();
        if (srv != null)
        {
            srv.notifyEvent(notifyEvent);
        }
        else
        {
            m_log.warn("Notification service should not be null.  Fired event: " + notifyEvent);
        }
    }

}
