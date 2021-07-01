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
