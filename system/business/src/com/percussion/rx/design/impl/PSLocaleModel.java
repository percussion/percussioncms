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
