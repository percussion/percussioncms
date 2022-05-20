/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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

package com.percussion.content.ui.browse;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.content.ui.aa.actions.impl.PSActionUtil;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.content.data.PSItemSummary.ObjectTypeEnum;
import com.percussion.services.content.data.PSItemSummary.OperationEnum;
import com.percussion.services.contentmgr.data.PSNodeDefinition;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCms;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.startsWith;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Singleton class to support the content browser web interface. All the results
 * returned are JSON strings that are meant to be parsed to JavaScript to
 * objects.
 */
public class PSContentBrowser
{
   /**
    * Get all registered sites from the system that are visible to the user
    * based on folder security.
    * 
    * @return site objects as JSON string that resolves to a JSON array. Never
    * <code>null</code> or empty. May return a string that resolves to empty
    * JSON array.
    * @throws PSSiteManagerException
    * @throws JSONException
    * @throws PSErrorException 
    */
   public static String getSites() throws JSONException, PSErrorException
   {
      IPSSiteManager sm = PSSiteManagerLocator.getSiteManager();
      List<IPSSite> sites = sm.findAllSites();
      List<IPSSite> filterSites = new ArrayList<IPSSite>();
      
      Map<String, List<PSItemSummary>> parentPaths = new HashMap<String, List<PSItemSummary>>();
      for (IPSSite site : sites) {
         if (isBlank(site.getFolderRoot()) || ! startsWith(site.getFolderRoot(), "//Sites") ) 
            continue;
         String parentPath = substringBeforeLast(site.getFolderRoot(), "/");
         String name = substringAfterLast(site.getFolderRoot(), "/");
         List<PSItemSummary> items = parentPaths.get(parentPath);
         if (items == null) {
            items = findAndFilterItemSummaries(parentPath);
            parentPaths.put(parentPath, items);
         }
         for (PSItemSummary i : items) {
            if (name != null && name.equals(i.getName())) {
               filterSites.add(site);
               break;
            }
         }
      }
      
      JSONArray result = new JSONArray();
      for (IPSSite site : filterSites)
      {
         if (StringUtils.isBlank(site.getFolderRoot()))
            continue; // skip site without root folder
               
         JSONObject jsonObj = new JSONObject();
         jsonObj.put(COLUMN_ID, site.getSiteId());
         jsonObj.put(COLUMN_NAME, getNameHtml(site.getName()));
         jsonObj.put(COLUMN_DESCRIPTION, StringUtils.defaultString(site.getDescription()));
         jsonObj.put(COLUMN_TYPE, SITE_TYPE);
         jsonObj.put(COLUMN_ICON_PATH, "");
         result.put(jsonObj);
      }
      return result.toString();

   }
   
   /**
    * Finds and filters item summaries based on security. 
    * @param path not null
    * @return not null maybe empty.
    * @throws PSErrorException
    */
   public static List<PSItemSummary> findAndFilterItemSummaries(String path) throws PSErrorException  {
      notNull(path);
      IPSContentWs cws = PSContentWsLocator.getContentWebservice();
      List<PSItemSummary> summaries;
      summaries = cws.findFolderChildren(path, true);
      return filterSummaries(summaries);
   }

   /**
    * Get all root folders (Children of //Folders in the CX) from the system.
    * 
    * @param request request context to instantiate the server folder processor,
    * must not be <code>null</code>.
    * @return root folder objects as JSON string that resolves to a JSON array.
    * Never <code>null</code> or empty. May return a string that resolves to
    * empty JSON array.
    * @throws PSErrorException
    * @throws JSONException
    */
   static public String getRootFolders(IPSRequestContext request)
      throws PSErrorException, JSONException
   {
      if (request == null)
      {
         throw new IllegalArgumentException("request must not be null");
      }
      IPSContentWs cws = PSContentWsLocator.getContentWebservice();
      List<PSItemSummary> summaries = cws.findFolderChildren(
         new PSLegacyGuid(3), true);
      summaries = filterSummaries(summaries);
      JSONArray result = new JSONArray();
      for (PSItemSummary summary : summaries)
         result.put(summaryToJsonObject(summary, new HashMap<String, String>()));
      return result.toString();
   }

