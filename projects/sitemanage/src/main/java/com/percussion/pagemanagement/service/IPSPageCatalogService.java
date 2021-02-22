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
package com.percussion.pagemanagement.service;


import com.percussion.pagemanagement.data.PSCatalogPageSummary;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.error.PSSiteImportException;

import java.util.List;

/**
 * @author JaySeletz
 *
 */
public interface IPSPageCatalogService
{

    /**
     * Find all catalog pages for the specified site
     * 
     * @param siteId The name of the site, not <code>null<code/> or empty.
     * 
     * @return A list of page ids, never <code>null</code>, may be empty. 
     * @throws Exception If there are any unexpected errors
     */
    List<String> findCatalogPages(String siteName) throws Exception;

    /**
     * Catalog a page, creating a "page stub" as a place-holder
     * 
     * The specified page cannot be cataloged and return <code>null</code>
     * in the following scenarios:
     * <ul>
     *   <li>the maximum number of cataloged pages has been reached</li>
     *   <li>the page already exists for that name and folder path</li>
     *   <li>the page already exists under the (imported) normal location of the site</li>
     *   <li>there is already a page with the specified folder-path.</li> 
     * </ul>
     * @param siteId The name of the site for which the page is cataloged, not
     *            <code>null<code/> or empty.
     * @param href The href of the source of the page, used to import the page
     *            content later.
     * @param pageName The name to use for the page
     * @param folderPath The folder path relative from the root of the site,
     *            including the leading "/". Not <code>null<code/> or empty.
     * @param linkText The link text of the page
     * 
     * @return The saved page stub, or <code>null</code> if meet one of the scenarios described above.  
     * @throws Exception if there are any unexpected errors
     */
    PSPage addCatalogPage(String siteName, String pageName, String linkText, String folderPath, String href) throws Exception;

    /**
     * Converts the specified catalog page path to the imported folder path. 
     * @param path the folder path of the cataloged page, not <code>null</code>.
     * @return the imported folder path. Never <code>null</code>
     */
    String convertToImportedFolderPath(String path);
    
    /**
     * Get a summary for the specified catalog page
     * 
     * @param id The id of the page, not <code>null<code/>.
     * 
     * @return The summary for the supplied id.  Will be <code>null</code> if not found.
     * 
     * @throws Exception If there are any unexpected errors
     */
    PSCatalogPageSummary getCatalogPageSummary(String id) throws Exception;
       
    /**
     * Get the unassigned template for the specified site.
     * 
     * @param siteId The name of the site, not <code>null<code/> or empty.
     * 
     * @return The template id.  Will be <code>null</code> if not found.
     * @throws Exception If there are any unexpected errors
     */
    String getCatalogTemplateIdBySite(String siteName) throws PSDataServiceException, PSSiteImportException;

    /**
     * Move the cataloged page from the "page stub" location to the local
     * location. If the local location doesn't exist, it is created.
     * 
     * @param pageId The id of the page, not <code>null<code/> or empty.
     * 
     * @return The new location for the page. Not <code>null</code> or empty.
     * @throws Exception If there are any unexpected errors
     */
    void createImportedPage(String pageId) throws Exception;
 
    /**
     * Find all imported catalog pages for the specified site
     * 
     * @param siteId The name of the site, not <code>null<code/> or empty.
     * 
     * @return A list of page ids, never <code>null</code>, may be empty. 
     * @throws Exception If there are any unexpected errors
     */
    List<String> findImportedPageIds(String siteName) throws Exception;

    /**
     * Verifies if the page is an already imported page for the given site.
     * 
     * @param site {@link PSSite} the site to check the page in. Assumed not
     *            <code>null</code>.
     * @return <code>true</code> if the page already exists under the given
     *         site. <code>false</code> otherwise.
     */
    boolean doesImportedPageExist(PSPage page); 
    
    public String getFullFolderPath(String folderPath, PSSiteSummary site);
   
    public boolean pageWithFolderPathExists(String fullFolderPath);
}
