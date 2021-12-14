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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
