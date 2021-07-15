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
package com.percussion.pathmanagement.service.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.IPSPathService.PSPathNotFoundServiceException;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.IPSItemEntry;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.share.dao.PSDateUtils;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.data.PSPagedObjectList;
import com.percussion.share.service.IPSLinkableItem;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.CompareToBuilder;

import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.percussion.pathmanagement.service.impl.PSSitePathItemService.SITE_ROOT;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Utility class to provide various utility methods for path services.
 */
public class PSPathUtils
{
    private PSPathUtils()
    {
        
    }
    
    /**
     * Strip out the leading site root "//Sites" (if exist) from the specified folder path.
     * @param path the folder path that may contain the leading site root, not <code>null</code>.
     * @return the folder path without the leading site root, not <code>null</code>.
     */
    public static String stripSitesRoot(String path)
    {
        notNull(path);
        
        if (path.startsWith(SITE_ROOT))
            return path.substring(SITE_ROOT.length());

        return path;
    }
    
    /**
     * Given an internal folder path, this will return the corresponding path seen in the finder.  Also, paths starting
     * with '//' will be normalized to start with '/'.
     * 
     * @param path the folder path, may not be <code>null</code>.
     * 
     * @return the normalized finder path, never <code>null</code>, may be empty.
     */
    public static String getFinderPath(String path)
    {
        notNull(path);

        String finderPath = path;
        if (path.startsWith(PSAssetPathItemService.ASSET_ROOT))
        {
            // convert asset path
            finderPath = ASSETS_FINDER_ROOT + path.substring(PSAssetPathItemService.ASSET_ROOT.length());
        }
        else if (path.startsWith(PSSitePathItemService.SITE_ROOT))
        {
            // convert site path
            finderPath = SITES_FINDER_ROOT + path.substring(PSSitePathItemService.SITE_ROOT.length());
        }
        else
        {
            while (finderPath.startsWith("//"))
            {
                // convert '//' to '/'
                finderPath = finderPath.substring(1);
            }
        }

        return finderPath;
    }

    /**
     * Given a finder path, this will return the corresponding internal folder path.  Also, paths starting with '/' will
     * be normalized to start with '//'.
     * 
     * @param path the finder path, may not be <code>null</code>.
     * 
     * @return the normalized folder path, never <code>null</code>, may be empty.
     */
    public static String getFolderPath(String path)
    {
        notNull(path);

        String serverPath = path;
        if (path.startsWith(ASSETS_FINDER_ROOT))
        {
            // convert asset path
            serverPath = PSAssetPathItemService.ASSET_ROOT + path.substring(ASSETS_FINDER_ROOT.length());
        }
        else if (path.startsWith(SITES_FINDER_ROOT))
        {
            // convert site path
            serverPath = PSSitePathItemService.SITE_ROOT + path.substring(SITES_FINDER_ROOT.length());
        }
        else if (path.startsWith("/"))
        {
            int i;
            for (i = 0; i < path.length(); i++)
            {
                if (path.charAt(i) != '/')
                {                    
                    break;
                }
            }
            
            // convert '/' to '//'
            serverPath = "//" + path.substring(i);
        }

        return serverPath;
    }
    
    /**
     * Indicates when the given sortColumn and sortOrder variables has valid sorting
     * information. Sort is specified if and only if both sortColumn and sortOrder
     * are not blank.
     * 
     * @param sortColumn The sort column. May be <code>null</code> or empty.
     * @param sortOrder The sort order. May be <code>null</code> or empty.
     * @return <code>true</code> if sorting is specified. <code>false</code>
     * otherwise.
     */
    public static boolean isSortSpecified(String sortColumn, String sortOrder)
    {
        return (StringUtils.isNotBlank(sortColumn) && StringUtils.isNotBlank(sortOrder));
    }
    
