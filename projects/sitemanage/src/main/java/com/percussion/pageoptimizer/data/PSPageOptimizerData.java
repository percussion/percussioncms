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

package com.percussion.pageoptimizer.data;

import com.percussion.cloudservice.data.PSCloudServicePageData;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PageOptimizerData")
public class PSPageOptimizerData extends PSCloudServicePageData
{
    String pageOptimizerUrl;
    
    public String getPageOptimizerUrl()
    {
        return pageOptimizerUrl;
    }
    public void setPageOptimizerUrl(String pageOptimizerUrl)
    {
        this.pageOptimizerUrl = pageOptimizerUrl;
        this.uiProviderUrl = pageOptimizerUrl;
    }
    
    @Override
    public void setUiProviderUrl(String uiProviderUrl)
    {
        this.pageOptimizerUrl = uiProviderUrl;
        this.uiProviderUrl = uiProviderUrl;
    }

    public static PSPageOptimizerData fromPSCloudServicePageData(PSCloudServicePageData cloudData) {
        PSPageOptimizerData data = new PSPageOptimizerData();
        data.setId(cloudData.getId());
        data.setPath(cloudData.getPath());
        data.setPageName(cloudData.getPageName());
        data.setPageTitle(cloudData.getPageTitle());
        data.setStatus(cloudData.getStatus());
        data.setWorkflow(cloudData.getWorkflow());
        data.setLastPublished(cloudData.getLastPublished());
        data.setLastEdited(cloudData.getLastEdited());
        data.setThumbUrl(cloudData.getThumbUrl());
        data.setClientIdentity(cloudData.getClientIdentity());
        data.setPageHtml(cloudData.getPageHtml());
        data.setUiProviderUrl(cloudData.getUiProviderUrl());
        return data;
    }
}
