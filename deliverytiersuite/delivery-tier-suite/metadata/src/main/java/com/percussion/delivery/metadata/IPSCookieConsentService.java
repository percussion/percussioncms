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

import com.percussion.delivery.metadata.data.PSCookieConsentQuery;

import java.util.Collection;
import java.util.Map;

/**
 * 
 * @author chriswright
 *
 */
public interface IPSCookieConsentService {
    
    /**
     * Saves the client cookie consent information.
     * @param consent the consent object to save.
     */
    public void save(Collection<PSCookieConsentQuery> consentQueries);

    /**
     * Saves the client cookie consent information.
     * @param consent the consent object to save.
     */
    public void updateOldSiteName(String oldName, String newName);
    
    /**
     * Gets a list of cookie consent entries in the database.
     * @see {@link #PSCookieConsent}
     * @return a list of cookie consent entries. May be empty, never <code>null</code>.
     */
    public Collection<IPSCookieConsent> getAllConsentStats();
    
    /**
     * Gets a list of cookie consent entries for site.
     * @param siteName - the name of the site to get the entries for.
     * @return a list of cookie consent entries.
     */
    public Collection<IPSCookieConsent> getAllConsentStatsForSite(String siteName);
    
    /**
     * Deletes all cookie consent entries from the database.
     */
    public void deleteAllCookieConsentEntries() throws Exception;
    
    /**
     * Deletes all cookie consent entries for the specified site.
     * @param siteName - the site in which to delete the entries for.
     * @throws Exception
     */
    public void deleteCookieConsentEntriesForSite(String siteName) throws Exception;
    
    /**
     * Gets cookie consent totals for all sites.
     *  
     * @return A map which contains corresponding siteName/cookie totals.
     */
    public Map<String, Integer> getAllConsentEntryTotals() throws Exception;
    
    /**
     * Gets cookie consent entries / totals for specified site.  Key/value
     * pair with key being service/cookie name value being total approved.
     * @param siteName - the name of the site to find entries for.
     * @return A map which contains key/value pairs for service/cookie name and total entries.
     */
    public Map<String, Integer> getCookieConsentEntryTotalsPerSite(String siteName) throws Exception;
    
}