   /**
    * Get the children of the supplied absolute site folder path filtered for
    * the supplied content type and slotid.
    * 
    * @param request request context to instantiate the server folder processor,
    * must not be <code>null</code>.
    * @param absSiteFolderPath site folder path which is resolved to an absolute
    * folder path as described in
    * {@link #computeAbsoluteFolderPath(String, StringBuffer)}.
    * @param contentTypeId content typeid to filter the items by,
    * <code>null</code> if no filtering is needed.
    * @param slotId id of the slot for which the children are being asked, must
    * not be <code>null</code> or empty and must be a valid slot in the
    * system.
    * @return child folders and items of the supplied site folder as JSON string
    * that resolves to a JSON array. Never <code>null</code> or empty. May
    * return a string that resolves to empty JSON array.
    * @throws PSSiteManagerException if site in the provided path does not exist.
    * @throws PSErrorException
    * @throws JSONException
    * @throws PSAssemblyException
    */
   static public String getSiteFolderChildren(IPSRequestContext request,
      String absSiteFolderPath, String contentTypeId, String slotId)
           throws PSSiteManagerException, PSErrorException, JSONException,
           PSAssemblyException, PSNotFoundException {
      if (request == null)
      {
         throw new IllegalArgumentException("request must not be null");
      }
      if (slotId == null || slotId.length() == 0)
      {
         throw new IllegalArgumentException("slotId must not be null or empty");
      }
      int ctypeid = -1;
      if (!StringUtils.isEmpty(contentTypeId))
      {
         try
         {
            ctypeid = Integer.parseInt(contentTypeId);
         }
         catch (NumberFormatException e)
         {
            throw new IllegalArgumentException("content type id specified as '"
               + contentTypeId + "' is invalid");
         }
      }

      try
      {
         Integer.parseInt(slotId);
      }
      catch (NumberFormatException e)
      {
         throw new IllegalArgumentException("slot id specified as '" + slotId
            + "' is invalid");
      }

      StringBuffer tmp1 = new StringBuffer();
      computeAbsoluteFolderPath(absSiteFolderPath, tmp1);
      String fullFolderPath = tmp1.toString();

      List<PSItemSummary> summaries = getFolderChildrenSummaries(request,
         fullFolderPath);

      Map<String, String> iconPathMap = getIconPathMap(summaries, request
            .getUserName());
      JSONArray result = new JSONArray();
      Set<Integer> allowedContentTypeIds = null;
      for (PSItemSummary summary : summaries)
      {
         if (allowedContentTypeIds == null)
         {
            allowedContentTypeIds = new HashSet<Integer>();
            Collection<IPSGuid> allowedContentTypeGuid = PSActionUtil.getAllowedContentIdsForSlot(slotId);
            for (IPSGuid id : allowedContentTypeGuid)
               allowedContentTypeIds.add(id.getUUID());
         }
         int t = summary.getContentTypeId();
         // Keep all folders without any filtering
         if (t != PSFolder.FOLDER_CONTENT_TYPE_ID)
         {
            // Not a folder
            // Does the slot allow this content type?
            if (!allowedContentTypeIds.contains(t))
               continue;
            // Apply content type filter
            if (ctypeid != -1 && t != ctypeid)
               continue;
         }
         result.put(summaryToJsonObject(summary,iconPathMap));
      }
      return result.toString();

   }

