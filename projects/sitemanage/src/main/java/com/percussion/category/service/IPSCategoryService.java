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

package com.percussion.category.service;

import com.percussion.category.data.PSCategory;
import com.percussion.category.data.PSCategoryNode;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSSpringValidationException;
import com.percussion.share.service.exception.PSValidationException;

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
	public PSCategory updateCategories(PSCategory category, String sitename) throws PSValidationException;
	
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
