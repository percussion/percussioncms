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
package com.percussion.sitemanage.service.impl;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.server.IPSRequestContext;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.service.IPSDataItemSummaryService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.data.PSSitePublishResponse;
import com.percussion.sitemanage.service.IPSSitePublishService;
import com.percussion.sitemanage.service.IPSSitePublishService.PubType;
import com.percussion.webservices.PSWebserviceUtils;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a workflow action that gets executed by the aging agent, Gets the locator from the supplied workflow context 
 * and calls the {@link IPSSitePublishService#publish(String, PubType, String, boolean)} to do the actual work.
 * Uses the type as PubType.PUBLISH_NOW.
 * 
 */
public class PSAutoPublishItem extends PSDefaultExtension implements
      IPSWorkflowAction
{

   /*
    * //see base class method for details
    */
   @Override
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {
      super.init(def, codeRoot);
      //This is for wiring the services
      PSSpringWebApplicationContextUtils.injectDependencies(this);
   }

   /*
    * //see base class method for details
    */
    public void performAction(IPSWorkFlowContext arg0, IPSRequestContext arg1) throws PSExtensionProcessingException
    {
        PSLocator loc = new PSLocator(arg0.getContentID(), arg0.getBaseRevisionNum());
        String cguid = idMapper.getString(loc);
        IPSItemSummary sum = itemSummaryService.find(cguid);
        PSSitePublishResponse response = sitePublishService.publish(null, PubType.PUBLISH_NOW, cguid, sum.isResource(),
                null);
        if (response != null
                && StringUtils.equalsIgnoreCase(IPSPublisherJobStatus.State.FORBIDDEN.toString(), response.getStatus()))
        {
            log.warn("Publication has been stopped because the license is inactive or suspended, or its usage limits have been exceeded. Please check the License Monitor Gadget on the Dashboard.");
        }
        if (response != null && response.getWarningMessage() != "")
        {
            log.warn(response.getWarningMessage());
        }
    }
   
   //Getters and setters for the services
   public IPSDataItemSummaryService getItemSummaryService()
   {
      return itemSummaryService;
   }

   public void setItemSummaryService(IPSDataItemSummaryService itemSummaryService)
   {
      this.itemSummaryService = itemSummaryService;
   }

   public IPSSitePublishService getSitePublishService()
   {
      return sitePublishService;
   }

   public void setSitePublishService(IPSSitePublishService sitePublishService)
   {
      this.sitePublishService = sitePublishService;
   }

   public IPSIdMapper getIdMapper()
   {
      return idMapper;
   }

   public void setIdMapper(IPSIdMapper idMapper)
   {
      this.idMapper = idMapper;
   }

   //Services
   private IPSSitePublishService sitePublishService;

   private IPSIdMapper idMapper;

   private IPSDataItemSummaryService itemSummaryService;
   
   public static Log log = LogFactory.getLog(PSAutoPublishItem.class);

}
