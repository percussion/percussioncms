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
		ownerPages = new HashSet<>();
		ownerTemplates = new HashSet<>();
		ownerSites = new HashSet<>();
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
