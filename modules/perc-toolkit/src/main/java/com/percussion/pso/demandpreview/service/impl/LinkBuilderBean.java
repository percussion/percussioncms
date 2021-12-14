/*
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.demandpreview.service.impl LinkBuilderBean.java
 *
 */
package com.percussion.pso.demandpreview.service.impl;

import com.percussion.pso.demandpreview.service.LinkBuilderService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.publisher.data.PSContentListItem;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

/**
 * Link Builder service implementation as a Spring bean. 
 * 
 * This service includes an optional suffix to be added to the 
 * generated path.  This may be useful with certain types of files.
 *  
 * @author davidbenua
 * @see LinkBuilderService
 */
public class LinkBuilderBean implements LinkBuilderService
{
   String linkSuffix = null; 
   
   public LinkBuilderBean()
   {
      
   }
    
   /***
    * @see LinkBuilderService
    */
   public String buildLinkUrl(IPSSite site, IPSAssemblyTemplate template, 
         IPSGuid content,IPSGuid folder, IPSPublishingContext context)
   {
	   return buildLinkUrl(site,template,content, folder, context, null);
   }

   /**
    * Gets the link suffix. 
    * @return the linkSuffix
    */
   public String getLinkSuffix()
   {
      return linkSuffix;
   }

   /**
    * Sets the link suffix. 
    * @param linkSuffix the linkSuffix to set
    */
   public void setLinkSuffix(String linkSuffix)
   {
      this.linkSuffix = linkSuffix;
   }


public String buildLinkUrl(IPSSite site, IPSAssemblyTemplate template,
		IPSGuid content, IPSGuid folder, IPSPublishingContext context,
		String contextVar) {	
	
	   PSContentListItem item = new PSContentListItem(
		        content, folder, template.getGUID(), site.getGUID(), context.getId());
		    
	  //If there is a root context variable configured pull from that variable first.
	String baseUrl = "";
	if(contextVar != null){
		baseUrl = site.getProperty(contextVar,context);
	}else{
		baseUrl = site.getBaseUrl();
	}
	   
		     StringBuilder url = new StringBuilder();
		     url.append(baseUrl);
		     
		     if(url.length() > 0 && !url.toString().endsWith("/"))
		     {
		        url.append("/"); 
		     }
		     String location = item.getLocation();
		     if(location.startsWith("http:") || location.startsWith("https:")){
		    	 url.replace(0, url.length(), location);
		     }else{
		    	 if(location.startsWith("/"))
		    	 {
		    		 location = location.substring(1); 
		    	 }
		    	 url.append(location);
		     }
		     if(StringUtils.isNotBlank(linkSuffix))
	    	 {
	    		 url.append(linkSuffix); 
	    	 }
		     return url.toString(); 

}
    
}
