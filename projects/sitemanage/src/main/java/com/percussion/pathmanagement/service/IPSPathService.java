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
package com.percussion.pathmanagement.service;

import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.pathmanagement.data.PSItemByWfStateRequest;
import com.percussion.pathmanagement.data.PSMoveFolderItem;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.data.PSRenameFolderItem;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.IPSNotFoundException;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.ui.service.IPSListViewHelper;

import java.util.List;

/**
 * 
 * Retrieves {@link PSPathItem}s and {@link PSPathItem} children for given paths.
 * Used for UI tree controls like a file browser.
 * <p>
 * The {@link PSPathItem}s returned by the implementation must 
 * have the property {@link PSPathItem#getPath()} never <code>null</code>, empty, or blank.
 * @author adamgent
 *
 */
public interface IPSPathService
{

    public static final String FOLDER_TYPE_FILESYSTEM = "FSFOLDER";

    /**
     * Gets the {@link IPSListViewHelper} implementation for this {@link IPSPathService}.
     * 
     * @see IPSListViewHelper
     * @return An {@link IPSListViewHelper} implementation.
     */
    IPSListViewHelper getListViewHelper();
    
    /**
     * Which user roles are allowed to reach this service. If the service is not
     * allowed for a specific role, then it's not shown in the UI nor reachable.
     * 
     * @return A list of user roles allowed to access the service. May be
     * <code>null</code>, in that case all user roles are allowed.
     */
    List<String> getRolesAllowed();
    
    /**
     * Finds a {@link PSPathItem} for a given path.
     * @param path never <code>null</code>, empty, or blank.
     * @return never <code>null</code>.
     * @throws PSPathNotFoundServiceException If an item cannot be found for the given path or the path is invalid.
     * @throws PSPathServiceException thrown when a path item cannot be created or other system failure.
     */
    PSPathItem find(String path) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException;
    
    /**
     * Finds the children of a given path.
     * Should only work for paths with {@link PSPathItem#isLeaf()} set to <code>false</code>.
     * 
     * @param path never <code>null</code>, empty, or blank.
     * @return never <code>null</code>, maybe empty.
     * @throws PSPathNotFoundServiceException
     * @throws PSPathServiceException
     */
    List<PSPathItem> findChildren(String path) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException;
    
    /**
     * Find item properties for a given path.
     * 
     * @param path the path of the specified item, never blank.
     * 
     * @return the item properties.
     */
    PSItemProperties findItemProperties(String path) throws PSDataServiceException, PSPathServiceException;
    
    /**
     * Find item properties for a given request.
     * 
     * @param request the request to find the item properties by workflow state, never <code>null</code>.
     * 
     * @return the properties for all items identified by the request.
     * @throws PSPathNotFoundServiceException If the request path could not be found.
     * @throws PSPathServiceException If the item properties or workflow could not be found, or other system failure.
     */
    List<PSItemProperties> findItemProperties(PSItemByWfStateRequest request) throws PSPathNotFoundServiceException,
            PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException;

    /**
     * Adds a folder {@link PSPathItem} for a given path.
     * @param path never <code>null</code>, empty, or blank.
     * @return never <code>null</code>.
     * @throws PSPathNotFoundServiceException If the created folder item could not be found.
     * @throws PSPathServiceException If the folder could not be created or other system failure.
     */
    PSPathItem addFolder(String path) throws PSPathNotFoundServiceException, PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException, IPSDataService.DataServiceLoadException;
    
    /**
     * Adds a new folder {@link PSPathItem} as a child of the given folder path or as a sibling of the given item path.
     * The folder will be named 'New Folder.' Additional new folders will be named 'New Folder (n)' with n starting at
     * 2.  The first name 'New Folder (n)' will be used with the first available n value >= 2.
     * @param path never <code>null</code>, empty, or blank.
     * @return never <code>null</code>.
     * @throws PSPathNotFoundServiceException If the created folder item could not be found.
     * @throws PSPathServiceException If the folder could not be created or other system failure.
     */
    PSPathItem addNewFolder(String path) throws PSPathNotFoundServiceException, PSPathServiceException, PSDataServiceException;
    
