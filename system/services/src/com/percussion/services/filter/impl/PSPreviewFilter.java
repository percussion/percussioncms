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

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.Validate.noNullElements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
      List<IPSFilterItem> rval = new ArrayList<>();
      
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
   private static final Logger ms_log = LogManager.getLogger(PSPreviewFilter.class);

}
