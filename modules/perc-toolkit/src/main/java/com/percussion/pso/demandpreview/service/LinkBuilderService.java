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