   /**
    * Helper method to get the map of content ids and icon paths for the
    * supplied summaries. The returned map may or may not contain all the
    * content ids of the supplied summaries, the icon path may be
    * <code>null</code> or empty. See
    * {@link PSItemDefManager#getContentTypeIconPaths(List)} for details on how
    * the icon paths are obtained.
    * 
    * @param summaries, item summaries for which icon paths are required.
    * @param user the current user, if same as check out user of the item then
    *           tip revision is used to detrmine the path otherwise current
    *           revision is used.
    * @return map of content ids and icon paths, may be empty but never
    *         <code>null</code>.
    */
   private static Map<String, String> getIconPathMap(
         List<PSItemSummary> summaries, String user)
   {
      Map<String, String> icmap = new HashMap<String, String>();
      if (summaries == null || summaries.size() < 1)
         return icmap;
      List<Integer> ids = new ArrayList<Integer>();
      for (PSItemSummary sum : summaries)
      {
         ids.add(sum.getGUID().getUUID());
      }
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      List<PSComponentSummary> csums = cms.loadComponentSummaries(ids);
      List<PSLocator> locs = new ArrayList<PSLocator>();
      for (PSComponentSummary csum : csums)
      {
         PSLocator loc = csum.getCurrentLocator();
         if (user.equalsIgnoreCase(csum.getCheckoutUserName()))
            loc = csum.getTipLocator();
         locs.add(loc);
      }
      PSItemDefManager defMgr = PSItemDefManager.getInstance();
      Map<PSLocator, String> temp = defMgr.getContentTypeIconPaths(locs);
      for (PSLocator locator : locs)
      {
         icmap.put("" + locator.getId(), temp.get(locator));
      }
      return icmap;
   }

   /**
    * Get the children of the supplied folder path filtered for the supplied
    * content type.
    * 
    * @param request request context to instantiate the server folder processor,
    * must not be <code>null</code>.
    * @param folderPath folder path, if starts with "//Sites" or "//Folders" it
    * is unmodified. Otherwise, it will be prefixed with "//Folders" to make it
    * an absolute path like "//Folders/<folderPath>". Must not be
    * <code>null</code> or empty.
    * @param contentTypeId content typeid to filter the items by,
    * <code>null</code> if no filtering is needed.
    * @return child folders and items of the supplied folder as JSON string that
    * resolves to a JSON array. Never <code>null</code> or empty. May return a
    * string that resolves to empty JSON array.
    * @throws PSErrorException
    * @throws JSONException
    */
   static public String getFolderChildren(IPSRequestContext request,
      String folderPath, String contentTypeId) throws PSErrorException,
      JSONException
   {
      folderPath = validateFolderPath(folderPath);

      int ctypeid = -1;
      if (!StringUtils.isEmpty(contentTypeId))
      {
         try
         {
            ctypeid = Integer.parseInt(contentTypeId);
         }
         catch (NumberFormatException e)
         {
            throw new IllegalArgumentException("content type id specified as '"
               + contentTypeId + "' is invalid");
         }
      }

      List<PSItemSummary> summaries = getFolderChildrenSummaries(request,
         folderPath);
      Map<String, String> iconPathMap = getIconPathMap(summaries, request
            .getUserName());
      
      JSONArray result = new JSONArray();
      for (PSItemSummary summary : summaries)
      {
         // Keep all folders and matching content types or all of type specified
         // is -1.
         int t = summary.getContentTypeId();
         if (t != PSFolder.FOLDER_CONTENT_TYPE_ID && ctypeid != -1
            && t != ctypeid)
            continue;
         result.put(summaryToJsonObject(summary,iconPathMap));
      }
      return result.toString();
   }

   /**
    * Helper method to validate and correct the folder path following the
    * following rules.
    * <p>
    * <ul>
    * <li>Throws {@link IllegalArgumentException} if th supplied path is
    * <code>null</code> or empty</li>
    * <li>if the supplied path starts with "//Sites" or "//Folders" it is
    * unmodified. Otherwise, it will be prefixed with "//Folders" to make it an
    * absolute path like "//Folders/<folderPath>"</li>
    * </ul>
    * 
    * @param folderPath folder path, Must not be <code>null</code> or empty.
    * 
    * @return path modified as above, never <code>null</code> or empty.
    */
   private static String validateFolderPath(String folderPath)
      throws IllegalArgumentException
   {
      if (folderPath == null || folderPath.length() == 0)
      {
         throw new IllegalArgumentException(
            "absSiteFolderPath must not be null or empty");
      }

      if (!folderPath.startsWith("//Sites")
         && !folderPath.startsWith("//Folders"))
      {
         if (folderPath.startsWith("/"))
            folderPath = folderPath.substring(1);
         folderPath = "//Folders/" + folderPath;
      }
      return folderPath;
   }

