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

package com.percussion.integrations.siteimprove.task.impl;

import com.google.common.collect.Iterators;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.integrations.siteimprove.data.PSSiteImproveSiteConfigurations;
import com.percussion.metadata.data.PSMetadata;
import com.percussion.metadata.service.IPSMetadataService;
import com.percussion.pagemanagement.assembler.impl.PSAssemblyConfig;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.pubserver.data.PSPublishServerInfo;
import com.percussion.rx.publisher.IPSEditionTask;
import com.percussion.rx.publisher.IPSEditionTaskStatusCallback;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
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
import java.util.concurrent.ConcurrentHashMap;

import com.percussion.utils.guid.IPSGuid;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A post publish edition task that runs if a site improve configuration is found.
 * <p>
 * The task alerts site improve that pages have been updated, or to crawl the whole site.
 */
public class PSSiteimproveEditionTask implements IPSEditionTask {

	private static final String SITEIMPROVE_CONFIGURATION_BASE_KEY = "perc.siteimprove.site.";
	private static final String SITEIMPROVE_CREDENTIALS_BASE_KEY = "perc.siteimprove.credentials.";
	private static final String TOKEN = "token";
	private static final String SITE_NAME = "sitename";
	private static final String DO_PRODUCTION = "doProduction";
	private static final String DO_STAGING = "doStaging";
	private static final String DO_ASSETS_SCAN_EXCLUDE = "doAssetsScanExclude";
	private static final String DO_PREVIEW = "doPreview";
	private static final String IS_SITEIMPROVE_ENABLED = "isSiteImproveEnabled";
	private static final String HTTPS = "https";
	private static final String HTTP = "http";
	ConcurrentHashMap<Long, String> templateDetails = new ConcurrentHashMap<>();
	private static IPSIntegrationProviderService siteimproveService = new PSSiteImproveProviderService();
	private static final Logger logger = LogManager.getLogger(PSSiteimproveEditionTask.class);
	private IPSMetadataService metadataService;
	private IPSPubServerService pubServerService;

	@Override
	public void init(IPSExtensionDef def, File codeRoot) {
		PSSpringWebApplicationContextUtils.injectDependencies(this);
	}

	@Override
	public void perform(IPSEdition edition, IPSSite site, Date startTime, Date endTime, long jobId, long duration, boolean success, Map<String, String> params, IPSEditionTaskStatusCallback status) throws Exception {

		logger.info("Starting Site improve post edition task.");

		PSMetadata credentialsMetadata = metadataService.find(SITEIMPROVE_CREDENTIALS_BASE_KEY + site.getName());
		PSMetadata siteConfiguration = metadataService.find(SITEIMPROVE_CONFIGURATION_BASE_KEY + site.getName());

		if (credentialsMetadata == null || siteConfiguration == null) {
			logger.debug("Did not find Site improve credentials or configurations for: {}.  Exiting Site improve post edition task.",
                    site.getName());
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

			alertSiteImproveToNewPublishes(credentials, jobPages, siteBaseUrl, edition, siteConfigurations);

			logger.info("Submitted {} URL(s) to site improve.",pageCount);
			logger.info("Ending Site improve post edition task.");

		} else {
			logger.info("No production or staging settings are configured for Site improve, publishing no URLs to Site improve.");
		}
	}

	private String getBaseURL(IPSSite site) {
		String siteBaseUrl = site.getBaseUrl();
		
		// if the user doesn't have canonical urls set, we set to http as default.
		if(site.getSiteProtocol() == null)
			site.setSiteProtocol(HTTP);

		//retrieve site improve site id.
		if (!siteBaseUrl.contains(HTTPS) && site.getSiteProtocol().equals(HTTPS)) {
			siteBaseUrl = convertHTTPtoHTTPS(siteBaseUrl);
		}
		return siteBaseUrl;
	}
	
