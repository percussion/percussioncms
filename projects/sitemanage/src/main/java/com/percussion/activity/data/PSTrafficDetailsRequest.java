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
