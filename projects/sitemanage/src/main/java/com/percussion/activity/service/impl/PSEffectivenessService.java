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
package com.percussion.activity.service.impl;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.activity.data.PSContentActivity;
import com.percussion.activity.data.PSEffectiveness;
import com.percussion.activity.data.PSEffectivenessRequest;
import com.percussion.activity.service.IPSActivityService;
import com.percussion.activity.service.IPSEffectivenessService;
import com.percussion.activity.service.IPSContentActivityService.PSDurationTypeEnum;
import com.percussion.activity.service.IPSContentActivityService.PSUsageEnum;
import com.percussion.analytics.data.IPSAnalyticsQueryResult;
import com.percussion.analytics.error.PSAnalyticsProviderException;
import com.percussion.analytics.service.IPSAnalyticsProviderQueryService;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.utils.date.PSDateRange;
import com.percussion.utils.date.PSDateRange.Granularity;

import java.util.ArrayList;
import java.util.List;

/**
 * The effectiveness data service.  This service provides actual data.
 *  
 * @author peterfrontiero
 */
public class PSEffectivenessService implements IPSEffectivenessService
{
    public PSEffectivenessService(IPSActivityService activityService, IPSAnalyticsProviderQueryService analyticsService)
    {
        this.activityService = activityService;
        this.analyticsService = analyticsService;
    }
    public List<PSEffectiveness> getEffectiveness(PSEffectivenessRequest request, List<PSContentActivity> activity)
        throws PSAnalyticsProviderException
    {
        notNull(request);
        notNull(activity);
                
        List<PSEffectiveness> eList = new ArrayList<>();

        PSDurationTypeEnum durationType = PSDurationTypeEnum.valueOf(request.getDurationType());
        int duration = Integer.parseInt(request.getDuration());
        Granularity granularity = getGranularity(durationType);
        PSDateRange currRange = new PSDateRange(granularity, duration);
        PSDateRange prevRange = new PSDateRange(currRange.getStart(), granularity, duration);
                
        String resultKey = (request.getUsage() == PSUsageEnum.unique_pageviews) ?
                IPSAnalyticsProviderQueryService.FIELD_UNIQUE_PAGEVIEWS :
                    IPSAnalyticsProviderQueryService.FIELD_PAGEVIEWS;

        List<Exception> exceptions = new ArrayList<>();
        for (PSContentActivity ca : activity)
        {
            long changes = (long) ca.getNewItems() + ca.getUpdatedItems();

            try
            {
                // Calculate the effectiveness
                Long currViews = getViews(ca, currRange, resultKey);
                Long prevViews = getViews(ca, prevRange, resultKey);
                Long effectiveness = (currViews - prevViews)/((changes > 0) ? changes : 1);

                eList.add(new PSEffectiveness(ca.getName(), effectiveness));
            }
            catch (PSAnalyticsProviderException | IPSGenericDao.LoadException e)
            {
                exceptions.add(e);
            }
        }
        
        if (!exceptions.isEmpty() && exceptions.size() == activity.size())
        {
            throw new PSAnalyticsProviderException(exceptions.get(0));
        }
     
        return eList;
    }
    
    /**
     * Gets the total number of analytics views for the given content activity during the specified interval.
     * 
     * @param ca the content activity, assumed not <code>null</code>.
     * @param range the date range interval, assumed not <code>null</code>.
     * @param resultKey determines which query result view field to extract, assumed not blank.
     * 
     * @return total number of views.
     * 
     * @throws PSAnalyticsProviderException if an error occurs retrieving the analytics data.
     */
    private Long getViews(PSContentActivity ca, PSDateRange range, String resultKey)
            throws PSAnalyticsProviderException, IPSGenericDao.LoadException {
        long views = 0L;
        
        List<IPSAnalyticsQueryResult> results = analyticsService.getPageViewsByPathPrefix(ca.getSiteName(),
                ca.getPath(), range);
        for (IPSAnalyticsQueryResult result : results)
        {
            views += result.getLong(resultKey);
        }
        
        return views;
    }
    
    /**
     * Maps a duration type to a granularity.
     * 
     * @param durationType assumed not <code>null</code>.
     * 
     * @return the corresponding granularity, never <code>null</code>.
     */
    private Granularity getGranularity(PSDurationTypeEnum durationType)
    {
        Granularity granularity;
        
        switch(durationType)
        {
            case days:
                granularity = Granularity.DAY;
                break;
            case weeks:
                granularity = Granularity.WEEK;
                break;
            case months:
                granularity = Granularity.MONTH; 
                break;
            case years:
                granularity = Granularity.YEAR;
                break;
            default:
                throw new IllegalArgumentException("Invalid duration type.");
        }
        
        return granularity;
    }
    
    private IPSActivityService activityService;
    private IPSAnalyticsProviderQueryService analyticsService;
 
}
