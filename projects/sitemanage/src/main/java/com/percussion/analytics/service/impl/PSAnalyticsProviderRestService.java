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
package com.percussion.analytics.service.impl;

import com.percussion.analytics.data.PSAnalyticsProviderConfig;
import com.percussion.analytics.error.PSAnalyticsProviderException;
import com.percussion.analytics.error.PSAnalyticsProviderException.CAUSETYPE;
import com.percussion.analytics.service.IPSAnalyticsProviderService;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.PSGAEntries;
import com.percussion.share.service.exception.PSValidationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
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
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
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
        } catch (IPSGenericDao.SaveException | IPSGenericDao.LoadException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
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
    public void storeConfig(PSAnalyticsProviderConfig config)
    {
        try {
            providerService.saveConfig(config);
        } catch (IPSGenericDao.SaveException | IPSGenericDao.LoadException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/config")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSAnalyticsProviderConfig getStoredConfig()
    {
        try {
            return providerService.loadConfig(true);
        } catch (IPSGenericDao.LoadException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
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
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
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
    public String isProfileConfigured(@PathParam("sitename") String sitename)
    {
        try {
            return ((Boolean) providerService.isProfileConfigured(sitename)).toString();
        } catch (IPSGenericDao.LoadException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    private IPSAnalyticsProviderService providerService;
}
