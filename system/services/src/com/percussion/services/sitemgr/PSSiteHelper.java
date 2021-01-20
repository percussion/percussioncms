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
package com.percussion.services.sitemgr;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.server.PSRequest;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase;
import com.percussion.utils.guid.IPSGuid;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Common code related to using a site
 * 
 * @author dougrand
 */
public class PSSiteHelper
{

   /**
    * Setup the information for the site. The bindings may be used for the
    * assembly subsystem or location generation
    * 
    * @param eval evaluator object, never <code>null</code>
    * @param siteidstr the site id string, may be <code>null</code>
    * @param contextstr the context string, never <code>null</code> or empty
    */
   public static void setupSiteInfo(PSServiceJexlEvaluatorBase eval,
         String siteidstr, String contextstr)
   {
      if (eval == null)
      {
         throw new IllegalArgumentException("eval may not be null");
      }
      if (StringUtils.isBlank(contextstr))
      {
         throw new IllegalArgumentException("contextstr may not be null or empty");
      }
      if (!StringUtils.isBlank(siteidstr))
      {
         IPSGuid siteid = PSGuidUtils.makeGuid(siteidstr, PSTypeEnum.SITE);
         IPSSiteManager sitemanager = PSSiteManagerLocator.getSiteManager();
         Map<String, String> variables = findVariablesForSite(siteid,
               contextstr);
         IPSSite site = sitemanager.loadUnmodifiableSite(siteid);

         eval.bind("$sys.variables", variables != null
               ? variables
               : new HashMap<String, String>());

         eval.bind("$sys.site.id", siteid);
         eval.bind("$sys.site.path", site.getFolderRoot());
         eval.bind("$sys.site.globalTemplate", site.getGlobalTemplate());
         eval.bind("$sys.site.url", site.getBaseUrl());
      }
   }
   
   /**
    * Lookup the variables defined for the given site id. If nothing is found
    * then this method returns an empty map.
    * 
    * @param siteid the id of the site, assumed not <code>null</code>
    * @param contextstr the pub context, assumed not <code>null</code>. This 
    * can either be the name (case-insensitive) or uuid/guid.
    * @return a map of names and values, may be empty if the site doesn't exist
    *         or if no definitions are provided.
    */
   private static Map<String, String> findVariablesForSite(IPSGuid siteid,
         String contextstr)
   {
      IPSSiteManager sitemgr = PSSiteManagerLocator.getSiteManager();
      IPSSite site = sitemgr.loadUnmodifiableSite(siteid);
      IPSPublishingContext context;
      // Is the context string a name or a number?
      try
      {
         long id = Long.parseLong(contextstr);
         context = sitemgr.loadContext(PSGuidUtils.makeGuid(id,
               PSTypeEnum.CONTEXT));
      }
      catch (NumberFormatException nfe)
      {
         context = sitemgr.loadContext(contextstr);
      }
      Set<String> names = site.getPropertyNames(context.getGUID());
      Map<String, String> rval = new HashMap<String, String>();
      for (String name : names)
      {
         rval.put(name, site.getProperty(name, context.getGUID()));
      }
      return rval;
   }

   /**
    * Utility method to find the folder id corresponding to the provided
    * site's siteroot path.
    * @param siteid id of the site.
    * @return the content id. Returns <code>-1</code> if siteid null or empty 
    *   or if there is no site exists or if there is no such relationship path 
    *   exists in the system.
    * @throws PSCmsException 
    * @throws PSSiteManagerException  
    */
   public static int getSiteFolderId(String siteid) throws PSCmsException,
         PSSiteManagerException
   {
      if (StringUtils.isBlank(siteid))
         return -1;
      IPSSiteManager sitemgr = PSSiteManagerLocator.getSiteManager();
      IPSSite site = sitemgr.loadUnmodifiableSite(
            new PSGuid(PSTypeEnum.SITE, siteid));

      String folderRoot = site.getFolderRoot();
      if (StringUtils.isBlank(folderRoot))
         return -1;

      PSRelationshipProcessor relProc = null;
      relProc = PSRelationshipProcessor.getInstance();
      int folderid = relProc.getIdByPath(
            PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE, folderRoot,
            PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      return folderid;
   }
}
