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

package com.percussion.licensemanagement.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.error.PSExceptionUtils;
import com.percussion.licensemanagement.data.PSLicenseStatus;
import com.percussion.licensemanagement.data.PSModuleLicense;
import com.percussion.licensemanagement.data.PSModuleLicenses;
import com.percussion.licensemanagement.error.PSLicenseServiceException;
import com.percussion.licensemanagement.service.IPSLicenseService;
import com.percussion.metadata.data.PSMetadata;
import com.percussion.metadata.service.IPSMetadataService;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.service.IPSSystemProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.text.MessageFormat;

import static org.apache.commons.lang.Validate.notNull;

@Path("/license")
@Component("licenseService")
public class PSLicenseService implements IPSLicenseService {


    /**
     * Logger for this service.
     */
    private static final Logger log = LogManager.getLogger(PSLicenseService.class);

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


    @Override
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
        } catch (IOException | IPSGenericDao.LoadException | IPSGenericDao.SaveException e){
            log.error(e);
            throw new PSLicenseServiceException(PSLicenseServiceException.ERROR_SAVING_LICENSES);
        }
        PSNoContent result = new PSNoContent();
        result.setOperation("DELETE");
        result.setResult("SUCCESS");
        return result;
    }

    @Override
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
        } catch (IOException | IPSGenericDao.LoadException | IPSGenericDao.SaveException e){
            log.error(e);
            throw new PSLicenseServiceException(PSLicenseServiceException.ERROR_SAVING_LICENSES);
        }
        PSNoContent result = new PSNoContent();
        result.setOperation("DELETE");
        result.setResult("SUCCESS");
        return result;
    }


    @Override
    @GET
    @Path("/module/{name}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSModuleLicense findModuleLicense(@PathParam("name") String name)
    {
            PSModuleLicense result = null;
            PSModuleLicenses mls = findAllModuleLicenses();
            if (mls.getModuleLicenses() != null) {
                for (PSModuleLicense ml : mls.getModuleLicenses()) {
                    if (ml.getName().equalsIgnoreCase(name)) {
                        result = ml;
                        break;
                    }
                }
            }
            if (result == null) {
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

    @Override
    @GET
    @Path("/module/all")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSModuleLicenses findAllModuleLicenses()
    {
     try {
         PSMetadata mldata = metadataService.find(MODULE_LICENSE_METADATA_KEY);
         PSModuleLicenses mls = null;
         if (mldata == null || StringUtils.isBlank(mldata.getData())) {
             mls = new PSModuleLicenses();
         } else {
             mls = mapToModuleLicenses(mldata.getData());
         }
         //set service url
         mls.setLicenseServiceUrl(systemProps.getProperty(CLOUD_LICENSES_URL_PROPNAME));
         return mls;
     } catch (IPSGenericDao.LoadException e) {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         throw new WebApplicationException(e);
     }
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
    @Override
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
    @Override
    public IPSSystemProperties getSystemProps()
    {
        return systemProps;
    }
    
}
