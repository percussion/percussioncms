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

import static org.apache.commons.lang.Validate.notEmpty;
import static org.springframework.util.StringUtils.trimLeadingCharacter;
import static org.springframework.util.StringUtils.trimTrailingCharacter;

import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.designmanagement.service.IPSFileSystemService;
import com.percussion.designmanagement.service.IPSFileSystemService.PSExistingFolderException;
import com.percussion.designmanagement.service.IPSFileSystemService.PSFolderNameLengthLimitException;
import com.percussion.designmanagement.service.IPSFileSystemService.PSFolderOperationException;
import com.percussion.designmanagement.service.IPSFileSystemService.PSInvalidCharacterInFolderNameException;
import com.percussion.designmanagement.service.IPSFileSystemService.PSInvalidFolderNameException;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.pathmanagement.data.PSFolderPermission;
import com.percussion.pathmanagement.data.PSItemByWfStateRequest;
import com.percussion.pathmanagement.data.PSMoveFolderItem;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.data.PSRenameFolderItem;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.PSDateUtils;
import com.percussion.share.data.IPSItemSummary.Category;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSBeanValidationUtils;
import com.percussion.ui.service.IPSListViewHelper;
import com.percussion.util.PSCharSets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link IPSPathService} implementation that handles requests to URL that
 * maps to the file system.
 * 
 * @author miltonpividori.
 *
 */
public abstract class PSFileSystemPathItemService implements IPSPathService
{
    /**
     * Type used for {@link PSPathItem} objects that represent a file in the file system.
     */
    public static final String FILE_SYSTEM_FILE_TYPE = "FSFile";

    /**
     * Type used for {@link PSPathItem} objects that represent a folder in the file system.
     */
    public static final String FILE_SYSTEM_FOLDER_TYPE = "FSFolder";

