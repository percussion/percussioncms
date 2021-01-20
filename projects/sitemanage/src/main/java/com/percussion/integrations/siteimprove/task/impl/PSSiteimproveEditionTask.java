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

package com.percussion.integrations.siteimprove.task.impl;

import com.google.common.collect.Iterators;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.integrations.siteimprove.data.PSSiteImproveSiteConfigurations;
import com.percussion.metadata.data.PSMetadata;
import com.percussion.metadata.service.IPSMetadataService;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.pubserver.data.PSPublishServerInfo;
import com.percussion.rx.publisher.IPSEditionTask;
import com.percussion.rx.publisher.IPSEditionTaskStatusCallback;
import com.percussion.services.integrations.IPSIntegrationProviderService;
import com.percussion.services.integrations.siteimprove.PSSiteImproveProviderService;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSSiteItem;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * A post publish edition task that runs if a siteimprove configuration is found.
 * <p>
 * The task alerts siteimprove that pages have been updated, or to crawl the whole site.
 */
public class PSSiteimproveEditionTask implements IPSEditionTask {

	private static final String SITEIMPROVE_CONFIGURATION_BASE_KEY = "perc.siteimprove.site.";
	private static final String SITEIMPROVE_CREDENTIALS_BASE_KEY = "perc.siteimprove.credentials.";
	private static final String TOKEN = "token";
	private static final String SITE_NAME = "sitename";
	private static final String DO_PRODUCTION = "doProduction";
	private static final String DO_STAGING = "doStaging";
	private static final String DO_PREVIEW = "doPreview";
	private static final String IS_SITEIMPROVE_ENABLED = "isSiteImproveEnabled";
	private static final String HTTPS = "https";
	private static final String HTTP = "http";

	private static IPSIntegrationProviderService siteimproveService = new PSSiteImproveProviderService();
	private static Logger logger = Logger.getLogger(PSSiteimproveEditionTask.class);
	private IPSMetadataService metadataService;
	private IPSPubServerService pubServerService;

	@Override
	public void init(IPSExtensionDef def, File codeRoot) {
		PSSpringWebApplicationContextUtils.injectDependencies(this);
	}

	@Override
	public void perform(IPSEdition edition, IPSSite site, Date startTime, Date endTime, long jobId, long duration, boolean success, Map<String, String> params, IPSEditionTaskStatusCallback status) throws Exception {

		logger.info("Starting Siteimprove post edition task.");

		PSMetadata credentialsMetadata = metadataService.find(SITEIMPROVE_CREDENTIALS_BASE_KEY + site.getName());
		PSMetadata siteConfiguration = metadataService.find(SITEIMPROVE_CONFIGURATION_BASE_KEY + site.getName());

		if (credentialsMetadata == null || siteConfiguration == null) {
			logger.debug("Did not find Siteimprove credentials or configurations for: " + site.getName()
					+ ".  Exiting Siteimprove post edition task.");
			return;
		}

		Map<String, String> credentials = obtainToken(credentialsMetadata.getData());
		PSSiteImproveSiteConfigurations siteConfigurations = obtainSiteConfiguration(siteConfiguration.getData());

		PSPublishServerInfo publishServerInfo
				= pubServerService.getPubServer(site.getSiteId().toString(), Long.toString(edition.getPubServerId().longValue()));

		Iterable<IPSPubItemStatus> jobPages = status.getIterableJobStatus();
		int pageCount = Iterators.size(jobPages.iterator());
		String siteBaseUrl = getBaseURL(site);
		handleProductionOrStagingEnabled(credentials, siteConfigurations, publishServerInfo, jobPages, pageCount, siteBaseUrl, edition);

	}
	
