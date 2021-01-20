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
    protected String getFullFolderPath(String path)
    {
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
    protected PSPathItem findRoot()
    {
        PSPathItem rootItem = findItem("/");
        
        rootItem.setName(rootName);
        
        String fullFolderPath = getFullFolderPath("/");
        rootItem.setFolderPath(fullFolderPath);
        
        // FIXME This "Design" value should not be here. Take a look at PSPathService to know how
        // it's handled.
        rootItem.setFolderPaths(Arrays.asList("//Design"));
        
        Map<String, String> displayProperties = new HashMap<String, String>();
        displayProperties.put(IPSListViewHelper.TITLE_NAME, rootName);
        rootItem.setDisplayProperties(displayProperties);
        
        return rootItem;
    }
    
    // FIXME These values here should not have "Design" in it. However, it doesn't work
    // without it.
    public static final String WEB_RESOURCES_ROOT_SUB = "/Design/web_resources";
    public static final String WEB_RESOURCES_ROOT = "/" + WEB_RESOURCES_ROOT_SUB;
}