   /**
    * Create a folder with supplied name under the parent folder supplied (by
    * path). The security/permissions and other properties og the new folder
    * will be inherited from the parent folder.
    * 
    * @param request request context to instantiate the server folder processor,
    * must not be <code>null</code>.
    * @param parentSiteFolderPath parent folder path, must not be
    * <code>null</code> or empty and should meet the following:
    * <ul>
    * <li>Must start with '/'</li>
    * <li>The part between first '/' and the next '/' (or end) must be a site
    * name registered in the system</li>
    * </ul>
    * @param folderName name of the new folder to create, must not be
    * <code>null</code> or empty.
    * @return JSON string that resolves to a JSON object representing the newly
    * created folder, never <code>null</code> or empty.
    * @throws PSSiteManagerException
    * @throws PSErrorException
    * @throws JSONException
    */
   static public String createSiteFolder(IPSRequestContext request,
      String parentSiteFolderPath, String folderName)
           throws PSSiteManagerException, PSErrorException, JSONException, PSNotFoundException {
      if (request == null)
      {
         throw new IllegalArgumentException("request must not be null");
      }
      if (folderName == null || folderName.length() == 0)
      {
         throw new IllegalArgumentException(
            "folderName must not be null or empty");
      }
      StringBuffer tmp1 = new StringBuffer();
      computeAbsoluteFolderPath(parentSiteFolderPath, tmp1);
      String fullFolderPath = tmp1.toString();
      return createFolder(request, fullFolderPath, folderName);
   }

   /**
    * Create a folder with supplied name under the parent folder supplied (by
    * path). The security/permissions and other properties og the new folder
    * will be inherited from the parent folder.
    * 
    * @param request request context to get the user name, must not be
    * <code>null</code>.
    * @param parentFolderPath folder path, if starts with "//Sites" or
    * "//Folders" it is unmodified. Otherwise, it will be prefixed with
    * "//Folders" to make it an absolute path like "//Folders/<folderPath>".
    * Must not be <code>null</code> or empty.
    * @param folderName name of the new folder to create, must not be
    * <code>null</code> or empty.
    * @return JSON string that resolves to a JSON object representing the newly
    * created folder, never <code>null</code> or empty.
    * @throws PSErrorException
    * @throws JSONException
    */
   @SuppressWarnings("unchecked")
   static public String createFolder(IPSRequestContext request,
      String parentFolderPath, String folderName) throws PSErrorException,
      JSONException
   {
      parentFolderPath = validateFolderPath(parentFolderPath);
      IPSContentWs cws = PSContentWsLocator.getContentWebservice();
      PSFolder newFolder = cws.addFolder(folderName, parentFolderPath);
      JSONObject jsonObj = new JSONObject();
      jsonObj.put(COLUMN_ID, newFolder.getLocator().getId());
      jsonObj.put(COLUMN_NAME, getNameHtml(newFolder.getName()));
      jsonObj.put(COLUMN_DESCRIPTION, newFolder.getDescription());
      jsonObj.put(COLUMN_TYPE, ObjectTypeEnum.FOLDER.getOrdinal());
      jsonObj.put(COLUMN_ICON_PATH, "");
      return jsonObj.toString();
   }

