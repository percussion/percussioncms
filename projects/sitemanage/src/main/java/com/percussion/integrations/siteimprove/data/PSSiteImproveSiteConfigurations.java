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
