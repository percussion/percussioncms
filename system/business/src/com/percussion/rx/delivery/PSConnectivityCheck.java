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

package com.percussion.rx.delivery;


import com.percussion.rx.delivery.impl.PSBaseFtpDeliveryHandler;
import com.percussion.rx.delivery.impl.PSFtpDeliveryHandler;
import com.percussion.rx.delivery.impl.PSSFtpDeliveryHandler;
import com.percussion.rx.publisher.impl.PSPublishingJob;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
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
         PSPubServer pubServer, String pubServerType, boolean isSSH)
   {
      
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
            throw new IllegalStateException("Error initializing delivery handler. Please check Delivery-servers.xml", e);
         throw new IllegalStateException("Cannot connect to target FTP Server: " + edition, e);
      }
      finally
      {
         ((PSBaseFtpDeliveryHandler)handler).logoff();
      }
  
      return true;
   } 
}
