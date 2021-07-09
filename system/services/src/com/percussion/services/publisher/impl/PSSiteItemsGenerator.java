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
