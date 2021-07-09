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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.filter.impl;

import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.IPSFilterServiceErrors;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.exceptions.PSORMException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Filter content by the publishable state of the workflow state
 * 
 * @author dougrand
 */
public class PSPublishableStateFilter extends PSBaseFilter
{

   @Override
   public List<IPSFilterItem> filter(List<IPSFilterItem> items, Map<String, String> params)
         throws PSFilterException
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      String flagstr = params.get("sys_flagValues");
      if (StringUtils.isBlank(flagstr))
      {
         throw new PSFilterException(IPSFilterServiceErrors.ARGUMENT_MISSING, 
               null, "sys_flagValues", "sys_filterByFolderPaths");
      }
      List<String> flags = new ArrayList<>();
      
      for(String f : flagstr.split(","))
      {
         flags.add(f);
      }
      
      try
      {
         return cms.filterItemsByPublishableFlag(items, flags);
      }
      catch (PSORMException e)
      {
         throw new PSFilterException(IPSFilterServiceErrors.DATABASE, e);
      }
   }

}
