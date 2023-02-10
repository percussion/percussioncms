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
package com.percussion.pathmanagement.service.impl;

import com.percussion.designmanagement.service.IPSFileSystemService;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.ui.service.IPSListViewHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link PSFileSystemPathItemService} subclass that points to the "web_resources"
 * directory of the CM1 root dir.
 * 
 * @author miltonpividori
 *
 */
public class PSWebResourcesPathItemService extends PSFileSystemPathItemService
{
    /**
     * @param folderHelper
     */
    public PSWebResourcesPathItemService(IPSFolderHelper folderHelper, IPSFileSystemService fileSystemManagerService,
            IPSListViewHelper listViewHelper)
    {
        super(folderHelper, fileSystemManagerService, listViewHelper);
    }
    
    /* (non-Javadoc)
     * @see com.percussion.pathmanagement.service.impl.PSFileSystemPathItemService#getFullFolderPath(java.lang.String)
     */
    @Override
    protected String getFullFolderPath(String path) throws PSPathNotFoundServiceException {
        PSPathUtils.validatePath(path);
        
        String fullFolderPath = WEB_RESOURCES_ROOT;
        if (!path.equals("/"))
        {
            fullFolderPath = folderHelper.concatPath(fullFolderPath, path);
        }
        
        return fullFolderPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.pathmanagement.service.impl.PSFileSystemPathItemService
     * #findRoot()
     */
    @Override
    protected PSPathItem findRoot() throws PSPathNotFoundServiceException {
        PSPathItem rootItem = findItem("/");
        
        rootItem.setName(rootName);
        
        String fullFolderPath = getFullFolderPath("/");
        rootItem.setFolderPath(fullFolderPath);
        
        // FIXME This "Design" value should not be here. Take a look at PSPathService to know how
        // it's handled.
        rootItem.setFolderPaths(Arrays.asList("//Design"));
        
        Map<String, String> displayProperties = new HashMap<>();
        displayProperties.put(IPSListViewHelper.TITLE_NAME, rootName);
        rootItem.setDisplayProperties(displayProperties);
        
        return rootItem;
    }
    
    // FIXME These values here should not have "Design" in it. However, it doesn't work
    // without it.
    public static final String WEB_RESOURCES_ROOT_SUB = "/Design/web_resources";
    public static final String WEB_RESOURCES_ROOT = "/" + WEB_RESOURCES_ROOT_SUB;
}