   /**
    * Evaluate if a folder can be created as child folder to the supplied folder
    * id string.
    * 
    * @param request request context to load the folder permissions, must not be
    * <code>null</code>.
    * @param folderId contentid of the folder, must not be <code>null</code>
    * or empty.
    * @return <code>true</code> if user can create, <code>false</code>
    * otherwise.
    * @throws PSCmsException
    */
   static public boolean canCreateFolder(IPSRequestContext request,
      String folderId) throws PSCmsException
   {
      if (request == null)
      {
         throw new IllegalArgumentException("request must not be null");
      }
      if (StringUtils.isBlank(folderId))
         throw new IllegalArgumentException(
            "parentFolderId must not be null or empty");
      request.setParameter(IPSHtmlParameters.SYS_FOLDERID, folderId);
      return PSCms.canWriteToFolders(request);
   }

   /**
    * Evaluate if a an item of given content type can be created as child to the
    * supplied folder path.
    * 
    * @param request request context to load the folder permissions, must not be
    * <code>null</code>.
    * @param parentFolderId contentid of the parent folder, must not be
    * <code>null</code> or empty.
    * @param contentType content type id string, must correspond to a type known
    * to the system.
    * @return <code>true</code> if user can create, <code>false</code>
    * otherwise.
    * @throws PSCmsException
    */
   static public boolean canCreateItem(IPSRequestContext request,
      String parentFolderId, String contentType) throws PSCmsException
   {
      if (StringUtils.isBlank(parentFolderId))
         throw new IllegalArgumentException(
            "parentFolderId must not be null or empty");
      if (StringUtils.isBlank(contentType))
      {
         throw new IllegalArgumentException(
            "contentType must not be null or empty");
      }
      request.setParameter(IPSHtmlParameters.SYS_FOLDERID, parentFolderId);

      if (!canCreateFolder(request, parentFolderId))
         return false;
      // TODO we need to check additionally if one can create an item of given
      // content type. We do not have this today as the workflow can be dynamic
      // and hence we cann not know unless we open the form and submit. We need
      // to add this check when we support.
      return true;
   }

   /**
    * Helper method a site folder path to an absolute folder path.
    * 
    * @param absSiteFolderPath site folder path which is resolved as described
    * below:
    * <ul>
    * <li> Must start with '/'. The string between first '/' and the next '/' is
    * treated as the site name. If there is no second '/', everything after the
    * first '/' is treated as the site name. </li>
    * <li> Everything after the second '/' is assumed to be the folder path. If
    * second '/' is not present then the folder path is an empty string. </li>
    * <li>The absolute folder path is computed by appenidng the path in the
    * second step and the folder root of the site object.</li>
    * @param absFolderPath String buffer to store the parsed absolute folder
    * path, must not be <code>null</code> and must be empty. Emptied if not
    * empty.
    * @return {@link IPSSite site} object corresponding to the site name parsed,
    * never <code>null</code>.
    */
   static public IPSSite computeAbsoluteFolderPath(String absSiteFolderPath,
      StringBuffer absFolderPath) throws PSNotFoundException {
      if (absSiteFolderPath == null || absSiteFolderPath.length() == 0)
      {
         throw new IllegalArgumentException(
            "absSiteFolderPath must not be null or empty");
      }
      if (!absSiteFolderPath.startsWith("/"))
      {
         throw new IllegalArgumentException(
            "absSiteFolderPath must start with '/'");
      }
      if (absFolderPath.length() > 0)
         absFolderPath.delete(0, absFolderPath.length() - 1);
      String fullpath = absSiteFolderPath.substring(1);
      String siteName = "";
      String folderPath = "";
      int index = fullpath.indexOf('/');
      if (index == -1)
      {
         siteName = fullpath;
      }
      else
      {
         siteName = fullpath.substring(0, index);
         if (fullpath.length() > index)
            folderPath = fullpath.substring(index + 1);
      }
      if (StringUtils.isEmpty(siteName))
      {
         throw new IllegalArgumentException(
            "siteName part in the path must not be null or empty");
      }
      IPSSiteManager sm = PSSiteManagerLocator.getSiteManager();
      IPSSite site = sm.loadSite(siteName);
      String sfRoot = site.getFolderRoot();
      if (!sfRoot.endsWith("/"))
         sfRoot += "/";
      String fullFolderPath = sfRoot + folderPath;
      absFolderPath.append(fullFolderPath);
      return site;
   }

