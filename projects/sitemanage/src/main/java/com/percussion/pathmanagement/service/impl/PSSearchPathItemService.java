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

@Component(value="searchPathItemService")
public class PSSearchPathItemService extends PSPathItemService
{

    @Autowired
    public PSSearchPathItemService(IPSFolderHelper folderHelper, IPSIdMapper idMapper,
            IPSItemWorkflowService itemWorkflowService, IPSAssetService assetService,
            IPSWidgetAssetRelationshipService widgetAssetRelationshipService, IPSContentMgr contentMgr,
            IPSWorkflowService workflowService, IPSPageService pageService,  @Qualifier("cm1ListViewHelper")IPSListViewHelper listViewHelper,
            IPSUserService userService)
    {
        super(folderHelper, idMapper, itemWorkflowService, assetService, widgetAssetRelationshipService, contentMgr,
                workflowService, pageService, listViewHelper, userService);
        this.setRootName("Search");
    }

    //TODO: Implement me.
    @Override
    protected String getFolderRoot() throws PSPathServiceException {
        throw new PSPathServiceException("Not implemented");
    }

    @Override
    protected String getFullFolderPath(String path) throws PSPathNotFoundServiceException {
        PSPathUtils.validatePath(path);
        
        String fullFolderPath = SEARCH_ROOT;
        if (!path.equals("/"))
        {
            fullFolderPath = folderHelper.concatPath(fullFolderPath, path);
        }
        
        return fullFolderPath;
    }

    //TODO: Implement me.
    @Override
    protected String getInUsePagesResult() throws PSPathServiceException {
        throw new PSPathServiceException("Not implemented");
    }

    //TODO: Implement me.
    @Override
    protected String getInUseTemplatesResult() throws PSPathServiceException {
        throw new PSPathServiceException("Not implemented");
    }

    //TODO: Implement me.
    @Override
    protected String getNotAuthorizedResult() throws PSPathServiceException {
        throw new PSPathServiceException("Not implemented");
    }
    

    /**
     * Constant for the site root folder path.
     */
    public static final String SEARCH_ROOT_SUB = "/Search";
    public static final String SEARCH_ROOT = "/" + SEARCH_ROOT_SUB;
}
