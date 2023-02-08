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
