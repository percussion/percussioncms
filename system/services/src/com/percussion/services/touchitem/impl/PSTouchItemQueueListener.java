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

import static com.percussion.relationship.IPSExecutionContext.RS_PRE_CONSTRUCTION;
import static com.percussion.relationship.IPSExecutionContext.RS_PRE_DESTRUCTION;
import static com.percussion.relationship.IPSExecutionContext.RS_PRE_UPDATE;

import com.percussion.design.objectstore.PSRelationship;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.services.notification.PSMessageQueueListenerAdapter;
import com.percussion.services.touchitem.IPSTouchItemService;
import com.percussion.services.touchitem.PSTouchItemLocator;
import com.percussion.services.touchitem.data.PSTouchItemData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Listens for Touching Messages.
 * 
 * @author adamgent
 *
 */
public class PSTouchItemQueueListener extends PSMessageQueueListenerAdapter<PSTouchItemData>
{

   /**
    * {@inheritDoc}
    */
   public void onMessage(PSTouchItemData object)
   {
      PSRelationship rel = object.getRelationship();
      int action = object.getAction();
      IPSRequestContext req = new PSRequestContext(PSRequest.getContextForRequest());

      switch (action)
      {
         case RS_PRE_CONSTRUCTION :
            ms_logger.debug("Info construction");
            touchItems(req, rel);
            break;
            
         case RS_PRE_DESTRUCTION :
            ms_logger.debug("Info destroy");
            updateSiteItems(req,rel);
            touchItems(req, rel);
            break;
            
         case RS_PRE_UPDATE :
            ms_logger.debug("Info updating");
            updateSiteItems(req,rel);
            touchItems(req, rel);
            break;
      }
   }

   /**
    * Touches items based on the request and relationship.
    * @param req not null.
    * @param rel not null.
    * @see IPSTouchItemService#touchItems(IPSRequestContext, PSRelationship)
    */
   private void touchItems(IPSRequestContext req, PSRelationship rel)
   {
      PSTouchItemLocator.getTouchItemService().touchItems(req, rel);
   }
   
   /**
    * See {@link IPSTouchItemService#updateSiteItems(IPSRequestContext, PSRelationship)}.
    * 
    * @param req not null.
    * @param rel not null.
    * @see IPSTouchItemService#updateSiteItems(IPSRequestContext, PSRelationship)
    */
   private void updateSiteItems(IPSRequestContext req, PSRelationship rel)
   {
      PSTouchItemLocator.getTouchItemService().updateSiteItems(req, rel);
   }
   
   @Override
   public Class<PSTouchItemData> getType()
   {
      return PSTouchItemData.class;
   }
   
   /**
    * The logger for this class.
    */
   private static final Logger ms_logger = LogManager.getLogger(PSTouchItemQueueListener.class);

}
