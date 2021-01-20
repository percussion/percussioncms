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

package com.percussion.licensemanagement.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.licensemanagement.data.PSLicenseStatus;
import com.percussion.licensemanagement.data.PSModuleLicense;
import com.percussion.licensemanagement.data.PSModuleLicenses;
import com.percussion.licensemanagement.error.PSLicenseServiceException;
import com.percussion.metadata.data.PSMetadata;
import com.percussion.metadata.service.IPSMetadataService;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.service.IPSSystemProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.text.MessageFormat;

import static org.apache.commons.lang.Validate.notNull;

@Path("/license")
@Component("licenseService")
public class PSLicenseService {
    /**
     * Constants for Netsuite response on getLicenseStatus
     */
    public static final String NETSUITE_STATUS_SUCCESS = "SUCCESS";

    public static final String NETSUITE_STATUS_UNEXPECTED_ERROR = "UNEXPECTED_ERROR";

    public static final String NETSUITE_STATUS_NO_ACCOUNT_EXISTS = "NO_ACCOUNT_EXISTS";

    public static final String NETSUITE_STATUS_REGISTERED = "NOT_ACTIVE";

    public static final String NETSUITE_STATUS_SUSPENDED = "SUSPENDED";

    public static final String NETSUITE_STATUS_EXCEEDED_QUOTA = "EXCEEDED_QUOTA";

    /**
     * Custom constant to represent Suspended, need refresh state
     */
    public static final String CUSTOM_STATUS_SUSPENDED_REFRESH = "SUSPENDED_REFRESH";

    public static final String CUSTOM_STATUS_ACTIVE_OVERLIMIT = "SUCCESS_OVERLIMIT";

    public static final String CLOUD_LICENSES_URL_PROPNAME = "CLOUD_LICENSES_URL";

    /**
     * Constants for License Status
     */
    public static final String LICENSE_STATUS_ACTIVE = "Active";

    public static final String LICENSE_STATUS_ACTIVE_OVERLIMIT = "Active, Overlimit";

    public static final String LICENSE_STATUS_INACTIVE = "Inactive";

    public static final String LICENSE_STATUS_REGISTERED = "Inactive, Registered";

    public static final String LICENSE_STATUS_SUSPENDED_REFRESH = "Suspended, Refresh Required";

    public static final String LICENSE_STATUS_SUSPENDED = "Suspended";

    public static final String MODULE_LICENSE_TYPE_PAGE_OPTIMIZER = "PAGE_OPTIMIZER";


    public static final String MODULE_LICENSE_TYPE_REDIRECT = "REDIRECT";
    /**
     * Constants for Module Licenses
     */
    public static final String MODULE_LICENSE_METADATA_KEY = "perc.license.module.license.data";

    /**
     * Logger for this service.
     */
    public static Log log = LogFactory.getLog(PSLicenseService.class);

    /**
     * The delivery service initialized by constructor, never <code>null</code>.
     */
    private IPSDeliveryInfoService deliveryService;

    /**
     * The service to get a list of all sites under this CM1 installation, to
     * request for their usage metrics.
     */
    private IPSSiteManager siteManager;

    /**
     * The metadata service to store module licenses.
     */
    private IPSMetadataService metadataService;

    private IPSSystemProperties systemProps;
    
    /**
     * The service to get current usage metrics
     */
    private IPSCmsObjectMgr cmsObjectMgr;

    private PSLicenseStatus INSTANCE_CACHED_LICENSE_STATUS = null;

    // needed for test
    protected PSLicenseService() {
    }

    @Autowired
    public PSLicenseService(IPSMetadataService metadataService) {

        this.metadataService = metadataService;

    }

    private static final String SEPARATOR = "||";


    @POST
    @Path("/module/save")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSNoContent saveModuleLicense(PSModuleLicense moduleLicense) throws PSLicenseServiceException
    {
        notNull(moduleLicense);
        validateModuleLicense(moduleLicense);
        ObjectMapper mapper = new ObjectMapper();
        try{
            PSModuleLicenses moduleLicenses = findAllModuleLicenses();
            moduleLicenses.addModuleLicense(moduleLicense);
            metadataService.save(new PSMetadata(MODULE_LICENSE_METADATA_KEY, mapper.writeValueAsString(moduleLicenses)));
        } catch (IOException e){
            log.error(e);
            throw new PSLicenseServiceException(PSLicenseServiceException.ERROR_SAVING_LICENSES);
        }
        PSNoContent result = new PSNoContent();
        result.setOperation("DELETE");
        result.setResult("SUCCESS");
        return result;
    }

