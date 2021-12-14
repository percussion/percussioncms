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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
