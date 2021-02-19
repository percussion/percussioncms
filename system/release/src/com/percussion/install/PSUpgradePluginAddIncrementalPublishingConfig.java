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
package com.percussion.install;

import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.sitemanage.impl.PSSitePublishDaoHelper;
import com.percussion.utils.guid.IPSGuid;

import java.io.PrintStream;
import java.util.List;

import org.w3c.dom.Element;

/**
 * @author JaySeletz
 *
 */
public class PSUpgradePluginAddIncrementalPublishingConfig extends PSSpringUpgradePluginBase
{
   private PrintStream logger;
   IPSSiteManager siteMgr;
   
   public PSUpgradePluginAddIncrementalPublishingConfig()
   {
      super();
      siteMgr = PSSiteManagerLocator.getSiteManager();
   }

   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      
      logger = config.getLogStream();
      
      try
      {
         // load all sites
         siteMgr = PSSiteManagerLocator.getSiteManager();
         List<IPSSite> sites = siteMgr.findAllSites();
         for (IPSSite site : sites)
         {
            updateSiteForIncremental(site);
         }
         
      }
      catch (Exception e)
      {
         e.printStackTrace(logger);
         return new PSPluginResponse(PSPluginResponse.EXCEPTION,
               e.getLocalizedMessage());
      }

      return new PSPluginResponse(PSPluginResponse.SUCCESS, "");
      

      
      
   }

   private void updateSiteForIncremental(IPSSite site) throws PSNotFoundException {
      // find default pub server
      PSPubServer pubServer = PSSitePublishDaoHelper.getDefaultPubServer(site.getGUID());
      
      // create content list
      IPSGuid itemFilterId = PSSitePublishDaoHelper.getPublicItemFilterGuid();
      PSSitePublishDaoHelper.createIncrementalContentList(site, pubServer, itemFilterId);
      
      // create edition
      PSSitePublishDaoHelper.createIncrementalEdition(site, pubServer, true);
   }

}
