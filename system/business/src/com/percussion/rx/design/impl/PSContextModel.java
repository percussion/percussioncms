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

import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.utils.guid.IPSGuid;

import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The design model manages {@link IPSPublishingContext} objects.
 * The service of the model is wired by Spring framework.
 *
 * @see PSDesignModel
 * @author YuBingChen
 */
public class PSContextModel extends PSDesignModel
{
   @Override
   public Collection<String> findAllNames()
   {
      Map<Integer, String> idNameMap = getSiteMgr().getContextNameMap();
      return idNameMap.values();
   }

   /**
    * Returns the Site Manager service, which is wired by the Spring framework.
    * @return the service object, never <code>null</code>.
    */
   private IPSSiteManager getSiteMgr()
   {
      return (IPSSiteManager) getService();
   }
   
   @Override
   public void delete(IPSGuid guid)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      IPSPublishingContext cxt = null;
      try
      {
         cxt = getSiteMgr().loadContextModifiable(guid);
      }
      catch (Exception e) 
      {
         ms_logger.info("Failed to load the context object for the " +
               "supplied guid. Skipping the deletion.",e);
         return;
      }
      if(cxt != null)
      {
         getSiteMgr().deleteContext(cxt);
      }
   }

   /**
    * The logger for this class.
    */
   private static final Logger ms_logger = LogManager.getLogger("PSContextModel");
   
 
}
