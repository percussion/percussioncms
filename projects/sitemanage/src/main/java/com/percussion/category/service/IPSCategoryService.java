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

package com.percussion.category.service;

import com.percussion.category.data.PSCategory;
import com.percussion.category.data.PSCategoryNode;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSSpringValidationException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

/**
 * Category service is responsible to create, edit and find category.
 *
 */
public interface IPSCategoryService {
	
	/**
	 * Get a list of all the categories in the system
	 * @return list of categories
	 */
	public PSCategory getCategoryList(String siteName) throws PSDataServiceException;


    /**
     * Get the category tree object for a site
     * @param sitename - name of the site.
     * @return list of categories
     */
	public PSCategory getCategoryTreeForSite(String sitename, String rootPath, boolean includeDeleted, boolean includeSelectable) throws PSDataServiceException;
    
	/**
	 * Method to add new / update / mark as deleted a category in the respective xml.
	 *  
	 * @param category
	 * @return the updated list of categories
	 */
	public PSCategory updateCategories(PSCategory category, String sitename) throws PSSpringValidationException;
	
	/**
	 * Method to get the information whether the category tab in the 'Administration' ui is being used by an admin.
	 * 
	 * @return - lock details if one exists. Else it returns null.
	 */
	public String getLockInfo();
	
	/**
	 * Method to create a file indicating that an admin is using the category tab in the 'Administration' ui.
	 * 
	 * @param sitename - name of the site.
	 * @param date - the current date.
	 */
	public void lockCategoryTab(String date);
	
	/**
	 * Delete the file that has the admin information who was using the category tab.
	 */
	public void removeCategoryTabLock();
	
	/**
	 * Method to update a category in the DTS when it is modified for any of its property.
	 * The method is responsible to update the relevant DTS based on the request that was made.
	 * 
	 * @param sitename - Site in which the category is modified
	 * @param deliveryserver - Staging or Production
	 */
	public void updateCategoryInDTS(String sitename, String deliveryserver);


    public PSCategoryNode findCategoryNode(String siteName, String rootPath, boolean includeDeleted,
            boolean includeNotSelectable);
}
