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

package com.percussion.pso.demandpreview.service;

import com.percussion.pso.demandpreview.exception.SiteLookUpException;
import com.percussion.utils.guid.IPSGuid;

import java.util.Set;

/**
 * Site Lookup Service
 * 
 *
 * @author davidbenua
 *
 */
public interface SiteEditionLookUpService 
{
    /**
     * Look up the preview information by site id. 
     * @param siteId the site id. 
     * @return the site preview site, edition, template and context. 
     * @throws SiteLookUpException when the site preview information cannot located
     * or the configuration information is invalid. 
     */
	public SiteEditionHolder LookUpSiteEdition(String siteId)throws SiteLookUpException;
	
	/**
	 * Look up the preview information by site GUID. 
	 * @param siteId the site GUID.
	 * @return the site preview site, edition, template and context. 
	 * @throws SiteLookUpException when the site preview information cannot located
     * or the configuration information is invalid.
	 */
	public SiteEditionHolder LookUpSiteEdition(IPSGuid siteId) throws SiteLookUpException;
	
	/**
	 * Gets the site names that appear in the configuration. 
	 * @return the site names. Never <code>null</code> but
	 * may be <code>empty</code>.
	 */
	public Set<String> getConfiguredSiteNames(); 
}