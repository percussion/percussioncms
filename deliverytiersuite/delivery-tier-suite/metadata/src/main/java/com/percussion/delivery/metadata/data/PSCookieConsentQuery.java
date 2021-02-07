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

package com.percussion.delivery.metadata.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

/**
 * Maps JavaScript http request data object to a
 * PSCookieConsent object.  This class includes a
 * variable 'services' which is needed to map all
 * cookies approved by the client to unique DB entries
 * per approved cookie.
 * 
 * @author chriswright
 *
 */
@XmlRootElement(name="consent")
public class PSCookieConsentQuery extends PSCookieConsent {
    
    private List<String> services;

    public PSCookieConsentQuery(){
        super();
    }
    
    /**
     * Constructor used to call {@link PSCookieConsent} super
     * and create PSCookieConsentQuery.
     * 
     * @param siteName name of the site accessed.
     * @param consentDate date the cookie consent was given.
     * @param optIn <code>true</code> always.  No information is saved
     * if user does not opt in.
     * @param services the list of services/cookies approved by the client.
     */
    @JsonCreator
    public PSCookieConsentQuery(@JsonProperty("siteName") String siteName,
            @JsonProperty("consentDate") Date consentDate,
            @JsonProperty("optIn") boolean optIn,
            @JsonProperty("services") List<String> services) {
        // Here "undefined" is passed for the serviceName as
        // we set each service name later with each service
        // that has been passed in with the 'services' parameter.
        // IP is undefined as that is set server side later.
        super(siteName, "undefined", consentDate, "undefined", optIn);
        
        if (services == null)
            throw new IllegalArgumentException("List of cookies approved by client may not be null.");
        
        setServices(services);
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }
    
    @Override
    public String toString() {
        return String.format("Site name: %s - IP: %s - Consent Date: %s - Services: %s",
                getSiteName(), getIP(), getConsentDate(), this.services);
    }
}
