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
package com.percussion.delivery.metadata;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.percussion.delivery.metadata.data.PSCookieConsentQuery;
import com.percussion.delivery.metadata.data.PSVisitQuery;

public interface IPSBlogPostVisitService
{
    public static final int INTIAL_DELAY_SECONDS = 0;
    public static final int SAVE_INTERVAL_SECONDS = 60;

    /**
     * Returns top visited pages within the given time period
     * 
	 * @param visitQuery visit query object
     * @return List of IPSMetadataEntry objects
     * @throws Exception on query parsing error
     */
    public List<String> getTopVisitedBlogPosts(PSVisitQuery visitQuery) throws Exception;

    /**
     * Returns top visited pages within the given time period
     * 
     * @param timePeriod if <code>null</code> defaults to WEEK
     * @param limit limits the results to this number
     * @return List of IPSMetadataEntry objects
     * @throws Exception on query parsing error
     */
    public void trackBlogPost(String pagePath);
    
    /**
     * Tracks a cookie consent query.  This method is added
     * to the blog post visit service to piggyback off of
     * the existing Runnable to avoid expenses of creating a
     * new thread to post updates in bulk.
     * @param query - obj with values to save.
     */
    public void logCookieConsentEntry(PSCookieConsentQuery query);
    
    public void delete(Collection<String> pagepaths);

    public int convertToLimit(String limit);

    public boolean visitSchedulerStatus();

    public void updatePostsAfterSiteRename(String prevSiteName, String newSiteName);
    
    public void startScheduler() throws Exception;
    
    public enum TIMEPERIOD
    {
        TODAY(1), WEEK(7), MONTH(30), YEAR(365), ALLTIME(-1);
    	private int days;
    	
    	private TIMEPERIOD (int days) {
    		this.days = days;
    	}
    	
    	public int getDays() {
    		return days;
    	}
    	
    	public static TIMEPERIOD fromName(String timePeriod) {
    		if (StringUtils.isBlank(timePeriod)) {
    			return null;
    		}
    		TIMEPERIOD res = null;
    		for (TIMEPERIOD val : values()) {
    			if (timePeriod.equalsIgnoreCase(val.name())) {
    				res = val;
    				break;
    			}
    		}
    		return res;
    	}
    }

}
