package com.percussion.pso.demandpreview.service;

import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSite;

/**
 * Data holder for site configuration information.
 * This object holds the actual data returned from the 
 * Site Edition lookup service.   
 *
 * @author davidbenua
 *
 */
public class SiteEditionHolder {
	private IPSSite site;
	private IPSEdition edition; 
	private IPSPublishingContext context; 
	private String contextURLRootVar;
	
	/**
	 * Default constructor. 
	 */
	public SiteEditionHolder()
	{

	}
	
	/**
	 * Gets the preview site
	 * @return the preview site.
	 */
	public IPSSite getSite()
	{
		return site;		
	}
	
	/**
	 * Sets the preview site
	 * @param site the preview site to set. 
	 */
	public void setSite(IPSSite site)
	{
		this.site = site;
	}
	
	/**
	 * Gets the edition to publish.
	 * @return the edition to publish.
	 */
	public IPSEdition getEdition()
	{
		return edition;		
	}
	
	/**
	 * Sets the edition.
	 * @param edition the edition to set. 
	 */
	public void setEdition(IPSEdition edition)
	{
		this.edition = edition;
	}

   
   /**
    * Gets the assembly context. 
    * @return the context
    */
   public IPSPublishingContext getContext()
   {
      return context;
   }

   /**
    * Sets the assembly context. 
    * @param context the context to set
    */
   public void setContext(IPSPublishingContext context)
   {
      this.context = context;
   }

/**
 * @return the contextURLRootVar
 */
public String getContextURLRootVar() {
	return contextURLRootVar;
}

/**
 * @param contextURLRootVar the contextURLRootVar to set
 */
public void setContextURLRootVar(String contextURLRootVar) {
	this.contextURLRootVar = contextURLRootVar;
}
	
     
}
