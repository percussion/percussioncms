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
package com.percussion.analytics.service.impl;

import com.percussion.analytics.data.IPSAnalyticsQueryResult;
import com.percussion.analytics.error.PSAnalyticsProviderException;
import com.percussion.analytics.service.IPSAnalyticsProviderQueryService;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.date.PSDateRange;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author erikserating
 *
 */
@PSSiteManageBean("analyticsProviderQueryService")
public class PSAnalyticsProviderQueryService
         implements
            IPSAnalyticsProviderQueryService
{
    @Autowired
   public PSAnalyticsProviderQueryService(IPSAnalyticsProviderQueryHandler handler)
   {
      this.handler = handler;
   }
   
   /* (non-Javadoc)
    * @see com.percussion.analytics.service.IPSAnalyticsProviderQueryService#getPageViewsByPathPrefix(
    *   java.lang.String, java.lang.String, com.percussion.utils.date.PSDateRange)
    */
   public List<IPSAnalyticsQueryResult> getPageViewsByPathPrefix(
            String sitename, String pathPrefix, PSDateRange range)
           throws PSAnalyticsProviderException, IPSGenericDao.LoadException, PSValidationException {
      return handler.getPageViewsByPathPrefix(sitename, pathPrefix, range);
   }

   /* (non-Javadoc)
    * @see com.percussion.analytics.service.IPSAnalyticsProviderQueryService#getVisitsViewsBySite(
    *   java.lang.String, com.percussion.utils.date.PSDateRange)
    */
   public List<IPSAnalyticsQueryResult> getVisitsViewsBySite(String sitename,
            PSDateRange range) throws PSAnalyticsProviderException, IPSGenericDao.LoadException, PSValidationException {
      return handler.getVisitsViewsBySite(sitename, range);
   }
   
   IPSAnalyticsProviderQueryHandler handler;

}
