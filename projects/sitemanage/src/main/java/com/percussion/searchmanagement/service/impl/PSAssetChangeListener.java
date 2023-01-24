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

package com.percussion.searchmanagement.service.impl;

import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.cms.IPSEditorChangeListener;
import com.percussion.cms.PSEditorChangeEvent;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.searchmanagement.service.IPSPageIndexService;
import com.percussion.server.IPSHandlerInitListener;
import com.percussion.server.IPSRequestHandler;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Interface to allow classes to listen for changes on the page, template or shared assets.
 */
@PSSiteManageBean("assetChangeListener")
public class PSAssetChangeListener implements IPSEditorChangeListener, IPSHandlerInitListener
{
    private static final Logger log = LogManager.getLogger(PSAssetChangeListener.class.getName());

    @Autowired
    public PSAssetChangeListener(IPSWorkflowHelper workflowHelper, 
            IPSWidgetAssetRelationshipService widgetAssetRelationshipService,IPSIdMapper idMapper,
            IPSPageIndexService indexService)
    {
        this.workflowHelper = workflowHelper;
        this.widgetAssetRelationshipService = widgetAssetRelationshipService;
        this.idMapper = idMapper;
        this.pageIndexService = indexService;
        // Register a listener with PSServer
        PSServer.addInitListener(this);
    }
    
    /**
     * Called to notify listeners when there is a change on a page, template or shared asset 
     * @param changeEvent The change event object, never <code>null</code>.
     */
    public void editorChanged(PSEditorChangeEvent changeEvent) throws PSValidationException {
        if (changeEvent.getActionType() == PSEditorChangeEvent.ACTION_DELETE)
        {
            return;
        }
        
        int contentId = changeEvent.getContentId();
        Set<Integer> pageContentIds = new HashSet<>();
        
        IPSGuid myGuid = PSGuidUtils.makeGuid(contentId, PSTypeEnum.LEGACY_CONTENT);
        String myGuidStr =  idMapper.getString(myGuid);
        
        if(workflowHelper.isTemplate(myGuidStr))
        {
            pageContentIds.add(myGuid.getUUID());
        }
        
        try
        {
            if(workflowHelper.isAsset(myGuidStr))
            {
                //Find owners of the asset
                if(changeEvent.getActionType() == PSEditorChangeEvent.ACTION_INSERT || 
                        changeEvent.getActionType() == PSEditorChangeEvent.ACTION_UPDATE)
                    pageContentIds = getAssetOwners(myGuidStr);
            }
        }
        catch (PSNotFoundException e)
        {
            log.error("Error notifying listeners for asset change with id: " + myGuidStr, e);
        }
        
        if (!pageContentIds.isEmpty())
        {
            pageIndexService.index(pageContentIds);
        }
    }
    
    // see IPSHandlerInitListener interface
    public void initHandler(IPSRequestHandler requestHandler)
    {
       if (requestHandler instanceof PSContentEditorHandler)
       {
          PSContentEditorHandler ceh = (PSContentEditorHandler)requestHandler;
          ceh.addEditorChangeListener(this);
       }
    }
    
    @Override
    public void shutdownHandler(IPSRequestHandler requestHandler)
    {
         
    }

    /**
     * Finds out owners of the provided assetId and returns set of content ids of the owners.
     * 
     * @param assetId
     * @return set of content ids
     */
    private Set<Integer> getAssetOwners(String assetId)
    {
        Set<Integer> contentIds = new HashSet<>();
        
        Set<String> owners = widgetAssetRelationshipService.getRelationshipOwners(assetId);
        for (String owner : owners)
        {
            contentIds.add(new Integer(idMapper.getGuid(owner).getUUID()));
        }
        
        return contentIds;
    }
    
    /**
     * The workflowHelper, initialized by constructor, never <code>null</code> after that.
     */
    private IPSWorkflowHelper workflowHelper;
    
    /**
     * The widgetAssetRelationshipService, initialized by constructor, never <code>null</code> after that.
     */
    private IPSWidgetAssetRelationshipService widgetAssetRelationshipService;
    
    /**
     * The idMapper, initialized by constructor, never <code>null</code> after that.
     */
    private IPSIdMapper idMapper;
    
    /**
     * The page index service, intialized by constructor, never <code>null</code> after that.
     */
    private IPSPageIndexService pageIndexService;
}
