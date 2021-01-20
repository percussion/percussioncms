/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.webservices.assembly.impl;

import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.sitemgr.IPSSiteManagerInternal;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.assembly.data.PSAssemblyTemplateWs;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Common implementations used with the public and private assembly 
 * webservices.
 */
public class PSAssemblyBaseWs
{

    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
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
      IPSSiteManagerInternal sitemgr = (IPSSiteManagerInternal) PSSiteManagerLocator
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

