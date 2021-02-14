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

package com.percussion.cookieconsent.service;

import javax.ws.rs.PathParam;

/**
 * Service to interface with cookie consent
 * service within DTS metadata service.
 * 
 * @author chriswright
 *
 */
public interface IPSCookieConsentService {
    
    /**
     * Exports all cookie consent information in .CSV format.
     * @param csvFileName - the file name to export.
     * @param siteName - the name of the site.  Used to concatenate
     * with CSV file name.
     * @return A string response in .CSV format.
     */
    public String exportCookieConsentData(@PathParam("csvFile") String csvFileName);
    
    /**
     * Exports all cookie consent information in .CSV format.
     * @param csvFileName - the file name to export.
     * @param siteName - the name of the site.  Used to concatenate
     * with CSV file name.
     * @return A string response in .CSV format.
     */
    public String exportCookieConsentData(@PathParam("siteName") String siteName, 
            @PathParam("csvFile") String csvFileName);
    
    /**
     * Returns the total number of cookie consent entries per site.
     * @return A map with each site as the key and the total number
     * of entries as the value.
     */
    public String getAllCookieConsentTotals();
    
    /**
     * Gets the total number of cookie consent
     * entries per service for each site.  Returns a map
     * of all cookies saved for the selected site.
     * 
     * @param siteName - the name of the site to get the entries for.
     * @return A string response in Map<String, Integer> format.  Key is site name
     * value is total number of cookie entries saved per site.
     */
    public String getCookieConsentForSite(@PathParam("siteName") String siteName);
    
    /**
     * Deletes all cookie consent entries from the DB.
     */
    public void deleteAllCookieConsentEntries();
    
    /**
     * Deletes the cookie consent entries for the site.
     * @param siteName - the site in which to delete the cookie consent entries for.
     */
    public void deleteCookieConsentEntriesForSite(@PathParam("siteName") String siteName);

}
