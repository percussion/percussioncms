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

package com.percussion.search;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.error.PSException;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.timing.PSStopwatchStack;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This extension acts on all search result rows that have one or more of the
 * following columns:
 * <ol>
 * <li>{@link com.percussion.util.IPSHtmlParameters#SYS_CONTENTTYPEID}</li>
 * <li>{@link com.percussion.util.IPSHtmlParameters#SYS_FOLDERID}</li>
 * <li>{@link com.percussion.util.IPSHtmlParameters#SYS_SITEID}</li>
 * <li>{@link com.percussion.util.IPSHtmlParameters#SYS_VARIANTID}</li>
 * </ol>
 * subject to the the conditions below (the processing has multiple steps and
 * intermediate results may differ from final results):
 * <ul>
 * <li>Replaces the default display value (configured in the content editor
 * system definition) for all the above fields with their names. For
 * {@link com.percussion.util.IPSHtmlParameters#SYS_FOLDERID} column the display
 * value will be the full folder path.</li>
 * <li>If a row has {@link com.percussion.util.IPSHtmlParameters#SYS_FOLDERID}
 * column, the row will be expanded in that it will be cloned for every folder
 * the item in the row exists. If it is not the child of any folder, no action
 * is taken.</li>
 * <li>If a row has {@link com.percussion.util.IPSHtmlParameters#SYS_SITEID} as
 * well as {@link com.percussion.util.IPSHtmlParameters#SYS_FOLDERID} then the
 * site column display value will be set to the site name the folder path
 * corresponds to. If the folder path does not correspond to a registered site,
 * the display value and internal value for site column will be set to empty.</li>
 * <li>If a row has {@link com.percussion.util.IPSHtmlParameters#SYS_SITEID}
 * column but not {@link com.percussion.util.IPSHtmlParameters#SYS_FOLDERID}
 * column, the row will be expanded to contain one row per site the item exists
 * in. For example, if the item in the row exists in "Internet" and "Internet
 * Mirror", that row becomes two rows with every column unchanged except for
 * {@link com.percussion.util.IPSHtmlParameters#SYS_SITEID} column. If an item
 * does not exist in a site, no action is taken.</li>
 * <li>If the search results are in the context of a slot (RC Search) and the
 * rows contain {@link com.percussion.util.IPSHtmlParameters#SYS_SITEID} and/or
 * {@link com.percussion.util.IPSHtmlParameters#SYS_FOLDERID} columns and any of
 * the column values is empty the rows are filtered out.</li>
 * <li>If the search results are in the context of a slot (RC Search) and the
 * rows contain {@link com.percussion.util.IPSHtmlParameters#SYS_SITEID} and
 * {@link com.percussion.util.IPSHtmlParameters#SYS_VARIANTID} columns, the rows
 * are validated to make sure the variant is of type page and is configured for
 * the site. If validation fails the row will be filtered out from the search
 * results.
 * </ul>
 * <p>
 * Implementation note: The only significant time user in this class is 
 * {@link #expandByVariant(IPSRequestContext, List)}, which needs to get all
 * node definitions. This is not cached at this time, it is not a major cost.
 */
@SuppressWarnings("unchecked")
public class PSAddVariantSiteFolder extends PSDefaultExtension
      implements
         IPSSearchResultsProcessor
{
   /**
    * Commons logger
    */
    private static final Logger ms_log = LogManager.getLogger(PSAddVariantSiteFolder.class);
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSSearchResultsProcessor
    *      #processRows(java.lang.Object[],java.util.List,
    *      com.percussion.server.IPSRequestContext)
    */
  public List processRows(@SuppressWarnings("unused") Object[] params, List rows, 
        IPSRequestContext request) throws PSExtensionProcessingException
   {
      rows = expandByVariant(request, rows);
      rows = expandBySiteFolders(request, rows);
      rows = expandBySites(request, rows);
      rows = filterRows(request, rows);

      return rows;
   }

   /**
    * Remove any row from the list if:
    * <ol>
    * <li>The row has {@link com.percussion.util.IPSHtmlParameters#SYS_SITEID}
    * column and the variant is configured as not allowed on that site.</li>
    * <li>If the search results are for a specified parent folder and the row
    * has {@link com.percussion.util.IPSHtmlParameters#SYS_FOLDERID} column
    * value that does not inherit from the parent folder. </l>
    * <li>If the search results are for a specified parent folder and the row
    * has {@link com.percussion.util.IPSHtmlParameters#SYS_SITEID} column value
    * that does not inherit from the parent folder. </l>
    * </ol>
    * <p>
    * In the last two cases above, the parent folder path is taken from the
    * request context parameter named
    * {@link IPSHtmlParameters#SYS_PARENTFOLDERPATH}.
    * 
    * @param request request context, assumed not <code>null</code>.
    * @param rows the rows to filter, assumed not <code>null</code> and each
    *           entry in the list to be {@link IPSSearchResultRow} object. May
    *           be empty.
    * @return filtered row list, never <code>null</code>, may be empty.
    * @throws PSExtensionProcessingException if it fails to build
    *            variant-allowed sites map for any reason.
    */
   private List filterRows(IPSRequestContext request, List rows)
         throws PSExtensionProcessingException
   {
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSAssemblyService assembly = PSAssemblyServiceLocator.getAssemblyService();
      for (int i = rows.size() - 1; i >= 0; i--)
      {
         IPSSearchResultRow row = (IPSSearchResultRow) rows.get(i);

         String parentFolderPath = request.getParameter(
               IPSHtmlParameters.SYS_PARENTFOLDERPATH, "");
         Map siteMap = null;
         if (parentFolderPath != null && parentFolderPath.length() > 0)
         {
            parentFolderPath = ensurePathEndsWithSlash(parentFolderPath);
            if (row.hasColumn(IPSHtmlParameters.SYS_FOLDERID))
            {
               String path = row
                     .getColumnDisplayValue(IPSHtmlParameters.SYS_FOLDERID);
               if (path == null)
                  path = "";
               path = ensurePathEndsWithSlash(path);
               if (!path.startsWith(parentFolderPath))
               {
                  rows.remove(row);
                  continue;
               }
            }
            else if (row.hasColumn(IPSHtmlParameters.SYS_SITEID))
            {
               if (siteMap == null)
               {
                  siteMap = buildSiteFolderMapBySiteId(getSiteMap());
               }
               String siteid = row.getColumnValue(IPSHtmlParameters.SYS_SITEID);
               if (siteid != null && siteid.length() > 0)
               {
                  SiteFolder sf = (SiteFolder) siteMap.get(siteid);
                  if (sf != null)
                  {
                     String sfPath = sf.getSiteFolderRoot();
                     if (sfPath != null)
                     {
                        sfPath = ensurePathEndsWithSlash(sfPath);
                        if (!parentFolderPath.startsWith(sfPath))
                        {
                           rows.remove(row);
                           continue;
                        }
                     }
                  }
               }
            }
         }

         if (!row.hasColumn(IPSHtmlParameters.SYS_SITEID))
            continue;

         String variantid = row.getColumnValue(IPSHtmlParameters.SYS_VARIANTID);
         if (StringUtils.isBlank(variantid) || 
               !StringUtils.isNumeric(variantid))
            continue;

         IPSAssemblyTemplate template;
         try
         {
            template = assembly.loadUnmodifiableTemplate(variantid);
         }
         catch (PSAssemblyException e)
         {
            throw new PSExtensionProcessingException("PSAddVariantSiteFolder", e);
         }
        if (template != null)
        {
           boolean isPage = template.getOutputFormat()
              .equals(IPSAssemblyTemplate.OutputFormat.Page);
           //Only page variants are validated for site variant registration
            if (isPage)
            {
               String siteid = row.getColumnValue(IPSHtmlParameters.SYS_SITEID);
               if (StringUtils.isBlank(siteid) || ! StringUtils.isNumeric(siteid))
               {
                  rows.remove(row);
                  continue;
               }
               IPSGuid siteguid = gmgr.makeGuid(siteid, PSTypeEnum.SITE);
               try
               {
                  IPSSite site = smgr.loadSite(siteguid);
                  long variantnumericid = Long.parseLong(variantid);
                  boolean found = false;

                  for (IPSAssemblyTemplate t : site.getAssociatedTemplates())
                  {
                     if (t.getGUID().longValue() == variantnumericid)
                     {
                        found = true;
                        break;
                     }
                  }

                  if (!found)
                  {
                     rows.remove(row);
                     continue;
                  }
               }
               catch(PSNotFoundException e)
               {
                  ms_log.error("The site " + siteid + " was not found", e);
                  rows.remove(row);
                  continue;
               }
            }
         }
      }
      return rows;
   }

   /**
    * Helper routine to assure the supplied path ends with a "/".
    * 
    * @param path path to modify to a trailing "/" if does not already end with
    *           "/", assumed not <code>null</code>, may be empty.
    * @return path that ends with trailing "/". Never <code>null</code>.
    */
   private String ensurePathEndsWithSlash(String path)
   {
      if (!path.endsWith("/"))
         path = path + "/";
      return path;
   }

   /**
    * Helper routine to build a site folder map in which the key is the siteid
    * as string and the value is the {@link SiteFolder} object for that site.
    * 
    * @param siteMap the site folder map as returned by
    *           {@link #getSiteMap()}.
    * @return site folder map as explained above. Never <code>null</code>,
    *         may be empty.
    */
   private Map buildSiteFolderMapBySiteId(Map siteMap)
   {
      Map siteFolderMapById = new HashMap();
      Iterator iter = siteMap.values().iterator();
      while (iter.hasNext())
      {
         Set sfSet = (Set) iter.next();
         Iterator iterSf = sfSet.iterator();
         while (iterSf.hasNext())
         {
            SiteFolder sf = (SiteFolder) iterSf.next();
            siteFolderMapById.put(sf.getSiteId(), sf);
         }
      }
      return siteFolderMapById;
   }

   /**
    * This method expands each row by cloning to contain as many rows as the
    * number of folders the item in the row exists in. The internal and display
    * values for {@link IPSHtmlParameters#SYS_FOLDERID} are set appropriately.
    * See class description for more details.
    * 
    * @param request request context, assumed not <code>null</code>
    * @param rows search result rows to expand by folder path, assumed not
    *           <code>null</code>. May be empty.
    * @return resulting rows after expansion, never <code>null</code>, may be
    *         empty.
    * @throws PSExtensionProcessingException if it fails to build required
    *            contentid-sitemap by making internal request.
    */
   private List expandBySiteFolders(IPSRequestContext request, List rows)
         throws PSExtensionProcessingException
   {
      int size = rows.size();
      if (size == 0)
         return rows;

      Set contentIdSet = new HashSet();
      Map<String,Integer> folderIdMap = new HashMap<>();
      boolean hasFolderId = false;
      for (int i = 0; i < size; i++)
      {
         IPSSearchResultRow row = (IPSSearchResultRow) rows.get(i);
         String cid = row.getColumnValue(IPSHtmlParameters.SYS_CONTENTID);
         if (cid == null || cid.length() == 0)
            continue;
         contentIdSet.add(cid);

         if (row.hasColumn(IPSHtmlParameters.SYS_FOLDERID))
            hasFolderId = true;
      }

      // Do nothing if there is no content id or folder id data
      if (contentIdSet.isEmpty() || (!hasFolderId))
      {
         return rows;
      }

      Map cidFolderMap = getParentFolderPaths(request, (String[]) contentIdSet
            .toArray(new String[0]));

      for (int i = size - 1; i >= 0; i--)
      {
         IPSSearchResultRow row = (IPSSearchResultRow) rows.get(i);

         String contentid = row.getColumnValue(IPSHtmlParameters.SYS_CONTENTID);
         if (contentid == null || contentid.length() == 0)
            continue;

         if (!row.hasColumn(IPSHtmlParameters.SYS_FOLDERID))
         {
            continue;
         }

         String[] paths = (String[]) cidFolderMap.get(contentid);

         if (paths == null || paths.length == 0)
         {
            // Item does not exist under any site, set values to empty
            row.setColumnDisplayValue(IPSHtmlParameters.SYS_FOLDERID, "");
            row.setColumnValue(IPSHtmlParameters.SYS_FOLDERID, "");
            continue;
         }

         boolean cloneRow = false;
         for (int j = 0; j < paths.length; j++)
         {
            String path = paths[j];
            if (!cloneRow)
            {
               cloneRow = true;
            }
            else
            {
               row = row.cloneRow();
               rows.add(row);
            }
            if (row.hasColumn(IPSHtmlParameters.SYS_FOLDERID))
            {
               row.setColumnDisplayValue(IPSHtmlParameters.SYS_FOLDERID, path);
               row.setColumnValue(IPSHtmlParameters.SYS_FOLDERID,
                     getFolderIdByPath(request, path, folderIdMap));
            }
         }
      }
      return rows;
   }

   /**
    * This method expands each row by cloning to contain as many rows as the
    * number of sites the item in the row exists in. The internal and display
    * values for {@link IPSHtmlParameters#SYS_SITEID} are set appropriately. See
    * class description for more details
    * 
    * @param request request context, assumed not <code>null</code>
    * @param rows search result rows to expand by sites, assumed not
    *           <code>null</code>. May be empty.
    * @return resulting rows after expansion, never <code>null</code>, may be
    *         empty.
    * @throws PSExtensionProcessingException if it fails to build required
    *            contentid-sitemap by making internal request.
    */
   private List expandBySites(IPSRequestContext request, List rows)
         throws PSExtensionProcessingException
   {
      int size = rows.size();
      if (size == 0)
         return rows;

      Map siteMap = null;
      Map<String, Set<PSSiteRef>> contentIdSiteMap = null;

      for (int i = size - 1; i >= 0; i--)
      {
         IPSSearchResultRow row = (IPSSearchResultRow) rows.get(i);

         if (!row.hasColumn(IPSHtmlParameters.SYS_SITEID))
         {
            continue;
         }

         String contentid = row.getColumnValue(IPSHtmlParameters.SYS_CONTENTID);
         if (contentid == null || contentid.length() == 0)
            continue;

         // If the row has folderid column it would already contain folder paths
         if (row.hasColumn(IPSHtmlParameters.SYS_FOLDERID))
         {
            // lazy load the site map
            if (siteMap == null)
               siteMap = getSiteMap();

            String path = row
                  .getColumnDisplayValue(IPSHtmlParameters.SYS_FOLDERID);
            path = ensurePathEndsWithSlash(path);
            Set sfSet = getSiteFolderSetForPath(siteMap, path);
            if (sfSet == null || sfSet.isEmpty())
            {
               row.setColumnValue(IPSHtmlParameters.SYS_SITEID, "");
               row.setColumnDisplayValue(IPSHtmlParameters.SYS_SITEID, "");
               continue;
            }
            Iterator sfIter = sfSet.iterator();
            boolean cloneRow = false;
            while (sfIter.hasNext())
            {
               SiteFolder sf = (SiteFolder) sfIter.next();
               if (!cloneRow)
               {
                  cloneRow = true;
               }
               else
               {
                  row = row.cloneRow();
                  rows.add(row);
               }
               row.setColumnValue(IPSHtmlParameters.SYS_SITEID, sf.getSiteId());
               row.setColumnDisplayValue(IPSHtmlParameters.SYS_SITEID, sf
                     .getSiteName());
            }
         }
         else
         {
            // lazy load ContentId & Site map
            if (contentIdSiteMap == null)
            {
               Set<String> contentIdSet = new HashSet<>();
               for (int j = 0; j < size; j++)
               {
                  IPSSearchResultRow r = (IPSSearchResultRow) rows.get(j);
                  String cid = r
                        .getColumnValue(IPSHtmlParameters.SYS_CONTENTID);
                  if (cid == null || cid.length() == 0)
                     continue;
                  contentIdSet.add(cid);
               }
              contentIdSiteMap = buildContentIdSiteMap(request,
                    contentIdSet.toArray(new String[0]));
           }
            Set<PSSiteRef> sfSet = contentIdSiteMap.get(contentid);
            if (sfSet == null || sfSet.isEmpty())
            {
               row.setColumnValue(IPSHtmlParameters.SYS_SITEID, "");
               row.setColumnDisplayValue(IPSHtmlParameters.SYS_SITEID, "");
               continue;
            }
            Iterator<PSSiteRef> sfIter = sfSet.iterator();
            boolean cloneRow = false;
            while (sfIter.hasNext())
            {
               PSSiteRef sf = sfIter.next();
               if (!cloneRow)
               {
                  cloneRow = true;
               }
               else
               {
                  row = row.cloneRow();
                  rows.add(row);
               }
               row.setColumnValue(IPSHtmlParameters.SYS_SITEID, sf.getSiteId());
              row.setColumnDisplayValue(IPSHtmlParameters.SYS_SITEID, 
                 sf.getSiteName());
            }
         }
      }
      return rows;
   }

   /**
    * Helper method to walkthrough and find site folder set for a given folder
    * path.
    * 
    * @param siteMap site map object as returned by
    *           {@link #getSiteMap()}. If <code>null</code>
    *           or empty, the return value will be <code>null</code>.
    * @param path the folder path to look up in the site map, if
    *           <code>null</code> or empty, the return value will be
    *           <code>null</code>.
    * @return set of site folders ({@link SiteFolder} objects) that the folder
    *         with given path is part of, may be <code>null</code> or empty.
    */
   private Set getSiteFolderSetForPath(Map siteMap, String path)
   {
      if (siteMap == null || siteMap.isEmpty() || path == null
            || path.length() == 0)
         return null;
      Iterator iter = siteMap.keySet().iterator();
      while (iter.hasNext())
      {
         String key = (String) iter.next();
         if (path.startsWith(key))
            return (Set) siteMap.get(key);
      }
      return null;
   }

   /**
    * This method expands each row by cloning to contain as many rows as the
    * number of variants that are allowed for the slot in the context. The
    * internal and display values for sys_siteid and sys_folderid are set
    * appropriately.
    * 
    * @param request request context, assumed not <code>null</code>.
    * @param rows search result rows to expand by variants, assumed not
    *           <code>null</code>. May be empty.
    * @return resulting rows after expansion, never <code>null</code>, may be
    *         empty.
    * @throws PSExtensionProcessingException if it fails to build required
    *            contenttype-allowed variant map by making internal request.
    */
   private List expandByVariant(IPSRequestContext request, List rows)
         throws PSExtensionProcessingException
   {
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start(getClass().getCanonicalName() + "#expandByVariant");
      Map varMap = null;
      List<IPSNodeDefinition> nodedefs = null;
      IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();

      String slotId = request.getParameter(IPSHtmlParameters.SYS_SLOTID, "");

      int size = rows.size();
      for (int i = size - 1; i >= 0; i--)
      {
         IPSSearchResultRow row = (IPSSearchResultRow) rows.get(i);

         String ctId = row.getColumnValue(IPSHtmlParameters.SYS_CONTENTTYPEID);
         if (StringUtils.isBlank(ctId) || !StringUtils.isNumeric(ctId))
            continue;         
         long contentTypeId = Long.parseLong(ctId);

         if (row.hasColumn(IPSHtmlParameters.SYS_CONTENTTYPEID))
         {
            if (nodedefs == null)
            {
               try
               {
                  nodedefs = cmgr.findAllItemNodeDefinitions();
               }
               catch (RepositoryException e)
               {
                  throw new PSExtensionProcessingException(m_def.getRef()
                        .getExtensionName(), e);
               }
            }

            IPSNodeDefinition founddef = null;
            for(IPSNodeDefinition def : nodedefs)
            {
               if (def.getGUID().longValue() == contentTypeId)
               {
                  founddef = def;
                  break;
               }
            }
            
            String typeName;
            if (founddef == null || StringUtils.isBlank(founddef.getLabel()))
            {
               if (contentTypeId == PSFolder.FOLDER_CONTENT_TYPE_ID)
               {
                  typeName = "Folder";
               }
               else
               {
                  rows.remove(row);
                  continue;
               }
            }
            else
            {
               typeName = founddef.getLabel();
            }
            row.setColumnDisplayValue(IPSHtmlParameters.SYS_CONTENTTYPEID,
                  typeName);
         }

         if (!row.hasColumn(IPSHtmlParameters.SYS_VARIANTID))
            continue;

         // Lasly load the map since it is very expensive to build the map.
         if (varMap == null)
         {
            try
            {
               varMap = getVariantMap(slotId);
            }
            catch (PSException e)
            {
               throw new PSExtensionProcessingException(m_def.getRef()
                     .getExtensionName(), e);
            }
         }

         // add first variant to this map, then create copies to add
         // to end of list for remaining variants
         Map variantMap = (Map) varMap.get(ctId);
         if (variantMap == null || variantMap.isEmpty())
         {
            rows.remove(row);
            continue;
         }

         Iterator vars = variantMap.keySet().iterator();
         boolean cloneRow = false;
         while (vars.hasNext())
         {
            String varId = (String) vars.next();
            if (!cloneRow)
            {
               cloneRow = true;
            }
            else
            {
               row = row.cloneRow();
               rows.add(row);
            }
            row.setColumnDisplayValue(IPSHtmlParameters.SYS_VARIANTID,
                  (String) variantMap.get(varId));
            row.setColumnValue(IPSHtmlParameters.SYS_VARIANTID, varId);
         }
      }
      sws.stop();
      return rows;
   }

   /**
    * Builds a map of content type id and allowed variants for the slot with
    * supplied slotid.
    * @param slotId The slot id, assumed not <code>null</code> may be empty in
    *           which case all content type varaints with slots registered will
    *           be returned.
    * 
    * @return The map, where the key is the content type id as a
    *         <code>String</code>, and the value is a <code>Map</code>
    *         containing variant id (<code>String</code> )as key and variant
    *         name as value. Never <code>null</code>.
    * @throws PSExtensionProcessingException if any errors occur during internal
    *            request to get the allowed content type varaints for the alot.
    */
    private Map getVariantMap(String slotId)
         throws PSExtensionProcessingException
   {
      try
      {
         IPSAssemblyService assembly = PSAssemblyServiceLocator
               .getAssemblyService();
         IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
         Collection<IPSAssemblyTemplate> templates = null;
         Collection<PSPair<IPSGuid, IPSGuid>> slotAssocColl = null;
         templates = assembly.findAllTemplates();
         if (StringUtils.isBlank(slotId))
         {
            // Keep any with at least one registered slot
            List<IPSAssemblyTemplate> twithslots = 
               new ArrayList<>();
            for(IPSAssemblyTemplate t : templates)
            {
               if (t.getSlots().size() > 0)
                  twithslots.add(t);
            }
            templates = twithslots;
         }
         else
         {
            IPSTemplateSlot slot = assembly.loadSlot(slotId);
            slotAssocColl = slot.getSlotAssociations();

            templates = filterTemplates(slotAssocColl, templates);
         }
         Map<String, Map<String, String>> varMap = new HashMap<>();
         for (IPSAssemblyTemplate template : templates)
         {
            List<IPSNodeDefinition> defs = cmgr
                  .findNodeDefinitionsByTemplate(template.getGUID());
            defs = filterNodeDefs(slotAssocColl, template, defs);

            Map<String, String> variantMap = null;
            for (IPSNodeDefinition def : defs)
            {
               variantMap = varMap.get(Long.toString(
                  def.getGUID().longValue()));
               if (variantMap == null)
               {
                  variantMap = new HashMap<>();
                  varMap.put(Long.toString(def.getGUID().longValue()),
                        variantMap);
               }
               variantMap.put(Long.toString(template.getGUID().longValue()),
                     template.getLabel());
            }
         }
         return varMap;
      }
      catch (PSMissingBeanConfigurationException e)
      {
         throw new PSExtensionProcessingException("Problem loading bean", e);
      }
      catch (PSAssemblyException e)
      {
         throw new PSExtensionProcessingException("Problem loading template "
               + "or slot information", e);
      }
      catch (RepositoryException e)
      {
         throw new PSExtensionProcessingException("Problem loading "
               + "content type info", e);
      }
   }

   /**
    * Filter the supplied collection of node defintions based on the specified
    * template and the slot associations.
    * 
    * @param slotAssocColl The collection of slot associations returned by
    *           {@link IPSTemplateSlot#getSlotAssociations()} to use to filter
    *           the node defs, may be <code>null</code> in which case the
    *           supplied list is simply returned.
    * @param template assumed not <code>null</code>, only node definitions
    *           that are associated to the slot with this template are returned.
    * @param defs The list of node definitions to filter, assumed not
    *           <code>null</code>.
    * 
    * @return The filtered list, never <code>null</code>, may be empty.
    */
   private List<IPSNodeDefinition> filterNodeDefs(
         Collection<PSPair<IPSGuid, IPSGuid>> slotAssocColl,
         IPSAssemblyTemplate template, List<IPSNodeDefinition> defs)
   {
      if (slotAssocColl == null)
         return defs;

      List<IPSNodeDefinition> filtered = new ArrayList<>();
      for (IPSNodeDefinition def : defs)
      {
        for (PSPair<IPSGuid, IPSGuid> pair : slotAssocColl)
        {
           if (pair.getFirst().equals(def.getGUID()) && pair.getSecond().equals(
              template.getGUID()))
           {
               filtered.add(def);
               break;
            }
         }
      }

      return filtered;
   }

   /**
    * Filter supplied collection of templates by removing the templates that are
    * not needed. Filtering is done by keeping only the templates that exist in
    * the slot association collection supplied. Existence is checked by
    * comparing the template guids.
    * 
    * @param coll it is the collection of slot associateions returned by
    *           {@link IPSTemplateSlot#getSlotAssociations()}, assumed not
    *           <code>null</code>.
    * @param templates set of templates to filter, assumed not <code>null</code>.
    * 
    * @return collection as explained above, never <code>null</code>, may be
    *         empty.
    */
   private Collection<IPSAssemblyTemplate> filterTemplates(
         Collection<PSPair<IPSGuid, IPSGuid>> coll,
         Collection<IPSAssemblyTemplate> templates)
   {
      Set<IPSGuid> set = new HashSet<>();
      for (PSPair<IPSGuid, IPSGuid> pair : coll)
         set.add(pair.getSecond());
      Iterator iter = templates.iterator();
      while (iter.hasNext())
      {
         IPSAssemblyTemplate template = (IPSAssemblyTemplate) iter.next();
         if (!set.contains(template.getGUID()))
            iter.remove();
      }
      return templates;
   }

   /**
    * Builds a map of content id and list of sites the item exists in.
    * 
    * @param request request context object, assumed not <code>null</code>.
    * @param cids <code>String</code> array of all content ids to build the
    *           map for, asssumed not <code>null</code> or empty.
    * @return Map of content id and the site folders the item exists in. The key
    *         in the map will be the content id as <code>String</code> and the
    *         value will be a <code>Set</code> of {@link SiteFolder} objects.
    * @throws PSExtensionProcessingException if errors occur during building the
    *            map.
    */
   private Map<String, Set<PSSiteRef>> buildContentIdSiteMap(
         IPSRequestContext request, String[] cids)
         throws PSExtensionProcessingException
   {
     Map<String, Set<PSSiteRef>> cidSiteMap = 
        new HashMap<>();

     Map cidFolderMap = getParentFolderPaths(request, cids);
     Map siteMap = getSiteMap();
     Iterator iter = cidFolderMap.keySet().iterator();
     while (iter.hasNext())
      {
         String cid = (String) iter.next();
         String[] paths = (String[]) cidFolderMap.get(cid);
         Set siteFolderSet = new HashSet<PSSiteRef>();
         cidSiteMap.put(cid, siteFolderSet);
         for (int i = 0; i < paths.length; i++)
         {
            String path = paths[i].replace('\\', '/');
            path = ensurePathEndsWithSlash(path);
            Set sfSet = getSiteFolderSetForPath(siteMap, path);
            if (sfSet != null)
            {
               Iterator sites = sfSet.iterator();
               while (sites.hasNext())
               {
                  SiteFolder siteFolder = (SiteFolder) sites.next();
                  String siteFolderRoot = siteFolder.getSiteFolderRoot();
                  if (siteFolderRoot == null || siteFolderRoot.length() == 0)
                     continue;
                  siteFolderRoot = siteFolderRoot.replace('\\', '/');
                  siteFolderRoot = ensurePathEndsWithSlash(siteFolderRoot);
                  if (path.startsWith(siteFolderRoot))
                  {
                     PSSiteRef siteRef = new PSSiteRef(siteFolder.getSiteId(),
                           siteFolder.getSiteName());
                     siteFolderSet.add(siteRef);
                  }
               }
            }
         }
      }
      return cidSiteMap;
   }

   /**
    * Build a map of siteid and {@link SiteFolder} object by making an interanl
    * request to a rhythmyx resource for site lookup.
    * 
    * @return Map of siteid as <code>String</code> and {@link SiteFolder}
    *         objects. Never <code>null</code> may be empty.
    */
   private Map<String, Set<SiteFolder>> getSiteMap()
   {
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      Map<String, Set<SiteFolder>> siteMap = new HashMap<>();

      for (IPSSite site : smgr.findAllSites())
      {
         String froot = site.getFolderRoot();
         if (StringUtils.isBlank(froot))
            continue;
         froot = ensurePathEndsWithSlash(froot);
         Set siteSet = siteMap.get(froot);
         if (siteSet == null)
         {
            siteSet = new HashSet<SiteFolder>();
            siteMap.put(froot, siteSet);
         }
         String id = Integer.toString(site.getGUID().getUUID());
         String name = site.getName();
         siteSet.add(new SiteFolder(id, name, froot));
      }

      return siteMap;
   }

   /**
    * Build a map of contentid and parent folder paths for each of the supplied
    * content ids using relationship API.
    * 
    * @param request request context, assumed not <code>null</code>
    * 
    * @param cids array of contentid's as <code>String</code> objects for
    *           which the parent folder paths are being requested. assumed not
    *           <code>null</code> or empty.
    * @return a map of contentid and parent folder paths. The key is the
    *         contentid as <code>String</code> and the value is a string array
    *         folder paths, which will never be <code>null</code> but may be
    *         empty. Never <code>null</code>, may be empty.
    * @throws PSExtensionProcessingException if the parent folder paths could
    *            not be obtained from server for any reason.
    */
   private Map getParentFolderPaths(IPSRequestContext request, String[] cids)
         throws PSExtensionProcessingException
   {
      Map map = new HashMap();

      PSRelationshipProcessor proxy;
      try
      {
         proxy = PSRelationshipProcessor.getInstance();
         for (int i = 0; i < cids.length; i++)
         {
            String cid = cids[i];
            PSLocator locator = new PSLocator(cid);
            String[] paths = proxy.getRelationshipOwnerPaths(PSFolder
                  .getComponentType(PSFolder.class), locator,
                  PSRelationshipConfig.TYPE_FOLDER_CONTENT);
            map.put(cid, paths);
         }
      }
      catch (PSCmsException e)
      {
         throw new PSExtensionProcessingException(e.getErrorCode(), e
               .getErrorArguments());
      }
      return map;
   }

   /**
    * Get content id of the folder given the full path.
    * 
    * @param request request context, assumed not <code>null</code>.
    * @param path full folder path, assumed not <code>null</code> or empty.
    * @param folderIdMap ids we look up are cached in this map, assumed never 
    *         <code>null</code>
    * @return content id of the folder specified by the path as
    *         <code>String</code>, never <code>null</code> or empty.
    * @throws PSExtensionProcessingException if it fails to get the id from the
    *            path for any reason.
    */
   private String getFolderIdByPath(IPSRequestContext request, String path, 
         Map<String, Integer> folderIdMap)
         throws PSExtensionProcessingException
   {
      Integer folderid = folderIdMap.get(path);
      if (folderid != null)
      {
         return folderid.toString();
      }
      PSRelationshipProcessor proc = null;
      try
      {
         proc = PSRelationshipProcessor.getInstance();
         int id = proc.getIdByPath(
               PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE, path,
               PSRelationshipConfig.TYPE_FOLDER_CONTENT);
         folderIdMap.put(path, id);
         return Integer.toString(id);
      }
      catch (PSCmsException e)
      {
         throw new PSExtensionProcessingException(e.getErrorCode(), e
               .getErrorArguments());
      }
   }

   /**
    * Inner class to hold data for a site folder. Used mainly in
    * {@link PSAddVariantSiteFolder#expandBySiteFolders(IPSRequestContext,
    * List)} and {@link PSAddVariantSiteFolder#expandBySites(IPSRequestContext,
    * List)}
    */
   private class SiteFolder
   {
      /**
       * Site id of the site folder
       */
      private String m_siteId = null;

      /**
       * Site name of the site folder
       */
      private String m_siteName = null;

      /**
       * Folder root of the site folder
       */
      private String m_folderRoot = null;

      /**
       * Ctor taking the site details
       * 
       * @param siteid site id as string, assumed not <code>null</code> or
       *           empty.
       * @param siteName name of the site, assumed not <code>null</code> or
       *           empty.
       * @param folderRoot folder root path for the site, assumed not
       *           <code>null</code> or empty.
       */
     SiteFolder(String siteid, String siteName, String folderRoot)
     {
         m_siteId = siteid;
         m_siteName = siteName;
         m_folderRoot = folderRoot;
      }

      /**
       * @return The value supplied in the ctor.
       */
      String getSiteId()
      {
         return m_siteId;
      }

      /**
       * @return The value supplied in the ctor.
       */
      String getSiteName()
      {
         return m_siteName;
      }

      /**
       * @return The value supplied in the ctor.
       */
      String getSiteFolderRoot()
      {
         return m_folderRoot;
      }
   }

   /**
    * Object to represent a site reference
    */
   private class PSSiteRef
   {
      /**
       * The site id, never <code>null</code> or empty.
       */
      private String mi_siteId;

      /**
       * The site name, never <code>null</code> or empty.
       */
      private String mi_siteName;

      /**
       * Construct a ref.
       * 
       * @param siteId The site id, assumed not <code>null</code> or empty.
       * @param siteName The site name, assumed not <code>null</code> or
       *           empty.
      */
     PSSiteRef(String siteId, String siteName)
     {
        mi_siteId = siteId;
        mi_siteName = siteName;
      }

      /**
       * Get the site name.
       * 
       * @return The name, never <code>null</code> or empty.
       */
      public String getSiteName()
      {
         return mi_siteName;
      }

      /**
       * Get the site id.
       * 
       * @return The id, never <code>null</code> or empty.
       */
      public String getSiteId()
      {
         return mi_siteId;
      }

      @Override
      public boolean equals(Object obj)
      {
         return EqualsBuilder.reflectionEquals(
               PSAddVariantSiteFolder.PSSiteRef.this, obj);
      }

     @Override
     public int hashCode()
     {
        return HashCodeBuilder.reflectionHashCode(
           PSAddVariantSiteFolder.PSSiteRef.this);
     }
     
     @Override
     public String toString()
     {
        return ToStringBuilder.reflectionToString(
           PSAddVariantSiteFolder.PSSiteRef.this);
     }
     
   }
}
