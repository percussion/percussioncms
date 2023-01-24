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
