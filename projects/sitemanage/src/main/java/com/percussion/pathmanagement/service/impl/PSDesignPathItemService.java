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

import com.percussion.pathmanagement.data.PSFolderPermission;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.recycle.service.IPSRecycleService;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.ui.service.IPSListViewHelper;
import com.percussion.ui.service.IPSUiService;
import com.percussion.user.service.IPSUserService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Handles requests for path items under the "Design" root folder.
 * 
 * @author miltonpividori
 *
 */
@Component("designPathItemService")
public class PSDesignPathItemService extends PSDispatchingPathService
{
    private String rootName;
    private IPSFolderHelper folderHelper;
    private IPSRecycleService recycleService;

    /**
     *
     * @param folderHelper
     * @param uiService
     * @param userService
     * @param defaultListViewHelper
     * @param recycleService
     */
    public PSDesignPathItemService(IPSFolderHelper folderHelper,
            IPSUiService uiService, IPSUserService userService,
            @Qualifier("fileSystemListViewHelper") IPSListViewHelper defaultListViewHelper,
            IPSRecycleService recycleService)
    {
        super(uiService, userService, defaultListViewHelper, recycleService, folderHelper);
        this.folderHelper = folderHelper;
        this.setRootName("Design");
    }
    
    public String getRootName()
    {
        return rootName;
    }

    public void setRootName(String rootName)
    {
        this.rootName = rootName;
    }

    @Override
    protected PSPathItem findRoot() throws PSPathNotFoundServiceException {
        PSPathItem root = new PSPathItem();
        root.setName(rootName);
        root.setPath("/");
        root.setLeaf(false);
        root.setFolderPath(getFullFolderPath("/"));
        root.setAccessLevel(PSFolderPermission.Access.ADMIN);
        return root;
    }
    
    protected String getFullFolderPath(String path) throws PSPathNotFoundServiceException {
        PSPathUtils.validatePath(path);
        
        String fullFolderPath = getRootFolderPath();
        if (!path.equals("/"))
        {
            fullFolderPath = folderHelper.concatPath(fullFolderPath, path);
        }
        
        return fullFolderPath;
    }
    
    protected String getRootFolderPath()
    {
        return "//" + getRootName();
    }
}
