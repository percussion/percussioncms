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

import com.percussion.assetmanagement.data.PSReportFailedToRunException;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.pagemanagement.data.PSNonSEOPagesRequest;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSPageChangeEvent;
import com.percussion.pagemanagement.data.PSPageReportLine;
import com.percussion.pagemanagement.data.PSSEOStatistics;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.data.PSUnassignedResults;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSSpringValidationException;
import com.percussion.share.service.exception.PSValidationException;

import java.util.List;

/**
 * Provides various CRUD operations for page objects.
 */
public interface IPSPageService extends IPSDataService<PSPage, PSPage, String>
{

   /**
    * Creates/saves a page.
    * 
    * @param page the Page, never <code>null</code>.
    * @return Page never <code>null</code>.
    */
   PSPage save(PSPage page) throws PSDataServiceException;

   /**
    * Finds the specified page.
    * 
    * @param id of the page, never <code>null</code> or empty.
    * 
    * @return the page item. It may be <code>null</code> if cannot find one.
    */
   PSPage find(String id) throws DataServiceLoadException, DataServiceNotFoundException, PSValidationException;

   /**
    * Loads the specified page.
    * 
    * @param id of the page, never <code>null</code> or empty.
    * 
    * @return the page, never <code>null</code>.
    */
   PSPage load(String id) throws PSValidationException, DataServiceLoadException, IPSDataService.DataServiceNotFoundException;

   /**
    * Finds the specified page by name and folder path.
    * 
    * @param name of the page, never <code>null</code> or empty.
    * @param folderPath the folder path of the page, never <code>null</code> or
    *  empty.
    * 
    * @return the page item. It may be <code>null</code> if cannot find one.
    * 
    * @throws PSPageException if an error occurs finding the page.
    */
   PSPage findPage(String name, String folderPath)
           throws PSDataServiceException;

    /**
     * See {@link #findPage(String, String)}.
     * 
     * @param fullPath folderPath + path, never <code>null</code>, empty, or
     *            blank.
     * @return A {@link PSPage} object. May be <code>null</code> if the page
     *         could not be found.
     * @throws PSPageException if an error occurs finding the page.
     * 
     */
    PSPage findPageByPath(String fullPath) throws PSPageException, PSValidationException;
   
    /**
     * Find all the pages that use a certain template and return the results
     * paged.
     * 
     * @param templateId The id of the template, from the UI, never
     *            <code>null</code>.
     * @param startIndex The starting index of the pagination. The resulting
     *            page of data will contain pages from startIndex to
     *            startIndex+maxResults, never <code>null</code>.
     * @param maxResults The results page size that the client paginator will
     *            handle, never <code>null</code> and equal or greater than 1.
     * @param sortColumn The results will be ordered by this attribute before
     *            being paged. If not specified, name will be used as default.
     * @param sortOrder Asc, or Desc order. If not specified, asc will be set as
     *            default.
     * @param pageId The page item id to find in the list to get the correct page,
     *          may be <code>null</code>
     *          
     * @return PSPagedItemList a paged item list of pathItems. These pathItems
     *         contain name and full path for the pages that use the template.
     * @throws PSPageException If there was a problem retrieving the list of
     *             pages.
     */
    PSPagedItemList findPagesByTemplate(String templateId, Integer startIndex, Integer maxResults,
            String sortColumn, String sortOrder, String pageId) throws PSDataServiceException;
    
    
    /**
    * Finds all pages for a given request which are not optimized for searching.
    * 
    * @param request the request to find the pages by workflow state, never <code>null</code>.
    * 
    * @return the seo statistics for pages which are considered sub-optimal for searching.
    * @throws PSPageException If the workflow could not be found, or other system failure.
    */
    List<PSSEOStatistics> findNonSEOPages(PSNonSEOPagesRequest request) throws PSPageException, IPSGenericDao.LoadException;
   
   /**
    * Deletes the specified page.  All local content of the page will also be deleted.  The page will not be deleted if
    * it is being edited by another user.
    * 
    * @param id the ID of the page, never <code>null</code> or empty.
    */
   void delete(String id) throws PSValidationException;
   
   /**
    * See {@link #delete(String)}.
    * 
    * @param id the ID of the page, never <code>null</code> or empty.
    * @param force <code>true</code> to delete the page even if it is being edited by another user, <code>false</code>
    * otherwise. 
    */
   void delete(String id, boolean force) throws PSValidationException;

    /**
     * See {@link #delete(String, boolean)}.
     *
     * @param id the ID of the page, never <code>null</code> or empty.
     * @param force <code>true</code> to delete the page even if it is being edited by another user, <code>false</code>
     * @param purgeItem <code>true</code> if the item should be purged. <code>false</code> if it should be recycled.
     * otherwise.
     */
    void delete(String id, boolean force, boolean purgeItem) throws PSValidationException;
   
   /**
    * Generates a new page name
    * @param pageName
    * @param folderPath
    * @return
    */
    String generateNewPageName(String pageName, String folderPath) throws PSPageException;
   
   /**
    * Creates a copy of the page in it's current folder.
    * @param id
    * @return
    * @throws DataServiceSaveException
    */
    String copy(String id, boolean addToRecent) throws PSDataServiceException, IPSPathService.PSPathNotFoundServiceException;

