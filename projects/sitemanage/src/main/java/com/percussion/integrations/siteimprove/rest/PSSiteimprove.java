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

package com.percussion.integrations.siteimprove.rest;

import com.percussion.integrations.siteimprove.data.PSSiteImproveCredentials;
import com.percussion.integrations.siteimprove.data.PSSiteImproveSiteConfigurations;
import com.percussion.metadata.data.PSMetadata;
import com.percussion.metadata.data.PSMetadataList;
import com.percussion.metadata.service.IPSMetadataService;
import com.percussion.services.integrations.siteimprove.PSSiteImproveProviderService;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManagerInternal;
import com.percussion.services.sitemgr.PSSiteManagerLocator;

import com.percussion.util.PSSiteManageBean;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints for our integration with Siteimprove
 */
@Path("/siteimprove")
@Lazy
@PSSiteManageBean("siteImproveService")
public class PSSiteimprove {

	private static PSSiteImproveProviderService providerService = new PSSiteImproveProviderService();
	private static IPSSiteManagerInternal sitemgr = (IPSSiteManagerInternal) PSSiteManagerLocator.getSiteManager();
	private IPSMetadataService metadataService;
	private static final Log logger = LogFactory.getLog(PSSiteimprove.class);

	private static final String SITEIMPROVE_CREDENTIALS_BASE_KEY = "perc.siteimprove.credentials.";
	private static final String SITEIMPROVE_CONFIGURATION_BASE_KEY = "perc.siteimprove.site.";
	private static final String EXT_NAME = "Java/global/percussion/task/perc_PSSiteimproveEditionTask";

	/**
	 * Auto wires the metadata service for us to use.
	 *
	 * @param service auto wired service
	 */
	@Autowired
	public PSSiteimprove(IPSMetadataService service) {
		this.metadataService = service;
	}
	
	/**
	 * Retrieve all site improve tokens for the cm1 instance
	 *
	 * @return 200 with the tokens or 500 if it failed to retrieve.
	 */
	@GET
	@Path("/token")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<PSMetadata> retrieveAllSiteImproveTokens() {
		return new PSMetadataList(metadataService.findByPrefix(SITEIMPROVE_CREDENTIALS_BASE_KEY));
	}
	
	/**
	 * Retrieve the tokens for a given site.
	 *
	 * @param siteName The name of the site we want to retrieve the credentials for.
	 * @return 200 with the credentials or 500 if failed to retrieve.
	 */
	@GET
	@Path("/token/{siteName}")
	@Produces(MediaType.APPLICATION_JSON)
	public PSMetadata retrieveSiteImproveTokens(@PathParam("siteName") String siteName) {
		String credentialsKey = SITEIMPROVE_CREDENTIALS_BASE_KEY + siteName;
		return metadataService.find(credentialsKey);
	}
	
	/**
	 * Called when Siteimprove is enabled for a site using the Dashboard gadget.
	 * We do not need a sitename because the token returned is account specific,
	 * not site specific.  The token will be applied to all sites that enable Siteimprove.
	 * Also called if the refresh token methd is called from the 'advanced' button
	 * within the gadget.
	 * @return 200 if successful with a json response with {"token":"token"} or 500 if error
	 */
	@GET
	@Path("/getnewtoken")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSiteImproveTokenForAccount() {
		
		String token = providerService.getNewSiteImproveToken();
		if(!"".equals(token)) {
			return Response.status(200).entity("{\"token\":\""+token+"\"}").build();
		}
		
		return Response.serverError().entity("Unable to get Siteimprove token.").build();
	}

