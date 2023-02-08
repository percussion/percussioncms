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
package com.percussion.install;

import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.sitemanage.impl.PSSitePublishDaoHelper;
import com.percussion.utils.guid.IPSGuid;
import org.w3c.dom.Element;

import java.io.PrintStream;
import java.util.List;

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