    /**
     * Renames the folder accordingly as specified by a given {@link PSRenameFolderItem}.
     * @param item never <code>null</code>.
     * @return never <code>null</code>.
     * @throws PSPathNotFoundServiceException If the renamed folder item could not be found.
     * @throws PSPathServiceException If the folder could not be renamed or other system failure.
     * @throws PSBeanValidationException If the item is not valid.
     */
    PSPathItem renameFolder(PSRenameFolderItem item) throws PSPathServiceException,
            PSDataServiceException, PSBeanValidationException;
    
    /**
     * Moves the specified item to the specified folder.
     * 
     * Note, if the item is a page, then both folders must be under a site;
     * otherwise the item is an asset, then both folders must be under the
     * generic "Assets" folder.
     * 
     * @param request it contains both the target folder and the to be moved
     * item.
     * 
     * @return no content object where operation = "moveItem" if the item is
     * successfully moved.
     */    
    PSNoContent moveItem(PSMoveFolderItem request) throws PSDataServiceException, PSPathServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException;
    
    /**
     * Deletes a folder accordingly as specified by a given {@link PSDeleteFolderCriteria}.
     * 
     * @param criteria never <code>null</code>.
     * 
     * @return the number of items cannot be deleted.
     * 
     * @throws PSPathServiceException If the folder could not be deleted or other system failure.
     */
    int deleteFolder(PSDeleteFolderCriteria criteria) throws PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException, IPSDataService.DataServiceLoadException, PSNotFoundException;
    
    /**
     * Validates a folder for deletion by the current user.
     *  
     * @param path the folder path, never blank.
     * 
     * @return string response indicating the reason why all items cannot be deleted or success.  Never blank.
     * 
     * @throws PSPathNotFoundServiceException If the folder path could not be found.
     * @throws PSPathServiceException If the folder could not be validated or other system failure.
     */
    String validateFolderDelete(String path) throws PSPathNotFoundServiceException, PSPathServiceException, IPSDataService.DataServiceNotFoundException, PSValidationException, IPSItemWorkflowService.PSItemWorkflowServiceException, IPSDataService.DataServiceLoadException, PSNotFoundException;
    
    /**
     * Checks for the existence of a path and finds the last portion of the path which exists.  Never blank.
     * 
     * @param path the folder or item path, never blank.
     * 
     * @return last existing path, never <code>null</code> or empty.  This is a relative path (no leading/trailing
     * forward slashes) or empty for root paths ("/Assets" or "/Sites").
     */
    String findLastExistingPath(String path) throws PSPathServiceException;

        
    /**
     * Thrown when a systemic failure occurs in the implementation.
     * @author adamgent
     *
     */
    public static class PSPathServiceException extends Exception {
    
        private static final long serialVersionUID = 1L;
    
        public PSPathServiceException(String message) {
            super(message);
        }
    
        public PSPathServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    
        public PSPathServiceException(Throwable cause) {
            super(cause);
        }
    
    }
    
    /**
     * Thrown when the path is invalid.
     * @author adamgent
     *
     */
    public static class PSPathNotFoundServiceException extends PSPathServiceException implements IPSNotFoundException {

        private static final long serialVersionUID = 1L;

        public PSPathNotFoundServiceException(String message) {
            super(message);
        }

        public PSPathNotFoundServiceException(String message, Throwable cause) {
            super(message, cause);
        }

        public PSPathNotFoundServiceException(Throwable cause) {
            super(cause);
        }

    }
    
    /**
     * Thrown when the name is a reserved word.
     * @author federicoromanelli
     *
     */
    public static class PSReservedNameServiceException extends PSPathServiceException {

        private static final long serialVersionUID = 1L;

        public PSReservedNameServiceException(String message) {
            super(message);
        }

        public PSReservedNameServiceException(String message, Throwable cause) {
            super(message, cause);
        }

        public PSReservedNameServiceException(Throwable cause) {
            super(cause);
        }

    }    
  
}