    /**
     * Gets the page of {@link PSPathItem} objects which has a PSPathItem object with a name
     * equals to the specified child. maxResults is necessary here to correctly create the pages.
     *  
     * @param allPathItems A List of PSPathItem objects. Cannot be <code>null</code>.
     * @param maxResults The maximum amount of results. Cannot be <code>null</code> and
     * must be greater or equals to 1.
     * @param child The child name to look for, for example: page1. Cannot be <code>null</code>
     * nor empty.
     * @return An Object array with two components:
     * <ol>
     *  <li>If child is found in the given PSPathItem list, the sublist of objects (page)
     *  which contains the specified child is returned. If not found, then the first page
     *  is returned..</li>
     *  </li>The start index value.</li>
     * </ol>
     */
    public static PSPagedItemList getPage(List<PSPathItem> allPathItems, Integer maxResults, String child)
    {
        Validate.notNull(allPathItems, "allItems list cannot be null");
        Validate.notNull(maxResults, "maxResults cannot be null nor lesser than 1");
        Validate.isTrue(maxResults >= 1, "maxResults cannot be lesser than 1");
        Validate.notEmpty(child, "child cannot be null nor empty");
        
        Integer childIndex = getChildIndex(child, allPathItems);
        
        // If child wasn't found, we return the first page.
        if (childIndex == null)
        {
            PSPagedObjectList<PSPathItem> result = PSPagedObjectList.getPage(allPathItems, 1, maxResults);
            
            List<PSPathItem> itemsInFirstPage = result.getChildrenInPage();
            Integer startIndex = result.getStartIndex();
            
            return new PSPagedItemList(itemsInFirstPage, allPathItems.size(), startIndex);
        }
        
        Integer startIndex = childIndex - (childIndex % maxResults) + 1;
        
        PSPagedObjectList<PSPathItem> results = PSPagedObjectList.getPage(allPathItems, startIndex, maxResults);
        List<PSPathItem> itemsInPage = results.getChildrenInPage();
        startIndex = results.getStartIndex();
        
        return new PSPagedItemList(itemsInPage, allPathItems.size(), startIndex);
    }
    
    /**
     * Validates a path. It cannot be null nor empty, and it must start and end with a
     * slash "/".
     * 
     * @param path A path to validate.
     * @throws PSPathNotFoundServiceException In case the path is invalid.
     */
    public static void validatePath(String path) throws PSPathNotFoundServiceException
    {
        if (path == null || "".equals(path))
            throw new PSPathNotFoundServiceException("Path cannot be null or empty");
        if (!path.startsWith("/") || !path.endsWith("/"))
            throw new PSPathNotFoundServiceException("Path must start and end with a /");
    }
    
    /**
     * Find a given item using the finder path like
     * 'Assets/uploads/www.erau.edu/import/assets/images/ignite-sm.png',
     * supplied as parameter. This path is converted into a system folder path,
     * so system is able to perform the search. If already exists, then returns
     * a true value.
     * 
     * @param destinationPath the finder path of the downloaded image. Including
     *            file name. Cannot be <code>null</code>.
     * 
     * @return A boolean value to indicate if the item exists in the system,
     *         <code>true</code> if item exists.
     */
    public static boolean doesItemExist(String destinationPath)
    {
        int contentId = getIdByPath(destinationPath);
        
        return contentId < 0 ? false : true;
    }
    
    /**
     * Gets the content ID of the item with the specified folder path.
     * @param path the folder path of the item, not <code>null</code>.
     * @return the content ID. It may be <code>-1</code> if there is no item with the folder path.
     */
    public static int getIdByPath(String path)
    {
        Validate.notNull(path, "destinationPath cannot be null");
        
        String folderSystemPath = getFolderPath(path);
        
        // Get the asset by the url
        PSServerFolderProcessor fp = PSServerFolderProcessor.getInstance();

        try
        {
            return fp.getIdByPath(folderSystemPath);
        }
        catch (PSCmsException e)
        {
            return -1;
        }
    }
    
