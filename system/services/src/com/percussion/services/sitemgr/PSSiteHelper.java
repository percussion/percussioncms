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
package com.percussion.services.sitemgr;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.server.PSServer;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.error.PSRuntimeException;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Common code related to using a site
 * 
 * @author dougrand
 */
public class PSSiteHelper
{

   public static HashMap<Integer, String> m_siteIdToGroup = null;

   private static final Logger ms_log = LogManager.getLogger(PSSiteHelper.class);
   /**
    * Setup the information for the site. The bindings may be used for the
    * assembly subsystem or location generation
    * 
    * @param eval evaluator object, never <code>null</code>
    * @param siteidstr the site id string, may be <code>null</code>
    * @param contextstr the context string, never <code>null</code> or empty
    */
   public static void setupSiteInfo(PSServiceJexlEvaluatorBase eval,
         String siteidstr, String contextstr) throws PSNotFoundException {
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

   public synchronized static void initializeSiteGroups()
   {
      if (m_siteIdToGroup == null)
      {
         // Setup configuration map.
         m_siteIdToGroup = new HashMap<Integer, String>();
         for (Map.Entry<Object, Object> entry : PSServer.getServerProps().entrySet())
         {
            String key = entry.getKey().toString();
            if (key.startsWith("siteGroups"))
            {
               String group = key.substring(11);
               String[] siteIds = StringUtils.split(entry.getValue().toString(), ",");

               for (String id : siteIds)
               {
                  m_siteIdToGroup.put(NumberUtils.toInt(StringUtils.trim(id)), group);
               }
            }
         }
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
         String contextstr) throws PSNotFoundException {
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
      catch (NumberFormatException | PSNotFoundException nfe)
      {
         context = sitemgr.loadContext(contextstr);
      }
      Set<String> names = site.getPropertyNames(context.getGUID());
      Map<String, String> rval = new HashMap<>();
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
           PSSiteManagerException, PSNotFoundException {
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


   /**
    * Fixup site and folder ids for a list of assembly items
    *
    * @param items
    */
   public static void fixupSiteFolderIds(List<IPSAssemblyItem> items)
   {
      if (shouldFixIds())
      {
         for (IPSAssemblyItem item : items)
         {
            fixupSiteFolderId(item);
         }
      }
   }


   /**
    * Check server.properties for the autofixFolderSiteIds=true property to use
    * new fixup functionality
    *
    * @return
    */
   private static boolean shouldFixIds()
   {
      return StringUtils.equals(PSServer.getProperty("autofixFolderSiteIds"), "true");
   }


   /**
    * Calculate and correct correct folder and site ids given sys_siteid,
    * sys_folderid, sys_contentid and sys_originalsiteid, sys_originalfolderid
    * parameters not all of these need to be provided e.g. if an item is in only
    * a single site and folder only sys_contentid is required requires
    * autofixFolderSiteIds=true to be set in server.properties file.
    *
    * Logic is.
    *
    * If item is in only one folder then siteid and folder id is caculated for
    * that folder and inserted or replaced If item is in more than one folder
    * and a folder id is specified that matches one of those folders, that
    * folderid is selected If an item is in more than one folder and a site id
    * is specified that matches the location of that folder, the site id is used
    * to find the correct site and folder If an item is in more than one folder
    * and a folder id that matches one of these is not provided then the folder
    * that is a closest path match to the original folder id/siteid (passed from
    * original page being rendered )is used If the folder that is selected has
    * multiple sites mapping to it (e.g. staging and public servers)
    * configuration is required in server.properties to set site groups so the
    * correct site can be selected based upon the original site group of the
    * page being rendered.
    *
    * @param params
    */
   public static void fixupSiteFolderId(HashMap<String, String> params)
   {
      if (shouldFixIds())
      {
         int siteId = NumberUtils.toInt(params.get(IPSHtmlParameters.SYS_SITEID));
         int origSiteId = NumberUtils.toInt(params.get(IPSHtmlParameters.SYS_ORIGINALSITEID));
         int folderId = NumberUtils.toInt(params.get(IPSHtmlParameters.SYS_FOLDERID));
         int origFolderId = NumberUtils.toInt(params.get(IPSHtmlParameters.SYS_ORIGINALFOLDERID));
         int id = NumberUtils.toInt(params.get(IPSHtmlParameters.SYS_CONTENTID));
         if (origSiteId <= 0)
         {
            origSiteId = siteId;

         }
         if (origFolderId <= 0)
         {
            origFolderId = folderId;
         }

         IPSGuid siteGuid = (siteId <= 0) ? null : new PSGuid(PSTypeEnum.SITE, siteId);
         IPSGuid originalSiteGuid = (origSiteId <= 0) ? siteGuid : new PSGuid(PSTypeEnum.SITE, origSiteId);

         IPSGuid calculatedSite = null;
         String calculatedFolderPath = null;

         String[] currentFolderPaths = null;

         PSServerFolderProcessor folderProc = PSServerFolderProcessor.getInstance();

         try
         {
            currentFolderPaths = folderProc.getFolderPaths(new PSLocator(id));
         }
         catch (PSCmsException e)
         {
            ms_log.warn("No folder found for item " + id, e);
            params.remove(IPSHtmlParameters.SYS_FOLDERID);
            return;
         }

         if (currentFolderPaths != null && currentFolderPaths.length > 0)
         {
            if (currentFolderPaths.length == 1)

            {
               // item is in only one folder. Ignore everything else and use
               // that folder and site
               calculatedFolderPath = currentFolderPaths[0];
               calculatedSite = findSiteForFolder(originalSiteGuid, siteGuid, calculatedFolderPath, id);
            }
            else
            {
               ms_log.debug("item " + id + " is in more than one folder need to find one to link to");

               // checking current folder specified in assembly item
               if (folderId > 0)
               {
                  String selectedFolderPath = null;
                  try
                  {
                     // Get the folder path for the item. It is an error for a
                     // folder to have multiple
                     // folder paths.
                     String[] selectedFolderPaths = null;
                     try
                     {
                        selectedFolderPaths = folderProc.getItemPaths(new PSLocator(folderId));
                     }
                     catch (PSNotFoundException e)
                     {
                        ms_log.error("sys_folderid " + folderId + " specified that does not exist");
                     }
                     if (selectedFolderPaths != null && selectedFolderPaths.length > 0)
                     {
                        if (selectedFolderPaths.length > 1)
                        {
                           throw new PSRuntimeException(
                                   "The folder "
                                           + folderId
                                           + " has multiple paths, this item or one of its parent folders has multiple parent folders."
                                           + " These relationships need to be resolved manually contact percussion support for help. "
                                           + selectedFolderPaths);
                        }

                        selectedFolderPath = selectedFolderPaths[0];
                        // check if item is correctly in folder specified.
                        for (String path : currentFolderPaths)
                        {
                           if (path.equals(selectedFolderPath))
                           {
                              ms_log.debug("item " + id + " " + selectedFolderPath
                                      + " id in folder specified by sys_folderid using this.");

                              calculatedFolderPath = path;
                              break;
                           }
                        }
                     }
                  }
                  catch (PSCmsException e)
                  {
                     ms_log.error("Cannot find folder for id " + folderId);
                  }

                  if (ms_log.isDebugEnabled())
                  {
                     if (calculatedFolderPath == null)
                     {
                        ms_log.debug("Item is not in folderid " + folderId);
                     }
                  }
               }

               // If site id is specified see if we can filter by site
               // If not in site continue check folder. folder may have been
               // moved between sites.
               // What if more than one item in destination and folder not
               // specified, check closeness.
               // If no paths match site ignore site and pass all paths to next
               // check. If site specified and more than one
               // match pass filtered list. If a single matches we are done.

               List<String> pathsForSite = new ArrayList<String>();

               if (calculatedFolderPath == null && siteGuid != null)
               {
                  // We could not find a folder, we now try to see if we can
                  // find the folder using the specified
                  // site id as a guide.
                  // If not site is explicitly specified we use the original
                  // siteid passed in, e.g. the currently
                  // publishing site.
                  // We just try and match against the site root. We may have
                  // multiple site ids match
                  // with this root and may have to change the actual site id
                  // later.
                  IPSSiteManager siteMgr = PSSiteManagerLocator.getSiteManager();

                  IPSSite siteObj = null;
                  String folderRoot = null;
                  if (siteGuid != null)
                  {
                     siteObj = siteMgr.findSite(siteGuid);
                     if (siteObj != null)
                        folderRoot = siteObj.getFolderRoot();
                  }

                  if (folderRoot != null)
                  {

                     for (String path : currentFolderPaths)
                     {
                        if (path.startsWith(folderRoot))
                           pathsForSite.add(path);
                     }

                     if (pathsForSite.size() == 1)
                     {
                        calculatedFolderPath = pathsForSite.get(0);
                     }
                     if (pathsForSite.size() > 1)
                     {
                        // If we still have more than one path we will try and
                        // filter further in the next step
                        currentFolderPaths = pathsForSite.toArray(new String[pathsForSite.size()]);
                     }
                     // If we can't find the item in the site we will continue
                     // with the original list.
                  }

               }

               /*
                * item is in more than one folder but either folder id was not
                * specified, or folder id did not specify a folder the item is
                * in. in this case we check to see if the item is in the same
                * folder as the parent page. if it is not in the same folder it
                * tries to find the folder that is the closest match to the path
                * of the current page.
                */
               if (calculatedFolderPath == null && origFolderId > 0)
               {

                  calculatedFolderPath = findClosestFolderPath(folderId, origFolderId, currentFolderPaths, folderProc);

               }
               // Cannot filter down to unique path.
               if (calculatedFolderPath == null && currentFolderPaths.length > 1)
               {
                  calculatedFolderPath = currentFolderPaths[0];
                  ms_log.debug("Cannot find unique folder for id " + id + " paths="
                          + Arrays.toString(currentFolderPaths)
                          + " picking first. Must select to include folder when linking to this item");

               }
               // We found a unique folder to use. In this case we try to
               // calculate the correct site to use for this folder
               if (calculatedFolderPath != null)
               {
                  calculatedSite = findSiteForFolder(originalSiteGuid, siteGuid, calculatedFolderPath, id);
                  ms_log.debug("using site " + calculatedSite + " for folder " + calculatedFolderPath);
               }

            }

         }
         else
         {
            if (!StringUtils.equals(PSServer.getProperty("allowLinksToOrphans"), "true"))
            {
               ms_log.error("Processing relationship to item " + id
                       + " that is not in any folder.  This item should be moved");
            }
         }

         int newFolderId;
         try
         {
            newFolderId = folderProc.getIdByPath(calculatedFolderPath);
            if (newFolderId > 0)
            {
               params.put(IPSHtmlParameters.SYS_FOLDERID, String.valueOf(newFolderId));
               if (origFolderId <= 0)
               {
                  params.put(IPSHtmlParameters.SYS_ORIGINALFOLDERID, String.valueOf(newFolderId));
               }
            }
         }
         catch (PSCmsException e)
         {
            ms_log.error("cannot get folder id for path " + calculatedFolderPath);
         }
         if (calculatedSite != null)
         {
            params.put(IPSHtmlParameters.SYS_SITEID, String.valueOf(calculatedSite.getUUID()));
            if (origSiteId <= 0)
            {
               params.put(IPSHtmlParameters.SYS_ORIGINALSITEID, String.valueOf(calculatedSite.getUUID()));
            }
         }
      }
   }

   /**
    * This method tries to decide which folder to choose by assuming that the
    * closest one to the original page folder, or current folder. This would be
    * the correct assumption if a folder structure had been replicated using
    * copy as link. You would expect to link to the folder under the current
    * section and not the link in a separate folder structure. If this is not
    * what is expected then the correct folder id is required in the
    * relationship.
    *
    * @param folderId
    * @param origFolderId
    * @param currentFolderPaths
    * @param folderProc
    * @return
    */
   private static String findClosestFolderPath(int folderId, int origFolderId, String[] currentFolderPaths,
                                               PSServerFolderProcessor folderProc)
   {
      String calculatedFolderPath = null;
      String originalFolderPath = null;
      try
      {
         String[] originalFolderPaths = folderProc.getItemPaths(new PSLocator(origFolderId));
         if (originalFolderPaths.length > 0)
         {
            originalFolderPath = originalFolderPaths[0];
         }
      }
      catch (PSCmsException | PSNotFoundException e)
      {
         ms_log.error("Cannot find folder for id " + folderId);
      }

      int index = 0;
      ArrayList<String> matchPaths = new ArrayList<String>();
      if (originalFolderPath != null)
      {
         ms_log.debug("Finding folder item is in with closest match to original original folder " + originalFolderPath);
         for (String path : currentFolderPaths)
         {
            int testIndex = StringUtils.indexOfDifference(path, originalFolderPath);
            // match
            if (testIndex == -1)
            {
               // found exact match use this path
               matchPaths.clear();
               matchPaths.add(path);
               ms_log.debug("item is in same folder as original item, using this folder " + path);

               break;
            }
            else if (testIndex > index)
            {
               // This folder is a closer match to original so use
               // this one.
               index = testIndex;
               matchPaths.clear();
               matchPaths.add(path);
            }
            else
            {
               // This path has the same match length so add it to
               // the list
               matchPaths.add(path);
            }
         }
         if (ms_log.isDebugEnabled() && matchPaths.size() > 1)
         {

            ms_log.debug("Item is in more than one sub paths at same level will pick first. should link with specific folder in relationship");
         }

         if (matchPaths.size() > 0)
         {
            calculatedFolderPath = matchPaths.get(0);
            ms_log.debug("Setting folder path to " + calculatedFolderPath);
         }
      }
      return calculatedFolderPath;
   }

   /**
    * If enabled in the server properties will adjust the assembly item and
    * fixup the folder and site ids for the item if they are incomplete or
    * incorrect. This will be easy if the item is in a single folder, but extra
    * logic is required if the item is in more than one site or folder.
    *
    * @param item
    */
   public static void fixupSiteFolderId(IPSAssemblyItem item)
   {
      String itemFolderStr = item.getParameterValue(IPSHtmlParameters.SYS_FOLDERID, null);
      String itemSiteStr = item.getParameterValue(IPSHtmlParameters.SYS_SITEID, null);

      String origSiteId = item.getParameterValue(IPSHtmlParameters.SYS_ORIGINALSITEID, null);
      String origFolderId = item.getParameterValue(IPSHtmlParameters.SYS_ORIGINALFOLDERID, null);
      if (origSiteId==null && itemSiteStr != null )
      {
         item.setParameterValue(IPSHtmlParameters.SYS_ORIGINALSITEID, itemSiteStr);
      }
      if (origFolderId==null && itemFolderStr != null )
      {
         item.setParameterValue(IPSHtmlParameters.SYS_ORIGINALFOLDERID, itemFolderStr);
      }

      if (shouldFixIds())
      {
         HashMap<String, String> params = new HashMap<String, String>();

         // If site and folder id are not in the relationship and we are creating a link within
         // a snippet and the snippet was originally inserted with a site id of a different site
         // we need to assume we are using the same site id as the parent item.
         if (itemSiteStr==null && itemFolderStr == null) {
            IPSAssemblyItem parentItem = item.getCloneParentItem();
            if (parentItem !=null)
               itemSiteStr = parentItem.getParameterValue(IPSHtmlParameters.SYS_SITEID, null);
         }

         params.put(IPSHtmlParameters.SYS_CONTENTID, item.getParameterValue(IPSHtmlParameters.SYS_CONTENTID, null));
         params.put(IPSHtmlParameters.SYS_FOLDERID, itemFolderStr);
         params.put(IPSHtmlParameters.SYS_SITEID, itemSiteStr);
         params.put(IPSHtmlParameters.SYS_ORIGINALSITEID,
                 item.getParameterValue(IPSHtmlParameters.SYS_ORIGINALSITEID, null));
         params.put(IPSHtmlParameters.SYS_ORIGINALFOLDERID,
                 item.getParameterValue(IPSHtmlParameters.SYS_ORIGINALFOLDERID, null));
         fixupSiteFolderId(params);
         String newFolderidStr = params.get(IPSHtmlParameters.SYS_FOLDERID);
         String newSitesidStr = params.get(IPSHtmlParameters.SYS_SITEID);
         if (newFolderidStr != null && !newFolderidStr.equals(itemFolderStr))
         {
            item.setParameterValue(IPSHtmlParameters.SYS_FOLDERID, String.valueOf(newFolderidStr));

         }
         if (item.getParameterValue(IPSHtmlParameters.SYS_ORIGINALFOLDERID, null) == null)
         {
            item.setParameterValue(IPSHtmlParameters.SYS_ORIGINALFOLDERID, String.valueOf(newFolderidStr));
         }
         if (newSitesidStr != null && !newSitesidStr.equals(itemSiteStr))
         {
            item.setParameterValue(IPSHtmlParameters.SYS_SITEID, String.valueOf(newSitesidStr));

         }
         if (item.getParameterValue(IPSHtmlParameters.SYS_ORIGINALSITEID, null) == null)
         {
            item.setParameterValue(IPSHtmlParameters.SYS_ORIGINALSITEID, String.valueOf(newSitesidStr));
         }
         int siteid = NumberUtils.toInt(newSitesidStr);
         int folderid = NumberUtils.toInt(newFolderidStr);
         IPSGuid siteGuid = (siteid > 0) ? new PSGuid(PSTypeEnum.SITE, siteid) : null;
         item.setSiteId(siteGuid);
         item.setFolderId(folderid);

      }
   }

   /**
    * Given a folder will try and find the correct site id. Normally a folder
    * will map to a single site. Sometimes customers have mapped more than one
    * site to a folder root, usually for a staging and public publish. In this
    * case we need to be able to identify the correct siteid to link to. To do
    * this we must group the sites into logical units. e.g. a staging site will
    * always point to the staging version of another site.
    *
    * @param originalSiteId
    * @param site
    * @param calculatedFolderPath
    * @param id
    * @return
    */
   private static IPSGuid findSiteForFolder(IPSGuid originalSiteId, IPSGuid site, String calculatedFolderPath, int id)
   {

      IPSSiteManager siteMgr = PSSiteManagerLocator.getSiteManager();
      PSServerFolderProcessor folderProc = PSServerFolderProcessor.getInstance();

      List<IPSSite> folderSites = null;
      // need guid, extract method.
      IPSGuid folderGuid = null;
      try
      {
         int folderid = folderProc.getIdByPath(calculatedFolderPath);
         folderGuid = new PSLegacyGuid(folderid, -1);
         folderSites = siteMgr.getItemSites(folderGuid);
      }
      catch (PSCmsException e)
      {
         ms_log.debug("Cannot find site for path " + calculatedFolderPath);
         return null;
      }

      IPSGuid calculatedSite = null;
      if (folderSites.size() == 1)
      {
         // use actual site and folder regardless of what was passed in
         calculatedSite = folderSites.get(0).getGUID();
      }
      else if (folderSites.size() > 1)
      {
         calculatedSite = findSiteWhenDuplicateMappings(originalSiteId, site, calculatedFolderPath, folderSites, id);
      }
      else if (folderSites.size() == 0)
      {
         calculatedSite = originalSiteId;
      }

      return calculatedSite;
   }

   /**
    * When a folder is mapped to multiple sites we need to use the original
    * site. i.e. the site we are publishing to work out which version. We
    * currently map this in the server.properties file.
    *
    * @param originalSiteId
    * @param site
    * @param calculatedFolderPath
    * @param folderSites
    * @param itemId
    * @return
    */
   private static IPSGuid findSiteWhenDuplicateMappings(IPSGuid originalSiteId, IPSGuid site,
                                                        String calculatedFolderPath, List<IPSSite> folderSites, int itemId)
   {
      IPSGuid calculatedSite = null;
      /*
       * As the item is in only one folder we assume that the separate sites we
       * have found are all mapped to the same folder and are equivalent sites
       * for staging,public etc. We need to know how to map a staging site to an
       * equivalent staging version of the site.
       */
      ms_log.debug("Folder " + calculatedFolderPath
              + " is mapped to more than one site definition finding which one to use");

      initializeSiteGroups();

      // Calculate current group from original site id if it exisits or site id
      // otherwise

      // If we have a site id and no original site id we assume this is an
      // initial assmbly item from a preview request
      // or publish, On publish site id is allways passed in.
      if (m_siteIdToGroup.size() > 0)
      {

         String baseSiteGroup = null;
         if (originalSiteId != null)
         {
            baseSiteGroup = m_siteIdToGroup.get(originalSiteId.getUUID());
         }
         else if (site != null)
         {
            baseSiteGroup = m_siteIdToGroup.get(site.getUUID());
         }
         String baseSiteGroupName = baseSiteGroup == null ? "default" : baseSiteGroup;
         // If we do not have original site id or site id specified then we use
         // the default site group.

         ms_log.debug("Using configured site group mapping in server.properties to locate site to link to");

         String linkSitePath = null;
         String currentSitePath = null;

         List<IPSSite> groupSites = new ArrayList<>();
         List<IPSSite> defaultSites = new ArrayList<>();

         for (IPSSite siteTestItem : folderSites)
         {

            if (site != null && siteTestItem.getGUID().equals(site))
            {
               linkSitePath = siteTestItem.getFolderRoot();
            }
            if (originalSiteId != null && siteTestItem.getGUID().equals(originalSiteId.getUUID()))
            {
               currentSitePath = siteTestItem.getFolderRoot();
            }
            String siteGroup = m_siteIdToGroup.get(siteTestItem.getGUID().getUUID());

            if (siteGroup == null && baseSiteGroup == null)
            {
               defaultSites.add(siteTestItem);
            }
            else if ((siteGroup != null && baseSiteGroup != null) && baseSiteGroup.equals(siteGroup))
            {
               groupSites.add(siteTestItem);
            }

         }

         if (groupSites.size() > 0 || defaultSites.size() > 0)
         {
            // Filtered sites may be subfolders of each other. If the link
            // specifies a site that matches one of these then we will use that
            // particular site id.
            // Otherwise if one of these is the current site we will use that
            // site id. If we still cannot decide which site we will try the
            // default site, .

            if (linkSitePath != null)
            {
               calculatedSite = findSiteInSiteGroup(site, groupSites, linkSitePath);
            }
            if (calculatedSite == null && currentSitePath != null)
            {
               calculatedSite = findSiteInSiteGroup(originalSiteId, groupSites, currentSitePath);
            }
            if (calculatedSite == null && linkSitePath != null)
            {
               calculatedSite = findSiteInSiteGroup(site, defaultSites, linkSitePath);
            }
            if (calculatedSite == null && currentSitePath != null)
            {
               calculatedSite = findSiteInSiteGroup(originalSiteId, defaultSites, currentSitePath);
            }
            if (calculatedSite == null && groupSites.size() > 0)
            {
               calculatedSite = groupSites.get(0).getGUID();
            }
            if (calculatedSite == null && defaultSites.size() > 0)
            {
               calculatedSite = defaultSites.get(0).getGUID();
            }

         }

      }
      else
      {
         ms_log.error("Found multiple sites mapped to path " + calculatedFolderPath
                 + " need to configure site groups in server.properties or do not have more than one"
                 + " site definition matching the same site root or subfolder.");
         for (IPSSite destSite : folderSites)
         {
            int siteid = destSite.getGUID().getUUID();
            ms_log.error("Mapped site =" + destSite.getName() + " id=" + siteid);
         }

      }
      // Work out site without help of site groups
      if (calculatedSite == null)
      {
         List<IPSGuid> folderSiteGuids = new ArrayList<IPSGuid>();

         for (IPSSite siteTestItem : folderSites)
            folderSiteGuids.add(siteTestItem.getGUID());

         if (site != null && folderSiteGuids.contains(site))
         {
            calculatedSite = site;
         }
         else if (originalSiteId != null && folderSiteGuids.contains(site))
         {
            calculatedSite = originalSiteId;
         }
         else
         {
            calculatedSite = folderSites.get(0).getGUID();
         }
      }
      return calculatedSite;
   }

   /**
    * @param site
    * @param filteredSitesByGroup
    * @param linkSitePath
    * @return
    */
   private static IPSGuid findSiteInSiteGroup(IPSGuid site, List<IPSSite> filteredSitesByGroup, String linkSitePath)
   {
      IPSGuid calculatedSite;
      IPSGuid linkSiteGuid = null;
      for (IPSSite testSite : filteredSitesByGroup)
      {
         if (testSite.getFolderRoot().equalsIgnoreCase(linkSitePath))
         {
            if (testSite.getGUID() == site)
            {
               linkSiteGuid = site;
               break;
            }
            else if (linkSiteGuid == null)
            {
               linkSiteGuid = testSite.getGUID();
            }
         }
      }
      calculatedSite = linkSiteGuid;
      return calculatedSite;
   }



}
