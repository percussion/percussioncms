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

package com.percussion.activity.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.analytics.service.IPSAnalyticsProviderQueryService;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

/**
 * A request object used for getting the traffic details data from the rest service. 
 */
@JsonRootName(value = "TrafficDetailsRequest")
public class PSTrafficDetailsRequest implements Serializable  
{
	
	/**
     * Default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * @return path folder path to site folder
     */
    public String getPath()
    {
        return path;
    }
    
    /**
     * @return startDate of the date range used for content traffic query.
     */
    public String getStartDate()
    {
        return startDate;
    }
    
    /**
     * @return endDate of the date range used for content traffic query.
     */
    public String getEndDate()
    {
        return endDate;
    }
    
    /**
     * @return returns usage for analytics.  Default is uniquepageviews.
     */
    public String getUsage()
    {
        if(StringUtils.equals(usage, IPSAnalyticsProviderQueryService.FIELD_PAGEVIEWS))
        {
            return IPSAnalyticsProviderQueryService.FIELD_PAGEVIEWS;
        }

        return IPSAnalyticsProviderQueryService.FIELD_UNIQUE_PAGEVIEWS;
    }

    /**
     * Sets analytics usage to pageviews or uniquepageviews.  
     * If not set or set to any other value default is uniquepageviews.
     * @param usage
     */
    public void setUsage(String usage)
    {
        this.usage = usage;
    }
    
    /**
     * Set path folder path to site folder. Required.
     * @param path 
     */
    public void setPath(String path)
    {
        this.path = path;
    }
    
    /**
     * Start date of the date range used for content traffic query. Required.
     * @param startDate
     */
    public void setStartDate(String startDate)
    {
        this.startDate = startDate;
    }
    
    /**
     * End date of the date range used for content traffic query. Required.
     * @param endDate
     */
    public void setEndDate(String endDate)
    {
        this.endDate = endDate;
    }
    
    //See getters for javadoc
    private String path;
	private String startDate;
	private String endDate;
	private String usage;
}
