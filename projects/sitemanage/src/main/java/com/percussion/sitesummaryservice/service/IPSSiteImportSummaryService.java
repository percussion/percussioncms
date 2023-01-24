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

package com.percussion.sitesummaryservice.service;

import com.percussion.services.siteimportsummary.data.PSSiteImportSummary;
import com.percussion.share.dao.IPSGenericDao;

import java.util.Map;

/**
 * Service to manage the site import summary.
 *
 */
public interface IPSSiteImportSummaryService
{
    /**
     * Finds the site import summary for a given site.
     * @param siteId assumed to be a valid siteId, if not returns null for site summary.
     * @return site summary may be <code>null</code>, if not found.
     */
    PSSiteImportSummary find(int siteId);
    
    /**
     * Creates a site summary for a given site.
     * @param siteId must be a valid site.
     * @return site summary for the site.
     */
    PSSiteImportSummary create(int siteId) throws IPSGenericDao.SaveException;
    
    /**
     * Deletes the site summary entry for a given site.
     * @param siteId
     */
    void deleteBySiteId(int siteId);
    
    /**
     * Updates the site summary details. Increments each field value by the supplied map.
     * @param siteId must be a valid site and an sntry must exist for this site.
     * @param fields may not be <code>null</code>.
     * @return updated site summary.
     */
    PSSiteImportSummary update(int siteId, Map<SiteImportSummaryTypeEnum, Integer> fields) throws IPSGenericDao.SaveException;
    
    enum SiteImportSummaryTypeEnum{
        PAGES, TEMPLATES, STYLESHEETS, FILES, INTERNALLINKS
    }
}
