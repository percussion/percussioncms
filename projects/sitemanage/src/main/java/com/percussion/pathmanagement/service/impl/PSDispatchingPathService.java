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
package com.percussion.pathmanagement.service.impl;

import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.pathmanagement.data.PSItemByWfStateRequest;
import com.percussion.pathmanagement.data.PSMoveFolderItem;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.data.PSRenameFolderItem;
import com.percussion.pathmanagement.service.IPSPathRecycleService;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.pathmanagement.service.impl.PSDispatchingPathService.IPSPathMatcher.PathMatch;
import com.percussion.recycle.service.IPSRecycleService;
import com.percussion.recycle.service.impl.PSRecycleService;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.impl.PSFolderHelper;
import com.percussion.share.data.IPSItemSummary.Category;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.data.PSPagedObjectList;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSBeanValidationUtils;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.ui.data.PSDisplayPropertiesCriteria;
import com.percussion.ui.data.PSSimpleDisplayFormat;
import com.percussion.ui.service.IPSListViewHelper;
import com.percussion.ui.service.IPSUiService;
import com.percussion.user.service.IPSUserService;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSStopwatch;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.text.MessageFormat.format;
import static java.util.Collections.unmodifiableCollection;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;


/**
 * The dispatching path service will dispatch to other {@link IPSPathService} implementations
 * based on a {@link IPSPathMatcher}.
 * <p>
 * By default the {@link IPSPathMatcher} implementation used is {@link PathMatcher} 
 * @author adamgent
 *
 */
public class PSDispatchingPathService implements IPSPathService, IPSPathRecycleService
{

    private IPSUserService userService;
    private IPSPathMatcher pathMatcher;
    protected PSDispatchingPathService pathService = this;
    private IPSPathService rootPathService;
    private IPSListViewHelper listViewHelper;
    private List<String> rolesAllowed = null;
    private IPSUiService uiService;
    private IPSRecycleService recycleService;
    private IPSFolderHelper folderHelper;

    public void setRegistry(Map<String, IPSPathService> pathRegistry)
    {
        this.pathMatcher = new PathMatcher(new PathNormalizer(), pathRegistry, this.uiService, this.listViewHelper);
        this.pathMatcher.registerPathService("/", rootPathService);
    }

    /**
     * Will use the given {@link IPSPathMatcher} to determine
     * delegation.
     * The root {@link IPSPathService}: {@link PSRootPathService} will 
     * be registered with path "/" for the given{@link IPSPathMatcher}
     */
    public PSDispatchingPathService(IPSUiService uiService, IPSUserService userService,
            IPSListViewHelper defaultListViewHelper, IPSRecycleService recycleService, IPSFolderHelper folderHelper)
    {
        super();
        this.userService = userService;
        this.uiService = uiService;
        this.listViewHelper = defaultListViewHelper;
        this.recycleService = recycleService;
        this.folderHelper = folderHelper;
        rootPathService = new PSRootPathService(defaultListViewHelper);
    }

    public List<String> getRolesAllowed()
    {
        return rolesAllowed;
    }

    public void setRolesAllowed(List<String> rolesAllowed)
    {
        this.rolesAllowed = rolesAllowed;
    }

    /**
     * Will create a path matcher from a path.
     * @param path never <code>null</code>.
     * @return a match, never <code>null</code>.
     * @throws PSPathNotFoundServiceException
     */
    protected PathMatch match(String path) throws PSPathNotFoundServiceException
    {
        if (path == null) throw new PSPathNotFoundServiceException("Path cannont be null");
        PathMatch pm = pathMatcher.matchPath(path);
        if (pm == null) throw new PSPathNotFoundServiceException("Path not found: " + path);
        return pm;
    }
    
    public IPSListViewHelper getListViewHelper()
    {
        return this.listViewHelper;
    }

    /**
     * {@inheritDoc}
     */
    public PSPathItem find(String path) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException {
        checkRolesAllowed();
        
        PathMatch pm = match(path);
        return find(pm);
    }
    
