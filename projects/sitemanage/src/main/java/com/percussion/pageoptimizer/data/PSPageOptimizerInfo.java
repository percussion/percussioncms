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
