/*
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.demandpreview.service LinkBuilderService.java
 *
 */
package com.percussion.pso.demandpreview.service;

import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.guid.IPSGuid;

/**
 * Service to build links to a content item.
 * 
 *
 * @author davidbenua
 *
 */
public interface LinkBuilderService
{
   
   /***
    * Service to build the URL of an item published on a site.  
    * 
    * The URL will contain the site root path and the location 
    * as computed by the location scheme in effect for the content
    * type, template and context.
    * 
    * @param site the site where the item is to be published.
    * @param template the template used for the publish
    * @param content the content summary of the item
    * @param folder the folder that the item is being published from
    * @param context the assembly context that is being used for the content generation
    * @param contextVar the context variable that contains the URL root for the context. 
    * @return
    */
   public String buildLinkUrl(IPSSite site, IPSAssemblyTemplate template,
	         IPSGuid content, IPSGuid folder, IPSPublishingContext context, String contextVar);
 
}
