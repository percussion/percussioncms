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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.utils.jspel;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.request.PSRequestInfo;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utilities to make internal document requests from jsp el
 * 
 * @author dougrand
 */
public class PSItemUtilities
{

   private PSItemUtilities(){
      //require static access
   }
   /**
    * Make an internal request and return the result.
    * 
    * @param url the url of the internal resource, never <code>null</code> or
    *           empty
    * @return the result
    */
   public static String getInternal(String url)
   {
      if (StringUtils.isBlank(url))
      {
         throw new IllegalArgumentException("url may not be null or empty");
      }
      try
      {
         PSRequest req = (PSRequest) PSRequestInfo
               .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
         PSInternalRequest ireq = PSServer.getInternalRequest(url, req, null,
               false, null);
         ByteArrayOutputStream os = ireq.getMergedResult();
         String output = new String(os.toByteArray(), "UTF-8");
         return output;
      }
      catch (Exception e)
      {
         return e.getLocalizedMessage();
      }
   }

   /**
    * Gets a multimap that contains the mapping from a content item to folders
    * within sites. Each entry maps from the name of a site to a collection of
    * folder paths. The special site "*" contains folders that are not part of a
    * site. If this returns an empty collection, the content item is not
    * associated with any folder.
    * 
    * @param contentid the item id
    * @return the map from site name to folder path collection, where "*" is the
    *         special site name for no site
    */
   public static MultiMap getItemSiteInfo(int contentid)
   {
      MultiMap rval = new MultiHashMap();
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      PSComponentSummary sum = cms.loadComponentSummary(contentid);
      if (sum == null)
      {
         throw new IllegalArgumentException("Content id " + contentid
               + " references a non-existant content item");
      }

      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      PSServerFolderProcessor fproc = PSServerFolderProcessor.getInstance();

      try
      {
         Set<String> foundpaths = new HashSet<>();
         String paths[] = fproc.getFolderPaths(sum.getCurrentLocator());
         List<IPSSite> sites = smgr.findAllSites();
         for (IPSSite site : sites)
         {
            String rootpath = site.getFolderRoot();
            for (String path : paths)
            {
               if (StringUtils.isNotBlank(rootpath) 
                     && path.startsWith(rootpath))
               {
                  foundpaths.add(path);
                  rval.put(site.getName(), path);
               }
            }
         }
         for (String path : paths)
         {
            if (!foundpaths.contains(path))
            {
               rval.put("*", path);
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Problem getting site info for content id "
               + contentid);
      }

      return rval;
   }

   /**
    * Lookup the site id for a given site name
    * 
    * @param name the name of the site, never <code>null</code> or empty
    * @return the id of the site, or <code>null</code> if the site is unknown
    */
   public static Long getSiteIdFromName(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      try
      {
         IPSSite site = smgr.loadSite(name);
         return site.getSiteId();
      }
      catch (PSNotFoundException e)
      {
         return null;
      }
   }

   /**
    * Get the title of the given content item
    * 
    * @param contentid the content id, never <code>null</code>
    * @return the title, or <code>null</code> if the item is not found
    */
   public static String getTitle(Integer contentid)
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary sum = cms.loadComponentSummary(contentid);
      return sum != null ? sum.getName() : null;
   }

   /**
    * Calculate action page url from available data
    * 
    * @param contentid the content id, never <code>null</code>
    * @param siteid the site id, may be <code>null</code>
    * @param folderid the folder id, may be <code>null</code> if the site id
    *           is <code>null</code>
    * @return an absolute url for the action page from the context root
    */
   public static String getPanelUrl(Integer contentid, Long siteid, Integer folderid)
   {
      if (contentid == null)
      {
         throw new IllegalArgumentException("contentid may not be null");
      }
      if (folderid == null)
      {
         if (siteid != null)
            throw new IllegalArgumentException(
                  "folderid may not be null if siteid is specified");
      }
      StringBuilder b = new StringBuilder();
      b.append("actionpage/panel?sys_contentid=");
      b.append(contentid);
      if (folderid != null && folderid != 0)
      {
         b.append("&sys_folderid=");
         b.append(folderid);
      }
      if (siteid != null && siteid != 0)
      {
         b.append("&sys_siteid=");
         b.append(siteid);
      }
      return b.toString();
   }

   /**
    * Get the folder id from the passed path.
    * 
    * @param path the path, never <code>null</code> or empty
    * @return the content id of the leaf folder, or <code>null</code> if the
    *         path is not known
    */
   public static Integer getFolderIdFromPath(String path)
   {
      if (StringUtils.isBlank(path))
      {
         throw new IllegalArgumentException("path may not be null or empty");
      }
      PSServerFolderProcessor fproc = PSServerFolderProcessor.getInstance();

      try
      {
         PSComponentSummary sum = fproc.getSummary(path);
         return sum != null ? new Integer(sum.getContentId()) : null;
      }
      catch (PSCmsException e)
      {
         return null;
      }
   }
}
