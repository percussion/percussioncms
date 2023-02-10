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

package com.percussion.sitemanage.service.impl;

import com.percussion.error.PSExceptionUtils;
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
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @PUT
    @Path("/server/on")
    public void setStagingOn() {
        try {
            setStagingServerEnabledMetadataState(true);
        } catch (IPSGenericDao.SaveException | IPSGenericDao.LoadException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @PUT
    @Path("/server/off")
    public void setStagingOff() {
        try {
            setStagingServerEnabledMetadataState(false);
        } catch (IPSGenericDao.SaveException | IPSGenericDao.LoadException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