	/**
	 * Updates the Siteimprove token within the PSMetadata object for the site
	 *
	 * @param credentials The Sitename, email address and apikey of the siteimprove account.
	 * @return 200 if valid. 401 if invalid and 500 if we ran into an exception.
	 */
	@PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
	@Path("/token")
	public Response storeSiteImproveToken(PSSiteImproveCredentials credentials) {
		try {

			Map<String, String> credentialsToValidate = new HashMap<>();

			credentialsToValidate.put("token", credentials.getToken());
			credentialsToValidate.put("sitename", credentials.getSiteName());
			credentialsToValidate.put("siteProtocol", credentials.getSiteProtocol());
			credentialsToValidate.put("defaultDocument", credentials.getDefaultDocument());
			credentialsToValidate.put("canonicalDist", credentials.getCanonicalDist());
			Boolean validCredentials = providerService.validateCredentials(credentialsToValidate);

			if (validCredentials) {
				JSONObject jsonMap = new JSONObject();
				jsonMap.accumulateAll(credentialsToValidate);
				addSiteImproveTaskToPreExistingEditions(credentials.getSiteName());
				String metadataKey = SITEIMPROVE_CREDENTIALS_BASE_KEY + credentials.getSiteName();
				metadataService.save(new PSMetadata(metadataKey, jsonMap.toString()));
				return Response.ok().build();
			} 
			
			return Response.status(Response.Status.UNAUTHORIZED).entity("Failed to validate credentials against siteImprove").build();
			
		} catch (Exception e) {
			
			String message = "Failed to store Siteimprove credentials. Exception is: " + e.getMessage();

			logger.error(message, e);

			return Response.serverError().entity(message).build();
		}
	}

	/**
	 * Store or update a site's configuration settings with Site improve.
	 * <p>
	 * * @param publishSettings Preview, Staging, Production settings for the site given.
	 *
	 * @return 200 or server error.
	 */
	@PUT
	@Path("publish/config")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response storeSiteImproveConfiguration(PSSiteImproveSiteConfigurations publishSettings) {
		try {

			// IF invalid settings
			if (!validateConfiguration(publishSettings)) {
				throw new Exception("Settings may not be null and a valid site name must be provided.");
			}

			String siteConfigKey = SITEIMPROVE_CONFIGURATION_BASE_KEY + publishSettings.getSiteName();
			JSONObject publishSettingsJSON = new JSONObject();
			publishSettingsJSON.put("doProduction", publishSettings.getDoProduction());
			publishSettingsJSON.put("doStaging", publishSettings.getDoStaging());
			publishSettingsJSON.put("doPreview", publishSettings.getDoPreview());
			publishSettingsJSON.put("isSiteImproveEnabled", publishSettings.getIsSiteImproveEnabled());

			metadataService.save(new PSMetadata(siteConfigKey, publishSettingsJSON.toString()));
			return Response.ok().build();
		} catch (Exception e) {
			String message = "Failed to save configuration settings for " + publishSettings.getSiteName() + " Exception is " + e.getMessage();
			logger.error(message, e);
			return Response.serverError().entity(message).build();
		}
	}
	
