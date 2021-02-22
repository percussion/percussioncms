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
package com.percussion.pagemanagement.dao;

import java.util.List;

import javax.jcr.RepositoryException;

import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSPageSummary;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSSpringValidationException;

public interface IPSPageDao extends IPSGenericDao<PSPage, String>
{
    
    PSPageSummary findSummary(String id) throws IPSPageService.PSPageException;
    
    List<PSPageSummary> findAllSummaries() throws IPSPageService.PSPageException;
    
    PSPage findPage(String name, String folderPath) throws PSDataServiceException;
    
    PSPage findPageByPath(String fullFolderPath) throws IPSPageService.PSPageException;
  
    /**
     * Finds all pages which are located under the specified path and utilize the specified template.
     * 
     * @param path the internal folder path.  If blank, the result be the same as a call to
     * {@link #findPageByPath(String)}.
     * @param templateId never blank.
     * 
     * @return list of pages, never <code>null</code>, may be empty.
     */
    List<PSPage> findPagesBySiteAndTemplate(String path, String templateId) throws PSDataServiceException;
    
    /**
     * Finds all pages which are located under the specified path in the specified workflow and state.
     * 
     * @param path the internal folder path, never blank.
     * @param workflowId 
     * @param stateId set to -1 to include pages in all workflow states.
     * 
     * @return list of pages, never <code>null</code>, may be empty.
     */
    List<PSPageSummary> findPagesBySiteAndWf(String path, int workflowId, int stateId) throws PSDataServiceException;
    
    /**
     * 
     * @param id the ID of the page, never <code>null</code> or empty.
     * @param force <code>true</code> to delete the page even if it is being edited by another user, <code>false</code>
     * otherwise. 
     */
    public void delete(String id, boolean force) throws PSDataServiceException;

    /**
     * Get the pages' ids by field name and field value.
     * 
     * @param fieldName the name of the field to search never <code>null</code>.
     * @param fieldValue the value of the field to search never
     *            <code>null</code>.
     * @return list of content ids, may be empty if no items found never
     *         <code>null</code>.
     * @throws RepositoryException
     */
    List<Integer> getPageIdsByFieldNameAndValue(String fieldName, String fieldValue) throws IPSPageService.PSPageException;

    /**
     * Get the content type id of the page content type
     * 
     * @return the id
     */
    long getPageContentTypeId() throws IPSPageService.PSPageException;

    /***
     * Find a return a list of all pages for the specified site path.
     * @param sitePath //Sites/SiteName
     * @return Never null.  A list of Pages for the specified site. 
     */
    public List<PSPage> findAllPagesBySite(String sitePath) throws PSDataServiceException;
}
