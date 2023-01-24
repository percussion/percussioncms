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

package com.percussion.integrations.siteimprove.data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Siteimprove credentials to access their api.
 */
@XmlRootElement(name = "SiteimproveCredentials")
public class PSSiteImproveCredentials {

	private String siteName;
	private String token;
	private String siteProtocol;
	private String defaultDocument;
	private String canonicalDist;

	/**
	 * Empty constructor for jax-rs to use.
	 */
	public PSSiteImproveCredentials() {
	}

	/**
	 * @param siteName the name of the site to associate credentials with
	 * @param token the token generated from the Siteimprove GET token endpoint for the site
	 * the token should be persisted in the PSMetadata object.
	 */
	public PSSiteImproveCredentials(String token, String siteName) {
		this.siteName = siteName;
		this.token = token;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public String getToken() {
		return token;
	}
	
	public String getSiteProtocol() {
		return siteProtocol;
	}

	public void setSiteProtocol(String protocol) {
		this.siteProtocol = protocol;
	}

	public String getDefaultDocument() {
		return defaultDocument;
	}

	public void setDefaultDocument(String defaultDocument) {
		this.defaultDocument = defaultDocument;
	}

	public String getCanonicalDist() {
		return canonicalDist;
	}

	public void setCanonicalDist(String canonicalDist) {
		this.canonicalDist = canonicalDist;
	}

}
