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

package com.percussion.services.integrations;

import java.util.Map;

/**
 * Interface for the services used by the integration REST endpoints.
 */
public interface IPSIntegrationProviderService {

	/**
	 * Used for testing the credentials provided by the user.
	 *
	 * @param credentials The credentials to validate.
	 * @return True if valid, or false if invalid.
	 * @throws Exception We failed to validate whether the credentials were valid or not.
	 */
	Boolean validateCredentials(Map<String, String> credentials) throws Exception;

	/**
	 * Used to retrieve from third party any information related to our site.
	 *
	 * @param siteName    Our site we are retrieving information for.
	 * @param credentials Used to authenticate with third party api.
	 * @return The associated service's site information in String form.
	 * @throws Exception Failed to retrieve information from third party.
	 */
	String retrieveSiteInfo(String siteName, Map<String, String> credentials) throws Exception;

	/**
	 * Update third party information on our site.
	 *
	 * @param siteName    Name of the site we are updating.
	 * @param credentials Used to authenticate with third party api.
	 * @throws Exception Failed to update associated site info.
	 */
	void updateSiteInfo(String siteName, Map<String, String> credentials) throws Exception;

	/**
	 * Retrieve page information associated with url.
	 *
	 * @param siteName    Our main page, i.e. https://www.percussion.com/
	 * @param pageURL     Absolute url to our page. for example: https://www.percussion.com/products
	 * @param credentials Used to authenticate with third party api.
	 * @return Information from the third party regarding the page.
	 * @throws Exception Failed to retrieve information about the page from thirdparty.
	 */
	String retrievePageInfo(String siteName, String pageURL, Map<String, String> credentials) throws Exception;

	/**
	 * Update page information associated with the url.
	 *
	 * @param siteName    Name of the site, the page we are updating on.
	 * @param pageURL     The url of the page we are updating information on.
	 * @param credentials Used to authenticate with third party api.
	 * @throws Exception Failed to update associated page info.
	 */
	void updatePageInfo(String siteName, String pageURL, Map<String, String> credentials) throws Exception;

}
