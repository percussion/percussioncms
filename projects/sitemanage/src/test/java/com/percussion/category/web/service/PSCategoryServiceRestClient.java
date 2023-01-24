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
