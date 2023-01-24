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

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This filter adjusts the IDs of the specified items to select the public 
 * revision or current revision of the content item. The current revision 
 * is returned if the item has never been public, otherwise the public 
 * revision is used.
 * 
 * @author dougrand
 *
 */
public class PSPublicFilter extends PSBaseFilter
{

   @Override
   public List<IPSFilterItem> filter(List<IPSFilterItem> items,
         Map<String, String> params) throws PSFilterException
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      List<IPSFilterItem> rval = new ArrayList<>();
      for(IPSFilterItem item : items)
      {
         PSLegacyGuid guid = (PSLegacyGuid) item.getItemId();
         PSComponentSummary sum = 
            cms.loadComponentSummary(guid.getContentId());
         int rev = sum.getPublicOrCurrentRevision();
         if (rev != guid.getRevision())
         {
            item = item.clone(new PSLegacyGuid(guid.getContentId(), rev));
         }
         rval.add(item);
      }
      return rval;
   }

}
