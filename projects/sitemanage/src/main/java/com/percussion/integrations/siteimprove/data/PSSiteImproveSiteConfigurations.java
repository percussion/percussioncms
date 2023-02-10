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
 * Base object model for the publish settings for Siteimprove usage for the assigned site
 */
@XmlRootElement(name = "SiteimproveConfiguration")
public class PSSiteImproveSiteConfigurations {

	private String siteName;
	private Boolean doProduction;
	private Boolean doStaging;
	private Boolean doPreview;
	private Boolean isSiteImproveEnabled;

	/**
	 * Empty constructor for Jax-rs to use.
	 */
	public PSSiteImproveSiteConfigurations() {
	}

	/**
	 * @param siteName The name of site the settings are associated with.
	 * @param doProduction True/False to have Siteimprove be used on production sites.
	 * @param doStaging Siteimprove usage for staging sites.
	 * @param doPreview Siteimprove usage for preview sites.
	 */
	public PSSiteImproveSiteConfigurations(String siteName, Boolean doProduction, Boolean doStaging, Boolean doPreview, Boolean isSiteImproveEnabled) {
		this.siteName = siteName;
		this.doProduction = doProduction;
		this.doStaging = doStaging;
		this.doPreview = doPreview;
		this.isSiteImproveEnabled = isSiteImproveEnabled;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public Boolean getDoProduction() {
		return doProduction;
	}

	public void setDoProduction(Boolean doProduction) {
		this.doProduction = doProduction;
	}

	public Boolean getDoStaging() {
		return doStaging;
	}

	public void setDoStaging(Boolean doStaging) {
		this.doStaging = doStaging;
	}

	public Boolean getDoPreview() {
		return doPreview;
	}

	public void setDoPreview(Boolean doPreview) {
		this.doPreview = doPreview;
	}
	
	public void setIsSiteImproveEnabled(Boolean isSiteImproveEnabled) {
		this.isSiteImproveEnabled = isSiteImproveEnabled;
	}
	
	public Boolean getIsSiteImproveEnabled() {
		return isSiteImproveEnabled;
	}
}