   /**
    * Get summaries of the children of supplied folder path.
    * 
    * @param request request context to instantiate the server folder processor,
    * must not be <code>null</code>.
    * @param absFolderPath absolute folder path must start with '//'.
    * @return summaries of the child folders and items of the supplied folder.
    * Never <code>null</code> may be empty.
    * @throws PSErrorException
    */
   static private List<PSItemSummary> getFolderChildrenSummaries(
      IPSRequestContext request, String absFolderPath) throws PSErrorException
   {
      if (absFolderPath == null || absFolderPath.length() == 0)
      {
         throw new IllegalArgumentException(
            "absSiteFolderPath must not be null or empty");
      }
      if (!absFolderPath.startsWith("//"))
      {
         throw new IllegalArgumentException(
            "absSiteFolderPath must start with '//'");
      }

      IPSContentWs cws = PSContentWsLocator.getContentWebservice();
      List<PSItemSummary> summaries = cws.findFolderChildren(absFolderPath,
         true);
      return filterSummaries(summaries);
   }
   
   /**
    * Filters Items Summaries that have no allowed operations.
    * @param items never null maybe empty.
    * @return never null maybe empty.
    */
   private static List<PSItemSummary> filterSummaries(List<PSItemSummary> items) {
      List<PSItemSummary> rvalue = new ArrayList<PSItemSummary>();
      for (PSItemSummary s : items) {
         Collection<OperationEnum> ops = s.getOperations();
         if (ops != null && ! ops.isEmpty() 
               && ops.iterator().next() != OperationEnum.NONE ) {
            rvalue.add(s);
         }
      }
      return rvalue;
   }

   /**
    * Gets the content type label from its ID.
    * @param contentTypeId the ID of the content type.
    * @return the label (or name if label is empty) of the content type, not blank.
    */
   static private String getContentTypeLabel(long contentTypeId)
   {
      try
      {
         return PSItemDefManager.getInstance().contentTypeIdToLabel(contentTypeId);
      }
      catch (PSInvalidContentTypeException e)
      {
         String msg = "Failed to get content type id = " + contentTypeId;
         ms_logger.error(msg, e);
         throw new RuntimeException(msg, e);
      }
   }


   /**
    * Generates HTML for name field.
    * 
    * @param name the name string to generate HTML for. Not <code>null</code>.
    */
   static public String getNameHtml(final String name)
   {
      assert name != null;
      // <a> is just a placeholder.
      // The client JavaScript will add necessary actions.
      return "<a href=\"#\">" + name + "</a>";
   }

   /**
    * Helper to convert the item summary object to a JSON object suitable to be
    * a row in the content browser table.
    * 
    * @param summary item summary object assumed not <code>null</code>.
    * @param iconMap map of content ids and icon paths, assumed not
    *           <code>null</code>.
    * @return corresponding JSON object, nevr <code>null</code>.
    * @throws JSONException
    */
   static JSONObject summaryToJsonObject(PSItemSummary summary,
         Map<String, String> iconMap) throws JSONException
   {
      JSONObject jsonObj = new JSONObject();
      jsonObj.put(COLUMN_ID, summary.getGUID().getUUID());
      jsonObj.put(COLUMN_NAME, getNameHtml(summary.getName()));
      jsonObj.put(COLUMN_DESCRIPTION, getContentTypeLabel(summary.getContentTypeId()));
      jsonObj.put(COLUMN_TYPE, summary.getObjectType().getOrdinal());
      String iconPath = StringUtils.defaultString(iconMap.get(""
            + summary.getGUID().getUUID()));
      jsonObj.put(COLUMN_ICON_PATH, iconPath);
      return jsonObj;
   }

