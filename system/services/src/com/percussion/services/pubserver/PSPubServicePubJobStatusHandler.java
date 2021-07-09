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