	/**
	 * Alert Site improve that we have published new pages for them to spider, or do a full crawl of the site.
	 *
	 * @param credentials Credentials to access the Site improve API.
	 * @param jobPages    The list of pages we are publishing.
	 * @param siteBaseUrl Our site's base URL.  i.e. https://www.percussion.com/
	 * @throws Exception We failed to alert SiteImprove to our new publishes
	 */
	private void alertSiteImproveToNewPublishes(Map<String, String> credentials, Iterable<IPSPubItemStatus> jobPages,
												String siteBaseUrl, IPSEdition edition, PSSiteImproveSiteConfigurations siteConfigurations) throws Exception {
		if (edition.getDisplayTitle().contains("_FULL")) {
			siteimproveService.updateSiteInfo(siteBaseUrl, credentials);
		} else if(isAssetsScanExcludeEnabled(siteConfigurations)){
			//This condition checks whether we have to exclude assets from scanning
			//individual page checks
			for (IPSPubItemStatus jobPage : jobPages) {
				//This condition excludes assets from scanning and scans only pages.
				if(isTemplateMatch(jobPage) && !jobPage.getLocation().startsWith("/Assets")) {
					alertSiteImproveUpdatePageInfo(credentials,jobPage,siteBaseUrl);
				}
			}
		} else{
			//individual page checks
			for (IPSPubItemStatus jobPage : jobPages) {
				// This method updates the pages.
				alertSiteImproveUpdatePageInfo(credentials,jobPage,siteBaseUrl);
			}
		}
	}

	/**
	 * Determine if we are a production publish and that production is enabled.
	 *
	 * @param siteConfigurations Our configuration settings.
	 * @param publishServerInfo  Our publishing settings.
	 * @return true or false
	 */
	private boolean isProductionEnabled(PSSiteImproveSiteConfigurations siteConfigurations, PSPublishServerInfo publishServerInfo) {

		boolean isEnabled = PSPubServer.PRODUCTION.equalsIgnoreCase(publishServerInfo.getServerType()) && siteConfigurations.getDoProduction();

		if (isEnabled) {
			logger.debug("Production configuration is enabled for this site for site improve, alerting site improve to update indices.");
		}

		return isEnabled;
	}

	/**
	 * Determine if we are a staging publish and that staging is enabled.
	 *
	 * @param siteConfigurations Our configuration settings.
	 * @param publishServerInfo  Our publishing settings.
	 * @return true or false
	 */
	private boolean isStagingEnabled(PSSiteImproveSiteConfigurations siteConfigurations, PSPublishServerInfo publishServerInfo) {

		boolean isEnabled = PSPubServer.STAGING.equalsIgnoreCase(publishServerInfo.getServerType()) && siteConfigurations.getDoStaging();

		if (isEnabled) {
			logger.debug("Staging configuration is enabled for this site for site improve, alerting site improve to update indices.");
		}

		return isEnabled;
	}

	/**
	 * Determine if assets scan exclude is enabled.
	 *
	 * @param siteConfigurations Our configuration settings.
	 * @return true or false
	 */
	private boolean isAssetsScanExcludeEnabled(PSSiteImproveSiteConfigurations siteConfigurations) {

		boolean isEnabled = siteConfigurations.getDoAssetsScanExclude();

		if (isEnabled) {
			logger.debug("Assets scan exclude is enabled for this site for site improve, alerting site improve to update indices.");
		}

		return isEnabled;
	}

