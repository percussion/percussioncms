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

package com.percussion.category.web.service;

import com.percussion.category.data.PSCategory;
import com.percussion.category.data.PSCategoryNode;
import com.percussion.category.service.IPSCategoryService;
import com.percussion.share.service.exception.PSDataServiceException;

//public class PSCategoryServiceRestClient extends PSObjectRestClient implements IPSCategoryService {
public class PSCategoryServiceRestClient extends PSJerseyRestClient implements IPSCategoryService {
	
	private String path = "/services/categorymanagement/category";
    
    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

	@Override
	public PSCategory getCategoryList(String siteName)  throws PSDataServiceException {
		
		login("Admin", "demo");

		return getData(concatPath(getPath(), "all", siteName));
	}

	@Override
	public PSCategory updateCategories(PSCategory category, String siteName) {
		
		return postData(concatPath(getPath(), "update", siteName), category);
	}
	
	@Override
	public String getLockInfo(){ return null;}
	
	@Override
	public void lockCategoryTab( String date){}
	
	@Override
	public void removeCategoryTabLock(){}
	
	@Override
	public void updateCategoryInDTS(String sitename, String deliveryserver){}
	/*
	public static void main(String[] args) {
		
		PSCategoryServiceRestClient client = new PSCategoryServiceRestClient();
		
		client.login("Admin", "demo");
		
		client.getCategoryList("xyz");
		
	}*/

    @Override
    public PSCategory getCategoryTreeForSite(String sitename, String rootPath, boolean includeDeleted, boolean includeSelectable){ return null;}
   
    @Override
    public PSCategoryNode findCategoryNode(String siteName, String rootPath, boolean includeDeleted,
            boolean includeNotSelectable) { return null;}
    
}
