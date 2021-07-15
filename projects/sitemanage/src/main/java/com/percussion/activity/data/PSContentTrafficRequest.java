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

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A request object used for getting the content traffic data from the rest service. 
 */
@JsonRootName(value = "ContentTrafficRequest")
public class PSContentTrafficRequest extends PSTrafficDetailsRequest implements Serializable  
{
	
	/**
     * Default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Get granularity of date list.
     * @return Option returned is DAYS,WEEKS,MONTHS,or YEARS.
     */
    public String getGranularity()
    {
        return granularity;
    }
    
    /**
     * @return trafficRequested list of types of date that is getting requested.
     * Options are: livePages,pageUpdates,newPages,takeDowns,visits
     */
    public List<String> getTrafficRequested()
    {
        return trafficRequested;
    }
    
    /**
     * Sets granularity of date list returned.  Options are DAYS,WEEKS,MONTHS,YEARS.
     * @param granularity
     */
    public void setGranularity(String granularity)
    {
        this.granularity = granularity;
    }
    
    /**
     * List of traffic data types that is getting requested.
     * Options are: livePages,pageUpdates,newPages,takeDowns,visits
     * @param trafficRequested
     */
    public void setTrafficRequested(List<String> trafficRequested)
    {
        this.trafficRequested = trafficRequested;
    }
    
    //See getters for javadoc
	private String granularity;
	private List<String> trafficRequested;
 
}
