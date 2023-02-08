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

package com.percussion.integrations.siteimprove.rest;

import com.percussion.integrations.siteimprove.data.PSSiteImproveCredentials;
import com.percussion.integrations.siteimprove.data.PSSiteImproveSiteConfigurations;
import com.percussion.share.test.PSDataServiceRestClient;

/**
 *
 */
public class PSSiteimproveRestClient extends PSDataServiceRestClient<PSSiteimprove> {

	private static final String TEST_URL = "/Rhythmyx/services/integrations/siteimprove";
	private static final String CREDENTIALS = "/credentials/";
	private static final String CONFIGURATIONS = "/publish/config/";

	/**
	 * Base constructor for our Rest Client to use our siteimprove endpoints.
	 *
	 * @param baseUrl Url to test against in unit tests (eg. localhost:9992)
	 */
	public PSSiteimproveRestClient(String baseUrl) {
		super(PSSiteimprove.class, baseUrl, TEST_URL);
	}

	/**
	 * Hit our siteimprove credentials endpoint with siteimprove credentials to validate and store.
	 *
	 * @param credentials Credentials to try and store.
	 * @return Results, should be blank.
	 */
	public String storeCredentials(PSSiteImproveCredentials credentials) {
		return putObjectToPath(concatPath(getPath(), CREDENTIALS), credentials);
	}

	/**
	 * Hits the retrieve endpoint for siteimprove credentials.
	 *
	 * @param siteName Retrieve for the given site name.
	 * @return The credentials for the site if found.
	 */
	public String retrieveCredentials(String siteName) {
		return GET(concatPath(getPath(), CREDENTIALS, siteName));
	}

	/**
	 * Hits the retrieve all endpoint for siteimprove credentials.
	 *
	 * @return All credentials found.
	 */
	public String retrieveAllCredentials() {
		return GET(concatPath(getPath(), CREDENTIALS));
	}

	/**
	 * Hits the site configure endpoint to store publishing configurations for siteimprove usage.
	 *
	 * @param configurations The credentials to try and store.
	 * @return Results, should be blank.
	 */
	public String storeSiteConfig(PSSiteImproveSiteConfigurations configurations) {
		return putObjectToPath(concatPath(getPath(), CONFIGURATIONS), configurations);
	}

	/**
	 * Hits the siteimprove configure endpoint to retrieve publish configurations.
	 *
	 * @param siteName The site whose configuration we want.
	 * @return The configuration for the site if found.
	 */
	public String retrieveSiteConfig(String siteName) {
		return GET(concatPath(getPath(), CONFIGURATIONS, siteName));
	}

	/**
	 * Retrieve all site configurations at the configuration endpoint for site improve.
	 *
	 * @return All site configurations for siteimprove.
	 */
	public String retrieveAllSiteConfig() {
		return GET(concatPath(getPath(), CONFIGURATIONS));
	}
}
