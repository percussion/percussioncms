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
