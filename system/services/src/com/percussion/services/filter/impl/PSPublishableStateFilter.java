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
