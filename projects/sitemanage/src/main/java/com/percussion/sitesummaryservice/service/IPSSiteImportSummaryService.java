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
