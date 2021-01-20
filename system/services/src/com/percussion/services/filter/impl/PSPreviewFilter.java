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

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.Validate.noNullElements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.webservices.PSWebserviceUtils;

/**
 * This filter adjusts the ids to select the edit revision or current revision
 * of the content item. The user's name is passed as sys_user from assembly. If
 * the user's name is not provided, then the username will be retrieved from the current
 * thread. If no user name is found or the user name does not match the checked out name 
 * then only the current revision is considered.
 * 
 * @author dougrand
 *
 */
public class PSPreviewFilter extends PSBaseFilter
{

   @Override
   public List<IPSFilterItem> filter(List<IPSFilterItem> items,
         Map<String, String> params) throws PSFilterException
   {
      noNullElements(items, "items");
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      IPSGuidManager gm = PSGuidManagerLocator.getGuidMgr();
      List<IPSFilterItem> rval = new ArrayList<IPSFilterItem>();
      
      String user = getUserName(params);
      
      for(IPSFilterItem item : items)
      {
         PSLocator cloc = gm.makeLocator(item.getItemId());
         if (cloc.getRevision() > 0)
         {
            // revision is known, keep it
            rval.add(item);
            continue;
         }
         
         // revision is unknown, find one
         PSComponentSummary sum = 
            cms.loadComponentSummary(cloc.getId());
         if ( sum == null ) {
            ms_log.error("Preview Filter failed to find content item: " + item.getItemId());
            /*
             * We skip the bad item.
             */
            continue;
         }
         int rev = sum.getAAViewableRevision(user);
         if (rev != cloc.getRevision())
         {
            PSLocator nloc = new PSLocator(cloc.getId(), rev);
            item = item.clone(gm.makeGuid(nloc));
         }
         rval.add(item);
      }
      return rval;
   }
   
   /**
    * Get the user name from the parameters and if its null then see if its thread-local.
    * @param params never <code>null</code>.
    * @return maybe <code>null</code>.
    */
   private String getUserName(Map<String, String> params) {
      /*
       * Adam Gent: fix for Inline link generation for items in quick edit.
       */
      String user = params.get(IPSHtmlParameters.SYS_USER);
      if (isBlank(user)) {
         try
         {
            user = PSWebserviceUtils.getUserName();
         }
         catch (Exception e)
         {
            ms_log.debug("Failed to get user for generating preview link",e);
         }
      }
      return user;
   }
   
   
   /**
    * The log instance to use for this class, never <code>null</code>.
    */
   private static final Log ms_log = LogFactory.getLog(PSPreviewFilter.class);

}
