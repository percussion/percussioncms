/*
 * 
 */
package com.percussion.pso.demandpreview.service.impl;

import com.percussion.pso.demandpreview.exception.SiteLookUpException;
import com.percussion.pso.demandpreview.service.SiteEditionConfig;
import com.percussion.pso.demandpreview.service.SiteEditionHolder;
import com.percussion.pso.demandpreview.service.SiteEditionLookUpService;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Site look up service implementation as a Spring bean. 
 * 
 * @author ravikatam
 *
 */
public class SiteEditionLookUpServiceImpl implements SiteEditionLookUpService
{
	 private static final Logger log = LogManager.getLogger(SiteEditionLookUpServiceImpl.class);
	 
	 private IPSSiteManager siteManager = null;
	 private IPSPublisherService publisherService = null;
	 private IPSGuidManager guidManager = null;
	 private IPSAssemblyService asm = null; 
	 private Map<String, SiteEditionConfig> siteLookUpMap;

	 /**
	  * Default Constructor
	  */
	 public SiteEditionLookUpServiceImpl()
	 {
		 
	 }
	 
	 /**
	  * Initialize the service bean. Must be called from Spring before any of the service methods.
	  * 
	  * @throws Exception
	  */
	 public void init() throws Exception
	 {
		 if(siteManager == null)
		 {
			 siteManager = PSSiteManagerLocator.getSiteManager();
		 }
		 if(publisherService == null)
		 {
			 publisherService = PSPublisherServiceLocator.getPublisherService();
		 }
		 if(guidManager == null)
		 {
			 guidManager = PSGuidManagerLocator.getGuidMgr();
		 }
		 
		 if(asm == null)
		 {
		    asm = PSAssemblyServiceLocator.getAssemblyService(); 
		 }
	 }
	 /**
	  * Gets the preview site, template, edition and context information from the 
	  * configuration.
	  * 
	  * @param siteId the site id to use as a lookup key.  
	  * @return the SiteTemplateHolder object. Never <code>null</code>
	  * @throws SiteLookUpException when the site information cannot be located, or the 
	  * names in the configuration are invalid. 
	  */
	 public SiteEditionHolder LookUpSiteEdition(String siteId)throws SiteLookUpException
	 {
	    String emsg; 
        if(StringUtils.isBlank(siteId))
        {
            emsg = "No site id was specified"; 
            log.error(emsg); 
            throw new SiteLookUpException(emsg);
        }
        IPSGuid siteGuid = guidManager.makeGuid(Integer.parseInt(siteId), PSTypeEnum.SITE);

	    return LookUpSiteEdition(siteGuid); 
	 }

     public SiteEditionHolder LookUpSiteEdition(IPSGuid siteId)throws SiteLookUpException
     {
	     String emsg; 
		 String pSiteName = null;
		 String editionName = null;
		 String siteName = null;
		 SiteEditionConfig siteConfig = null;
		 SiteEditionHolder siteEditionHolder = new SiteEditionHolder();
		 
		 IPSSite site; 
		 try
	        {
	           site = siteManager.loadSite(siteId);
	        } catch (PSNotFoundException ex)
	        {
	           emsg = "Site Not Found for Id " + siteId; 
	           log.error( emsg ,ex);
	           throw new SiteLookUpException(emsg, ex); 
	        }
         siteName = site.getName();
		 
         siteConfig = siteLookUpMap.get(siteName);	 
		 if(siteConfig == null)
		 {
		    emsg = "Site not found in configuration: " + siteName;
		    log.error(emsg); 
			throw new SiteLookUpException(emsg);
		 }
		 
         pSiteName = siteConfig.getSiteName();
         editionName = siteConfig.getEditionName();
		 if(StringUtils.isBlank(pSiteName))
		 {
		    emsg = "Preview site not configured for " + siteName; 
		    log.error(emsg); 
			throw new SiteLookUpException(emsg);
		 }
		 //Store the context variable
		 siteEditionHolder.setContextURLRootVar(siteConfig.getContextURLRootVar());

		 IPSSite pSite=null;
		 try {
			  pSite = siteManager.loadSite(pSiteName);
		 } catch (PSNotFoundException e) {
			 log.error("Unable to load Site: {}", pSiteName);
			 log.debug(e.getMessage(),e);
			 throw new SiteLookUpException(e);
		 }
		 siteEditionHolder.setSite(pSite);
		 if(editionName == null || editionName.equals(""))
		 {
		    emsg = "Edition not configured for " + siteName; 
		    log.error(emsg); 
			throw new SiteLookUpException(emsg);
		 }
		 IPSEdition sEdition = getEdition(editionName);
		 siteEditionHolder.setEdition(sEdition);

		 IPSPublishingContext ctx=null;
		 try {
			  ctx = siteManager.loadContext(siteConfig.getAssemblyContext());
		 } catch (PSNotFoundException e) {
		 	 log.error(e.getMessage());
			 log.debug(e.getMessage(),e);
		 }
		 if(ctx==null){
     		 emsg = "Context " + siteConfig.getAssemblyContext() +" not configured for " + siteName;
     		 log.error(emsg);
     		 throw new SiteLookUpException(emsg);
     	 }
     	 
	     siteEditionHolder.setContext(ctx);  	 
	 
	  return siteEditionHolder;
	 }
	 
	 /**
	  * Gets the set of sites for which there are configuration entries.	  * 
	  * @return the set of names. Never <code>null</code> but may be 
	  * <code>empty</code>
	  */
	 public Set<String> getConfiguredSiteNames()
	 {
	    Set<String> names = new LinkedHashSet<String>();
	    names.addAll(siteLookUpMap.keySet()); 
	    return names; 
	 }
	 

	/**
	 * Gets the edition
	 * @param editionName
	 * @return the edition. Never <code>null</code>
	 * @throws SiteLookUpException
	 */
	protected IPSEdition getEdition(String editionName) throws SiteLookUpException
	{
	    String emsg; 
		IPSEdition edition = null;
		edition = publisherService.findEditionByName(editionName);
		if(edition == null)
		{
		   emsg = "Edition not found: " + editionName;
		   log.error(emsg); 
		   throw new SiteLookUpException(emsg);
		}
		return edition;
	}
	/**
	 * Gets the site look up map instance
	 * @return the siteLookUpMap
	 */
	public Map<String, SiteEditionConfig> getSiteLookUpMap()
	{
		return siteLookUpMap;		
	}
	/**
	 * Sets the site look up map instance.
	 * @param siteLookUpMap
	 */
	public void setSiteLookUpMap(Map<String, SiteEditionConfig> siteLookUpMap)
	{
		this.siteLookUpMap = siteLookUpMap;
	}
	/**
	 * This method is only for testing purpose.
	 * @param siteManager
	 */
	protected void setSiteManager(IPSSiteManager siteManager)
	{
		this.siteManager = siteManager;
	}
	
	/**
	 * Sets the publisher service.
	 * This method is only for testing purpose.
	 * @param publisherService
	 */
	protected void setPubisherService(IPSPublisherService publisherService)
	{
		this.publisherService = publisherService;
	}
	/**
	 * This method is only for testing purpose.
	 * @param guidManager
	 */
	protected void setGuidManager(IPSGuidManager guidManager)
	{
		this.guidManager = guidManager;
	}

   /**
    * This method is only for testing purpose.
    * @param asm the asm to set
    */
   protected void setAsm(IPSAssemblyService asm)
   {
      this.asm = asm;
   }
	
	
}
