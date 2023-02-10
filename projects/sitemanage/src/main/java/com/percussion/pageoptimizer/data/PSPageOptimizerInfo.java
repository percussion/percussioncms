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

import com.percussion.cloudservice.data.PSCloudServiceInfo;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PageOptimizerInfo")
public class PSPageOptimizerInfo extends PSCloudServiceInfo
{
    private String pageOptimizerUrl;
    
    public String getPageOptimizerUrl()
    {
        return pageOptimizerUrl;
    }
    public void setPageOptimizerUrl(String pageOptimizerUrl)
    {
        this.pageOptimizerUrl = pageOptimizerUrl;
        this.uiProvider = pageOptimizerUrl;
    }
    
    @Override
    public void setUiProvider(String uiProvider)
    {
        this.pageOptimizerUrl = uiProvider;
        this.uiProvider = uiProvider;
    }
    
    public static PSPageOptimizerInfo fromPSCloudServiceInfo(PSCloudServiceInfo cloudInfo)
    {
        PSPageOptimizerInfo info = new PSPageOptimizerInfo();
        info.setClientIdentity(cloudInfo.getClientIdentity());
        info.setPageOptimizerUrl(cloudInfo.getUiProvider());
        return info;
    }
}
