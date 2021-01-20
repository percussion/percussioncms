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
      List<IPSFilterItem> rval = new ArrayList<IPSFilterItem>();
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
