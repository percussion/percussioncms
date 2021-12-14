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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.delivery.metadata.rdbms.impl;

import com.percussion.delivery.metadata.IPSCookieConsent;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

/**
 * 
 * @author chriswright
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSCookieConsent")
@Table(name = "PERC_COOKIE_CONSENT")
public class PSDbCookieConsent implements IPSCookieConsent, Serializable {

    @Id
    @GeneratedValue
    @Column(name = "CONSENT_ID")
    private long consentId;

    @Basic
    @Column(length = 100,
            name = "IP_ADDRESS")
    private String ip;

    @Basic
    @Column(length = 2000,
            name = "SERVICE_NAME")
    private String serviceName;
    
    @Basic
    @Column(length = 255,
            name = "SITE_NAME")
    private String siteName;
    
    @Basic
    @Column(name = "OPT_IN")
    private boolean optIn;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CONSENT_DATE")
    private Date consentDate;
    
    public PSDbCookieConsent() {}
    
    public PSDbCookieConsent(String siteName, String serviceName,
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (consentId ^ (consentId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PSDbCookieConsent other = (PSDbCookieConsent) obj;
        if (consentId != other.consentId)
            return false;
        return true;
    }
    
}
