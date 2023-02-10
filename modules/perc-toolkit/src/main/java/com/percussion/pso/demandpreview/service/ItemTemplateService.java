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
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.guid.IPSGuid;

/**
 * Service for locating the publishable template for an item. 
 * 
 * @author DavidBenua
 *
 */
public interface ItemTemplateService {

	/**
	 * Finds the appropriate template based on site and content item. 
	 * @param site the site
	 * @param contentId the content item id. 
	 * @return the appropriate template. Never <code>null</code>. 
	 * @throws SiteLookUpException when the template cannot be located. 
	 */
	public IPSAssemblyTemplate findTemplate(IPSSite site, IPSGuid contentId)
	  throws SiteLookUpException; 
	
}
