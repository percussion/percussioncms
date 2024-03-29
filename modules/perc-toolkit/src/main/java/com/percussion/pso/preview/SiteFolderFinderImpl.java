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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.pso.jexl.PSOObjectFinder;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.timing.PSStopwatch;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;


/**
 * Site Folder Finder default implementation. 
 * 
 * @author DavidBenua
 *
 */
public class SiteFolderFinderImpl implements SiteFolderFinder 
{

    private static final Logger log = LogManager.getLogger(SiteFolderFinderImpl.class);
    
    private static IPSGuidManager gmgr = null; 
    private static IPSContentWs cws = null; 
    private static IPSSecurityWs secws = null; 
    private static PSOObjectFinder finder = null; 
    private SiteLoader siteLoader = null;
    
    private boolean testCommunityVisibility = true; 
    /**
     * Default constructor
     */
    public SiteFolderFinderImpl()
    {
       super(); 
    }   
   
    
    /**
     * Initialize the service pointers. 
     */
    private static void initServices()
    {
       if(gmgr == null)
       {
          cws = PSContentWsLocator.getContentWebservice();
          secws = PSSecurityWsLocator.getSecurityWebservice();
          gmgr = PSGuidManagerLocator.getGuidMgr(); 
          finder = new PSOObjectFinder();
       }
    }
   
    /**
    * @see SiteFolderFinder#findSiteFolderLocations(String, String, String)
    */
    public List<SiteFolderLocation> findSiteFolderLocations(String contentid, String folderid, String siteid) throws Exception
    {
       initServices();
       boolean useFolderid = false; 
       List<SiteFolderLocation> previewList = new ArrayList<SiteFolderLocation>(); 
       long siteidl = 0L;  
       if(StringUtils.isNotBlank(siteid))
       {
          siteidl = Long.valueOf(siteid).longValue(); 
       }
       PSLocator loc = null;
       PSFolder folder = null; 
       IPSGuid guid; 
       String[] paths; 
       String emsg; 
       PSStopwatch sw = new PSStopwatch();
       sw.start();
       if(StringUtils.isBlank(folderid))
       {
          log.debug("starting with contentid");
          loc = new PSLocator(contentid);
          guid = gmgr.makeGuid(loc);
          paths = cws.findFolderPaths(guid);
          
       }
       else
       {
          log.debug("starting with folderid"); 
          loc = new PSLocator(folderid,"0");
          guid = gmgr.makeGuid(loc);
          String folderTitle = findFolderTitle(folderid);
          useFolderid = true;
          paths = cws.findFolderPaths(guid); 
          StringBuilder path1 = new StringBuilder();
          path1.append(paths[0]); 
          path1.append("/"); 
          path1.append(folderTitle); 
          log.debug("folder path is " + path1.toString()); 
          paths = new String[]{path1.toString()}; 
         
       }    
       
                 
       sw.stop();
       log.debug("Time to load folder " + sw.elapsed());
       if(log.isDebugEnabled())
       {
          for(String p : paths)
          {
             log.debug("path is: " + p);
          }
       }
        
 
       PSStopwatch sw3 = new PSStopwatch(); 
       sw3.start(); 
       List<IPSSite> allSites = siteLoader.findAllSites(); 
          //siteMgr.findAllSites();
       log.debug("time to find all sites {}", sw3.elapsed());
       if(isTestCommunityVisibility())
       {
          allSites = filterVisibleSites(allSites);
       }
       sw3.stop(); 
       log.debug("time to find and filter visible sites {}", sw3.elapsed()) ;
       
       PSStopwatch sw4 = new PSStopwatch(); 
       sw4.start();
       for(String path : paths)
       {
          for(IPSSite site : allSites)
          {
             if((siteidl != 0) && (site.getSiteId().longValue() != siteidl))
             {
                //site was specified, and this is NOT our site
                log.debug("site id {} != {}",siteidl, site.getSiteId());
                continue; 
             }
             
             if(path.startsWith(site.getFolderRoot()))
             {
                //We found a matching site
                log.debug("Matching site {} root {}",site.getName(), site.getFolderRoot());
                SiteFolderLocation location = new SiteFolderLocation();
                location.setFolderPath(path); 
                location.setSite(site); 
                if(useFolderid)
                {
                   location.setFolderid(Integer.valueOf(folderid).intValue());                    
                }
                else
                {
                   List<IPSGuid> glist = cws.findPathIds(path); 
                   IPSGuid last = glist.get(glist.size()-1); 
                   location.setFolderid(gmgr.makeLocator(last).getId());
                }
                previewList.add(location); 
             }
          }
       }
       sw4.stop();
       log.debug("time to process site folders {}", sw4.toString());
       log.debug("total time is {}", sw4.elapsed());
       return previewList; 
    }
   
    /**
     * Filter sites by runtime visibility. 
     * @param insites the list of all sites
     * @return the list of all visible sites. 
     */
   private List<IPSSite> filterVisibleSites(List<IPSSite> insites)
   {
      List<IPSSite> visible = new ArrayList<IPSSite>(); 
      List<IPSGuid> glist = new ArrayList<IPSGuid>(); 
      
      for(IPSSite site : insites)
      {
         glist.add(site.getGUID()); 
      }
      List<IPSGuid> g2 = secws.filterByRuntimeVisibility(glist);
      for(IPSSite site : insites)
      {
         if(g2.contains(site.getGUID()))
         {
            log.debug("Site {} is visible", site.getName());
            visible.add(site); 
         }
         else
         {
            log.debug("Site {} is not visible",site.getName());
         }
      }
      return visible; 
   }
   
   public String findFolderTitle(String folderid) throws Exception
   {
      PSComponentSummary summ = finder.getComponentSummaryById(folderid);
      return summ.getName();
   }
   
   public static void setGmgr(IPSGuidManager gmgr)
   {
      SiteFolderFinderImpl.gmgr = gmgr;
   }

 
   public static void setCws(IPSContentWs cws)
   {
      SiteFolderFinderImpl.cws = cws;
   }

  
   /**
    * @param secws the secws to set
    */
   public static void setSecws(IPSSecurityWs secws)
   {
      SiteFolderFinderImpl.secws = secws;
   }


   /**
    * @return the testCommunityVisibility
    */
   public boolean isTestCommunityVisibility()
   {
      return testCommunityVisibility;
   }


   /**
    * @param testCommunityVisibility the testCommunityVisibility to set
    */
   public void setTestCommunityVisibility(boolean testCommunityVisibility)
   {
      this.testCommunityVisibility = testCommunityVisibility;
   }


   /**
    * @return the siteLoader
    */
   public SiteLoader getSiteLoader()
   {
      return siteLoader;
   }


   /**
    * @param siteLoader the siteLoader to set
    */
   public void setSiteLoader(SiteLoader siteLoader)
   {
      this.siteLoader = siteLoader;
   }

   /**
    * @param finder the finder to set
    */
   public static void setFinder(PSOObjectFinder finder)
   {
      SiteFolderFinderImpl.finder = finder;
   }
  
}