    /**
     * Verifies if the current user is allowed to reach this service. If it's not
     * allowed, a {@link PSPathServiceException} is thrown.
     * <p>
     * This method should be call before each public REST method.
     */
    private void checkRolesAllowed() throws PSPathServiceException {
        try {
            List<String> currentUserRoles = userService.getCurrentUser().getRoles();

            if (currentUserRoles != null && getRolesAllowed() != null &&
                    !CollectionUtils.containsAny(currentUserRoles, getRolesAllowed()))
                throw new PSPathServiceException("You are not authorized to access this path");
        } catch (PSDataServiceException e) {
            throw new PSPathServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public PSItemProperties findItemProperties(String path) throws PSPathServiceException, PSDataServiceException {
        checkRolesAllowed();
        
        PathMatch pm = match(path);
        return pm.findItemProperties();
    }
    

    protected PSPathItem find(PathMatch pm) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException {
        return pm.find();
    }

    /**
     * {@inheritDoc}
     */
    public List<PSPathItem> findChildren(String path) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException {
        checkRolesAllowed();
        
        PathMatch pm = match(path);
        return findChildren(pm);
    }
    
    protected List<PSPathItem> findChildren(PathMatch pm) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException {
        return pm.findChildren();
    }
    
    public List<PSPathItem> findChildren(String path, Integer displayFormatId, String sortColumn, String sortOrder) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException {
        PathMatch pm = match(path);
        return findChildren(pm, displayFormatId, sortColumn, sortOrder);
    }
    
    protected List<PSPathItem> findChildren(PathMatch pm, Integer displayFormatId, String sortColumn, String sortOrder) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException {
        return pm.findChildren(displayFormatId, sortColumn, sortOrder);
    }
    
    public PSPagedItemList findChildren(String path, Integer startIndex, Integer maxResults, Integer displayFormatId, String sortColumn, String sortOrder, String category, String type) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException {
        PathMatch pm = match(path);
        return findChildren(pm, startIndex, maxResults, displayFormatId, sortColumn, sortOrder, category, type);
    }
    
    protected PSPagedItemList findChildren(PathMatch pm, Integer startIndex, Integer maxResults, Integer displayFormatId, String sortColumn, String sortOrder, String category, String type) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException {
        return pm.findChildren(startIndex, maxResults, displayFormatId, sortColumn, sortOrder, category, type);
    }
    
    public PSPagedItemList findChildren(String path, Integer maxResults, String child, Integer displayFormatId) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException {
        PathMatch pm = match(path);
        return findChildren(pm, maxResults, child, displayFormatId);
    }
    
    protected PSPagedItemList findChildren(PathMatch pm, Integer maxResults, String child, Integer displayFormatId) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException {
        return pm.findChildren(maxResults, child, displayFormatId);
    }
    
    /**
     * {@inheritDoc}
     */
    public PSPathItem addFolder(String path) throws PSPathNotFoundServiceException, PSPathServiceException, PSValidationException, IPSDataService.DataServiceNotFoundException, IPSDataService.DataServiceLoadException {
        checkRolesAllowed();
        
        PathMatch pm = match(path);
        return addFolder(pm);
    }    

    protected PSPathItem addFolder(PathMatch pm) throws PSPathNotFoundServiceException, PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException, IPSDataService.DataServiceLoadException {
        return pm.addFolder();
    }
    
    /**
     * {@inheritDoc}
     */
    public PSPathItem addNewFolder(String path) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException {
        checkRolesAllowed();
        
        PathMatch pm = match(path);
        return addNewFolder(pm);
    }    

    protected PSPathItem addNewFolder(PathMatch pm) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException {
        return pm.addNewFolder();
    }
    
    /**
     * {@inheritDoc}
     */
    public PSPathItem renameFolder(PSRenameFolderItem item) throws PSPathNotFoundServiceException,
            PSPathServiceException, PSDataServiceException
    {
        checkRolesAllowed();
        
        validate(item);
        PathMatch pm = match(item.getPath());
        return renameFolder(pm, item.getName());
    }    

    public PSNoContent moveItem(PSMoveFolderItem request) throws PSPathServiceException, PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException {
        checkRolesAllowed();
        
        PathMatch pm = match(request.getTargetFolderPath());
        return pm.moveItem(request);
    }

    protected PSPathItem renameFolder(PathMatch pm, String name) throws PSPathNotFoundServiceException,
            PSPathServiceException, PSDataServiceException {
        return pm.renameFolder(name);
    }
    
    /**
     * {@inheritDoc}
     */
    public int deleteFolder(PSDeleteFolderCriteria criteria) throws PSPathServiceException, PSValidationException, IPSDataService.DataServiceNotFoundException, IPSDataService.DataServiceLoadException, PSNotFoundException {
        checkRolesAllowed();
        
        PathMatch pm = match(criteria.getPath());
        return deleteFolder(pm, criteria);
    }    

    protected int deleteFolder(PathMatch pm, PSDeleteFolderCriteria criteria) throws PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException, IPSDataService.DataServiceLoadException, PSNotFoundException {
        return pm.deleteFolder(criteria);
    }
    
    /**
     * {@inheritDoc}
     */
    public String validateFolderDelete(String path) throws PSPathNotFoundServiceException, PSPathServiceException, PSValidationException, IPSDataService.DataServiceNotFoundException, IPSItemWorkflowService.PSItemWorkflowServiceException, IPSDataService.DataServiceLoadException, PSNotFoundException {
        checkRolesAllowed();
        
        PathMatch pm = match(path);
        return validateFolderDelete(pm);
    }    

    protected String validateFolderDelete(PathMatch pm) throws PSPathNotFoundServiceException, PSPathServiceException, PSValidationException, IPSDataService.DataServiceNotFoundException, IPSItemWorkflowService.PSItemWorkflowServiceException, IPSDataService.DataServiceLoadException, PSNotFoundException {
        return pm.validateFolderDelete();
    }
    
    /**
     * {@inheritDoc}
     */
    public List findItemProperties(PSItemByWfStateRequest request)
            throws PSPathNotFoundServiceException, PSPathServiceException, PSValidationException, IPSDataService.DataServiceNotFoundException {
        checkRolesAllowed();
        
        PathMatch pm = match(request.getPath());
        return findItemProperties(pm, request.getWorkflow(), request.getState());
    }    

    protected List findItemProperties(PathMatch pm, String workflowName, String stateName)
            throws PSPathNotFoundServiceException, PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException {
        return pm.findItemProperties(workflowName, stateName);
    }
    
    /**
     * {@inheritDoc}
     */
    public String findLastExistingPath(String path) throws PSPathServiceException
    {
        checkRolesAllowed();
        
        PathMatch pm = match(path);
        return exists(pm);
    }    

    protected String exists(PathMatch pm) throws PSPathServiceException
    {
        return pm.findLastExistingPath();
    }
    
    /**
     * Returns the root {@link PSPathItem} for this {@link IPSPathService}.
     * @return never <code>null</code>.
     * @see PSRootPathService
     */
    protected PSPathItem findRoot() throws PSPathNotFoundServiceException {
        PSPathItem item = new PSPathItem();
        item.setPath("/");
        item.setName("root");
        return item;
    }
    
    /**
     * Returns the root {@link PSPathItem} children.
     * This would return all the registered top level paths
     * of the {@link IPSPathMatcher}. 
     * @return never <code>null</code>, maybe empty.
     */
    protected List<PSPathItem> findRootChildren() throws PSPathServiceException, PSDataServiceException {
        List<PSPathItem> items = new ArrayList<>();
        List<String> paths = new ArrayList<>( pathMatcher.getPaths());
        paths.remove("/");
        
        // Check the current user roles and return only the childs that are
        // visible for him.
        List<String> currentUserRoles = userService.getCurrentUser().getRoles();
        List<String> rolesAllowedByPathService;
        
        for(String p : paths) 
        {
            rolesAllowedByPathService = pathMatcher.getPathService(p).getRolesAllowed();
            
            if (currentUserRoles != null && rolesAllowedByPathService != null &&
                    !CollectionUtils.containsAny(currentUserRoles, rolesAllowedByPathService))
                continue;
            
            PSPathItem item = find(p);
            items.add(item);
        }
        
        return items;
    }
    
    public PSValidationErrors validate(PSRenameFolderItem object) throws PSPathServiceException, PSDataServiceException {
        PSBeanValidationException e = PSBeanValidationUtils.validate(object);
        e.throwIfInvalid();
        
        String path = object.getPath();
        PSPathItem pathItem;

        pathItem = find(path);

        if (pathItem.isLeaf())
        {
            throw new PSPathServiceException("Path: " + path + " is not a valid folder path");
        }
        
        return e.getValidationErrors();
    }
    
    public String validateEnteredPath(String path) throws PSPathServiceException {
        String exceptionMessage = "The path that you entered is invalid. Please check and re-enter the path.";
        String strReturn = path;
        try
        {
           if (path.toLowerCase().startsWith("sites"))
           {
              //Call the method to replace sites for Sites because other methods need this prefix
              path = replaceIgnoreCase(path, "sites", "Sites");
           }
           if (path.toLowerCase().startsWith("assets"))
           {
              //Call the method to replace assets for Assets because other methods need this prefix
              path = replaceIgnoreCase(path, "assets", "Assets");
           }
           if (path.toLowerCase().startsWith("design"))
           {
              //Call the method to replace assets for Assets because other methods need this prefix
              path = replaceIgnoreCase(path, "design", "Design");
           }
           PathMatch pm = match(path);
           PSPathItem pathItem = find(pm);
           if(pathItem != null)
           {
               strReturn = "";
               for(String element : pathItem.getFolderPaths()){
                   strReturn = strReturn + element;
               }
               if (strReturn.startsWith("//Folders/$System$"))
               {
                   strReturn = strReturn.substring(18);
               }
               String pathType = pathItem.getType();
               if (pathType != null && pathType != "site")
               {
                   strReturn = strReturn + "/" + pathItem.getName();
               }
               else if(strReturn == "" &&
                       (pathItem.getName().equalsIgnoreCase("assets") ||
                               pathItem.getName().equalsIgnoreCase("design") ||
                               pathItem.getName().equalsIgnoreCase("sites")))
               {
                   strReturn = pathItem.getPath();
               }    
           }
           if (strReturn.startsWith("//")){
               strReturn = strReturn.substring(1);
           }
        }
        catch(Exception ex)
        {
           //Any exception will be treated as an error with the validation process
           throw new PSPathServiceException(exceptionMessage,ex);
        }
        return strReturn;
   }
    
    /**
     * Replaces all instances of oldString with newString in a given string with the
     * added feature that matches of newString in oldString ignore case.
     *
     * @param source      the String to search to perform replacements on
     * @param oldString the String that should be replaced by newString
     * @param newString the String that will replace all instances of oldString
     * @return a String will all instances of oldString replaced by newString
     */
    private static String replaceIgnoreCase(String source, String oldString, String newString) {
      if (source == null) 
      {
         return null;
      }
      String lcEntryLine = source.toLowerCase();
      String lcOldString = oldString.toLowerCase();
      int i = 0;
      if ((i = lcEntryLine.indexOf(lcOldString, i)) >= 0) 
      {
         char[] tempLine = source.toCharArray();
         char[] newString2 = newString.toCharArray();
         int oLength = oldString.length();
         StringBuilder buf = new StringBuilder(tempLine.length);
         buf.append(tempLine, 0, i).append(newString2);
         i += oLength;
         int j = i;
         while ((i = lcEntryLine.indexOf(lcOldString, i)) > 0) {
            buf.append(tempLine, j, i - j).append(newString2);
            i += oLength;
            j = i;
         }
         buf.append(tempLine, j, tempLine.length - j);
         return buf.toString();
      }
      return source;
    }

    @Override
    public PSNoContent restoreFolder(String guid) throws PSPathServiceException {
        boolean hasErrors = false;
        boolean isValidForRecycle = false;
        try {
            PSPathItem item = folderHelper.findItemById(guid, RECYCLED_TYPE);
            String folderPath = item.getFolderPaths().get(0);
            String pathToCheck = PSFolderHelper.getOppositePath(folderPath);
            isValidForRecycle = folderHelper.isFolderValidForRecycleOrRestore(pathToCheck, folderPath, FOLDER_TYPE, RECYCLED_TYPE);
        } catch (Exception e) {
            log.error("Error finding item properties by id when restoring folder: " + guid);
            hasErrors = true;
        }
        if (hasErrors) {
            throw new PSPathServiceException("Error restoring folder. See log for details.");
        }
        if (isValidForRecycle) {
            recycleService.restoreFolder(guid);
        } else {
            throw new PSPathServiceException("Error restoring folder.  Destination may already " +
                    "contain a folder" + "with the same name.");
        }
        return new PSNoContent("Successfully restored folder with guid: " + guid);
    }

    /**
     * An adapter that is used to register the root path of the {@link PSDispatchingPathService}.
     * @author adamgent
     *
     */
    public class PSRootPathService implements IPSPathService
    {

        private IPSListViewHelper defaultListViewHelper;

        public PSRootPathService(IPSListViewHelper defaultListViewHelper)
        {
            this.defaultListViewHelper = defaultListViewHelper;
        }
        
        public PSPathItem find(String path) throws PSPathNotFoundServiceException, PSPathServiceException
        {
            validatePath(path); 
            return pathService.findRoot();
        }

        public List<String> getRolesAllowed()
        {
            return null;
        }
        
        public PSItemProperties findItemProperties(String path) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException {
            validatePath(path); 
            return pathService.findItemProperties(path);
        }
        
        public List<PSPathItem> findChildren(String path) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException {
            validatePath(path);
            return pathService.findRootChildren();
        }
        
        public PSPathItem addFolder(String path) throws PSPathNotFoundServiceException, PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException, IPSDataService.DataServiceLoadException {
            return pathService.addFolder(path);
        }
        
        public PSPathItem addNewFolder(String path) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException {
            return pathService.addNewFolder(path);
        }
        
        public PSPathItem renameFolder(PSRenameFolderItem item) throws PSPathNotFoundServiceException,
                PSPathServiceException, PSDataServiceException
        {
            return pathService.renameFolder(item);
        }
        
        public PSNoContent moveItem(PSMoveFolderItem request) throws PSPathServiceException, PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException {
            return pathService.moveItem(request);
        }

        public int deleteFolder(PSDeleteFolderCriteria criteria) throws PSPathNotFoundServiceException,
                PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException, IPSDataService.DataServiceLoadException, PSNotFoundException {
            return pathService.deleteFolder(criteria);
        }
        
        public String validateFolderDelete(String path) throws PSPathNotFoundServiceException,
                PSPathServiceException, PSValidationException, IPSDataService.DataServiceNotFoundException, IPSItemWorkflowService.PSItemWorkflowServiceException, IPSDataService.DataServiceLoadException, PSNotFoundException {
            return pathService.validateFolderDelete(path);
        }
        
        public List findItemProperties(PSItemByWfStateRequest request)
                throws PSPathNotFoundServiceException, PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException {
            return pathService.findItemProperties(request);
        }
        
        public String findLastExistingPath(String path) throws PSPathServiceException {
            return pathService.findLastExistingPath(path);
        }

        private void validatePath(String path) throws PSPathNotFoundServiceException {
            if ( ! "/".equals(path) ) throw new PSPathNotFoundServiceException("Path not found: " + path);
        }
        
        public IPSListViewHelper getListViewHelper()
        {
            return defaultListViewHelper;
        }
    }



    /**
     * Normalizes a path as some paths can have
     * the trailing slash and other may not.
     * What is an acceptable path may vary.
     * @author adamgent
     *
     */
    public interface IPSPathNormalizer {

        /**
         * Converts the path to a canonical form.
         * @param path never <code>null</code>.
         * @return never <code>null</code>.
         * @throws IllegalArgumentException If the path is null.
         */
        public String normalizePath(String path) throws IllegalArgumentException;
    }
    
    /**
     * The path matcher creates {@link PathMatch}s
     * based on paths.
     * 
     * @author adamgent
     *
     */
    public interface IPSPathMatcher {
        
        /**
         * Matchs a path to a {@link IPSPathService}.
         * If a path matches a path that has been registered: {@link #registerPathService(String, IPSPathService)}
         * a {@link PathMatch} is returned. 
         * @param path never <code>null</code>.
         * @return never <code>null</code>.
         * @throws PSPathNotFoundServiceException when a path cannot be matched.
         */
        public PathMatch matchPath(String path) throws PSPathNotFoundServiceException;
        
        IPSPathService getPathService(String prefix);
        
        /**
         * The registered paths that the {@link IPSPathMatcher} is matching on.
         * @return never <code>null</code>, maybe empty.
         */
        public Collection<String> getPaths();
        
        /**
         * Registers a path to match on.
         * @param path never <code>null</code>.
         * @param pathService never <code>null</code>.
         */
        public void registerPathService(String path, IPSPathService pathService);
        
        /**
         * Represents a match of a path to the {@link IPSPathService} that should handle it.
         * Wraps the {@link IPSPathService} so that paths passed into the service are the 
         * {@link #relativePath}s that the matched {@link IPSPathService} implementation understands.
         * Consequently the returned {@link PSPathItem}s property {@link PSPathItem#setPath(String)} is updated
         * using the {@link #fullPath}.
         * @author adamgent
         *
         */
        public static class PathMatch
        {
            public String pathPrefix;
            public String relativePath;
            public String fullPath;
            public IPSPathService pathService;
            public IPSUiService uiService;
            private PSStopwatch stopWatch;
            private IPSListViewHelper listViewHelper;
            
            /**
             * Creates a match.
             * @param pathPrefix never <code>null</code>.
             * @param relativePath never <code>null</code>.
             * @param fullPath never <code>null</code>.
             * @param pathService never <code>null</code>.
             */
            public PathMatch(String pathPrefix, String relativePath, String fullPath,
                    IPSPathService pathService, IPSUiService uiService,
                    IPSListViewHelper listViewHelper)
            {
                super();
                
                this.stopWatch = new PSStopwatch();
                
                this.pathPrefix = pathPrefix;
                this.relativePath = relativePath;
                this.fullPath = fullPath;
                this.pathService = pathService;
                this.uiService = uiService;
                this.listViewHelper = listViewHelper;
            }
            /**
             * Convert the relative path back to the full path.
             * @param relativePath never <code>null</code>.
             * @return never <code>null</code>.
             */
            public String toFullPath(String relativePath) {
                notNull(relativePath,"relative path cannot be null");
                relativePath = StringUtils.removeStart(relativePath, "/").trim();
                return pathPrefix + relativePath;
            }
            
            /**
             * Gets the display format object ({@link PSSimpleDisplayFormat} according
             * to the given display format id. It returns the default value if the
             * display format id passed in is null.
             * 
             * @param displayFormatId
             * @return The {@link PSSimpleDisplayFormat} object according to the given
             * display format id.
             */
            private PSSimpleDisplayFormat getDisplayFormat(Integer displayFormatId)
            {
                if (displayFormatId == null)
                    return null;
                
                return uiService.getDisplayFormat(displayFormatId.intValue());
            }
            
            /**
             * Will call the matching {@link IPSPathService} {@link IPSPathService#findChildren(String)}.
             * The {@link #relativePath} will be passed to the matched {@link IPSPathService}.
             * The returned {@link PSPathItem}s will have their paths corrected to the full path
             * ({@link #toFullPath(String)}). 
             * @return never <code>null</code>.
             */
            public List<PSPathItem> findChildren() throws PSPathServiceException, PSDataServiceException {
                return findChildren(1, Integer.MAX_VALUE, null, null, null, null, null).getChildrenInPage();
            }
            
            /**
             * It provides the same functionality as {@link #findChildren()}, but allows to
             * include display properties information in the returned {@link PSPathItem} list.
             * 
             * @param displayFormatId Display format to look for and fulfill {@link PSPathItem}
             * objects. It may be <code>null</code>, in that case it works in the same way as
             * {@link #findChildren()}.
             * @return A list of {@link PSPathItem} object with display properties (if
             * 'displayFormatId' is not null and exists).
             */
            public List<PSPathItem> findChildren(Integer displayFormatId, String sortColumn, String sortOrder) throws PSPathServiceException, PSDataServiceException {
                List<PSPathItem> items = findChildren();
                
                PSSimpleDisplayFormat format = getDisplayFormat(displayFormatId);
                listViewHelper.fillDisplayProperties(new PSDisplayPropertiesCriteria(items, format));
                
                PSPathUtils.sort(items, sortColumn, sortOrder);
                
                return items;
            }
            
            /**
             * Apart from providing the same functionality than {@link #findChildren()}, it support
             * pagination.
             *  
             * @param startIndex The starting index. Cannot be <code>null</code> nor lesser than 1.
             * @param maxResults The maximum amount of results. It may be <code>null</code>, but if it's
             * not, then it cannot be lesser than 1.
             * @return An @{link PSPagedItemList} with information about a {@link PSPathItem}, like
             * a list of children for a specified page of data and the total count of items that it has.
             * Never <code>null</code>.
             * @see #findChildren()
             */
            public PSPagedItemList findChildren(Integer startIndex, Integer maxResults, Integer displayFormatId,
                    String sortColumn, String sortOrder, String category, String type) throws PSPathServiceException, PSDataServiceException {
                stopWatch.start();
                List<PSPathItem> items = pathService.findChildren(relativePath);
                stopWatch.stop();
                log.debug("pathService.findChildren: " + stopWatch.toString());
                
                items = filterByCategoryAndType(items, category, type);
                
                if (items.size() == 0)
                    return new PSPagedItemList(items, items.size(), 1);
                
                stopWatch.start();
                validateAndSetFullPaths(items);
                stopWatch.stop();
                log.debug("validateAndSetFullPaths: " + stopWatch.toString());
                
                boolean isSortSpecified = PSPathUtils.isSortSpecified(sortColumn, sortOrder);
                
                stopWatch.start();
                PSSimpleDisplayFormat format = getDisplayFormat(displayFormatId);
                stopWatch.stop();
                log.debug("getDisplayFormat: " + stopWatch.toString());
                
                if (isSortSpecified)
                {
                    stopWatch.start();
                    listViewHelper.fillDisplayProperties(new PSDisplayPropertiesCriteria(items, format));
                    stopWatch.stop();
                    log.debug("PSUiHelper.setDisplayFormatInfo (before sorting): " + stopWatch.toString());
                    
                    stopWatch.start();
                    PSPathUtils.sort(items, sortColumn, sortOrder);
                    stopWatch.stop();
                    log.debug("PSPathUtils.sort: " + stopWatch.toString());
                }
                
                stopWatch.start();
                PSPagedObjectList<PSPathItem> result = PSPagedObjectList.getPage(items, startIndex, maxResults);
                List<PSPathItem> itemsInPage = result.getChildrenInPage();
                Integer resultingStartIndex = result.getStartIndex();
                stopWatch.stop();
                log.debug("PSPathUtils.getPage: " + stopWatch.toString());
                
                // If sort wasn't specified, I need to update items with display properties here
                if (!isSortSpecified)
                {
                    stopWatch.start();
                    listViewHelper.fillDisplayProperties(new PSDisplayPropertiesCriteria(itemsInPage, format));
                    stopWatch.stop();
                    log.debug("PSUiHelper.setDisplayFormatInfo (before returning): " + stopWatch.toString());
                }
                
                return new PSPagedItemList(itemsInPage, items.size(), resultingStartIndex);
            }
            
            /**
             * Filters the supplied items by category and type.
             * 
             * @param items The list to filter, is not modified by this method.
             * @param category The category to use, may be <code>null</code> or empty to ignore.
             * @param type The type to use, may be <code>null</code> or empty to ignore.
             * 
             * @return The filtered list, if neither category or type are specified, the original list of items supplied
             * is returned.
             */
            private List<PSPathItem> filterByCategoryAndType(List<PSPathItem> items, String category, String type)
            {
                
                Set<String> categories = new HashSet<>();
                Set<String> types = new HashSet<>();
                
                boolean hasCat = !StringUtils.isBlank(category);
                boolean hasType = !StringUtils.isBlank(type);

                
                if (!hasCat && !hasType)
                    return items;
                
                if (hasCat)
                {
                    categories.addAll(Arrays.asList(category.split(",")));
                }
                
                if (hasType)
                {
                    types.addAll(Arrays.asList(type.split(",")));
                }

                
                List<PSPathItem> result = new ArrayList<>();
                for (PSPathItem item : items)
                {
                    Category itemCat = item.getCategory();
                    if (Category.SYSTEM.equals(itemCat))
                        itemCat = Category.FOLDER;
                    
                    if (hasCat && (itemCat == null || !categories.contains(itemCat.name())))
                        continue;
                    
                    if (hasType && !types.contains(item.getType()))
                        continue;
                    
                    result.add(item);
                }
                
                return result;
            }
            /**
             * Apart from providing the same functionality than {@link #findChildren()}, it allows
             * to return the page where the specified children resides in.
             * 
             * @param maxResults The maximum amount of results. It's used to calculate pages.
             * It cannot be <code>null</code>, nor lesser than 1.
             * @param child The child name to look for, for example: page1. It cannot be
             * <code>null</code> nor empty.
             * @return An @{link PSPagedItemList} with information about a {@link PSPathItem}, like
             * a list of children for a specified page of data and the total count of items that it has.
             * The child item will be present, if found, in the children list. If not found, the first
             * page is returned. Never <code>null</code>.
             * @see #findChildren()
             */
            public PSPagedItemList findChildren(Integer maxResults, String child, Integer displayFormatId) throws PSPathServiceException, PSDataServiceException {
                Validate.notNull(maxResults, "maxResults cannot be null nor lesser than 1");
                Validate.isTrue(maxResults >= 1, "maxResults cannot be null nor lesser than 1");
                Validate.notEmpty(child, "child cannot be null nor empty");
                
                List<PSPathItem> items = findChildren();
                
                if (items.size() == 0)
                    return new PSPagedItemList(items, items.size(), 1);
                
                PSPagedItemList result = PSPathUtils.getPage(items, maxResults, child);
                List<PSPathItem> itemsInPage = result.getChildrenInPage();
                Integer realStartIndex = result.getStartIndex();
                
                PSSimpleDisplayFormat format = getDisplayFormat(displayFormatId);
                listViewHelper.fillDisplayProperties(new PSDisplayPropertiesCriteria(itemsInPage, format));
                
                return new PSPagedItemList(itemsInPage, items.size(), realStartIndex);
            }
            
            /**
             * Validates and set the full path of each {@link PSPathItem} given in the
             * list.
             * @param items A list of {@link PSPathItem} objects. May be <code>null</code>,
             * in that case id does nothing.
             */
            private void validateAndSetFullPaths(List<PSPathItem> items) throws PSPathServiceException {
                if (items == null || items.size() == 0)
                    return;
                
                for(PSPathItem item : items) {
                    validateReturnedPathItem(item);
                    item.setPath(toFullPath(item.getPath()));
                }
            }
            
            /**
             * See {@link IPSPathService#findItemProperties(String)}.
             * 
             * @return item properties, never <code>null</code>.
             */
            public PSItemProperties findItemProperties() throws PSDataServiceException, PSPathServiceException {
                return pathService.findItemProperties(relativePath);
            }
            
            /**
             * See {@link IPSPathService#findItemProperties(PSItemByWfStateRequest)}.
             * 
             * @param workflowName the workflow of the items, never <code>null</code> or empty.
             * @param stateName the workflow state of the items, may be <code>null<code>.
             * 
             * @return list of item properties, never <code>null</code>, may be empty.
             */
            public List<PSItemProperties> findItemProperties(String workflowName, String stateName) throws PSPathServiceException, PSValidationException, IPSDataService.DataServiceNotFoundException {
                PSItemByWfStateRequest request = new PSItemByWfStateRequest();
                request.setPath(relativePath);
                request.setWorkflow(workflowName);
                request.setState(stateName);
                
                return pathService.findItemProperties(request);
            }
            
            /**
             * See {@link #findChildren()}
             * 
             * @return never <code>null</code>.
             */
            public PSPathItem find() throws PSPathServiceException, PSDataServiceException {
                PSPathItem item = pathService.find(relativePath);
                validateReturnedPathItem(item);
                item.setPath(toFullPath(item.getPath()));
                return item;
            }
            
            /**
             * See {@link #findChildren()}.  Instead of finding children, a folder will be created.
             * 
             * @return never <code>null</code>.
             */
            public PSPathItem addFolder() throws PSPathServiceException, PSValidationException, IPSDataService.DataServiceNotFoundException, IPSDataService.DataServiceLoadException {
                PSPathItem item = pathService.addFolder(relativePath);
                validateReturnedPathItem(item);
                item.setPath(toFullPath(item.getPath()));
                return item;
            }
            
            /**
             * See {@link #findChildren()}.  Instead of finding children, a folder will be created.
             * 
             * @return never <code>null</code>.
             */
            public PSPathItem addNewFolder() throws PSPathServiceException, PSDataServiceException {
                PSPathItem item = pathService.addNewFolder(relativePath);
                if (!find().isLeaf())
                {
                    // only validate relative path if item is not a leaf
                    validateReturnedPathItem(item, false);
                }
                item.setPath(toFullPath(item.getPath()));
                return item;
            }
            
            /**
             * See {@link #findChildren()}.  Instead of finding children, the folder will be renamed.
             * 
             * @param name the new name for the folder, may not be <code>null</code> or empty.
             * 
             * @return never <code>null</code>.
             */
            public PSPathItem renameFolder(String name) throws PSPathServiceException, PSDataServiceException {
                notEmpty(name, "name may not be null or empty");
                
                PSRenameFolderItem folderItem = new PSRenameFolderItem();
                folderItem.setPath(relativePath);
                folderItem.setName(name);
                PSPathItem item = pathService.renameFolder(folderItem);
                validateReturnedPathItem(item, false);
                item.setPath(toFullPath(item.getPath()));
                return item;
            }
            
            public PSNoContent moveItem(PSMoveFolderItem request) throws PSDataServiceException, PSPathServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException {
                return pathService.moveItem(request);
            }
            
            /**
             * See {@link #findChildren()}.  Deletes the folder.
             * @param criteria
             * @return number of undeleted items.
             */
            public int deleteFolder(PSDeleteFolderCriteria criteria) throws PSPathServiceException, PSValidationException, IPSDataService.DataServiceNotFoundException, IPSDataService.DataServiceLoadException, PSNotFoundException {
                PSDeleteFolderCriteria folderCriteria = new PSDeleteFolderCriteria();
                folderCriteria.setPath(relativePath);
                folderCriteria.setSkipItems(criteria.getSkipItems());
                folderCriteria.setShouldPurge(criteria.getShouldPurge());
                return pathService.deleteFolder(folderCriteria);
            }
            
            /**
             * See {@link #findChildren()}.  Validates the folder for deletion.
             * @return never <code>null</code> or empty.
             */
            public String validateFolderDelete() throws PSPathServiceException, PSValidationException, IPSDataService.DataServiceNotFoundException, IPSItemWorkflowService.PSItemWorkflowServiceException, IPSDataService.DataServiceLoadException, PSNotFoundException {
                return pathService.validateFolderDelete(relativePath);
            }
            
            /**
             * See {@link IPSPathService#findLastExistingPath(String)}.
             * @return never <code>null</code>, may be empty.
             */
            public String findLastExistingPath() throws PSPathServiceException {
                return pathService.findLastExistingPath(relativePath);
            }
            
            protected void validateReturnedPathItem(PSPathItem item) throws PSPathServiceException {
                validateReturnedPathItem(item, true);
            }
            
            protected void validateReturnedPathItem(PSPathItem item, boolean checkRelative)
            throws PSPathServiceException
            {
                if (item.getPath() == null) 
                    throw new PSPathServiceException(
                            format("Path Service: {0} did not set the " 
                                    + PSPathItem.class.getSimpleName() + 
                                    "#path property for path: {1}. PathItem: {2}", 
                                    pathService.getClass(), fullPath, item));
                if (checkRelative && !StringUtils.startsWithIgnoreCase(item.getPath(), relativePath))
                    throw new PSPathServiceException(
                            format("Path Service: {0} did not return a " 
                                    + PSPathItem.class.getSimpleName() +
                                    "#path property that begins with relative path: {1}. " +
                                    "Path returned is {2}", pathService.getClass(), relativePath, item.getPath()));
            }
            
        }
    
    }
    

    public static class PathNormalizer implements IPSPathNormalizer {

        /**
         * {@inheritDoc}
         */
        public String normalizePath(String path)
        {
            notNull(path, "Path cannot be null");
            String rvalue = path.trim();
            if ( ! StringUtils.endsWith(rvalue, "/") ) {
                rvalue = rvalue + "/";
            }
            
            if ( ! StringUtils.startsWith(rvalue, "/") ) {
                rvalue = "/" + rvalue;
            }

            if( StringUtils.startsWith(rvalue, "//")){
                rvalue = rvalue.substring(1);
            }
            if (log.isDebugEnabled()) {
                log.debug(format("Original path: {0}, Normalized path: {1}", path,rvalue));
            }
            return rvalue;
            
        }
    }
    
    public static class PathMatcher implements IPSPathMatcher
    {
        private static final Object DESIGN_BASE_PATH = "/Design/";
        private Map<String, IPSPathService> pathRegistry;
        private IPSPathNormalizer pathNormalizer;
        private IPSUiService uiService;
        private IPSListViewHelper defaultListViewHelper;

        public PathMatcher(IPSPathNormalizer pathNormalizer, Map<String, IPSPathService> pathRegistry,
                IPSUiService uiService, IPSListViewHelper defaultListViewHelper)
        {
            super();
            this.pathNormalizer = pathNormalizer;
            this.pathRegistry = pathRegistry;
            this.uiService = uiService;
            this.defaultListViewHelper = defaultListViewHelper;
        }
        
        public IPSPathService getPathService(String prefix)
        {
            return pathRegistry.get(prefix);
        }

        /**
         * {@inheritDoc}
         */
        public PathMatch matchPath(String path) throws PSPathNotFoundServiceException {
            path = pathNormalizer.normalizePath(path);
            
            Set<String> pathPrefixSet = pathRegistry.keySet();
            IPSPathService pathService;
            IPSListViewHelper listViewHelper;
            
            List<String> pathPrefixs = new ArrayList<>(pathPrefixSet);
            Collections.sort(pathPrefixs);
            Collections.reverse(pathPrefixs);
            
            for (String pre : pathPrefixs)
            {
                if (StringUtils.startsWith(path, pre))
                {
                    /*
                     * Decode the path if under '/Design' (just in case of
                     * 'Web%20Resources'). This was decoded by methods: 
                     * - PercFolderHelper().getAccessLevelByPath 
                     * - perc_pathmanager.open_path
                     */
                    if(DESIGN_BASE_PATH.equals(pre))
                    {
                        path = getDecodedPath(path);
                    }
                    
                    String relativePath = "/" + StringUtils.substringAfter(path, pre).trim();
                    pathService = pathRegistry.get(pre);
                    
                    listViewHelper = pathService.getListViewHelper();
                    if (listViewHelper == null)
                        listViewHelper = defaultListViewHelper;
                    
                    return new PathMatch(pre, relativePath, path, pathService, uiService, listViewHelper);
                }
            }
            
            throw new PSPathNotFoundServiceException("Path not found: " + path);
        }

        /**
         * {@inheritDoc}
         */
        public Collection<String> getPaths()
        {
            return unmodifiableCollection(this.pathRegistry.keySet());
        }

        /**
         * 
         * {@inheritDoc}
         */
        public void registerPathService(String path, IPSPathService pathService)
        {
            notNull(path);
            notNull(pathService);
            this.pathRegistry.put(path, pathService);
        }

        /**
         * Calls {@link URLDecoder#decode(String, String)} for the given path, using
         * the encoding {@link java.nio.charset.StandardCharsets}. If that encoding is not
         * supported (cannot happen), it calls {@link URLDecoder#decode(String)}
         * (that is deprecated).
         * 
         * @param path the encoded Path. Assumed not blank.
         * @return a {@link String}. Never <code>null</code>
         */
        private String getDecodedPath(String path)
        {
            try
            {
                return URLDecoder.decode(path, PSCharSets.rxJavaEnc());
            }
            catch (UnsupportedEncodingException e1)
            {
                return URLDecoder.decode(path);
            }
        }

    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSDispatchingPathService.class);

    private static final String RECYCLED_TYPE = PSRelationshipConfig.TYPE_RECYCLED_CONTENT;

    private static final String FOLDER_TYPE = PSRelationshipConfig.TYPE_FOLDER_CONTENT;

    private static final String RECYCLED_ROOT = PSRecycleService.RECYCLING_ROOT;
}
