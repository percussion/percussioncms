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
