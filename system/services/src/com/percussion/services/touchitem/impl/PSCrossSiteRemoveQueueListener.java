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
package com.percussion.services.touchitem.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.server.webservices.crosssite.PSCrossSiteFolderRemoveActionData;
import com.percussion.server.webservices.crosssite.PSCrossSiteFolderRemoveActionProcessor;
import com.percussion.services.notification.PSMessageQueueListenerAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Listens for Cross Site Remove Item operations.
 * @author adamgent
 *
 */
public class PSCrossSiteRemoveQueueListener extends PSMessageQueueListenerAdapter<PSCrossSiteFolderRemoveActionData>
{

   /**
    * {@inheritDoc}
    */
   public void onMessage(PSCrossSiteFolderRemoveActionData object)
   {
      try
      {
         PSCrossSiteFolderRemoveActionProcessor ap = new PSCrossSiteFolderRemoveActionProcessor(object);
         int size = ap.getDependentItems() == null ? 0 : ap.getDependentItems().size();
         ms_logger.info("Updating cross site links asyncronously for number of items: " + size + " " + ap.getData());
         ap.processLinks();
         ap.saveLinks();
         ms_logger.info("Finished updating cross site links: " + ap.getData());
      }
      catch (PSCmsException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public Class<PSCrossSiteFolderRemoveActionData> getType()
   {
      return PSCrossSiteFolderRemoveActionData.class;
   }
   
   private static final Logger ms_logger = LogManager.getLogger(PSCrossSiteRemoveQueueListener.class);

}