	/**
	 * Convert an url that is http to https.
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
	 * @param credentialsData A credentials map to be parsed by our Site improve services.
	 * @return The parsed token and site name
	 * @throws Exception Missing a part of the credentials.
	 */
	private Map<String, String> obtainToken(String credentialsData) throws Exception {

		JSONObject credentialsJSON = JSONObject.fromObject(credentialsData);

		Map<String, String> credentials = new HashMap<>();


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
	 * Parse metadata json for our Site improve configuration settings.
	 *
	 * @param siteConfigurationData Our json to parse
	 * @return A Site improve configuration object
	 * @throws Exception Missing configuration setting, all settings are either true or false.
	 */
	private PSSiteImproveSiteConfigurations obtainSiteConfiguration(String siteConfigurationData) throws Exception {

		JSONObject siteConfigurationJson = JSONObject.fromObject(siteConfigurationData);

		if (!siteConfigurationJson.has(DO_PRODUCTION)) {
			String message = "Site improve configuration details were missing the production setting";
			logger.error(message);
			throw new Exception(message);
		}

		if (!siteConfigurationJson.has(DO_STAGING)) {
			String message = "Site improve configuration details were missing the staging setting";
			logger.error(message);
			throw new Exception(message);
		}

		if (!siteConfigurationJson.has(DO_ASSETS_SCAN_EXCLUDE)) {
			String message = "Site improve configuration details were missing the assets scan exclude setting";
			logger.error(message);
			throw new Exception(message);
		}

		if (!siteConfigurationJson.has(DO_PREVIEW)) {
			String message = "Site improve configuration details were missing the preview setting";
			logger.error(message);
			throw new Exception(message);
		}
		
		if (!siteConfigurationJson.has(IS_SITEIMPROVE_ENABLED)) {
			String message = "Site improve configuration details were missing the Site improve enabled setting";
			logger.error(message);
			throw new Exception(message);
		}

		PSSiteImproveSiteConfigurations siteConfiguration = new PSSiteImproveSiteConfigurations();
		siteConfiguration.setDoProduction(siteConfigurationJson.getBoolean(DO_PRODUCTION));
		siteConfiguration.setDoStaging(siteConfigurationJson.getBoolean(DO_STAGING));
		siteConfiguration.setDoAssetsScanExclude(siteConfigurationJson.getBoolean(DO_ASSETS_SCAN_EXCLUDE));
		siteConfiguration.setDoPreview(siteConfigurationJson.getBoolean(DO_PREVIEW));
		siteConfiguration.setIsSiteImproveEnabled(siteConfigurationJson.getBoolean(IS_SITEIMPROVE_ENABLED));

		return siteConfiguration;
	}

	/**
	 * Determine if the template name of the job page is matching with the standard template name.
	 *
	 * @param jobPage Individual Pages that we want to update through site improve.
	 * @return true or false
	 */
	private boolean isTemplateMatch(IPSPubItemStatus jobPage) throws PSAssemblyException {
		boolean isEnabled = false;
		Long templateId = jobPage.getTemplateId();
		String templateName = "";
		if(!templateDetails.isEmpty() && templateDetails.containsKey(templateId)){
			templateName = templateDetails.get(templateId);
		} else {
			IPSAssemblyService assembly = PSAssemblyServiceLocator.getAssemblyService();
			IPSGuid guid = PSGuidManagerLocator.getGuidMgr().makeGuid(templateId, PSTypeEnum.TEMPLATE);
			IPSAssemblyTemplate template = assembly.loadUnmodifiableTemplate(guid);
			templateName = template.getName();
			templateDetails.put(templateId,templateName);
		}
		if(templateName.equals(PSAssemblyConfig.PERC_RESOURCE_ASSEMBLY_TEMPLATE)){
			isEnabled = true;
		}
		return isEnabled;
	}

	/**
	 * Alert Site improve that we have published new pages for them to spider, or do a full crawl of the site.
	 *
	 * @param credentials Credentials to access the Site improve API.
	 * @param jobPage Individual Pages that we want to update through site improve.
	 * @param siteBaseUrl Our site's base URL.  i.e. https://www.percussion.com/
	 * @throws Exception We failed to alert SiteImprove to our new publishes
	 */
	private void alertSiteImproveUpdatePageInfo(Map<String, String> credentials, IPSPubItemStatus jobPage,
												String siteBaseUrl) throws Exception {
	//remove last forward slash
		String url = StringUtils.removeEnd(siteBaseUrl, "/") + jobPage.getLocation();
		if (jobPage.getStatus().equals(IPSSiteItem.Status.SUCCESS)) {
			siteimproveService.updatePageInfo(siteBaseUrl, url, credentials);
		} else {
			logger.debug("Did not submit page:{} to site improve because of page's status of {}" ,jobPage.getContentId(), jobPage.getStatus());
		}
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
