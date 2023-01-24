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

package com.percussion.rx.delivery;


import com.percussion.rx.delivery.impl.PSBaseFtpDeliveryHandler;
import com.percussion.rx.publisher.impl.PSPublishingJob;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.publisher.IPSDeliveryType;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.guid.IPSGuid;

public class PSConnectivityCheck
{
   public static boolean checkFTPConnectivity(IPSGuid edition, PSPublishingJob job, 
         PSPubServer pubServer, String pubServerType, boolean isSSH) throws PSNotFoundException {
      
      IPSDeliveryHandler handler;
      IPSDeliveryType t;
      IPSPublisherService pubsvc = PSPublisherServiceLocator.getPublisherService();
      if (pubServerType != null
               && !isSSH) {
           t = pubsvc.loadDeliveryType("ftp_only");
           handler = (IPSDeliveryHandler)PSBaseServiceLocator.getBean(t.getBeanName());
       } else {
          t = pubsvc.loadDeliveryType("sftp_only");
          handler = (IPSDeliveryHandler)PSBaseServiceLocator.getBean(t.getBeanName());;
       }
       PSGuid siteGuid = new PSGuid(PSTypeEnum.SITE, pubServer.getSiteId());
       
      try
      {
         handler.init(job.getJobid(), PSSiteManagerLocator.getSiteManager().loadSite(siteGuid), 
               pubServer);
         ((PSBaseFtpDeliveryHandler)handler).doLogin(job.getJobid(), false, ((PSBaseFtpDeliveryHandler)handler).getTimeout(), 1);
      }
      catch (PSDeliveryException e)
      {
         if(e.getErrorCode() == IPSDeliveryErrors.UNEXPECTED_ERROR)
            throw new IllegalStateException("Error initializing delivery handler.", e);
         throw new IllegalStateException("Cannot connect to target FTP Server: " + edition, e);
      }
      finally
      {
         ((PSBaseFtpDeliveryHandler)handler).logoff();
      }
  
      return true;
   } 
}
