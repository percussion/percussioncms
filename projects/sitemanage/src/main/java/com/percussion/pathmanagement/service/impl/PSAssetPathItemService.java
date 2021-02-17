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

import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.ui.service.IPSListViewHelper;
import com.percussion.user.service.IPSUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Path item service used for asset item lookup.
 */
@Component(value="assetPathItemService")
public class PSAssetPathItemService extends PSPathItemService
{
    @Autowired
    public PSAssetPathItemService(IPSFolderHelper folderHelper, IPSIdMapper idMapper,
            IPSItemWorkflowService itemWorkflowService, IPSAssetService assetService,
            IPSWidgetAssetRelationshipService widgetAssetRelationshipService, IPSContentMgr contentMgr,
            IPSWorkflowService workflowService, IPSPageService pageService,  @Qualifier("cm1ListViewHelper") IPSListViewHelper listViewHelper,
            IPSUserService userService)
    {
        super(folderHelper, idMapper, itemWorkflowService, assetService, widgetAssetRelationshipService,
                contentMgr, workflowService, pageService, listViewHelper, userService);
        this.setRootName("Assets");
    }

    @Override
    protected String getFullFolderPath(String path) throws PSPathNotFoundServiceException {
        PSPathUtils.validatePath(path);
        
        String fullFolderPath = ASSET_ROOT;
        if (!path.equals("/"))
        {
            fullFolderPath = folderHelper.concatPath(fullFolderPath, path);
        }
        
        return fullFolderPath;
    }
  
    @Override
    protected String getInUsePagesResult()
    {
        return ASSETS_IN_USE_PAGES;        
    }
    
    @Override
    protected String getNotAuthorizedResult()
    {
        return ASSETS_NOT_AUTHORIZED;        
    }
    
    @Override
    protected String getInUseTemplatesResult()
    {
        return ASSETS_IN_USE_TEMPLATES;        
    }
    
    @Override
    protected String getFolderRoot()
    {
        return ASSET_ROOT;
    }
    
    /**
     * Constant for the asset root folder path.
     */
    
    public static final String ASSET_ROOT_SUB = "//Folders/$System$";
    public static final String ASSET_ROOT = ASSET_ROOT_SUB + "/Assets";
    
    /**
     * Constant for the response given when a folder contains in use assets.
     */
    public static final String ASSETS_IN_USE_PAGES = "AssetsInUsePages";
    
    /**
     * Constant for the response given when a user is not authorized to delete assets in a folder.
     */
    public static final String ASSETS_NOT_AUTHORIZED = "AssetsNotAuthorized";
    
    /**
     * Constant for the response given when a folder contains assets that are in use by templates.
     */
    public static final String ASSETS_IN_USE_TEMPLATES = "AssetsInUseTemplates";
}
