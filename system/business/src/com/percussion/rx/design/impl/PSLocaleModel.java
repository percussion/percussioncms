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

import com.percussion.i18n.PSLocale;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.data.PSContentList;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;

import java.text.MessageFormat;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PSLocaleModel extends PSLimitedDesignModel
{
   @Override
   public Object load(IPSGuid guid)
   {
      throw new UnsupportedOperationException(
            "load(IPSGuid) is not currently "
                  + "implemented for design objects of type "
                  + getType().name());
   }

   @Override
   public Object load(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      IPSCmsObjectMgr cmsObjMgr = (IPSCmsObjectMgr) getService();
      PSLocale locale = cmsObjMgr.findLocaleByLanguageString(name);
      if (locale == null)
      {
         String msg = "Failed to get the design object for name {0}";
         Object[] args = { name };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      return locale;
   }

   @Override
   public void delete(IPSGuid guid)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      IPSCmsObjectMgr cmsObjMgr = (IPSCmsObjectMgr) getService();
      PSLocale loc = null;
      try
      {
         loc = cmsObjMgr.loadLocale(guid.getUUID());
      }
      catch (Exception e)
      {
         ms_logger.info("Failed to load the locale object for the "
               + "supplied guid. Skipping the deletion.", e);
         return;
      }
      if (loc != null)
      {
         try
         {
            cmsObjMgr.deleteLocale(loc);
         }
         catch (PSORMException e)
         {
            throw new RuntimeException(
                  "Failed to delete the locale with guid "
                        + guid.toString(), e);
         }
      }

   }

   /**
    * The logger for this class.
    */
   private static final Logger ms_logger = LogManager.getLogger("PSLocaleModel");

}