    /**
     * Constant for the response given when validation of a folder for delete is
     * successful.
     */
    public static final String VALIDATE_SUCCESS = "Success";
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSFileSystemPathItemService.class);
    
    // FIXME We should not use this class directly.
    private PSItemDefManager itemDefManager = PSItemDefManager.getInstance();
    
    protected IPSListViewHelper listViewHelper;
    
    protected IPSFileSystemService fileSystemService;
    
    protected String rootName;
    
    /**
     * Used for folder item operations. Initialized in ctor, never
     * <code>null</code> after that.
     */
    protected IPSFolderHelper folderHelper;
    
    public PSFileSystemPathItemService(IPSFolderHelper folderHelper,
            IPSFileSystemService fileSystemManagerService, IPSListViewHelper listViewHelper)
    {
        this.folderHelper = folderHelper;
        this.fileSystemService = fileSystemManagerService;
        this.listViewHelper = listViewHelper;
    }

    public IPSListViewHelper getListViewHelper()
    {
        return listViewHelper;
    }
    
    public void setListViewHelper(IPSListViewHelper listViewHelper)
    {
        this.listViewHelper = listViewHelper;
    }
    
    public String getRootName()
    {
        return rootName;
    }

    public void setRootName(String rootName)
    {
        this.rootName = rootName;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.pathmanagement.service.IPSPathService#getRolesAllowed()
     */
    public List<String> getRolesAllowed()
    {
        // By default, any role is allowed access URL served by this IPSPathService
        // implementation.
        return null;
    }
    
    /**
     * Given a folder path, it translates the path to an internal file system
     * representation, gets the children and returns a List of File objects.
     * 
     * @param path The path of the folder. Assumed not <code>null</code>.
     * @return A list of children of the folder path represented by 'path'.
     */
    private List<File> getChildren(String path)
    {
        try
        {
            return fileSystemService.getChildren(path);
        }
        catch (FileNotFoundException e)
        {
            return new ArrayList<File>();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.pathmanagement.service.IPSPathService#find(java.lang.String)
     */
    public PSPathItem find(String path) throws PSPathNotFoundServiceException, PSPathServiceException
    {
        log.debug("Find root of path: " + path);
        if ("/".equals(path))
            return findRoot();
        
        return findItem(path);
    }
    
    /**
     * Returns the {@link PSPathItem} associated with the given path.
     * 
     * @param path
     * @return
     */
    protected PSPathItem findItem(String path)
    {
        notEmpty(path);
        
        File file = fileSystemService.getFile(path);
        
        if (!file.exists())
            throw new PSPathNotFoundServiceException("The path doesn't exist: " + path);
        
        String parentPath = null;
        if ("/".equals(path))
            parentPath = "/";
        else
        {
            if (path.endsWith("/"))
                parentPath = "/" + FilenameUtils.getPath(path.substring(0, path.length() - 1));
            else
                parentPath = "/" + FilenameUtils.getPath(path);
        }
        
        if (!parentPath.endsWith("/"))
            parentPath += parentPath + "/";
        
        PSPathItem item = getPathItemFromFile(parentPath, file);
        
        // This method always adds a slash at the end of the path, never mind if
        // it's a folder or a file.
        if (!item.getPath().endsWith("/"))
            item.setPath(item.getPath() + "/");
        
        return item;
    }

    @Override
    public List<PSPathItem> findChildren(String path) throws PSPathNotFoundServiceException, PSPathServiceException
    {
        List<File> children = getChildren(path);
        List<PSPathItem> folderPathItems = new ArrayList<PSPathItem>();
        List<PSPathItem> filePathItems = new ArrayList<PSPathItem>();
        
        for (File child : children)
        {
            if (child.isDirectory())
                folderPathItems.add(getPathItemFromFile(path, child));
            else if (!PSPathOptions.folderChildrenOnly())
                filePathItems.add(getPathItemFromFile(path, child));
        }
        
        Collections.sort(folderPathItems, PSPathItemComparator.getInstance());
        Collections.sort(filePathItems, PSPathItemComparator.getInstance());
        
        folderPathItems.addAll(filePathItems);
        
        return folderPathItems;
    }

    /**
     * @param child
     * @return
     */
    private PSPathItem getPathItemFromFile(String parentPath, File child)
    {
        // parent path should be a folder
        if(fileSystemService.getFile(parentPath).isFile())
        {
            parentPath = fileSystemService.getParentFolder(parentPath);
        }
        
        PSPathItem item = new PSPathItem();
        item.setName(fileSystemService.getNameFromFile(child));
        
        item.setId(generatePathItemId(child));
        
        if (child.isDirectory())
            item.setType(FILE_SYSTEM_FOLDER_TYPE);
        else
            item.setType(FILE_SYSTEM_FILE_TYPE);
        
        item.setIcon(getIcon(child));
        
        String itemPath = parentPath + child.getName();
        if (!itemPath.endsWith("/") && child.isDirectory())
            itemPath += "/";
        
        item.setPath(itemPath);
        
        item.setFolderPath(folderHelper.concatPath(getFullFolderPath(parentPath), item.getName()));
        item.setFolderPaths(Arrays.asList(FilenameUtils.getFullPathNoEndSeparator(item.getFolderPath())));
        item.setCategory(Category.SYSTEM);
        item.setRevisionable(false);
        
        item.setLeaf(!child.isDirectory());
        item.setAccessLevel(PSFolderPermission.Access.ADMIN);
        
        item.setRelatedObject(child);
        
        return item;
    }

    /**
     * Generates the id of the path item from its path. It uses the <code>hashCode</code> method.
     * 
     * @param file the path item for which we want to generate the id
     * 
     * @return a String with the id of the item.
     */
    private String generatePathItemId(File file)
    {
        return Integer.valueOf(file.getPath().hashCode()).toString();
    }
    
    /**
     * Given a File object, it returns the associated extension. It also
     * checks if the File object points to a folder, in that case it returns
     * the correct icon.
     * 
     * @return The URL for the icon associated to the given file extension.
     */
    private String getIcon(File file)
    {
        if (file.isDirectory())
            return "/Rhythmyx/sys_resources/images/finderFolder.png";
        
        Properties rxProps = itemDefManager.getRxFileIconProperties();
        Properties sysProps = itemDefManager.getSysFileIconProperties();
        
        String fileExtension = FilenameUtils.getExtension(file.getName()).toLowerCase();
        
        String iconFn = rxProps.getProperty(fileExtension);
        iconFn = getIconPath(iconFn, false);
        
        //Get it from system properties if it is blank
        if (StringUtils.isBlank(iconFn))
        {
           iconFn = sysProps.getProperty(fileExtension);
           iconFn = getIconPath(iconFn, true);
        }
        
        return iconFn;
    }
    
    /**
     * Given a file extension, it returns the icon URL associated.
     * 
     * @param extension The file extension.
     * @param isSys
     * @return The URL for the icon associated to the given file extension.
     */
    private String getIconPath(String fileExtension, boolean isSys)
    {
        String iconPath = itemDefManager.getFullIconPath(fileExtension, isSys);
        
        if (iconPath == null)
            return StringUtils.EMPTY;
        
        // FIXME This shouldn't be hardcoded
        return "/Rhythmyx" + iconPath.substring(2);
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.pathmanagement.service.IPSPathService#findItemProperties(java.lang.String)
     */
    @Override
    public PSItemProperties findItemProperties(String path)
    {
        File file = fileSystemService.getFile(path);
        
        PSItemProperties itemProperties = new PSItemProperties();
        itemProperties.setId(file.getName());
        itemProperties.setName(file.getName());
        itemProperties.setSize(String.valueOf(file.length()));
        itemProperties.setLastModifiedDate(PSDateUtils.getDateToString(new Date(file.lastModified())));
        itemProperties.setPath(path);
        
        return itemProperties;
    }

    @Override
    public List<PSItemProperties> findItemProperties(PSItemByWfStateRequest request)
            throws PSPathNotFoundServiceException, PSPathServiceException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public PSPathItem addFolder(String path) throws PSPathNotFoundServiceException, PSPathServiceException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public PSPathItem addNewFolder(String path) throws PSPathNotFoundServiceException, PSPathServiceException
    {
        try
        {
            File newFolder = fileSystemService.addFolder(path);
        
            return getPathItemFromFile(path, newFolder);
        }
        catch (IOException e)
        {
            throw new PSPathServiceException(e);
        }
    }

    @Override
    public PSPathItem renameFolder(PSRenameFolderItem item) throws PSBeanValidationException
    {
        PSBeanValidationException errors = PSBeanValidationUtils.validate(item);
        errors.throwIfInvalid();
        
        try
        {
            File newFolder = fileSystemService.renameFolder(item.getPath(), item.getName());
            
            return getPathItemFromFile(fileSystemService.getParentFolder(item.getPath()), newFolder);
        }
        catch (PSFolderNameLengthLimitException e)
        {
            errors.rejectValue("name", "renameFolderItem.longName",
                    "Cannot rename folder '<old_name>' to '<new_name>' because that name exceeds character limit.");
            throw errors;
        }
        catch (PSInvalidFolderNameException e)
        {
            errors.rejectValue("name", "renameFolderItem.reservedName",
                    "Cannot rename folder '<old_name>' to '<new_name>' because that is a reserved folder name.");
            throw errors;
        }
        catch (PSExistingFolderException e)
        {
            errors.rejectValue("name", "renameFolderItem.duplicatedName",
                    "Cannot rename folder '<old_name>' to '<new_name>' because there is another folder or file with that name.");
            throw errors;
        }
        catch (PSInvalidCharacterInFolderNameException e)
        {
            errors.rejectValue("name", "renameFolderItem.invalidCharInName",
                    "Cannot rename folder '<old_name>' to '<new_name>' because folder names cannot contain the following characters: "
                            + e.getInvalidChars());
            throw errors;
        }
        catch (PSFolderOperationException e)
        {
            // FIXME Fix the message here
            errors.rejectValue("name", "renameFolderItem.unknownCause",
                    "Unknown problem when renaming the folder.");
            throw errors;
        }
    }

    @Override
    public PSNoContent moveItem(PSMoveFolderItem request)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int deleteFolder(PSDeleteFolderCriteria criteria) throws PSPathServiceException
    {
        try
        {
            fileSystemService.deleteFolder(criteria.getPath());
            
            return 0;
        }
        catch (IOException e)
        {
            throw new PSPathServiceException("An error ocurred when deleting the folder '" 
                    + getFolderName(criteria) + "'. Some files or folders may not have been deleted.");
        }
    }

    /**
     * @param criteria
     * @return
     */
    private String getFolderName(PSDeleteFolderCriteria criteria)
    {
        String[] paths = criteria.getPath().split("/");
        
        if(paths[paths.length - 1] != "")
        {
            return paths[paths.length - 1];
        }
        else 
        {
            return paths[paths.length - 2];
        }
    }

    @Override
    public String validateFolderDelete(String path) throws PSPathNotFoundServiceException, PSPathServiceException
    {
        notEmpty(path, "path");
        
        String response = "";

        // validate that the folder we are about to delete is below the 'themes' folder
        String auxPath = trimLeadingCharacter(trimTrailingCharacter(path, '/'), '/');
        String paths[] = auxPath.split("/");
        if(paths.length < 2)
        {
            // it means that we only have the themes folder, so it can't be deleted
            response = "VALIDATE_ERROR_NOT_UNDER_THEMES";
        }
        
        return (StringUtils.isEmpty(response))? VALIDATE_SUCCESS : response;
    }

    @Override
    public String findLastExistingPath(String path)
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Returns the root element of this {@link IPSPathService} implementation.
     * 
     * @return A {@link PSPathItem} object representing the root element. Never
     * <code>null</code>.
     */
    protected abstract PSPathItem findRoot();
    
    /**
     * Generates the full internal folder path for the specified relative path.
     * This path is used for all item lookup operations.
     * 
     * @param path the path identifying a relative location of an item or folder
     *            in the system.
     * 
     * @return the complete folder path used for item lookup. Never
     *         <code>null</code> or empty.
     */
    protected abstract String getFullFolderPath(String path);
}
