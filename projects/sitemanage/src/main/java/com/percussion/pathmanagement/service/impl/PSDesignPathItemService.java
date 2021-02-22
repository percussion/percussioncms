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
