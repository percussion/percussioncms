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
