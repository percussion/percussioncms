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

package com.percussion.sitemanage.service.impl;

import com.percussion.metadata.data.PSMetadata;
import com.percussion.metadata.service.IPSMetadataService;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.IPSSystemProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.helper.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

@Path("/publish/staging")
@Component("publishStagingService")
@Lazy
public class PSPublishStagingService {

    private static final Logger log = LogManager.getLogger(PSPublishStagingService.class);

    private IPSSystemProperties systemProps;
    
    private IPSMetadataService metadata;
    private static final String STAGING_SERVER_ENABLED = "STAGING_SERVER_ENABLED";

    @Autowired
    public PSPublishStagingService(IPSMetadataService metadata) {
	Validate.notNull(metadata);
	this.metadata = metadata;
    }

    /**
     * Set the system properties on this service. This service will always use
     * the the values provided by the most recently set instance of the
     * properties.
     * 
     * @param systemProps
     *            the system properties
     */
    @Autowired
    public void setSystemProps(IPSSystemProperties systemProps) {
	this.systemProps = systemProps;
    }

    /**
     * Gets the system properties used by this service.
     * 
     * @return The properties
     */
    public IPSSystemProperties getSystemProps() {
	return systemProps;
    }

    @GET
    @Path("/feature/enabled")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean isStagingFeatureEnabled() {
	String enabled = getSystemProps().getProperty("isStagingEnabled");
	if (enabled == null) {
	    enabled = "false";
	}
	return Boolean.parseBoolean(enabled);

    }

    @GET
    @Path("/server/enabled")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean isStagingServerEnabled() {
        try {
            boolean stagingServerEnabled = false;
            PSMetadata metadataItem = metadata.find(STAGING_SERVER_ENABLED);
            if (metadataItem == null) {
                setStagingOff();
            } else {
                try {
                    stagingServerEnabled = Boolean.parseBoolean(metadataItem
                            .getData());
                } catch (Exception e) {
                    setStagingOff();
                }
            }
            return stagingServerEnabled;
        } catch (IPSGenericDao.LoadException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @PUT
    @Path("/server/on")
    public void setStagingOn() {
        try {
            setStagingServerEnabledMetadataState(true);
        } catch (IPSGenericDao.SaveException | IPSGenericDao.LoadException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @PUT
    @Path("/server/off")
    public void setStagingOff() {
        try {
            setStagingServerEnabledMetadataState(false);
        } catch (IPSGenericDao.SaveException | IPSGenericDao.LoadException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    private void setStagingServerEnabledMetadataState(boolean state) throws IPSGenericDao.LoadException, IPSGenericDao.SaveException {
	PSMetadata metadataItem = new PSMetadata();
	metadataItem.setKey(STAGING_SERVER_ENABLED);
	metadataItem.setData(Boolean.toString(state));
	metadata.save(metadataItem);
    }

    @GET
    @Path("/active")
    public boolean isStagingActive() {
	return (isStagingFeatureEnabled() && isStagingServerEnabled());
    }

}
