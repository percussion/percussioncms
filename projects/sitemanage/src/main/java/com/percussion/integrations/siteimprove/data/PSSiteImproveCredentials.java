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
