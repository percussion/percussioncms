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
package com.percussion.services.publisher.impl;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.contentmgr.data.PSQueryResult;
import com.percussion.services.contentmgr.data.PSRowComparator;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.IPSSiteItem;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This generator returns all the items published for the site. This is used
 * as the source list for unpublishing. 
 * 
 * @author dougrand
 */
public class PSSiteItemsGenerator extends PSBaseGenerator
{
   /**
    * The publisher service
    */
   private static IPSPublisherService ms_pubsvc = PSPublisherServiceLocator
         .getPublisherService();

   /**
    * The guid manager
    */
   private static IPSGuidManager ms_gmgr = PSGuidManagerLocator.getGuidMgr();


   @SuppressWarnings("unused")
   public QueryResult generate(Map<String, String> parameters)
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSQueryResult qr = createQueryResult();
      String siteidstr = parameters.get(IPSHtmlParameters.SYS_SITEID);
      String contextstr = parameters.get(IPSHtmlParameters.SYS_CONTEXT);
      String unpublishflags = parameters.get("sys_unpublishFlags");
      if (StringUtils.isBlank(siteidstr) || StringUtils.isBlank(contextstr))
      {
         ms_log.warn("sys_siteid and sys_context are required parameters");
         return qr;
      }
      int context = Integer.parseInt(contextstr);
      IPSGuid siteid = ms_gmgr.makeGuid(siteidstr, PSTypeEnum.SITE);
      Collection<IPSSiteItem> items = ms_pubsvc.findSiteItems(siteid, context);
      // Create potential unpublish list
      List<IPSFilterItem> potentials = new ArrayList<>();
      for (IPSSiteItem item : items)
      {
         int contentid = item.getContentId();
         // Skip purged items
         if (cms.loadComponentSummary(contentid) == null) continue;
         addToResults(qr, contentid);
      }
      return qr;
   }
}
