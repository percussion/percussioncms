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
package com.percussion.webservices.assembly.impl;

import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.assembly.data.PSAssemblyTemplateWs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common implementations used with the public and private assembly 
 * webservices.
 */
public class PSAssemblyBaseWs
{

   /**
    * Gets a list of associated site references with each template in the
    * specified template list.
    * 
    * @param templates a list of templates, not <code>null</code>.
    * @return the template and associated sites, never <code>null</code>, may
    *    be empty.
    */
   protected List<PSAssemblyTemplateWs> getTemplateWs(
      List<IPSAssemblyTemplate> templates)
   {
      if (templates == null)
         throw new IllegalArgumentException("templates cannot be null");


      // get site / templates associations
      IPSSiteManager sitemgr = PSSiteManagerLocator
            .getSiteManager();
      Map<PSPair<IPSGuid, String>, Collection<IPSGuid>> siteToTemplates = sitemgr
            .findSiteTemplatesAssociations();

      
      // get associated sites per template
      List<PSAssemblyTemplateWs> results = 
         new ArrayList<PSAssemblyTemplateWs>();
      for (IPSAssemblyTemplate template : templates)
      {
         Map<IPSGuid, String> sites = new HashMap<IPSGuid, String>();
         for (Map.Entry<PSPair<IPSGuid, String>, Collection<IPSGuid>> entry : siteToTemplates
               .entrySet())
         {
            PSPair<IPSGuid, String> siteId = entry.getKey();
            Collection<IPSGuid> templateIds = entry.getValue();
            if (templateIds.contains(template.getGUID()))
               sites.put(siteId.getFirst(), siteId.getSecond());
         }
         results.add(new PSAssemblyTemplateWs(template, sites));
      }

      return results;
   }
}

