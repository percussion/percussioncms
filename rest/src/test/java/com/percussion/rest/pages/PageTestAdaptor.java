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

import com.percussion.rest.errors.FolderNotFoundException;
import com.percussion.rest.errors.PageNotFoundException;
import com.percussion.rest.errors.SiteNotFoundException;
import com.percussion.rest.util.Examples;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Component
@Lazy
public class PageTestAdaptor implements IPageAdaptor
{

    @Override
    public Page getPage(URI baseUri,String id)
    {
        Page page = Examples.SAMPLE_PAGE;
        page.setId(id);
        if (id.equals("invalidId"))
        {
            throw new PageNotFoundException();
        }
        return page;

    }

    @Override
    public Page getPage(URI baseUri, String siteName, String path, String pageName)
    {

        Page page = Examples.SAMPLE_PAGE;

        page.setName(pageName);
        page.setFolderPath(path);
        page.setSiteName(siteName);

        if (siteName.equals("testNotFound"))
            throw new SiteNotFoundException();

        if (path.contains("testNotFound"))
            throw new FolderNotFoundException();

        if (pageName.equals("testNotFound"))
            throw new PageNotFoundException();

        return page;
    }

    @Override
    public Page updatePage(URI baseUri, Page page)
    {
        return page;
    }

    @Override
    public void deletePage(URI baseUri, String siteName, String path, String pageName)
    {
        if (pageName.equals("testNotFound"))
            throw new PageNotFoundException();
    }

	@Override
	public Page renamePage(URI baseURI, String siteName, String path, String pageName, String name) {
		//Not really sure how useful these tests are
		Page p = new Page();
		p.setName(name);
		return p;
	}


    @Override
    public int approveAllPages(URI baseURI, String folderPath) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Page changePageTemplate(URI baseUri, Page p) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> allPagesReport(URI baseUri, String siteFolderPath) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int archiveAllPages(URI baseUri, String folderPath) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int submitForReviewAllPages(URI baseUri, String folderPath) {
        // TODO Auto-generated method stub
        return 0;
    }


}
