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
package com.percussion.secure.data;

import org.apache.commons.lang.Validate;

public class PSMembershipConfiguration
{
    // set by spring beans config
    private String membershipServiceHost;
    private String membershipServiceProtocol;
    private String membershipServicePort;
    private String membershipSessionCookieName;
    private String useLdap;
    

    /**
     * Set on first access by {@link #getBaseUrl()}, not modified after that.
     */
    private String baseUrl = null;
    
    /**
     * Get the name of the cookie used to store the membership session id.
     * 
     * @return The name, not <code>null</code> or empty.
     */
    public String getMembershipSessionCookieName()
    {
        return membershipSessionCookieName;
    }

    /**
     * Set the host to use to access the membership service.
     * 
     * @param membershipServiceHost The host name, may not be <code>null</code> or empty.
     */
    public void setMembershipServiceHost(String membershipServiceHost)
    {
        Validate.notEmpty(membershipServiceHost);
        this.membershipServiceHost = membershipServiceHost;
    }

    /**
     * Set the protocol to use to access the membership service.
     * 
     * @param membershipServiceProtocol The protocol (http or https), may not be <code>null</code> or empty
     */
    public void setMembershipServiceProtocol(String membershipServiceProtocol)
    {
        Validate.notEmpty(membershipServiceProtocol);
        this.membershipServiceProtocol = membershipServiceProtocol;
    }

    /**
     * Set the port to use to access the membership service.
     * 
     * @param membershipServicePort The port, may not be <code>null</code> or empty, should be valid for the
     * specified protocol.
     */
    public void setMembershipServicePort(String membershipServicePort)
    {
        Validate.notEmpty(membershipServicePort);
        this.membershipServicePort = membershipServicePort;
    }
    
    /**
     * Set the cookie name to use when setting the session id cookie for membership
     * 
     * @param membershipSessionCookieName The cookie name, not <code>null</code> or empty.
     */
    public void setMembershipSessionCookieName(String membershipSessionCookieName)
    {
        Validate.notEmpty(membershipSessionCookieName);
        this.membershipSessionCookieName = membershipSessionCookieName;
    }
    
    /**
     * Get the base url to use for the membership service host
     * 
     * @return the url, never <code>null</code> or empty.
     */
    public String getBaseUrl()
    {
        if (baseUrl == null)
            baseUrl = membershipServiceProtocol + "://"
                    + membershipServiceHost + ":" 
                    + membershipServicePort;

        return baseUrl;
    }

    /**
     * Get the property which defines whether to user secure ldap membership or not.
     * 
     * @return the value provided by user in the perc-secured-sections.properties file for perc.use.ldap
     */
	public String getUseLdap() {
		return useLdap;
	}

	/**
	 * set the value from property file for secure ldap membership use
	 * 
	 * @param useLdap
	 */
	public void setUseLdap(String useLdap) {
		this.useLdap = useLdap;
	}
}
