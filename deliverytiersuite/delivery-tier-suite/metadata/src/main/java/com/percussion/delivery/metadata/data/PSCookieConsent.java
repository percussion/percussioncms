/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.delivery.metadata.data;

import com.percussion.delivery.metadata.IPSCookieConsent;

import java.util.Date;
import java.util.Optional;

/**
 * Provides model for cookie consent
 * entries.  Statistics include:
 * <ul>
 * <li>siteName</li>
 * <li>IP</li>
 * <li>serviceName</li>
 * <li>optIn</li>
 * <li>consentDate</li>
 * </ul>
 * 
 * @author chriswright
 *
 */
public class PSCookieConsent implements IPSCookieConsent {
    
    private String siteName;
    private String ip;
    private String serviceName;
    private boolean optIn;
    private Date consentDate;

    public PSCookieConsent(){}
    public PSCookieConsent(String siteName, String serviceName,
            Date consentDate, String ip, boolean optIn) {
        
        if (siteName == null)
            throw new IllegalArgumentException("siteName may not be null");
        if (serviceName == null)
            throw new IllegalArgumentException("serviceName may not be null");
        if (consentDate == null)
            throw new IllegalArgumentException("consentDate may not be null");
        if (ip == null)
            throw new IllegalArgumentException("ip may not be null");
        
        setSiteName(siteName);
        setService(serviceName);
        setConsentDate(consentDate);
        setIP(ip);
        setOptIn(optIn);
    }

    @Override
    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    @Override
    public String getSiteName() {
        return siteName;
    }

    @Override
    public void setIP(String ip) {
        this.ip = ip;
    }

    @Override
    public String getIP() {
        return ip;
    }
    
    @Override
    public void setConsentDate(Date consentDate) {
        this.consentDate = Optional
                .ofNullable(consentDate)
                .map(Date::getTime)
                .map(Date::new)
                .orElse(null);
    }

    @Override
    public Date getConsentDate() {
        return Optional
                .ofNullable(consentDate)
                .map(Date::getTime)
                .map(Date::new)
                .orElse(null);
    }

    @Override
    public void setService(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String getService() {
        return serviceName;
    }

    @Override
    public void setOptIn(boolean optIn) {
        this.optIn = optIn;
    }

    @Override
    public boolean getOptIn() {
        return optIn;
    }

}
