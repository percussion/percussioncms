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
