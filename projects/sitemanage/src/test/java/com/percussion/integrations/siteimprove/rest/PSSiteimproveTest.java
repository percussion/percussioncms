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
import com.percussion.share.test.PSRestClient.RestClientException;
import com.percussion.share.test.PSRestTestCase;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.UUID;

@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PSSiteimproveTest extends PSRestTestCase<PSSiteimproveRestClient> {


	// Testing Resources
	private static final String TESTING_USER = "percussionbot@gmail.com";
	private static final String TESTING_TOKEN = "388c3dc6a18b9582baada754b1408b7e";
	private static final String TESTING_SITENAME = "unitTest";

	/**
	 * @param baseUrl base url for unit testing
	 * @return Construct the rest client for testing purposes.
	 */
	@Override
	protected PSSiteimproveRestClient getRestClient(String baseUrl) {
		return new PSSiteimproveRestClient(baseUrl);
	}

	@Test
	public void a_storeCredentialsTest() throws Exception {
		PSSiteImproveCredentials credentials = new PSSiteImproveCredentials(TESTING_SITENAME, TESTING_TOKEN);
		restClient.storeCredentials(credentials);
	}

	@Test
	public void b_storebadCredentialsTest() throws Exception {
		try {
			PSSiteImproveCredentials credentials = new PSSiteImproveCredentials(UUID.randomUUID().toString(),
					UUID.randomUUID().toString());
			restClient.storeCredentials(credentials);
		} catch (Exception exception) {
			Assert.assertTrue(exception instanceof RestClientException);
			RestClientException restClientException = (RestClientException) exception;
			Assert.assertTrue(restClientException.getStatus() == 401);
		}
	}

	@Test
	public void c_storeSiteConfigurationTest() throws Exception {
		PSSiteImproveSiteConfigurations configurations = new PSSiteImproveSiteConfigurations(TESTING_SITENAME, true, false, true, null);
		restClient.storeSiteConfig(configurations);
	}

	@Test
	public void d_storeBadSiteConfigurationTest() throws Exception {
		try {
			PSSiteImproveSiteConfigurations configurations = new PSSiteImproveSiteConfigurations(null, null, null, null, null);
			restClient.storeSiteConfig(configurations);
		} catch (Exception exception) {
			Assert.assertTrue(exception instanceof RestClientException);
			RestClientException restClientException = (RestClientException) exception;
			Assert.assertTrue(restClientException.getStatus() == 500);
		}
	}

	@Test
	public void retrieveSiteCredentialsTest() throws Exception {
		String results = restClient.retrieveCredentials(TESTING_SITENAME);
		Assert.assertNotNull(results);
		Assert.assertTrue(results.contains("perc.siteimprove.credentials." + TESTING_SITENAME));
		Assert.assertTrue(results.contains(TESTING_USER));
		Assert.assertTrue(results.contains(TESTING_TOKEN));
	}

	@Test
	public void retrieveAllCredentialsTest() throws Exception {
		String results = restClient.retrieveAllCredentials();

		Assert.assertNotNull(results);
		Assert.assertTrue(results.length() > 0);
		Assert.assertTrue(results.contains(TESTING_SITENAME));
		Assert.assertTrue(results.contains(TESTING_TOKEN));
		Assert.assertTrue(results.contains(TESTING_USER));
	}

	@Test
	public void retrieveBadCredentialsTest() throws Exception {
		String results = null;
		try {
			results = restClient.retrieveCredentials(UUID.randomUUID().toString());
		} catch (Exception exception) {
			Assert.assertTrue(exception instanceof RestClientException);
		}
		Assert.assertNull(results);
	}

	@Test
	public void retrieveSiteConfigurationTest() throws Exception {
		String results = restClient.retrieveSiteConfig(TESTING_SITENAME);
		Assert.assertNotNull(results);
		Assert.assertTrue(results.contains("doStaging"));
		Assert.assertTrue(results.contains("false"));
	}

	@Test
	public void retrieveAllSiteConfigurationsTest() throws Exception {
		String results = restClient.retrieveAllSiteConfig();

		Assert.assertNotNull(results);
		Assert.assertTrue(results.length() > 0);
		Assert.assertTrue(results.contains(TESTING_SITENAME));
		Assert.assertTrue(results.contains("doPreview"));
		Assert.assertTrue(results.contains("doProduction"));
		Assert.assertTrue(results.contains("true"));
	}

	@Test
	public void retrievebadSiteConfigTest() throws Exception {
		String results = null;
		try {
			results = restClient.retrieveSiteConfig(UUID.randomUUID().toString());
		} catch (Exception exception) {
			Assert.assertTrue(exception instanceof RestClientException);
		}
		Assert.assertNull(results);
	}

}
