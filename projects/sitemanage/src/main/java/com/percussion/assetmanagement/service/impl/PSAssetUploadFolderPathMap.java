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
package com.percussion.assetmanagement.service.impl;

import com.percussion.assetmanagement.data.PSAssetSummary;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.IPSAssetService.PSAssetServiceException;
import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.share.dao.IPSFolderHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * 
 * Responsible for the mapping of Asset content type
 * to Rhythmyx system folder path.
 * <p>
 * The paths specified by {@link #getTypeToFolderPathMap()}
 * property are relative to the root asset folder and can be
 * configured through spring.
 * <p>
 * During runtime the folders are created on demand if
 * they do not already exist.
 * 
 * @author adamgent
 *
 */
public class PSAssetUploadFolderPathMap
{

    /**
     * The name of the uploads folder.
     * @see #getBaseUploadsFolderPath()
     */
    private static final String UPLOADS_FOLDER_NAME = "uploads";
    private IPSFolderHelper folderHelper;
    private Map<String, String> typeToFolderPathMap = new HashMap<>();
    private static String ASSET_ROOT = PSAssetPathItemService.ASSET_ROOT;
    
    public PSAssetUploadFolderPathMap(IPSFolderHelper folderHelper)
    {
        super();
        this.folderHelper = folderHelper;
    }

    /**
     * Finds the upload folder for an asset represented as a legacy folder id.
     * <p>
     * It will use one of the folders that the asset is already saved in if it
     * is in a folder otherwise it will use {@link #getLegacyFolderIdForType(String)}
     * for the assets type.
     * 
     * @param asset never <code>null</code>.
     * @return never <code>null</code>.
     */
    public Number getLegacyFolderIdForAsset(PSAssetSummary asset) throws PSAssetServiceException {
        notNull(asset, "asset");
        Map<String, Number> folderIds = getFolderIdsForPaths(asset.getFolderPaths());
        if ( ! folderIds.isEmpty() )
            return folderIds.entrySet().iterator().next().getValue();
        return getLegacyFolderIdForType(asset.getType());
        
    }
    
    private Map<String,Number> getFolderIdsForPaths(Collection<String> paths) {
        Map<String, Number> pathToFolderId = new HashMap<>();
        if (paths == null) return pathToFolderId;
        for(String p : paths) {
            try
            {
                Number folderId = folderHelper.findLegacyFolderIdFromPath(p);
                pathToFolderId.put(p, folderId);
            }
            catch (Exception e)
            {
                // Skip this folder path.
                log.warn("Bad folder path: " +  p);
            }
        }
        return pathToFolderId;
    }
    
    /**
     * Retrieves the uploads folder associated with an asset type.
     * <strong>The folder will be created if it does not exist.</strong>
     * If the folder cannot be created or used the default folder 
     * path will be used. If that fails an exception is thrown.
     * 
     * @param type never <code>null</code>.
     * @return never <code>null</code>.
     * @throws PSAssetServiceException cannot find a folder path 
     * because even the default path is bad.
     */
    public Number getLegacyFolderIdForType(String type) throws PSAssetServiceException {
        notNull(type);
        String path = getFolderPathForType(type);
        boolean defaultPath = false;
        if (path == null) {
            defaultPath = true;
            path = getDefaultFolderPath();
        }
        
        try
        {
            return getFolderForTypeHelper(path);
        }
        catch (PSAssetServiceException e)
        {
            /*
             * If we are using the default folder path
             * and it does work then we have to fail now.
             */
            if (defaultPath) {
                throw e;
            }
            log.warn("Cannot use folder path for uploading assets: {}" , path);
            /*
             * Try again with the default.
             */
            path = getDefaultFolderPath();
        }
        /*
         * This our second try but now using the default path.
         * This time we will let the failure propagate upwards.
         */
        return getFolderForTypeHelper(path);
    }
    
    private Number getFolderForTypeHelper(String folderPath) 
        throws PSAssetServiceException {
        
        notEmpty(folderPath, "folderPath for type");
        
        /*
         * Create the folder if its not created.
         */
        Number folder;
        try
        {
            folderHelper.createFolder(folderPath);
            folder = folderHelper.findLegacyFolderIdFromPath(folderPath);
        }
        catch (Exception e)
        {
            throw new IPSAssetService.PSAssetServiceException("Failed to get uploads folder", e);
        }
        
        return folder;
    }
    
    
    
    /**
     * Gets the folder path for a given type.
     * @param type never <code>null</code>.
     * @return <code>null</code> if there is no type matching a path.
     */
    protected String getFolderPathForType(String type) {
        notNull(type);
        String path = getTypeToFolderPathMap().get(type);
        if (path == null) return null;
        return folderHelper.concatPath(ASSET_ROOT,  path);
    }
    
    /**
     * The base uploads folder path.
     * @return never <code>null</code> or empty.
     */
    protected String getBaseUploadsFolderPath() {
        return folderHelper.concatPath(ASSET_ROOT, UPLOADS_FOLDER_NAME);
    }
    /**
     * Gets the default upload folder path.
     * @return never <code>null</code>.
     */
    protected String getDefaultFolderPath() {
        return getBaseUploadsFolderPath();
    }
    

    public Map<String, String> getTypeToFolderPathMap()
    {
        return typeToFolderPathMap;
    }

    public void setTypeToFolderPathMap(Map<String, String> typeForFolderPath)
    {
        this.typeToFolderPathMap = typeForFolderPath;
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSAssetUploadFolderPathMap.class);
    
    
}

