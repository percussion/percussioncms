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
package com.percussion.sitemanage.service.impl;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.itemmanagement.service.IPSItemService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.service.IPSDataItemSummaryService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.data.PSSitePublishResponse;
import com.percussion.sitemanage.service.IPSSitePublishService;
import com.percussion.sitemanage.service.IPSSitePublishService.PubType;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * This is a workflow action that gets executed by the aging agent, Gets the locator from the supplied workflow context 
 * and calls the  to do the actual work.
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
    public void performAction(IPSWorkFlowContext arg0, IPSRequestContext arg1) throws PSExtensionProcessingException, PSDataServiceException,   PSNotFoundException {
       try {
           PSLocator loc = new PSLocator(arg0.getContentID(), arg0.getBaseRevisionNum());
           String cguid = idMapper.getString(loc);
           IPSItemSummary sum = itemSummaryService.find(cguid);
           PSSitePublishResponse response = sitePublishService.publish(null, PubType.PUBLISH_NOW, cguid, sum.isResource(),
                   null);
           if (response != null
                   && StringUtils.equalsIgnoreCase(IPSPublisherJobStatus.State.FORBIDDEN.toString(), response.getStatus())) {
               log.warn("Publication has been stopped because the license is inactive or suspended, or its usage limits have been exceeded. Please check the License Monitor Gadget on the Dashboard.");
           }
           if (response != null && response.getWarningMessage() != "") {
               log.warn(response.getWarningMessage());
           }
       } catch (IPSPubServerService.PSPubServerServiceException | IPSItemWorkflowService.PSItemWorkflowServiceException | IPSItemService.PSItemServiceException e) {
           log.error(PSExceptionUtils.getMessageForLog(e));
           log.debug(PSExceptionUtils.getDebugMessageForLog(e));
           throw new PSExtensionProcessingException(e.getMessage(),e);
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
   
   public static final Logger log = LogManager.getLogger(PSAutoPublishItem.class);

}
