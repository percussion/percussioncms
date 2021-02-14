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
package com.percussion.sitemanage.data;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotEqual;
import net.sf.oval.constraint.NotNull;

import com.percussion.share.data.PSAbstractDataObject;

/**
 * @author DavidBenua
 *
 */
@XmlRootElement(name = "SitePublishJob")
public class PSSitePublishJob extends PSAbstractDataObject {
	
	/**
	 * Job Id for publishing job. Unique and never <code>null</code>. 
	 */
	@NotEqual(value = "0")
	private long jobId;
	/**
	 * Site name
	 */
	@NotBlank
	@NotNull
	private String siteName;
	
	/**
	 * Site id
	 */
	@NotBlank
	@NotNull
	private String siteId;
	
	/**
	 * Job status
	 */
	private String status;
	
	/**
	 * Starting time as formatted string (hh:MM A) 
	 */
	private String startTime;
	
	/**
	 * Starting date as formatted string (mm/DD/yyyy)
	 */
	private String startDate;
	
	/**
	 * Id of the server where it is being published.
	 */
	private long pubServerId;
	
	/**
	 * Name of the server where it is being published.
	 */
	private String pubServerName;
	
	/**
	 * Elapsed time in milliseconds
	 */
	private long elapsedTime;  
	
	/**
	 * Total items in this job
	 */
	private long totalItems;
	
	/**
	 * Completed items in this job
	 */
	private long completedItems;
	
	private long failedItems;
	
	private long removedItems;
	
	private Boolean isStopping;
	/**
	 * @return the jobId
	 */
	public long getJobId() {
		return jobId;
	}
	/**
	 * @param jobId the jobId to set
	 */
	public void setJobId(long jobId) {
		this.jobId = jobId;
	}
	/**
	 * @return the siteName
	 */
	public String getSiteName() {
		return siteName;
	}
	/**
	 * @param siteName the siteName to set
	 */
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	/**
	 * @return the siteId
	 */
	public String getSiteId(){
	    return siteId;
	}
	/**
	 * @param siteId the siteId to set
	 */
	public void setSiteId(String siteId) {
	    
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the startTime
	 */
	public String getStartTime() {
		return startTime;
	}
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	/**
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}
	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	/**
     * @return the pubServerId
     */
    public long getPubServerId()
    {
        return pubServerId;
    }
    /**
     * @param pubServerId the pubServerId to set
     */
    public void setPubServerId(long pubServerId)
    {
        this.pubServerId = pubServerId;
    }
    /**
     * @return the pubServerName
     */
    public String getPubServerName()
    {
        return pubServerName;
    }
    /**
     * @param pubServerName the pubServerName to set
     */
    public void setPubServerName(String pubServerName)
    {
        this.pubServerName = pubServerName;
    }
    /**
	 * @return the elapsedTime
	 */
	public long getElapsedTime() {
		return elapsedTime;
	}
	/**
	 * @param elapsedTime the elapsedTime to set
	 */
	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
	/**
	 * @return the totalItems
	 */
	public long getTotalItems() {
		return totalItems;
	}
	/**
	 * @param totalItems the totalItems to set
	 */
	public void setTotalItems(long totalItems) {
		this.totalItems = totalItems;
	}
	/**
	 * @return the completedItems
	 */
	public long getCompletedItems() {
		return completedItems;
	}
	/**
	 * @param completedItems the completedItems to set
	 */
	public void setCompletedItems(long completedItems) {
		this.completedItems = completedItems;
	}
	public long getFailedItems() {
		return failedItems;
	}
	public void setFailedItems(long failedItems) {
		this.failedItems = failedItems;
	}
	public long getRemovedItems() {
		return removedItems;
	}
	public void setRemovedItems(long removedItems) {
		this.removedItems = removedItems;
	}
	
    /**
     * @return the isStopping
     */
    public Boolean getIsStopping()
    {
        return isStopping;
    }
    /**
     * @param isStopping the isStopping to set
     */
    public void setIsStopping(Boolean isStopping)
    {
        this.isStopping = isStopping;
    } 	
}
