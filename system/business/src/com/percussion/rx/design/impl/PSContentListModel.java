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
package com.percussion.rx.design.impl;

import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.data.PSContentList;
import com.percussion.utils.guid.IPSGuid;

import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PSContentListModel extends PSDesignModel
{
   @Override
   public void delete(IPSGuid guid)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      IPSPublisherService srvc = (IPSPublisherService) getService();
      IPSContentList clist = null;
      try
      {
         clist = (PSContentList) load(guid);
      }
      catch (Exception e) 
      {
         ms_logger.info("Failed to load the content list object for the " +
               "supplied guid. Skipping the deletion.",e);
         return;
      }
      if(clist != null)
      {
         srvc.deleteContentLists(Collections.singletonList(clist));
      }
   }

   /**
    * The logger for this class.
    */
   private static final Logger ms_logger = LogManager.getLogger("PSContentListModel");
   
   
}