   /**
    * Helper to get the id of the folder by path.
    * 
    * @param folderPath folder path in the form of "//Sites/foo/bar to return
    * the id of the folder "bar", must not be <code>null</code> or empty.
    * @return the contentid for the folder.
    * @throws PSErrorException
    */
   static public int getIdForPath(String folderPath) throws PSErrorException
   {
      if (StringUtils.isBlank(folderPath))
         throw new IllegalArgumentException(
            "folderPath must not be null or empty");
      
      IPSContentWs cws = PSContentWsLocator.getContentWebservice();
      List<IPSGuid> guids = cws.findPathIds(folderPath);
      return guids.get(guids.size() - 1).getUUID();
   }

   /**
    * Get the create item url for the given content type and parent site folder
    * path.
    * 
    * @param parentPath site folder path to craete the item under, must follow
    * the syntax expected by
    * {@link #computeAbsoluteFolderPath(String, StringBuffer)}.
    * @param ctypeid content typeid string, must not be <code>null</code> or
    * empty and must be a know content typeid.
    * @return New item url as registered with the system for the given content
    * type. All the required parameters are added including the parent folderid.
    * Never <code>null</code> or empty.
    * @throws PSSiteManagerException
    * @throws PSErrorException
    */
   public static String getNewItemUrlBySiteFolderPath(String parentPath,
      String ctypeid) throws PSSiteManagerException, PSErrorException, PSNotFoundException {
      StringBuffer fullPath = new StringBuffer();
      computeAbsoluteFolderPath(parentPath, fullPath);
      return getNewItemUrlByFolderPath(fullPath.toString(), ctypeid);
   }

   /**
    * Get the create item url for the given content type and parent folder path.
    * 
    * @param parentPath folder path, if starts with "//Sites" or
    * "//Folders" it is unmodified. Otherwise, it will be prefixed with
    * "//Folders" to make it an absolute path like "//Folders/<parentPath>".
    * Must not be <code>null</code> or empty.
    * @param ctypeid content typeid string, must not be <code>null</code> or
    * empty and must be a know content typeid.
    * @return New item url as registered with the system for the given content
    * type. All the required parameters are added including the parent folderid.
    * Never <code>null</code> or empty.
    * @throws PSErrorException
    */
   public static String getNewItemUrlByFolderPath(String parentPath,
      String ctypeid) throws PSErrorException
   {
      parentPath = validateFolderPath(parentPath);
      if (ctypeid == null || ctypeid.length() == 0)
      {
         throw new IllegalArgumentException("ctypeid must not be null or empty");
      }
      int folderid = getIdForPath(parentPath.toString());
      IPSGuid guid = new PSGuid(PSTypeEnum.NODEDEF, Long.parseLong(ctypeid));
      PSNodeDefinition def = PSContentTypeHelper.findNodeDef(guid);
      String urlString = def.getQueryRequest();
      Map<String, String> params = new HashMap<String, String>();
      params.put(IPSHtmlParameters.SYS_COMMAND, "edit");
      params.put(IPSHtmlParameters.SYS_VIEW, "sys_All");
      params.put(IPSHtmlParameters.SYS_FOLDERID, String.valueOf(folderid));
      String url = PSUrlUtils.createUrl(urlString,
         params.entrySet().iterator(), null);
      return url;
   }

   /**
    * Name of the first column which is hidden and is unique for each row. Will
    * be used by the client while sorting by columns.
    */
   public static final String COLUMN_ID = "Id";

   /**
    * Second column name.
    */
   public static final String COLUMN_NAME = "Name";

   /**
    * Third column name.
    */
   public static final String COLUMN_DESCRIPTION = "Description";

   /**
    * Data indicating type of id, where Item is 1, Folder is 2, Site is 9.
    */
   public static final String COLUMN_TYPE = "Type";

   /**
    * Path of the content type icon for an item.
    */
   public static final String COLUMN_ICON_PATH = "IconPath";


   /**
    * Constant to indicate a site record type in {@link #COLUMN_TYPE}.
    */
   public static final int SITE_TYPE = 9;
   
   private static Logger ms_logger = Logger.getLogger(PSContentBrowser.class);
}
