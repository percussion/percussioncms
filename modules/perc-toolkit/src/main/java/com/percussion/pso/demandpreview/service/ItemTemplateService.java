/*
 * COPYRIGHT (c) 1999 - 2010 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.demandpreview.service.ItemTemplateService.java
 *
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