    @DELETE
    @Path("/module/delete")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSNoContent deleteModuleLicense(PSModuleLicense moduleLicense) throws PSLicenseServiceException
    {
        notNull(moduleLicense);
        validateModuleLicense(moduleLicense);
        ObjectMapper mapper = new ObjectMapper();
        try{
            PSModuleLicenses moduleLicenses = findAllModuleLicenses();
            moduleLicenses.removeModuleLicense(moduleLicense);
            metadataService.save(new PSMetadata(MODULE_LICENSE_METADATA_KEY, mapper.writeValueAsString(moduleLicenses)));
        } catch (IOException e){
            log.error(e);
            throw new PSLicenseServiceException(PSLicenseServiceException.ERROR_SAVING_LICENSES);
        }
        PSNoContent result = new PSNoContent();
        result.setOperation("DELETE");
        result.setResult("SUCCESS");
        return result;
    }


    @GET
    @Path("/module/{name}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSModuleLicense findModuleLicense(@PathParam("name") String name) throws PSLicenseServiceException
    {
        PSModuleLicense result = null;
        PSModuleLicenses mls = findAllModuleLicenses();
        if(mls.getModuleLicenses() != null){
            for (PSModuleLicense ml : mls.getModuleLicenses()) {
                if(ml.getName().equalsIgnoreCase(name)){
                    result = ml;   
                    break;
                }
            }
        }
        if(result == null){
            Object[] obj = {name};
            throw new PSLicenseServiceException(MessageFormat.format(PSLicenseServiceException.LICENSE_NOT_FOUND, obj));
        }
        return result;
    }
    
    /**
     * Validates the module licenses supplied.
     * @param moduleLicense assumed not <code>null</code>.
     */
    private void validateModuleLicense(PSModuleLicense moduleLicense){
        if(StringUtils.isBlank(moduleLicense.getName()) || StringUtils.isBlank(moduleLicense.getKey()) || StringUtils.isBlank(moduleLicense.getHandshake())){
            ObjectMapper mapper = new ObjectMapper();
            try
            {
                log.error("Supplied module license is invalid. " + mapper.writeValueAsString(moduleLicense));
            }
            catch (Exception e)
            {
                //Ignore
            }
            throw new PSLicenseServiceException(PSLicenseServiceException.ERROR_SAVING_LICENSES);
        }
    }

    @GET
    @Path("/module/all")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSModuleLicenses findAllModuleLicenses() throws PSLicenseServiceException
    {
        PSMetadata mldata = metadataService.find(MODULE_LICENSE_METADATA_KEY);
        PSModuleLicenses mls = null;
        if(mldata == null || StringUtils.isBlank(mldata.getData())){
            mls = new PSModuleLicenses();
        }
        else{
            mls = mapToModuleLicenses(mldata.getData());
        }
        //set service url
        mls.setLicenseServiceUrl(systemProps.getProperty(CLOUD_LICENSES_URL_PROPNAME));
        return mls;
    }
    
    /**
     * Helper function that maps the module license string to module licenses object.
     * @param jsonStr module license string assumed not <code>blank</code>
     * @return PSModuleLicenses corresponding to the supplied string.
     */
    private PSModuleLicenses mapToModuleLicenses(String jsonStr)
    {
        PSModuleLicenses mls = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            mls = mapper.readValue(jsonStr, PSModuleLicenses.class);
        } catch (IOException e){
            log.error(e);
            throw new PSLicenseServiceException(PSLicenseServiceException.ERROR_FINDING_LICENSE);
        }
        return mls;
    }
    /**
     * Set the system properties on this service. This service will always use
     * the the values provided by the most recently set instance of the
     * properties.
     * 
     * @param systemProps the system properties
     */
    @Autowired
    public void setSystemProps(IPSSystemProperties systemProps)
    {
        this.systemProps = systemProps;
    }

    /**
     * Gets the system properties used by this service.
     * 
     * @return The properties
     */
    public IPSSystemProperties getSystemProps()
    {
        return systemProps;
    }
    
}
