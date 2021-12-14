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
package com.percussion.sitemanage.data;

import com.percussion.share.data.PSAbstractDataObject;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author DavidBenua
 *
 */
@XmlRootElement(name="SitePublishLogRequest")
public class PSSitePublishLogRequest extends PSAbstractDataObject {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String siteId;
    private int days;
	private int maxcount;
	private int skipCount;
	private String pubServerId;
	
    private boolean showOnlyFailures;
	
    /**
	 * @return the id of the site.
	 */
    public String getSiteId()
	{
        return siteId;
    }
	   
	/**
	 * @param siteId the id of the site to be published.
     */
    public void setSiteId(String siteId)
    {
        this.siteId = siteId;
    }
	
	/**
	 * @return the days
	 */
	public int getDays() {
		return days;
	}
	/**
     * @return the pubServerId
     */
    public String getPubServerId()
    {
        return pubServerId;
    }

    /**
     * @param pubServerId the pubServerId to set
     */
    public void setPubServerId(String pubServerId)
    {
        this.pubServerId = pubServerId;
    }
	/**
	 * @param days the days to set
	 */
	public void setDays(int days) {
		this.days = days;
	}
	/**
	 * @return the maxcount
	 */
	public int getMaxcount() {
		return maxcount;
	}
	/**
	 * @param maxcount the maxcount to set
	 */
	public void setMaxcount(int maxcount) {
		this.maxcount = maxcount;
	}
	/**
	 * @return the skipCount
	 */
	public int getSkipCount() {
		return skipCount;
	}
	/**
	 * @param skipCount the skipCount to set
	 */
	public void setSkipCount(int skipCount) {
		this.skipCount = skipCount;
	}
	/**
	 * @return the showOnlyFailures
	 */
	public boolean isShowOnlyFailures() {
		return showOnlyFailures;
	}
	/**
	 * @param showOnlyFailures the showOnlyFailures to set
	 */
	public void setShowOnlyFailures(boolean showOnlyFailures) {
		this.showOnlyFailures = showOnlyFailures;
	}   
	
}
