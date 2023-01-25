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

import com.percussion.services.PSBaseServiceLocator;

/**
 * Service locator for Preview services. 
 *
 * @author davidbenua
 *
 */
public class PreviewServiceLocator extends PSBaseServiceLocator
{
   
    /**
     * Gets the Site Edition lookup service.
     * @return the site edition lookup service method. 
     */
	public static SiteEditionLookUpService getSiteEditionLookUpService()
	{
		return (SiteEditionLookUpService) getBean("OCCSiteEditionLookUpService");
		
	}

	/**
	 * Gets the Demand Publisher Service method.
	 * @return the demand publisher service implementation.  
	 */
	public static DemandPublisherService getDemandPublisherService()
	{
	   return (DemandPublisherService) getBean("OCCDemandPublisherBean");
	}
	
	/**
	 * Gets the Link Builder Service method.
	 * @return the link builder service implementation. 
	 */
	public static LinkBuilderService getLinkBuilderService()
	{
	   return (LinkBuilderService) getBean("OCCLinkBuilderBean");
	}
}