	private void handleProductionOrStagingEnabled(Map<String, String> credentials, PSSiteImproveSiteConfigurations siteConfigurations, PSPublishServerInfo publishServerInfo, Iterable<IPSPubItemStatus> jobPages, int pageCount, String siteBaseUrl, IPSEdition edition) throws Exception {
		if (isProductionEnabled(siteConfigurations, publishServerInfo) || isStagingEnabled(siteConfigurations, publishServerInfo)) {

			alertSiteimproveToNewPublishes(credentials, jobPages, pageCount, siteBaseUrl, edition);

			logger.info("Submitted " + pageCount + " URL(s) to siteimprove.");
			logger.info("Ending Siteimprove post edition task.");

		} else {
			logger.info("No production or staging settings are configured for Siteimprove, publishing no URLs to Siteimprove.");
		}
	}

	private String getBaseURL(IPSSite site) {
		String siteBaseUrl = site.getBaseUrl();
		
		// if the user doesn't have canonical urls set, we set to http as default.
		if(site.getSiteProtocol() == null)
			site.setSiteProtocol(HTTP);

		//retrieve siteimprove site id.
		if (!siteBaseUrl.contains(HTTPS) && site.getSiteProtocol().equals(HTTPS)) {
			siteBaseUrl = convertHTTPtoHTTPS(siteBaseUrl);
		}
		return siteBaseUrl;
	}
	
	/**
	 * Alert Siteimprove that we have published new pages for them to spider, or do a full crawl of the site.
	 *
	 * @param credentials Credentials to access the Siteimprove API.
	 * @param jobPages    The list of pages we are publishing.
	 * @param pageCount   The amount of pages we are publishing
	 * @param siteBaseUrl Our site's base URL.  i.e. https://www.percussion.com/
	 * @param siteId      Siteimprove's site id of our site.
	 * @throws Exception We failed to alert SiteImprove to our new publishes
	 */
	private void alertSiteimproveToNewPublishes(Map<String, String> credentials, Iterable<IPSPubItemStatus> jobPages,
												int pageCount, String siteBaseUrl, IPSEdition edition) throws Exception {
		if (edition.getDisplayTitle().contains("_FULL")) {
			siteimproveService.updateSiteInfo(siteBaseUrl, credentials);
		} else {
			//individual page checks
			for (IPSPubItemStatus jobPage : jobPages) {
				//remove last forward slash
				String url = StringUtils.removeEnd(siteBaseUrl, "/") + jobPage.getLocation();

				if (jobPage.getStatus().equals(IPSSiteItem.Status.SUCCESS)) {
					siteimproveService.updatePageInfo(siteBaseUrl, url, credentials);
				} else {
					logger.debug("Did not submit page: " + jobPage.getContentId() +
							" to siteimprove because of page's status of " + jobPage.getStatus());
				}

			}
		}
	}

	/**
	 * Determine if we are a production publish and that production is enabled.
	 *
	 * @param siteConfigurations Our configuration settings.
	 * @param publishServerInfo  Our publish settings.
	 * @return true or false
	 */
	private boolean isProductionEnabled(PSSiteImproveSiteConfigurations siteConfigurations, PSPublishServerInfo publishServerInfo) {

		boolean isEnabled = PSPubServer.PRODUCTION.equalsIgnoreCase(publishServerInfo.getServerType()) && siteConfigurations.getDoProduction();

		if (isEnabled) {
			logger.debug("Production configuration is enabled for this site for siteimprove, alerting siteimprove to update indices.");
		}

		return isEnabled;
	}

	/**
	 * Determine if we are a staging publish and that staging is enabled.
	 *
	 * @param siteConfigurations Our configuration settings.
	 * @param publishServerInfo  Our publish settings.
	 * @return true or false
	 */
	private boolean isStagingEnabled(PSSiteImproveSiteConfigurations siteConfigurations, PSPublishServerInfo publishServerInfo) {

		boolean isEnabled = PSPubServer.STAGING.equalsIgnoreCase(publishServerInfo.getServerType()) && siteConfigurations.getDoStaging();

		if (isEnabled) {
			logger.debug("Staging configuration is enabled for this site for siteimprove, alerting siteimprove to update indices.");
		}

		return isEnabled;
	}

