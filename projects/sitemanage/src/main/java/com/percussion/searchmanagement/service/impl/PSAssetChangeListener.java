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
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Interface to allow classes to listen for changes on the page, template or shared assets.
 */
@PSSiteManageBean("assetChangeListener")
public class PSAssetChangeListener implements IPSEditorChangeListener, IPSHandlerInitListener
{
    private static final Logger log = Logger.getLogger(PSAssetChangeListener.class.getName());

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
    public void editorChanged(PSEditorChangeEvent changeEvent)
    {
        if (changeEvent.getActionType() == PSEditorChangeEvent.ACTION_DELETE)
        {
            return;
        }
        
        int contentId = changeEvent.getContentId();
        Set<Integer> pageContentIds = new HashSet<Integer>();
        
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
        Set<Integer> contentIds = new HashSet<Integer>(); 
        
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
