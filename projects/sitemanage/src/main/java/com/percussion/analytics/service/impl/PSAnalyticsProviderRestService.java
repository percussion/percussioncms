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
package com.percussion.analytics.service.impl;

import com.percussion.analytics.data.PSAnalyticsProviderConfig;
import com.percussion.analytics.error.PSAnalyticsProviderException;
import com.percussion.analytics.error.PSAnalyticsProviderException.CAUSETYPE;
import com.percussion.analytics.service.IPSAnalyticsProviderService;
import com.percussion.error.PSExceptionUtils;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.PSGAEntries;
import com.percussion.share.service.exception.PSValidationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
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

/**
 * @author erikserating
 *
 */
@Path("/provider")
@Component("analyticsProviderRestService")
public class PSAnalyticsProviderRestService
{
    private static final Logger log = LogManager.getLogger(PSAnalyticsProviderRestService.class);

    /**
     * @param providerService
     */
    @Autowired
    public PSAnalyticsProviderRestService(IPSAnalyticsProviderService providerService)
    {
        this.providerService = providerService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.analytics.service.IPSAnalyticsProviderService#getProfiles
     * (java.lang.String, java.lang.String)
     */
    @GET
    @Path("/profiles")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSGAEntries getProfiles() throws PSAnalyticsProviderException, PSValidationException {
        try {
            PSGAEntries result = new PSGAEntries();
            result.setEntries(providerService.getProfiles(null, null));
            return result;
        } catch (IPSGenericDao.LoadException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.analytics.service.IPSAnalyticsProviderService#testConnection
     * (java.lang.String, java.lang.String)
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/testConnection/{uid}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void testConnection(@PathParam(value = "uid") String uid, @Multipart(value = "file") Attachment attachment)
            throws PSAnalyticsProviderException, PSValidationException {
        try {
            String creds = null;
            try {
                creds = IOUtils.toString(attachment.getDataHandler().getInputStream());
            } catch (IOException e) {
                log.debug("Cannot parse .json key file", e);
                throw new PSAnalyticsProviderException("Cannot parse .json key file", CAUSETYPE.INVALID_DATA);
            }

            providerService.testConnection(StringUtils.trimToEmpty(uid), creds);
        }catch (PSValidationException e){
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw e;
        } catch (IPSGenericDao.SaveException | IPSGenericDao.LoadException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.analytics.service.IPSAnalyticsProviderService#storeConfig(
     * com.percussion.analytics.data.PSAnalyticsProviderConfig)
     */
    @POST
    @Path("/config")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void storeConfig(PSAnalyticsProviderConfig config) throws PSValidationException
    {
        try {
            providerService.saveConfig(config);
        }catch (PSValidationException e){
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw e;
        } catch (IPSGenericDao.SaveException | IPSGenericDao.LoadException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/config")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSAnalyticsProviderConfig getStoredConfig() throws PSValidationException {
        try {
            return providerService.loadConfig(true);
        }catch (PSValidationException e){
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw e;
        } catch (IPSGenericDao.LoadException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @DELETE
    @Path("/config")
    public void deleteConfig()
    {
        try {
            providerService.deleteConfig();
        } catch (IPSGenericDao.DeleteException | IPSGenericDao.LoadException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.analytics.service.IPSAnalyticsProviderService#
     * isProfileConfigured(
     * com.percussion.analytics.data.PSAnalyticsProviderConfig)
     */
    @GET
    @Path("/isProfileConfigured/{sitename}")
    @Produces(MediaType.TEXT_PLAIN)
    public String isProfileConfigured(@PathParam("sitename") String sitename) throws PSValidationException {
        try {
            return ((Boolean) providerService.isProfileConfigured(sitename)).toString();
        }catch (PSValidationException e){
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw e;
        } catch (IPSGenericDao.LoadException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    private IPSAnalyticsProviderService providerService;
}
