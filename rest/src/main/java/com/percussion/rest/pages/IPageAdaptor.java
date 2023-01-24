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

package com.percussion.rest.pages;

import com.percussion.rest.errors.BackendException;
import com.percussion.rest.errors.ContentMigrationException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;

import java.net.URI;
import java.util.List;

public interface IPageAdaptor
{

    public Page getPage(URI baseURI, String siteName, String path, String pageName) throws BackendException, PSDataServiceException;

    public Page updatePage(URI baseURI, Page page) throws BackendException, PSDataServiceException;

    public void deletePage(URI baseURI, String siteName, String path, String pageName) throws BackendException;

    public Page getPage(URI baseURI, String id) throws BackendException;
    
    public Page renamePage(URI baseURI, String siteName, String path, String pageName, String newName) throws BackendException, PSDataServiceException;
  
    public int approveAllPages(URI baseURI, String folderPath) throws BackendException;
    
    public int archiveAllPages(URI baseUri, String folderPath) throws BackendException;
    
    public int submitForReviewAllPages(URI baseUri, String folderPath) throws BackendException;

	public Page changePageTemplate(URI baseUri, Page p) throws BackendException;
	
	public List<String> allPagesReport(URI baseUri, String siteFolderPath) throws BackendException;
	
}
