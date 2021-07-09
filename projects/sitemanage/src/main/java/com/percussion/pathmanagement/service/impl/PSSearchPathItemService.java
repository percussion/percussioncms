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
 *      https://www.percussion.com
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
