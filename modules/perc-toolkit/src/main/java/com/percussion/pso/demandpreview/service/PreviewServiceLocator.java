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
