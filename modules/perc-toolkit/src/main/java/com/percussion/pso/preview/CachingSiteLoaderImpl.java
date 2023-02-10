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
package com.percussion.pso.preview;

import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.timing.PSStopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

/**
 * A site loader implementation that caches the list of sites. 
 * The caching behavior is controlled by the the siteReloadDelay 
 * property.  
 * <p>
 * If siteReloadDelay < 0 then the site information is not cached, it is 
 * reloaded each time it is requested. This setting may be best for small development
 * servers without large numbers of sites or templates.  
 * <p>
 * If siteReloadDelay == 0 then the sites will be reloaded only on server startup. 
 * This setting may be best for production servers where the templates are never modified. 
 * <p>
 * Otherwise, the siteReloadDelay is the number of seconds to wait before reloading 
 * the sites.  
 * @author DavidBenua
 *
 */
public class CachingSiteLoaderImpl implements InitializingBean, SiteLoader
{
   private static final Logger log = LogManager.getLogger(CachingSiteLoaderImpl.class);
   
   private static IPSSiteManager siteMgr = null; 
   
   protected List<IPSSite> allSites = null; 
   
   /**
    * The site reload delay, in seconds.  Set to 0, the sites will never
    * be reloaded.  Set < 0, the sites will not be cached. 
    */
   private long siteReloadDelay = 0;  
   
   public CachingSiteLoaderImpl()
   {
   }
   
   private static void initServices()
   {
      if(siteMgr == null)
      {
         siteMgr = PSSiteManagerLocator.getSiteManager();
      }
   }
   
   /**
    * @see InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      allSites = loadAllSites();
      
      if(siteReloadDelay > 0)
      {
         Thread reloaderThread = new Thread(new SiteReloader());
         reloaderThread.start();
      }
   }

   protected List<IPSSite> loadAllSites() throws PSSiteManagerException
   {
      initServices(); 
      log.debug("Loading all sites"); 
      PSStopwatch tm = new PSStopwatch();
      tm.start();
      List<IPSSite> mySites = siteMgr.findAllSites();
      tm.stop(); 
      log.debug("Loading all sites took {} ms.",tm.elapsed());
      return mySites;
   }
   
   /**
    * @see SiteLoader#findAllSites()
    */
   public synchronized List<IPSSite> findAllSites() throws PSSiteManagerException 
   {
      if(allSites == null || siteReloadDelay < 0)
      {
         allSites = loadAllSites(); 
      }
      return allSites;
   }

   /**
    * Gets the site reload delay in seconds.  If this value is 0, the sites
    * will not be reloaded.  
    * @return the siteReloadDelay
    */
   public long getSiteReloadDelay()
   {
      return siteReloadDelay;
   }

   /**
    * Sets the site reload delay in Seconds.  
    * @param siteReloadDelay the siteReloadDelay to set
    */
   public void setSiteReloadDelay(long siteReloadDelay)
   {
      this.siteReloadDelay = siteReloadDelay;
   }

   /**
    * Sets the site manager. Used for unit testing. 
    * @param siteMgr the siteMgr to set
    */
   public static void setSiteMgr(IPSSiteManager siteMgr)
   {
      CachingSiteLoaderImpl.siteMgr = siteMgr;
   }
   
   protected synchronized List<IPSSite> getAllSites() {
		return allSites;
	}

	protected synchronized void setAllSites(List<IPSSite> allSites) {
		this.allSites = allSites;
	}
	
   private class SiteReloader implements Runnable
   {

      /**
       * @see Runnable#run()
       */
      public void run()
      {
         try
         {
            //TODO: NC-?????
            while(true)
            {
               setAllSites(loadAllSites()); 
               Thread.sleep(siteReloadDelay*1000); 
            }
         } catch (PSSiteManagerException ex)
         {
            log.error("Unexpected Site Manager Exception Error: {}",ex.getMessage());
            log.debug(ex.getMessage(), ex);
         } catch (InterruptedException ex)
         {
             log.error("Site Reloading was Interrupted Error: {}", ex.getMessage());
            Thread.currentThread().interrupt();
         }
      }
      
   }



   
}
