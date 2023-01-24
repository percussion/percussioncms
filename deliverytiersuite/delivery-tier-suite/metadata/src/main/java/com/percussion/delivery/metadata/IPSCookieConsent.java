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

package com.percussion.delivery.metadata;

import java.util.Date;

/**
 * Object to store information related
 * to client cookie consent.
 * 
 * <ul>
 * <li> Consent Date </li>
 * <li>  IP Address </li>
 * <li>  Opt In </li>
 * <li>  Service Name </li>
 * <li>  Site Name </li>
 * </ul>
 * 
 * @author chriswright
 *
 */
public interface IPSCookieConsent {
    
    /**
     * Sets the name of site in which to apply the cookie(s).
     * @param siteName the name of the site
     */
    public void setSiteName(String siteName);
    
    /**
     * Gets the name of site.
     * @return the name of the site
     */
    public String getSiteName();
    
    /**
     * Set the IP address.
     * @param ip the IP address
     */
    public void setIP(String ip);
    
    /**
     * Gets the IP address.
     * @return string representation of IP address
     */
    public String getIP();
    
    /**
     * Sets the date consent to use cookies was given.
     * @param consentDate the date object of consent date
     */
    public void setConsentDate(Date consentDate);
    
    /**
     * Gets the date consent was given to use cookies.
     * @return the date consent was given
     */
    public Date getConsentDate();
    
    /**
     * Sets the services approved to use cookies.
     * @param serviceName the name of service
     * cookies.
     */
    public void setService(String serviceName);
    
    /**
     * Gets the service(s) approved for use with cookies.
     * @return the name of the service
     */
    public String getService();
    
    /**
     * Sets whether the user opted in for cookie consent.
     * Should always be true as if they did not, no information
     * should be recorded.
     * @param optIn <code>true</code> if the user opted to use cookies
     */
    public void setOptIn(boolean optIn);
    
    /**
     * Returns whether the user opted in to use cookies.
     * Should always be true if there is a record present.
     * @return <code>true</code> if the user opted in for cookies.
     */
    public boolean getOptIn();
    
}
