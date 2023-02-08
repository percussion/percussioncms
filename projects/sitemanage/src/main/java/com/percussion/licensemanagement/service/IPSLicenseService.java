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

package com.percussion.licensemanagement.service;

import com.percussion.licensemanagement.data.PSModuleLicense;
import com.percussion.licensemanagement.data.PSModuleLicenses;
import com.percussion.licensemanagement.error.PSLicenseServiceException;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.service.IPSSystemProperties;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public interface IPSLicenseService {
    /**
     * Constants for Netsuite response on getLicenseStatus
     */
    String NETSUITE_STATUS_SUCCESS = "SUCCESS";
    String NETSUITE_STATUS_UNEXPECTED_ERROR = "UNEXPECTED_ERROR";
    String NETSUITE_STATUS_NO_ACCOUNT_EXISTS = "NO_ACCOUNT_EXISTS";
    String NETSUITE_STATUS_REGISTERED = "NOT_ACTIVE";
    String NETSUITE_STATUS_SUSPENDED = "SUSPENDED";
    String NETSUITE_STATUS_EXCEEDED_QUOTA = "EXCEEDED_QUOTA";
    /**
     * Custom constant to represent Suspended, need refresh state
     */
    String CUSTOM_STATUS_SUSPENDED_REFRESH = "SUSPENDED_REFRESH";
    String CUSTOM_STATUS_ACTIVE_OVERLIMIT = "SUCCESS_OVERLIMIT";
    String CLOUD_LICENSES_URL_PROPNAME = "CLOUD_LICENSES_URL";
    /**
     * Constants for License Status
     */
    String LICENSE_STATUS_ACTIVE = "Active";
    String LICENSE_STATUS_ACTIVE_OVERLIMIT = "Active, Overlimit";
    String LICENSE_STATUS_INACTIVE = "Inactive";
    String LICENSE_STATUS_REGISTERED = "Inactive, Registered";
    String LICENSE_STATUS_SUSPENDED_REFRESH = "Suspended, Refresh Required";
    String LICENSE_STATUS_SUSPENDED = "Suspended";
    String MODULE_LICENSE_TYPE_PAGE_OPTIMIZER = "PAGE_OPTIMIZER";
    String MODULE_LICENSE_TYPE_REDIRECT = "REDIRECT";
    /**
     * Constants for Module Licenses
     */
    String MODULE_LICENSE_METADATA_KEY = "perc.license.module.license.data";

    @POST
    @Path("/module/save")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    PSNoContent saveModuleLicense(PSModuleLicense moduleLicense) throws PSLicenseServiceException;

    @DELETE
    @Path("/module/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    PSNoContent deleteModuleLicense(PSModuleLicense moduleLicense) throws PSLicenseServiceException;

    @GET
    @Path("/module/{name}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    PSModuleLicense findModuleLicense(@PathParam("name") String name);

    @GET
    @Path("/module/all")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    PSModuleLicenses findAllModuleLicenses();

    @Autowired
    void setSystemProps(IPSSystemProperties systemProps);

    IPSSystemProperties getSystemProps();
}