    /**
     * Checks if the given list has a {@link PSPathItem} with its name equals
     * to 'child'.
     *  
     * @param child The name of the PSPathItem that is being searched. Cannot be
     * <code>null</code>.
     * @param list The list where the search should be done. Cannot be
     * <code>null</code>, maybe empty.
     * @return 'true' if any PSPathItem with its name equals to 'child' is found.
     * 'false' otherwise.
     */
    private static Integer getChildIndex(String child, List<PSPathItem> list)
    {
        PSPathItem item;
        int index = 0;
        
        for (index=0; index<list.size(); index++)
        {
            item = list.get(index);
            
            if (StringUtils.equalsIgnoreCase(item.getName(), child))
                break;
        }
        
        return index < list.size() ? index : null;
    }
    
    /**
     * Sorts the given list of {@link PSPathItem} objects according to the given sortColumn and
     * sortOrder values.
     * 
     * @param pathItemList A List of PSPathItem object to sort. Cannot be <code>null</code>.
     * @param sortColumn The sort column name to sort by. For example: sys_title.
     * According to its value and {@link #isSortSpecified(String, String)}, sorting may not be
     * performed.
     * @param sortOrder The sort order ("asc" or "desc"). According to its value and
     * {@link #isSortSpecified(String, String)}, sorting may not be performed.
     */
    public static void sort(List<PSPathItem> pathItemList, final String sortColumn, String sortOrder)
    {
        Validate.notNull(pathItemList, "searchResults cannot be null");
        
        if (!PSPathUtils.isSortSpecified(sortColumn, sortOrder))
            return;
        
        final int sortOrderNumber = getSortOrderNumber(sortOrder);
        
        Collections.sort(pathItemList, new Comparator<PSPathItem>()
        {
            public int compare(PSPathItem o1, PSPathItem o2)
            {
                Object prop1, prop2;
                
                // Group folders (at the top or bottom). Makes sorting to behave as the Window explorer.
                // It's commented out for now, as Lorena doesn't want to behave like that.
//                String o1type = o1.getDisplayProperties().get(PSUiHelper.CONTENTTYPE_NAME);
//                String o2type = o2.getDisplayProperties().get(PSUiHelper.CONTENTTYPE_NAME);
//                
//                if (!StringUtils.equals(o1type, o2type))
//                {
//                    if (PSUiHelper.FOLDER_CONTENTTYPE.equals(o1type))
//                        return sortOrderNumber * -1;
//                    else if (PSUiHelper.FOLDER_CONTENTTYPE.equals(o2type))
//                        return sortOrderNumber * 1;
//                }
                
                String prop1str = StringUtils.EMPTY;
                String prop2str = StringUtils.EMPTY;
                
                if (o1.getDisplayProperties() != null)
                    prop1str = o1.getDisplayProperties().get(sortColumn);
                
                if (o2.getDisplayProperties() != null)
                    prop2str = o2.getDisplayProperties().get(sortColumn);
                
                prop1 = getRealDataType(prop1str);
                prop2 = getRealDataType(prop2str);
                
                int compareResult =
                        new CompareToBuilder()
                            .append(prop1, prop2)
                            .toComparison();
                
                return sortOrderNumber * compareResult;
            }

            private Object getRealDataType(String propValue)
            {
                try
                {
                    return PSDateUtils.getDateFromString(propValue);
                }
                catch (ParseException e)
                {
                }
                
                try
                {
                    return Integer.valueOf(propValue);
                }
                catch (NumberFormatException e)
                {
                    
                }
                
                if (propValue != null)
                    return propValue.toLowerCase();
                
                return propValue;
            }
        });
    }

    /***
     * Given a path, will correct the path so that it is pointed at the Site Folder instead of the Site Name
     * @return
     */
    public static String fixSiteFolderPath(IPSSiteDataService service, String path){
        Validate.notEmpty(path);

        String temp = getSiteFromPath(path);
        PSSiteSummary site = null;
        try {
             site = service.findByName(temp);
        }catch(Exception e){
            //ignore error if not found
        }

        if(site != null) {
            //if we found the site - fix the path to use the sites folder path instead of name.
            String actualPath = site.getFolderPath();
            actualPath = getSiteFromPath(actualPath);

            path = path.replaceFirst(site.getName(), actualPath);
        }

        return path;
    }
    /**
     * Assumes the folder path is in the format "//Sites/sitename[/otherpath]" or "/Sites/sitename[/otherpath]",
     * or "Sites/sitename[/otherpath]"
     * if it is then returns the sitename otherwise returns null.
     * @param folderPath assumed to be a proper folder path.
     * @return sitename or null, if the supplied folder path is of not a valid form.
     */
    public static String getSiteFromPath(String folderPath){
        if(StringUtils.isBlank(folderPath))
            return null;
        folderPath = folderPath.startsWith("//Sites/")?folderPath.replace("//Sites/",""):
            (folderPath.startsWith("/Sites/")?folderPath.replace("/Sites/",""):
            (folderPath.startsWith("Sites/")?folderPath.replace("Sites/",""):null));
        if(StringUtils.isBlank(folderPath))
            return null;
        folderPath = folderPath.indexOf("/")!=-1?folderPath.substring(0,folderPath.indexOf("/")):folderPath;
        return folderPath;
    }