	/**
	 * Convert a url that is http to https.
	 * For example:
	 * http://www.percussion.com/ -> https://www.percussion.com/
	 *
	 * @param siteBaseUrl Our url to convert.
	 * @return Our converted url.
	 */
	private String convertHTTPtoHTTPS(String siteBaseUrl) {

		return StringUtils.replace(siteBaseUrl, HTTP, HTTPS);

	}
	
	/**
	 * Obtain the token and site name from a metadata json.
	 *
	 * @param credentialsData A credentials map to be parsed by our Siteimprove services.
	 * @return The parsed token and sitename
	 * @throws Exception Missing a part of the credentials.
	 */
	private Map<String, String> obtainToken(String credentialsData) throws Exception {

		JSONObject credentialsJSON = JSONObject.fromObject(credentialsData);

		Map<String, String> credentials = new HashMap<String, String>();


		if (!credentialsJSON.has(SITE_NAME)) {
			String message = "The credentials were missing the associated site name.";
			logger.error(message);
			throw new Exception(message);
		}

		if (!credentialsJSON.has(TOKEN)) {
			String message = "The credentials were missing the apikey.";
			logger.error(message);
			throw new Exception(message);
		}

		credentials.put(SITE_NAME, credentialsJSON.getString(SITE_NAME));
		credentials.put(TOKEN, credentialsJSON.getString(TOKEN));
		credentials.put("siteProtocol", credentialsJSON.getString("siteProtocol"));
		credentials.put("defaultDocument", credentialsJSON.getString("defaultDocument"));
		credentials.put("canonicalDist", credentialsJSON.getString("canonicalDist"));

		return credentials;
	}

	/**
	 * Parse metadata json for our Siteimprove configuration settings.
	 *
	 * @param siteConfigurationData Our json to parse
	 * @return A Siteimprove configuration object
	 * @throws Exception Missing configuration setting, all settings are either true or false.
	 */
	private PSSiteImproveSiteConfigurations obtainSiteConfiguration(String siteConfigurationData) throws Exception {

		JSONObject siteConfigurationJson = JSONObject.fromObject(siteConfigurationData);

		if (!siteConfigurationJson.has(DO_PRODUCTION)) {
			String message = "Siteimprove configuration details were missing the production setting";
			logger.error(message);
			throw new Exception(message);
		}

		if (!siteConfigurationJson.has(DO_STAGING)) {
			String message = "Siteimprove configuration details were missing the staging setting";
			logger.error(message);
			throw new Exception(message);
		}

		if (!siteConfigurationJson.has(DO_PREVIEW)) {
			String message = "Siteimprove configuration details were missing the preview setting";
			logger.error(message);
			throw new Exception(message);
		}
		
		if (!siteConfigurationJson.has(IS_SITEIMPROVE_ENABLED)) {
			String message = "Siteimprove configuration details were missing the Siteimprove enabled setting";
			logger.error(message);
			throw new Exception(message);
		}

		PSSiteImproveSiteConfigurations siteConfiguration = new PSSiteImproveSiteConfigurations();
		siteConfiguration.setDoProduction(siteConfigurationJson.getBoolean(DO_PRODUCTION));
		siteConfiguration.setDoStaging(siteConfigurationJson.getBoolean(DO_STAGING));
		siteConfiguration.setDoPreview(siteConfigurationJson.getBoolean(DO_PREVIEW));
		siteConfiguration.setIsSiteImproveEnabled(siteConfigurationJson.getBoolean(IS_SITEIMPROVE_ENABLED));

		return siteConfiguration;
	}

	@Override
	public TaskType getType() {
		return TaskType.POSTEDITION;
	}

	public IPSPubServerService getPubServerService() {
		return pubServerService;
	}

	public void setPubServerService(IPSPubServerService pubServerService) {
		this.pubServerService = pubServerService;
	}

	public IPSMetadataService getMetadataService() {
		return metadataService;
	}

	public void setMetadataService(IPSMetadataService metadataService) {
		this.metadataService = metadataService;
	}

}