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
