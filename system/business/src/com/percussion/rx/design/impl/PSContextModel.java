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

import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.utils.guid.IPSGuid;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

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
   private static Logger ms_logger = Logger.getLogger("PSContextModel");
   
 
}
