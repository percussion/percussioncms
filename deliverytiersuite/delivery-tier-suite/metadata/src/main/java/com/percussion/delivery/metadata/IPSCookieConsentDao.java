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
