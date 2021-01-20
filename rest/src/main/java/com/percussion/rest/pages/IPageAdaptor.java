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

package com.percussion.rest.pages;

import com.percussion.rest.errors.ContentMigrationException;

import java.net.URI;
import java.util.List;

public interface IPageAdaptor
{

    public Page getPage(URI baseURI, String siteName, String path, String pageName);

    public Page updatePage(URI baseURI, Page page);

    public void deletePage(URI baseURI, String siteName, String path, String pageName);

    public Page getPage(URI baseURI, String id);
    
    public Page renamePage(URI baseURI, String siteName, String path, String pageName, String newName);
  
    public int approveAllPages(URI baseURI, String folderPath);
    
    public int archiveAllPages(URI baseUri, String folderPath);
    
    public int submitForReviewAllPages(URI baseUri, String folderPath);

	public Page changePageTemplate(URI baseUri, Page p) throws ContentMigrationException;
	
	public List<String> allPagesReport(URI baseUri, String siteFolderPath); 
	
}