    /**
     *Creates a copy of the page in the specified folder.
     * @param id
     * @return
     * @throws DataServiceSaveException
     */
     String copy(String id, String targetFolder, boolean addToRecent) throws PSDataServiceException, IPSPathService.PSPathNotFoundServiceException;

   /**
    * Gets an URL which can be used for editing an existing page.
    * 
    * @param id the ID of the page, never blank.
    * 
    * @return the URL described above, never blank.
    */  
    String getPageEditUrl(String id);
   
   /**
    * Gets an URL which can be used for viewing a read only page.
    * 
    * @param id the ID of the page, never blank.
    * 
    * @return the URL described above, never blank.
    */  
    String getPageViewUrl(String id);

   /**
    * Determines if the supplied item is one of the pages or its content type is {@link #PAGE_CONTENT_TYPE}.
    * @param id the item ID in question, it may be <code>null</code> or empty.
    * @return <code>true</code> if the item is a page; otherwise <code>false</code>.
    */
    boolean isPageItem(String id) throws PSPageException;
   
   /**
    * Add a page change listener to get notified when the page change happens.
    * @param pageChangeListener, must not be <code>null</code>.
    */
    void addPageChangeListener(IPSPageChangeListener pageChangeListener);
   
   /**
    * Call this method to notify the listeners that the page has changed.
    * @param pageChangeEvent, must not be <code>null</code>.
    */
   public void notifyPageChange(PSPageChangeEvent pageChangeEvent);

   
   /**
    * At present the page meta-data saving happens through the content editor. Adding a dummy service to get notified
    * on page meta-data save.
    * 
    * TODO: - The page meta-data save needs to be rerouted from this method to the content editor, at that time depending on the implementation the signature needs to be updated.
    * 
    * @param pageId, the string representation of the page guid. must not be <code>null</code>.
    * returns PSNoContent, never blank
    */
    PSNoContent savePageMetadata(String pageId);
   
   /**
    * Changes the template of the supplied page.
    * @param pageId the string representation of the page guid. must not be <code>null</code>.
    * @param templateId the string representation of the page guid. must not be <code>null</code>.
    * @return PSNoContent, never blank
    */
    PSNoContent changeTemplate(String pageId, String templateId);

    /**
     * Update the template migration version of the page to match the version in its template
     * 
     * @param pageId The id of the page to update, must specify an existing page, and the page must be checked out to
     * the current user.
     */
     void updateTemplateMigrationVersion(String pageId) throws PSDataServiceException;
    
    /**
     * Update the migration empty widget flag for the page
     * 
     * @param pageId The id of the page to update, must specify an existing page, and the page must be checked out to
     * the current user.
     */
     void updateMigrationEmptyWidgetFlag(String pageId, boolean flag) throws PSDataServiceException;
    
    /**
     * Get the status of empty widget flag for the page
     * 
     * @param pageId The id of the page to get the flag, must specify an existing page, and the page must be checked out to
     * the current user.
     */
     boolean getMigrationEmptyWidgetFlag(String pageId) throws DataServiceLoadException, DataServiceNotFoundException, PSValidationException;

    /***
     * A listing of all Pages in the Content Repository for the specified site.
     * @return A list of CSV formattable report lines. 
     * @throws PSReportFailedToRunException 
     */
    public List<PSPageReportLine> findAllPages(String siteName) throws PSReportFailedToRunException, PSPageException, IPSGenericDao.LoadException;
   
    /**
     * get the import status for cataloged pages.
     * 
     * @param sitename {@link String} with the name of the site. Must not be
     *            <code>null</code> nor empty.
     * @param startIndex {@link Integer} with the start index. The first item is
     *            1. If the value is <code>null</code>, or less than 1, it will
     *            be changed to 1.
     * @param maxResults {@link Integer} indicating the maximum amount of
     *            results to return. May be <code>null</code>, but if it isn't
     *            <code>null</code>, it must be greater than 0.
     * @return {@link PSUnassignedResults} with the results, never
     *         <code>null</code>.
     * @throws Exception 
     */
    public PSUnassignedResults getUnassignedPagesBySite(String sitename,Integer startIndex, Integer maxResults) throws PSPageException;


    
    public PSNoContent clearMigrationEmptyFlag(String pageid) throws PSDataServiceException;
   
    /*
    * (Runtime) Exception is thrown when an unexpected error occurs in this
    * service.
    */
   public static class PSPageException extends PSDataServiceException
   {
      /**
       * Generated serial number.
       */
      private static final long serialVersionUID = 1L;

      /**
       * Default constructor.
       */
      public PSPageException()
      {
         super();
      }

      /**
       * Constructs an exception with the specified detail message and the
       * cause.
       * 
       * @param message the specified detail message.
       * @param cause the cause of the exception.
       */
      public PSPageException(String message, Throwable cause)
      {
         super(message, cause);
      }

      /**
       * Constructs an exception with the specified detail message.
       * 
       * @param message the specified detail message.
       */
      public PSPageException(String message)
      {
         super(message);
      }

      /**
       * Constructs an exception with the specified cause.
       * 
       * @param cause the cause of the exception.
       */
      public PSPageException(Throwable cause)
      {
         super(cause);
      }
   }
   
   
   /**
    * The content type name of the page item.
    */
   String PAGE_CONTENT_TYPE = "percPage";


    PSNoContent validateDelete(String id) throws PSValidationException;



}
