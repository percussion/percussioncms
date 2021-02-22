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

package com.percussion.cloudservice.data;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CloudServicePageData")
public class PSCloudServicePageData
{
    protected String id;
    protected String path;
    protected String pageName;
    protected String pageTitle;
    protected String pageDescription;
    protected String pagePath;
    protected String status;
    protected String workflow;
    protected String lastPublished;
    protected String lastEdited;
    protected String thumbUrl;
    protected String clientIdentity;
    protected String pageHtml;
    protected String uiProviderUrl;
    
    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
    }
    public String getPath()
    {
        return path;
    }
    public void setPath(String path)
    {
        this.path = path;
    }
    public String getPageName()
    {
        return pageName;
    }
    public void setPageName(String pageName)
    {
        this.pageName = pageName;
    }
    public String getPageTitle()
    {
        return pageTitle;
    }
    public void setPageTitle(String pageTitle)
    {
        this.pageTitle = pageTitle;
    }
    public String getPageDescription()
    {
        return pageDescription;
    }
    public void setPageDescription(String pageDescription)
    {
        this.pageDescription = pageDescription;
    }
    public String getPagePath()
    {
        return pagePath;
    }
    public void setPagePath(String pagePath)
    {
        this.pagePath = pagePath;
    }
    public String getStatus()
    {
        return status;
    }
    public void setStatus(String status)
    {
        this.status = status;
    }
    public String getWorkflow()
    {
        return workflow;
    }
    public void setWorkflow(String workflow)
    {
        this.workflow = workflow;
    }
    public String getLastPublished()
    {
        return lastPublished;
    }
    public void setLastPublished(String lastPublished)
    {
        this.lastPublished = lastPublished;
    }
    public String getLastEdited()
    {
        return lastEdited;
    }
    public void setLastEdited(String lastEdited)
    {
        this.lastEdited = lastEdited;
    }
    public String getThumbUrl()
    {
        return thumbUrl;
    }
    public void setThumbUrl(String thumbUrl)
    {
        this.thumbUrl = thumbUrl;
    }
    public String getClientIdentity()
    {
        return clientIdentity;
    }
    public void setClientIdentity(String clientIdentity)
    {
        this.clientIdentity = clientIdentity;
    }
    public String getPageHtml()
    {
        return pageHtml;
    }
    public void setPageHtml(String pageHtml)
    {
        this.pageHtml = pageHtml;
    }
    public String getUiProviderUrl()
    {
        return uiProviderUrl;
    }
    public void setUiProviderUrl(String uiProviderUrl)
    {
        this.uiProviderUrl = uiProviderUrl;
    }
}
