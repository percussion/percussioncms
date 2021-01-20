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

package com.percussion.delivery.metadata;

import com.percussion.delivery.metadata.rdbms.impl.PSDbCookieConsent;

import java.util.Collection;
import java.util.Map;

/**
 * 
 * @author chriswright
 *
 */
public interface IPSCookieConsentDao {
    
    /**
     * Saves a list of cookie consent entries.
     * 
     * @param consents the collection of consent objects
     * to save.
     */
    public void save(Collection<PSDbCookieConsent> consents);
    
    /**
     * Gets the entire list of cookie consent entries.
     * 
     * @see IPSCookieConsent
     * @return A collection of cookie consent entries, may be empty
     * never <code>null</code>.
     */
    public Collection<IPSCookieConsent> getAllCookieConsentStats();
    
    /**
     * Returns the list of cookie consent entries for a site.
     * 
     * @param siteName - the site name in which to get entries for;
     * @return A collection of cookie consent entries
     * @see IPSCookieConsent
     */
    public Collection<IPSCookieConsent> getAllCookieStatsForSite(String siteName);
    
    /**
     * Deletes all cookie consent entries from the DB.
     */
    public void deleteAll() throws Exception;
    
    /**
     * Deletes all cookie consent entries for the specified site.
     * 
     * @param siteName - the site in which to delete the entries for.
     */
    public void deleteForSite(String siteName) throws Exception;
    
    /**
     * Gets the totals from DB for all sites.
     * 
     * @return Key/value pair with siteName/total being pair.
     */
    public Map<String, Integer> getTotalsForAllSites() throws Exception;
    
    /**
     * Gets the totals from DB for specified site.  Returns
     * Map format with service/total being key/value pair.
     * 
     * @param siteName - the site in which to retrieve entries for.
     * @return A map representation of each serviceName/total for site.
     * @throws Exception
     */
    public Map<String, Integer> getTotalsForSite(String siteName) throws Exception;

    public void updateOldSiteName(String oldSiteName, String newSiteName) throws Exception;
    
}
