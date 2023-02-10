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