	public static IPSLinkableItem getLinkableItem(String id) {
		notNull(id,"Id is null");
		IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
		IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
		PSItemDefManager mgr = PSItemDefManager.getInstance();
		IPSContentWs contentWs = PSContentWsLocator.getContentWebservice();
		IPSItemEntry item = objMgr.findItemEntry(guidMgr.makeGuid(id).getUUID());
		try {
			String typeName = mgr.contentTypeIdToName(item.getContentTypeId());
			String[] paths = contentWs.findFolderPaths(guidMgr.makeGuid(id));
			String newPath = null;
			if (paths!=null)
			{
				for (String path : paths)
				{
					//newPath =  getFinderPathIfExists(path);
					newPath = path;
					if (newPath!=null)
						break;
				}
			}
			if (newPath==null)
			{
				throw new IllegalArgumentException("Cannot find Finder path for id "+id);
			}
			
			return new PSLinkableItem(id, newPath, typeName);
				
		} catch (PSInvalidContentTypeException e) {
			throw new IllegalArgumentException("Cannot get type for id "+id);
		}
	
	}
	
    private static int getSortOrderNumber(String sortOrder)
    {
        if (StringUtils.equalsIgnoreCase(sortOrder, "desc"))
            return -1;
        
        return 1;
    }

    /**
     * Assumes the folder path is in the format "//Sites/sitename[/otherpath]" or "/Sites/sitename[/otherpath]",
     * or "Sites/sitename[/otherpath]"
     * if it is then returns the sitename otherwise returns null.
     * @param folderPath assumed to be a proper folder path.
     * @return sitename or null, if the supplied folder path is of not a valid form.
     */
    public static String getBaseFolderFromPath(String folderPath){
        if(StringUtils.isBlank(folderPath))
            return null;
        
        folderPath = folderPath.startsWith("//Sites/")?folderPath.replace("//Sites/",""):(folderPath.startsWith("/Sites/")?folderPath.replace("/Sites/",""):
            (folderPath.startsWith("Sites/")?folderPath.replace("Sites/",""):null));
        
        if(StringUtils.isBlank(folderPath))
            return null;
     
        
        folderPath = folderPath.indexOf("/")!=-1?folderPath.substring(folderPath.indexOf("/")+1,folderPath.lastIndexOf("/")):folderPath;
        
        return folderPath;
    }

    /**
     * Removes the trailing slash from a folder path if it exists
     * @param path
     * @return
     */
    public static String chopTrailingSlash(String path){

        while(path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    /***
     * Returns the folder name portion of the path
     * @param path
     * @return
     */
    public static String getFolderName(String path){
        path = chopTrailingSlash(path);
        return path.substring(path.lastIndexOf("/")+1);
    }

    /***
     * Given a specified path, returns only the foldername portion with no leading o trailing slash
     * @param path
     * @return
     */
    public static String stripFolderNameFromPath(String path){

        String folderName = getFolderName(path);
        return chopTrailingSlash(path.substring(0, path.lastIndexOf(folderName)));

    }
    /**
     * The root finder path for assets.
     */
    public static final String ASSETS_FINDER_ROOT = "/Assets";

    /**
     * The root finder path for sites.
     */
    public static final String SITES_FINDER_ROOT = "/Sites";
}
