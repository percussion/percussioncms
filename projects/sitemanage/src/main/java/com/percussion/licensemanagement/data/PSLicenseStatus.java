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
package com.percussion.licensemanagement.data;

import static com.percussion.share.dao.PSDateUtils.getDateFromString;
import static com.percussion.share.dao.PSDateUtils.getDateToString;

import com.percussion.licensemanagement.service.impl.PSLicenseService;
import com.percussion.share.dao.PSDateUtils;
import com.percussion.share.service.IPSDataService.DataServiceLoadException;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlType(name = "", propOrder =
{"company", "licenseType", "status", "licenseStatus", "activationStatus", "maxSites", "maxPages", "licenseId",
        "currentSites", "currentPages", "lastRefresh", "usageExceeded", "serverUUID"})
@XmlRootElement(name = "licenseStatus")
public class PSLicenseStatus implements Serializable
{
    private String company;

    private String licenseType;

    private String status;

    private String licenseStatus;

    private Boolean activationStatus;

    private Integer maxSites;

    private Integer maxPages;

    private Integer currentSites;

    private Integer currentPages;

    private String licenseId;

    private Date lastRefresh;
    
    private Date usageExceeded;

    private String serverUUID;

    /**
     * @return the serverUUID
     */
    public String getServerUUID()
    {
        return serverUUID;
    }

    /**
     * @param serverUUID the serverUUID to set
     */
    public void setServerUUID(String serverUUID)
    {
        this.serverUUID = serverUUID;
    }

    /**
     * @return the company
     */
    public String getCompany()
    {
        return company;
    }

    /**
     * @param company the company to set
     */
    public void setCompany(String company)
    {
        this.company = company;
    }

    /**
     * @return the licenseType
     */
    public String getLicenseType()
    {
        return licenseType;
    }

    /**
     * @param licenseType the licenseType to set
     */
    public void setLicenseType(String licenseType)
    {
        this.licenseType = licenseType;
    }

    /**
     * @return status a user friendly license status to be displayed in client
     *         side.
     */
    public String getStatus()
    {
        if (activationStatus != null)
        {
            if (StringUtils.equalsIgnoreCase(licenseStatus, PSLicenseService.CUSTOM_STATUS_SUSPENDED_REFRESH))
            {
                this.status = PSLicenseService.LICENSE_STATUS_SUSPENDED_REFRESH;
            }
            else if (StringUtils.equalsIgnoreCase(licenseStatus, PSLicenseService.NETSUITE_STATUS_SUSPENDED))
            {
                this.status = PSLicenseService.LICENSE_STATUS_SUSPENDED;
            }            
            else if (StringUtils.equalsIgnoreCase(licenseStatus, PSLicenseService.CUSTOM_STATUS_ACTIVE_OVERLIMIT))
            {
                this.status = PSLicenseService.LICENSE_STATUS_ACTIVE_OVERLIMIT;
            }
            else if (activationStatus)
            {
                this.status = PSLicenseService.LICENSE_STATUS_ACTIVE;
            }
            else if (StringUtils.equalsIgnoreCase(licenseStatus, PSLicenseService.NETSUITE_STATUS_REGISTERED))
            {
                this.status = PSLicenseService.LICENSE_STATUS_REGISTERED;
            }
            else
            {
                this.status = PSLicenseService.LICENSE_STATUS_INACTIVE;
            }
        }
        else
        {
            this.status = PSLicenseService.LICENSE_STATUS_INACTIVE;
        }
        return this.status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * @return the licenseStatus
     */
    public String getLicenseStatus()
    {
        return licenseStatus;
    }

    /**
     * @param licenseStatus the licenseStatus to set
     */
    public void setLicenseStatus(String licenseStatus)
    {
        this.licenseStatus = licenseStatus;
    }

    /**
     * @return the activationStatus
     */
    public Boolean getActivationStatus()
    {
        return activationStatus;
    }

    /**
     * @param activationStatus the activationStatus to set
     */
    public void setActivationStatus(Boolean activationStatus)
    {
        this.activationStatus = activationStatus;
    }

    /**
     * @return the maxSites
     */
    public Integer getMaxSites()
    {
        return maxSites;
    }

    /**
     * @param maxSites the maxSites to set
     */
    public void setMaxSites(Integer maxSites)
    {
        this.maxSites = maxSites;
    }

    /**
     * @return the maxPages
     */
    public Integer getMaxPages()
    {
        return maxPages;
    }

    /**
     * @param maxPages the maxPages to set
     */
    public void setMaxPages(Integer maxPages)
    {
        this.maxPages = maxPages;
    }

    /**
     * @return the licenseId
     */
    public String getLicenseId()
    {
        return licenseId;
    }

    /**
     * @param licenseId the licenseId to set
     */
    public void setLicenseId(String licenseId)
    {
        this.licenseId = licenseId;
    }

    /**
     * @return the currentSites
     */
    public Integer getCurrentSites()
    {
        return currentSites;
    }

    /**
     * @param currentSites the currentSites to set
     */
    public void setCurrentSites(Integer currentSites)
    {
        this.currentSites = currentSites;
    }

    /**
     * @return the currentPages
     */
    public Integer getCurrentPages()
    {
        return currentPages;
    }

    /**
     * @param currentPages the currentPages to set
     */
    public void setCurrentPages(Integer currentPages)
    {
        this.currentPages = currentPages;
    }

    /**
     * @return the lastRefresh
     */
    @XmlElement(name = "lastRefresh")
    public String getLastRefresh()
    {
        return getDateToString(this.lastRefresh);
    }

    /**
     * @return the lastRefresh
     */
    public Date getLastRefreshDate()
    {
        return this.lastRefresh;
    }

    /**
     * @param lastRefresh the lastRefresh to set
     */
    public void setLastRefresh(Date lastRefresh)
    {
        this.lastRefresh = lastRefresh;
    }

    public void setLastRefresh(String lastRefresh) throws DataServiceLoadException {
        Date formattedDate;
        try {
            formattedDate = getDateFromString(lastRefresh);
        } catch (ParseException e) {
            throw new DataServiceLoadException("Error parsing date", e);
        }
        this.lastRefresh = formattedDate;
    }
    
    @XmlElement(name = "usageExceeded")
    public Integer getUsageExceeded()
    {
        // If it's not activated. Eg: it's "inactive, registered" do not return the amount of days
        if (this.usageExceeded == null || !this.activationStatus) {
            return null;
        }
        return PSDateUtils.getDaysDiff(this.usageExceeded, new Date());
    }
    
    public Date getUsageExceededDate()
    {
        return this.usageExceeded;
    }
    
    public void setUsageExceeded(Date usageExceededDate)
    {
        this.usageExceeded = usageExceededDate;
    }
}
