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

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;



/**
 * This object holds traffic activity details of the items under named site by date.
 */
@JsonRootName(value = "ContentTraffic")
public class PSContentTraffic 
{
	public PSContentTraffic()
	{
		
	}
	
    public PSContentTraffic(String site)    
    {
        setSite(site);
    }
    
    /**
     * @return site name used for content traffic query.
     */
    public String getSite()
    {
        return site;
    }
    
    /**
     * @return site id used for content traffic query.
     */
    public String getSiteId()
    {
        return siteId;
    }
    
    /**
     * 
     * @return startDate used for content traffic query.
     */
    public String getStartDate()
    {
        return startDate;
    }
    
    /**
     * @return endDate used for content traffic query.
     */
    public String getEndDate()
    {
        return endDate;
    }
    /**
     * @return visits total by date interval. Value in array index matches up 
     * date in the date array at the same index. 
     */
    public List<Integer> getVisits()
    {
        return visits;
    }
    
    /**
     * @return newPages total by date interval. Value in array index matches up 
     * date in the date array at the same index. 
     */
    public List<Integer> getNewPages()
    {
        return newPages;
    }
    
    /**
     * @return pageUpdates total by date interval. Value in array index matches 
     * up date in the date array at the same index. 
     */
    public List<Integer> getPageUpdates()
    {
        return pageUpdates;
    }
    
    /**
     * @return takeDowns total by date interval. Value in array index matches up 
     * date in the date array at the same index. 
     */
    public List<Integer> getTakeDowns()
    {
        return takeDowns;
    }
    
    /**
     * @return livePages total by date interval. Value in array index matches up 
     * date in the date array at the same index. 
     */
    public List<Integer> getLivePages()
    {
        return livePages;
    }
    
    /**
     * @return dates by date interval. Dates in array matches up with values in  
     * all other list in this object.
     */
    public List<String> getDates()
    {
        return dates;
    }
    
    /**
     * @return updateTotals sum of newPages, pageUpdates and takeDowns at the 
     * same index point in array.
     */
    public List<Integer> getUpdateTotals()
    {
        return updateTotals;
    }
    
    /**
     * Sets the site that is used for query of content Traffic.
     * @param site Required.
     */
    public void setSite(String site)
    {
        this.site = site;
    }
    
    /**
     * Sets the site id that is used for query of content Traffic.
     * @param siteId Required.
     */
    public void setSiteId(String siteId)
    {
        this.siteId = siteId;
    }
    
    /**
     * Sets the startDate used for content traffic query.
     * @param startDate Required.
     */
    public void setStartDate(String startDate)
    {
        this.startDate = startDate;
    }
    
    /**
     * Sets the endDate used for content traffic query.
     * @param endDate Required.
     */
    public void setEndDate(String endDate)
    {
        this.endDate = endDate;
    }
    
    /**
     * Sets map of visits to a site based on google analytics.
     * Value in array index matches up date in the date array at the same index. 
     * @param visits
     */
    public void setVisits(List<Integer> visits)
    {
        this.visits = visits;
    }
    
    /**
     * Sets map of new pages to a site.
     * Value in array index matches up date in the date array at the same index. 
     * @param newPages
     */
    public void setNewPages(List<Integer> newPages)
    {
        this.newPages = newPages;
    }
    
    /**
     * Sets map of updated pages to a site.
     * Value in array index matches up date in the date array at the same index. 
     * @param pageUpdates
     */
    public void setPageUpdates(List<Integer> pageUpdates)
    {
        this.pageUpdates = pageUpdates;
    }
    
    /**
     * Sets map of unpublished pages to a site.
     * Value in array index matches up date in the date array at the same index. 
     * @param takeDowns
     */
    public void setTakeDowns(List<Integer> takeDowns)
    {
        this.takeDowns = takeDowns;
    }
    
    /**
     * Sets map of published pages to a site.
     * Value in array index matches up date in the date array at the same index. 
     * @param livePages
     */
    public void setLivePages(List<Integer> livePages)
    {
        this.livePages = livePages;
    }
    
    /**
     * Set dates for traffic content based on granularity.  Index of list needs 
     * to match up with values set on all other list in this object.
     * @param dates
     */
    public void setDates(List<String> dates)
    {
        this.dates = dates;
    }
    
    /**
     * Set sum of newPages, pageUpdates and takeDowns at the same index point in array.
     * @param updateTotals
     */
    public void setUpdateTotals(List<Integer> updateTotals)
    {
        this.updateTotals = updateTotals;
    }
    
    //See getters for javadoc
    private String site;
    private String siteId;
    private String startDate;
    private String endDate;
    private List<String> dates;
    private List<Integer> visits;
    private List<Integer> newPages;
    private List<Integer> pageUpdates;
    private List<Integer> takeDowns;
    private List<Integer> livePages;
    private List<Integer> updateTotals;
        
}
