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

package com.percussion.itemmanagement.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.share.data.PSItemProperties;

/***
 * Java representation of the Site Impact of an Asset
 * 
 * @author natechadwick
 *
 */
public class PSAssetSiteImpact {

	private Set<PSItemProperties> ownerPages;
	private Set<PSTemplateSummary> ownerTemplates;
	private Set<IPSSite> ownerSites;
	
	public PSAssetSiteImpact() {
		ownerPages = new HashSet<PSItemProperties>();
		ownerTemplates = new HashSet<PSTemplateSummary>();
		ownerSites = new HashSet<IPSSite>();
	}


	/**
	 * @return the ownerTemplates
	 */
	public Set<PSTemplateSummary> getOwnerTemplates() {
		return ownerTemplates;
	}


	/**
	 * @param ownerTemplates the ownerTemplates to set
	 */
	public void setOwnerTemplates(Set<PSTemplateSummary> ownerTemplates) {
		this.ownerTemplates = ownerTemplates;
	}


	/**
	 * @return the ownerPages
	 */
	public Set<PSItemProperties> getOwnerPages() {
		return ownerPages;
	}


	/**
	 * @param ownerPages the ownerPages to set
	 */
	public void setOwnerPages(Set<PSItemProperties> ownerPages) {
		this.ownerPages = ownerPages;
	}


	/**
	 * @return the ownerSites
	 */
	public Set<IPSSite> getOwnerSites() {
		return ownerSites;
	}


	/**
	 * @param ownerSites the ownerSites to set
	 */
	public void setOwnerSites(Set<IPSSite> ownerSites) {
		this.ownerSites = ownerSites;
	}

}