	/**
	 * Remove the siteimprove configuration for a given site.
	 * 
	 * @param siteName The site we want to remove the configuraiton information for.
	 */
	@DELETE
	@Path("delete/config/{siteName}")
	public void deleteSiteImproveConfiguration(@PathParam("siteName") String siteName) {
		metadataService.delete(SITEIMPROVE_CONFIGURATION_BASE_KEY + siteName);
		metadataService.delete(SITEIMPROVE_CREDENTIALS_BASE_KEY + siteName);
		
		try {
			removeSiteImproveTaskFromEditions(siteName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Retrieve siteimprove configuration for the given site.
	 *
	 * @param siteName The site we want to retrieve the configuraiton information from.
	 * @return The siteimprove configuration settings for the associated site.
	 */
	@GET
	@Path("publish/config/{siteName}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public PSMetadata retrieveSiteImproveConfiguration(@PathParam("siteName") String siteName) {
		String siteConfigKey = SITEIMPROVE_CONFIGURATION_BASE_KEY + siteName;
		return metadataService.find(siteConfigKey);
	}


	/**
	 * Retrieve all siteimprove configuration for every site in the system.
	 *
	 * @return Siteimprove configurations.
	 */
	@GET
	@Path("publish/config")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Collection<PSMetadata> retrieveAllSiteImproveConfiguration() {
		return metadataService.findByPrefix(SITEIMPROVE_CONFIGURATION_BASE_KEY);
	}


	/**
	 * Validate that the configuration has no nulls or an empty string for the site name.
	 *
	 * @param configuration The configuration to validate.
	 * @return True if siteName is not null or empty, and that the configuration settings are not null.
	 */
	private Boolean validateConfiguration(PSSiteImproveSiteConfigurations configuration) {
		Boolean validSiteName = configuration.getSiteName() != null && !configuration.getSiteName().isEmpty();
		return validSiteName && configuration.getDoPreview() != null
				&& configuration.getDoStaging() != null && configuration.getDoProduction() != null;
	}

	/**
	 * For the given site, see that all editions for it, add the siteimprove post edition task if it is not there.
	 *
	 * @param siteName The site whose editions we are adding the new post edition task to.
	 * @throws Exception If we encountered an issue adding the new post task definition
	 */
	private void addSiteImproveTaskToPreExistingEditions(String siteName) throws Exception {

		IPSPublisherService pubService = PSPublisherServiceLocator.getPublisherService();
		IPSSite site = sitemgr.findSite(siteName);

		List<IPSEdition> editions = pubService.findAllEditionsBySite(site.getGUID());

		for (IPSEdition edition : editions) {
			List<IPSEditionTaskDef> tasks = pubService.loadEditionTasks(edition.getGUID());
			if (!hasSiteimproveTask(tasks)) {
			   /*
				* Add the siteimprove publication cache post edition task on the editions if they don't have that already
                */
				logger.info("Adding the " + EXT_NAME + " post editiontask on the edition that doesn't have it already");
				IPSEditionTaskDef siteImprovePostEditionTask = pubService.createEditionTask();
				siteImprovePostEditionTask.setContinueOnFailure(true);
				siteImprovePostEditionTask.setEditionId(edition.getGUID());
				siteImprovePostEditionTask.setSequence(3);
				siteImprovePostEditionTask.setExtensionName(EXT_NAME);
				pubService.saveEditionTask(siteImprovePostEditionTask);
				logger.info("Finished adding " + EXT_NAME + " post edition task on the edition");
			}
		}
	}
	
	/**
	 * For the given site, see that all editions for it, remove the siteimprove post edition task if it exists.
	 *
	 * @param siteName The site from whose editions we are removing the post edition task.
	 * @throws Exception If we encountered an issue adding the new post task definition
	 */
	private void removeSiteImproveTaskFromEditions(String siteName) throws Exception {
		
		IPSPublisherService pubService = PSPublisherServiceLocator.getPublisherService();
		IPSSite site = sitemgr.findSite(siteName);

		List<IPSEdition> editions = pubService.findAllEditionsBySite(site.getGUID());
		
		for (IPSEdition edition : editions) {
			List<IPSEditionTaskDef> tasks = pubService.loadEditionTasks(edition.getGUID());
			if (hasSiteimproveTask(tasks)) {

				for (IPSEditionTaskDef task : tasks) {
					if (task.getExtensionName().equals(EXT_NAME)) {
						logger.info("Removing the " + EXT_NAME + " post editiontask from the edition that have it.");
						pubService.deleteEditionTask(task);
						logger.info("Finished removing " + EXT_NAME + " post edition task on the edition");
					}
				}
			}
		}
	}

	/**
	 * Verify that the siteimprove post edition task does not already exist, before we add it.
	 *
	 * @param tasks See if any of the tasks are a Siteimprove task, if so return true so we don't add it a second time.
	 * @return true if there is a pre-existing Siteimprove task, false otherwise.
	 */
	private boolean hasSiteimproveTask(Collection<IPSEditionTaskDef> tasks) {
		for (IPSEditionTaskDef task : tasks) {
			if (task.getExtensionName().equals(EXT_NAME))
				return true;
		}
		return false;
	}

}