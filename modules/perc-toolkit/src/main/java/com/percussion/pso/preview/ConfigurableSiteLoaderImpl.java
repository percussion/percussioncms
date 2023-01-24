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
package com.percussion.pso.preview;

import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.PSSiteManagerException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DavidBenua
 *
 */
public class ConfigurableSiteLoaderImpl extends CachingSiteLoaderImpl
   implements SiteLoader 
{

	private static final Logger log = LogManager.getLogger(ConfigurableSiteLoaderImpl.class);
	
	private List<String> allowedSites; 

	/**
	 * 
	 */
	public ConfigurableSiteLoaderImpl() {
		super(); 
		allowedSites = new ArrayList<String>(); 
	}

	@Override
	public synchronized List<IPSSite> loadAllSites() throws PSSiteManagerException {
		List<IPSSite> allSites = super.loadAllSites();
		List<IPSSite> mySites = new ArrayList<IPSSite>(); 
		
		for(IPSSite site : allSites)
		{
			if(allowedSites.contains(site.getName()))
			{
				log.debug("found allowed site {}", site.getName());
				mySites.add(site); 
			}
			else
			{
				log.debug("ignoring site {}", site.getName());
			}
		}
		
		return mySites; 
	}

	public List<String> getAllowedSites() {
		return allowedSites;
	}

	public void setAllowedSites(List<String> allowedSites) {
		this.allowedSites = allowedSites;
	}
	
	

}
