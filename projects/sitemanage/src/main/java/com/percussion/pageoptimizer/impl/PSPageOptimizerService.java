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

package com.percussion.pageoptimizer.impl;

import com.percussion.cloudservice.data.PSCloudLicenseType;
import com.percussion.cloudservice.data.PSCloudServiceInfo;
import com.percussion.cloudservice.data.PSCloudServicePageData;
import com.percussion.cloudservice.impl.PSCloudService;
import com.percussion.licensemanagement.service.impl.PSLicenseService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSRenderService;
import com.percussion.pageoptimizer.IPSPageOptimizerService;
import com.percussion.pageoptimizer.data.PSPageOptimizerData;
import com.percussion.pageoptimizer.data.PSPageOptimizerInfo;
import com.percussion.share.dao.IPSFolderHelper;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/pageoptimizer")
@Component("pageOptimizerService")
@Lazy
public class PSPageOptimizerService extends PSCloudService implements IPSPageOptimizerService {

    @Autowired
    public PSPageOptimizerService(IPSFolderHelper folderHelper, IPSRenderService renderService, 
            IPSPageService pageService, PSLicenseService licenseService)
    {
    	super(folderHelper, renderService, pageService, licenseService);
    	this.log = LogManager.getLogger(PSPageOptimizerService.class);
    }
    
    @Override
    @GET
    @Path("/active")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean isPageOptimizerActive()
    {
        return isValidLicense(PSCloudLicenseType.PAGE_OPTIMIZER);
    }

    @Override
    @GET
    @Path("/info")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSPageOptimizerInfo getPageOptimizerInfo()
    {
        PSCloudServiceInfo cloudInfo = super.getInfo(PSCloudLicenseType.PAGE_OPTIMIZER);
        PSPageOptimizerInfo info = PSPageOptimizerInfo.fromPSCloudServiceInfo(cloudInfo);
        return info;
    }

    @Override
    @GET
    @Path("/pagedata/{pageId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSPageOptimizerData getPageOptimizerData(@PathParam("pageId") String pageId)
    {
        PSCloudServicePageData cloudData = super.getPageData(PSCloudLicenseType.PAGE_OPTIMIZER, pageId);
        PSPageOptimizerData poData = PSPageOptimizerData.fromPSCloudServicePageData(cloudData);
        return poData;
    }
}
